/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.dal;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.v1.StatementResult;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;

public class InsightsDaoFactory implements InsightsDaoFactoryInterface {
	private static final InsightsDaoFactoryInterface insightsDaoFactory = new InsightsDaoFactory();
	
	private InsightsDaoFactory() {
	}
	
	public static InsightsDaoFactoryInterface getInstance() {
		return insightsDaoFactory;
	}
	
	@Override
	public GraphDBHandler getNeo4jRESTDBHandler() {
		return new GraphDBHandler();
	}

	@Override
	public BaseGraphDBHandler getNeo4jBoltDBHandler() {
		return new InsightsGraphDBHandler();
	}
	
	public static void main(String [] args) {
		ApplicationConfigCache.loadConfigCache();
		//System.out.println("Testing start....");
		try (GraphDBConnection connection = GraphDBConnection.getInstance()){
			BaseGraphDBHandler dbHandler = getInstance().getNeo4jBoltDBHandler();
			//dbHandler.write("CREATE (n:NEWDALTEST) return n");
			StatementResult st =  dbHandler.read("MATCH (n:GIT:DATA) return n LIMIT 10");
			List<InsightsGraphNode> node =  dbHandler.getNodes("MATCH (n:GIT:DATA) return n LIMIT 10");
			//System.out.println("Testing end....");
			//dbHandler.write("MATCH (n:NEWDALTEST) delete n");
			//dbHandler.close();
		} catch (DataDeleteException e) {
		}catch (Exception e1) {
		}
		/*
		List<String> queries = new ArrayList<>();
		
		queries.add("CREATE (n:NEWDALTEST) return n");
		queries.add("CREATE (n:NEWDALTEST) return n");
		queries.add("CREATE (n:NEWDALTEST) return n");
		queries.add("CREATE (n:NEWDALTEST) return n");
		queries.add("CREATE (n:NEWDALTEST) return n");
		queries.add("CREATE (n:NEWDALTEST) return n");
		
		dbHandler.writeBulk(queries);
		
		dbHandler.read("MATCH (n:NEWDALTEST) return n");
		
		
		queries = new ArrayList<>();
		
		queries.add("CREATE (m:NEWDALTEST1) return m");
		queries.add("CREATE (m:NEWDALTEST1) return m");
		queries.add("CREATE (m:NEWDALTEST1) return m");
		queries.add("CREATE (m:NEWDALTEST1) return m");
		queries.add("CREATE (m:NEWDALTEST1) return m");
		queries.add("CREATE (m:NEWDALTEST1) return m");
		
		dbHandler.writeBulk(queries);
		
		dbHandler.write("MATCH (n:NEWDALTEST) MATCH (m:NEWDALTEST1) WITH n,m CREATE (n)-[r:KNOWS]->(m) return n,m,r");
		
		dbHandler.read("MATCH (n:NEWDALTEST) return n");
		
		dbHandler.read("MATCH (n) DETACH DELETE n");*/ 
		
		//System.exit(0);
	}

}
