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
package com.cognizant.devops.platforminsights.core.job.config;

import java.util.Map;

// import org.apache.spark.api.java.function.Function;

import com.google.gson.Gson;

// import scala.Tuple2;

public class SparkJobMapFunction { //implements Function<Tuple2<String,Map<String,Object>>, SparkJobConfiguration>
	/**
	 * 
	 */
	private static final long serialVersionUID = -6572694321006877098L;

	/*@Override
	public SparkJobConfiguration call(Tuple2<String, Map<String, Object>> v1) throws Exception {
		Map<String, Object> data = v1._2;
		Gson gson = new Gson();
		SparkJobConfiguration model = gson.fromJson(gson.toJson(data), SparkJobConfiguration.class);
		model.setId(v1._1);
		return model;
	}*/
}
