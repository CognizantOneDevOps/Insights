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
package com.cognizant.devops.platformservice.security.config;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.spi.loaderwriter.CacheWritingException;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.traceabilitydashboard.constants.TraceabilityConstants;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component("tokenProviderUtility")
@DependsOn("platformServiceInitializer")
public class TokenProviderUtility {
	private static Logger log = LogManager.getLogger(TokenProviderUtility.class);
	private String signingKey = ApplicationConfigProvider.getInstance().getSingleSignOnConfig()
			.getTokenSigningKey();
	static CacheManager cacheManager = null;
	static Cache<String, TokenDataDTO> tokenCache = null;

	public TokenProviderUtility() {
		signingKey = ApplicationConfigProvider.getInstance().getSingleSignOnConfig()
				.getTokenSigningKey();
		if (TokenProviderUtility.cacheManager == null) {
			log.debug("Inside TokenProviderUtility constructer initilizeTokenCache ");
			initilizeTokenCache();
		}
	}

	/**
	 * used to initilize cache
	 */
	@PostConstruct
	public synchronized void initilizeTokenCache() {
		log.debug("Inside initilizeTokenCache of tokenProviderUtility ==== ");
		if (TokenProviderUtility.cacheManager == null) {
			TokenProviderUtility.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
					.withCache("tokenCache",
							CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
									ResourcePoolsBuilder.newResourcePoolsBuilder().heap(30, EntryUnit.ENTRIES)
											.offheap(10, MemoryUnit.MB)))
					.build();

			TokenProviderUtility.cacheManager.init();
			if (TokenProviderUtility.tokenCache == null) {
				TokenProviderUtility.tokenCache = cacheManager.createCache("cache",
						CacheConfigurationBuilder
								.newCacheConfigurationBuilder(String.class, TokenDataDTO.class,
										ResourcePoolsBuilder.heap(TraceabilityConstants.PIPELINE_CACHE_HEAP_SIZE_BYTES))
								.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(
										Duration.ofSeconds(TraceabilityConstants.PIPELINE_CACHE_EXPIRY_IN_SEC))));
			}
		}
	}

	
	/**
	 * used to create token and add it in customize cache,This will use custom expiration time 
	 * 
	 * @param userName
	 * @return
	 */
	public String createToken(String userName,int tokenTime) {
		String strJWTToken = "";
		log.debug("Inside Create token with tokenTime {} === ",tokenTime);
		try {
			String username = ValidationUtils.cleanXSS(userName);
			String id = UUID.randomUUID().toString().replace("-", "");
			Date now = new Date();
			Date expDate = new Date(System.currentTimeMillis() + (tokenTime * 60 * 1000));

			strJWTToken = createAndStoreToken(username, id, now, expDate, new HashMap<String, Object>(0));

		} catch (CacheWritingException e) {
			log.error(e);
			log.error("CacheWritingException While writing token in cache  ==== {} ", e.getMessage());
		} catch (Exception e) {
			log.error(e);
			log.error("Error While creating JWT token ==== {} ", e.getMessage());
		}
		return strJWTToken;
	}
	
	
	/**
	 * used to create token and add it in customize cache,This will use custom expiration time 
	 * 
	 * @param userName
	 * @return
	 */
	public String createToken(String userName,int tokenTime, Map<String, Object> claimparam) {
		String strJWTToken = "";
		log.debug("Inside Create token with tokenTime {} === ",tokenTime);
		try {
			String username = ValidationUtils.cleanXSS(userName);
			String id = UUID.randomUUID().toString().replace("-", "");
			Date now = new Date();
			Date expDate = new Date(System.currentTimeMillis() + (tokenTime * 60 * 1000));

			strJWTToken = createAndStoreToken(username, id, now, expDate,claimparam);

		} catch (CacheWritingException e) {
			log.error(e);
			log.error("CacheWritingException While writing token in cache  ==== {} ", e.getMessage());
		} catch (Exception e) {
			log.error(e);
			log.error("Error While creating JWT token ==== {} ", e.getMessage());
		}
		return strJWTToken;
	}
	
	
	private String createAndStoreToken(String username, String id, Date now, Date expDate, Map<String, Object> claimparams)
			throws JOSEException {
		String strJWTToken;
		// Create HMAC signer
		JWSSigner signer = new MACSigner(signingKey.getBytes());

		// Prepare JWT with claims set
		JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
				.subject(username).jwtID(id).issueTime(now).issuer("cognizant.com").expirationTime(expDate);
		for (Entry<String, Object> entry : claimparams.entrySet()) {
			claimsSetBuilder.claim(entry.getKey(), entry.getValue());
		}
		
		JWTClaimsSet claimsSet = claimsSetBuilder.build();
		
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

		// Apply the HMAC protection
		signedJWT.sign(signer);

		// Serialize to compact form, produces something like
		strJWTToken = signedJWT.serialize();
		TokenDataDTO tokenDetail = new TokenDataDTO(strJWTToken,expDate);

		TokenProviderUtility.tokenCache.put(id, tokenDetail);
		return strJWTToken;
	}

	/**
	 * Used to verify received token without cache
	 * 
	 * @param token
	 * @return
	 * @throws AuthorizationServiceException
	 * @throws AuthenticationCredentialsNotFoundException
	 * @throws AccountExpiredException
	 * @throws InsightsCustomException
	 */
	public JWTClaimsSet verifyAndFetchCliaimsToken(String token) throws InsightsCustomException {
		boolean isVerify = Boolean.FALSE;
		JWTClaimsSet claims = null;
		log.debug(" In verifyAndFetchCliaimsToken method ==== ");
		try {
			String authToken = ValidationUtils.cleanXSS(token);
			if (authToken == null || authToken.isEmpty()) {
				throw new InsightsCustomException("Invalid authToken or empty authToken");
			}
			// parse the JWS and verify its HMAC
			SignedJWT signedJWT = SignedJWT.parse(authToken);
			JWSVerifier verifier = new MACVerifier(signingKey);
			isVerify = signedJWT.verify(verifier);
			String id = signedJWT.getJWTClaimsSet().getJWTID();
			String username = signedJWT.getJWTClaimsSet().getSubject();
			TokenDataDTO tokenValueFromCache = TokenProviderUtility.tokenCache.get(id);
			 
			SignedJWT signedCacheTokenJWT = SignedJWT.parse(tokenValueFromCache.getTokenValue());
			
			if(!isVerify) {
				log.debug("Token signuture not match ");
				throw new AuthorizationServiceException("Token signuture not match");
			} else if (tokenValueFromCache.getTokenValue() == null) {
				log.error("No token found in cache");
				throw new AuthorizationServiceException("No token found in cache");
			} else if (!tokenValueFromCache.getTokenValue().equalsIgnoreCase(authToken) && 
					!signedCacheTokenJWT.getJWTClaimsSet().getSubject().equalsIgnoreCase(username)) {
				log.error("Token Details not match, system token value not matched with received token ");
				throw new AuthorizationServiceException("Token Details not match");
			} else {
				claims = signedCacheTokenJWT.getJWTClaimsSet();

				//signedJWT.getJWTClaimsSet().getClaims().forEach((k,v) -> log.debug(" k ======== {} v ======== {} ",k,v));
				
				if(new Date().after(tokenValueFromCache.getSessionTime())) {
					log.debug(" Token session expire {} ", tokenValueFromCache.getSessionTime());
					throw new AccountExpiredException("Session Expire");
				}else {
					Date expDate = new Date(System.currentTimeMillis() + AuthenticationUtils.SESSION_TIME * 60 * 1000);
					TokenDataDTO updateobj = new TokenDataDTO(tokenValueFromCache.getTokenValue(),expDate);
					TokenProviderUtility.tokenCache.replace(id, updateobj);
					log.debug("Token verified sucessfully ==== Before {} After {} session time check ",tokenValueFromCache.getSessionTime(), TokenProviderUtility.tokenCache.get(id).getSessionTime());
				}
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Exception while validating token {} ", e.getMessage());
			throw new InsightsCustomException("Exception while varifing token ==== " + e.getMessage());
		}
		return claims;
	}
	
	/**
	 * Used to verify received token without cache
	 * 
	 * @param token
	 * @return
	 * @throws AuthorizationServiceException
	 * @throws AuthenticationCredentialsNotFoundException
	 * @throws AccountExpiredException
	 * @throws InsightsCustomException
	 */
	public JWTClaimsSet verifyExternalTokenAndFetchClaims(String token) throws InsightsCustomException {
		boolean isVerify = Boolean.FALSE;
		boolean validateTokenDate = Boolean.FALSE;
		JWTClaimsSet claims = null;
		log.debug(" In verifyExternalTokenAndFetchClaims method ==== ");
		try {
			String authToken = ValidationUtils.cleanXSS(token);
			if (authToken == null || authToken.isEmpty()) {
				log.error("External authToken is not valid or empty");
				throw new InsightsCustomException("Invalid External authToken or empty authToken");
			}

			// parse the JWS and verify its HMAC
			SignedJWT signedJWT = SignedJWT.parse(authToken);
			JWSVerifier verifier = new MACVerifier(signingKey);
			isVerify = signedJWT.verify(verifier);

			claims = signedJWT.getJWTClaimsSet();
			
			//signedJWT.getJWTClaimsSet().getClaims().forEach((k,v) -> log.debug(" k ======== {} v ======== {} ",k,v));

			validateTokenDate = new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime());

			if (!isVerify) {
				log.error("External Token signuture not match ");
				throw new AuthorizationServiceException("External Token signuture not match");
			} else if (!validateTokenDate) {
				throw new AccountExpiredException("External token validity Expire");
			} else {
				log.debug("External Token verified sucessfully ==== ");
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Exception while validating External token {} ", e.getMessage());
			throw new InsightsCustomException("Exception while varifing External token ==== " + e.getMessage());
		}
		return claims;
	}

	/**
	 * used to delete token from cache
	 * 
	 * @param csrfauthToken
	 * @return
	 * @throws Exception
	 */
	public boolean deleteToken(String csrfauthToken)  {
		Boolean isTokenRemoved = Boolean.FALSE;
		try {
			SignedJWT signedJWT = SignedJWT.parse(csrfauthToken);
			JWSVerifier verifier = new MACVerifier(signingKey);
			Boolean isVerify = signedJWT.verify(verifier);

			String id = signedJWT.getJWTClaimsSet().getJWTID();
			TokenDataDTO tokenDetail = TokenProviderUtility.tokenCache.get(id);
			if (tokenDetail.getTokenValue() != null && isVerify) {
				TokenProviderUtility.tokenCache.remove(id);
				isTokenRemoved = Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Exception while deleting token {}", e.getMessage());
		}
		return isTokenRemoved;
	}
}