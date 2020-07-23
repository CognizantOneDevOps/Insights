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

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.cognizant.devops.platformservice.security.config.grafana.InsightsSecurityConfigurationAdapter;
import com.cognizant.devops.platformservice.security.config.jwt.InsightsSecurityConfigurationAdapterJWT;
import com.cognizant.devops.platformservice.security.config.kerberos.InsightsSecurityConfigurationAdapterKerberos;
import com.cognizant.devops.platformservice.security.config.saml.InsightsSecurityConfigurationAdapterSAML;

public class SpringMvcInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	
	private static Logger LOG = LogManager.getLogger(SpringMvcInitializer.class);

	/**
	 * used to configure RootConfigClasses which are application spring security classes
	 */
	@Override
	protected Class<?>[] getRootConfigClasses() {
		Class<?>[] returnArray = null;
		returnArray = new Class[] { InsightsSecurityConfigurationAdapter.class,
				InsightsSecurityConfigurationAdapterSAML.class, InsightsSecurityConfigurationAdapterKerberos.class,
				InsightsSecurityConfigurationAdapterJWT.class };
		LOG.debug("In SpringMvcInitializer {} ", Arrays.toString(returnArray));
		return returnArray;
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return null;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/*" };
	}

}
