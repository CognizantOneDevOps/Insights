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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@ComponentScan(basePackages = { "com.cognizant.devops.platformservice.*" })
@Configuration
public class CommonBeanConfiguration {
	private static Logger LOG = LogManager.getLogger(CommonBeanConfiguration.class);

	/**
	 * This method is used to create message source based on
	 * src/main/resources/messages
	 * 
	 * @return
	 */
	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:/messages/build/msg", "classpath:/messages/deploy/msg",
				"classpath:/messages/codequality/msg", "i18n");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setCacheSeconds(120);
		return messageSource;
	}

	/**
	 * used to read and write byte arrays based on filter
	 * 
	 * @return
	 */
	@Bean
	public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
		final ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
		arrayHttpMessageConverter.setSupportedMediaTypes(getSupportedMediaTypes());
		return arrayHttpMessageConverter;
	}

	/**
	 * byte arrays filter
	 * 
	 * @return
	 */
	private List<MediaType> getSupportedMediaTypes() {
		final List<MediaType> list = new ArrayList<MediaType>();
		list.add(MediaType.IMAGE_JPEG);
		list.add(MediaType.IMAGE_PNG);
		list.add(MediaType.APPLICATION_OCTET_STREAM);
		return list;
	}

	/**
	 * used for CORS validation, A container for CORS configuration to validate
	 * against the actual origin, HTTP methods, and headers of a given request.
	 * 
	 * @return
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		LOG.debug("Setting up corsConfigurationSource ");
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "DELETE", "PATCH"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		UrlBasedCorsConfigurationSource sourceCors = new UrlBasedCorsConfigurationSource();
		sourceCors.registerCorsConfiguration("/**", configuration);
		return sourceCors;
	}

	/**
	 * used for initialization of MultipartResolver ,MultipartResolver check files
	 * in our application it apply before initialization.
	 * It restirct any unwanted file attact in our application.
	 * 
	 * @return
	 */
	@Bean(name = "filterMultipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("utf-8");
		resolver.setMaxUploadSize(2197152);
		return resolver;
	}
}
