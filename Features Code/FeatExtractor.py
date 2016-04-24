# CS1671 Group Project
# Feature Extractor - tags given review sentences with specified tags, then searches for mostly SUBJECTIVE features
# coding=UTF-8

# uses punkt tokenizer (Model)
# uses brown (Corpora)
# these can be downloaded using nltk.downloader():
#       source: http://www.nltk.org/data.html

# installation: need PyYAML and NLTK files:
#       http://www.lfd.uci.edu/~gohlke/pythonlibs/#pyyaml

# if need help installing wheel files:
#       http://stackoverflow.com/questions/27885397/how-do-i-install-a-python-package-with-a-whl-file

# This is a fast and simple noun phrase extractor (uses NLTK)
#   http://streamhacker.com/2008/11/03/part-of-speech-tagging-with-nltk-part-1/
#   http://thetokenizer.com/2013/05/09/efficient-way-to-extract-the-main-topics-of-a-sentence/


# uses POS tagger (MT)
# uses Bigrams,
# This is our fast Part of Speech tagger
##############################################################################################################

import nltk
from nltk.corpus import brown
import csv

brown_train = brown.tagged_sents(categories='news')
regexp_tagger = nltk.RegexpTagger(
    [(r'^-?[0-9]+(.[0-9]+)?$', 'CD'),
     (r'(-|:|;)$', ':'),
     (r'\'*$', 'MD'),
     (r'(The|the|A|a|An|an)$', 'AT'),
     (r'.*able$', 'JJ'),
     (r'^[A-Z].*$', 'NNP'),
     (r'.*ness$', 'NN'),
     (r'.*ly$', 'RB'),
     (r'.*s$', 'NNS'),
     (r'.*ing$', 'VBG'),
     (r'.*ed$', 'VBD'),
     (r'.*', 'NN')
])
unigram_tagger = nltk.UnigramTagger(brown_train, backoff=regexp_tagger)
bigram_tagger = nltk.BigramTagger(brown_train, backoff=unigram_tagger)

# basic 'CFG' to help match correct phrases (features)
# this is just like linked example above
cfg = {}
cfg["NN+NN"] = "NNI"
cfg["NNI+NN"] = "NNI"
cfg["JJ+JJ"] = "JJ"
cfg["JJ+NN"] = "NNI"


# custom class to use NLTK lib functions
class FeatureExtractor(object):

    def __init__(self, sentence):
        self.sentence = sentence

    # Split the sentence into singlw words/tokens
    def tokenize_sentence(self, sentence):
        tokens = nltk.word_tokenize(sentence)
        return tokens

    # Normalize brown corpus' tags ("NN", "NN-PL", "NNS" => "NN")
    # too many tags, simplify what we are trying to do
    def normalize_tags(self, tagged):
        n_tagged = []
        for tag in tagged:
            if tag[1] == "NP-TL" or tag[1] == "NP":
                n_tagged.append((tag[0], "NNP"))
                continue
            if tag[1].endswith("-TL"):
                n_tagged.append((tag[0], tag[1][:-3]))
                continue
            if tag[1].endswith("S"):
                n_tagged.append((tag[0], tag[1][:-1]))
                continue
            n_tagged.append((tag[0], tag[1]))
        return n_tagged

    # Extract features from given sentence
    def extract_feats(self):

        # tokenize current sentence + get tags from tokenized sentence
        sen_tokens = self.tokenize_sentence(self.sentence)
        sen_tags = self.normalize_tags(bigram_tagger.tag(sen_tokens))

        # check if any tags/tag sequences match tags in CFG
        merge = True
        while merge:
            merge = False
            for x in range(0, len(sen_tags) - 1):
                tag1 = sen_tags[x]
                tag2 = sen_tags[x + 1]
                cfg_key = "%s+%s" % (tag1[1], tag2[1])
                cfg_value = cfg.get(cfg_key, '')
                # tag match found, save word + tag
                if cfg_value:
                    merge = True
                    sen_tags.pop(x)
                    sen_tags.pop(x)
                    tok_match = "%s %s" % (tag1[0], tag2[0])
                    pos_tag = cfg_value
                    sen_tags.insert(x, (tok_match, pos_tag))
                    break

        # get all words that matched specified cfg tags = features
        feats = []
        for t in sen_tags:
            if t[1] == "NNI":
                feats.append(t[0])
        return feats


# Main method -> jsut
def main():

    wf = open('pitt_features.csv', 'w', newline='')
    csv_wr = csv.writer(wf)

    with open('pittsburgh_reviews.csv', 'rt') as csv_file:
        reader = csv.reader(csv_file)

        # cnt = 0
        first = True
        for row in csv_file:

            sentences = row[3].split(" </s> ")
            if first:
                csv_wr.writerow([row[0], row[1], row[2], "features"])
                first = False
            else:
                feat = []
                for sent in sentences:
                    # just a bit more 'pre-processing' (strangely faster than using regex?)
                    sent = sent.replace("<s>", " ")
                    sent = sent.replace("</s>", " ")
                    sent = sent.replace("~", "")
                    sent = sent.replace(",", "")
                    sent = sent.replace("'", "")
                    sent = sent.replace("[", "")
                    sent = sent.replace("]", "")
                    sent = sent.strip().lower()

                    # print("sentence: ", sent)

                    np_extractor = FeatureExtractor(sent)
                    result = np_extractor.extract_feats()
                    feat.extend(result)

                # print("feat: ", cnt, feat)
                csv_wr.writerow([row[0], row[1], row[2], feat])

           # cnt = cnt + 1
           # if cnt == 20:
               # break

if __name__ == '__main__':
    main()
