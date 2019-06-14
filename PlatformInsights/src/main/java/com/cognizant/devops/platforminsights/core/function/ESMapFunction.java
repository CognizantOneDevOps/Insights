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
package com.cognizant.devops.platforminsights.core.function;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// import org.apache.spark.api.java.function.Function;

import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;

// import scala.Tuple2;

public class ESMapFunction { //implements Function<Tuple2<String, Map<String, Object>>, Long>
	/**
	 * 
	 */
	private static final long serialVersionUID = -1131195930697674868L;
	
	private String startTimeField;
	private String endTimeField;
	private String timeFormat;
	private String avgField;
	private transient DateTimeFormatter sdf;
	
	public ESMapFunction(KPIDefinition def){
		if(def.getStartTimeField() != null){
			this.startTimeField = def.getStartTimeField();
		}
		if(def.getEndTimeField() != null){
			this.endTimeField = def.getEndTimeField();
		}
		if(def.getTimeFormat() != null){
			this.timeFormat = def.getTimeFormat();
		}
		if(def.getAverageField() != null){
			this.avgField = def.getAverageField();
		}
		if(timeFormat != null){
			sdf = DateTimeFormatter.ofPattern ( timeFormat );
		}
	}

	/*	@Override
		public Long call(Tuple2<String, Map<String, Object>> tuple) throws Exception {
			Map<String, Object> data = tuple._2;
			if (avgField != null) {
				return (Long) data.get(avgField);
			} else if (timeFormat != null && !(data.get(endTimeField) instanceof Long)) {
					return getTime((String)data.get(endTimeField)) - getTime((String)data.get(startTimeField));
			} else {
				return ((Long) data.get(endTimeField) - (Long) data.get(startTimeField));
			}
		}*/
	
	private Long getTime(String time) {
	
		Long epochTime = ZonedDateTime.parse( time , sdf)
					      .toInstant()
					      .toEpochMilli();
      return epochTime;
	}
	
	
}
