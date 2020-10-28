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
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCrossScriptingFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCustomCsrfFilter;
import com.cognizant.devops.platformservice.security.config.InsightsResponseHeaderWriterFilter;

@ComponentScan(basePackages = { "com.cognizant.devops" })
@Configuration
@EnableWebSecurity
@Order(value = 1)
@Conditional(InsightsNativeBeanInitializationCondition.class)
public class InsightsSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

	private static Logger log = LogManager.getLogger(InsightsSecurityConfigurationAdapter.class);

	@Autowired
	private SpringAccessDeniedHandler springAccessDeniedHandler;

	@Autowired
	private AuthenticationUtils authenticationUtils;

	@Autowired
	private GrafanaUserDetailsService userDetailsService;

	DefaultSpringSecurityContextSource contextSource;

	private static final String AUTH_TYPE = "NativeGrafana";

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		log.debug("message Inside InsightsSecurityConfigurationAdapter, AuthenticationManagerBuilder **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			log.debug("message Inside InsightsSecurityConfigurationAdapter, check authentication provider **** ");
			ApplicationConfigProvider.performSystemCheck();
			auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.debug("message Inside InsightsSecurityConfigurationAdapter ,HttpSecurity **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			log.debug("message Inside InsightsSecurityConfigurationAdapter,HttpSecurity check **** ");

			http.cors().and().authorizeRequests().antMatchers("/datasources/**").permitAll().antMatchers("/admin/**")
					.access("hasAuthority('Admin')").antMatchers("/traceability/**").access("hasAuthority('Admin')")
					.antMatchers("/configure/loadConfigFromResources").permitAll().antMatchers("/**").authenticated() // .permitAll()
					.and().exceptionHandling().accessDeniedHandler(springAccessDeniedHandler).and().httpBasic()
					.disable()

					.csrf().ignoringAntMatchers(AuthenticationUtils.CSRF_IGNORE.toArray(new String[0]))
					.csrfTokenRepository(authenticationUtils.csrfTokenRepository()).and()
					.addFilterBefore(insightsFilter(), BasicAuthenticationFilter.class)

					.headers().frameOptions().sameOrigin().and().sessionManagement().maximumSessions(1).and()
					.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
		}
	}

	/**
	 * used to configure WebSecurity ignore
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/settings/getLogoImage");
		web.ignoring().antMatchers("/datasource/**");
	}

	/**
	 * Used to add necessary filter for Grafana Authentication
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Conditional(InsightsNativeBeanInitializationCondition.class)
	public FilterChainProxy insightsFilter() throws Exception {
		log.debug("message Inside FilterChainProxy, initial bean InsightsSecurityConfigurationAdapter **** ");

		List<Filter> filters = new ArrayList<>();
		filters.add(0, new InsightsCustomCsrfFilter());
		filters.add(1, new InsightsCrossScriptingFilter());
		filters.add(2, insightsProcessingFilter());
		filters.add(3, new InsightsResponseHeaderWriterFilter());

		AuthenticationUtils
				.setSecurityFilterchain(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), filters));//chains.add

		return new FilterChainProxy(AuthenticationUtils.getSecurityFilterchains());
	}

	/**
	 * Used to configure authentication Filter for all Request Matcher
	 * 
	 * @return
	 * @throws Exception
	 */
	public InsightsAuthenticationFilter insightsProcessingFilter() throws Exception {
		return new InsightsAuthenticationFilter("/**", authenticationManager());
	}

	/**
	 * Used to set authenticationManager Native Grafana
	 */
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return new ProviderManager(Arrays.asList(new NativeAuthenticationProvider()));
	}
}
