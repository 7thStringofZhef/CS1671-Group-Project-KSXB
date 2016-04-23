import csv
import random
import collections
import nltk.classify.util, nltk.metrics
from nltk.classify import NaiveBayesClassifier
import itertools
from nltk.collocations import BigramCollocationFinder
from nltk.metrics import BigramAssocMeasures
from nltk.tokenize import sent_tokenize
from nltk.tokenize import word_tokenize

def bigram_word_feats(words, score_fn=BigramAssocMeasures.chi_sq, n=300):
	try:
		bigram_finder = BigramCollocationFinder.from_words(words)
		bigrams = bigram_finder.nbest(score_fn, n)
		return dict([(ngram, True) for ngram in itertools.chain(words, bigrams)])
	except ZeroDivisionError:
		return dict([(word, True) for word in words])

def word_feats(words):
	return dict([(word, True) for word in words])
	

review = []
trainfeats = []
#Extract all reviews
with open('pittsburgh_reviews.csv', 'rt') as csvfile:
	reader = csv.reader(csvfile)
	rownum = 0
	for row in reader:
		if rownum != 0:
			col = row[3]
			review.append([col, row[2]])
			
		rownum+=1
	
	
#Choose 5000 random reviews to sample
samples = random.sample(review, 5000)	
testSamples = random.sample(review, 1500)

#word tokenize these reviews, get their features
for x in range(0,5000):
	samples[x][0] = word_tokenize(samples[x][0])
	trainfeats.append((bigram_word_feats(samples[x][0]), samples[x][1]))
	
classifier = NaiveBayesClassifier.train(trainfeats)

fileToWrite = open("output.txt", "w")
i = 0;
for i in range(0, len(review)):
	if type(review[i][0]) is str:	
		observed = classifier.classify(bigram_word_feats(word_tokenize(review[i][0])))
	else:
		observed = classifier.classify(bigram_word_feats(review[i][0]))
		
	fileToWrite.write(observed)
	fileToWrite.write("\r\n")
	i+=1
	if i % 1000 == 0:
		print("Iteration ", i, "\n")
	
fileToWrite.close()
	
	
