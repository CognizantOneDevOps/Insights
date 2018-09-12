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
package com.cognizant.devops.platformdal.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.PostgreData;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.dashboards.CustomDashboard;
import com.cognizant.devops.platformdal.entity.definition.EntityDefinition;
import com.cognizant.devops.platformdal.grafana.user.User;
import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetails;
import com.cognizant.devops.platformdal.icon.Icon;
import com.cognizant.devops.platformdal.mapping.hierarchy.HierarchyMapping;
import com.cognizant.devops.platformdal.mapping.projects.ProjectMapping;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfiguration;
import com.cognizant.devops.platformdal.tools.layout.ToolsLayout;
import com.cognizant.devops.platformdal.user.UserPortfolio;

public class PlatformDALSessionFactoryProvider {
	private static SessionFactory sessionFactory;
	private static SessionFactory grafanaSessionFactory;
	private PlatformDALSessionFactoryProvider(){
		
	}
	static{
		initInSightsDAL();
		initGrafanaDAL();
	}
	private synchronized static void initInSightsDAL(){
		if(sessionFactory == null){
			Configuration configuration = new Configuration().configure();
			configuration.addAnnotatedClass(UserPortfolio.class);
			configuration.addAnnotatedClass(CustomDashboard.class);
			configuration.addAnnotatedClass(ProjectMapping.class);
			configuration.addAnnotatedClass(AgentConfig.class);
			configuration.addAnnotatedClass(ToolsLayout.class);
			configuration.addAnnotatedClass(EntityDefinition.class);
			configuration.addAnnotatedClass(HierarchyDetails.class);
			configuration.addAnnotatedClass(HierarchyMapping.class);
			configuration.addAnnotatedClass(Icon.class);
			configuration.addAnnotatedClass(SettingsConfiguration.class);
			PostgreData postgre = ApplicationConfigProvider.getInstance().getPostgre();
			if(postgre != null){
				configuration.setProperty("hibernate.connection.username", postgre.getUserName());
				configuration.setProperty("hibernate.connection.password", postgre.getPassword());
				configuration.setProperty("hibernate.connection.url", postgre.getInsightsDBUrl());
			}
			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
			sessionFactory = configuration.buildSessionFactory(builder.build());
		}
	}
	
	private static void initGrafanaDAL() {
		if(grafanaSessionFactory == null){
			Configuration configuration = new Configuration().configure();
			configuration.addAnnotatedClass(User.class);
			PostgreData postgre = ApplicationConfigProvider.getInstance().getPostgre();
			if(postgre != null){
				configuration.setProperty("hibernate.connection.username", postgre.getUserName());
				configuration.setProperty("hibernate.connection.password", postgre.getPassword());
				configuration.setProperty("hibernate.connection.url", postgre.getGrafanaDBUrl());
				configuration.setProperty("hbm2ddl.auto", "validate");
			}
			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
			grafanaSessionFactory = configuration.buildSessionFactory(builder.build());
		}
	}

	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}
	
	public static SessionFactory getGrafanaSessionFactory(){
		return grafanaSessionFactory;
	}
}
