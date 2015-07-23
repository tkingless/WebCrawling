import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Formatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.FileWriter;
import java.lang.String;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.*;

public class CrawlECTutor {

	/**
	 * @param args the command line arguments
	 */

	static class Crawlee {

		public int case_index;
		public HashMap<String,String> map = new HashMap<String,String>();

		/*
		Current keys of HashMap are:
		Location,LastUpdateAt,Time,Gender,Info,Subject,Fee,Other
		*/

		public Crawlee (int idx){
			case_index = idx;
		}

		public void Put (String Key, String Value) {
			map.put(Key,Value);
		}

		public String Context () {
			String content = "";
			Collection<String> strings = map.values();
			for (String str: strings){
				content = content + str + "\n";
			}
			//System.out.println("[Crawlee] content: " + content);
			return content;
		}

		public int GetFee () {
			if(map.containsKey("Fee")){
				System.out.println("[SearchCrit] fee: " + map.get("Fee"));
				Pattern price = Pattern.compile("\\$[0-9]{2,4}");
				Matcher matcher = price.matcher(map.get("Fee"));
				if(matcher.find()){
					String casePriceStr = matcher.group(0).substring(1);
					int casePrice = 99999;
					casePrice = Integer.parseInt(casePriceStr);
					if (casePrice != 99999)
						return casePrice;
				}
			}
			return 0;	
		}

		public String GetLocation () {

			if(map.containsKey("Location")){
				return map.get("Location");
			}

			return "the Earth";
		}
	}

	static class Crawlee_DB {
		
		List<Crawlee> records = new ArrayList<Crawlee>();

		public void Add (Crawlee crle){
			records.add(crle);
		}

		public boolean ContainIndex (String key){
			for(Crawlee crle: records){
				if(Integer.toString(crle.case_index) == key)
				return true;
			}
			return false;
		}

	}

	//Params
	public static String URL_KEY = "WC_URL";
	public static String URL_INDEX_KEY = "WC_INDEX_URL";
	public static String CRIT_SUBJECT_KEY = "WC_SEARCH_CRIT";
	public static String CRIT_LOCATION_KEY = "WC_SEARCH_OUT_CRIT";
	public static String CRIT_PRICE_KEY = "WC_SEARCH_COND_PRICE_ABOVE";
	public static String[] config_header_mapping = {"TYPE","VALUE"};
	public static String[] library_header_mapping = {"DISCOVERD DATE","INDEX","TIME","GENDER","INFO","SUBJECT","FEE"};
	public static String OUTPUT_DELIMITER = ",";
	public static String OUTPUT_LINE_ENDING = "\n";
	public static String LAST_RECORD = "last_index.csv";
	public static String DB_HISTORY = "case_library.csv";
	public static int MAX_CONTU_ERR = 10;

	//Runtime global var
	static List<Crawlee> crawlees = new ArrayList<Crawlee>();
	static int startIndex = 0; //pop up case start index

	public static void main(String[] args) throws IOException {

		//	if(args[0] != null){

		/*try {
		  startIndex  = Integer.parseInt(args[0]);
		  } catch (NumberFormatException e) {
		  System.err.println("Argument " + args[0] + " must be an integer.");
		  System.exit(1);
		  }*/

		MultiMap<String,String> config = new MultiValueMap<String,String>();
		ParseInConfig(config);

		ProcessUrl(config);

		//FilterByCriteria(config);

		//Result:
		//	for (Crawlee cr: crawlees){
		//		System.out.println("[SearchCrit] Remaining crawlee: " + cr.case_index);
		//	}

		//	ParseInResult();

		//	}else {
		//		System.err.println("Need to ASSIGN starting pop up case number");
		//	}

	}

	static void ParseInConfig (MultiMap<String,String> mapConfig) throws IOException {

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(config_header_mapping);
		FileReader fileReader = new FileReader("config.csv");
		System.out.println("The encoding is: " + fileReader.getEncoding());
		CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
		List csvRecords = csvFileParser.getRecords();
		System.out.println("[Apache] csvRecords.getRecords() size: " + csvRecords.size());

		for(int i = 1; i < csvRecords.size(); i++) {
			CSVRecord record = (CSVRecord) csvRecords.get(i);
			System.out.println("[Apache] apache commons csv here, The TYPE: " + record.get(config_header_mapping[0]) + " and the VALUE: " + record.get(config_header_mapping[1]));
			mapConfig.put(record.get(config_header_mapping[0]),record.get(config_header_mapping[1]));
		}
	}

