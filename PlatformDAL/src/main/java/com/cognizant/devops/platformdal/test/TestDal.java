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
package com.cognizant.devops.platformdal.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

public class TestDal {
	final static Logger logger = LogManager.getLogger(TestDal.class);
	public static void main(String[] args) {
		/*Configuration configuration = new Configuration();
		configuration.configure("hibernate.cfg.xml");
		configuration.setProperty("hibernate.connection.username","grafana123");
		ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).configure().build();*/
		ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
		MetadataSources sources = new MetadataSources( standardRegistry );
		sources.addAnnotatedClass( Test.class );
		Metadata metadata = sources.getMetadataBuilder().applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE).build();
		SessionFactory sessionFactory = metadata.buildSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Test s = new Test();
		s.setName("12Vishal123");
		session.save(s);
		session.getTransaction().commit();
		session.close();
		sessionFactory.close();
	}
}
