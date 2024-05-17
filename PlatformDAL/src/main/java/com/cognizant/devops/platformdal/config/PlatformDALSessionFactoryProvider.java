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

import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.PostgreData;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportTemplateConfigFiles;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsVisualizationConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformdal.groupemail.InsightsGroupEmailConfiguration;
import com.cognizant.devops.platformdal.healthutil.InsightsAgentHealthDetails;
import com.cognizant.devops.platformdal.healthutil.InsightsComponentHealthDetails;
import com.cognizant.devops.platformdal.icon.Icon;
import com.cognizant.devops.platformdal.milestone.InsightsMileStoneOutcomeConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfig;
import com.cognizant.devops.platformdal.neo4jScaling.InsightsReplicaConfig;
import com.cognizant.devops.platformdal.neo4jScaling.InsightsStreamsSourceConfig;
import com.cognizant.devops.platformdal.offlineAlerting.InsightsOfflineAlerting;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.cognizant.devops.platformdal.outcome.InsightsOutcomeTools;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.relationshipconfig.RelationshipConfiguration;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDefinition;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskStatus;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;

public class PlatformDALSessionFactoryProvider {
	private static SessionFactory sessionFactory;
	private static SessionFactory grafanaSessionFactory;
	private static Logger log = LogManager.getLogger(PlatformDALSessionFactoryProvider.class);
	
	private PlatformDALSessionFactoryProvider(){
	
	}
	static{
		initInSightsDAL();
		initGrafanaDAL();
	}
	private synchronized static void initInSightsDAL(){
		if(sessionFactory == null){
			Configuration configuration = new Configuration();
			configuration.addAnnotatedClass(AgentConfig.class);
			configuration.addAnnotatedClass(Icon.class);
			configuration.addAnnotatedClass(WebHookConfig.class);
			configuration.addAnnotatedClass(CorrelationConfiguration.class);
			configuration.addAnnotatedClass(RelationshipConfiguration.class);
			configuration.addAnnotatedClass(WebhookDerivedConfig.class);
			configuration.addAnnotatedClass(InsightsKPIConfig.class);
			configuration.addAnnotatedClass(InsightsContentConfig.class);
    		configuration.addAnnotatedClass(InsightsAssessmentReportTemplate.class);
		    configuration.addAnnotatedClass(InsightsAssessmentConfiguration.class);
		    configuration.addAnnotatedClass(InsightsReportsKPIConfig.class);
		    configuration.addAnnotatedClass(InsightsVisualizationConfig.class);
		    configuration.addAnnotatedClass(InsightsWorkflowTask.class);
		    configuration.addAnnotatedClass(InsightsWorkflowTaskSequence.class);
		    configuration.addAnnotatedClass(InsightsWorkflowConfiguration.class);
		    configuration.addAnnotatedClass(InsightsWorkflowExecutionHistory.class);
		    configuration.addAnnotatedClass(InsightsWorkflowType.class);
		    configuration.addAnnotatedClass(InsightsDataArchivalConfig.class);
		    configuration.addAnnotatedClass(InsightsEmailTemplates.class);
		    configuration.addAnnotatedClass(InsightsReportVisualizationContainer.class);
		    configuration.addAnnotatedClass(InsightsReportTemplateConfigFiles.class);
		    configuration.addAnnotatedClass(InsightsConfigFiles.class);
		    configuration.addAnnotatedClass(AutoMLConfig.class);
		    configuration.addAnnotatedClass(GrafanaDashboardPdfConfig.class);
		    configuration.addAnnotatedClass(UpshiftAssessmentConfig.class);
		    configuration.addAnnotatedClass(GrafanaOrgToken.class);
		    configuration.addAnnotatedClass(InsightsTools.class);
		    configuration.addAnnotatedClass(InsightsOutcomeTools.class);
		    configuration.addAnnotatedClass(MileStoneConfig.class);
		    configuration.addAnnotatedClass(InsightsMileStoneOutcomeConfig.class);
		    configuration.addAnnotatedClass(InsightsSchedulerTaskDefinition.class);
		    configuration.addAnnotatedClass(InsightsSchedulerTaskStatus.class);
		    configuration.addAnnotatedClass(InsightsGroupEmailConfiguration.class);
		    configuration.addAnnotatedClass(InsightsAgentHealthDetails.class);
		    configuration.addAnnotatedClass(InsightsComponentHealthDetails.class);
		    configuration.addAnnotatedClass( InsightsOfflineConfig.class);
		    configuration.addAnnotatedClass( InsightsOfflineAlerting.class);
		    configuration.addAnnotatedClass( InsightsStreamsSourceConfig.class);
		    configuration.addAnnotatedClass( InsightsReplicaConfig.class);

			PostgreData postgre = ApplicationConfigProvider.getInstance().getPostgre();
			if(postgre != null){				
				configuration.setProperty(AvailableSettings.USER, postgre.getUserName());
				configuration.setProperty(AvailableSettings.PASS, postgre.getPassword());
				configuration.setProperty(AvailableSettings.URL, postgre.getInsightsDBUrl());
				configuration.setProperty(AvailableSettings.DRIVER, postgre.getDriver());
				configuration.setProperty(AvailableSettings.DIALECT,postgre.getDialect());
				configuration.setProperty(AvailableSettings.SHOW_SQL, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.FORMAT_SQL, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, "update");
				/* c3p configuration setting */
				if (postgre.getDriver().equals("org.postgresql.Driver")) {
					configuration.setProperty(AvailableSettings.C3P0_MIN_SIZE, postgre.getC3pMinSize());
					configuration.setProperty(AvailableSettings.C3P0_MAX_SIZE, postgre.getC3pMaxSize());
					configuration.setProperty(AvailableSettings.C3P0_MAX_STATEMENTS, postgre.getC3pMaxStatements());
					configuration.setProperty(AvailableSettings.C3P0_TIMEOUT, postgre.getC3pTimout());
					configuration.setProperty(AvailableSettings.C3P0_ACQUIRE_INCREMENT, "1");
				}
			}		
			try {
				StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
				sessionFactory = configuration.buildSessionFactory(builder.build());
			} catch (HibernateException e) {
				printHibernateDALException(e,"HibernateException occur during staring session Factory");
				log.error(e);
				throw e;
			}
		}
	}
	
