/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.serverConfig;

import java.util.UUID;

import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.google.gson.JsonObject;

public class ServerConfigTestData {
	
	public String extractServerConfig(JsonObject serverConfigStatus) {
		String serverConfigStr = serverConfigStatus.get("data").getAsString();
		String serverConfigJsonDecrypt = AES256Cryptor.decryptWeb(serverConfigStr);
		return serverConfigJsonDecrypt;
	}

	public String encryptServerConfig(String serverConfigNormal) {
		String passKey = UUID.randomUUID().toString().substring(0, 15);
		String serverConfigJsonEncrypt = AES256Cryptor.encryptWeb(passKey, serverConfigNormal);
		return serverConfigJsonEncrypt;
	}
}
