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
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.traceabilitydashboard.constants.TraceabilityConstants;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component("tokenProviderUtility")
@Repository
public class TokenProviderUtility {
	private static Logger log = LogManager.getLogger(TokenProviderUtility.class);
	private final String signingKey = ApplicationConfigProvider.getInstance().getSingleSignOnConfig()
			.getTokenSigningKey();
	public static CacheManager cacheManager = null;
	public static Cache<String, String> tokenCache = null;

	public TokenProviderUtility() {
		if (TokenProviderUtility.cacheManager == null) {
			log.debug("Inside TokenProviderUtility constructer initilizeTokenCache ");
			initilizeTokenCache();
		}
	}



	/**
	 * used to initilize cache
	 */
	@PostConstruct
	public void initilizeTokenCache() {
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
				TokenProviderUtility.tokenCache = cacheManager.createCache("pipeline",
						CacheConfigurationBuilder
								.newCacheConfigurationBuilder(String.class, String.class,
										ResourcePoolsBuilder.heap(TraceabilityConstants.PIPELINE_CACHE_HEAP_SIZE_BYTES))
								.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(
										Duration.ofSeconds(TraceabilityConstants.PIPELINE_CACHE_EXPIRY_IN_SEC))));
			}
		}
	}

	/**
	 * used to create token and add it in customize cache
	 * 
	 * @param ssoname
	 * @return
	 */
	public String createToken(String ssoname) {
		String strJWTToken = "";
		log.debug("Inside Create token === ");
		try {
			String username = ValidationUtils.cleanXSS(ssoname);
			String id = UUID.randomUUID().toString().replace("-", "");
			Date now = new Date();
			Date expDate = new Date(System.currentTimeMillis() + AuthenticationUtils.TOKEN_TIME * 60 * 1000);

			// Create HMAC signer
			JWSSigner signer = new MACSigner(signingKey.getBytes());

			// Prepare JWT with claims set
			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(username).jwtID(id).issueTime(now)
					.issuer("cognizant.com").expirationTime(expDate).build();

			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

			// Apply the HMAC protection
			signedJWT.sign(signer);

			// Serialize to compact form, produces something like
			strJWTToken = signedJWT.serialize();

			log.debug("token created with id {} {}", id, strJWTToken);

			TokenProviderUtility.tokenCache.put(id, strJWTToken);

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
	 * Used to verify received token with cached token
	 * 
	 * @param token
	 * @return
	 * @throws AuthorizationServiceException
	 * @throws AuthenticationCredentialsNotFoundException
	 * @throws AccountExpiredException
	 * @throws InsightsCustomException
	 */
	public boolean verifyToken(String token) throws InsightsCustomException {
		boolean isVerify = Boolean.FALSE;
		boolean isTokenExistsInCache = Boolean.FALSE;
		boolean validateTokenDate = Boolean.FALSE;
		//log.debug(" In verifyToken ");
		try {
			String authToken = ValidationUtils.cleanXSS(token);
			if (authToken == null || authToken.isEmpty()) {
				log.error("authToken is null or empty");
				throw new InsightsCustomException("authToken is null or empty");
			}

			// parse the JWS and verify its HMAC
			SignedJWT signedJWT = SignedJWT.parse(authToken);
			JWSVerifier verifier = new MACVerifier(signingKey);
			isVerify = signedJWT.verify(verifier);

			String id = signedJWT.getJWTClaimsSet().getJWTID();
			String tokenValueFromCache = null;
			if (TokenProviderUtility.tokenCache != null) {
				tokenValueFromCache = TokenProviderUtility.tokenCache.get(id);
			} else {
				log.error("cache is not initilize properly");
			}

			if (tokenValueFromCache == null) {
				log.debug("No token found in cache");
			} else if (tokenValueFromCache.equalsIgnoreCase(authToken)) {
				//log.debug("Token value matched in cache === ");
				isTokenExistsInCache = Boolean.TRUE;
			} else {
				log.error("Token value not matched in cache=== ");
			}

			//log.debug("alice  after " + signedJWT.getJWTClaimsSet().getSubject());
			//log.debug("cognizant.com  " + signedJWT.getJWTClaimsSet().getIssuer());
			//log.debug("Exceperation Time after  " + signedJWT.getJWTClaimsSet().getExpirationTime());
			log.debug("Check date of token with current date {} ",
					new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime()));//after
			validateTokenDate = new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime());//after

		} catch (Exception e) {
			log.error(e);
			log.error(" Exception while validating token {} ", e.getMessage());
			isVerify = Boolean.FALSE;
			throw new InsightsCustomException("Exception while varifing token ==== " + e.getMessage());
		}

		if (!isVerify) {
			log.debug("Token signuture not match ");
			isVerify = Boolean.FALSE;
			throw new AuthorizationServiceException("Token signuture not match");
		} else if (!isTokenExistsInCache) {
			log.error("Token Not matched ");
			isVerify = Boolean.FALSE;
			throw new AuthenticationCredentialsNotFoundException("Token not found in cache");
		} else if (!validateTokenDate) {
			isVerify = Boolean.FALSE;
			throw new AccountExpiredException("Token Expire");
		} else {
			log.debug("Token verified sucessfully ==== ");
			isVerify = Boolean.TRUE;
		}

		log.debug(" is Token Verify  ====  {} ", isVerify);

		return isVerify;
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
		boolean validateTokenDate = Boolean.FALSE;
		JWTClaimsSet claims = null;
		log.debug(" In verifyAndFetchCliaimsToken method ");
		try {
			String authToken = ValidationUtils.cleanXSS(token);
			if (authToken == null || authToken.isEmpty()) {
				log.error("authToken is null or empty");
				throw new InsightsCustomException("authToken is null or empty");
			}

			// parse the JWS and verify its HMAC
			SignedJWT signedJWT = SignedJWT.parse(authToken);
			JWSVerifier verifier = new MACVerifier(signingKey);
			isVerify = signedJWT.verify(verifier);

			claims = signedJWT.getJWTClaimsSet();

			log.debug("alice  after  username  {} ", signedJWT.getJWTClaimsSet().getSubject());
			log.debug(" domain {} ", signedJWT.getJWTClaimsSet().getIssuer()); //cognizant.com
			log.debug("Exceperation Time after  {}", signedJWT.getJWTClaimsSet().getExpirationTime());
			log.debug("Check date of token with current date {} ",
					new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime()));//after
			validateTokenDate = new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime());//after

			if (!isVerify) {
				log.debug("Token signuture not match ");
				isVerify = Boolean.FALSE;
				throw new AuthorizationServiceException("Token signuture not match");
			} else if (!validateTokenDate) {
				isVerify = Boolean.FALSE;
				throw new AccountExpiredException("Token Expire");
			} else {
				log.debug("Token verified sucessfully ==== ");
				isVerify = Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Exception while validating token {} ", e.getMessage());
			throw new InsightsCustomException("Exception while varifing token ==== " + e.getMessage());
		}

		log.debug(" is Token Verify  ====  {} ", isVerify);

		return claims;
	}

	/**
	 * used to delete token from cache
	 * 
	 * @param csrfauthToken
	 * @return
	 * @throws Exception
	 */
	public boolean deleteToken(String csrfauthToken) throws Exception {
		Boolean isTokenRemoved = Boolean.FALSE;
		try {
			SignedJWT signedJWT = SignedJWT.parse(csrfauthToken);
			JWSVerifier verifier = new MACVerifier(signingKey);
			Boolean isVerify = signedJWT.verify(verifier);

			String id = signedJWT.getJWTClaimsSet().getJWTID();
			String key = TokenProviderUtility.tokenCache.get(id);
			if (key != null && isVerify) {
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