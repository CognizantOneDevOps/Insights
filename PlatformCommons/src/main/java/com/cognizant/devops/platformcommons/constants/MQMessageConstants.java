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

public interface MQMessageConstants  {
	 String EXCHANGE_NAME = "iSight";
	 String EXCHANGE_TYPE = "topic";
	 String ROUTING_KEY_FOR_DATA = "DATA";
	 String ROUTING_KEY_FOR_HEALTH = "HEALTH";
	 String ROUTING_KEY_SEPERATOR = "\\.";
	 String MESSAGE_ENCODING = "UTF-8";
	 String RECOVER_EXCHANGE_NAME ="iRecover";
	 String RECOVER_EXCHANGE_TYPE ="fanout";
	 String RECOVER_QUEUE="INSIGHTS_RECOVER_QUEUE";
	 String RECOVER_ROUNTINGKEY_QUEUE="INSIGHTS.RECOVER.QUEUE";
	 String RECOVER_EXCHANGE_PROPERTY= "x-dead-letter-exchange";
	 String MILESTONE_STATUS_QUEUE = "MILESTONE_STATUS_QUEUE";
}
