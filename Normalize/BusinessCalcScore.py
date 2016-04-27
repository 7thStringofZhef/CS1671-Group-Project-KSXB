import csv


# get all business information
def get_bus_ratings():
    business = {}
    with open('yelp_academic_dataset_business.csv', 'rt') as csv_file:
        reader = csv.reader(csv_file)
        first = True
        for row in reader:
            if first:
                first = False
            else:
                # also grabs business review count in case needed later
                business.update({row[0]: [row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]]})

    return business


# sums all sentiment scores and review counts for each business
def get_bus_sentiment_scores():
    business_revs = {}
    with open('pittsburgh_review_sentiment_normalize.csv', 'rt') as csv_file:
        reader = csv.reader(csv_file)
        first = True
        for row in reader:

            if first:
                first = False
            else:
                bus_id = row[1]
                norm_score = float(row[5])
                if bus_id in business_revs:
                    business_revs[bus_id][0] += norm_score
                    business_revs[bus_id][1] += 1
                else:
                    business_revs.update({bus_id: [norm_score, 1]})
    return business_revs


def round_to(n, precision):
    correction = 0.5 if n >= 0 else -0.5
    return int(n/precision+correction) * precision


# method to round to nearest 0.5
# http://stackoverflow.com/questions/4265546/python-round-to-nearest-05
def round_to_5(n):
    return round_to(n, 0.5)


# Main method
def main():
    # 'yelp_academic_dataset_business.csv''pittsburgh_review_sentiment_normalize.csv'
    wf = open('businesses_with_score.csv', 'w', newline='')
    writer = csv.writer(wf)

    # header row
    writer.writerow(["business_id", "full_address", "latitude", "longitude", "stars", "review_count", "categories", "attributes", "neighborhood", "sentiment_stars"])

    # get all business scores (gold standard)
    business_ratings = get_bus_ratings()

    # get all sentiment review scores and store counts for each business
    business_scores = get_bus_sentiment_scores()

    #iterate over businesses
    businesses = business_scores.keys()
    for id in businesses:
        bus_add = business_ratings[id][0]
        bus_lat = business_ratings[id][1]
        bus_lon = business_ratings[id][2]
        bus_star = business_ratings[id][3]
        bus_cat = business_ratings[id][5]
        bus_att = business_ratings[id][6]
        bus_neigh = business_ratings[id][7]

        rev_sum = business_scores[id][0]
        rev_cnt = business_scores[id][1]
        # calc average sentiment score
        sent_avg = float(rev_sum) / float(rev_cnt)
        sent_avg = round_to_5(sent_avg)
        writer.writerow([id, bus_add, bus_lat, bus_lon, bus_star, rev_cnt, bus_cat, bus_att, bus_neigh, sent_avg])

if __name__ == '__main__':
    main()