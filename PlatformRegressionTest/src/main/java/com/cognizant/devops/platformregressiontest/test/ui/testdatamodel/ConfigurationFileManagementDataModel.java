/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.testdatamodel;

public class ConfigurationFileManagementDataModel {

	private String configurationFile;
	private String filename;
	private String filetype;
	private String module;
	private String filepath;
	private String updatedfilepath;

	public String getConfigurationFile() {
		return configurationFile;
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFiletype() {
		return filetype;
	}

	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getUpdatedfilepath() {
		return updatedfilepath;
	}

	public void setUpdatedfilepath(String updatedfilepath) {
		this.updatedfilepath = updatedfilepath;
	}

}
