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

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public interface BaseGraphDBHandler{

    public StatementResult read(String query) throws DataDeleteException;
    public List<InsightsGraphNode> getNodes(String query) throws DataDeleteException;
    public List<InsightsGraphNode> getNodeWithRelationship(String query) throws DataDeleteException;
    public void write(String query) throws DataDeleteException;
    public void writeBulk(List<String> queries) throws DataDeleteException;
    public StatementResult execute(Transaction tx,String query);	
    public void delete(String query);
    public Session getSession();
    public void closeSession(Session session);
}
