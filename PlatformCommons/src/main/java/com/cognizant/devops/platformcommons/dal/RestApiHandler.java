/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.dal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.exception.RestAPI404Exception;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RestApiHandler {

	private static Logger log = LogManager.getLogger(RestApiHandler.class);

	static Client client ;
	static Client multipartClient;
	static {
		try {


			initializeClient();
		} catch (Exception e) {
			log.error(e);
		}
	}

	/** Used to initialize Rest client for communication 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 * @throws InsightsCustomException 
	 * @throws Exception 
	 * 
	 */
	private static void initializeClient() throws NoSuchAlgorithmException, KeyManagementException, InsightsCustomException {
		SSLContext sslContext = null;
		try {
			TrustManager[] trustManager = new X509TrustManager[] { new X509TrustManager() {

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					X509Certificate[] myTrustedAnchors = new X509Certificate[0];
					for (X509Certificate cert : myTrustedAnchors) {
						try {
							cert.checkValidity();
						} catch (Exception e) {
							log.error(e);
						}
					}
					return myTrustedAnchors;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
					try {
						checkCertValidity(certs);
					} catch (Exception e) {
						log.error(e);
					}
				}

				private void checkCertValidity(X509Certificate[] certs)
						throws CertificateExpiredException, CertificateNotYetValidException {
					for (X509Certificate cert : certs) {
						cert.checkValidity();
					}
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
					try {
						checkCertValidity(certs);
					} catch (Exception e) {
						log.error(e);
					}
				}
			}};
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustManager, null);
			client = ClientBuilder.newBuilder().sslContext(sslContext).build();
			multipartClient = ClientBuilder.newBuilder().sslContext(sslContext).register(MultiPartFeature.class)
					.build();
			if(client == null) {
				throw new InsightsCustomException("unable to initilize client");
			}
			client.property(ClientProperties.CONNECT_TIMEOUT, 5001);
		} catch (NoSuchAlgorithmException e) {
			log.error("NoSuchAlgorithmException occured", e);
			throw new NoSuchAlgorithmException(e.getMessage());
		} catch (KeyManagementException e) {
			log.error("KeyManagementException occured ", e);
			throw new KeyManagementException(e.getMessage());
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}
	}

	private RestApiHandler() {

	}

	/**
	 * @param url
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public static String doGet(String url, Map<String, String> headers) throws InsightsCustomException {
		return getRequestBuilder(url, null, headers, HttpMethod.GET);
	}

	/**
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public static String doPost(String url, JsonObject requestJson, Map<String, String> headers)
			throws InsightsCustomException {
		return getRequestBuilder(url, requestJson, headers, HttpMethod.POST);
	}

	/**
	 * @param url
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public static String doDelete(String url, Map<String, String> headers) throws InsightsCustomException {
		return getRequestBuilder(url, null, headers, HttpMethod.DELETE);
	}

	/**
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public static String doPatch(String url, JsonObject requestJson, Map<String, String> headers)
			throws InsightsCustomException {
		return getRequestBuilder(url, requestJson, headers, HttpMethod.PATCH);
	}


	/**
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public static String doPut(String url, JsonObject requestJson, Map<String, String> headers)
			throws InsightsCustomException {
		return getRequestBuilder(url, requestJson, headers, HttpMethod.PUT);
	}

	/**
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @param action
	 * @return
	 * @throws InsightsCustomException
	 */
	private static String getRequestBuilder(String url, JsonObject requestJson, Map<String, String> headers,
			String action) throws InsightsCustomException {
		String returnStr = null;
		Builder invocationBuilder = null;
		Response response = null;
		WebTarget webTarget = null;
		try {
			webTarget = client.target(url);
			invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
			invocationBuilder = invocationBuilderSetter(headers, invocationBuilder);
			response = responseSetter(requestJson, action, invocationBuilder, response);

			if (response != null) {
				returnStr = response.readEntity(String.class);
				if (response.getStatus() == 404) {
					log.debug("HTTP error code for 404 received");
					JsonObject errorResponse = new JsonObject();
					errorResponse.addProperty(PlatformServiceConstants.STATUS, response.getStatus());
					errorResponse.addProperty(PlatformServiceConstants.DATA, returnStr);
					throw new RestAPI404Exception(errorResponse.toString());
				} else if (!(response.getStatus() == 200 || response.getStatus() == 204)) {
					JsonObject errorResponse = new JsonObject();
					errorResponse.addProperty(PlatformServiceConstants.STATUS, response.getStatus());
					errorResponse.addProperty(PlatformServiceConstants.DATA, returnStr);
					log.error(" HTTP response has issue for URL ");
					throw new InsightsCustomException(errorResponse.toString());
				}
			}
		} catch (ProcessingException e) {
			log.error("ProcessingException occured  ", e);
			throw e;
		} catch (RestAPI404Exception e) {
			log.error("Error while connecting to server RestAPI404Exception ", e);
			throw new RestAPI404Exception(e.getMessage());
		} catch (Exception e) {
			log.error("Error while connecting to server --", e);
			throw new InsightsCustomException(e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return returnStr;
	}

	private static Response responseSetter(JsonObject requestJson, String action, Builder invocationBuilder,
			Response response) {
		if (HttpMethod.POST.equalsIgnoreCase(action)) {
			response = invocationBuilder.post(Entity.json(requestJson.toString()), Response.class);
		} else if (HttpMethod.GET.equalsIgnoreCase(action)) {
			response = invocationBuilder.get(Response.class);
		} else if (HttpMethod.DELETE.equalsIgnoreCase(action)) {
			response = invocationBuilder.delete();
		} else if (HttpMethod.PATCH.equalsIgnoreCase(action)) {
			response = invocationBuilder.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
					.method("PATCH", Entity.json(requestJson.toString()));
		}else if (HttpMethod.PUT.equalsIgnoreCase(action)) {
			response = invocationBuilder.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
					.method("PUT", Entity.json(requestJson.toString()));
		}
		return response;
	}

	
	public static String httpQueryParamRequest(String url, JsonObject queryParams, Map<String, String> headers,String action) throws InsightsCustomException {
		String data = null;
		Builder invocationBuilder = null;
		Response response = null;
		WebTarget webTarget = null;
		try {
			webTarget = client.target(url);
			if (queryParams != null && !queryParams.entrySet().isEmpty()) {
				Set<Entry<String, JsonElement>> entitySet = queryParams.entrySet();
				for (Entry<String, JsonElement> entity : entitySet) {
					String key = entity.getKey();
					webTarget=webTarget.queryParam(key, queryParams.get(key).getAsString());
				}
			}
			invocationBuilder = webTarget.request();
			invocationBuilder = setInvocationBuilder(headers, invocationBuilder);
			if (HttpMethod.POST.equalsIgnoreCase(action)) {
				response = invocationBuilder.post(null, Response.class);
			} else if (HttpMethod.GET.equalsIgnoreCase(action)) {
				response = invocationBuilder.get(Response.class);
			}
			if (response != null) {
				data = response.readEntity(String.class);
				if (response.getStatus() == 404) {
					log.debug("HTTP error code for 404 received ");
					JsonObject errorResponse = new JsonObject();
					errorResponse.addProperty(PlatformServiceConstants.STATUS, response.getStatus());
					errorResponse.addProperty(PlatformServiceConstants.DATA, data);
					throw new RestAPI404Exception(errorResponse.toString());
				} else if (!(response.getStatus() == 200 || response.getStatus() == 204)) {
					throw new InsightsCustomException(
							"Failed : HTTP error code.. : " + response.getStatus() + " message " + data);
				}
			}
		} catch (ProcessingException e) {
			log.error("ProcessingException occured  ", e);
			throw e;
		} catch (RestAPI404Exception e) {
			log.error("Error while connecting to server RestAPI404Exception ", e);
			throw new RestAPI404Exception(e.getMessage());
		} catch (Exception e) {
			log.error("Error while connecting to server. ", e);
			throw new InsightsCustomException(e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return data;
	}

	private static Builder setInvocationBuilder(Map<String, String> headers, Builder invocationBuilder) {
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				invocationBuilder = invocationBuilder.header(entry.getKey(), entry.getValue());
			}
		}
		return invocationBuilder;
	}


	/**
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public static Map<String, NewCookie> getCookies(String url, JsonObject requestJson, Map<String, String> headers)
			throws InsightsCustomException {
		Builder invocationBuilder = null;
		Response response = null;
		WebTarget webTarget = null;
		Map<String, NewCookie> cookies = new HashMap<>();
		try {
			webTarget = client.target(url);
			invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
			invocationBuilder = invocationBuilderSetter(headers, invocationBuilder);

			response = invocationBuilder.post(Entity.json(requestJson.toString()), Response.class);

			if (response.getStatus() != 200) {
				throw new InsightsCustomException(
						"Failed : HTTP error code... : " + response.getStatus() + " response message are_ " + response);
			} else {
				cookies = response.getCookies();
			}
		} catch (Exception e) {
			log.error("Error while connecting to server. ", e);
			throw new InsightsCustomException(e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return cookies;
	}

	/**
	 * This method used to upload multipart file using API
	 * 
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 * @throws IOException 
	 */
	public static InputStream uploadMultipartFile(String url, Map<String, String> multipartFiles,
			Map<String, String> multipartFileData,
			Map<String, String> headers, String returnMediaType)
					throws InsightsCustomException, IOException {
		InputStream retunInputStream = null;
		Builder invocationBuilder = null;
		Response response = null;
		WebTarget webTarget = null;
		FileDataBodyPart filePart = null;
		//FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
		try(FormDataMultiPart formDataMultiPart = new FormDataMultiPart();) {

			if (multipartFiles != null && multipartFiles.size() > 0) {
				for (Map.Entry<String, String> entry : multipartFiles.entrySet()) {
					filePart = new FileDataBodyPart(entry.getKey(), new File(entry.getValue()));
				}
			}

			processFormDataMultiPart(multipartFileData,formDataMultiPart);

			formDataMultiPart.bodyPart(filePart);

			webTarget = multipartClient.target(url);
			invocationBuilder = webTarget.request().accept(returnMediaType);

			invocationBuilder = invocationBuilderSetter(headers, invocationBuilder); 

			response = invocationBuilder.post(Entity.entity(formDataMultiPart, formDataMultiPart.getMediaType()));

			if (response.getStatus() != 200) {
				throw new InsightsCustomException(
						"Failed : HTTP error code.... : " + response.getStatus() + " response message are__ " + response);
			} else {
				response.bufferEntity();
				retunInputStream = response.readEntity(InputStream.class);
			}

		} catch (Exception e) {
			log.error("Error while connecting to server..", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return retunInputStream;
	}

	private static void processFormDataMultiPart(Map<String, String> multipartFileData, FormDataMultiPart formDataMultiPart) {

		if (multipartFileData != null && multipartFileData.size() > 0) {
			for (Map.Entry<String, String> entry : multipartFileData.entrySet()) {
				formDataMultiPart = formDataMultiPart.field(entry.getKey(), entry.getValue());
			}
		}
	}

	private static Builder invocationBuilderSetter(Map<String, String> headers, Builder invocationBuilder) {
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				invocationBuilder = invocationBuilder.header(entry.getKey(), entry.getValue());
			}
		}
		return invocationBuilder;
	}

	/**
	 * This method used to upload multipart file using API
	 * 
	 * @param url
	 * @param requestJson
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	public static InputStream downloadMultipartFile(String url, Map<String, String> headers)
			throws InsightsCustomException {
		InputStream retunInputStream = null;
		Builder invocationBuilder = null;
		Response response = null;
		WebTarget webTarget = null;	
		try {
			webTarget = multipartClient.target(url);
			invocationBuilder = webTarget.request();
			invocationBuilder = invocationBuilderSetter(headers, invocationBuilder);
			response =invocationBuilder.get();
			if (response.getStatus() != 200) {
				throw new InsightsCustomException(
						"Failed : HTTP error code- : " + response.getStatus() + " response message are:-- " + response);
			} else {
				response.bufferEntity();
				retunInputStream = response.readEntity(InputStream.class);
			}

		} catch (Exception e) {
			log.error("Error while connecting to server- ", e);
			throw new InsightsCustomException(e.getMessage());
		} 		
		return retunInputStream;
	}



	public static String uploadMultipartFileWithData(String url, Map<String, String> multipartFiles,
			Map<String, String> multipartFileData,
			Map<String, String> headers, String returnMediaType)
					throws InsightsCustomException, IOException {
		String returnData = null;
		Builder invocationBuilder = null;
		Response response = null;
		WebTarget webTarget = null;
		FileDataBodyPart filePart = null;
		//FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
		try(FormDataMultiPart formDataMultiPart = new FormDataMultiPart();) {

			if (multipartFiles != null && multipartFiles.size() > 0) {
				for (Map.Entry<String, String> entry : multipartFiles.entrySet()) {
					filePart = new FileDataBodyPart(entry.getKey(), new File(entry.getValue()));
					formDataMultiPart.bodyPart(filePart);
				}
			}

			getUploadMulipartWithData(multipartFileData,formDataMultiPart);	

			formDataMultiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
			webTarget = multipartClient.target(url);
			invocationBuilder = webTarget.request().accept(returnMediaType);

			invocationBuilder = invocationBuilderSetter(headers, invocationBuilder);

			response = invocationBuilder.post(Entity.entity(formDataMultiPart, formDataMultiPart.getMediaType()));

			if (response.getStatus() != 200) {
				throw new InsightsCustomException(
						"Failed : HTTP error code : " + response.getStatus() + " response message are:- " + response);
			} else {
				returnData = response.readEntity(String.class);
			}

		} catch (Exception e) {
			log.error("Error while connecting to server : ", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return returnData;
	}

	private static void getUploadMulipartWithData(Map<String, String> multipartFileData, FormDataMultiPart formDataMultiPart) {

		if (multipartFileData != null && multipartFileData.size() > 0) {
			for (Map.Entry<String, String> entry : multipartFileData.entrySet()) {
				formDataMultiPart = formDataMultiPart.field(entry.getKey(), entry.getValue());
			}
		}	

	}
}
