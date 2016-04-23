import csv
with open("output.txt", "rt") as txtfile:
	with open("pittsburgh_reviews.csv", "rt") as csvfile:
		with open("pittsburgh_reviews_with_sentiment.csv", "w") as csvout:
			writer = csv.writer(csvout, lineterminator="\n")
			values = []
			for line in txtfile:
				if line != '\n':
					values.append(line.strip())
			
			
			rownum = 0
			reader = csv.reader(csvfile)
			all = []
			row = next(reader)
			row.append('Sentiment 1-5')
			all.append(row)
			i = 0
			for row in reader:
				row.append(values[i])
				all.append(row)
				i+=1
			
			writer.writerows(all)