	static void ProcessUrl (MultiMap<String,String> config) throws IOException {

		/*boolean loop = true;
		  int _case = 0;
		  int continuous_error_count = 0;

		  _case = startIndex;

		  while (loop) {
		  String URL = urlStr + Integer.toString(_case);
		  System.out.println("URL : "+ URL);
		  Document aDoc = Jsoup.connect(URL).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();

		  if (!aDoc.text().contains("Server Error")) {
		//	String title = aDoc.title();
		//	System.out.println("[Doc] Title: " + title);
		//	String result = aDoc.text();
		//	System.out.println("[Doc] Result: " + result);

		DoSearchOnContent(aDoc,_case);
		continuous_error_count = 0;
		}
		else {
		continuous_error_count++;
		if(continuous_error_count >= MAX_CONTU_ERR){
		loop = false;
		}
		}

		if (!loop){

		Date today = new Date();
		DateFormat df = new SimpleDateFormat();
		FileWriter filewriter = new FileWriter(LAST_RECORD,true);
		filewriter.append(df.format(today));
		filewriter.append(",");
		filewriter.append(Integer.toString(_case-continuous_error_count));
		filewriter.append("\n");
		filewriter.close();

		break;
		}
		_case++;
		}*/

		Collection<String> idx_urls = (Collection<String>) config.get(URL_INDEX_KEY);
		for(String idx_url: idx_urls){
			System.out.println("The idx url: " + idx_url);

			Document idxDoc = Jsoup.connect(idx_url).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();

			List<String> onboard_indices = new ArrayList<String>();
			Pattern atrbt = Pattern.compile("bk_case_[0-9]{6}");
			Matcher idxMatcher = atrbt.matcher(idxDoc.body().toString());

			while(idxMatcher.find()){
				String str = idxMatcher.group();
				str = str.substring(str.lastIndexOf('_') + 1);
				onboard_indices.add(str);
			}

			Collections.sort(onboard_indices);

			//DB File checking
			File DBfile = new File(DB_HISTORY);

			if(DBfile.exists())
				System.out.println("[File] db file exists");
			else
				System.out.println("[File] db file not exists");

			if(DBfile.isDirectory())
				System.out.println("[File] db file is directory");
			else
				System.out.println("[File] db file is not directory");
			boolean needHeader = false;
			if(!DBfile.exists() && !DBfile.isDirectory()){
				needHeader = true;	
			}
			
			//Create filewriter
			FileWriter filewriter = new FileWriter(DB_HISTORY,true);

			if(needHeader){
				System.out.println("[DB] writing headers");
				int size = library_header_mapping.length;
				for(int i = 0; i < size-1; i++){
				filewriter.append(library_header_mapping[i]+",");
				}
				filewriter.append(library_header_mapping[size-1]);
				filewriter.append("\n");
			}

			//Create CSV reader
			//{"DISCOVERD DATE","INDEX","TIME","GENDER","INFO","SUBJECT","FEE"}
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(library_header_mapping);
			FileReader fileReader = new FileReader(DB_HISTORY);
			CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
			List DB = csvFileParser.getRecords();

			//TODO: need to archive last day record	

			Crawlee_DB past_record = new Crawlee_DB();

			for(int i = 1; i < DB.size(); i++){
				CSVRecord record = (CSVRecord) DB.get(i);
				System.out.println("[DB] sampling: " + record.get(library_header_mapping[0]) + " , " + record.get(library_header_mapping[1]));
				Crawlee sample = new Crawlee(Integer.parseInt(record.get(library_header_mapping[1])));
				sample.Put("Time",record.get(library_header_mapping[2]));
				sample.Put("Gender",record.get(library_header_mapping[3]));
				sample.Put("Info",record.get(library_header_mapping[4]));
				sample.Put("Subject",record.get(library_header_mapping[5]));
				sample.Put("Fee",record.get(library_header_mapping[6]));
				
				past_record.Add(sample);

			}


			//Do searches on contents
			for(String index: onboard_indices){
				//System.out.println("[On-board] idx : " + str);
				Collection<String> urls = (Collection<String>) config.get(URL_KEY);
				for(String url: urls){
					String URL = url + index;
					Document caseDoc = Jsoup.connect(URL).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();
					if (!caseDoc.text().contains("Server Error")) {
						//      String title = caseDoc.title();
						//      System.out.println("[Doc] Title: " + title);
						//      String result = caseDoc.text();
						//      System.out.println("[Doc] Result: " + result);
						DoSearchOnContent(caseDoc,Integer.parseInt(index)); //crawlees got filled

						//TODO: remember to replace comma to \comma

					}
				}
			}

			filewriter.close();
		}
	}

