/**
 * 
 */
package com.cognizant.devops.auditservice.audit.service;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 *
 */

public interface PdfWriter {

	public byte[] generatePdf(List<Map> assetList, String pdfName) throws IOException, JAXBException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException;

	//public void protectPdf(String pdfName) throws IOException;

	//public void signPdf(String pdfName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException;
}
