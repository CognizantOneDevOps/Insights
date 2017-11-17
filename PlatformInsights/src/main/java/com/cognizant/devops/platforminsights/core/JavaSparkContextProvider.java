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
package com.cognizant.devops.platforminsights.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import com.cognizant.devops.platforminsights.configs.ConfigConstants;

public class JavaSparkContextProvider {
	private static final JavaSparkContext s_sparkContext = buildContext();
	
	public static JavaSparkContext getJavaSparkContext(){
		return s_sparkContext;
	}
	
	private static JavaSparkContext buildContext(){
		//load these configurations from the server-config.json
		SparkConf conf = new SparkConf()
				.setAppName(ConfigConstants.APP_NAME)
				.setMaster(ConfigConstants.MASTER)
				.set("spark.executor.memory", ConfigConstants.SPARK_EXECUTOR_MEMORY)
				.set("es.nodes", ConfigConstants.SPARK_ES_HOST)
				.set("es.port", ConfigConstants.SPARK_ES_PORT)
				.set("es.nodes.wan.only", "true");
				//.set("spark.driver.extraClassPath", "/home/s463188/downloads/elasticsearch-spark-20_2.11-5.4.0.jar")
				//.set("spark.driver.extraClassPath", "D:\\VishalGanjare\\DevOpsPlatform\\CodeRepo\\BitBucket\\DevOpsPlatformRepo\\devopsplatformlicense\\PlatformInsights\\target\\classes");//Take this into the sprak setting
		return new JavaSparkContext(conf);
	}
	
	public static void terminateContext(){
		//context.cancelAllJobs();
		s_sparkContext.stop();
		s_sparkContext.close();
	}
}
