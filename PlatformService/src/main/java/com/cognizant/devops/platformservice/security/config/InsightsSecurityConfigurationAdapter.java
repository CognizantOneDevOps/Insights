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

import java.io.IOException;
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
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.SingleSignOnConfig;

@ComponentScan(basePackages = { "com.cognizant.devops.platformservice", "com.cognizant.devops.auditservice",
		"com.cognizant.devops.platformservice.test" })
@Configuration
@EnableWebSecurity
public class InsightsSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {


	private static Logger LOG = LogManager.getLogger(InsightsSecurityConfigurationAdapter.class);

	private SingleSignOnConfig singleSignOnConfig = ApplicationConfigProvider.getInstance().getSingleSignOnConfig();

	@Autowired
	ResourceLoaderService resourceLoaderService;

	@Autowired
	private SpringAccessDeniedHandler springAccessDeniedHandler;

	@Autowired
	private GrafanaUserDetailsService userDetailsService;

	DefaultSpringSecurityContextSource contextSource;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		LOG.debug("message Inside SAMLAuthConfig, AuthenticationManagerBuilder **** "
				+ ApplicationConfigProvider.getInstance().isEnableSSO());
		if (ApplicationConfigProvider.getInstance().isEnableSSO()) {
			LOG.debug("message Inside SAMLAuthConfig, check authentication provider **** ");
			auth.authenticationProvider(samlAuthenticationProvider());
		} else if (!ApplicationConfigProvider.getInstance().isEnableSSO()) {
			LOG.debug("message Inside SecurityConfiguration, check authentication provider **** ");
			ApplicationConfigProvider.performSystemCheck();
			auth.userDetailsService(userDetailsService);
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOG.debug("message Inside SAMLAuthConfig,HttpSecurity **** "
				+ ApplicationConfigProvider.getInstance().isEnableSSO());
		if (ApplicationConfigProvider.getInstance().isEnableSSO()) {
			LOG.debug("message Inside SAMLAuthConfig, check http security **** ");

			http.cors();
			http.csrf().ignoringAntMatchers(AuthenticationUtils.CSRF_IGNORE).csrfTokenRepository(csrfTokenRepository()).and()
			.addFilterAfter(new CustomCsrfFilter(), CsrfFilter.class);

			http.exceptionHandling().authenticationEntryPoint(samlEntryPoint());
			http.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
			.addFilterAfter(samlFilter(),BasicAuthenticationFilter.class);

			http.anonymous().disable().authorizeRequests()
			.antMatchers("/error").permitAll()
			.antMatchers("/admin/**").access("hasAuthority('Admin')")
			.antMatchers("/saml/**").permitAll()
			//.antMatchers("/logout").permitAll()
			.anyRequest().authenticated();

			http.logout().logoutSuccessUrl("/");

		} else if (!ApplicationConfigProvider.getInstance().isEnableSSO()) {
			LOG.debug("message Inside SecurityConfiguration,HttpSecurity check **** ");

			http.cors().and().authorizeRequests()
			.antMatchers("/datasources/**").permitAll()
			.antMatchers("/admin/**").access("hasAuthority('Admin')")
			.antMatchers("/traceability/**").access("hasAuthority('Admin')")
			.antMatchers("/configure/loadConfigFromResources").permitAll()
			.antMatchers("/**").authenticated() //.permitAll()
			.and().exceptionHandling().accessDeniedHandler(springAccessDeniedHandler).and().httpBasic()
			.disable()

			.csrf().ignoringAntMatchers(AuthenticationUtils.CSRF_IGNORE).csrfTokenRepository(csrfTokenRepository()).and()
			.addFilterBefore(insightsFilter(), BasicAuthenticationFilter.class)

			.headers().frameOptions().sameOrigin().and().sessionManagement().maximumSessions(1).and()
			.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
		}
	}

