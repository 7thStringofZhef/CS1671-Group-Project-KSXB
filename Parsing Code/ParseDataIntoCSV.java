import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.csv.*;

//parse data into a java object
public class ParseDataIntoCSV {
	public static void main (String[] args)
	{
		//Checking stuff. Gotta pass a filename arg, it has to be JSON
		if(args.length < 1){
			return;
		}
		String filename = args[0];
		String ext =filename.substring(filename.length()-4, filename.length());
		if(!ext.equals("json")){
			return;
		}
		
		String newFilename = filename.substring(0, filename.length()-4) + "csv";
		
		JSONParser parser = new JSONParser();
		try{
			CSVPrinter csvWriter = new CSVPrinter(new FileWriter(newFilename), CSVFormat.DEFAULT); //Use csv printer from apache
			//Parse whole file, but get one line to start
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			line = br.readLine();
			//Get the type of file from first entry
			JSONObject first = (JSONObject)parser.parse(line);
			String type = (String)first.get("type");
			
			
			//Parse according to type
			if(type.equals("review")){
				parseReviewJSON(parser, br, csvWriter, first);
			}
			else if(type.equals("business")){
				parseBusinessJSON(parser, br, csvWriter, first);
			}
			else if(type.equals("tip")){
				parseTipJSON(parser, br, csvWriter, first);
			}
			else if(type.equals("user")){
				parseUserJSON(parser, br, csvWriter, first);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//Split parsing into separate functions for different files
	
	
	private static void parseReviewJSON(JSONParser parser, BufferedReader br, CSVPrinter writer, JSONObject first) throws Exception{
		//Eliminate type, date, votes
		//Use fields: user_id, business_id, stars, text
		
		//Write header
		writer.print("user_id");
		writer.print("business_id");
		writer.print("stars");
		writer.print("text");
		writer.println();
		
		//Write first
		writer.print(first.get("user_id"));
		writer.print(first.get("business_id"));
		writer.print(first.get("stars"));
		writer.print(first.get("text"));
		writer.println();
		
		//Write each object in order
		String line;
		while((line=br.readLine())!=null){
			JSONObject o = (JSONObject)parser.parse(line);
			writer.print(o.get("user_id"));
			writer.print(o.get("business_id"));
			writer.print(o.get("stars"));
			writer.print(o.get("text"));
			writer.println();
		}
		br.close();
		writer.close();
	}
	
	private static void parseBusinessJSON(JSONParser parser, BufferedReader br, CSVPrinter writer, JSONObject first) throws Exception{
		//Eliminate any businesses not in Pittsburgh (city != Pittsburgh)
		//Eliminate fields: type, name, neighborhoods, city, state
		//Use fields: business_id, full_address, latitude, longitude, stars, review_count, categories, attributes
		//attributes is a nested json, categories is an array of strings
		
		//Write header
		writer.print("business_id");
		writer.print("full_address");
		writer.print("latitude");
		writer.print("longitude");
		writer.print("stars");
		writer.print("review_count");
		writer.print("categories");
		writer.print("attributes");
		writer.println();
		
		//Write first
		writer.print(first.get("business_id"));
		writer.print(first.get("full_address"));
		writer.print(first.get("latitude"));
		writer.print(first.get("longitude"));
		writer.print(first.get("stars"));
		writer.print(first.get("review_count"));
		writer.print(first.get("categories").toString());
		writer.print(first.get("attributes").toString());
		writer.println();
		
		//Write each object in order
		String line;
		while((line=br.readLine())!=null){
			JSONObject o = (JSONObject)parser.parse(line);
			if(!o.get("city").toString().toLowerCase().equals("pittsburgh")){ //Get only pitt restaurants
				continue;
			}
			writer.print(o.get("business_id"));
			writer.print(o.get("full_address"));
			writer.print(o.get("latitude"));
			writer.print(o.get("longitude"));
			writer.print(o.get("stars"));
			writer.print(o.get("review_count"));
			writer.print(o.get("categories").toString());
			writer.print(o.get("attributes").toString());
			writer.println();
		}
		br.close();
		writer.close();
	}
	
	private static void parseUserJSON(JSONParser parser, BufferedReader br, CSVPrinter writer, JSONObject first) throws Exception{
		//Eliminate fields: type, name, friends, elite, yelping_since, votes, fans, and compliments
		//Use fields: user_id, review_count, average_stars
		
		//Write header
		writer.print("user_id");
		writer.print("review_count");
		writer.print("average_stars");
		writer.println();
		
		//Write first
		writer.print(first.get("user_id"));
		writer.print(first.get("review_count"));
		writer.print(first.get("average_stars"));
		writer.println();
		
		//Write each object in order
		String line;
		while((line=br.readLine())!=null){
			JSONObject o = (JSONObject)parser.parse(line);
			writer.print(o.get("user_id"));
			writer.print(o.get("review_count"));
			writer.print(o.get("average_stars"));
			writer.println();
		}
		br.close();
		writer.close();
	}
	
	private static void parseTipJSON(JSONParser parser, BufferedReader br, CSVPrinter writer, JSONObject first) throws Exception{
		//Eliminate fields: type, date, likes
		//Use fields: user_id, business_id, text
		
		//Write header
		writer.print("user_id");
		writer.print("business_id");
		writer.print("text");
		writer.println();
		
		//Write first
		writer.print(first.get("user_id"));
		writer.print(first.get("business_id"));
		writer.print(first.get("text"));
		writer.println();
		
		//Write each object in order
		String line;
		while((line=br.readLine())!=null){
			JSONObject o = (JSONObject)parser.parse(line);
			writer.print(o.get("user_id"));
			writer.print(o.get("business_id"));
			writer.print(o.get("text"));
			writer.println();
		}
		br.close();
		writer.close();
	}
}
