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
package com.cognizant.devops.platformengine.test.subscriber;

import java.util.List;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformengine.message.factory.MessagePublisherFactory;
import com.cognizant.devops.platformengine.modules.aggregator.EngineAggregatorModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import junit.framework.TestCase;

/**
 * @author 146414
 *
 */
public class SubscriberTest {
	

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	/*
	protected void setUp() throws Exception {
		super.setUp();
		ApplicationConfigCache.loadConfigCache();
	}
	*/

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	/*
	protected void tearDown() throws Exception {
		super.tearDown();		
	}
    */
	/**
	 * Test method for {@link com.cognizant.devops.platformengine.message.subscriber.AgentDataSubscriber#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])}.
	 */
	/*
	public final void testHandleDeliveryStringEnvelopeBasicPropertiesByteArray() {
		ApplicationConfigCache.loadConfigCache();
		Neo4jDBHandler neo4jDBHandler = new Neo4jDBHandler();
		try {
			neo4jDBHandler.executeCypherQuery("Match (n:CONFIG:TEST) delete n");
			EngineAggregatorModule engineAggregatorModule = new EngineAggregatorModule();
			engineAggregatorModule.execute(null);
			neo4jDBHandler.executeCypherQuery("create (n:CONFIG:SCM:GIT:TEST { functionalBlock: 'SCM', toolName: 'GIT', routingKeys: '[\"TEST_SCM.TEST_GIT.DATA\",\"TEST_SCM.TEST_GIT.HEALTH\"]'}) return n");
			engineAggregatorModule.execute(null);
			
			JsonArray dataArray = new JsonArray();
			JsonObject data = new JsonObject();
			dataArray.add(data);
			data.addProperty("testDataKey", "testDataValue");
			MessagePublisherFactory.publish("TEST_SCM.TEST_GIT.DATA", dataArray);
			Thread.sleep(5000);
			List<NodeData> result = neo4jDBHandler.executeCypherQuery("Match (n:TEST_SCM:TEST_GIT) return n").getNodes();
			assertEquals("Incorrect Data inserted by subscriber", 1, result.size());
			neo4jDBHandler.executeCypherQuery("Match (n:CONFIG:TEST) delete n");
			neo4jDBHandler.executeCypherQuery("Match (n:TEST_SCM:TEST_GIT) delete n");
			
			engineAggregatorModule.deregisterAggregator("TEST_SCM.TEST_GIT.DATA");
			MessagePublisherFactory.publish("TEST_SCM.TEST_GIT.DATA", dataArray);
			Thread.sleep(5000);
			result = neo4jDBHandler.executeCypherQuery("Match (n:TEST_SCM:TEST_GIT) return n").getNodes();
			assertEquals("Publisher is not removed properly", 0, result.size());
			
			neo4jDBHandler.executeCypherQuery("Match (n:CONFIG:TEST) delete n");
			neo4jDBHandler.executeCypherQuery("Match (n:TEST_SCM:TEST_GIT) delete n");			
		} catch (GraphDBException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
}
