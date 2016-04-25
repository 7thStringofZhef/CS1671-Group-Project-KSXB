import csv


# Main method
def main():

    wf = open('evaluation.csv', 'w', newline='')
    writer = csv.writer(wf)

    # header row
    writer.writerow(["strict", "relaxed_.5", "pretty_relaxed_1", "very_relaxed_1.5"])

    # cnt of scored businesses
    cnt = 0

    # evaluation metrics
    strict = 0
    relaxed_5 = 0
    relaxed_1 = 0
    relaxed_15 = 0

    with open('businesses_with_score.csv', 'rt') as csv_file:
        reader = csv.reader(csv_file)
        first = True

        # iterate over businesses in score file
        for row in reader:

            if first:
                first = False
            else:
                cnt += 1
                # get star and sentiment scores
                bus_star = float(row[1])
                bus_sent = float(row[3])

                # strict
                if bus_star == bus_sent:
                    strict += 1
                # relaxed (0.5)
                if abs(bus_star - bus_sent) <= 0.5:
                    relaxed_5 += 1
                # very relaxed (1.0)
                if abs(bus_star - bus_sent) <= 1.0:
                    relaxed_1 += 1
                # SUPER relaxed (1.5)
                if abs(bus_star - bus_sent) <= 1.5:
                    relaxed_15 += 1

    cnt = float(cnt)
    writer.writerow([strict/cnt, relaxed_5/cnt, relaxed_1/cnt, relaxed_15/cnt])


if __name__ == '__main__':
    main()
