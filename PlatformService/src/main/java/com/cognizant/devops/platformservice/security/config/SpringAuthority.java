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
package com.cognizant.devops.platformservice.security.config;

import org.springframework.security.core.GrantedAuthority;

public enum SpringAuthority implements GrantedAuthority {
	
	Viewer("Viewer"),
    Editor("Editor"),
    Admin("Admin"),
    Read_Only_Editor("Read Only Editor"),
    INVALID("INVALID");
 
    private String name;
 
    SpringAuthority(String name) {
        this.name = name;
    }
 
	/**
	 * used to get valid Authority
	 */
    @Override
	public String getAuthority() {
		return this.name;
	}

	
}
