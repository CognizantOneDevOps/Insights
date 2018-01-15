package com.cognizant.devops.platformservice.security.config;

import org.apache.log4j.Logger;

public class SpringAuthorityUtil {
	
	private static Logger log = Logger.getLogger(SpringAuthorityUtil.class.getName());
	
	public static SpringAuthority getSpringAuthorityRole(String grafanaCurrentOrgRole) {
		try{
			return SpringAuthority.valueOf(grafanaCurrentOrgRole.replaceAll("\\s", "_"));
		}catch (Exception e) {
			log.error("Unable to find grafana role in Spring Authority.", e);
		}
		return SpringAuthority.valueOf("Viewer");
	}

}