	static void DoSearchOnContent (Document doc, int indx) throws IOException {

		HashMap<String,String> searchNodes = new HashMap<String,String>();
		searchNodes.put("Location","span[class$=title]");
		searchNodes.put("LastUpdateAt","span[class$=loginTime]");
		//searchNodes.put("Detail","div[class$=detail]:eq(1) > p:eq(2)");
		searchNodes.put("Details","div[class$=detail] > div[class$=item]");
		//String JsoupSearchNode_CONTENT = "div[class$=detail]:eq(1)";

		Elements location = doc.select(searchNodes.get("Location"));
		Elements lastUpdate = doc.select(searchNodes.get("LastUpdateAt"));
		Elements eles = doc.select(searchNodes.get("Details"));

		//97077 System.out.println("[Jsoup] location: " + location.text() + " and lastUpdate: " + lastUpdate.text());

		for (int i = 0; i < eles.size(); i++){
			Element ele = eles.get(i);
		//97077	System.out.println("[Jsoup] ele text: " + ele.text());
		}

		Crawlee crawlee = new Crawlee(indx);
		//location
		crawlee.Put("Location","Location: " + location.text());
		//LastupdateAt
		crawlee.Put("LastUpdateAt","Last Update: " + lastUpdate.text());
		//Time
		crawlee.Put("Time", eles.get(0).text());
		//Gender
		crawlee.Put("Gender", eles.get(1).text());
		//Info
		crawlee.Put("Info", eles.get(2).text());
		//Subject
		crawlee.Put("Subject", eles.get(3).text());
		//Fee
		crawlee.Put("Fee", eles.get(4).text());
		//Other
		crawlee.Put("Other", eles.get(5).text());

		crawlees.add(crawlee);
		// 97077 System.out.println("[Crawlee] crawlees size: " + crawlees.size() + " and the cralwee content: \n" + crawlee.Context());
	}

	//Case filter descriptor
	static void FilterByCriteria (MultiMap<String,String> config) throws IOException {

		for (Iterator<Crawlee> crawlee_ite = crawlees.iterator(); crawlee_ite.hasNext();) {
			Crawlee crawlee = crawlee_ite.next();
			Boolean beDeleted = true;

			if(FilterInBySubject(crawlee,config)){
				if(!FilterByFee(crawlee,config)){
					if(FilterOutByLocation(crawlee, config)){
						beDeleted = false;
					}
				}
			}

			if(beDeleted) {
				//	System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index + " , " + crawlee.context_text);
				System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index);
				crawlee_ite.remove();
			}
		}
	}

	static Boolean FilterByFee (Crawlee crawlee, MultiMap<String,String> config) {
		int price_above = -1;
		Collection<String> price_str = (Collection<String>) config.get(CRIT_PRICE_KEY);
		price_above = Integer.parseInt((String) price_str.toArray()[0]);
		if (price_above != -1) {
			if( crawlee.GetFee() > price_above)
				return false;
		}
		return true;
	}

	static Boolean FilterOutByLocation(Crawlee crawlee, MultiMap<String,String> config) {

		Collection<String> location_Strs = (Collection<String>) config.get(CRIT_LOCATION_KEY);

		for (String aCrit: location_Strs){
			Pattern crit = Pattern.compile(aCrit);
			Matcher matcher = crit.matcher(crawlee.GetLocation());
			if(matcher.find())
				return true;
		}
		return false;
	}

	static Boolean FilterInBySubject(Crawlee crawlee, MultiMap<String,String> config) {

		Collection<String> subject_Strs = (Collection<String>) config.get(CRIT_SUBJECT_KEY);

		for (String aCrit: subject_Strs){
			Pattern crit = Pattern.compile(aCrit);
			Matcher matcher = crit.matcher(crawlee.GetLocation());
			if(matcher.find())
				return false;
		}
		return true;
	}

	static void ParseInResult () throws IOException {

		//Parsing
		FileWriter filewriter = new FileWriter("result.csv");
		filewriter.append(new SimpleDateFormat().format(new Date()) + " 's update:\n"); 
		for (Crawlee cr: crawlees){
			filewriter.append("The case index: " + cr.case_index + "\n");
			filewriter.append(cr.Context());
			filewriter.append(OUTPUT_LINE_ENDING);
		}
		filewriter.close();

	}
}
