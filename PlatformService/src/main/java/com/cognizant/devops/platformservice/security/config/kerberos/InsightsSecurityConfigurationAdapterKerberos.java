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
package com.cognizant.devops.platformservice.security.config.kerberos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.Filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.SingleSignOnConfig;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCrossScriptingFilter;
import com.cognizant.devops.platformservice.security.config.InsightsCustomCsrfFilter;
import com.cognizant.devops.platformservice.security.config.InsightsResponseHeaderWriterFilter;
import com.cognizant.devops.platformservice.security.config.saml.ResourceLoaderService;

@ComponentScan(basePackages = { "com.cognizant.devops" })
@Configuration
@EnableWebSecurity
@Order(value = 3)
@Conditional(InsightsKerberosBeanInitializationCondition.class)
public class InsightsSecurityConfigurationAdapterKerberos extends WebSecurityConfigurerAdapter {

	private static Logger LOG = LogManager.getLogger(InsightsSecurityConfigurationAdapterKerberos.class);

	private SingleSignOnConfig singleSignOnConfig = ApplicationConfigProvider.getInstance().getSingleSignOnConfig();

	DefaultSpringSecurityContextSource contextSource;
	
	@Autowired
	ResourceLoaderService resourceLoaderService;

	final String AUTH_TYPE = "Kerberos";

	@Autowired
	private AuthenticationUtils authenticationUtils;

	/**
	 * This method is used to configure spring security using AuthenticationManagerBuilder
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		LOG.debug("message Inside InsightsSecurityConfigurationAdapterKerberos, AuthenticationManagerBuilder **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			auth.authenticationProvider(kerberosAuthenticationProvider())
					.authenticationProvider(kerberosServiceAuthenticationProvider());
		}
	}
	
	/**
	 * This method is used to configure spring security using http
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOG.debug("message Inside InsightsSecurityConfigurationAdapterKerberos,HttpSecurity **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			LOG.debug("message Inside SAMLAuthConfig, check http security **** ");

			http.cors();
			http.csrf().ignoringAntMatchers(AuthenticationUtils.CSRF_IGNORE.toArray(new String[0]))
					.csrfTokenRepository(authenticationUtils.csrfTokenRepository())
					.and().addFilterAfter(new InsightsCustomCsrfFilter(), CsrfFilter.class);

			http.exceptionHandling().authenticationEntryPoint(spnegoEntryPoint());
			http.addFilterAfter(kerberosFilter(),
					BasicAuthenticationFilter.class);
			
			http.anonymous().disable().authorizeRequests().antMatchers("/error").permitAll().antMatchers("/admin/**")
					.access("hasAuthority('Admin')")
					.anyRequest().authenticated();

			http.logout().logoutSuccessUrl("/");
		}
	}

	/**
	 * Used to add filter chain based on Request Matcher
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public FilterChainProxy kerberosFilter() throws Exception {
		LOG.debug("message Inside InsightsSecurityConfigurationAdapterKerberos FilterChainProxy, initial bean **** ");
		
		List<Filter> filters = new ArrayList<>();
		filters.add(0, new InsightsCustomCsrfFilter());
		filters.add(1, new InsightsCrossScriptingFilter());
		filters.add(2, spnegoAuthenticationProcessingFilter(authenticationManagerBean()));
		filters.add(3, new InsightsResponseHeaderWriterFilter());

		AuthenticationUtils
				.setSecurityFilterchain(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), filters));

		ListIterator<SecurityFilterChain> securityFilters = AuthenticationUtils.getSecurityFilterchains()
				.listIterator();
		while (securityFilters.hasNext()) {
			SecurityFilterChain as = securityFilters.next();
			LOG.debug("message Inside FilterChainProxy, initial bean name {} **** ",
					Arrays.toString(as.getFilters().toArray()));
		}

		return new FilterChainProxy(AuthenticationUtils.getSecurityFilterchains());//chains
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
	 * Used to configure authenticationManagerBean
	 */
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	/**
	 * Used to configure kerberos Authentication Provider
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public KerberosAuthenticationProvider kerberosAuthenticationProvider() {
		KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
		SunJaasKerberosClient client = new SunJaasKerberosClient();
		client.setDebug(true);
		provider.setKerberosClient(client);
		provider.setUserDetailsService(kerberosUserDetailsService());
		return provider;
	}

	/**
	 * Used to configure authentication Filter for all Request Matcher
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public InsightsAuthenticationFilter insightsServiceProcessingFilter() throws Exception {
		InsightsAuthenticationFilter filter = new InsightsAuthenticationFilter("/**", authenticationManager());
		return filter;
	}

	/**
	 * Entry point for kerberos validation
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public InsightsSpnegoEntryPoint spnegoEntryPoint() {
		InsightsSpnegoEntryPoint spnegoEntryPoint = new InsightsSpnegoEntryPoint("/user/insightsso/getKerberosUserDetail");//"/klogin"
		
		return spnegoEntryPoint;
	} 
	

	/**
	 * Used to handle logout senerio if unautheticated
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		LOG.debug(" Inside authenticationFailureHandler ==== ");
		return new InsightsKerberosAuthenticationFailureHandler();
	}

	/**
	 * used to set default authentication filter
	 * @param authenticationManager
	 * @return
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(
			AuthenticationManager authenticationManager) {
		SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
		filter.setAuthenticationManager(authenticationManager);
		filter.setFailureHandler(authenticationFailureHandler());
		return filter;
	}

	/**
	 * used to set kerberos Service Authentication Provider
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public InsightsKerberosAuthenticationProvider kerberosServiceAuthenticationProvider() {
		InsightsKerberosAuthenticationProvider provider = new InsightsKerberosAuthenticationProvider();
		provider.setTicketValidator(sunJaasKerberosTicketValidator());
		provider.setUserDetailsService(kerberosUserDetailsService());
		return provider;
	}

	/**
	 * Default Kerberos ticket validator
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
		Resource storeFile = resourceLoaderService
				.getResource("file:" + singleSignOnConfig.getKeyTabLocationKerberos());
		SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
		ticketValidator.setServicePrincipal(singleSignOnConfig.getServicePrincipalKerberos());
		ticketValidator.setKeyTabLocation(storeFile);
		ticketValidator.setDebug(true);
		return ticketValidator;
	}

	/**
	 * user detail service for kerberos
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsKerberosBeanInitializationCondition.class)
	public KerberosUserDetailsService kerberosUserDetailsService() {
		return new KerberosUserDetailsService();
	}
}
