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

public final class MQMessageConstants  {
	public static final String EXCHANGE_NAME = "iSight";
	public static final String EXCHANGE_TYPE = "topic";
	public static final String ROUTING_KEY_FOR_DATA = "DATA";
	public static final String ROUTING_KEY_FOR_HEALTH = "HEALTH";
	public static final String ROUTING_KEY_SEPERATOR = "\\.";
	public static final String MESSAGE_ENCODING = "UTF-8";
	public static final String RECOVER_EXCHANGE_NAME ="iRecover";
	public static final String RECOVER_EXCHANGE_TYPE ="fanout";
	public static final String RECOVER_QUEUE="INSIGHTS_RECOVER_QUEUE";
	public static final String RECOVER_ROUNTINGKEY_QUEUE="INSIGHTS.RECOVER.QUEUE";
	public static final String RECOVER_EXCHANGE_PROPERTY= "x-dead-letter-exchange";
	public static final String MILESTONE_STATUS_QUEUE = "MILESTONE_STATUS_QUEUE";
	public static final String FIFO_EXTENSION = ".fifo";
}