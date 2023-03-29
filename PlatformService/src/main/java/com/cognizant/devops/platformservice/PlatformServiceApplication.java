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
package com.cognizant.devops.platformservice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

import com.cognizant.devops.platformservice.config.LoadServerConfig;
import com.cognizant.devops.platformservice.config.PlatformServiceInitializer;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class PlatformServiceApplication {
	
	static Logger log = LogManager.getLogger(PlatformServiceApplication.class.getName());


	public static void main(String[] args) {
		log.debug("============================ Inside Aplication Startup BEFORE MAIN ============================ ");
		
		 SpringApplication springApplication = new SpringApplication(PlatformServiceApplication.class);
	     springApplication.addListeners(new LoadServerConfig());
	     springApplication.run(args);
		
		log.debug("============================ Inside Aplication Startup AFTER MAIN  ============================ ");

	}

	

}
