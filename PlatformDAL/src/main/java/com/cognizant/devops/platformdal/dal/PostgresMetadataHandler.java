package com.cognizant.devops.platformdal.dal;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class PostgresMetadataHandler extends BaseDAL {
	
	public String getPostgresDBVersion() {
		Object result =null;
		try {
			String query="Select version()";
			result = getSession().createNativeQuery(query).getSingleResult();
			terminateSession();
		} catch (Exception e) {
			terminateSession();
		}
		return (String) result;
	}

}