	private static void initGrafanaDAL() {
		if(grafanaSessionFactory == null){
			Configuration configuration = new Configuration();
			PostgreData postgre = ApplicationConfigProvider.getInstance().getPostgre();
			if(postgre != null && postgre.getDriver().equals("org.postgresql.Driver")){
				configuration.setProperty(AvailableSettings.USER, postgre.getUserName());
				configuration.setProperty(AvailableSettings.PASS, postgre.getPassword());
				configuration.setProperty(AvailableSettings.URL, postgre.getGrafanaDBUrl());
				configuration.setProperty(AvailableSettings.DRIVER, postgre.getDriver());
				configuration.setProperty(AvailableSettings.DIALECT,postgre.getDialect());
				configuration.setProperty(AvailableSettings.SHOW_SQL, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.FORMAT_SQL, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, Boolean.FALSE.toString());
				configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, "validate");
				try {
					StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
					grafanaSessionFactory = configuration.buildSessionFactory(builder.build());
				} catch (HibernateException e) {
					printHibernateDALException(e,"HibernateException occur during staring grafana session Factory");
					log.error(e);
					throw e;
				}
			}
		}
	}

	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}
	
	public static SessionFactory getGrafanaSessionFactory(){
		return grafanaSessionFactory;
	}
	
	static void  printHibernateDALException(PersistenceException e, Object sourceProperty) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTrace = stacktrace[3];
		StackTraceElement stacktraceService = stacktrace[4];
		
		log.error(e);
		log.error(
				"Type=HibernateException className={} methodName={} lineNo={} serviceFileName={} serviceMethodName={} serviceFileLineNo={} "
						+ "exceptionClass={} sourceProperty={} message={} ",
				stackTrace.getFileName(), stackTrace.getMethodName(), stackTrace.getLineNumber(),stacktraceService.getFileName(),stacktraceService.getMethodName(),stacktraceService.getLineNumber(), e.getClass(),
				sourceProperty, e.getMessage());
	}
}