/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.core.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ValidationUtils {
	
	private ValidationUtils() {
		super();
	}
	
	private static final Logger log = LogManager.getLogger(ValidationUtils.class);
	private static Pattern agentNamePattern = Pattern.compile("[^A-Za-z]", Pattern.CASE_INSENSITIVE);
	private static Pattern agentIdPattern = Pattern.compile("[^A-Za-z0-9\\_]", Pattern.CASE_INSENSITIVE);
	private static Pattern agentVersionPattern = Pattern.compile("[v0-9]", Pattern.CASE_INSENSITIVE);
	private static Pattern LabelPattern = Pattern.compile("[^A-Za-z0-9\\_\\.]", Pattern.CASE_INSENSITIVE);

	public static String checkHTTPResponseSplitting(String value, boolean isReplace) {
		Pattern CRLF = Pattern.compile(ConfigOptions.CRLF_PATTERN);
		Matcher valueMatcher = CRLF.matcher(value);
		if (valueMatcher.find()) {
			if (isReplace) {
				value = value.replace("\\n", "").replace("\\r", "");
			} else {
				value = "";
			}
		}
		return value;
	}

	public static boolean checkString(String toolName) {
		boolean returnBoolean = false;
		Matcher m = agentNamePattern.matcher(toolName);
		if (m.find()) {
			returnBoolean = true;
		}
		return returnBoolean;
	}

	public static boolean checkNewLineCarriage(String value) {
		Pattern CRLF = Pattern.compile(ConfigOptions.CRLF_PATTERN);
		boolean returnBoolean = false;
		Matcher m = CRLF.matcher(value);
		if (m.find()) {
			returnBoolean = true;
		}
		return returnBoolean;
	}

	public static boolean checkAgentIdString(String agentId) {
		boolean returnBoolean = false;
		Matcher m = agentIdPattern.matcher(agentId);
		if (m.find()) {
			returnBoolean = true;
		}
		return returnBoolean;
	}
	
	public static boolean checkAgentVersion(String version) {
		boolean returnBoolean = false;
		Matcher m = agentVersionPattern.matcher(version);
		if (m.find()) {
			returnBoolean = true;
		}
		return returnBoolean;
	}

	public static boolean checkLabelNameString(String labelData) {
		boolean returnBoolean = false;
		Matcher m = LabelPattern.matcher(labelData);
		if (m.find()) {
			returnBoolean = true;
		}
		return returnBoolean;
	}
	
	
	/**
	 * Validate response data which doesnot contain any HTML String
	 * 
	 * @param JsonObject
	 *            data
	 * @return JsonObject
	 */
	public static JsonObject validateJSONForHTMLContent(JsonObject data) {
		String jsonString = "";
		JsonObject json = null;

		if (data instanceof JsonObject) {
			jsonString = data.toString();
			if (null != jsonString) {
				jsonString = jsonString.replaceAll(StringExpressionConstants.STR_REGEX, "");
				// replace &nbsp; with space
				jsonString = jsonString.replace(ConfigOptions.NBSP, " ");
				// replace &amp; with &
				jsonString = jsonString.replace(ConfigOptions.AMP, "&");
				if (!jsonString.equalsIgnoreCase(data.toString())) {
					log.error(" Invilid response data ");
					json = null;
				} else {
					json = new Gson().fromJson(jsonString, JsonObject.class);
				}
			}
			
		}
		return json;
	}

	public static Boolean validateStringForHTMLContent(String data) {
		String modifiedString = "";
		Boolean hasHTML = Boolean.FALSE;

		if (data instanceof String) {
			modifiedString = data;
			if (modifiedString != "") {
				modifiedString = modifiedString.replaceAll(StringExpressionConstants.STR_REGEX, "");
				// replace &nbsp; with space
				modifiedString = modifiedString.replace(ConfigOptions.NBSP, " ");
				// replace &amp; with &
				modifiedString = modifiedString.replace(ConfigOptions.AMP, "&");
			}
			if (!modifiedString.equalsIgnoreCase(data.toString())) {
				log.error(" Invilid response data ");
				log.error("Invalid html pattern found in data value validateStringForHTMLContent ==== ");
				hasHTML = Boolean.TRUE;
			}
		}
		return hasHTML;
	}

	/**
	 * Pattern Array defination for XSS
	 */
	private static Pattern[] patterns = new Pattern[] {
			// Script fragments
			Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
			Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
			Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile(ConfigOptions.CRLF_PATTERN) };

	/**
	 * Strips any potential XSS threats out of the value
	 * 
	 * @param value
	 * @return
	 * @throws InsightsCustomException
	 */
	public static String cleanXSS(String value) {
		boolean isXSSPattern = Boolean.FALSE;
		String valueWithXSSPattern = "";
		if (value != null || !("").equals(value)) {
	
			validateCleanXss(value);			
		
		} else {
			log.debug("In cleanXSS , value is empty ");
		}
		return value;
	}
	
	private static void validateCleanXss(String value) {
		
		boolean isXSSPattern = Boolean.FALSE;
		String valueWithXSSPattern = "";
		
		try {
			boolean hasHTML = validateStringForHTMLContent(value);
			if (hasHTML) {
				isXSSPattern = true;
			} else {
				// match sections that match a pattern
				for (Pattern scriptPattern : patterns) {
					Matcher m = scriptPattern.matcher(value);
					if (m.find()) {
						isXSSPattern = true;
						valueWithXSSPattern = value;
						break;
					}
				}
			}
			if (isXSSPattern) {
				log.error("Invalid pattern found in data value ******  ");
				throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
			}
		} catch (RuntimeException e) {
			log.error("Invalid pattern found in data value ==== ");
			throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
		}
		
	}
	
	/**
	 * Strips any potential XSS threats out of the value
	 * 
	 * @param value
	 * @param key
	 * @return
	 * @throws InsightsCustomException
	 */
	public static String cleanXSS(String key, String value) {
		boolean isXSSPattern = Boolean.FALSE;
		String valueWithXSSPattern = "";
		if (value != null) {
			try {
				boolean hasHTML = validateStringForHTMLContent(value);
				if (hasHTML) {
					isXSSPattern = true;
				} else {
					// match sections that match a pattern
					getValidatedString(value,isXSSPattern, valueWithXSSPattern);
				}
				if (isXSSPattern) {
					log.error("Invalid pattern found in data value for key {}  ******  {} ",key , valueWithXSSPattern);
					throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
				}
			} catch (RuntimeException e) {
				log.error("Invalid pattern found in data value for key {} ==== {} ",key , valueWithXSSPattern);
				throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
			}
		} else {
			log.debug("In cleanXSS , value is empty for key {}  ",key);
		}
		return value;
	}

	public static void getValidatedString (String value, boolean isXSSPattern,String valueWithXSSPattern) {
		for (Pattern scriptPattern : patterns) {
			Matcher m = scriptPattern.matcher(value);
			if (m.find()) {
				isXSSPattern = true;
				valueWithXSSPattern = value;
				break;
			}
		}
	}
	
	public static String cleanXSSWithHTMLCheck(String value) {
		String valueWithXSSPattern = "";
		if (value != null) {
			try {
				valueWithXSSPattern = checkValidateString(value);
			} catch (RuntimeException e) {
				log.error("Invalid pattern found in data value ==== {} ", valueWithXSSPattern);
				throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
			}
		} else {
			log.debug("In cleanXSSWithHTMLCheck , value is empty  ");
		}
		return value;
	}

	private static String checkValidateString(String value) {
		boolean isXSSPattern = Boolean.FALSE;
		String valueWithXSSPattern = "";
		boolean hasHTML = validateStringForHTMLContent(value);
		if (hasHTML) {
			isXSSPattern = true;
		} else {
			// match sections that match a pattern
			for (Pattern scriptPattern : patterns) {
				Matcher m = scriptPattern.matcher(value);
				if (m.find()) {
					isXSSPattern = true;
					valueWithXSSPattern = value;
					break;
				}
			}
		}
		
		if (isXSSPattern) {
			throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
		}
		return valueWithXSSPattern;
	}
	
	/** Method use to validate pdf files 
	 * @param value
	 * @return
	 */
	public static String cleanXSSWithoutHTMLCheck(String value) {
		boolean isXSSPattern = Boolean.FALSE;
		String valueWithXSSPattern = "";
		if (value != null) {
			try {
				// match sections that match a pattern
				for (Pattern scriptPattern : patterns) {
					Matcher m = scriptPattern.matcher(value);
					if (m.find()) {
						isXSSPattern = true;
						valueWithXSSPattern = value;
						break;
					}
				}

				if (isXSSPattern) {
					throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
				}
			} catch (RuntimeException e) {
				log.error("Invalid pattern found in data value ==== {} ", valueWithXSSPattern);
				throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST);
			}
		} else {
			log.debug("In cleanXSSWithHTMLCheck , value is empty  ");
		}
		return value;
	}

	public static String decryptAutharizationToken(String authHeaderTokenReq) {
		String authTokenDecrypt = "";
		String authHeaderToken = ValidationUtils.cleanXSS(authHeaderTokenReq);
		log.debug("In Authorization token processing ");
		try {
			if (authHeaderToken != null && !authHeaderToken.startsWith("Basic ")) {
				String auth = authHeaderToken.substring(0, authHeaderToken.length() - 15);
				String passkey = authHeaderToken.substring(authHeaderToken.length() - 15, authHeaderToken.length());
				authTokenDecrypt = AES256Cryptor.decrypt(auth, passkey);
			} else {
				log.debug(" Token starts with basic ");
				authTokenDecrypt=authHeaderToken;
			}
		} catch (Exception e) {
			log.error(" InsightsCustomException Invalid Autharization Token {} ", e.getMessage());
			throw new RuntimeException(PlatformServiceConstants.INVALID_TOKEN);
		}
		log.debug("In Authorization token processing Complated ");
		return authTokenDecrypt;
	}

	public static String getSealedObject(String userValue) {
		String encryptedData = AES256Cryptor.encrypt(userValue, "123456$#@$^@1ERF");
		return encryptedData;
	}

	public static String getDeSealedObject(String encryptedData) {
		String decryptedData = AES256Cryptor.decrypt(encryptedData, "123456$#@$^@1ERF");
		return decryptedData;
	}

	/**
	 * Validate response data which doesnot contain any HTML String
	 * 
	 * @param JsonObject
	 *            data
	 * @return JsonObject
	 */
	public static JsonObject replaceHTMLContentFormString(JsonObject data) {
		String jsonString = "";
		JsonObject json = null;

		if (data instanceof JsonObject) {
			jsonString = data.toString();
			if (jsonString != null) {
				jsonString = jsonString.replaceAll(StringExpressionConstants.STR_REGEX, "");
				// replace &nbsp; with space
				jsonString = jsonString.replace("&nbsp;", " ");
				// replace &amp; with &
				jsonString = jsonString.replace("&amp;", "&");
			}
			json = new Gson().fromJson(jsonString, JsonObject.class);
		}
		return json;
	}

	public static String validateRequestBody(String inputData) {
		log.debug(" In validateRequestBody ==== ");
		String outputData = null;
		try {
			outputData = ValidationUtils.cleanXSSWithHTMLCheck(inputData);
		} catch (RuntimeException e) {
			log.error("validate Request Body has some issue === {}", e.getMessage());
			throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST_BODY);
		}
		return outputData;
	}
	
	public static String validateResponseBody(String inputData) {
		log.debug(" In validateRequestBody ==== ");
		String outputData = null;
		try {
			outputData = ValidationUtils.cleanXSSWithHTMLCheck(inputData);
		} catch (RuntimeException e) {
			log.error("validate Response Body has some issue === {}", e.getMessage());
			throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST_BODY);
		}
		return outputData;
	}
	
	public static <T> Set<T> differenceOfSet(final Set<T> setOne, final Set<T> setTwo) {
	     Set<T> result = new HashSet<T>();
	     Set<T> one = new HashSet<>(setOne);
	     Set<T> two = new HashSet<>(setTwo);
	     one.removeAll(setTwo);
	     two.removeAll(setOne);
	     result.addAll(one);
	     result.addAll(two);
	     return result;
	}
}