/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.platforminsightswebhook.message.factory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;

public abstract class WebhookMessageFactory {

	public abstract void initializeConnection() throws Exception;

	public abstract void publishEventAction(String data, String webHookMqChannelName) throws Exception;

	public abstract void publishHealthData(String data, String healthQueueName)
			throws TimeoutException, IOException, JMSException;

}
