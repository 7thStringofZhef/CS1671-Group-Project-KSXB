import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.StringBuilder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.csv.*;
/* To use the program just change the names of the inputFile and the outputFile, compile and run */
public class boundries {
	
	public static void main	(String [] args){
		String inputFile = "yelp_academic_dataset_review.csv";
		StringBuilder data;
		String outputFile = "test3.csv";
		boolean flag = false;
		String y = "\\!";
		File file = new File(inputFile);
		File file2 = new File(outputFile);
		String pattern = "[,;:\\$\"\\(\\)\\#\\.\\?\\!]";
		String pattern2 = "[a-zA-Z]";
		Matcher m;
		Matcher m2;
		int original;
		int j;
		Pattern r = Pattern.compile(pattern);
		Pattern r2 = Pattern.compile(pattern2);
		try{
			FileReader fr = new FileReader(file);
			FileWriter fw = new FileWriter(file2);
			BufferedWriter bf = new BufferedWriter(fw);
			CSVPrinter CSVWriter = new CSVPrinter(bf, CSVFormat.DEFAULT);
			CSVParser parser = CSVFormat.DEFAULT.parse(fr);
			for (CSVRecord csvRecord : parser) {
		//		System.out.println(csvRecord);
				data =  new StringBuilder(csvRecord.get(3));
				if (flag == true){
					data =  new StringBuilder("<s> " + csvRecord.get(3));
		//			System.out.println(data + "****** " + data.length());
					original = data.length();
					m = r.matcher(data);
					m2 = r2.matcher(data);
					while(m.find()){
						m2.region(m.start()-1, m.start());
						try{
						if (data.charAt(m.start()) == 36 || (data.charAt(m.start()) == 34) && (data.charAt(m.start()-1) == 32) || data.charAt(m.start()) == 40 || data.charAt(m.start()) == 35){
							data.insert(m.start()+1, " ");
							m.region(m.start()+1, m.regionEnd()+1);
						}	
						
						else if ((data.charAt(m.start()) == 46 && m2.find())  || (data.charAt(m.start()) == 33) || (data.charAt(m.start()) == 63)){			
							data.insert(m.start(), " </s>");
							data.deleteCharAt(m.start() + 5);
							int i = m.start() + 5;
							j = 0;
							try{
								while (data.charAt(i) == 46 || data.charAt(i) == 33 || data.charAt(i) == 63){
									data.deleteCharAt(i);
									j++;
								}					
								data.insert(m.start()+5, " <s> ");	
								if (original >= data.length()){
									m.region(m.start()+10, data.length()-1);

								}
								else{
									m.region(m.start()+10, data.length()+1);
								}
							}
							catch (Exception x){
								m.region(m.start()+5, data.length());
							}
							
							
						}
						
						else{
							if (data.charAt(m.start()) == 46 && !m2.find()){

							}
							else{
								data.insert(m.start(), " ");
								m.find(m.start()+1);
							}
						}
						}
						catch (Exception z){

						}
					}
			//		System.out.println(data + "MODIFIED " + data.length());
			//		System.out.println();
			//		System.out.println();
					
				}
				flag = true;
				CSVWriter.print(csvRecord.get(0));
				CSVWriter.print(csvRecord.get(1));
				CSVWriter.print(csvRecord.get(2));
				CSVWriter.print(data);
				CSVWriter.println();
 			}
			fr.close();
			bf.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}


}
