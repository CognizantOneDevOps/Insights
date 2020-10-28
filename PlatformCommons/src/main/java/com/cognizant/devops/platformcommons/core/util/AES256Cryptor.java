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

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AES256Cryptor {
	private static final Logger log = LogManager.getLogger(AES256Cryptor.class);
	/**
	 * Encrypt
	 * 
	 * @param plaintext
	 *            plain string
	 * @param passphrase
	 *            passphrase
	 * @return
	 */
	public static String encrypt(String plaintext, String passphrase) {
		try {
			final int keySize = 256;
			final int ivSize = 128;

			// Create empty key and iv
			byte[] key = new byte[keySize / 8];
			byte[] iv = new byte[ivSize / 8];

			// Create random salt
			byte[] saltBytes = generateSalt(8);

			// Derive key and iv from passphrase and salt
			EvpKDF(passphrase.getBytes("UTF-8"), keySize, ivSize, saltBytes, key, iv);

			// Actual encrypt
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			byte[] cipherBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

			/**
			 * Create CryptoJS-like encrypted string from encrypted data
			 * This is how CryptoJS do:
			 * 1. Create new byte array to hold ecrypted string (b)
			 * 2. Concatenate 8 bytes to b
			 * 3. Concatenate salt to b
			 * 4. Concatenate encrypted data to b
			 * 5. Encode b using Base64
			 */
			byte[] sBytes = "Salted__".getBytes("UTF-8");
			byte[] b = new byte[sBytes.length + saltBytes.length + cipherBytes.length];
			System.arraycopy(sBytes, 0, b, 0, sBytes.length);
			System.arraycopy(saltBytes, 0, b, sBytes.length, saltBytes.length);
			System.arraycopy(cipherBytes, 0, b, sBytes.length + saltBytes.length, cipherBytes.length);

			byte[] base64b = Base64.getEncoder().encode(b);

			return new String(base64b);
		} catch (Exception e) {
		}

		return null;
	}

	/**
	 * Decrypt
	 * 
	 * @param ciphertext
	 *            encrypted string
	 * @param passphrase
	 *            passphrase
	 */
	public static String decrypt(String ciphertext, String passphrase) {
		try {
			log.debug(" In function of extract token ");
			final int keySize = 256;
			final int ivSize = 128;

			// Decode from base64 text
			byte[] ctBytes = Base64.getDecoder().decode(ciphertext.getBytes("UTF-8"));

			// Get salt
			byte[] saltBytes = Arrays.copyOfRange(ctBytes, 8, 16);

			// Get ciphertext
			byte[] ciphertextBytes = Arrays.copyOfRange(ctBytes, 16, ctBytes.length);

			// Get key and iv from passphrase and salt
			byte[] key = new byte[keySize / 8];
			byte[] iv = new byte[ivSize / 8];
			EvpKDF(passphrase.getBytes("UTF-8"), keySize, ivSize, saltBytes, key, iv);

			// Actual decrypt
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			byte[] recoveredPlaintextBytes = cipher.doFinal(ciphertextBytes);

			return new String(recoveredPlaintextBytes);
		} catch (Exception e) {
			log.error(" Error while extracting token "+e.getMessage());
		}

		return null;
	}

	@SuppressWarnings("unused")
	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * @return a new pseudorandom salt of the specified length
	 */
	private static byte[] generateSalt(int length) {
		Random r = new SecureRandom();
		byte[] salt = new byte[length];
		r.nextBytes(salt);
		return salt;
	}

	private static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey,
			byte[] resultIv) throws NoSuchAlgorithmException {
		return EvpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
	}

	private static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, int iterations,
			String hashAlgorithm, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
		keySize = keySize / 32;
		ivSize = ivSize / 32;
		int targetKeySize = keySize + ivSize;
		byte[] derivedBytes = new byte[targetKeySize * 4];
		int numberOfDerivedWords = 0;
		byte[] block = null;
		MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
		while (numberOfDerivedWords < targetKeySize) {
			if (block != null) {
				hasher.update(block);
			}
			hasher.update(password);
			block = hasher.digest(salt);
			hasher.reset();

			// Iterations
			for (int i = 1; i < iterations; i++) {
				block = hasher.digest(block);
				hasher.reset();
			}

			System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4,
					Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));

			numberOfDerivedWords += block.length / 4;
		}

		System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
		System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);

		return derivedBytes; // key + iv
	}


	/**
	 * Generates a key and an initialization vector (IV) with the given salt and
	 * password.
	 * <p>
	 * This method is equivalent to OpenSSL's EVP_BytesToKey function
	 * (see https://github.com/openssl/openssl/blob/master/crypto/evp/evp_key.c).
	 * By default, OpenSSL uses a single iteration, MD5 as the algorithm and UTF-8
	 * encoded password data.
	 * </p>
	 * 
	 * @param keyLength
	 *            the length of the generated key (in bytes)
	 * @param ivLength
	 *            the length of the generated IV (in bytes)
	 * @param iterations
	 *            the number of digestion rounds
	 * @param salt
	 *            the salt data (8 bytes of data or <code>null</code>)
	 * @param password
	 *            the password data (optional)
	 * @param md
	 *            the message digest algorithm to use
	 * @return an two-element array with the generated key and IV
	 */
	public static byte[][] GenerateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password,
			MessageDigest md) {

		int digestLength = md.getDigestLength();
		int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
		byte[] generatedData = new byte[requiredLength];
		int generatedLength = 0;

		try {
			md.reset();

			// Repeat process until sufficient data has been generated
			while (generatedLength < keyLength + ivLength) {

				// Digest data (last digest if available, password data, salt if available)
				if (generatedLength > 0)
					md.update(generatedData, generatedLength - digestLength, digestLength);
				md.update(password);
				if (salt != null)
					md.update(salt, 0, 8);
				md.digest(generatedData, generatedLength, digestLength);

				// additional rounds
				for (int i = 1; i < iterations; i++) {
					md.update(generatedData, generatedLength, digestLength);
					md.digest(generatedData, generatedLength, digestLength);
				}

				generatedLength += digestLength;
			}

			// Copy key and IV into separate byte arrays
			byte[][] result = new byte[2][];
			result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
			if (ivLength > 0)
				result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);

			return result;

		} catch (DigestException e) {
			throw new RuntimeException(e);

		} finally {
			// Clean out temporary data
			Arrays.fill(generatedData, (byte) 0);
		}
	}
}
