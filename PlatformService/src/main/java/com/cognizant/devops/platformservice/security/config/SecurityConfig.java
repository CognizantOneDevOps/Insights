/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.security.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.LDAPConfiguration;

@ComponentScan(basePackages = {"com.cognizant.devops.platformservice"})
@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	static Logger log = LogManager.getLogger(SecurityConfig.class.getName());

	@Autowired
	private SpringAccessDeniedHandler springAccessDeniedHandler;

	@Autowired
	private SpringAuthenticationEntryPoint springAuthenticationEntryPoint;
	
	@Autowired
	private SpringHeaderWriter springHeaderWriter;
	
	@Autowired
	private SpringAuthorityProvider springAuthorityProvider;
	
	@Autowired
	private GrafanaUserDetailsService userDetailsService;

	@Bean
	public DefaultSpringSecurityContextSource getDefaultSpringSecurityContextSource() {
		if(ApplicationConfigProvider.getInstance().isDisableAuth() || ApplicationConfigProvider.getInstance().isEnableNativeUsers()){
			return null; 
		}
		LDAPConfiguration ldapConfiguration = ApplicationConfigProvider.getInstance().getLdapConfiguration();
		String ldapUrl = ldapConfiguration.getLdapUrl();
		DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(ldapUrl+"/"+ldapConfiguration.getSearchBaseDN());
		contextSource.setUserDn(ldapConfiguration.getBindDN());
		contextSource.setPassword(ldapConfiguration.getBindPassword());
		contextSource.setReferral("ignore");
		contextSource.afterPropertiesSet();
		return contextSource;
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, DefaultSpringSecurityContextSource contextSource) throws Exception {
		ApplicationConfigProvider.performSystemCheck();
		if(ApplicationConfigProvider.getInstance().isDisableAuth()){
			auth.inMemoryAuthentication().withUser("PowerUser").password("C0gnizant@1").roles("USER");
			return;
		}else if(ApplicationConfigProvider.getInstance().isEnableNativeUsers()){
			auth.userDetailsService(userDetailsService);
			return;
		}
		LDAPConfiguration ldapConfiguration = ApplicationConfigProvider.getInstance().getLdapConfiguration();
		FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch("", ldapConfiguration.getSearchFilter(), contextSource);
		userSearch.setSearchSubtree(true);
		BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
		bindAuthenticator.setUserSearch(userSearch);
		LdapAuthenticationProvider ldapAuthProvider = new LdapAuthenticationProvider(bindAuthenticator);
		ldapAuthProvider.setUserDetailsContextMapper(springAuthorityProvider);
		auth.authenticationProvider(ldapAuthProvider);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
			.antMatchers("/datasources/**").permitAll()
			.antMatchers("/admin/**").access("hasAuthority('Admin')")
			.antMatchers("/configure/loadConfigFromResources").permitAll()
			.antMatchers("/**").authenticated()
			.and().exceptionHandling().accessDeniedHandler(springAccessDeniedHandler)
			.and().httpBasic().authenticationEntryPoint(springAuthenticationEntryPoint)
			//.and().sessionManagement().maximumSessions(1).and().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			.and().sessionManagement().maximumSessions(1).and().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			.and().csrf().disable()
			.headers().addHeaderWriter(springHeaderWriter);
	}
	
	 @Bean
	    public CommonsMultipartResolver multipartResolver() throws IOException{
	        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
	         
	        //Set the maximum allowed size (in bytes) for each individual file.
	        resolver.setMaxUploadSizePerFile(5242880);//5MB
	         
	        //You may also set other available properties.
	         
	        return resolver;
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

		private List<MediaType> getSupportedMediaTypes() {
			final List<MediaType> list = new ArrayList<MediaType>();
			list.add(MediaType.IMAGE_JPEG);
			list.add(MediaType.IMAGE_PNG);
			list.add(MediaType.APPLICATION_OCTET_STREAM);

			return list;
		}
}