	@Bean
	public FilterChainProxy insightsFilter() throws Exception {
		LOG.debug("message Inside FilterChainProxy, initial bean **** ");

		List<SecurityFilterChain> chains = new ArrayList<>();

		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new CustomCsrfFilter());
		filters.add(new CrossScriptingFilter());
		filters.add(new AdvanceAuthenticationFilter());
		//filters.add(new ExceptionHandlerFilter());

		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), filters));

		return new FilterChainProxy(chains);

	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public FilterChainProxy samlFilter() throws Exception {
		LOG.debug("message Inside FilterChainProxy, initial bean **** ");
			List<SecurityFilterChain> chains = new ArrayList<>();

			chains.add(
					new DefaultSecurityFilterChain(new AntPathRequestMatcher("/metadata/**"), metadataDisplayFilter()));

			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));

			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
					samlWebSSOProcessingFilter()));

			chains.add(
					new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));

			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
					samlLogoutProcessingFilter()));

			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/user/insightsso/**"),
					insightsSSOProcessingFilter()));

			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"),
					insightsServiceProcessingFilter()));

			return new FilterChainProxy(chains);

	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public WebSSOProfileOptions defaultWebSSOProfileOptions() {

		WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
		webSSOProfileOptions.setRelayState("/user/insightsso/authenticateSSO");
		webSSOProfileOptions.setIncludeScoping(false);
		//webSSOProfileOptions.setForceAuthN(true);
		return webSSOProfileOptions;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLEntryPoint samlEntryPoint() {

		SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
		samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
		return samlEntryPoint;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public MetadataDisplayFilter metadataDisplayFilter() {

		return new MetadataDisplayFilter();
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		LOG.debug(" Inside authenticationFailureHandler ==== ");
		return new InsightsSimpleUrlAuthenticationFailureHandler("/insightsso/logout");
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {

		SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SAMLRelayStateSuccessHandler();
		successRedirectHandler.setDefaultTargetUrl(singleSignOnConfig.getDefaultTargetUrl());
		return successRedirectHandler;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
			SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
			samlWebSSOProcessingFilter.setAuthenticationManager(super.authenticationManager());
			samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
			samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
			samlWebSSOProcessingFilter.setFilterProcessesUrl("/saml/SSO");
			return samlWebSSOProcessingFilter;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLLogoutFilter samlLogoutFilter() {
		LOG.debug(" Inside samlLogoutFilter ==== ");
		return new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[] { logoutHandler() },
				new LogoutHandler[] { logoutHandler() });
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
		LOG.debug(" Inside samlLogoutProcessingFilter ==== ");
		return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
		LOG.debug(" Inside successLogoutHandler ==== ");
		SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
		simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/user/insightsso/logout");
		simpleUrlLogoutSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
		//simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategy); //		RedirectStrategy
		return simpleUrlLogoutSuccessHandler;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SecurityContextLogoutHandler logoutHandler() {
		LOG.debug(" Inside logoutHandler ==== ");
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.setInvalidateHttpSession(true);
		logoutHandler.setClearAuthentication(true);
		return logoutHandler;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public MetadataGeneratorFilter metadataGeneratorFilter() {

		return new MetadataGeneratorFilter(metadataGenerator());
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public InsightsAuthenticationFilter insightsServiceProcessingFilter() throws Exception {

		InsightsAuthenticationFilter filter = new InsightsAuthenticationFilter("/**", authenticationManager());
		return filter;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public AdvanceAuthenticationFilter insightsSSOProcessingFilter() throws Exception {

		AdvanceAuthenticationFilter filter = new AdvanceAuthenticationFilter();
		return filter;
	}

	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return new ProviderManager(Arrays.asList(new InsightsAuthenticationProviderImpl()));
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public MetadataGenerator metadataGenerator() {
		if (ApplicationConfigProvider.getInstance().isEnableSSO()) {
			MetadataGenerator metadataGenerator = new MetadataGenerator();
			metadataGenerator.setEntityId(singleSignOnConfig.getEntityId());
			metadataGenerator.setEntityBaseURL(singleSignOnConfig.getAppBaseUrl());
			metadataGenerator.setExtendedMetadata(extendedMetadata());
			metadataGenerator.setIncludeDiscoveryExtension(false);
			metadataGenerator.setKeyManager(keyManager());
			return metadataGenerator;
		} else {
			return null;
		}
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public KeyManager keyManager() {
		Resource storeFile = resourceLoaderService.getResource("file:" + singleSignOnConfig.getKeyStoreFilePath());
		Map<String, String> passwords = new HashMap<>();
		passwords.put(singleSignOnConfig.getKeyAlias(), singleSignOnConfig.getKeyPass());
		return new JKSKeyManager(storeFile, singleSignOnConfig.getKeyStorePass(), passwords,
				singleSignOnConfig.getKeyAlias());
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public ExtendedMetadata extendedMetadata() {

		ExtendedMetadata extendedMetadata = new ExtendedMetadata();
		extendedMetadata.setIdpDiscoveryEnabled(false);
		extendedMetadata.setSignMetadata(false);
		return extendedMetadata;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public VelocityEngine velocityEngine() {

		return VelocityFactory.getEngine();
	}

	@Bean(initMethod = "initialize")
	@Conditional(InsightsBeanInitializationCondition.class)
	public StaticBasicParserPool parserPool() {

		return new StaticBasicParserPool();
	}

	@Bean(name = "parserPoolHolder")
	@Conditional(InsightsBeanInitializationCondition.class)
	public ParserPoolHolder parserPoolHolder() {

		return new ParserPoolHolder();
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public HTTPPostBinding httpPostBinding() {

		return new HTTPPostBinding(parserPool(), velocityEngine());
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {

		return new HTTPRedirectDeflateBinding(parserPool());
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLProcessorImpl processor() throws IOException {

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
	@Conditional(InsightsBeanInitializationCondition.class)
	public HttpClient httpClient() throws IOException {

		return new HttpClient(multiThreadedHttpConnectionManager());
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {

		return new MultiThreadedHttpConnectionManager();
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public static SAMLBootstrap sAMLBootstrap() {

		return new SAMLBootstrap();
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLDefaultLogger samlLogger() {

		SAMLDefaultLogger samlDefaultLogger = new SAMLDefaultLogger();
		samlDefaultLogger.setLogAllMessages(true);
		samlDefaultLogger.setLogErrors(true);
		samlDefaultLogger.setLogMessagesOnException(true);
		return samlDefaultLogger;
	}
	
	/*
	@Bean
	@Conditional(SSOBeanCondition.class)
	public SAMLContextProviderImpl contextProvider() {
		return new SAMLContextProviderImpl();
	}
	*/

	 @Bean
	 @Conditional(InsightsBeanInitializationCondition.class)
	  public SAMLContextProviderLB contextProvider() {
	    SAMLContextProviderLB samlContextProviderLB = new SAMLContextProviderLB();
	    samlContextProviderLB.setScheme(AuthenticationUtils.SMAL_SCHEMA);
	    samlContextProviderLB.setServerName(AuthenticationUtils.getHost(null));
	    samlContextProviderLB.setContextPath(AuthenticationUtils.APPLICATION_CONTEXT_NAME);
	    samlContextProviderLB.setServerPort(AuthenticationUtils.DEFAULT_PORT);
	    samlContextProviderLB.setIncludeServerPortInRequestURL(Boolean.FALSE);
	    LOG.debug(" samlContextProviderLB ==== "+samlContextProviderLB.toString());
	    return samlContextProviderLB;
	  }

	// SAML 2.0 WebSSO Assertion Consumer
	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public WebSSOProfileConsumer webSSOprofileConsumer() {
		WebSSOProfileConsumerImpl webSSOProfileConsumerImpl = new WebSSOProfileConsumerImpl();
		webSSOProfileConsumerImpl.setResponseSkew(600);
		webSSOProfileConsumerImpl.setMaxAuthenticationAge(36000);
		return webSSOProfileConsumerImpl;
	}

	// SAML 2.0 Web SSO profile
	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public WebSSOProfile webSSOprofile() {
		return new WebSSOProfileImpl();
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	// not used but autowired...
	// SAML 2.0 Holder-of-Key Web SSO profile
	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SingleLogoutProfile logoutProfile() {
		return new SingleLogoutProfileImpl();
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public ExtendedMetadataDelegate idpMetadata() throws MetadataProviderException, ResourceException {

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

	@Bean
	@Qualifier("metadata")
	@Conditional(InsightsBeanInitializationCondition.class)
	public CachingMetadataManager metadata() throws MetadataProviderException, ResourceException {
		List<MetadataProvider> providers = new ArrayList<>();
		providers.add(idpMetadata());
		return new CachingMetadataManager(providers);
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLAuthenticationProvider samlAuthenticationProvider() {
		SAMLAuthenticationProvider samlAuthenticationProvider = new InsightsSAMLAuthenticationProviderImpl();
		//SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
		samlAuthenticationProvider.setUserDetails(samlUserDetailsService());
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		samlAuthenticationProvider.setSamlLogger(samlLogger());
		return samlAuthenticationProvider;
	}

	@Bean
	@Conditional(InsightsBeanInitializationCondition.class)
	public SAMLUserDetailsService samlUserDetailsService() {
		return new SamlUserDetailsServiceImpl();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/settings/getLogoImage");
		web.ignoring().antMatchers("/datasource/**");
	}

	@Bean
	public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
		final ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
		arrayHttpMessageConverter.setSupportedMediaTypes(getSupportedMediaTypes());
		return arrayHttpMessageConverter;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "DELETE", "PATCH"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		UrlBasedCorsConfigurationSource sourceCors = new UrlBasedCorsConfigurationSource();
		sourceCors.registerCorsConfiguration("/**", configuration);
		return sourceCors;
	}

	private List<MediaType> getSupportedMediaTypes() {
		final List<MediaType> list = new ArrayList<MediaType>();
		list.add(MediaType.IMAGE_JPEG);
		list.add(MediaType.IMAGE_PNG);
		list.add(MediaType.APPLICATION_OCTET_STREAM);
		return list;
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() throws IOException {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setMaxUploadSizePerFile(5242880);
		return resolver;
	}

	private CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName(AuthenticationUtils.CSRF_COOKIE_NAME);
		return repository;
	}

}
