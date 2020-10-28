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
package com.cognizant.devops.platformservice.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.WebApplicationInitializer;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsResponseHeaderWriterFilter;
/**
 * 
 * @author 146414
 * This class will initialize the config.json.
 */
public class PlatformServiceInitializer implements WebApplicationInitializer {
	static Logger log = LogManager.getLogger(PlatformServiceInitializer.class.getName());

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		ApplicationConfigCache.loadConfigCache();
		disableSslVerification();
		validateAutheticationProtocol();
		servletContext.addFilter("InsightsResponseHeaderWriter", InsightsResponseHeaderWriterFilter.class)
				.addMappingForUrlPatterns(null, false, "/*");
	}

	private void validateAutheticationProtocol() {
		String autheticationProtocol = ApplicationConfigProvider.getInstance().getAutheticationProtocol();
		log.debug(" Authetication Protocol for applications {} ", autheticationProtocol);
		Assert.isTrue(AuthenticationUtils.AUTHENTICATION_PROTOCOL_LIST.contains(autheticationProtocol),
				"Please provide valid authetication Protocol from list "
					+ AuthenticationUtils.AUTHENTICATION_PROTOCOL_LIST.toString());
	}

	private static void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            @Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return new X509Certificate[0];
	            }
	            @Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException{
						try {
							for (X509Certificate cert : certs) {
								cert.checkValidity();
							}
						} catch (Exception e) {
							throw new CertificateException("Certificate not valid or trusted.");
						}
	            }
	            @Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException{
						try {
							for (X509Certificate cert : certs) {
								cert.checkValidity();
							}
						} catch (Exception e) {
							throw new CertificateException("Certificate not valid or trusted.");
						}
	            }
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("TLS");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = (hostname,session) -> hostname.equalsIgnoreCase(session.getPeerHost());
	        
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException | KeyManagementException e) {
	        log.error(e);
	    } 
	}

}
