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
import java.util.ArrayList;
import java.util.List;

public class LDAPAttributes implements Serializable{
	private String name;
	private String surname; 
	private String memberOf;
	private String email;
	private String username;
	
	public String[] getAttributeList(){
		List<String> attributes = new ArrayList<String>();
		if(name != null && name.trim().length() > 0){
			attributes.add(name);
		}
		if(surname != null && surname.trim().length() > 0){
			attributes.add(surname);
		}
		if(memberOf != null && memberOf.trim().length() > 0){
			attributes.add(memberOf);
		}
		if(email != null && email.trim().length() > 0){
			attributes.add(email);
		}
		if(username != null && username.trim().length() > 0){
			attributes.add(username);
		}
		String[] s = {};
		return attributes.toArray(s);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getMemberOf() {
		return memberOf;
	}
	public void setMemberOf(String memberOf) {
		this.memberOf = memberOf;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
