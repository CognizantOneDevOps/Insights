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
package com.cognizant.devops.platformservice.security.config.saml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.servlet.Filter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLRelayStateSuccessHandler;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPArtifactBinding;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
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

@ComponentScan(basePackages = { "com.cognizant.devops" })
@Configuration
@EnableWebSecurity
@Order(value = 2)
@Conditional(InsightsSAMLBeanInitializationCondition.class)
public class InsightsSecurityConfigurationAdapterSAML extends WebSecurityConfigurerAdapter {

	private static Logger LOG = LogManager.getLogger(InsightsSecurityConfigurationAdapterSAML.class);

	private SingleSignOnConfig singleSignOnConfig = ApplicationConfigProvider.getInstance().getSingleSignOnConfig();

	@Autowired
	ResourceLoaderService resourceLoaderService;

	DefaultSpringSecurityContextSource contextSource;

	String AUTH_TYPE = "SAML";

	@Autowired
	private AuthenticationUtils authenticationUtils;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		LOG.debug("message Inside InsightsSecurityConfigurationAdapterSAML, AuthenticationManagerBuilder **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			LOG.debug("message Inside SAMLAuthConfig, check authentication provider **** ");
			auth.authenticationProvider(samlAuthenticationProvider());
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOG.debug("message Inside InsightsSecurityConfigurationAdapterSAML,HttpSecurity **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			LOG.debug("message Inside SAMLAuthConfig, check http security **** ");

			http.cors();
			http.csrf().ignoringAntMatchers(AuthenticationUtils.CSRF_IGNORE.toArray(new String[0]))
					.csrfTokenRepository(authenticationUtils.csrfTokenRepository())
					.and().addFilterAfter(new InsightsCustomCsrfFilter(), CsrfFilter.class);

			http.exceptionHandling().authenticationEntryPoint(samlEntryPoint());
			http.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class).addFilterAfter(samlFilter(),
					BasicAuthenticationFilter.class);

			http.anonymous().disable().authorizeRequests().antMatchers("/error").permitAll().antMatchers("/admin/**")
					.access("hasAuthority('Admin')").antMatchers("/saml/**").permitAll()
					// .antMatchers("/user/insightsso/**").permitAll() ///logout
					.anyRequest().authenticated();

			http.logout().logoutSuccessUrl("/");
		}
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		if (AUTH_TYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			web.ignoring().antMatchers("/settings/getLogoImage");
			web.ignoring().antMatchers("/datasource/**");
		}
	}

	/**
	 * Used to add filter in saml flow, This will call right filter based on Request
	 * Matcher pattern
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public FilterChainProxy samlFilter() throws Exception {
		LOG.debug("message Inside FilterChainProxy, initial bean **** ");

		AuthenticationUtils.setSecurityFilterchain(
				new DefaultSecurityFilterChain(new AntPathRequestMatcher("/metadata/**"), metadataDisplayFilter()));//chains.add

		AuthenticationUtils.setSecurityFilterchain(
				new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));

		AuthenticationUtils.setSecurityFilterchain(new DefaultSecurityFilterChain(
				new AntPathRequestMatcher("/saml/SSO/**"), samlWebSSOProcessingFilter()));

		AuthenticationUtils.setSecurityFilterchain(
				new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));

		AuthenticationUtils.setSecurityFilterchain(new DefaultSecurityFilterChain(
				new AntPathRequestMatcher("/saml/SingleLogout/**"), samlLogoutProcessingFilter()));

		AuthenticationUtils.setSecurityFilterchain(new DefaultSecurityFilterChain(
				new AntPathRequestMatcher("/user/insightsso/**"), insightsSSOProcessingFilter()));

		List<Filter> filters = new ArrayList<>();
		filters.add(0, new InsightsCustomCsrfFilter());
		filters.add(1, new InsightsCrossScriptingFilter());
		filters.add(2, insightsServiceProcessingFilter());
		filters.add(3, new InsightsResponseHeaderWriterFilter());

		AuthenticationUtils
				.setSecurityFilterchain(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), filters));

		return new FilterChainProxy(AuthenticationUtils.getSecurityFilterchains());//chains

	}

	/**
	 * Used to redirect after SAML authentication sucessful
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public WebSSOProfileOptions defaultWebSSOProfileOptions() {

		WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
		webSSOProfileOptions.setRelayState("/user/insightsso/authenticateSSO");
		webSSOProfileOptions.setIncludeScoping(false);
		return webSSOProfileOptions;
	}

	/**
	 * Used to initilize bean for saml entry point
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLEntryPoint samlEntryPoint() {
		SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
		samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
		return samlEntryPoint;
	}

	/**
	 * Add metadata Display filter
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public MetadataDisplayFilter metadataDisplayFilter() {
		return new MetadataDisplayFilter();
	}

	/**
	 * Used to handle logout senerio if unautheticated
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		LOG.debug(" Inside authenticationFailureHandler ==== ");
		return new InsightsSimpleUrlAuthenticationFailureHandler("/insightsso/logout");
	}

	/**
	 * used when authetication is successful to redirect to target URL success
	 * Redirect Handler
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
		SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SAMLRelayStateSuccessHandler();
		successRedirectHandler.setDefaultTargetUrl(singleSignOnConfig.getDefaultTargetUrl());
		return successRedirectHandler;
	}

	/**
	 * Used to initialize WebSSOProcessingFilter which can redirect saml/login
	 * request to /saml/SSO
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
		SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
		samlWebSSOProcessingFilter.setAuthenticationManager(super.authenticationManager());
		samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
		samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		samlWebSSOProcessingFilter.setFilterProcessesUrl("/saml/SSO");
		return samlWebSSOProcessingFilter;
	}

	/**
	 * used to initialize logout Handler
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLLogoutFilter samlLogoutFilter() {
		LOG.debug(" Inside samlLogoutFilter ==== ");
		return new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[] { logoutHandler() },
				new LogoutHandler[] { logoutHandler() });
	}

	/**
	 * used to initialize logout processing filter
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
		LOG.debug(" Inside samlLogoutProcessingFilter ==== ");
		return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
	}

	/**
	 * used to initialize successLogoutHandler and it also redirect to insights
	 * Application API when SSO logout is successful
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
		LOG.debug(" Inside successLogoutHandler ==== ");
		SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
		simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/user/insightsso/logout");
		simpleUrlLogoutSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
		return simpleUrlLogoutSuccessHandler;
	}

	/**
	 * used to initialize logout handler
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SecurityContextLogoutHandler logoutHandler() {
		LOG.debug(" Inside logoutHandler ==== ");
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.setInvalidateHttpSession(true);
		logoutHandler.setClearAuthentication(true);
		return logoutHandler;
	}

	/**
	 * used to initialize metadataGeneratorFilter
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public MetadataGeneratorFilter metadataGeneratorFilter() {
		return new MetadataGeneratorFilter(metadataGenerator());
	}

	/**
	 * used to initialize Insight sAuthentication Filter which used for all
	 * subsequent request
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public InsightsAuthenticationFilter insightsServiceProcessingFilter() throws Exception {
		InsightsAuthenticationFilter filter = new InsightsAuthenticationFilter("/**", authenticationManager());
		return filter;
	}

	/**
	 * This authentication filter used to handle all SAML login request
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public InsightsSAMLAuthenticationFilter insightsSSOProcessingFilter() throws Exception {
		InsightsSAMLAuthenticationFilter filter = new InsightsSAMLAuthenticationFilter();
		return filter;
	}

	/**
	 * used to initialize Authentication Provider for authentication Manager for all
	 * subsequent request
	 */
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return new ProviderManager(Arrays.asList(new InsightsSAMLTokenAuthenticationImpl()));
	}

	/**
	 * used to generate metadata
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public MetadataGenerator metadataGenerator() {
		MetadataGenerator metadataGenerator = new MetadataGenerator();
		metadataGenerator.setEntityId(singleSignOnConfig.getEntityId());
		metadataGenerator.setEntityBaseURL(singleSignOnConfig.getAppBaseUrl());
		metadataGenerator.setExtendedMetadata(extendedMetadata());
		metadataGenerator.setIncludeDiscoveryExtension(false);
		metadataGenerator.setKeyManager(keyManager());
		return metadataGenerator;
	}

	/**
	 * Used to load .Jks file
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public KeyManager keyManager() {
		Resource storeFile = resourceLoaderService.getResource("file:" + singleSignOnConfig.getKeyStoreFilePath());
		Map<String, String> passwords = new HashMap<>();
		passwords.put(singleSignOnConfig.getKeyAlias(), singleSignOnConfig.getKeyPass());
		return new JKSKeyManager(storeFile, singleSignOnConfig.getKeyStorePass(), passwords,
				singleSignOnConfig.getKeyAlias());
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public ExtendedMetadata extendedMetadata() {

		ExtendedMetadata extendedMetadata = new ExtendedMetadata();
		extendedMetadata.setIdpDiscoveryEnabled(false);
		extendedMetadata.setSignMetadata(false);
		return extendedMetadata;
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public VelocityEngine velocityEngine() {

		return VelocityFactory.getEngine();
	}

	@Bean(initMethod = "initialize")
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public StaticBasicParserPool parserPool() {

		return new StaticBasicParserPool();
	}

	@Bean(name = "parserPoolHolder")
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public ParserPoolHolder parserPoolHolder() {

		return new ParserPoolHolder();
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public HTTPPostBinding httpPostBinding() {
		return new HTTPPostBinding(parserPool(), velocityEngine());
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
		return new HTTPRedirectDeflateBinding(parserPool());
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLProcessorImpl processor() {

		Collection<SAMLBinding> bindings = new ArrayList<>();

		ArtifactResolutionProfileImpl artifactResolutionProfile = new ArtifactResolutionProfileImpl(httpClient());
		HTTPSOAP11Binding soapBinding = new HTTPSOAP11Binding(parserPool());
		artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding));

		bindings.add(httpRedirectDeflateBinding());
		bindings.add(httpPostBinding());
		bindings.add(new HTTPArtifactBinding(parserPool(), velocityEngine(), artifactResolutionProfile));
		bindings.add(new HTTPSOAP11Binding(parserPool()));
		bindings.add(new HTTPPAOS11Binding(parserPool()));
		return new SAMLProcessorImpl(bindings);
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public HttpClient httpClient() {
		return new HttpClient(multiThreadedHttpConnectionManager());
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
		return new MultiThreadedHttpConnectionManager();
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public static SAMLBootstrap sAMLBootstrap() {
		return new SAMLBootstrap();
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLDefaultLogger samlLogger() {

		SAMLDefaultLogger samlDefaultLogger = new SAMLDefaultLogger();
		samlDefaultLogger.setLogAllMessages(true);
		samlDefaultLogger.setLogErrors(true);
		samlDefaultLogger.setLogMessagesOnException(true);
		return samlDefaultLogger;
	}

	/**
	 * Used to create custome SAMLContextProviderLB
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLContextProviderLB contextProvider() {
		SAMLContextProviderLB samlContextProviderLB = new SAMLContextProviderLB();
		samlContextProviderLB.setScheme(AuthenticationUtils.SMAL_SCHEMA);
		samlContextProviderLB.setServerName(AuthenticationUtils.getHost(null));
		samlContextProviderLB.setContextPath(AuthenticationUtils.APPLICATION_CONTEXT_NAME);
		samlContextProviderLB.setServerPort(AuthenticationUtils.DEFAULT_PORT);
		samlContextProviderLB.setIncludeServerPortInRequestURL(Boolean.FALSE);
		LOG.debug(" samlContextProviderLB ==== {} ", samlContextProviderLB);
		return samlContextProviderLB;
	}

	/**
	 * SAML 2.0 WebSSO Assertion Consumer
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public WebSSOProfileConsumer webSSOprofileConsumer() {
		WebSSOProfileConsumerImpl webSSOProfileConsumerImpl = new WebSSOProfileConsumerImpl();
		webSSOProfileConsumerImpl.setResponseSkew(600);
		webSSOProfileConsumerImpl.setMaxAuthenticationAge(36000);
		return webSSOProfileConsumerImpl;
	}

	/**
	 * SAML 2.0 Web SSO profile
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public WebSSOProfile webSSOprofile() {
		return new WebSSOProfileImpl();
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	/**
	 * SAML 2.0 Holder-of-Key Web SSO profile,not used but autowired...
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	/**
	 * Set up logout profile for SAML
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SingleLogoutProfile logoutProfile() {
		return new SingleLogoutProfileImpl();
	}

	/**
	 * Provide IDP Metadata
	 * 
	 * @return
	 * @throws MetadataProviderException
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public ExtendedMetadataDelegate idpMetadata() throws MetadataProviderException {

		Timer backgroundTaskTimer = new Timer(true);

		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(backgroundTaskTimer, new HttpClient(),
				singleSignOnConfig.getMetadataUrl());

		httpMetadataProvider.setParserPool(parserPool());

		ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(httpMetadataProvider,
				extendedMetadata());
		extendedMetadataDelegate.setMetadataTrustCheck(true);
		extendedMetadataDelegate.setMetadataRequireSignature(true);
		return extendedMetadataDelegate;
	}

	/**
	 * used to provide Metadata Manager
	 * 
	 * @return
	 * @throws MetadataProviderException
	 */
	@Bean
	@Qualifier("metadata")
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public CachingMetadataManager metadata() throws MetadataProviderException {
		List<MetadataProvider> providers = new ArrayList<>();
		providers.add(idpMetadata());
		return new CachingMetadataManager(providers);
	}

	/**
	 * used to provide Saml Authentication Provider which is responsible to
	 * validatate authentication at first time
	 * This used for first API call to SAML
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLAuthenticationProvider samlAuthenticationProvider() {
		SAMLAuthenticationProvider samlAuthenticationProvider = new InsightsSAMLAuthenticationProviderImpl();
		samlAuthenticationProvider.setUserDetails(samlUserDetailsService());
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		samlAuthenticationProvider.setSamlLogger(samlLogger());
		return samlAuthenticationProvider;
	}

	/**
	 * Used to provide SamlUserDetails object
	 * 
	 * @return
	 */
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public SAMLUserDetailsService samlUserDetailsService() {
		return new SamlUserDetailsServiceImpl();
	}
}
