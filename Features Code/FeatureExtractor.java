
/*
* takes 2 arguments: review csv file, output csv file to create
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
 

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.csv.*;
/*
 * uses 'Basic English Stanford Tagger' from: http://nlp.stanford.edu/software/tagger.html
 * compiled and tested with .jar files in 'jar' directory and (.model + .props) files in 'models' directory
 */

public class FeatureExtractor {
    public static void main(String[] args) throws IOException,ClassNotFoundException{
    	
    	/* Initialize the tagger
    	 * argument = path to corresponding .tagger file
    	 * this path needs to contain the .tagger file as well as the .props files
    	 */
    	MaxentTagger tagger = new MaxentTagger("taggers/left3words-wsj-0-18.tagger");
    	
    	File f_in = new File(args[0]);
    	File f_out = new File(args[1]);
    	FileReader fr = new FileReader(f_in);
    	FileWriter fw = new FileWriter(f_out);
    	BufferedWriter bf_w = new BufferedWriter(fw);
    	CSVPrinter csv_writer = new CSVPrinter(bf_w, CSVFormat.DEFAULT);
		CSVParser parser = CSVFormat.DEFAULT.parse(fr);
    
    	StringBuilder cur_rev_feat;
    	
    	/*
		int test_ind = 0;							// TESTING PURPOSES						
		int test_num = 1000;
		*/
		
		
		boolean skip = true;
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
				for (String s : csv_record.get(3).split(" </s> <s> ") ){
					cur_rev_feat.append(featureExtraction(tagger, s));
				}
				
				// write out review features to file
				csv_writer.print(cur_rev_feat.toString());
			}
			
			/*												// TESTING PURPOSES
			if (test_ind == test_num){
				System.out.println(test_num);
				test_num = test_num + 1000;
			}
			test_ind++;
			*/
			
		}
		
		// close writers
		csv_writer.close();
		fr.close();
		bf_w.close();
    	
    }
    
    public static String featureExtraction(MaxentTagger mt, String line){
    	
    	StringBuilder features = new StringBuilder();
    	String cur_adj;
    	String cur_nn;
    	
    	String tagged = mt.tagString(line);			// The tagged string
    	String[] arr_tagged = tagged.split(" ");
    	
    	String[] cur;
    	double nn_L2R = Double.POSITIVE_INFINITY;
    	double nn_R2L = Double.POSITIVE_INFINITY;
    	for (int i = 0; i < arr_tagged.length; i++){
    		
    		// check each token for adj or adv tags
    		cur = arr_tagged[i].split("/");
    		if(cur.length > 1){							// error check -> eliminate empty sentences (poor spacing)
    			if(cur[1].equals("JJ") ||
        				cur[1].equals("JJR") ||
        				cur[1].equals("JJS") ||
        				cur[1].equals("RB") ||
        				cur[1].equals("RBR") ||
        				cur[1].equals("RBS") ){
        			
        			cur_adj = cur[0];		
        			
        			/*
        			// now check if next word is also adj/adv = chain (not going to worry about more than 2)
        			if(i+1 != arr_tagged.length){
        				cur = arr_tagged[i+1].split("/");
            			if(cur[1].equals("JJ") ||
                				cur[1].equals("JJR") ||
                				cur[1].equals("JJS") ||
                				cur[1].equals("RB") ||
                				cur[1].equals("RBR") ||
                				cur[1].equals("RBS") ){ 
            				
            				cur_adj = cur_adj + " " + cur[0];		
            				i = i+1;
            			}
        			}
        			*/
        			
        			// found adj or adv, look for NN (L2R)
        			for(int j = i+1; j < arr_tagged.length; j++){
        				cur = arr_tagged[j].split("/");
        				if(cur[1].equals("NN") || 
        						cur[1].equals("NNP") || 
        						cur[1].equals("NNPS") || 
        						cur[1].equals("NNS") ||
        						cur[1].equals("VBN") && 
        						!cur[1].equals("</s>")){		
        					if(j <= i + 2){
        					nn_L2R = j;			// store found NN index
        					}
        					break;
        				}
        			}
        			
        			/*
        			// found adj or adv, look for NN (R2L)
        			for(int j = i-1; j > 0; j--){					
        				
        				cur = arr_tagged[j].split("/");
        				
        				if(cur[1].equals("NN") || 
        						cur[1].equals("NNP") || 
        						cur[1].equals("NNPS") || 
        						cur[1].equals("NNS") ||
        						cur[1].equals("VBN") && 
        						!cur[1].equals("</s>")){	
        					
    	    				nn_R2L = j;			// store found NN index
        					break;
        				}
        			}
        			*/
        			
        			// get closet NN (one with smallest difference from adj) = check if right-to-left NN is closer
        			if(i - nn_R2L < nn_L2R - i && nn_R2L != Double.POSITIVE_INFINITY){	
        				cur_nn = arr_tagged[(int) nn_R2L].split("/")[0];		// get current NN word
        				// if not at beginning of sentence, check for NN chain (R2L) 
        				if((int) nn_L2R + 1 != arr_tagged.length){
        					cur = arr_tagged[(int) (nn_R2L - 1)].split("/");	
            				if(cur[1].equals("NN") || 
            						cur[1].equals("NNP") || 
            						cur[1].equals("NNPS") || 
            						cur[1].equals("NNS") ||
            						cur[1].equals("VBN") && 
            						!cur[1].equals("</s>")){				
            					
            					cur_nn = cur_nn + " " + cur[0];
            				}
        				}
        				features.append("<f> " + cur_nn + " " + cur_adj + " <//> ");
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
            					
            					cur_nn = cur_nn + " " + cur[0];
            				}
        				}
        				features.append("<f> " + cur_adj + " " + cur_nn + " </f> ");
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
