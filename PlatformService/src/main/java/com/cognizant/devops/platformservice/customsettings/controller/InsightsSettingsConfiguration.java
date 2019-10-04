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
package com.cognizant.devops.platformservice.customsettings.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformdal.icon.Icon;
import com.cognizant.devops.platformdal.icon.IconDAL;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfiguration;
import com.cognizant.devops.platformservice.customsettings.ImageResponse;
import com.cognizant.devops.platformservice.customsettings.service.SettingsConfigurationService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/settings")
public class InsightsSettingsConfiguration {

	private static final Logger LOG = LogManager.getLogger(InsightsSettingsConfiguration.class);

	@Autowired	
	SettingsConfigurationService settingsConfigurationService;

	@RequestMapping(value = "/saveSettingsConfiguration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject saveSettingsConfiguration(@RequestParam String settingsJson, @RequestParam String settingsType,
													@RequestParam String activeFlag,@RequestParam String lastModifiedByUser) {
		Boolean result = settingsConfigurationService.saveSettingsConfiguration(settingsJson,settingsType,activeFlag,lastModifiedByUser);
		if (result) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to save or update Setting Configuration for the request");
		}
	}

	@RequestMapping(value = "/loadSettingsConfiguration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadSettingsConfiguration(@RequestParam String settingsType ) {
		SettingsConfiguration settingsConfiguration = settingsConfigurationService.loadSettingsConfiguration(settingsType);		
		return PlatformServiceUtil.buildSuccessResponseWithData(settingsConfiguration);
	}

	@RequestMapping(value = "/uploadCustomLogo",headers=("content-type=multipart/*"), method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject handleFileUpload(@RequestParam("file") MultipartFile file) {
		InputStream inputStream = null;
		String originalFilename = file.getOriginalFilename();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		if (fileExt.equalsIgnoreCase("png") && file.getSize() < 1048576) {
			byte[] imageBytes = null;
			try {
				inputStream = new BufferedInputStream(file.getInputStream());
				imageBytes = IOUtils.toByteArray(inputStream);
			} catch (IOException e) {
				LOG.error("Unable to upload custom logo", e);
			}
			Icon icon = new Icon();
			icon.setIconId("logo");
			icon.setImage(imageBytes);
			icon.setFileName(originalFilename);
			icon.setImageType(fileExt);
			IconDAL dal = new IconDAL();
			dal.addEntityData(icon);
			String base64 = Base64.getEncoder().encodeToString(imageBytes);
			ImageResponse imgResp = new ImageResponse();
			imgResp.setEncodedString(base64);
			imgResp.setImageType(fileExt);
			return PlatformServiceUtil.buildSuccessResponseWithData(imgResp);
		} else {
			LOG.error("Invalid file  " + file.getName() + "  With extension  " + fileExt + " size " + file.getSize());
			return PlatformServiceUtil.buildFailureResponse(PlatformServiceConstants.INVALID_FILE);
		}
	}


}
