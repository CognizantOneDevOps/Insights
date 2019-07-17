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
package com.cognizant.devops.platformauditing.hyperledger.client;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;

import com.cognizant.devops.platformauditing.util.LoadFile;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import com.cognizant.devops.platformauditing.hyperledger.user.BCUserContext;
import com.cognizant.devops.platformauditing.hyperledger.user.UserUtil;

public class CertificationAuthorityClient {

	String caUrl;
	Properties caProperties;

	HFCAClient instance;

	BCUserContext adminContext;
	
	private static final Logger LOG = LogManager.getLogger(CertificationAuthorityClient.class);
	JsonObject Config= LoadFile.getConfig();

	public BCUserContext getAdminUserContext() {
		return adminContext;
	}

	/*
	 Set the admin user context for registering and enrolling users
	 */
	public void setAdminUserContext(BCUserContext BCUserContext) {
		this.adminContext = BCUserContext;
	}

	public CertificationAuthorityClient(String caUrl, Properties caProperties) throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException {
		this.caUrl = caUrl;
		this.caProperties = caProperties;
		init();
	}

	public void init() throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException {
		CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
		instance = HFCAClient.createNewInstance(caUrl, caProperties);
		instance.setCryptoSuite(cryptoSuite);
	}

	public HFCAClient getInstance() {
		return instance;
	}

	/*
	 * Enroll admin user
	 */
	public BCUserContext enrollAdminUser(String username, String password) throws Exception {
		BCUserContext BCUserContext = UserUtil.readUserContext(adminContext.getAffiliation(), username);
		if (BCUserContext != null) {
			LOG.debug("CA -" + caUrl + " admin is already enrolled.");
			return BCUserContext;
		}
		Enrollment adminEnrollment = instance.enroll(username, password);
		adminContext.setEnrollment(adminEnrollment);
		LOG.debug("CA -" + caUrl + " Enrolled Admin.");
		UserUtil.writeUserContext(adminContext);
		return adminContext;
	}

	/*
	 * Register user
	 */
	public String registerUser(String username, String organization) throws Exception {
		BCUserContext BCUserContext = UserUtil.readUserContext(adminContext.getAffiliation(), username);
		if (BCUserContext != null) {
			LOG.debug("CA -" + caUrl +" User " + username+ " is already registered.");
			return null;
		}
		RegistrationRequest rr = new RegistrationRequest(username, organization);
		String enrollmentSecret = instance.register(rr, adminContext);
		LOG.debug("CA -" + caUrl + " Registered User - " + username);
		return enrollmentSecret;
	}

	/*
	 * Enroll user
	 */
	public BCUserContext enrollUser(BCUserContext user, String secret) throws Exception {
		BCUserContext BCUserContext = UserUtil.readUserContext(adminContext.getAffiliation(), user.getName());
		if (BCUserContext != null) {
			LOG.debug("CA -" + caUrl + " User " + user.getName()+" is already enrolled");
			return BCUserContext;
		}
		Enrollment enrollment = instance.enroll(user.getName(), secret);
		user.setEnrollment(enrollment);
		UserUtil.writeUserContext(user);
		LOG.debug("CA -" + caUrl +" Enrolled User - " + user.getName());
		return user;
	}


	/*
	Read the user context from the .ser file or get enrollment from the key and cert files(initialization)
	 */

	public BCUserContext getUserContext(BCUserContext user) throws Exception {
		BCUserContext BCUserContext = UserUtil.readUserContext(user.getAffiliation(), user.getName());
		if (BCUserContext != null) {
			LOG.debug("CA -" + caUrl + " User " + user.getName() +" is already enrolled");
			return BCUserContext;
		}
		LOG.debug("==============Inside getUserContext===========");
		Enrollment enrollment = UserUtil.getEnrollment(Config.get("USER_KEYSTORE_PATH").getAsString(), Config.get("USER_KEYSTORE_NAME").getAsString(), Config.get("USER_SIGNCERT_PATH").getAsString(), Config.get("USER_SIGNCERT_NAME").getAsString());
		user.setEnrollment(enrollment);
		UserUtil.writeUserContext(user);
		LOG.debug("CA -" + caUrl + " User " + user.getName()+" is read from the cert");
		return user;	
	}
	
	public BCUserContext getUserContext(String userId) throws Exception {
        BCUserContext BCUserContext = new BCUserContext();
        BCUserContext.setName(userId);
        BCUserContext.setAffiliation(Config.get("USER_ORG").getAsString());
        BCUserContext.setMspId(Config.get("USER_ORG_MSP").getAsString());
        
        return getUserContext(BCUserContext);
	}


}
