package com.cognizant.devops.platformservice.bulkupload.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Stream;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("bulkUploadService")
public class BulkUploadService {
 private static final Logger log = LogManager.getLogger(BulkUploadService.class);

 public boolean createBulkUploadMetaData(MultipartFile file, String toolName, String label)
 throws InsightsCustomException, IOException {

  File csvfile = null;
  boolean status = false;
  String originalFilename = file.getOriginalFilename();
  String fileExt = FilenameUtils.getExtension(originalFilename);

  try {
   if (fileExt.equalsIgnoreCase("csv")) {
    if (file.getSize() < 2097152) {


     csvfile = convertToFile(file);
     CSVFormat format = CSVFormat.newFormat(',').withHeader();
     Reader reader = new FileReader(csvfile);
     CSVParser csvParser = new CSVParser(reader, format);
     Neo4jDBHandler dbHandler = new Neo4jDBHandler();
     Map < String, Integer > headerMap = csvParser.getHeaderMap();
     String query = "UNWIND {props} AS properties " + "CREATE (n:" + label.toUpperCase() + ") " +
      "SET n = properties";
     status = parseCsvRecords(csvParser, dbHandler, headerMap, query);

    } else {
     throw new InsightsCustomException("File is exceeding the size.");
    }
   } else {
    throw new InsightsCustomException("Invalid file format.");
   }


  }

  /*catch (InsightsCustomException exc) {
  	status = false;
  	log.error("Invalid file  " + file.getName() + "  With extension  " + fileExt + " size " + file.getSize());
  	throw new InsightsCustomException(exc.getMessage());

  } */
  catch (IOException ex) {
   log.debug("Exception while creating csv on server", ex.getMessage());
   throw new InsightsCustomException("Exception while creating csv on server");

  } catch (ArrayIndexOutOfBoundsException e) {
   log.error("Error in file.", e);
   throw new InsightsCustomException("Error in File Format");
  } catch (Exception e) {
   status = false;
   log.error("Error in uploading csv file", e);
   throw new InsightsCustomException("Error in uploading csv file");
   // e.printStackTrace();
  }
  return status;

 }

 // return status;

 private boolean parseCsvRecords(CSVParser csvParser, Neo4jDBHandler dbHandler, Map < String, Integer > headerMap,
  String query) throws IOException, GraphDBException, InsightsCustomException {
  List < JsonObject > nodeProperties = new ArrayList < > ();
  int numberOfRecords = headerMap.size();

  for (CSVRecord csvRecord: csvParser.getRecords()) {
   // int numberOfRecordInRow = csvRecord.
   //	log.debug(csvRecord.toString());
   /*log.debug(" numberOfRecordsHEader " + numberOfRecords + "  numberOfRecordInRow  "
   		+ csvRecord.getRecordNumber() + "  csvRecord size " + csvRecord.size()); */
   try {
    JsonObject json = getToolFileDetails(csvRecord, headerMap);
    nodeProperties.add(json);
   } catch (ArrayIndexOutOfBoundsException e) {
    log.error("Error in file.", e);
    throw new InsightsCustomException("Error in File Format");
   } catch (Exception e) {
    log.error(e);
    throw new InsightsCustomException("Error in uploading csv file");
   }

  }
  JsonObject graphResponse = dbHandler.bulkCreateNodes(nodeProperties, null, query);

  // log.error("GRAPH RESPONSE......."+graphResponse);
  // log.error(graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get("results").getAsJsonArray());
  if (graphResponse.get(DatataggingConstants.RESPONSE).getAsJsonObject().get(DatataggingConstants.ERRORS)
   .getAsJsonArray().size() > 0) {
   // log.error("GRAPH RESPONSE......."+graphResponse);
   return false;
  } else {
   return true;
  }
 }

 private JsonObject getToolFileDetails(CSVRecord record, Map < String, Integer > headerMap) throws InsightsCustomException {
  JsonObject json = new JsonObject();
  for (Map.Entry < String, Integer > header: headerMap.entrySet()) {
   if (header.getKey() != null) {
    /*
     * if (DatataggingConstants.METADATA_ID.equalsIgnoreCase(header.getKey()) &&
     * (record.get(header.getValue()) != null &&
     * !record.get(header.getValue()).isEmpty())) {
     * json.addProperty(header.getKey(),
     * Integer.valueOf(record.get(header.getValue()))); log.error("newtest"); } else
     * { json.addProperty(header.getKey(), record.get(header.getValue()));
     * log.error("test"); }
     */
    // log.debug("HEADER KEY"+header.getKey());
    // log.debug("HEADER VALUE"+header.getValue());
    try {
     json.addProperty(header.getKey(), record.get(header.getValue()));
    } catch (Exception e) {
     log.error("Error " + e + " at Header Key..." + header.getKey());
     throw new InsightsCustomException("Error " + e + " at Header Key..." + header.getKey());
    }
   }
  }
  return json;
 }

 private File convertToFile(MultipartFile multipartFile) throws IOException {
  File file = new File(multipartFile.getOriginalFilename());

  try (FileOutputStream fos = new FileOutputStream(file)) {
   fos.write(multipartFile.getBytes());
  }

  return file;
 }

 public Object getToolDetailJson() throws InsightsCustomException {
  // TODO Auto-generated method stub
  // Path dir = Paths.get(filePath);
  String agentPath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
  Path dir = Paths.get(agentPath);
  Object config = null;
  try (Stream < Path > paths = Files.find(dir, Integer.MAX_VALUE,
   (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.TOOLDETAIL_TEMPLATE)); FileReader reader = new FileReader(paths.limit(1).findFirst().get().toFile())) {

   JsonParser parser = new JsonParser();
   Object obj = parser.parse(reader);
   // config = ((JsonArray) obj).toString();
   config = obj;
  } catch (IOException e) {
   log.error("Offline file reading issue", e);
   throw new InsightsCustomException("Offline file reading issue -" + e.getMessage());
  }
  return config;
 }

}