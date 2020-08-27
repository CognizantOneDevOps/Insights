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
package com.cognizant.devops.platformservice.properties.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropertyValues{

	String result = "";
	InputStream inputStream;
 
	public Properties getPropValues() throws IOException 
	{
		Properties prop = null;
 
		try {
			prop = new Properties();
			String propFileName = "version.properties";
 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null)
			{
				prop.load(inputStream);
			}  
			else 
			{
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}

			
		} 
		catch (Exception e)
		{
			//System.out.println("Exception: " + e);
		}
		finally 
		{
			inputStream.close();
		}
		return prop;
	}
}
