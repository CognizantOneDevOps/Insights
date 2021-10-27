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
package com.cognizant.devops.platformservice.security.config.grafana;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.querycaching.service.CustomExpiryPolicy;
import com.cognizant.devops.platformservice.rest.querycaching.service.EhcacheValue;

public class GrafanaExternalUserDetailsUtil {
	private static final Logger log = LogManager.getLogger(GrafanaExternalUserDetailsUtil.class);

	static Map<String, String> grafanaResponseCookies = new HashMap<>();
	public static final Long CACHE_HEAP_SIZE_BYTES = 1000l;
	public static final int CACHE_SESSION_TIME = 5;

	Cache<String, EhcacheValue<?>> authorizationCacheForExternalAPI;

	{
		CustomExpiryPolicy<String, EhcacheValue<?>> expiryPolicy = new CustomExpiryPolicy<String, EhcacheValue<?>>();

		Class<EhcacheValue<?>> myEhcacheValue = (Class<EhcacheValue<?>>) (Class<?>) EhcacheValue.class;

		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
		cacheManager.init();

		CacheConfiguration<String, EhcacheValue<?>> cacheConfiguration = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(String.class, myEhcacheValue,
						ResourcePoolsBuilder.heap(CACHE_HEAP_SIZE_BYTES))
				.withExpiry(expiryPolicy).build();

		authorizationCacheForExternalAPI = cacheManager.createCache("authorizationCacheForExternalAPI",
				cacheConfiguration);

	}

	/**
	 * used to validate grafana user detail for External API, 
	 * It check if token exist in cache then it will assume that request is authenticated
	 * If token not exist then it will login to grafana for validation 
	 * header
	 * 
	 * @param token external authentication token
	 * @return
	 */

	public void validateExternalUserDetails(String token) throws InsightsCustomException {
		ApplicationConfigProvider.performSystemCheck();
		if (authorizationCacheForExternalAPI.get(token) == null) {
			log.debug(" Inside validateExternalUserDetails function call with token, Token not found in cache !");
			checkGrafanaAuthenticationForExteranlUser(token);
			EhcacheValue<String> ehcacheValue = new EhcacheValue<>(token,
					Duration.of(CACHE_SESSION_TIME, ChronoUnit.MINUTES));
			authorizationCacheForExternalAPI.put(token, ehcacheValue);
		} else {
			log.debug(" Inside validateExternalUserDetails function call with token, Token found in cache !");
		}
	}
	
	/**
	 * used to validate grafana user detail for External API 
	 * header
	 * 
	 * @param request
	 * @return
	 */
	private boolean checkGrafanaAuthenticationForExteranlUser(String token) throws InsightsCustomException {
		String authHeader = ValidationUtils.decryptAutharizationToken(token);
		try {
			String decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]),
					StandardCharsets.UTF_8);
			String[] authTokens = decodedAuthHeader.split(":");
			log.debug("GrafanaUserDetailsUtil ====Establishing valid Grafana's session ");
			GrafanaUserDetailsUtil.getValidGrafanaSession(authTokens[0], authTokens[1]);
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(" Error while validating user " + e.getMessage());
		}
	}
}
