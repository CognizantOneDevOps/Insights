package com.cognizant.devops.platformengine.modules.datapurging;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfigurationDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class DataPurgingExecutor implements Job {

	
	private static Logger log = Logger.getLogger(DataPurgingExecutor.class.getName());
	protected static final String  DATAPURGING_SETTINGS_TYPE = "DATAPURGING";

	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			getLabelsAndNodes();
		} catch (GraphDBException e) {
			log.error(e);
		}
	}


	public  void getLabelsAndNodes() throws GraphDBException{
		List<String> labelList = new ArrayList<String>();
		boolean isDelete = false;
		String rowLimit = null ;
		String backupFileLocation = null ;
		int backupDurationInDays = 0;
		// reading data from neo4jbackup.json file in classpath 
		/*
		 * File backupFile = new File(ConfigOptions.DATABACKUP_RESOLVED_PATH);
		JsonObject backupJsonObj = new JsonObject();
		if (!backupFile.exists()) {
			URL resource = ApplicationConfigCache.class.getClassLoader().getResource(ConfigOptions.NEO4JBACKUP_TEMPLATE);
			if (resource != null) {
				backupFile = new File(resource.getFile());
			}
		}
		try(FileReader fileReader = new FileReader(backupFile)){
			JsonElement jsonElement = new JsonParser().parse(fileReader);
			backupJsonObj = jsonElement.getAsJsonObject();
			JsonArray array = backupJsonObj.get("labels").getAsJsonArray();
			for (int i = 0; i < array.size(); i++) {
				labelList.add(array.get(i).getAsString());
			}
			limit = backupJsonObj.get("limit") .getAsString();
			fileLocation =backupJsonObj.get("backupLocation").getAsString();
			deleteFrom = -(backupJsonObj.get("deleteFrom").getAsInt());

		} catch (FileNotFoundException e) {
			log.error("Unable to find  back up json file: "+ConfigOptions.DATABACKUP_RESOLVED_PATH, e);
		} catch (IOException e) {
			log.error(e);
		}*/
		
		/**
		 * To get Settings Configuration which is set by User from Insights application UI
		 * is stored into Settings_Configuration table of PostGres database
		 */
		JsonObject configJsonObj = getSettingsJsonObject(); 
		if (configJsonObj != null) {
			JsonArray array = configJsonObj.get("labels").getAsJsonArray();
			if (array != null) {
				for (int i = 0; i < array.size(); i++) {
					labelList.add(array.get(i).getAsString());
				}
			}			
			rowLimit = configJsonObj.get("rowLimit") .getAsString();
			backupFileLocation =configJsonObj.get("backupFileLocation").getAsString();
			backupDurationInDays = -(configJsonObj.get("backupDurationInDays").getAsInt());
		}
		
		
		//convert to epoch time 
		Calendar cal = GregorianCalendar.getInstance();
		cal.add( Calendar.DAY_OF_YEAR, backupDurationInDays);
		Date tenDaysAgo = cal.getTime();
		long epochTime = tenDaysAgo.getTime() /1000;
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		int labelSize = 0;
		
		for(String label : labelList){
			labelSize = labelSize + 1;
			int splitlength = 0;
			int count = getNodeCnt(dbHandler, label ,epochTime);
			while(splitlength  < count){
				GraphResponse response = executeCypherQuery(label ,rowLimit,splitlength , epochTime) ;
				String location = backupFileLocation +"/"+ label+ "_"+splitlength + ".csv";
				try {
					writeToCSVFile(response , location);
				} catch (IOException e) {
					log.error(e);
				}
				splitlength = splitlength + Integer.parseInt(rowLimit);
			}	
			if( labelSize >= labelList.size()){
				isDelete = true;
			}
		}
		
		if( isDelete){
			for(String label : labelList){
				String deleteQry =  "MATCH (n:"+label+")  where n.inSightsTime < "+ epochTime  +"   delete n ";
				dbHandler.executeCypherQuery(deleteQry);
			}
		}
	}

	private int getNodeCnt(Neo4jDBHandler dbHandler, String label, long epochTime) throws GraphDBException {
		String cntQry = "MATCH (n:"+label+")  where n.inSightsTime < "+ epochTime  +"    return count(n) ";
		GraphResponse cntResponse = dbHandler.executeCypherQuery(cntQry);
		int count = cntResponse.getJson() .get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray()
				.get(0).getAsJsonObject().get("row").getAsInt();
		return count;
	}

	private GraphResponse executeCypherQuery(String label, String limit, int splitlength, long epochTime) throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:"+label+")  where n.inSightsTime < "+ epochTime  +"   return n skip  "+ splitlength +" limit  " +limit;
		GraphResponse response = dbHandler.executeCypherQuery(query);
		return response;
	}

	private void writeToCSVFile(GraphResponse response, String location) throws IOException {

		Gson gson = new Gson();
		List<MetaData> list = new ArrayList<MetaData>();
		JsonArray array = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray();
		for(JsonElement element : array) {

			MetaData metadata = gson.fromJson(element.getAsJsonObject().get("row").getAsJsonArray().get(0).toString(), new TypeToken<MetaData>() {}.getType());
			list.add(metadata);
		}	
		CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		FileWriter fWriter = new FileWriter(location);
		CSVPrinter csvPrinter = new CSVPrinter(fWriter, csvFormat);
		csvPrinter.printRecord( "metadataid","level_1","level_2","level_3","level_4",
				"toolproperty1","propertyvalue1","toolproperty2","propertyvalue2",
				"toolproperty3","propertyvalue3","toolproperty4","propertyvalue4","toolname","action");
		for(MetaData data : list){
			List<String> record= new ArrayList<String>();
			record.add(data.getMetadata_id());
			record.add(data.getLevel_1());
			record.add(data.getLevel_2());
			record.add(data.getLevel_3());
			record.add(data.getLevel_4());
			record.add(data.getToolProperty1());
			record.add(data.getPropertyValue1());
			record.add(data.getToolProperty2());
			record.add(data.getPropertyValue2());
			record.add(data.getToolProperty3());
			record.add(data.getPropertyValue3());
			record.add(data.getToolProperty4());
			record.add(data.getPropertyValue4());
			record.add(data.getToolName());
			record.add(data.getAction());
			csvPrinter.printRecord(record);
		}
		csvPrinter.close();

	}
	
	private JsonObject getSettingsJsonObject() {
		SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();	
		String settingsJson = settingsConfigurationDAL.getSettingsJsonObject(DATAPURGING_SETTINGS_TYPE);
		if (settingsJson != null && !settingsJson.isEmpty()) {
			Gson gson = new Gson();
			JsonElement jelement = gson.fromJson(settingsJson.trim(),JsonElement.class);
			return jelement.getAsJsonObject();
		}
		return null;
	}


	public static void main(String[] a){
		DataPurgingExecutor dataPurgingExecutor=new DataPurgingExecutor();
		ApplicationConfigCache.loadConfigCache();
		try {
			dataPurgingExecutor.getLabelsAndNodes();
		} catch (GraphDBException e) {
			log.error(e);
		}

	}
}
