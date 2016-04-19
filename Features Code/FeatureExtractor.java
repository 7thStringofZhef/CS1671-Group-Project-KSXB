
/*
*	CS1671 Group Porject - Feature Extractor 
*	
*	tsb20, btk15, dts24, xix35
*	
*	Extracts all features from reviews 
*	Feature Pairs can be in line with one another (so 2 words right next to eachother) or
*	also be separated within the sentence.
*	EX: "The steak that all of us ordered was salty." Feature: salty steak
*	It will also look for "chains" of words (so 2 adj followed by a nn) (or an adv followed by chain of nn)
*	EX: "old fashioned menu" "good steak fries"
*
* 	uses 'Basic English Stanford Tagger' from: http://nlp.stanford.edu/software/tagger.html
* 	compiled and tested with tag files (.model + .props files) in 'taggers' directory
*	
*	Uses optional arguments, so if not args specified, will use hard-coded values
*
*	Run-time: 25ish minutes
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.csv.*;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class FeatureExtractor {
    public static void main(String[] args) throws IOException,ClassNotFoundException{
    	
    	String in_rev = "";
    	String out_feat = "";
    	
    	/*
		 * Optional arguments
		 * can give program arguments in specified order: review_file feature_output_file_name
		 * if no arguments given, uses hard coded values 
		 */
		if(args.length == 0){
			
			// HARD CODED FILE NAMES
			in_rev = "pittsburgh_rev.csv";
			out_feat = "pittsburgh_rev_feat.csv";
		}
		else if(args.length != 2){
			System.out.println("ERROR: Invalid number of arguments. Expected num of args = 2");
			System.exit(0);
		}
		else{
			in_rev = args[0];
			out_feat = args[1];
		}
    	/* Initialize the tagger
    	 * argument = path to corresponding .tagger file
    	 * this path needs to contain the .tagger file as well as the .props files
    	 */
    	MaxentTagger tagger = new MaxentTagger("taggers/left3words-wsj-0-18.tagger");
    	
    	File f_in = new File(in_rev);
    	File f_out = new File(out_feat);
    	FileReader fr = new FileReader(f_in);
    	FileWriter fw = new FileWriter(f_out);
    	BufferedWriter bf_w = new BufferedWriter(fw);
    	CSVPrinter csv_writer = new CSVPrinter(bf_w, CSVFormat.DEFAULT);
		CSVParser parser = CSVFormat.DEFAULT.parse(fr);
    	StringBuilder cur_rev_feat;				
		boolean skip = true;
		
		// indicate start of extraction
		System.out.print("Extracting features from " + in_rev + " to " + out_feat + "...");
		
		// iterate over reviews
		for (CSVRecord csv_record : parser) {
			cur_rev_feat = new StringBuilder();
			csv_writer.print(csv_record.get(0));
			csv_writer.print(csv_record.get(1));
			csv_writer.print(csv_record.get(2));
			
			if (skip){
				// first iteration => write out header fields
				csv_writer.print("features");
				skip = false;
			}
			else{
				
				// iterate over review sentences
				for (String s : csv_record.get(3).split(" </s> <s> ") ){
					cur_rev_feat.append(featureExtraction(tagger, s));
				}
				
				// write out review features to file
				csv_writer.print(cur_rev_feat.toString());
			}
			csv_writer.println();
		}
		
		// close writers
		csv_writer.close();
		fr.close();
		bf_w.close();
    	
		// indicate end of feature extraction
		System.out.println(" done");
    }
    
    // extract any features from sentence using tagger
    public static String featureExtraction(MaxentTagger mt, String line){
    	
    	StringBuilder features = new StringBuilder();
    	String cur_adj;
    	String cur_nn;
    	String[] cur;
    	double nn_L2R;
    	double nn_R2L;
    	
    	// tag the sentence
    	String tagged = mt.tagString(line);	
    	String[] arr_tagged = tagged.split(" ");
    
    	// iterate over word/tags of sentence
    	for (int i = 0; i < arr_tagged.length; i++){
    		cur_adj = "";
        	cur_nn = "";
        	nn_L2R = Double.POSITIVE_INFINITY;
        	nn_R2L = Double.POSITIVE_INFINITY;
    		cur = arr_tagged[i].split("/");
    		
    		// check word for adj or adv tags
    		if(cur.length > 1){							// error check -> eliminate empty sentences (poor spacing)
    			if(cur[0].equals("<s>") &&
    					cur[1].equals("JJ") ||
        				cur[1].equals("JJR") ||
        				cur[1].equals("JJS") ||
        				cur[1].equals("RB") ||
        				cur[1].equals("RBR") ||
        				cur[1].equals("RBS") ){
        			
        			cur_adj = cur[0];		// store found adj
        			
        			// now check if next word is also adj/adv = chain (not going to worry about more than 2)
        			if(i+1 != arr_tagged.length){
        				cur = arr_tagged[i+1].split("/");
            			if(cur[0].equals("<s>") &&
            					cur[1].equals("JJ") ||
                				cur[1].equals("JJR") ||
                				cur[1].equals("JJS") ||
                				cur[1].equals("RB") ||
                				cur[1].equals("RBR") ||
                				cur[1].equals("RBS") ){ 
            				
            				cur_adj = cur_adj + " " + cur[0];	// add new adj	
            				i = i+1;
            			}
        			}
        			
        			if(i+1 != arr_tagged.length){
        				// found adj or adv, look for NN (L2R)
            			for(int j = i+1; j < arr_tagged.length; j++){
            				cur = arr_tagged[j].split("/");
            				if(cur[1].equals("NN") || 
            						cur[1].equals("NNP") || 
            						cur[1].equals("NNPS") || 
            						cur[1].equals("NNS") ||
            						cur[1].equals("VBN") && 
            						!cur[1].equals("</s>")){		
            					
            					nn_L2R = j;			// store found NN index
            					break;
            				}
            			}
        			}
        			
        			if(i-1 > 0){
        				// found adj or adv, look for NN (R2L)
            			for(int j = i-1; j > 0; j--){					
            				
            				cur = arr_tagged[j].split("/");
            				
            				if(cur[1].equals("NN") || 
            						cur[1].equals("NNP") || 
            						cur[1].equals("NNPS") || 
            						cur[1].equals("NNS") ||
            						cur[1].equals("VBN") && 
            						!cur[1].equals("<s>")){	
            					
        	    				nn_R2L = j;			// store found NN index
            					break;
            				}
            			}
        			}
        			
        			// get closet NN (one with smallest difference from adj) = check if right-to-left NN is closer
        			if(i - nn_R2L < nn_L2R - i && nn_R2L != Double.POSITIVE_INFINITY){	
        				cur_nn = arr_tagged[(int) nn_R2L].split("/")[0];		// get current NN word
        				// if not at beginning of sentence, check for NN chain (R2L) 
        				if((int) nn_R2L - 1 != 0){
        					cur = arr_tagged[(int) (nn_R2L - 1)].split("/");	
            				if(cur[1].equals("NN") || 
            						cur[1].equals("NNP") || 
            						cur[1].equals("NNPS") || 
            						cur[1].equals("NNS") ||
            						cur[1].equals("VBN") && 
            						!cur[1].equals("</s>")){				
            					
            					cur_nn = cur_nn + " " + cur[0];		// add new nn
            				}
        				}
        				features.append("<f> " + cur_nn + " " + cur_adj + " </f> ");
        			}
   
        			// check if left-to-right NN is closer (if tie, pick this NN)
        			else if (nn_L2R - i <= i - nn_R2L && nn_L2R != Double.POSITIVE_INFINITY){
        				cur_nn = arr_tagged[(int) nn_L2R].split("/")[0];		// get current NN word 
        				// if not at end of sentence, check for NN chain (L2R) 
        				if((int) nn_L2R + 1 != arr_tagged.length){
        					cur = arr_tagged[(int) (nn_L2R + 1)].split("/");		
            				if(cur[1].equals("NN") || 
            						cur[1].equals("NNP") || 
            						cur[1].equals("NNPS") || 
            						cur[1].equals("NNS") ||
            						cur[1].equals("VBN") && 
            						!cur[1].equals("</s>")){				
            					
            					cur_nn = cur_nn + " " + cur[0];		// add new nn
            				}
        				}
        				features.append("<f> " + cur_adj + " " + cur_nn + " </f> ");		// add newly found feature pair
        			}
        			// both sides == infinity => found no NN
        			else
        				continue;
        		}
    		}
    	}
    	
    	// return found features
    	if (features.length() == 0)
    		features.append("");
    	return features.toString();
    }
}
