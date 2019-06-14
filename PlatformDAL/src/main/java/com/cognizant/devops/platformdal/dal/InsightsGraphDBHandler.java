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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

public class InsightsGraphDBHandler implements BaseGraphDBHandler {
	private static final Logger log = LogManager.getLogger(InsightsGraphDBHandler.class);


	public InsightsGraphDBHandler() {
	}
	
	@Override
	public List<InsightsGraphNode> getNodes(String query) throws DataDeleteException {

		StatementResult results = read(query);
		return getNodes(results);
	}

	@Override
	public List<InsightsGraphNode> getNodeWithRelationship(String query) throws DataDeleteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void write(final String query) throws DataDeleteException {
		
		deleteDetachKeywordValidation(query);
		
		try ( Session session = GraphDBConnection.getInstance().getDriver().session() )
        {
            session.writeTransaction( new TransactionWork<Integer>()
            {
                @Override
                public Integer execute( Transaction tx )
                {
                    return createNode( tx, query );
                }
            } );
        }
	}
	
	@Override
	public void writeBulk(final List<String> queries) throws DataDeleteException {
		
		validateDeleteDetach(queries);
		try ( Session session = GraphDBConnection.getInstance().getDriver().session() )
        {
            session.writeTransaction( new TransactionWork<Integer>()
            {
                @Override
                public Integer execute( Transaction tx )
                {
                    return createBulkNodes( tx, queries );
                }
            } );
        }
		
	}
	
	@Override
	public StatementResult read(final String query)  throws DataDeleteException {
		deleteDetachKeywordValidation(query);
		try ( Session session = GraphDBConnection.getInstance().getDriver().session() )
        {
			StatementResult records = session.readTransaction( new TransactionWork<StatementResult>()
            {
                @Override
                public StatementResult execute( Transaction tx )
                {
                    return runQuery( tx,query );
                }
            } );
			
			return records;
        }
	}
	
	@Override
	public void delete(final String query) {
		try ( Session session = GraphDBConnection.getInstance().getDriver().session() )
        {
            session.writeTransaction( new TransactionWork<Integer>()
            {
                @Override
                public Integer execute( Transaction tx )
                {
                    return deleteNode( tx,query );
                }
            } );
        }
	}

	@Override
	public StatementResult execute(Transaction tx,String query) {
		return runQuery(tx,query);
	}
	
	@Override
	public Session getSession() {
		return GraphDBConnection.getInstance().getDriver().session();
	}
	
	@Override
	public void closeSession(Session session) {
		if(session != null && session.isOpen()) {
			session.close();
		}
	}
	
   private Integer createNode( Transaction tx,String query )
   {
	   runQuery(tx,query);
       tx.success();
       return 1;
   }
   
   private Integer deleteNode( Transaction tx,String query )
   {
	   runQuery(tx,query);
       tx.success();
       return 1;
   }
   
   private Integer createBulkNodes(Transaction tx, List<String> queries) {
		
		Iterator<String> queryItr = queries.iterator();
		
		while(queryItr.hasNext()) {
			runQuery(tx,queryItr.next());
		}
		tx.success();
		return 1;
	}
   
   private StatementResult runQuery(Transaction tx,String query) {
	   return tx.run( query );
   }
   
   private void validateDeleteDetach(List<String> queries) throws DataDeleteException {
   		Iterator<String> queryItr = queries.iterator();
		
		while(queryItr.hasNext()) {
			deleteDetachKeywordValidation(queryItr.next());;
		}
   }
   	
	private void deleteDetachKeywordValidation(String query) throws DataDeleteException {
		
		boolean flag = query.contains("delete") || query.contains("detach");

	    if (flag)
	    {
	    	throw new DataDeleteException("Node deletion not allowed through this process");
	    }
	}
	
	private List<InsightsGraphNode> getNodes(StatementResult result){
		List<InsightsGraphNode> insightNodes = new ArrayList<>();
		
		while(result.hasNext()) {
			Record record = result.next();
			Iterable<String> keys = record.keys();
			log.debug(keys);
			Iterator<String> keyItr = keys.iterator();
			Node node =  null;
			Relationship relation = null;
			InsightsGraphNode graphNode = new InsightsGraphNode();
			InsightsRelationShip nodeRelationShip = null;
			while(keyItr.hasNext()) {
				String key = keyItr.next();
				Object o = record.get(key);
				if(o instanceof NodeValue) {
					node = ((NodeValue)o).asNode();
					graphNode = new InsightsGraphNode();
					
					Iterable<String> nodeLabel = node.labels(); //.iterator()
					List<String> labelList =(List<String>) StreamSupport
							.stream(nodeLabel.spliterator(), false)
							.collect(Collectors.toList());
					
					log.debug(" labelList ==== "+labelList.toString()); 				
					graphNode.setLabels(labelList);
					graphNode.setPropertyMap(node.asMap());
					
					if(relation != null && node.id() == relation.startNodeId()) {
						graphNode.setRelation(nodeRelationShip);
						if(node != null && node.id() == relation.startNodeId()) {
							if(graphNode != null) {
								graphNode.setRelation(nodeRelationShip);
							}
							nodeRelationShip.setStartNode(graphNode);
						} else if (node != null && node.id() == relation.endNodeId()) {
							if(graphNode != null) {
								graphNode.setRelation(nodeRelationShip);
							}
							nodeRelationShip.setEndNode(graphNode);
						}
					}
					
				} else if (o instanceof RelationshipValue) {
					relation = ((RelationshipValue)o).asRelationship();
					
					nodeRelationShip = new InsightsRelationShip();
					nodeRelationShip.setPropertyMap(relation.asMap());
					nodeRelationShip.setName(relation.type());
					
					if(node != null && node.id() == relation.startNodeId()) {
						if(graphNode != null) {
							graphNode.setRelation(nodeRelationShip);
						}
						nodeRelationShip.setStartNode(graphNode);
					} else if (node != null && node.id() == relation.endNodeId()) {
						if(graphNode != null) {
							graphNode.setRelation(nodeRelationShip);
						}
						nodeRelationShip.setEndNode(graphNode);
					}
				}
				
			}
		}
		
		return insightNodes;
	}
}
