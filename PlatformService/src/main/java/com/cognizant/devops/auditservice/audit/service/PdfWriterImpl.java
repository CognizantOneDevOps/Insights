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
package com.cognizant.devops.auditservice.audit.service;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.xml.bind.JAXBException;

import org.springframework.stereotype.Service;

import com.cognizant.devops.platformauditing.util.PdfTableUtil;
import com.google.gson.JsonArray;

/**
 *	All pdf write operations are customized here.
 */
@Service
public class PdfWriterImpl implements PdfWriter{
	
	@Override
	public byte[] generatePdf(JsonArray assetList, String pdfName) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, JAXBException{
		PdfTableUtil pdfTableUtil = new PdfTableUtil();
		return pdfTableUtil.generateTableContent(assetList, pdfName);		
	}

}
