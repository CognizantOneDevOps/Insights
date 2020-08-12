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

import org.hibernate.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.config.PlatformDALSessionFactoryProvider;

public class BaseDAL {
	//private static Logger logger = LogManager.getLogger(BaseDAL.class);
	private static Logger logger = LogManager.getLogger(BaseDAL.class);
	private Session session;

	protected Session getSession(){
		if(session == null || !session.isOpen()){
			session = PlatformDALSessionFactoryProvider.getSessionFactory().openSession();
		}
		return session;
	}
	
	protected void terminateSession(){
		if(session != null){
			session.close();
			session = null;
		}
	}
	
	protected void terminateSessionFactory(){
		terminateSession();
	}
}
