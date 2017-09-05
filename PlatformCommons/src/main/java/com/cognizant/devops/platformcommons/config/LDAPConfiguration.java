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
package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

public class LDAPConfiguration implements Serializable{
	private String bindDN;;
	private String bindPassword;
	private String searchBaseDN;
	private String searchFilter;
	private LDAPAttributes ldapAttributes;
	private String ldapUrl;
	
	public String getBindDN() {
		return bindDN;
	}
	public void setBindDN(String bindDN) {
		this.bindDN = bindDN;
	}
	public String getBindPassword() {
		return bindPassword;
	}
	public void setBindPassword(String bindPassword) {
		this.bindPassword = bindPassword;
	}
	public String getSearchBaseDN() {
		return searchBaseDN;
	}
	public void setSearchBaseDN(String searchBaseDN) {
		this.searchBaseDN = searchBaseDN;
	}
	public String getSearchFilter() {
		return searchFilter;
	}
	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}
	public LDAPAttributes getLdapAttributes() {
		return ldapAttributes;
	}
	public void setLdapAttributes(LDAPAttributes ldapAttributes) {
		this.ldapAttributes = ldapAttributes;
	}
	public String getLdapUrl() {
		return ldapUrl;
	}
	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}
}
