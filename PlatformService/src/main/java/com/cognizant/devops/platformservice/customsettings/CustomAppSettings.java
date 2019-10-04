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
package com.cognizant.devops.platformservice.customsettings;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformdal.icon.Icon;
import com.cognizant.devops.platformdal.icon.IconDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/settings")
public class CustomAppSettings {

	private static final String INSIGHTS_HOME = "INSIGHTS_HOME";

	private static Logger LOG = LogManager.getLogger(CustomAppSettings.class);

	/*@Autowired
	private HttpServletRequest request;*/

	
	
    @CrossOrigin
	@RequestMapping(value = "/getLogoImage", method = RequestMethod.GET )
	public @ResponseBody JsonObject getImageWithMediaType() throws IOException {
		/**String base64 = "";
		String fileExt = "";
		File[] dirFiles = getFilesinDir();
        if(dirFiles.length > 0){
	        String imageFileName = dirFiles[0].getName();
	        fileExt = FilenameUtils.getExtension(imageFileName);
			InputStream in = new  FileInputStream(System.getenv().get(INSIGHTS_HOME) + File.separator + imageFileName);
			byte[] imageBytes = IOUtils.toByteArray(in);
			base64 = Base64.getEncoder().encodeToString(imageBytes);
        }**/
		Icon image = new IconDAL().fetchEntityData("logo");
		ImageResponse imgResp = new ImageResponse();
		String base64 = "";
		String imageType = "";
		if (null != image.getImageType()) {
			imageType = image.getImageType();
		}
		if (null != image.getImage()) {
			byte[] imageBytes = image.getImage();
			base64 = Base64.getEncoder().encodeToString(imageBytes);
		}
		imgResp.setEncodedString(base64);
		imgResp.setImageType(imageType);
		return PlatformServiceUtil.buildSuccessResponseWithData(imgResp);
	}

	private File[] getFilesinDir() {
		File dir = new File(System.getenv().get(INSIGHTS_HOME));
        File[] dirFiles = dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith(".png") || filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".svg"); }
        } );
		return dirFiles;
	}
	
}
