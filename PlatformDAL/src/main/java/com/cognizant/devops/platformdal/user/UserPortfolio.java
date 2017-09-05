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
package com.cognizant.devops.platformdal.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * user_portfolio table will have the mapping between grafana user id and portfolio
 * e.g. user with grafana id 1 might be RELEASE_MANAGER for org 1 and QA_MANAGER for org 2
 * e.g. user with grafana id 2 might be RELEASE_MANAGER for all orgs
 */

@Entity
@Table (name="user_portfolio")
public class UserPortfolio {
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@Column(name ="org_id")
	private int orgId; //0 represents all orgs
	
	@Column(name ="user_id")
	private int userId; //Grafana user ID
	
	@Column(name ="portfolio")
	@Enumerated(EnumType.ORDINAL) 
	private UserPortfolioEnum portfolio;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrgId() {
		return orgId;
	}

	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public UserPortfolioEnum getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(UserPortfolioEnum portfolio) {
		this.portfolio = portfolio;
	}
}
