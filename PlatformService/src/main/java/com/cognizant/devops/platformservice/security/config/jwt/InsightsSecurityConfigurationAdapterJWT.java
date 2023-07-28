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
package com.cognizant.devops.platformservice.security.config.jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCrossScriptingFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCustomCsrfFilter;
import com.cognizant.devops.platformservice.security.config.InsightsExternalAPIAuthenticationFilter;
import com.cognizant.devops.platformservice.security.config.InsightsResponseHeaderWriterFilter;
import com.cognizant.devops.platformservice.security.config.grafana.SpringAccessDeniedHandler;

@ComponentScan(basePackages = { "com.cognizant.devops" })
@Configuration
@EnableWebSecurity
@Order(value = 4)
@Conditional(InsightsJWTBeanInitializationCondition.class)
public class InsightsSecurityConfigurationAdapterJWT {

	private static Logger log = LogManager.getLogger(InsightsSecurityConfigurationAdapterJWT.class);

	@Autowired
	private SpringAccessDeniedHandler springAccessDeniedHandler;

	@Autowired
	private AuthenticationUtils authenticationUtils;

	DefaultSpringSecurityContextSource contextSource;

	static final String AUTHTYPE = "JWT";

	@Bean
	AuthenticationManager nativeAuthenticationManager(AuthenticationManagerBuilder auth) {
		log.debug("message Inside InsightsSecurityConfigurationAdapterJWT, AuthenticationManagerBuilder **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTHTYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			log.debug("message Inside InsightsSecurityConfigurationAdapter, check authentication provider **** ");
			ApplicationConfigProvider.performSystemCheck();
			auth.authenticationProvider(jwtAuthenticationProvider());
		}
		return auth.getObject();
	}


	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.debug("message Inside InsightsSecurityConfigurationAdapterJWT ,HttpSecurity **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTHTYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			log.debug("message Inside InsightsSecurityConfigurationAdapter,HttpSecurity check **** ");

			List<AntPathRequestMatcher> antMatchers = new ArrayList<>();
            AuthenticationUtils.CSRF_IGNORE.forEach(str ->antMatchers.add(new AntPathRequestMatcher(str)));
            http.csrf().ignoringRequestMatchers(antMatchers.toArray(new AntPathRequestMatcher[0])).csrfTokenRepository(authenticationUtils.csrfTokenRepository());
			
			http.exceptionHandling().accessDeniedHandler(springAccessDeniedHandler);

			http.addFilterAfter(insightsFilter(), BasicAuthenticationFilter.class);
			http.headers().addHeaderWriter(new StaticHeadersWriter("X-FRAME-OPTIONS", "ALLOW-FROM "
					+ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getJwtTokenOriginServerURL()));
			http.sessionManagement().maximumSessions(1).and() // sameOrigin()
					.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
			
			http.anonymous().disable().authorizeHttpRequests()
			.requestMatchers(new AntPathRequestMatcher("/datasources/**")).permitAll()
			.requestMatchers(new AntPathRequestMatcher("/datasource/**")).permitAll()
			.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAuthority("Admin")
			.requestMatchers(new AntPathRequestMatcher("/traceability/**")).hasAuthority("hasAuthority('Admin')")
			.requestMatchers(new AntPathRequestMatcher("/configure/loadConfigFromResources")).permitAll()
			.anyRequest().authenticated()
			.and().exceptionHandling().accessDeniedHandler(springAccessDeniedHandler);
			

			
		}
		return http.build();
	}

	/**
	 * used to configure WebSecurity ignore
	 */
	 @Bean
	 @Conditional(InsightsJWTBeanInitializationCondition.class)
	    public WebSecurityCustomizer webSecurityCustomizer() {
	        return (web) -> web.ignoring()
	        		.requestMatchers(new AntPathRequestMatcher("/datasource/**"));
	  }
	

	/**
	 * Used to add necessary filter for JWT Authentication
	 * 
	 * @return
	 * @throws Exception
	 */
	@Conditional(InsightsJWTBeanInitializationCondition.class)
	public FilterChainProxy insightsFilter() throws Exception {
		log.debug("message Inside FilterChainProxy, initial bean InsightsSecurityConfigurationAdapterJWT **** ");

		List<Filter> filters = new ArrayList<>();
		filters.add(0, new InsightsCustomCsrfFilter());
		filters.add(1, new InsightsCrossScriptingFilter());
		filters.add(2, insightsInitialJWTProcessingFilter());
		filters.add(3, new InsightsResponseHeaderWriterFilter());

		AuthenticationUtils.setSecurityFilterchain(
				new DefaultSecurityFilterChain(new AntPathRequestMatcher("/user/insightsso/**"), filters));// chains.add
		
		List<Filter> filtersexternal = new ArrayList<>();
		filtersexternal.add(0, new InsightsCustomCsrfFilter());
		filtersexternal.add(1, new InsightsCrossScriptingFilter());
		filtersexternal.add(2, insightsExternalProcessingFilter());
		filtersexternal.add(3, new InsightsResponseHeaderWriterFilter());
		
		AuthenticationUtils.setSecurityFilterchain(new DefaultSecurityFilterChain(
				new AntPathRequestMatcher("/externalApi/**"), insightsExternalProcessingFilter()));

		List<Filter> filtersohter = new ArrayList<>();
		filtersohter.add(0, new InsightsCustomCsrfFilter());
		filtersohter.add(1, new InsightsCrossScriptingFilter());
		filtersohter.add(2, insightsJWTProcessingFilter());
		filtersohter.add(3, new InsightsResponseHeaderWriterFilter());

		AuthenticationUtils
				.setSecurityFilterchain(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), filtersohter));// chains.add

		return new FilterChainProxy(AuthenticationUtils.getSecurityFilterchains());
	}

	/**
	 * Used to configure authentication Filter for all Request Matcher
	 * 
	 * @return
	 * @throws Exception
	 */
	public InsightsAuthenticationFilter insightsJWTProcessingFilter() {
		InsightsAuthenticationFilter filter = new InsightsAuthenticationFilter("/**");
		return filter;
	}
	
	/** This is use to validate Initial API Request 
	 * @return
	 */
	public InsightsAuthenticationFilter insightsInitialJWTProcessingFilter() throws Exception {
		InsightsAuthenticationFilter filter = new InsightsAuthenticationFilter("/user/insightsso/**");
		return filter;
	}

	/**
	 * Used to set authenticationManager Native Grafana
	 */
	protected AuthenticationManager authenticationManager() throws Exception {
		return new ProviderManager(Arrays.asList(new JWTAuthenticationProvider()));
	}

	/**
	 * used to setup JWT Authentication provider
	 * 
	 * @return
	 */
	@Conditional(InsightsJWTBeanInitializationCondition.class)
	public JWTAuthenticationProvider jwtAuthenticationProvider() {
		JWTAuthenticationProvider provider = new JWTAuthenticationProvider();
		return provider;
	}
	
	/** This bean use to validate External Request 
	 * @return
	 */
	@Conditional(InsightsJWTBeanInitializationCondition.class)
	public InsightsExternalAPIAuthenticationFilter insightsExternalProcessingFilter() {
		return new InsightsExternalAPIAuthenticationFilter();
	}
}
