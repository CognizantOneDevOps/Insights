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

import org.springframework.stereotype.Service;

import com.cognizant.devops.auditservice.audit.utils.PdfTableUtil;

/**
 *	All pdf write operations are customized here.
 */
@Service
public class PdfWriterImpl implements PdfWriter{
	
	@Override
	public byte[] generatePdf(List<Map> assetList, String pdfName) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, JAXBException{
		PdfTableUtil pdfTableUtil = new PdfTableUtil();
		return pdfTableUtil.generateTableContent(assetList, pdfName);		
	}
	
//	@Override
//	public void signPdf(String pdfName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
//		PdfSignUtil pdfSignUtil =new PdfSignUtil();
//		pdfSignUtil.digitalSign(pdfName);
//	}
//
//	@Override
//	public void protectPdf(String pdfName) throws IOException {
//		PdfTableUtil pdfTableUtil = new PdfTableUtil();
//		pdfTableUtil.protectPdf(pdfName);
//	}


}
