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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration.Builder;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
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
import com.cognizant.devops.platformservice.security.config.grafana.SpringAccessDeniedHandler;

@ComponentScan(basePackages = { "com.cognizant.devops" })
@Configuration
@EnableWebSecurity
@Order(value = 2)
@Conditional(InsightsSAMLBeanInitializationCondition.class)
public class InsightsSecurityConfigurationAdapterSAML {

	private static Logger log = LogManager.getLogger(InsightsSecurityConfigurationAdapterSAML.class);
	static final String AUTHTYPE = "SAML";

	static final String REGISTRATION_ID = ApplicationConfigProvider.getInstance().getSingleSignOnConfig()
			.getRegistrationId();

	@Autowired
	ResourceLoaderService resourceLoaderService;
	
	@Autowired
	private SpringAccessDeniedHandler springAccessDeniedHandler;
	

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.debug("message Inside InsightsSecurityConfigurationAdapterSAML,HttpSecurity **** {} ",
				ApplicationConfigProvider.getInstance().getAutheticationProtocol());
		
		if (AUTHTYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			
			AuthenticationUtils authenticationUtils = new AuthenticationUtils();
			log.debug("Inside SAMLAuthConfig, check http security **** ");
			http.cors();
			
			List<AntPathRequestMatcher> antMatchers = new ArrayList<>();
            AuthenticationUtils.CSRF_IGNORE.forEach(str ->antMatchers.add(new AntPathRequestMatcher(str)));
            http.csrf().ignoringRequestMatchers(antMatchers.toArray(new AntPathRequestMatcher[0])).csrfTokenRepository(authenticationUtils.csrfTokenRepository());
			
			http.headers().xssProtection().and().contentSecurityPolicy("script-src 'self'");
			
			http.saml2Login(saml2 -> saml2.defaultSuccessUrl("/user/insightsso/authenticateSSO"));
			http.saml2Logout(Customizer.withDefaults());
			http.addFilterAfter(samlFilter(), BasicAuthenticationFilter.class);

			http.anonymous().disable().authorizeHttpRequests()
			.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAuthority("Admin")
			.requestMatchers(new AntPathRequestMatcher("/traceability/**")).hasAuthority("hasAuthority('Admin')") 
			.requestMatchers(new AntPathRequestMatcher("/configure/loadConfigFromResources")).permitAll()
			.anyRequest().authenticated().and().exceptionHandling().accessDeniedHandler(springAccessDeniedHandler);
		}
		return http.build();
	}
	
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
			RelyingPartyRegistrationRepository registrations) {
		return new DefaultRelyingPartyRegistrationResolver(id -> registrations.findByRegistrationId(REGISTRATION_ID));
	}
	
	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	Saml2AuthenticationTokenConverter authentication(RelyingPartyRegistrationResolver registrations) {
		return new Saml2AuthenticationTokenConverter(registrations);
	}

	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	FilterRegistrationBean<Saml2MetadataFilter> metadata(RelyingPartyRegistrationResolver registrations) {
		Saml2MetadataFilter metadata = new Saml2MetadataFilter(registrations, new OpenSamlMetadataResolver());
		FilterRegistrationBean<Saml2MetadataFilter> filter = new FilterRegistrationBean<>(metadata);
		filter.setOrder(-101);
		return filter;
	}



	@Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public RelyingPartyRegistrationRepository relyingPartyRegistrations() {
		try {
			
			RSAPrivateKey privateKey = readPrivateKey(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getPrivatekeyLocation());
			
			Saml2X509Credential signing = Saml2X509Credential.signing(privateKey, relyingPartyCertificate());

			Builder registrationBuilder = null;
			if( ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getMetadataUrl().isEmpty()) {
				File metaData = new File(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getMetdataFilePath());
			    InputStream metaDataStream = new FileInputStream(metaData);				
			    registrationBuilder = RelyingPartyRegistrations
						.fromMetadata(metaDataStream);				
			} else {
				registrationBuilder = RelyingPartyRegistrations
						.fromMetadataLocation(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getMetadataUrl());						
			}
			registrationBuilder.registrationId(REGISTRATION_ID)
			.entityId(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getEntityId())
			.assertionConsumerServiceLocation(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getAppBaseUrl()+"/login/saml2/sso/"+REGISTRATION_ID)
			.signingX509Credentials(c -> c.add(signing))			
			.assertingPartyDetails(party -> party
					.entityId(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getEntityId())
					.singleSignOnServiceLocation(ApplicationConfigProvider.getInstance().getSingleSignOnConfig()
							.getSingleSignOnServiceLocation())
					);
			
			RelyingPartyRegistration registration = registrationBuilder.build();
		 	
			return new InMemoryRelyingPartyRegistrationRepository(registration);

		} catch (Exception e) {
			log.error(e);
		}

		return null;
	}
	
	X509Certificate relyingPartyCertificate() {
		File cerFile = new File(
				ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getKeyStoreFilePath());
		try (InputStream is = new FileInputStream(cerFile)) {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
		}
		catch (Exception ex) {
			throw new UnsupportedOperationException(ex);
		}
	}
	
	private  RSAPrivateKey readPrivateKey(String privateKeyLocation) {
		File cerFile = new File(privateKeyLocation);
		try (InputStream inputStream = new FileInputStream(cerFile)) {
			return RsaKeyConverters.pkcs8().convert(inputStream);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * used to configure WebSecurity ignore
	 */
	 @Bean
	    public WebSecurityCustomizer webSecurityCustomizer() {
	        return web -> web.ignoring()
	        		.requestMatchers(new AntPathRequestMatcher("/datasource/**"));
	  }
	
	
	/**
	 * Used to add filter in saml flow, This will call right filter based on Request
	 * Matcher pattern
	 * 
	 * @return
	 * @throws Exception
	 */
	// @Bean
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public FilterChainProxy samlFilter() throws Exception {
		log.debug("message Inside FilterChainProxy, initial bean **** ");

		List<Filter> filtersExteranl = new ArrayList<>();
		filtersExteranl.add(0, new InsightsCustomCsrfFilter());
		filtersExteranl.add(1, new InsightsCrossScriptingFilter());
		filtersExteranl.add(2, insightsExternalProcessingFilter());
		filtersExteranl.add(3, new InsightsResponseHeaderWriterFilter());
		
		AuthenticationUtils.setSecurityFilterchain(new DefaultSecurityFilterChain(
				new AntPathRequestMatcher("/externalApi/**"), filtersExteranl));
			
		List<Filter> filters = new ArrayList<>();
		filters.add(0, new InsightsCustomCsrfFilter());
		filters.add(1, new InsightsCrossScriptingFilter());
		filters.add(2, insightsServiceProcessingFilter());
		filters.add(3, new InsightsResponseHeaderWriterFilter());

		AuthenticationUtils
				.setSecurityFilterchain(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), filters));

		return new FilterChainProxy(AuthenticationUtils.getSecurityFilterchains());// chains

	}
	
	
	/**
	 * used to initialize Insight sAuthentication Filter which used for all
	 * subsequent request
	 * 
	 * @return
	 * @throws Exception
	 */
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public InsightsAuthenticationFilter insightsServiceProcessingFilter() throws Exception {
		return  new InsightsAuthenticationFilter("/**", authenticationManager());
	}
	
	
	/**
	 * used to initialize Authentication Provider for authentication Manager for all
	 * subsequent request
	 */
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {
		return new ProviderManager(Arrays.asList(new InsightsSAMLTokenAuthenticationImpl()));
	}

	/** This bean use to validate External Request 
	 * @return
	 */
	@Conditional(InsightsSAMLBeanInitializationCondition.class)
	public InsightsExternalAPIAuthenticationFilter insightsExternalProcessingFilter() {
		return new InsightsExternalAPIAuthenticationFilter();
	}

}
