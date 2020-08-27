/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformcommons.dal.multipart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;

public class MultipartDataHandler {

	private static Logger log = LogManager.getLogger(MultipartDataHandler.class);

	public boolean uploadMultipartFile(String url, Map<String, String> multipartFiles,
			Map<String, String> multipartFileData, Map<String, String> headers, String returnMediaType,
			String exportedFile)
			throws InsightsCustomException {
		boolean status = Boolean.FALSE;
		OutputStream outStream = null;
		try {
			InputStream initialStream = RestApiHandler.uploadMultipartFile(url, multipartFiles, multipartFileData,
					headers, returnMediaType);

			byte[] buffer = new byte[initialStream.available()];
			int readByte = initialStream.read(buffer);
			log.debug("exportedFile  {} ", exportedFile);
			if (readByte > 0) {
				File extractedPdfFile = new File(exportedFile);
				outStream = new FileOutputStream(extractedPdfFile);
				outStream.write(buffer);
				status = Boolean.TRUE;
				log.debug("extractedPdfFile status {} and  path   {} ", status, extractedPdfFile.getAbsoluteFile());

			} else {
				log.debug("Unable to read Input stream ");
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
		return status;

	}
}
