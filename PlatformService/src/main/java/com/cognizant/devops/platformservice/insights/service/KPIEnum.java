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
package com.cognizant.devops.platformservice.insights.service;

public enum KPIEnum {
  
	    KPI1("110","Average Build Time","build","daily"),
	    KPI2("111","Average Build Time","build","weekly"),
	    KPI3("120","Number of builds","build","daily"),
	    KPI4("121","Number of builds","build","weekly");

	    private final String kpiId;
	    private final String kpiName;
	    private final String vector;
	    private final String schedule;

	    private KPIEnum(String kpiId,String kpiName, String vector, String schedule) {
	        this.kpiId = kpiId;
	        this.kpiName = kpiName;
	        this.vector = vector;
	        this.schedule = schedule;
	    }
	    
	    public String getKpiName() {
	        return this.kpiName;
	      }

	    public static String getKey(String kpiName,String sentiment, String schedule) {
	    	for (KPIEnum b : KPIEnum.values()) {
	    	      if (b.kpiName.equalsIgnoreCase(kpiName)) {
	    	        return b.vector +"."+b.kpiId+"."+sentiment+"."+b.schedule;
	    	      }
	    	    }
	        return null;
	    }

}
