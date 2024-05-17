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

@Entity
@Table(name = "\"INSIGHTS_REPLICA_CONFIG\"")
public class InsightsReplicaConfig {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name = "replicaName", unique = true, nullable = false)
	private String replicaName;
	
	@Column(name = "replicaIP", nullable = false)
	private String replicaIP;
	
	@Column(name = "replicaEndpoint", unique = true, nullable = false)
	private String replicaEndpoint;

	public int getId() {
		return id;
	}

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public String getReplicaIP() {
		return replicaIP;
	}

	public void setReplicaIP(String replicaIP) {
		this.replicaIP = replicaIP;
	}

	public String getReplicaEndpoint() {
		return replicaEndpoint;
	}

	public void setReplicaEndpoint(String replicaEndpoint) {
		this.replicaEndpoint = replicaEndpoint;
	}

	
}
