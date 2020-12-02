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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class ReportConfigDAL extends BaseDAL {
	private static Logger log = LogManager.getLogger(ReportConfigDAL.class);

	/**
	 * Method to update InsightsKPIConfig record
	 * 
	 * @param config
	 * @return int
	 */
	public int updateKpiConfig(InsightsKPIConfig config) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.update(config);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsKPIConfig> createQuery = session.createQuery(
					"FROM InsightsKPIConfig CC WHERE CC.isActive = TRUE AND CC.kpiId IN :ids ORDER BY CC.kpiId  ",
					InsightsKPIConfig.class);
			createQuery.setParameterList("ids", kpiIds);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int kpiId = (int) session.save(config);
			session.getTransaction().commit();
			return kpiId;
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int contentId = (int) session.save(config);
			session.getTransaction().commit();
			return contentId;
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
		try (Session session = getSessionObj()) {
			Query<InsightsKPIConfig> createQuery = session
					.createQuery("FROM InsightsKPIConfig KC WHERE KC.kpiId = :kpiId ", InsightsKPIConfig.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.KPIID, kpiId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsContentConfig> createQuery = session.createQuery(
					"FROM InsightsContentConfig CC WHERE CC.contentId = :contentId ", InsightsContentConfig.class);
			createQuery.setParameter("contentId", contentId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsContentConfig> createQuery = session.createQuery(
					"FROM InsightsContentConfig CC WHERE CC.isActive = TRUE AND CC.kpiConfig.kpiId =:kpiId ORDER BY CC.contentId  ",
					InsightsContentConfig.class);
			createQuery.setParameter("kpiId", kpiId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj(); Session saveSession = getSessionObj()) {
			Query<InsightsAssessmentConfiguration> createQuery = session.createQuery(
					"FROM InsightsAssessmentConfiguration IC WHERE IC.id = :id", InsightsAssessmentConfiguration.class);
			createQuery.setParameter("id", id);
			InsightsAssessmentConfiguration parentConfigList = createQuery.getSingleResult();
			if (parentConfigList != null) {
				saveSession.beginTransaction();
				saveSession.saveOrUpdate(assessmentReportConfiguration);
				saveSession.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentReportTemplate> query = session.createQuery(
					"FROM InsightsAssessmentReportTemplate RE where RE.isActive =TRUE ORDER BY RE.reportId",
					InsightsAssessmentReportTemplate.class);
			return query.getResultList();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<InsightsAssessmentConfiguration> createQuery = session.createQuery(
					"FROM InsightsAssessmentConfiguration a WHERE a.id= :id", InsightsAssessmentConfiguration.class);
			createQuery.setParameter("id", id);
			InsightsAssessmentConfiguration asssessment = createQuery.getSingleResult();
			asssessment.setReportTemplateEntity(null);
			session.delete(asssessment);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentConfiguration> createQuery = session.createQuery(
					"FROM InsightsAssessmentConfiguration CE WHERE CE.id = :reportId ",
					InsightsAssessmentConfiguration.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			return createQuery.getSingleResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentReportTemplate> createQuery = session.createQuery(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.isActive = TRUE AND RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsReportsKPIConfig> createQuery = session.createQuery(
					"FROM InsightsReportsKPIConfig REK WHERE REK.kpiConfig.kpiId = :kpiId",
					InsightsReportsKPIConfig.class);
			createQuery.setParameter("kpiId", kpiId);
			reportList = createQuery.getResultList();
			return reportList;
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentReportTemplate> createQuery = session.createQuery(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.REPORTID, reportId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			List<InsightsKPIConfig> kpiConfigList = new ArrayList<>();
			Query<InsightsAssessmentReportTemplate> createQuery = session.createQuery(
					"FROM InsightsAssessmentReportTemplate RE WHERE RE.isActive = TRUE AND RE.reportId = :reportId",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter("reportId", reportId);
			InsightsAssessmentReportTemplate report = createQuery.getSingleResult();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int reportId = (int) session.save(reportConfig);
			session.getTransaction().commit();
			return reportId;
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentReportTemplate> createQuery = session.createQuery(
					"FROM InsightsAssessmentReportTemplate a WHERE a.reportId= :id",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter("id", reportTemplateEntity.getReportId());
			InsightsAssessmentReportTemplate reportDBConfigList = createQuery.getSingleResult();
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
				session.beginTransaction();
				session.saveOrUpdate(reportDBConfigList);
				session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.saveOrUpdate(reportConfig);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentConfiguration> createQuery = session
					.createQuery("FROM InsightsAssessmentConfiguration CE", InsightsAssessmentConfiguration.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentConfiguration> createQuery = session.createQuery(
					"FROM InsightsAssessmentConfiguration RE WHERE RE.asseementreportname = :assessmentName",
					InsightsAssessmentConfiguration.class);
			createQuery.setParameter("assessmentName", assessmentName);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentConfiguration> createQuery = session.createQuery(
					"FROM InsightsAssessmentConfiguration RE WHERE RE.id = :id", InsightsAssessmentConfiguration.class);
			createQuery.setParameter("id", configId);
			return createQuery.getSingleResult();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int assessmentReportId = (int) session.save(config);
			session.getTransaction().commit();
			return assessmentReportId;
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<InsightsKPIConfig> createQuery = session.createQuery("FROM InsightsKPIConfig a WHERE a.kpiId= :id",
					InsightsKPIConfig.class);
			createQuery.setParameter("id", kpiID);
			InsightsKPIConfig executionRecord = createQuery.getSingleResult();
			session.delete(executionRecord);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<InsightsContentConfig> createQuery = session
					.createQuery("FROM InsightsContentConfig a WHERE a.contentId= :id", InsightsContentConfig.class);
			createQuery.setParameter("id", contentID);
			InsightsContentConfig executionRecord = createQuery.getSingleResult();
			session.delete(executionRecord);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<InsightsContentConfig> createQuery = session.createQuery(
					"FROM InsightsContentConfig a WHERE a.kpiConfig.kpiId= :id", InsightsContentConfig.class);
			createQuery.setParameter("id", kpiId);
			List<InsightsContentConfig> executionRecord = createQuery.getResultList();
			for (InsightsContentConfig insightsContentConfig : executionRecord) {
				session.delete(insightsContentConfig);
			}
			session.getTransaction().commit();
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

		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentReportTemplate> createQuery = session.createQuery(
					"FROM InsightsAssessmentReportTemplate a WHERE a.reportId= :id",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter("id", reportID);
			InsightsAssessmentReportTemplate executionRecord = createQuery.uniqueResult();
			if (executionRecord != null) {
				List<InsightsAssessmentConfiguration> resultList = getAssessmentConfigListByReportTemplateId(
						executionRecord.getReportId());
				if (resultList.isEmpty()) {
					session.beginTransaction();
					session.delete(executionRecord);
					session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentConfiguration> createQuery = session.createQuery(
					"FROM InsightsAssessmentConfiguration CE WHERE CE.reportTemplateEntity.reportId = :reportId ",
					InsightsAssessmentConfiguration.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.REPORTID, reportTemplateId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsReportsKPIConfig> createQuery = session.createQuery(
					"FROM InsightsReportsKPIConfig a WHERE a.reportTemplateEntity.reportId= :id",
					InsightsReportsKPIConfig.class);
			createQuery.setParameter("id", reportID);
			List<InsightsReportsKPIConfig> executionRecord = createQuery.getResultList();
			for (InsightsReportsKPIConfig record : executionRecord) {
				session.beginTransaction();
				record.setReportTemplateEntity(null);
				record.setKpiConfig(null);
				session.delete(record);
				session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.update(config);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsKPIConfig> createQuery = session.createQuery(
					"FROM InsightsKPIConfig IK WHERE IK.isActive = TRUE ORDER BY IK.kpiId desc ",
					InsightsKPIConfig.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsContentConfig> createQuery = session.createQuery(
					"FROM InsightsContentConfig IC WHERE IC.isActive = TRUE ORDER BY IC.contentId desc ",
					InsightsContentConfig.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentReportTemplate> query = session
					.createQuery("FROM InsightsAssessmentReportTemplate RE", InsightsAssessmentReportTemplate.class);
			return query.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsReportsKPIConfig> createQuery = session.createQuery(
					"FROM InsightsReportsKPIConfig a WHERE a.reportTemplateEntity.reportId= :reportId",
					InsightsReportsKPIConfig.class);
			createQuery.setParameter("reportId", reportId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsAssessmentReportTemplate> createQuery = session.createQuery(
					"FROM InsightsAssessmentReportTemplate RE where RE.templateName = :reportTemplateName",
					InsightsAssessmentReportTemplate.class);
			createQuery.setParameter("reportTemplateName", reportTemplateName);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsKPIConfig> createQuery = session
					.createQuery("FROM InsightsKPIConfig RE WHERE RE.usecase = :usecase", InsightsKPIConfig.class);
			createQuery.setParameter("usecase", usecase);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
