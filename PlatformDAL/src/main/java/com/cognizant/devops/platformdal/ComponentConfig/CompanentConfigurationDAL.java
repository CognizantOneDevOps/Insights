package com.cognizant.devops.platformdal.ComponentConfig;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class CompanentConfigurationDAL extends BaseDAL {
	public static Logger log = Logger.getLogger(CompanentConfigurationDAL.class); 

	public List<ComponentConfiguration> getComponentConfigurations(String componentType) {
		Query<ComponentConfiguration> getQuery = getSession().createQuery(
				"FROM ComponentConfiguration CC WHERE CC.componentType = :componentType",
				ComponentConfiguration.class);
		getQuery.setParameter("componentType", componentType);
		List<ComponentConfiguration> result = getQuery.getResultList();
		result.forEach(r -> log.info(r));
		terminateSession();
		terminateSessionFactory();
		return result;
	}
	
}
