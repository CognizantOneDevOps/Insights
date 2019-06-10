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
import java.util.Arrays;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

@ComponentScan(basePackages = {"com.cognizant.devops.platformservice"})
@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	static Logger log = LogManager.getLogger(SecurityConfiguration.class.getName());

	@Autowired
	private SpringAccessDeniedHandler springAccessDeniedHandler;

	@Autowired
	private SpringAuthenticationEntryPoint springAuthenticationEntryPoint;

	@Autowired
	private GrafanaUserDetailsService userDetailsService;

	DefaultSpringSecurityContextSource contextSource;


	private static final String[] CSRF_IGNORE = { "/login/**", "/user/authenticate/**" };

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		ApplicationConfigProvider.performSystemCheck();
		auth.userDetailsService(userDetailsService);
		return;
	}



	@Override
	protected void configure(HttpSecurity http) throws Exception {

		final AdvanceAuthenticationFilter tokenFilter = new AdvanceAuthenticationFilter();
		http
				.cors().and().authorizeRequests().antMatchers("/datasources/**").permitAll().antMatchers("/admin/**")
				.access("hasAuthority('Admin')").antMatchers("/configure/loadConfigFromResources").permitAll()
				.antMatchers("/**").authenticated() //.permitAll()
				.and().exceptionHandling().accessDeniedHandler(springAccessDeniedHandler).and().httpBasic().disable()
				.addFilterBefore(tokenFilter, BasicAuthenticationFilter.class) // .disable()  .authenticationEntryPoint(springAuthenticationEntryPoint) .and() .and()
				.headers().frameOptions().sameOrigin().and().sessionManagement().maximumSessions(1).and()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and().csrf().ignoringAntMatchers(CSRF_IGNORE)
				.csrfTokenRepository(csrfTokenRepository()).and()
				.addFilterAfter(new CustomCsrfFilter(), CsrfFilter.class)
		;
	}
	
	@Bean
	public CommonsMultipartResolver multipartResolver() throws IOException {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setMaxUploadSizePerFile(5242880);
		return resolver;
	}

	private CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName(CustomCsrfFilter.CSRF_COOKIE_NAME);
		return repository;
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

}
