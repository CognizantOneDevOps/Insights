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
package com.cognizant.devops.platformservice.rest.customSettings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformdal.icon.Icon;
import com.cognizant.devops.platformdal.icon.IconDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/settings")
public class CustomAppSettings {

	private static final String INSIGHTS_HOME = "INSIGHTS_HOME";

	private static Logger LOG = Logger.getLogger(CustomAppSettings.class);

	@Autowired
	private HttpServletRequest request;

	@RequestMapping(value = "/uploadCustomLogo",headers=("content-type=multipart/*"), method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject handleFileUpload(@RequestParam("file") MultipartFile file) {
		/**if (!file.isEmpty()) {
			try {
				String originalFilename = file.getOriginalFilename();
				File[] dirFiles = getFilesinDir();
		        if(dirFiles.length > 0){
			        for (File dirfile : dirFiles)
			        {
			            if (!dirfile.isDirectory())
			            {
			            	dirfile.delete();
			            	
			            	FileDeleteStrategy.FORCE.delete(dirfile);
			            }
			        }
		        }
				File destinationFile = new File(System.getenv().get(INSIGHTS_HOME) + File.separator + originalFilename);
				file.transferTo(destinationFile);
				
			} catch (IllegalStateException e) {
				LOG.error("Unable to upload custom logo",e);
			} catch (IOException e) {	
				LOG.error("Unable to upload custom logo",e);
			}
		}**/
		InputStream inputStream = null;
		String originalFilename = file.getOriginalFilename();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		byte[] imageBytes = null;
		try {
			inputStream = new BufferedInputStream(file.getInputStream());
			imageBytes = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			LOG.error("Unable to upload custom logo",e);
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
	}
	
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
