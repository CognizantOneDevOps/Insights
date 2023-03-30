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
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCrossScriptingFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCustomCsrfFilter;
import com.cognizant.devops.platformservice.security.config.InsightsExternalAPIAuthenticationFilter;
import com.cognizant.devops.platformservice.security.config.InsightsResponseHeaderWriterFilter;

@ComponentScan(basePackages = { "com.cognizant.devops" })
@Configuration
@EnableWebSecurity
@Order(value = 1)
@Conditional(InsightsNativeBeanInitializationCondition.class)
public class InsightsSecurityConfigurationAdapter  {

	private static Logger log = LogManager.getLogger(InsightsSecurityConfigurationAdapter.class);

	@Autowired
	private SpringAccessDeniedHandler springAccessDeniedHandler;

	@Autowired
	private AuthenticationUtils authenticationUtils;

	DefaultSpringSecurityContextSource contextSource;

	private static final String AUTH_TYPE = "NativeGrafana";
	
	@Bean
	AuthenticationManager nativeAuthenticationManager(AuthenticationManagerBuilder auth) {
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			log.debug("message Inside InsightsSecurityConfigurationAdapter, check authentication provider **** ");
			ApplicationConfigProvider.performSystemCheck();
			auth.authenticationProvider(new NativeInitialAuthenticationProvider());
		}
		return auth.getObject();
	}


	@Bean
	 SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.debug("message Inside InsightsSecurityConfigurationAdapter ,HttpSecurity **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
	
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			log.debug("message Inside InsightsSecurityConfigurationAdapter,HttpSecurity check **** ");
			
			http.cors();
			http.csrf().ignoringRequestMatchers(AuthenticationUtils.CSRF_IGNORE.toArray(new String[0]))
					.csrfTokenRepository(authenticationUtils.csrfTokenRepository());
					
			http.exceptionHandling().accessDeniedHandler(springAccessDeniedHandler); 
			http.headers().xssProtection().and().contentSecurityPolicy("script-src 'self'");
			
			http
			.addFilterAfter(insightsFilter(), BasicAuthenticationFilter.class)
			.addFilterAfter(new InsightsResponseHeaderWriterFilter(), BasicAuthenticationFilter.class);
			
			http.headers().frameOptions().sameOrigin();
			
			http.anonymous().disable().authorizeHttpRequests()
			.requestMatchers("/datasources/**").permitAll()
			.requestMatchers("/datasource/**").permitAll()
			.requestMatchers("/admin/**").hasAuthority("Admin")
			.requestMatchers("/traceability/**").hasAuthority("hasAuthority('Admin')")
			.requestMatchers("/configure/loadConfigFromResources").permitAll()
			.anyRequest().authenticated();
			
			http.logout().logoutSuccessUrl("/");
		}
		return http.build();
	}

	/**
	 * used to configure WebSecurity ignore
	 */
	 @Bean
	    public WebSecurityCustomizer webSecurityCustomizer() {
	        return (web) -> web.ignoring()
	        		.requestMatchers("/datasource/**");
	  }
	
	/**
	 * Used to add necessary filter for Grafana Authentication
	 * 
	 * @return
	 * @throws Exception
	 */
	
	@Conditional(InsightsNativeBeanInitializationCondition.class)
	public FilterChainProxy insightsFilter() throws Exception {
		log.debug("message Inside FilterChainProxy, initial bean InsightsSecurityConfigurationAdapter **** ");
		
		List<Filter> filterlogin = new ArrayList<>();
		filterlogin.add(0, new InsightsCustomCsrfFilter());
		filterlogin.add(1, new InsightsCrossScriptingFilter());
		filterlogin.add(2, insightsInitialProcessingFilter());
		filterlogin.add(3, new InsightsResponseHeaderWriterFilter());
		
		List<SecurityFilterChain> securityFilterchains = new ArrayList<>();
		securityFilterchains.add(
				new DefaultSecurityFilterChain(new AntPathRequestMatcher("/user/authenticate/**"), filterlogin));
		
		List<Filter> filtersExteranl = new ArrayList<>();
		filtersExteranl.add(0, new InsightsCustomCsrfFilter());
		filtersExteranl.add(1, new InsightsCrossScriptingFilter());
		filtersExteranl.add(2, insightsExternalProcessingFilter());
		filtersExteranl.add(3, new InsightsResponseHeaderWriterFilter());
		
		securityFilterchains.add(
				new DefaultSecurityFilterChain(new AntPathRequestMatcher("/externalApi/**"), filtersExteranl));
		
		List<Filter> filtersohter = new ArrayList<>();
		filtersohter.add(0, new InsightsCustomCsrfFilter());
		filtersohter.add(1, new InsightsCrossScriptingFilter());
		filtersohter.add(2, insightsProcessingFilter());
		filtersohter.add(3, new InsightsResponseHeaderWriterFilter());
		
		securityFilterchains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), filtersohter));

		return new FilterChainProxy(securityFilterchains);
	}
	
	/**
	 * Used to configure authentication Filter for all Request Matcher
	 * 
	 * @return
	 * @throws Exception
	 */
	public InsightsAuthenticationFilter insightsProcessingFilter() {
		return new InsightsAuthenticationFilter("/**");
	}
	
	/** This bean use for initial level validation
	 * @return
	 */
//	@Bean
	@Conditional(InsightsNativeBeanInitializationCondition.class)
	public InsightsGrafanaAuthenticationFilter insightsInitialProcessingFilter() {
		InsightsGrafanaAuthenticationFilter initialAuthProcessingFilter = new InsightsGrafanaAuthenticationFilter("/user/authenticate/**");
		initialAuthProcessingFilter.setAuthenticationManager(authenticationInitialManager());
		return initialAuthProcessingFilter;
	}
	

	@Conditional(InsightsNativeBeanInitializationCondition.class)
	public InsightsExternalAPIAuthenticationFilter insightsExternalProcessingFilter() {
		return new InsightsExternalAPIAuthenticationFilter();
	}
	
	/** This bean use to validate all subsequent request 
	 * @return
	 */
	protected AuthenticationManager authenticationInitialManager()  {
		return new ProviderManager(Arrays.asList(new NativeInitialAuthenticationProvider()));
	}
}
