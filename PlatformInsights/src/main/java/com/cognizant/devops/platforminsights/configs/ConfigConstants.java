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
package com.cognizant.devops.platforminsights.configs;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public interface ConfigConstants {
	String APP_NAME = ApplicationConfigProvider.getInstance().getSparkConfigurations().getAppName();
	String MASTER = ApplicationConfigProvider.getInstance().getSparkConfigurations().getMaster();
	String SPARK_EXECUTOR_MEMORY = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkExecutorMemory();
	String SPARK_ES_HOST = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkElasticSearchHost();
	String SPARK_ES_PORT = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkElasticSearchPort();
	String SPARK_ES_CONFIGINDEX = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkElasticSearchConfigIndex();
	String SPARK_ES_RESULTINDEX = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkElasticSearchResultIndex();
	String KPISIZE = ApplicationConfigProvider.getInstance().getSparkConfigurations().getKpiSize();
	Long SPARK_RESULT_SINCE = ApplicationConfigProvider.getInstance().getSparkConfigurations().getSparkResultSince();
	String SPARK_TIMEZONE = ApplicationConfigProvider.getInstance().getInsightsTimeZone();
}
