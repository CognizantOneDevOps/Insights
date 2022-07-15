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
package com.cognizant.devops.platformauditing.util;

import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.x509.KeyPurposeId;

public class SignatureUtils {
	private static final String TRANSFORMPARAMS = "TransformParams";
	private static final Log LOG = LogFactory.getLog(SignatureUtils.class);

	private SignatureUtils() {
	}

	public static int getMDPPermission(PDDocument doc) {
		COSBase base = doc.getDocumentCatalog().getCOSObject().getDictionaryObject(COSName.PERMS);
		if (base instanceof COSDictionary) {
			COSDictionary permsDict = (COSDictionary) base;
			base = permsDict.getDictionaryObject(COSName.DOC);
			if (base instanceof COSDictionary) {
				COSDictionary signatureDict = (COSDictionary) base;
				base = signatureDict.getDictionaryObject("Reference");
				if (base instanceof COSArray) {
					COSArray refArray = (COSArray) base;				
					return accessPermissions(refArray,base);				
				}
			}
		}
		return 0;
	}
	
	private static int accessPermissions(COSArray refArray, COSBase base) {
		
		for (int i = 0; i < refArray.size(); ++i) {
			base = refArray.getObject(i);
			if (base instanceof COSDictionary) {
				COSDictionary sigRefDict = (COSDictionary) base;			
			    return validateAccess(sigRefDict,base);			
			}
		}
		return 0;
	}
	
	private static int validateAccess(COSDictionary sigRefDict, COSBase base) {
		
		if (COSName.DOC.equals(sigRefDict.getDictionaryObject(TRANSFORMPARAMS))) {
			base = sigRefDict.getDictionaryObject(TRANSFORMPARAMS);
			if (base instanceof COSDictionary) {
				COSDictionary transformDict = (COSDictionary) base;
				int accessPermissions = transformDict.getInt(COSName.P, 2);
				if (accessPermissions < 1 || accessPermissions > 3) {
					accessPermissions = 2;
				}
				return accessPermissions;
			}
		}
		return 0;
	}

	public static void setMDPPermission(PDDocument doc, PDSignature signature, int accessPermissions) {
		COSDictionary sigDict = signature.getCOSObject();

		// DocMDP specific stuff
		COSDictionary transformParameters = new COSDictionary();
		transformParameters.setItem(COSName.TYPE, COSName.getPDFName(TRANSFORMPARAMS));
		transformParameters.setInt(COSName.P, accessPermissions);
		transformParameters.setName(COSName.V, "1.2");
		transformParameters.setNeedToBeUpdated(true);

		COSDictionary referenceDict = new COSDictionary();
		referenceDict.setItem(COSName.TYPE, COSName.getPDFName("SigRef"));
		referenceDict.setItem("TransformMethod", COSName.DOC);
		referenceDict.setItem("DigestMethod", COSName.getPDFName("SHA1"));
		referenceDict.setItem(TRANSFORMPARAMS, transformParameters);
		referenceDict.setNeedToBeUpdated(true);

		COSArray referenceArray = new COSArray();
		referenceArray.add(referenceDict);
		sigDict.setItem("Reference", referenceArray);
		referenceArray.setNeedToBeUpdated(true);

		// Catalog
		COSDictionary catalogDict = doc.getDocumentCatalog().getCOSObject();
		COSDictionary permsDict = new COSDictionary();
		catalogDict.setItem(COSName.PERMS, permsDict);
		permsDict.setItem(COSName.DOC, signature);
		catalogDict.setNeedToBeUpdated(true);
		permsDict.setNeedToBeUpdated(true);
	}

	public static void checkCertificateUsage(X509Certificate x509Certificate) throws CertificateParsingException {
		boolean[] keyUsage = x509Certificate.getKeyUsage();
		if (keyUsage != null && !keyUsage[0] && !keyUsage[1]) {
			LOG.error("Certificate key usage does not include " + "digitalSignature nor nonRepudiation");
		}
		List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
		if (extendedKeyUsage != null && !extendedKeyUsage.contains(KeyPurposeId.id_kp_emailProtection.toString())
				&& !extendedKeyUsage.contains(KeyPurposeId.id_kp_codeSigning.toString())
				&& !extendedKeyUsage.contains(KeyPurposeId.anyExtendedKeyUsage.toString())
				&& !extendedKeyUsage.contains("1.2.840.113583.1.1.5")
				&& !extendedKeyUsage.contains("1.3.6.1.4.1.311.10.3.12")) {
			LOG.error("Certificate extended key usage does not include "
					+ "emailProtection, nor codeSigning, nor anyExtendedKeyUsage, "
					+ "nor 'Adobe Authentic Documents Trust'");
		}
	}

	/**
	 * Log if the certificate is not valid for timestamping.
	 *
	 * @param x509Certificate
	 * @throws java.security.cert.CertificateParsingException
	 */
	public static void checkTimeStampCertificateUsage(X509Certificate x509Certificate)
			throws CertificateParsingException {
		List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
		if (extendedKeyUsage != null && !extendedKeyUsage.contains(KeyPurposeId.id_kp_timeStamping.toString())) {
			LOG.error("Certificate extended key usage does not include timeStamping");
		}
	}

	public static void checkResponderCertificateUsage(X509Certificate x509Certificate)
			throws CertificateParsingException {
		List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
		if (extendedKeyUsage != null && !extendedKeyUsage.contains(KeyPurposeId.id_kp_OCSPSigning.toString())) {
			LOG.error("Certificate extended key usage does not include OCSP responding");
		}
	}

	public static PDSignature getLastRelevantSignature(PDDocument document) throws IOException {
		SortedMap<Integer, PDSignature> sortedMap = new TreeMap<Integer, PDSignature>();
		for (PDSignature signature : document.getSignatureDictionaries()) {
			int sigOffset = signature.getByteRange()[1];
			sortedMap.put(sigOffset, signature);
		}
		if (sortedMap.size() > 0) {
			PDSignature lastSignature = sortedMap.get(sortedMap.lastKey());
			COSBase type = lastSignature.getCOSObject().getItem(COSName.TYPE);
			if (type.equals(COSName.SIG) || type.equals(COSName.DOC_TIME_STAMP)) {
				return lastSignature;
			}
		}
		return null;
	}
}
