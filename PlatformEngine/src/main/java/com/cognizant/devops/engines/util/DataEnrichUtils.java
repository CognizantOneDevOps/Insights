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
package com.cognizant.devops.engines.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DataEnrichUtils {
	
	private static Pattern p = Pattern.compile("((?<!([A-Z]{1,10})-?)(#|)+[A-Z]+(-|.)\\d{1,10})");
	
	private DataEnrichUtils() {
	}
	
	public static String dataExtractor(String message, String keyPattern) {
		   String enrichDataValue = null;
		    if (message != null&& keyPattern != null && !keyPattern.isEmpty()) {
		        Matcher m = p.matcher(message);
		        StringBuffer sb = new StringBuffer();
		        
		        while (m.find()) {
		            enrichDataValue = m.group();
		            m.appendReplacement(sb, "");
		        }
		        
		        m.appendTail(sb);
		        message = sb.toString();
		    }
		    return enrichDataValue;
		}
}