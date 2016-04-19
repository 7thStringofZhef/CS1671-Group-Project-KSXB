
/*
*	CS1671 Group Porject - Pittsburgh Review Parser
*	
*	tsb20, btk15, dts24, xix35
*	
*	Collects all Pittsburgh-based reviews from review file
*	
*	Avg Run-time: 2-3 minutes
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.csv.*;

// collects all reviews located in Pittsburgh area
public class PittsburghReviewParser {
    public static void main(String[] args) throws IOException,ClassNotFoundException{
		
    	String in_business = "";
		String in_rev = "";
		String out_name = "";
		/*
		 * Optional arguments
		 * can give program arguments in specified order: business_data_file review_data_file output_file_name
		 * if no arguments given, uses hard coded values 
		 */
		if(args.length == 0){
			
			// HARD CODED FILE NAMES
			in_business = "yelp_academic_dataset_business.csv";
			in_rev = "review_data.csv";
			out_name = "pittsburgh_reviews.csv";
		}
		else if(args.length != 3){
			System.out.println("ERROR: Invalid number of arguments. Expected num of args = 3");
			System.exit(0);
		}
		else{
			in_business = args[0];
			in_rev = args[1];
			out_name = args[2];
		}
		
		String[] pitt_business_ids;
		pitt_business_ids = getBusinessIDs(in_business);	// get Pittsbrugh business id array
		
    	File f_rev_in = new File(in_rev);
    	File f_out = new File(out_name);
    	FileReader fr = new FileReader(f_rev_in);
    	FileWriter fw = new FileWriter(f_out);
    	BufferedWriter bf_w = new BufferedWriter(fw);
    	CSVPrinter csv_writer = new CSVPrinter(bf_w, CSVFormat.DEFAULT);
		CSVParser parser = CSVFormat.DEFAULT.parse(fr);
		boolean skip = true;
		
		// indicate start of parse
		System.out.print("Collecting Pittsburgh based reviews from " + in_rev + " to " + out_name + "...");
		
		// iterate over all reviews
		for (CSVRecord csv_record : parser) {
			
			if (skip){
				// skip header row -> write out header fields to new file
				csv_writer.print(csv_record.get(0));
				csv_writer.print(csv_record.get(1));
				csv_writer.print(csv_record.get(2));
				csv_writer.print(csv_record.get(3));
				csv_writer.println();
				skip = false;
			}
			else{
				// if review business id = Pittsburgh business id, write to output file
				for (String s: pitt_business_ids){
					if(csv_record.get(1).equals(s)){
						csv_writer.print(csv_record.get(0));
						csv_writer.print(csv_record.get(1));
						csv_writer.print(csv_record.get(2));
						csv_writer.print(csv_record.get(3));
						csv_writer.println();
						break;
					}
				}
			}
		}
		// close writers
		csv_writer.close();
		fr.close();
		bf_w.close();
		
		// indicate end of parse
		System.out.println(" done");
    }
    
    // collect + store Pittsburgh business ids from business file into array
	public static String[] getBusinessIDs(String f_in) throws IOException{
			
		File f_busi_in = new File(f_in);
    	FileReader fr = new FileReader(f_busi_in);
		CSVParser parser = CSVFormat.DEFAULT.parse(fr);
		ArrayList<String> busi_ids = new ArrayList<String>();
		boolean skip = true;
		
		// iterate over Pittsburgh business data entries
		for (CSVRecord csv_record : parser) {
			
			if (skip){
				skip = false;						// skip over header row
			}
			else{
				busi_ids.add(csv_record.get(0));	// store business ids
			}
		}
		fr.close();
		
		String[] busi_id_arr = new String[busi_ids.size()];
		busi_id_arr = busi_ids.toArray(busi_id_arr);
		return busi_id_arr;
	}
}
