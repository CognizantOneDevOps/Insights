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


import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.util.Matrix;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.google.gson.JsonObject;

public class PdfSignUtil extends PdfCreateSignatureBase
{
	private static final Logger log = LogManager.getLogger(PdfSignUtil.class.getName());
	
	private boolean lateExternalSigning = false;
	private File imageFile;
	private static final String PDF_PATH = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR + File.separator + "Pdf" + File.separator;
	private static final String TARGET_PDF = "target/Traceability_report.pdf";
	private static final String PDF_NAME_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.pdf)$";
	
	public PdfSignUtil(KeyStore keystore, char[] pin)
			throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException
	{
		super(keystore, pin);
	}

	public PdfSignUtil() {
		super();
	}

	/**
	 * Sign pdf file and create new file that ends with "_signed.pdf".
	 *
	 * @param inputFile The source pdf document file.
	 * @param signedFile The file to be signed.
	 * @param humanRect rectangle from a human viewpoint (coordinates start at top left)
	 * @param tsaUrl optional TSA url
	 * @throws IOException
	 */
	public void signPDF(PDDocument protectDoc, File signedFile, Rectangle2D humanRect, String tsaUrl) throws IOException
	{
		this.signPDF(protectDoc, signedFile, humanRect, tsaUrl, null);
	}

	/**
	 * Sign pdf file and create new file that ends with "_signed.pdf".
	 *
	 * @param protectDoc The source pdf document file.
	 * @param signedFile The file to be signed.
	 * @param humanRect rectangle from a human viewpoint (coordinates start at top left)
	 * @param tsaUrl optional TSA url
	 * @param signatureFieldName optional name of an existing (unsigned) signature field
	 * @throws IOException
	 */
	public byte[] signPDF(PDDocument protectDoc, File signedFile, Rectangle2D humanRect, String tsaUrl, String signatureFieldName) throws IOException
	{
		if (protectDoc == null)
		{
			log.info("Input file in sigining - "+protectDoc);
			throw new IOException("Document for signing does not exist");
		}

		setTimeStampUrl(tsaUrl);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		protectDoc.save(byteArrayOutputStream);
		PDDocument doc = PDDocument.load(byteArrayOutputStream.toByteArray(),"12345");
		int accessPermissions = SignatureUtils.getMDPPermission(doc);
		if (accessPermissions == 1)
		{
			throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
		}

		PDSignature signature = null;
		PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
		PDRectangle rect = null;

		if (acroForm != null)
		{
			signature = findExistingSignature(acroForm, signatureFieldName);
			if (signature != null)
			{
				rect = acroForm.getField(signatureFieldName).getWidgets().get(0).getRectangle();
			}
		}

		if (signature == null)
		{
			signature = new PDSignature();
		}

		if (rect == null)
		{
			rect = createSignatureRectangle(doc, humanRect);
		}

		if (doc.getVersion() >= 1.5f && accessPermissions == 0)
		{
			SignatureUtils.setMDPPermission(doc, signature, 2);
		}

		if (acroForm != null && acroForm.getNeedAppearances())
		{
			if (acroForm.getFields().isEmpty())
			{
				acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
			}
			else
			{
				log.info("/NeedAppearances is set, signature may be ignored by Adobe Reader");
			}
		}
		signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
		signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
		signature.setName("Insights");
		signature.setLocation("Location");
		signature.setReason("Asset Audit Report");
		signature.setSignDate(Calendar.getInstance());
		SignatureInterface signatureInterface = isExternalSigning() ? null : this;
		SignatureOptions signatureOptions = new SignatureOptions();
		signatureOptions.setVisualSignature(createVisualSignatureTemplate(doc, doc.getNumberOfPages()-1, rect, signature));
		signatureOptions.setPage(doc.getNumberOfPages()-1);
		doc.addSignature(signature, signatureInterface, signatureOptions);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.saveIncremental(baos);
		IOUtils.closeQuietly(signatureOptions);
		doc.close();
		protectDoc.close();
		return baos.toByteArray();
	}

	private PDRectangle createSignatureRectangle(PDDocument doc, Rectangle2D humanRect)
	{
		float x = (float) humanRect.getX();
		float y = (float) humanRect.getY();
		float width = (float) humanRect.getWidth();
		float height = (float) humanRect.getHeight();
		PDPage page = doc.getPage(doc.getNumberOfPages()-1);
		PDRectangle pageRect = page.getCropBox();
		PDRectangle rect = new PDRectangle();
		switch (page.getRotation())
		{
		case 90:
			rect.setLowerLeftY(x);
			rect.setUpperRightY(x + width);
			rect.setLowerLeftX(y);
			rect.setUpperRightX(y + height);
			break;
		case 180:
			rect.setUpperRightX(pageRect.getWidth() - x);
			rect.setLowerLeftX(pageRect.getWidth() - x - width);
			rect.setLowerLeftY(y);
			rect.setUpperRightY(y + height);
			break;
		case 270:
			rect.setLowerLeftY(pageRect.getHeight() - x - width);
			rect.setUpperRightY(pageRect.getHeight() - x);
			rect.setLowerLeftX(pageRect.getWidth() - y - height);
			rect.setUpperRightX(pageRect.getWidth() - y);
			break;
		case 0:
		default:
			rect.setLowerLeftX(x);
			rect.setUpperRightX(x + width);
			rect.setLowerLeftY(pageRect.getHeight() - y - height);
			rect.setUpperRightY(pageRect.getHeight() - y);
			break;
		}
		return rect;
	}

	private InputStream createVisualSignatureTemplate(PDDocument srcDoc, int pageNum, PDRectangle rect, PDSignature signature) throws IOException
	{
		PDDocument doc = new PDDocument();
		PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
		doc.addPage(page);
		PDAcroForm acroForm = new PDAcroForm(doc);
		doc.getDocumentCatalog().setAcroForm(acroForm);
		PDSignatureField signatureField = new PDSignatureField(acroForm);
		PDAnnotationWidget widget = signatureField.getWidgets().get(0);
		List<PDField> acroFormFields = acroForm.getFields();
		acroForm.setSignaturesExist(true);
		acroForm.setAppendOnly(true);
		acroForm.getCOSObject().setDirect(true);
		acroFormFields.add(signatureField);

		widget.setRectangle(rect);

		PDStream stream = new PDStream(doc);
		PDFormXObject form = new PDFormXObject(stream);
		PDResources res = new PDResources();
		form.setResources(res);
		form.setFormType(1);
		PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
		float height = bbox.getHeight();
		Matrix initialScale = null;
		switch (srcDoc.getPage(pageNum).getRotation())
		{
		case 90:
			form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
			initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
			height = bbox.getWidth();
			break;
		case 180:
			form.setMatrix(AffineTransform.getQuadrantRotateInstance(2)); 
			break;
		case 270:
			form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
			initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
			height = bbox.getWidth();
			break;
		case 0:
		default:
			break;
		}
		form.setBBox(bbox);
		PDFont font = PDType1Font.HELVETICA_BOLD;

		PDAppearanceDictionary appearance = new PDAppearanceDictionary();
		appearance.getCOSObject().setDirect(true);
		PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
		appearance.setNormalAppearance(appearanceStream);
		widget.setAppearance(appearance);

		PDPageContentStream cs = new PDPageContentStream(doc, appearanceStream);
		if (initialScale != null)
		{
			cs.transform(initialScale);
		}

		cs.setNonStrokingColor(Color.WHITE);
		cs.addRect(-5000, -5000, 10000, 10000);
		cs.fill();

		cs.saveGraphicsState();
		cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
		PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, doc);
		cs.drawImage(img, 0, 0);
		cs.restoreGraphicsState();

		float fontSize = 6;
		float leading = fontSize * 1.5f;
		cs.beginText();
		cs.setFont(font, fontSize);
		cs.setNonStrokingColor(Color.black);
		cs.newLineAtOffset(fontSize, height - leading);
		cs.setLeading(leading);
		X509Certificate cert = (X509Certificate) getCertificates()[0];
		X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
		RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
		String name = IETFUtils.valueToString(cn.getFirst().getValue());
		log.info("Digitally Signed by -  {}",name);
		String date = signature.getSignDate().getTime().toString();
		log.info("Date - {}",date);
		String reason = signature.getReason();
		log.info("Reason - {}",reason);
		cs.showText("Digitally Signed by "+name);
		cs.newLine();
		cs.showText("Reason:"+reason);
		cs.newLine();
		cs.showText("Date :"+date);
		cs.endText();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cs.close();
		doc.save(baos);
		return new ByteArrayInputStream(baos.toByteArray());
	}

	private PDSignature findExistingSignature(PDAcroForm acroForm, String sigFieldName)
	{
		PDSignature signature = null;
		PDSignatureField signatureField;
		if (acroForm != null)
		{
			signatureField = (PDSignatureField) acroForm.getField(sigFieldName);
			if (signatureField != null)
			{
				signature = signatureField.getSignature();
				if (signature == null)
				{
					signature = new PDSignature();
					signatureField.getCOSObject().setItem(COSName.V, signature);
				}
				else
				{
					throw new IllegalStateException("The signature field " + sigFieldName + " is already signed.");
				}
			}
		}
		return signature;
	}


	/**
	 * Arguments are
	 * [0] key store
	 * [1] pin
	 * [2] document that will be signed
	 * [3] image of visible signature
	 * generate with
	 * 				keytool -storepass 123456 -storetype PKCS12 -keystore file.p12 -genkey -alias client -keyalg RSA
	 * @param doc 
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws UnrecoverableKeyException
	 */
	public byte[] digitalSign(PDDocument doc, String pdfName) throws KeyStoreException, IOException, NoSuchAlgorithmException,
	CertificateException, UnrecoverableKeyException {
		byte[] signdoc = null;
		try{
			log.info("Pdf Name in sign == {} ",pdfName);
			String tsaUrl = null;
			boolean externalSig = false;
			JsonObject config = LoadFile.getConfig();
			File ksFile = new File(PDF_PATH+config.get("KEYSTORE_P12").getAsString()).getAbsoluteFile();
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			char[] pin = config.get("PIN_PROTECT").getAsString().toCharArray();
			keystore.load(new FileInputStream(ksFile), pin);
			boolean valid = checkValidFile(pdfName);
			if(valid){
				File documentFile = new File(pdfName==null ? TARGET_PDF : pdfName);
				log.info("documentFile name sign--{}",documentFile.getName());
				PdfSignUtil signing = new PdfSignUtil(keystore, pin.clone());
				signing.setImageFile(new File(PDF_PATH+config.get("DIGI_IMG").getAsString()).getAbsoluteFile());
				File signedDocumentFile;
				String name = pdfName;
				String substring = name.substring(0, name.lastIndexOf('.'));
				signedDocumentFile = new File(documentFile.getParent(), substring + "_signed.pdf");
				log.info("signedDocumentFile sign-- {}",signedDocumentFile.getName());
				signing.setExternalSigning(externalSig);

				Rectangle2D humanRect = new Rectangle2D.Float(100, 200, 150, 50);
				log.info("--Signining PDF---");
				signdoc = signing.signPDF(doc, signedDocumentFile, humanRect, tsaUrl, "Signature1");
			}else{
				log.info("PDF name is not valid for Regex -- {}",PDF_NAME_VALIDATOR);
			}
		}catch(Exception e){
			log.error(e);
		}
		return signdoc;
	}
	
	/**
	 * Checks for pdf name
	 * @param pdfName
	 * @return validname
	 */
	private boolean checkValidFile(String pdfName) {
		final Pattern pattern = Pattern.compile(PDF_NAME_VALIDATOR, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(pdfName);

		if(matcher.find()) {
		    log.debug("File name is Valid for regex -- "+PDF_NAME_VALIDATOR);
		    return true;
		} 
		
		return false;
	}
	
	public File getImageFile()
	{
		return imageFile;
	}

	public void setImageFile(File imageFile)
	{
		this.imageFile = imageFile;
	}

	public boolean isLateExternalSigning()
	{
		return lateExternalSigning;
	}

	public void setLateExternalSigning(boolean lateExternalSigning)
	{
		this.lateExternalSigning = lateExternalSigning;
	}

}