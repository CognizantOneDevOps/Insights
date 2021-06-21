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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;

public class ReportConfigDAL extends BaseDAL {
	private static Logger log = LogManager.getLogger(ReportConfigDAL.class);

	/**
	 * Method to update InsightsKPIConfig record
	 * 
	 * @param config
	 * @return int
	 */
	public int updateKpiConfig(InsightsKPIConfig config) {
		try {
			update(config);
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Active KPI using KpiIds
	 * 
	 * @param kpiIds
	 * @return List<InsightsKPIConfig>
	 */
	public List<InsightsKPIConfig> getActiveKPIConfigurationsBasedOnKPIId(List<Integer> kpiIds) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			Map<String, Object> extraParameters = new HashMap<>();
			extraParameters.put("ids", kpiIds);
			
			return executeQueryWithExtraParameter(
					"FROM InsightsKPIConfig CC WHERE CC.isActive = TRUE AND CC.kpiId IN :ids ORDER BY CC.kpiId  ",
					InsightsKPIConfig.class, parameters, extraParameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save KPI Configuration
	 * 
	 * @param config
	 * @return int
	 */
	public int saveKpiConfig(InsightsKPIConfig config) {
		try {
			return (int) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save Content Configuration
	 * 
	 * @param config
	 * @return contentId
	 */
	public int saveContentConfig(InsightsContentConfig config) {
		try {
			return (int) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get KPI config using KpiId
	 * 
	 * @param kpiId
	 * @return InsightsKPIConfig object
	 */
	public InsightsKPIConfig getKPIConfig(int kpiId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.KPIID, kpiId);
			return getUniqueResult(
					"FROM InsightsKPIConfig KC WHERE KC.kpiId = :kpiId ",
					InsightsKPIConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Content config using contentId
	 * 
	 * @param contentId
	 * @return InsightsContentConfig object
	 */
	public InsightsContentConfig getContentConfig(int contentId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("contentId", contentId);
			return getUniqueResult(
					"FROM InsightsContentConfig CC WHERE CC.contentId = :contentId ",
					InsightsContentConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Active Content Config using KpiId
	 * 
	 * @param kpiId
	 * @return List<InsightsContentConfig>
	 */
	public List<InsightsContentConfig> getActiveContentConfigByKPIId(int kpiId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("kpiId", kpiId);
			return getResultList(
					"FROM InsightsContentConfig CC WHERE CC.isActive = TRUE AND CC.kpiConfig.kpiId =:kpiId ORDER BY CC.contentId  ",
					InsightsContentConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

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
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", id);
			InsightsAssessmentConfiguration parentConfigList = getSingleResult(
					"FROM InsightsAssessmentConfiguration IC WHERE IC.id = :id",
					InsightsAssessmentConfiguration.class,
					parameters);
			
			if (parentConfigList != null) {
				saveOrUpdate(assessmentReportConfiguration);
				return id;
			} else {
				throw new InsightsCustomException("id's data is not present in the table");
			}
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all active Report Templates
	 * 
	 * @return List<InsightsAssessmentReportTemplate>
	 */
	public List<InsightsAssessmentReportTemplate> getAllReportTemplates() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsAssessmentReportTemplate RE where RE.isActive =TRUE ORDER BY RE.reportId",
					InsightsAssessmentReportTemplate.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to delete Assessment report
	 * 
	 * @param id
	 * @return String
	 */
	public String deleteAssessmentReport(int id) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", id);
			InsightsAssessmentConfiguration asssessment = getSingleResult(
					"FROM InsightsAssessmentConfiguration a WHERE a.id= :id",
					InsightsAssessmentConfiguration.class,
					parameters);
			asssessment.setReportTemplateEntity(null);
			delete(asssessment);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Assessment Configuration using Report Id
	 * 
	 * @param reportId
	 * @return InsightsAssessmentConfiguration object
	 */
	public InsightsAssessmentConfiguration getAssessmentConfigListByReportId(int reportId) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			return getSingleResult(
					"FROM InsightsAssessmentConfiguration CE WHERE CE.id = :reportId ",
					InsightsAssessmentConfiguration.class,
					parameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Active Report template using Report Id
	 * 
	 * @param reportId
	 * @return Object
	 */
	public Object getActiveReportTemplateByReportId(int reportId) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			return getUniqueResult(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.isActive = TRUE AND RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class,
					parameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
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
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("kpiId", kpiId);
			return getResultList(
					"FROM InsightsReportsKPIConfig REK WHERE REK.kpiConfig.kpiId = :kpiId",
					InsightsReportsKPIConfig.class,
					parameters);
			
		} catch (Exception e) {
			log.error(e);
			return reportList;
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
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			return getUniqueResult(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class,
					parameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get KPI Config using ReportId
	 * 
	 * @param reportId
	 * @return List<InsightsKPIConfig>
	 */
	public List<InsightsKPIConfig> getKpiConfigByTemplateReportId(int reportId) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			InsightsAssessmentReportTemplate report = getSingleResult(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.isActive = TRUE AND RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class,
					parameters);
			
			List<InsightsKPIConfig> kpiConfigList = new ArrayList<>();
			report.getReportsKPIConfig()
					.forEach(reportsKpiConfig -> kpiConfigList.add(reportsKpiConfig.getKpiConfig()));
			return kpiConfigList;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save InsightsAssessmentReportTemplate record
	 * 
	 * @param reportConfig
	 * @return int
	 */
	public int saveReportConfig(InsightsAssessmentReportTemplate reportConfig) {
		try {
			return (int) save(reportConfig);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * This Method is used to update report Template
	 * 
	 * @param reportTemplateEntity
	 * @return Boolean
	 * @throws InsightsCustomException
	 */
	public Boolean updateReportTemplate(InsightsAssessmentReportTemplate reportTemplateEntity)
			throws InsightsCustomException {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", reportTemplateEntity.getReportId());
			InsightsAssessmentReportTemplate reportDBConfigList =  getSingleResult(
					"FROM InsightsAssessmentReportTemplate a WHERE a.reportId= :id",
					InsightsAssessmentReportTemplate.class,
					parameters);
			
			if (reportDBConfigList != null) {
				Set<InsightsReportsKPIConfig> kpiDataFromUI = reportTemplateEntity.getReportsKPIConfig();
				reportDBConfigList.setReportId(reportTemplateEntity.getReportId());
				reportDBConfigList.setActive(reportTemplateEntity.isActive());
				reportDBConfigList.setDescription(reportTemplateEntity.getDescription());
				reportDBConfigList.setTemplateName(reportTemplateEntity.getTemplateName());
				reportDBConfigList.setFile(reportTemplateEntity.getFile());
				reportDBConfigList.setVisualizationutil(reportTemplateEntity.getVisualizationutil());
				Set<InsightsReportsKPIConfig> kpiDataFromTable = reportDBConfigList.getReportsKPIConfig();
				kpiDataFromTable.clear();
				kpiDataFromTable.addAll(kpiDataFromUI);
				reportDBConfigList.setReportsKPIConfig(kpiDataFromTable);
				saveOrUpdate(reportDBConfigList);
				return Boolean.TRUE;
			} else {
				throw new InsightsCustomException(" Report template not exists in database for edit template "
						+ reportTemplateEntity.getReportId());
			}
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to update InsightsAssessmentReportTemplate record
	 * 
	 * @param reportConfig
	 * @return int
	 */
	public void updateReportConfig(InsightsAssessmentReportTemplate reportConfig) {
		try {
			saveOrUpdate(reportConfig);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Assessment Configurations
	 * 
	 * @return List<InsightsAssessmentConfiguration>
	 */
	public List<InsightsAssessmentConfiguration> getAllAssessmentConfig() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsAssessmentConfiguration CE ORDER by CE.asseementreportname ASC",
					InsightsAssessmentConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Assessment Configuration using assessment name
	 * 
	 * @param assessmentName
	 * @return InsightsAssessmentConfiguration object
	 */
	public InsightsAssessmentConfiguration getAssessmentByAssessmentName(String assessmentName) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("assessmentName", assessmentName);
			return getUniqueResult(
					"FROM InsightsAssessmentConfiguration RE WHERE RE.asseementreportname = :assessmentName",
					InsightsAssessmentConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get AssessmentConfiguration using configId
	 * 
	 * @param configId
	 * @return InsightsAssessmentConfiguration object
	 */
	public InsightsAssessmentConfiguration getAssessmentByConfigId(int configId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", configId);
			return getSingleResult(
					"FROM InsightsAssessmentConfiguration RE WHERE RE.id = :id",
					InsightsAssessmentConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save Assessment Configuration record
	 * 
	 * @param config
	 * @return int
	 */
	public int saveInsightsAssessmentConfig(InsightsAssessmentConfiguration config) {
		try {
			return (int) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete KPI using KpiId
	 * 
	 * @param kpiID
	 * @return String
	 */
	public String deleteKPIbyKpiID(int kpiID) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", kpiID);
			InsightsKPIConfig executionRecord = getSingleResult(
					"FROM InsightsKPIConfig a WHERE a.kpiId= :id",
					InsightsKPIConfig.class,
					parameters);
			delete(executionRecord);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Content using contentId
	 * 
	 * @param contentID
	 * @return String
	 */
	public String deleteContentbyContentID(int contentID) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", contentID);
			InsightsContentConfig executionRecord = getSingleResult(
					"FROM InsightsContentConfig a WHERE a.contentId= :id",
					InsightsContentConfig.class,
					parameters);
			delete(executionRecord);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Content using KPIId
	 * 
	 * @param contentID
	 * @return String
	 */
	public String deleteContentbyKPIID(int kpiId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", kpiId);
			List<InsightsContentConfig> executionRecord =  getResultList(
					"FROM InsightsContentConfig a WHERE a.kpiConfig.kpiId= :id",
					InsightsContentConfig.class,
					parameters);
			for (InsightsContentConfig insightsContentConfig : executionRecord) {
				delete(insightsContentConfig);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Report Template using reportId
	 * 
	 * @param reportID
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String deleteReportTemplatebyReportID(int reportID) throws InsightsCustomException {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", reportID);
			InsightsAssessmentReportTemplate executionRecord = getUniqueResult(
					"FROM InsightsAssessmentReportTemplate a WHERE a.reportId= :id",
					InsightsAssessmentReportTemplate.class,
					parameters);

			if (executionRecord != null) {
				List<InsightsAssessmentConfiguration> resultList = getAssessmentConfigListByReportTemplateId(
						executionRecord.getReportId());
				if (resultList.isEmpty()) {
					delete(executionRecord);
				} else {
					throw new InsightsCustomException(
							"Report Template should not be deleted as it is attached to Assessment Report.");
				}
			} else {
				throw new InsightsCustomException("Report Template record not found");
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException("Error while deleting report template, Please check log for detail  ");
		}
		return PlatformServiceConstants.SUCCESS;

	}

	/**
	 * Method to get Assessment Configuration List using Report Template Id
	 * 
	 * @param reportId
	 * @return InsightsAssessmentConfiguration object
	 */
	public List<InsightsAssessmentConfiguration> getAssessmentConfigListByReportTemplateId(int reportTemplateId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.REPORTID, reportTemplateId);
			return getResultList(
					"FROM InsightsAssessmentConfiguration CE WHERE CE.reportTemplateEntity.reportId = :reportId ",
					InsightsAssessmentConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete ReportKPIConfig Record using reportID
	 * 
	 * @param reportID
	 * @return String
	 */
	public String deleteReportKPIConfigRecordByReportID(int reportID) {
		try  {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", reportID);
			List<InsightsReportsKPIConfig> executionKpiListRecord = getResultList(
					"FROM InsightsReportsKPIConfig a WHERE a.reportTemplateEntity.reportId= :id",
					InsightsReportsKPIConfig.class,
					parameters);

			for (InsightsReportsKPIConfig record : executionKpiListRecord) {
				record.setReportTemplateEntity(null);
				record.setKpiConfig(null);delete(record);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to update Content configuration
	 * 
	 * @param config
	 * @return int
	 */
	public int updateContentConfig(InsightsContentConfig config) {
		try  {
			update(config);
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method used to get all active kpiConfig list
	 * 
	 * @return List<InsightsKPIConfig>
	 */
	public List<InsightsKPIConfig> getAllActiveKpiConfig() {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsKPIConfig IK WHERE IK.isActive = TRUE ORDER BY IK.kpiId desc ",
					InsightsKPIConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method used to get all active content config list
	 * 
	 * @return List<InsightsContentConfig>
	 */
	public List<InsightsContentConfig> getAllActiveContentList() {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsContentConfig IC WHERE IC.isActive = TRUE ORDER BY IC.contentId desc ",
					InsightsContentConfig.class,
					parameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Report Templates
	 * 
	 * @return List<InsightsAssessmentReportTemplate>
	 */
	public List<InsightsAssessmentReportTemplate> getAllReportTemplatesList() {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsAssessmentReportTemplate RE",
					InsightsAssessmentReportTemplate.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get KPI details using reportId
	 * 
	 * @param reportId
	 * @return List<InsightsReportsKPIConfig>
	 */
	public List<InsightsReportsKPIConfig> getTemplateKpiDetailsByReportId(int reportId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			return getResultList(
					"FROM InsightsReportsKPIConfig a WHERE a.reportTemplateEntity.reportId= :reportId",
					InsightsReportsKPIConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get Active Report template using Report Id
	 * 
	 * @param reportId
	 * @return Object
	 */
	public Object getActiveReportTemplateByName(String reportTemplateName) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("reportTemplateName", reportTemplateName);
			return getUniqueResult(
					"FROM InsightsAssessmentReportTemplate RE where RE.templateName = :reportTemplateName",
					InsightsAssessmentReportTemplate.class,
					parameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get KPI Config using usecase name
	 * 
	 * @param usecase
	 * @return List<InsightsKPIConfig>
	 */
	public List<InsightsKPIConfig> getKpiConfigByUsecase(String usecase) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("usecase", usecase);
			return getResultList(
					"FROM InsightsKPIConfig RE WHERE RE.usecase = :usecase",
					InsightsKPIConfig.class,
					parameters);
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save Report Template Config Files
	 * 
	 * @param config
	 * @return int
	 */
	public int saveReportTemplateConfigFiles(InsightsReportTemplateConfigFiles config) {
		try {
			return (int) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to fetch Report Template Config Files using Report ID
	 * 
	 * @param reportId
	 * @return List<InsightsReportTemplateConfigFiles>
	 */
	public List<InsightsReportTemplateConfigFiles> getReportTemplateConfigFileByReportId(int reportId) {
		try  {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("reportId", reportId);
			return getResultList(
					"FROM InsightsReportTemplateConfigFiles RE WHERE RE.reportId = :reportId",
					InsightsReportTemplateConfigFiles.class,
					parameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	
	/**
	 * Method to fetch Report Template Config Files using Filename and ReportId
	 * 
	 * @param fileName
	 * @param reportId
	 * @return InsightsReportTemplateConfigFiles
	 */
	public InsightsReportTemplateConfigFiles getReportTemplateConfigFileByFileNameAndReportId(String fileName,int reportId) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("fileName", fileName);
			parameters.put("reportId", reportId);
			return getUniqueResult(
					"FROM InsightsReportTemplateConfigFiles RE WHERE RE.fileName = :fileName and RE.reportId = :reportId",
					InsightsReportTemplateConfigFiles.class,
					parameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to update Report Template Config Files
	 * 
	 * @param config
	 * @return int
	 */
	public int updateReportTemplateConfigFiles(InsightsReportTemplateConfigFiles config) {
		try  {
			update(config);
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * Method to delete Report Template Design Files using reportTemplateID
	 * 
	 * @param reportTemplateID
	 * @return String
	 */
	public String deleteTemplateDesignFilesByReportTemplateID(int reportTemplateID) {
		try  {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", reportTemplateID);
			List<InsightsReportTemplateConfigFiles> executionRecord = getResultList(
					"FROM InsightsReportTemplateConfigFiles a WHERE a.reportId= :id",
					InsightsReportTemplateConfigFiles.class,
					parameters);
			
			for (InsightsReportTemplateConfigFiles record : executionRecord) {
				delete(record);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
