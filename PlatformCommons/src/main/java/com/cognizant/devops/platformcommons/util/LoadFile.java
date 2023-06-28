/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformcommons.util;

import com.google.gson.JsonObject;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;


public class LoadFile {
    private static final Logger LOG = LogManager.getLogger(LoadFile.class);
    private JsonObject dataModel = new JsonObject();
    private JsonObject processModel = new JsonObject();
    private JsonObject config;
    //this object creation loads the datamodel by calling the constructor
    private static final LoadFile loadDataModelObj = new LoadFile();

    private LoadFile(){
        try {
            //Reading the blockchain network network config
            config = JsonUtils.parseReaderAsJsonObject(new FileReader(ConfigOptions.BLOCKCHAIN_CONFIG_FILE_RESOLVED_PATH));
            dataModel = JsonUtils.parseReaderAsJsonObject(new FileReader(config.get("DATAMODEL_JSON_PATH").getAsString()));
            processModel = JsonUtils.parseReaderAsJsonObject(new FileReader(config.get("PROCESS_JSON_PATH").getAsString()));
        }catch (FileNotFoundException fnf){
            LOG.error(fnf);
        }catch (Exception e){
            LOG.error(e);
        }
    }
    
    public static LoadFile getInstance(){
    	return loadDataModelObj;
    }
    
    public JsonObject getDataModel(){
        return dataModel;
    }

    public JsonObject getProcessModel() {
        return processModel;
    }

    public JsonObject getConfig() {
        return config;
    }

}