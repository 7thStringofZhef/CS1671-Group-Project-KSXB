import csv

# get all user information
def get_user_info():
    users = {}
    with open('yelp_academic_dataset_user.csv', 'rt') as csv_file:
        reader = csv.reader(csv_file)
        first = True
        for row in reader:
            if first:
                first = False
            else:
                users.update({row[0]: [row[1], row[2]]})

    return users


# simple method to calculate normalization using user average review score
# (method just to single out normalization calculation if want to change later)
def user_normalize(rev_sentiment, user_avg_score):

    # cast to float
    user_avg_score = float(user_avg_score)
    rev_sentiment = float(rev_sentiment)

    # if 5 or 1, we know they always give 5 or 1, so normalize score to = user avg
    if user_avg_score == 5.0 or user_avg_score == 1.0:
        return user_avg_score
    else:
        # get "polarity" of sentiment compared to user avg
        normalized = float(rev_sentiment) - float(user_avg_score)
        # calculate normalized difference
        normalized /= 2
        normalized *= -1
        # add normalized change to original sentiment
        normalized += rev_sentiment

        # simple check (don't think this is necessary, but w/e)
        if normalized < 1:
            normalized = 1
        elif normalized > 5:
            normalized = 5
        return int(round(normalized))


# Main method -> just
def main():

	# new output file
    wf = open('pittsburgh_review_sentiment_normalize.csv', 'w', newline='')
    writer = csv.writer(wf)

    # get all user information
    user_info = get_user_info()

    # compute sentiment score normalization using specified sentimnet file
    with open('pittsburgh_reviews_with_sentiment.csv', 'rt') as csv_file:
        reader = csv.reader(csv_file)
        first = True
        cnt = 0
        for row in reader:
            if first:
                writer.writerow([row[0], row[1], row[2], row[4], "User Avg", "Normalized"])
                first = False
            else:
                user_id = row[0]
                sentiment = row[4]
                user_avg = user_info[user_id][1]
                normalized_score = user_normalize(sentiment, user_avg)
                writer.writerow([user_id, row[1], row[2], sentiment, user_avg, normalized_score])

if __name__ == '__main__':
    main()