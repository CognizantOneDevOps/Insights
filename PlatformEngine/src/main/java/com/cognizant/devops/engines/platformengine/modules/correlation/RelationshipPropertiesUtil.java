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
package com.cognizant.devops.engines.platformengine.modules.correlation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RelationshipPropertiesUtil {
	
	public static String calSUM(String operationJson) {
		JsonParser operationParser = new JsonParser();
		JsonObject operationObject = (JsonObject) operationParser.parse(operationJson);
		String operandOne = operationObject.get("OperandOne").getAsString();
		String operandTwo = operationObject.get("OperandTwo").getAsString();
		return "abs((" + "source." + operandOne + "+" + "destination." + operandTwo + "))";
	}

	public static String calDiff(String operationJson) {
		JsonParser operationParser = new JsonParser();
		JsonObject operationObject = (JsonObject) operationParser.parse(operationJson);
		String operandOne = operationObject.get("OperandOne").getAsString();
		String operandTwo = operationObject.get("OperandTwo").getAsString();
		return "abs((" + "source." + operandOne + "-" + "destination." + operandTwo + "))";
	}


}
