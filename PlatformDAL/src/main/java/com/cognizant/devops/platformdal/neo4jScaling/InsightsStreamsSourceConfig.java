/*******************************************************************************
* Copyright 2024 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.neo4jScaling;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This entity is used to store Kafka neo4j configuration
 */
@Entity
@Table(name = "\"INSIGHTS_STREAMS_SOURCE_CONFIG\"")
public class InsightsStreamsSourceConfig {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "configID", unique = true, nullable = false)
	private String configID;

	@Column(name = "kafkaEndpoint", unique = true, nullable = false)
	private String kafkaEndpoint;

	@Column(name = "sourceIP", nullable = false)
	private String sourceIP;

	@Column(name = "topicName", unique = true, nullable = false)
	private String topicName;

	@Column(name = "nodeLabels", nullable = false)
	private String nodeLabels;

	@Column(name = "relationshipLabels", nullable = false)
	private String relationshipLabels;

	public int getId() {
		return id;
	}

	public String getKafkaEndpoint() {
		return kafkaEndpoint;
	}

	public void setKafkaEndpoint(String kafkaEndpoint) {
		this.kafkaEndpoint = kafkaEndpoint;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getNodeLabels() {
		return nodeLabels;
	}

	public void setNodeLabels(String nodeLabels) {
		this.nodeLabels = nodeLabels;
	}

	public String getRelationshipLabels() {
		return relationshipLabels;
	}

	public void setRelationshipLabels(String relationshipLabels) {
		this.relationshipLabels = relationshipLabels;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public void setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
	}

	public String getConfigID() {
		return configID;
	}

	public void setConfigID(String configID) {
		this.configID = configID;
	}

}
