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
/*package com.cognizant.devops.platformengine.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.platformengine.modules.correlation.EngineCorrelatorModule;
import com.cognizant.devops.platformengine.modules.users.EngineUsersModule;

public class EngineInitializerServlet extends HttpServlet {
	
	*//**
	 * 
	 *//*
	private static final long serialVersionUID = -7134437169679897752L;

	@Override
	public void init() throws ServletException {
		super.init();
		//Load the config nodes from graph
		ApplicationConfigCache.loadConfigCache();
		//Subscribe for desired events.
		new EngineAggregatorModule().registerAggregators();
		//Schedule the Correlation Module.
		new EngineCorrelatorModule().initializeCorrelationModule();		
		//Create Default users
		new EngineUsersModule().onboardDefaultUsers();
	}
}
*/
