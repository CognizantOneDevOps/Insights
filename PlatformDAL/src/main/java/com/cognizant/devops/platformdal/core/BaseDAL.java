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
package com.cognizant.devops.platformdal.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.Type;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;
import org.owasp.esapi.reference.DefaultSecurityConfiguration;

import com.cognizant.devops.platformdal.config.PlatformDALSessionFactoryProvider;

public class BaseDAL implements IBaseDAL {
	private static Logger logger = LogManager.getLogger(BaseDAL.class);
	private Session sessionObj;

	@Deprecated
	protected Session getSession() {
		if (sessionObj == null || !sessionObj.isOpen()) {
			sessionObj = PlatformDALSessionFactoryProvider.getSessionFactory().openSession();
		}
		return sessionObj;
	}

	/**This method used to get session object 
	 * @return a session object 
	 */
	private Session getSessionObj() {
		Session newsession;
		try {
			newsession = PlatformDALSessionFactoryProvider.getSessionFactory().openSession();
		} catch (HibernateException e) {
			logger.error("HibernateException occur during initializing session  :{} {}", e.getClass(), e.getMessage());
			throw e;
		}
		return newsession;
	}
	
	/**This method used to get session object 
	 * @return a session object 
	 */
	private Session getGrafanaSessionObj() {
		Session newsession;
		try {
			newsession = PlatformDALSessionFactoryProvider.getGrafanaSessionFactory().openSession();
		} catch (HibernateException e) {
			logger.error("HibernateException occur during initializing grafana session  :{} {}", e.getClass(), e.getMessage());
			throw e;
		}
		return newsession;
	}

	@Deprecated
	protected void terminateSession() {
		if (sessionObj != null) {
			sessionObj.close();
			sessionObj = null;
		}
	}

	protected void terminateSessionFactory() {
		terminateSession();
	}

