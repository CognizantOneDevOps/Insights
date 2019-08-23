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
package com.cognizant.devops.platformauditing.hyperledger.user;

import com.cognizant.devops.platformauditing.util.LoadFile;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class UserUtil {
	private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(UserUtil.class.getName());
	private static JsonObject Config= LoadFile.getConfig();

	/*
	Write the user context into the path specified as USER_SER_PATH as a .ser file
	 */
	public static void writeUserContext(BCUserContext BCUserContext) throws Exception {
		String directoryPath = Config.get("USER_SER_PATH").getAsString() + BCUserContext.getAffiliation();
		String filePath = directoryPath + "/" + BCUserContext.getName() + ".ser";
		File directory = new File(directoryPath);
		if (!directory.exists())
			directory.mkdirs();

		FileOutputStream file = new FileOutputStream(filePath);
		ObjectOutputStream out = new ObjectOutputStream(file);

		// Method for serialization of object
		out.writeObject(BCUserContext);

		out.close();
		file.close();
	}

	/*
	Read the user context stored as .ser file from the USER_SER_PATH
	 */
	public static BCUserContext readUserContext(String affiliation, String username) throws Exception {
		String filePath = Config.get("USER_SER_PATH") + affiliation + "/" + username + ".ser";
		File file = new File(filePath);
		if (file.exists()) {
			// Reading the object from a file
			FileInputStream fileStream = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileStream);

			// Method for deserialization of object
			BCUserContext uContext = (BCUserContext) in.readObject();

			in.close();
			fileStream.close();
			return uContext;
		}

		return null;
	}

	/*
	 Create enrollment from key and certificate files
	 */
	public static CertificationAuthorityEnrollment getEnrollment(String keyFolderPath, String keyFileName, String certFolderPath, String certFileName)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		PrivateKey key;
		String certificate;
		InputStream isKey = null;
		BufferedReader brKey = null;

		try {

			isKey = new FileInputStream(keyFolderPath + File.separator + keyFileName);
			brKey = new BufferedReader(new InputStreamReader(isKey));
			StringBuilder keyBuilder = new StringBuilder();

			for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
				if (line.indexOf("PRIVATE") == -1) {
					keyBuilder.append(line);
				}
			}

			certificate = new String(Files.readAllBytes(Paths.get(certFolderPath, certFileName)));

			byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("EC");
			key = kf.generatePrivate(keySpec);
		} finally {
			if (isKey != null) {
				isKey.close();
			}
			if (brKey != null) {
				brKey.close();
			}
		}

		return new CertificationAuthorityEnrollment(key, certificate);
	}
	/*
	Clean up the USER_SER_PATH
	 */
	public static void cleanUp() {
		String directoryPath = Config.get("USER_SER_PATH").getAsString();
		File directory = new File(directoryPath);
		deleteDirectory(directory);
	}
	
	  public static boolean deleteDirectory(File dir) {
	        if (dir.isDirectory()) {
	            File[] children = dir.listFiles();
	            for (int i = 0; i < children.length; i++) {
	                boolean success = deleteDirectory(children[i]);
	                if (!success) {
	                    return false;
	                }
	            }
	        }

	        // either file or an empty directory
		  LOG.info( "Deleting - " + dir.getName());
	        return dir.delete();
	    }

}
