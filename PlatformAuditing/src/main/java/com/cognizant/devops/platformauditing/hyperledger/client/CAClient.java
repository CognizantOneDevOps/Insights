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

import com.cognizant.devops.platformauditing.hyperledger.user.UserContext;
import com.cognizant.devops.platformauditing.hyperledger.user.UserUtil;

public class CAClient {

	String caUrl;
	Properties caProperties;

	HFCAClient instance;

	UserContext adminContext;
	
	private static final Logger LOG = LogManager.getLogger(CAClient.class);
	JsonObject Config= LoadFile.getConfig();

	public UserContext getAdminUserContext() {
		return adminContext;
	}

	/**
	 * Set the admin user context for registering and enrolling users.
	 *
	 * @param userContext
	 */
	public void setAdminUserContext(UserContext userContext) {
		this.adminContext = userContext;
	}

	/**
	 * Constructor
	 *
	 * @param caUrl
	 * @param caProperties
	 * @throws MalformedURLException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws InvalidArgumentException
	 * @throws CryptoException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public CAClient(String caUrl, Properties caProperties) throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException {
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

	/**
	 * Enroll admin user.
	 *
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public UserContext enrollAdminUser(String username, String password) throws Exception {
		UserContext userContext = UserUtil.readUserContext(adminContext.getAffiliation(), username);
		if (userContext != null) {
			LOG.debug("CA -" + caUrl + " admin is already enrolled.");
			return userContext;
		}
		Enrollment adminEnrollment = instance.enroll(username, password);
		adminContext.setEnrollment(adminEnrollment);
		LOG.debug("CA -" + caUrl + " Enrolled Admin.");
		UserUtil.writeUserContext(adminContext);
		return adminContext;
	}

	/**
	 * Register user.
	 *
	 * @param username
	 * @param organization
	 * @return
	 * @throws Exception
	 */
	public String registerUser(String username, String organization) throws Exception {
		UserContext userContext = UserUtil.readUserContext(adminContext.getAffiliation(), username);
		if (userContext != null) {
			LOG.debug("CA -" + caUrl +" User " + username+ " is already registered.");
			return null;
		}
		RegistrationRequest rr = new RegistrationRequest(username, organization);
		String enrollmentSecret = instance.register(rr, adminContext);
		LOG.debug("CA -" + caUrl + " Registered User - " + username);
		return enrollmentSecret;
	}

	/**
	 * Enroll user.
	 *
	 * @param user
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public UserContext enrollUser(UserContext user, String secret) throws Exception {
		UserContext userContext = UserUtil.readUserContext(adminContext.getAffiliation(), user.getName());
		if (userContext != null) {
			LOG.debug("CA -" + caUrl + " User " + user.getName()+" is already enrolled");
			return userContext;
		}
		Enrollment enrollment = instance.enroll(user.getName(), secret);
		user.setEnrollment(enrollment);
		UserUtil.writeUserContext(user);
		LOG.debug("CA -" + caUrl +" Enrolled User - " + user.getName());
		return user;
	}




	public UserContext getUserContext(UserContext user) throws Exception {
		UserContext userContext = UserUtil.readUserContext(user.getAffiliation(), user.getName());
		if (userContext != null) {
			LOG.debug("CA -" + caUrl + " User " + user.getName() +" is already enrolled");
			return userContext;
		}
		LOG.debug("==============Inside getUserContext===========");
		Enrollment enrollment = UserUtil.getEnrollment(Config.get("USER_KEYSTORE_PATH").getAsString(), Config.get("USER_KEYSTORE_NAME").getAsString(), Config.get("USER_SIGNCERT_PATH").getAsString(), Config.get("USER_SIGNCERT_NAME").getAsString());
		user.setEnrollment(enrollment);
		UserUtil.writeUserContext(user);
		LOG.debug("CA -" + caUrl + " User " + user.getName()+" is read from the cert");
		return user;	
	}
	
	public UserContext getUserContext(String userId) throws Exception {
        UserContext userContext = new UserContext();
        userContext.setName(userId);
        userContext.setAffiliation(Config.get("USER_ORG").getAsString());
        userContext.setMspId(Config.get("USER_ORG_MSP").getAsString());
        
        return getUserContext(userContext);
	}


}
