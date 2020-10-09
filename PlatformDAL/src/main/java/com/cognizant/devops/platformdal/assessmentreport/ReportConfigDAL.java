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
package com.cognizant.devops.platformdal.assessmentreport;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class ReportConfigDAL extends BaseDAL {

	/**
	 * Method to update InsightsKPIConfig record
	 * 
	 * @param config
	 * @return int
	 */
	public int updateKpiConfig(InsightsKPIConfig config) {

		getSession().beginTransaction();
		getSession().update(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return 0;
	}

	/**
	 * Method to get Active KPI using KpiIds
	 * 
	 * @param kpiIds
	 * @return List<InsightsKPIConfig>
	 */
	public List<InsightsKPIConfig> getActiveKPIConfigurationsBasedOnKPIId(List<Integer> kpiIds) {
		Query<InsightsKPIConfig> createQuery = getSession().createQuery(
				"FROM InsightsKPIConfig CC WHERE CC.isActive = TRUE AND CC.kpiId IN :ids ORDER BY CC.kpiId  ",
				InsightsKPIConfig.class);
		createQuery.setParameterList("ids", kpiIds);
		List<InsightsKPIConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to save KPI Configuration
	 * 
	 * @param config
	 * @return int
	 */
	public int saveKpiConfig(InsightsKPIConfig config) {
		getSession().beginTransaction();
		int kpiId = (int) getSession().save(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return kpiId;
	}

	/**
	 * Method to save Content Configuration
	 * 
	 * @param config
	 * @return contentId
	 */
	public int saveContentConfig(InsightsContentConfig config) {
		getSession().beginTransaction();
		int contentId = (int) getSession().save(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return contentId;
	}

	/**
	 * Method to get KPI config using KpiId
	 * 
	 * @param kpiId
	 * @return InsightsKPIConfig object
	 */
	public InsightsKPIConfig getKPIConfig(int kpiId) {
		Query<InsightsKPIConfig> createQuery = getSession()
				.createQuery("FROM InsightsKPIConfig KC WHERE KC.kpiId = :kpiId ", InsightsKPIConfig.class);
		createQuery.setParameter("kpiId", kpiId);
		InsightsKPIConfig result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Content config using contentId
	 * 
	 * @param contentId
	 * @return InsightsContentConfig object
	 */
	public InsightsContentConfig getContentConfig(int contentId) {
		Query<InsightsContentConfig> createQuery = getSession().createQuery(
				"FROM InsightsContentConfig CC WHERE CC.contentId = :contentId ", InsightsContentConfig.class);
		createQuery.setParameter("contentId", contentId);
		InsightsContentConfig result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Active Content Config using KpiId
	 * 
	 * @param kpiId
	 * @return List<InsightsContentConfig>
	 */
	public List<InsightsContentConfig> getActiveContentConfigByKPIId(int kpiId) {
		Query<InsightsContentConfig> createQuery = getSession().createQuery(
				"FROM InsightsContentConfig CC WHERE CC.isActive = TRUE AND CC.kpiConfig.kpiId =:kpiId ORDER BY CC.contentId  ",
				InsightsContentConfig.class);
		createQuery.setParameter("kpiId", kpiId);
		List<InsightsContentConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;

	}

	/**
	 * Method to update Assessment Report
	 * 
	 * @param assessmentReportConfiguration
	 * @param id
	 * @return int
	 * @throws InsightsCustomException
	 */
	public int updateAssessmentReportConfiguration(InsightsAssessmentConfiguration assessmentReportConfiguration,
			int id) throws InsightsCustomException {
		Query<InsightsAssessmentConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsAssessmentConfiguration IC WHERE IC.id = :id", InsightsAssessmentConfiguration.class);
		createQuery.setParameter("id", id);
		InsightsAssessmentConfiguration parentConfigList = createQuery.getSingleResult();
		terminateSession();
		if (parentConfigList != null) {
			getSession().beginTransaction();
			getSession().saveOrUpdate(assessmentReportConfiguration);
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();
			return id;
		} else {
			terminateSession();
			terminateSessionFactory();
			throw new InsightsCustomException("id's data is not present in the table");
		}

	}

	/**
	 * Method to get all Report Templates
	 * 
	 * @return List<InsightsAssessmentReportTemplate>
	 */
	public List<InsightsAssessmentReportTemplate> getAllReportTemplates() {
		Query<InsightsAssessmentReportTemplate> query = getSession().createQuery(
				"FROM InsightsAssessmentReportTemplate RE where RE.isActive =TRUE ORDER BY RE.reportId",
				InsightsAssessmentReportTemplate.class);
		List<InsightsAssessmentReportTemplate> reportEntityResult = query.getResultList();
		terminateSession();
		terminateSessionFactory();
		return reportEntityResult;

	}

	/**
	 * Method to delete Assessment report
	 * 
	 * @param id
	 * @return String
	 */
	public String deleteAssessmentReport(int id) {
		getSession().beginTransaction();
		Query<InsightsAssessmentConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsAssessmentConfiguration a WHERE a.id= :id", InsightsAssessmentConfiguration.class);
		createQuery.setParameter("id", id);
		InsightsAssessmentConfiguration asssessment = createQuery.getSingleResult();
		asssessment.setReportTemplateEntity(null);
		getSession().delete(asssessment);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to get Assessment Configuration using Report Id
	 * 
	 * @param reportId
	 * @return InsightsAssessmentConfiguration object
	 */
	public InsightsAssessmentConfiguration getAssessmentConfigListByReportId(int reportId) {
		Query<InsightsAssessmentConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsAssessmentConfiguration CE WHERE CE.id = :reportId ",
				InsightsAssessmentConfiguration.class);
		createQuery.setParameter("reportId", reportId);
		InsightsAssessmentConfiguration result = createQuery.getSingleResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Active Report template using Report Id
	 * 
	 * @param reportId
	 * @return Object
	 */
	public Object getActiveReportTemplateByReportId(int reportId) {
		try {
			Query<InsightsAssessmentReportTemplate> createQuery = getSession().createQuery(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.isActive = TRUE AND RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter("reportId", reportId);
			return createQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	/**
	 * Method to get Active Report template using kpi Id
	 * 
	 * @param reportId
	 * @return Object
	 */
	public List<InsightsReportsKPIConfig> getActiveReportTemplateByKPIId(int kpiId) {
		List<InsightsReportsKPIConfig> reportList = new ArrayList<>();
		try {
			Query<InsightsReportsKPIConfig> createQuery = getSession().createQuery(
					"FROM InsightsReportsKPIConfig REK WHERE REK.kpiConfig.kpiId = :kpiId",
					InsightsReportsKPIConfig.class);
			createQuery.setParameter("kpiId", kpiId);
			reportList = createQuery.getResultList();
			return reportList;
		} catch (Exception e) {
			return reportList;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	/**
	 * Method to get Report Template using ReportId
	 * 
	 * @param reportId
	 * @return Object
	 */
	public Object getReportTemplateByReportId(int reportId) {
		try {
			Query<InsightsAssessmentReportTemplate> createQuery = getSession().createQuery(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter("reportId", reportId);
			return createQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}

	}

	/**
	 * Method to get KPI Config using ReportId
	 * 
	 * @param reportId
	 * @return List<InsightsKPIConfig>
	 */
	public List<InsightsKPIConfig> getKpiConfigByTemplateReportId(int reportId) {
		List<InsightsKPIConfig> kpiConfigList = new ArrayList<>();
		Query<InsightsAssessmentReportTemplate> createQuery = getSession().createQuery(
				"FROM InsightsAssessmentReportTemplate RE WHERE RE.isActive = TRUE AND RE.reportId = :reportId",
				InsightsAssessmentReportTemplate.class);
		createQuery.setParameter("reportId", reportId);
		InsightsAssessmentReportTemplate report = createQuery.getSingleResult();
		report.getReportsKPIConfig().forEach(reportsKpiConfig -> kpiConfigList.add(reportsKpiConfig.getKpiConfig()));
		terminateSession();
		terminateSessionFactory();
		return kpiConfigList;
	}

	/**
	 * Method to save InsightsAssessmentReportTemplate record
	 * 
	 * @param reportConfig
	 * @return int
	 */
	public int saveReportConfig(InsightsAssessmentReportTemplate reportConfig) {
		getSession().beginTransaction();
		int reportId = (int) getSession().save(reportConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return reportId;
	}

	/**
	 * Method to get all Assessment Configurations
	 * 
	 * @return List<InsightsAssessmentConfiguration>
	 */
	public List<InsightsAssessmentConfiguration> getAllAssessmentConfig() {
		Query<InsightsAssessmentConfiguration> createQuery = getSession()
				.createQuery("FROM InsightsAssessmentConfiguration CE", InsightsAssessmentConfiguration.class);
		List<InsightsAssessmentConfiguration> configEntity = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return configEntity;
	}

	/**
	 * Method to get Assessment Configuration using assessment name
	 * 
	 * @param assessmentName
	 * @return InsightsAssessmentConfiguration object
	 */
	public InsightsAssessmentConfiguration getAssessmentByAssessmentName(String assessmentName) {
		try {
			Query<InsightsAssessmentConfiguration> createQuery = getSession().createQuery(
					"FROM InsightsAssessmentConfiguration RE WHERE RE.asseementreportname = :assessmentName",
					InsightsAssessmentConfiguration.class);
			createQuery.setParameter("assessmentName", assessmentName);
			return createQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	/**
	 * Method to get AssessmentConfiguration using configId
	 * 
	 * @param configId
	 * @return InsightsAssessmentConfiguration object
	 */
	public InsightsAssessmentConfiguration getAssessmentByConfigId(int configId) {
		Query<InsightsAssessmentConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsAssessmentConfiguration RE WHERE RE.id = :id", InsightsAssessmentConfiguration.class);
		createQuery.setParameter("id", configId);
		InsightsAssessmentConfiguration report = createQuery.getSingleResult();
		terminateSession();
		terminateSessionFactory();
		return report;
	}

	/**
	 * Method to save Assessment Configuration record
	 * 
	 * @param config
	 * @return int
	 */
	public int saveInsightsAssessmentConfig(InsightsAssessmentConfiguration config) {

		getSession().beginTransaction();
		int assessmentReportId = (int) getSession().save(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return assessmentReportId;
	}

	/**
	 * Method to delete KPI using KpiId
	 * 
	 * @param kpiID
	 * @return String
	 */
	public String deleteKPIbyKpiID(int kpiID) {
		getSession().beginTransaction();
		Query<InsightsKPIConfig> createQuery = getSession().createQuery("FROM InsightsKPIConfig a WHERE a.kpiId= :id",
				InsightsKPIConfig.class);
		createQuery.setParameter("id", kpiID);
		InsightsKPIConfig executionRecord = createQuery.getSingleResult();
		getSession().delete(executionRecord);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to delete Content using contentId
	 * 
	 * @param contentID
	 * @return String
	 */
	public String deleteContentbyContentID(int contentID) {
		getSession().beginTransaction();
		Query<InsightsContentConfig> createQuery = getSession()
				.createQuery("FROM InsightsContentConfig a WHERE a.contentId= :id", InsightsContentConfig.class);
		createQuery.setParameter("id", contentID);
		InsightsContentConfig executionRecord = createQuery.getSingleResult();
		getSession().delete(executionRecord);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to delete Content using KPIId
	 * 
	 * @param contentID
	 * @return String
	 */
	public String deleteContentbyKPIID(int kpiId) {
		getSession().beginTransaction();
		Query<InsightsContentConfig> createQuery = getSession()
				.createQuery("FROM InsightsContentConfig a WHERE a.kpiConfig.kpiId= :id", InsightsContentConfig.class);
		createQuery.setParameter("id", kpiId);
		List<InsightsContentConfig> executionRecord = createQuery.getResultList();
		for (InsightsContentConfig insightsContentConfig : executionRecord) {
			getSession().delete(insightsContentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to delete Report Template using reportId
	 * 
	 * @param reportID
	 * @return String
	 */
	public String deleteReportTemplatebyReportID(int reportID) {
		Query<InsightsAssessmentReportTemplate> createQuery = getSession().createQuery(
				"FROM InsightsAssessmentReportTemplate a WHERE a.reportId= :id",
				InsightsAssessmentReportTemplate.class);
		createQuery.setParameter("id", reportID);
		InsightsAssessmentReportTemplate executionRecord = createQuery.getSingleResult();
		executionRecord.setReportsKPIConfig(null);
		String status = deleteReportKPIConfigRecordByReportID(executionRecord.getReportId());
		getSession().beginTransaction();
		if (PlatformServiceConstants.SUCCESS.equals(status)) {
			getSession().delete(executionRecord);
			getSession().getTransaction().commit();
		} else {
			return PlatformServiceConstants.FAILURE;
		}
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;

	}

	/**
	 * Method to delete ReportKPIConfig Record using reportID
	 * 
	 * @param reportID
	 * @return String
	 */
	public String deleteReportKPIConfigRecordByReportID(int reportID) {

		Query<InsightsReportsKPIConfig> createQuery = getSession().createQuery(
				"FROM InsightsReportsKPIConfig a WHERE a.reportTemplateEntity.reportId= :id",
				InsightsReportsKPIConfig.class);
		createQuery.setParameter("id", reportID);
		List<InsightsReportsKPIConfig> executionRecord = createQuery.getResultList();
		for (InsightsReportsKPIConfig record : executionRecord) {
			getSession().beginTransaction();
			record.setReportTemplateEntity(null);
			record.setKpiConfig(null);
			getSession().delete(record);
			getSession().getTransaction().commit();
		}
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to update Content configuration
	 * 
	 * @param config
	 * @return int
	 */
	public int updateContentConfig(InsightsContentConfig config) {

		getSession().beginTransaction();
		getSession().update(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return 0;
	}

	/**
	 * Method used to get all active kpiConfig list
	 * 
	 * @return
	 */
	public List<InsightsKPIConfig> getAllActiveKpiConfig() {
		Query<InsightsKPIConfig> createQuery = getSession().createQuery(
				"FROM InsightsKPIConfig IK WHERE IK.isActive = TRUE ORDER BY IK.kpiId desc ", InsightsKPIConfig.class);
		List<InsightsKPIConfig> configEntity = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return configEntity;
	}

	/**
	 * Method used to get all active content config list
	 * 
	 * @return
	 */
	public List<InsightsContentConfig> getAllActiveContentList() {
		Query<InsightsContentConfig> createQuery = getSession().createQuery(
				"FROM InsightsContentConfig IC WHERE IC.isActive = TRUE ORDER BY IC.contentId desc ",
				InsightsContentConfig.class);
		List<InsightsContentConfig> configEntity = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return configEntity;
	}
}
