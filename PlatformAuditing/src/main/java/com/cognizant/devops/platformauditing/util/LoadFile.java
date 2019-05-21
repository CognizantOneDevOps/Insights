package com.cognizant.devops.platformauditing.util;

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;


public class LoadFile {
    private static final Logger LOG = LogManager.getLogger(InsightsAuditImpl.class);
    private static JsonObject dataModel = new JsonObject();
    private static JsonObject processModel = new JsonObject();
    private static JsonObject Config;
    //this object creation loads the datamodel by calling the constructor
    private static final LoadFile loadDataModelObj = new LoadFile();

    private LoadFile(){
        try {
            JsonParser parser = new JsonParser();
            //Reading the blockchain network network config
            Config = (JsonObject) parser.parse(new FileReader(ConfigOptions.BLOCKCHAIN_CONFIG_FILE_RESOLVED_PATH));
            dataModel = (JsonObject) parser.parse(new FileReader(Config.get("DATAMODEL_PATH").getAsString()));
            processModel = (JsonObject) parser.parse (new FileReader(Config.get("PROCESSJSON_PATH").getAsString()));
        }catch (FileNotFoundException fnf){
            LOG.error(fnf);
        }catch (Exception e){
            LOG.error(e);
        }
    }

    public static JsonObject getDataModel(){
        return dataModel;
    }

    public static JsonObject getProcessModel() {
        return processModel;
    }

    public static JsonObject getConfig() {
        return Config;
    }

}