	/** Method used to update Entity object 
	 * @param It required Hibernate Entity Object  
	 */
	@Override
	public void update(Object entityObj) {
		long starttime = System.nanoTime();
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.update(entityObj);
			session.getTransaction().commit();
			printHibernateStatistics(starttime, "UpdateData" + entityObj, 1);
		} catch (PersistenceException e) {
			printHibernateException(e, entityObj.toString());
			throw e;
		}
	}

	/** Method used to save Entity object 
	 * @param It required Hibernate Entity Object
	 * @return It return unique id created in system
	 *
	 */
	@Override
	public Object save(Object entityObj) {
		long starttime = System.nanoTime();
		Object returnIndentifire = null;
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			returnIndentifire = session.save(entityObj);
			session.getTransaction().commit();
			printHibernateStatistics(starttime, "SaveData" + entityObj, 1);
		} catch (PersistenceException e) {
			printHibernateException(e, entityObj);
			throw e;
		}
		return returnIndentifire;
	}

	/** Method used to saveOrupdate Entity object 
	 * @param It required Hibernate Entity Object
	 *
	 */
	@Override
	public void saveOrUpdate(Object entityObj) {
		long starttime = System.nanoTime();
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.saveOrUpdate(entityObj);
			session.getTransaction().commit();
			printHibernateStatistics(starttime, "saveOrUpdateData" + entityObj, 1);
		} catch (PersistenceException e) {
			printHibernateException(e, entityObj);
			throw e;
		}
	}
	
	@Override
	public int executeUpdate(String query, Map<String, Object> parameters) {
		long starttime = System.nanoTime();
		int recordUpdated = -1;
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query createQuery = session.createQuery(query);
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			recordUpdated = createQuery.executeUpdate();
			session.getTransaction().commit();
			printHibernateStatistics(starttime, query, recordUpdated);
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return recordUpdated;
	}

	/** Method used to delete Entity object 
	 * @param It required Hibernate Entity Object
	 *
	 */
	@Override
	public void delete(Object entityObj) {
		long starttime =System.nanoTime();
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.delete(entityObj);
			session.getTransaction().commit();
			printHibernateStatistics(starttime, "DeleteData" + entityObj, 1);
		} catch (PersistenceException e) {
			printHibernateException(e, entityObj);
			throw e;
		}
	}

	/** Used to get result list of entity from Hibernate 
	 * @param query Hibernate query to be executed 
	 * @param parameters list of parameter in key value pair, 
	 *	this will be use to set parameter placeholder in query if any 
	 *	@return List of entity object 
	 */
	@Override
	public <T> List<T> getResultList(String query, Class<T> type, Map<String, Object> parameters) {
		long starttime = System.nanoTime();
		List<T> returnList = null;
		try (Session session = getSessionObj()) {
			Query<T> createQuery = session.createQuery(query, type);
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			returnList = createQuery.getResultList();
			printHibernateStatistics(starttime, query, returnList.size());
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return returnList;

	}

	/** Used to get Unique result of entity from Hibernate 
	 * @param query Hibernate query to be executed 
	 * @param parameters list of parameter in key value pair, 
	 *	this will be use to set parameter placeholder in query if any 
	 *	@return Unique record of entity object 
	 *
	 */
	@Override
	public <T> T getUniqueResult(String query, Class<T> type, Map<String, Object> parameters) {
		long starttime = System.nanoTime();
		T returnResult = null;
		try (Session session = getSessionObj()) {
			Query<T> createQuery = session.createQuery(query, type);
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			returnResult = createQuery.uniqueResult();
			printHibernateStatistics(starttime, query, 1);
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return returnResult;
	}

	/** Used to get Single result of entity from Hibernate 
	 * @param query Hibernate query to be executed 
	 * @param parameters list of parameter in key value pair, 
	 *	this will be use to set parameter placeholder in query if any 
	 *	@return Single result of entity object 
	 *
	 */
	@Override
	public <T> T getSingleResult(String query, Class<T> type, Map<String, Object> parameters) {
		long starttime = System.nanoTime();
		T returnResult = null;
		try (Session session = getSessionObj()) {
			Query<T> createQuery = session.createQuery(query, type);
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			returnResult = createQuery.getSingleResult();
			printHibernateStatistics(starttime, query, 1);
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return returnResult;
	}

	/** Used to get result list of entity from Hibernate based on extra parameter like MaxResult, Parameter list  
	 * @param query Hibernate query to be executed 
	 * @param parameters list of parameter in key value pair, 
	 *	this will be use to set parameter placeholder in query if any 
	 *	@return List of entity object 
	 *
	 */
	@Override
	public <T> List<T> executeQueryWithExtraParameter(String query, Class<T> type, Map<String, Object> parameters,
			Map<String, Object> extraParameters) {
		long starttime = System.nanoTime();
		Query<T> createQuery = null;
		List<T> returnList = null;
		try (Session session = getSessionObj()) {
			createQuery = session.createQuery(query, type);
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			for (Map.Entry<String, Object> parameter : extraParameters.entrySet()) {
				if (parameter.getKey().equalsIgnoreCase("MaxResults")) {
					createQuery.setMaxResults((int) parameter.getValue());
				} else if (parameter.getValue() instanceof Collection) {
					createQuery.setParameterList(parameter.getKey(), (List) parameter.getValue());
				}
			}
			returnList = createQuery.getResultList();
			printHibernateStatistics(starttime, query, returnList.size());
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return returnList;
	}

	/** Used to get unique result of entity from Hibernate based on extra parameter like MaxResult, Parameter list  
	 * @param query Hibernate query to be executed 
	 * @param parameters list of parameter in key value pair, 
	 *	this will be use to set parameter placeholder in query if any 
	 *	@return unique result of entity object 
	 *
	 */
	@Override
	public <T> Object executeUniqueResultQueryWithExtraParameter(String query, Class<T> type,
			Map<String, Object> parameters, Map<String, Object> extraParameters) {
		long starttime = System.nanoTime();
		Query<T> createQuery = null;
		Object returnResult = null;
		try (Session session = getSessionObj()) {
			createQuery = session.createQuery(query, type);
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			for (Map.Entry<String, Object> parameter : extraParameters.entrySet()) {
				if (parameter.getKey().equalsIgnoreCase("MaxResults")) {
					createQuery.setMaxResults((int) parameter.getValue());
				}
			}
			returnResult = createQuery.getSingleResult();
			printHibernateStatistics(starttime, query, 1);
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return returnResult;
	}

	/** Used to get result list of entity from Hibernate based Native query   
	 * @param query SQL/native query to be executed 
	 * @param sclarValues Map of query return Scalar value with Hibernate datatype
	 * @param parameters list of parameter in key value pair, 
	 *	this will be use to set parameter placeholder in query if any 
	 *	@return List of Object Array object
	 *
	 */
	@Override
	public <T> List<T> executeSQLQueryAndRetunList(String query, Map<String, Type> sclarValues,
			Map<String, Object> parameters) {
		long starttime = System.nanoTime();
		NativeQuery createQuery = null;
		List<T> returnList = null;
		try (Session session = getSessionObj()) {
			createQuery = session.createNativeQuery(query);
			for (Map.Entry<String, Type> parameter : sclarValues.entrySet()) {
				createQuery.addScalar(parameter.getKey(), parameter.getValue());
			}
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			returnList = createQuery.getResultList();
			printHibernateStatistics(starttime, query, returnList.size());
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return returnList;
	}

	/** used to update DB record based on SQL native query 
	 *	@param createQuery native/SQL query
	 *	@return the number of entities updated or deleted 
	 */
	@Override
	public int executeUpdateWithSQLQuery(String createQuery) {
		long starttime = System.nanoTime();
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			NativeQuery createSQLQuery = session.createNativeQuery(createQuery);
			int recordUpdated = createSQLQuery.executeUpdate();
			session.getTransaction().commit();
			printHibernateStatistics(starttime, createQuery, recordUpdated);
			return recordUpdated;
		} catch (PersistenceException e) {
			printHibernateException(e, createQuery);
			throw e;
		}
	}
	
	/** Used to get result list of entity from Hibernate based Native query   
	 * @param query SQL/native query to be executed 
	 * @param sclarValues Map of query return Scalar value with Hibernate datatype
	 * @param parameters list of parameter in key value pair, 
	 *	this will be use to set parameter placeholder in query if any 
	 * @return 
	 *	@return List of Object Array object
	 *
	 */
	@Override
	public <T> List<T> executeGrafanaSQLQueryAndRetunList(String query, Map<String, Type> scalarValues,
			Map<String, Object> parameters) {
		long starttime = System.nanoTime();
		NativeQuery createQuery = null;
		List<T> returnList = null;
		try (Session session = getGrafanaSessionObj()) {
			createQuery = session.createNativeQuery(query);
			for (Map.Entry<String, Type> parameter : scalarValues.entrySet()) {
				createQuery.addScalar(parameter.getKey(), parameter.getValue());
			}
			for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
				createQuery.setParameter(parameter.getKey(), parameter.getValue());
			}
			returnList = createQuery.getResultList();
			printHibernateStatistics(starttime, query, returnList.size());
		} catch (PersistenceException e) {
			printHibernateException(e, query);
			throw e;
		}
		return returnList;
	}

	/** used to log PersistenceException in perticular format 
	 * @param e
	 * @param sourceProperty
	 */
	private void printHibernateException(PersistenceException e, Object sourceProperty) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTrace = stacktrace[3];
		StackTraceElement stacktraceService = stacktrace[4];
		
		logger.error(e);
		logger.error(
				"Type=HibernateException className={} methodName={} lineNo={} serviceFileName={} serviceMethodName={} serviceFileLineNo={} "
						+ "exceptionClass={} sourceProperty={} message={} ",
				stackTrace.getFileName(), stackTrace.getMethodName(), stackTrace.getLineNumber(),stacktraceService.getFileName(),stacktraceService.getMethodName(),stacktraceService.getLineNumber(), e.getClass(),
				sourceProperty, e.getMessage());
	}

	/** used to logs HibernateStatistics in perticulat format
	 * @param startTime
	 * @param sourceProperty
	 * @param rowCount
	 */
	private void printHibernateStatistics(long startTime, Object sourceProperty, int rowCount) {
		long endTime=System.nanoTime();
		long processingTime = TimeUnit.MILLISECONDS.convert( endTime - startTime, TimeUnit.NANOSECONDS );
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTrace = stacktrace[3];
		StackTraceElement stacktraceService = stacktrace[4];
		
		logger.debug(
				"Type=HibernateStatistics className={} methodName={} lineNo={} serviceFileName={} serviceMethodName={} serviceFileLineNo={} "
						+ "processingTime={} sourceProperty={} rows={} ",
				stackTrace.getFileName(), stackTrace.getMethodName(), stackTrace.getLineNumber(),stacktraceService.getFileName(),stacktraceService.getMethodName(),stacktraceService.getLineNumber(), processingTime,
				sourceProperty, rowCount);
	}
	
	/** use to create ESAPI validator object 
	 * @param startTime
	 * @param sourceProperty
	 * @param rowCount
	 */
	public Validator getESAPIValidator() {
		Properties esapiProps = new Properties();
		try {
			esapiProps.load(getClass().getClassLoader().getResourceAsStream("ESAPI.properties"));

		} catch (Exception e) {
			logger.error(e);
		}
		ESAPI.override(new DefaultSecurityConfiguration(esapiProps));
		Validator validate = ESAPI.validator();
		return validate;
	}

}
