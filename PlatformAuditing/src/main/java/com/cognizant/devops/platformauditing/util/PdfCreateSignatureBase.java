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
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;


public abstract class PdfCreateSignatureBase implements SignatureInterface
{
    private PrivateKey privateKey;
    private Certificate[] certificates;
    private String timeStampUrl;
    private boolean externalSigning;

    /**
     * Initialize the signature creator with a keystore (pkcs12) and pin that should be used for the
     * signature.
     *
     * @param keystore is a pkcs12 keystore.
     * @param pin is the pin for the keystore / private key
     * @throws KeyStoreException if the keystore has not been initialized (loaded)
     * @throws NoSuchAlgorithmException if the algorithm for recovering the key cannot be found
     * @throws UnrecoverableKeyException if the given password is wrong
     * @throws CertificateException if the certificate is not valid as signing time
     * @throws IOException if no certificate could be found
     */
    public PdfCreateSignatureBase(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException
    {
        Enumeration<String> aliases = keystore.aliases();
        String alias;
        Certificate cert = null;
        while (aliases.hasMoreElements())
        {
            alias = aliases.nextElement();
            setPrivateKey((PrivateKey) keystore.getKey(alias, pin));
            Certificate[] certChain = keystore.getCertificateChain(alias);
            if (certChain == null)
            {
                continue;
            }
            setCertificates(certChain);
            cert = certChain[0];
            if (cert instanceof X509Certificate)
            {
                ((X509Certificate) cert).checkValidity();
                SignatureUtils.checkCertificateUsage((X509Certificate) cert);
            }
            break;
        }

        if (cert == null)
        {
            throw new IOException("Could not find certificate");
        }
    }
    
    
    /**
     * SignatureInterface implementation.
     *
     * This method will be called from inside of the pdfbox and create the PKCS #7 signature.
     * The given InputStream contains the bytes that are given by the byte range.
     *
     * Use your favorite cryptographic library to implement PKCS #7 signature creation.
     *
     * @throws IOException
     */
    public byte[] sign(InputStream content) throws IOException
    {
        try
        {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            X509Certificate cert = (X509Certificate) certificates[0];
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert));
            gen.addCertificates(new JcaCertStore(Arrays.asList(certificates)));
            BountyInputStream msg = new BountyInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);
            if (timeStampUrl != null && timeStampUrl.length() > 0)
            {
                ValidateTS validation = new ValidateTS(timeStampUrl);
                signedData = validation.addSignedTimeStamp(signedData);
            }
            return signedData.getEncoded();
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }
    }

    public PdfCreateSignatureBase() {
		// TODO Auto-generated constructor stub
	}

	public final void setPrivateKey(PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }

    public final void setCertificates(final Certificate[] certificates)
    {
        this.certificates = certificates;
    }
    
    public Certificate[] getCertificates()
    {
    	return certificates;
    }

    public void setTimeStampUrl(String timeStampUrl)
    {
        this.timeStampUrl = timeStampUrl;
    }

    public void setExternalSigning(boolean externalSigning)
    {
        this.externalSigning = externalSigning;
    }

    public boolean isExternalSigning()
    {
        return externalSigning;
    }
}