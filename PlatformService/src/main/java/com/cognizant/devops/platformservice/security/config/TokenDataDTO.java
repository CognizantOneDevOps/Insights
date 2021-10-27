/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.security.config;

import java.io.Serializable;
import java.util.Date;

public class TokenDataDTO implements Serializable {

	private static final long serialVersionUID = -11994006968623559L;

	private String tokenValue;
	private Date sessionTime;
		
	public TokenDataDTO(String tokenValue, Date sessionTime) {
		super();
		this.tokenValue = tokenValue;
		this.sessionTime = sessionTime;
	}
	
	public String getTokenValue() {
		return tokenValue;
	}
	
	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}
	
	public Date getSessionTime() {
		return sessionTime;
	}
	
	public void setSessionTime(Date sessionTime) {
		this.sessionTime = sessionTime;
	}
	
	@Override
	public String toString() {
		return "TokenDataDTO [sessionTime=" + sessionTime  + ", tokenValue= " +  tokenValue + "]";
	}
}
