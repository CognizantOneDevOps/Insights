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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AES256Cryptor {
	
	private AES256Cryptor() {
		super();
	}
	
	private static final Logger log = LogManager.getLogger(AES256Cryptor.class);

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 32;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final int KEY_SIZE = 128;
    private static final int ITERATION_COUNT = 1000;
    
    

    /** This function use to encrypt text with password 
     * It usages 32 byte Random salt value 
     * 16 Byte Random IV
     * @param plaintext  text to encrypt
     * @param password passkey use for encrypt
     * @return
     */
    public static String encrypt( String plaintext, String password) {
    	try {
    		SecureRandom random = new SecureRandom();
	    	byte[] pText = plaintext.getBytes(UTF_8);
	    	
	    	byte[] salt = new byte[SALT_LENGTH_BYTE];
	    	random.nextBytes(salt);

	    	byte[] bytesIV = new byte[IV_LENGTH_BYTE];
	        random.nextBytes(bytesIV);
	
	        // secret key from password
	        SecretKey aesKeyFromPassword =  getAESKeyFromPassword(password.toCharArray(), salt);
	
	        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
	        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(128, bytesIV));
			
			// encryption
	        byte[] cipherText = cipher.doFinal(pText);
	
	        // prefix IV and Salt to cipher text
	        byte[] cipherTextWithIvSalt = ByteBuffer.allocate(bytesIV.length + salt.length + cipherText.length)
	                .put(bytesIV)
	                .put(salt)
	                .put(cipherText)
	                .array();
	
	        return Base64.getEncoder().encodeToString(cipherTextWithIvSalt);
        
    	} catch (Exception e) {
			log.error(e);
			log.error(" Error while encrypting value {}", e.getMessage());
		}
    	return null;

    }

    /**This function use to decrypt value 
     * @param cText  Text to decrypt
     * @param password  passkey for decrypt
     * @return
     */
    public static String decrypt(String cText, String password) {
    	
    	try {

	        byte[] decode = Base64.getDecoder().decode(cText.getBytes(UTF_8));
	
	        // get back the iv and salt from the cipher text
	        ByteBuffer bb = ByteBuffer.wrap(decode);
	
	        byte[] iv = new byte[IV_LENGTH_BYTE];
	        bb.get(iv);
	
	        byte[] salt = new byte[SALT_LENGTH_BYTE];
	        bb.get(salt);
	
	        byte[] cipherText = new byte[bb.remaining()];
	        bb.get(cipherText);
	
	        // get back the aes key from the same password and salt
	        SecretKey aesKeyFromPassword =  getAESKeyFromPassword(password.toCharArray(), salt);
        
	        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
	        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword,  new GCMParameterSpec(128, iv));
	
	        byte[] plainText = cipher.doFinal(cipherText);
	
	        return new String(plainText, UTF_8);
        
    	} catch (InvalidKeyException ek) {
			log.error(ek);
			log.error("Invalid java version encountered make sure java version must be greater than 1.8_181");
		} catch (Exception e) {
			log.error(e);
			log.error(" Error while extracting value {}", e.getMessage());
		}
    	return null;
    }

    /** This function use to Generate AES key from Password with iterationCount = 65536  and keyLength = 256
     * @param password password value 
     * @param salt : input random salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

    }
    
    /** This function is used to encrypt web related value 
     * @param passphrase random UUID
     * @param plaintext
     * @return encryptedText
     */
    public static String encryptWeb( String passphrase, String plaintext) {
        try {

            String saltStr = random(16);
            String ivStr = random(16);

            SecretKey key = generateKey(saltStr, passphrase);
            byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, ivStr, plaintext.getBytes("UTF-8"));

            String encryptedStr = base64(encrypted);

            return encryptedStr + saltStr + ivStr + passphrase;
        }
        catch (UnsupportedEncodingException e) {
            throw fail(e);
        }
    }
    
    
    /** This function is used to encrypt web related value 
     * @param ciphertext
     * @return decryptedText
     */
    public static String decryptWeb(String ciphertext) {
    	String passPhrase = ciphertext.substring(ciphertext.length() - 15, ciphertext.length());
    	String iv = ciphertext.substring(ciphertext.length() - (15+32), ciphertext.length() - 15);
    	String salt = ciphertext.substring(ciphertext.length() - (15+32+32), ciphertext.length() - (15+32));
    	String cipher = ciphertext.substring(0, ciphertext.length() - (15+32+32));
        SecretKey key = generateKey(salt, passPhrase);
		byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, base64(cipher));
		return new String(decrypted, UTF_8);
    }
    
    /** This function is used to perform the cipher functions 
     * @param encryptionMode type of encryption
     * @param key secret key
     * @param iv initialization vector
     * @param bytes text in bytes
     * @return encrypted or decrypted Text
     */
    private static byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher .init(encryptMode, key, new IvParameterSpec(hex(iv)));
            return cipher.doFinal(bytes);
        }
        catch (InvalidKeyException
        		| NoSuchAlgorithmException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException
                | BadPaddingException | NoSuchPaddingException e) {
            throw fail(e);
        }
    }
    
    /** This function is used to generate secret key
     * @param salt
     * @param passphrase
     * @return secretKey
     */
    private static SecretKey generateKey(String salt, String passphrase) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), ITERATION_COUNT, KEY_SIZE);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw fail(e);
        }
    }
    
    /** This function is used to generate random bytes
     * @param length
     * @return randomBytes
     */
    public static String random(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return hex(salt);
    }
    
    /** This function is used to encode bytes to base64 string
     * @param bytes
     * @return base64 encoded string
     */
    public static String base64(byte[] bytes) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
    }
    
    /** This function is used to decode base64 string to bytes
     * @param base64 encoded string
     * @return decoded bytes
     */
    public static byte[] base64(String str) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(str);
    }
    
    /** This function is used to encode bytes to hex string
     * @param bytes
     * @return hex encoded string
     */
    public static String hex(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }
    
    /** This function is used to decode hex string to bytes
     * @param hex encoded string
     * @return decoded bytes
     */
    public static byte[] hex(String str) {
        try {
            return Hex.decodeHex(str.toCharArray());
        }
        catch (DecoderException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /** This function is used to return exception
     * @param exception
     * @return new IllegalStateException
     */
    private static IllegalStateException fail(Exception e) {
        return new IllegalStateException(e);
    }
}