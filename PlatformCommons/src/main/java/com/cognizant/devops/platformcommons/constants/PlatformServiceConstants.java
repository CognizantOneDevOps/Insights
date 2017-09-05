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
package com.cognizant.devops.platformcommons.constants;

import java.util.Base64;

public interface PlatformServiceConstants {
	String STATUS = "status";
	String SUCCESS = "success";
	String FAILURE = "failure";
	String MESSAGE = "message";
	String DATA = "data";
	
	String TRANSFORMATION_DECODED = new String(Base64.getDecoder().decode(ConfigOptions.TRANSFORMATION_ENCODED.getBytes()));
	String SP_DECODED = new String(Base64.getDecoder().decode(ConfigOptions.SP_ENCODED.getBytes()));
	String RSA_DECODED = new String(Base64.getDecoder().decode(ConfigOptions.RSA_ENCODED.getBytes()));
}
