import csv
import sys


# Script takes optional arguments. If no args given, uses hard coded args in main
# args order: business_with_score.csv neighborhood category output_file.csv

# get all business information
def get_bus_info(in_file, neigh, cat):
    business = {}
    with open(in_file, 'rt') as csv_file:
        reader = csv.reader(csv_file)
        first = True
        # iterate over all businesses
        for row in reader:
            if first:
                first = False
            else:
                n_found = False
                c_found = False
                bus_id = row[0]
                # get neighborhood
                n_hood = row[8]
                n_hood = n_hood.replace('[', '')
                n_hood = n_hood.replace(']', '')
                n_hood = n_hood.replace('"', '')
                n_hoods = n_hood.split(',')

                # check for given neighborhood
                for n in n_hoods:
                    if n == neigh:
                        # neighborhood found
                        n_found = True
                        break

                # neighborhood found -> get categories
                if n_found:
                    cur_cats = row[6]
                    cur_cats = cur_cats.replace('[', '')
                    cur_cats = cur_cats.replace(']', '')
                    cur_cats = cur_cats.replace('"', '')
                    cur_cats = cur_cats.split(',')

                    for c in cur_cats:
                        if c == cat:
                            c_found = True
                            break
                    if c_found:
                        # found neighborhood and category -> add cur business to list
                        business.update({bus_id: [row[4], row[9]]})

        if len(business) == 0:
            print("Did not find any businesses matching Neighborhood and/or category")
            sys.exit(0)
        else:
            return business


def round_to(n, precision):
    correction = 0.5 if n >= 0 else -0.5
    return int(n/precision+correction) * precision


# method to round to nearest 0.5
# http://stackoverflow.com/questions/4265546/python-round-to-nearest-05
def round_to_5(n):
    return round_to(n, 0.5)


# Main method
def main():

    args = sys.argv
    if len(args) == 1:

        # OPTIONAL ARGS:
        #######################################################################
        f_in = 'businesses_with_score.csv'
        neighborhood = 'South Side'
        category = 'Restaurants'
        f_out = 'results_business_neighborhood_category.csv'
        #######################################################################

    elif len(args) == 5:
        # args given
        f_in = args[1]
        neighborhood = args[2]
        category = args[3]
        f_out = args[4]
    else:
        # not right number of args given = ERROR
        print("ERROR: not given correct num of args. User entered:", len(args)-1, "Expected: 4")
        sys.exit(0)

    # get all business info that match neighborhood + category
    bus_info = get_bus_info(f_in, neighborhood, category)

    # output file
    wf = open(f_out, 'w', newline='')
    writer = csv.writer(wf)
    # header row
    writer.writerow(["neigborhood", "attribute", "avg_stars", "avg_sentiment_score"])

    # get business ids for matched businesses
    bus_ids = bus_info.keys()

    avg_stars = 0.0
    avg_score = 0.0
    cnt = len(bus_info)
    # iterate over matched businesses
    for b_id in bus_ids:
        avg_stars += float(bus_info[b_id][0])
        avg_score += float(bus_info[b_id][1])

    # calc average and round
    avg_stars = round_to_5(avg_stars/float(cnt))
    avg_score = round_to_5(avg_score/float(cnt))

    # output results
    writer.writerow([neighborhood, category, avg_stars, avg_score])
    print(neighborhood, category, avg_stars, avg_score)


if __name__ == '__main__':
    main()