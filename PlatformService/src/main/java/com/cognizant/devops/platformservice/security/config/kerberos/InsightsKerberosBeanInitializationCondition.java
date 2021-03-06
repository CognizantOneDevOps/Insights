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
package com.cognizant.devops.platformservice.security.config.kerberos;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public class InsightsKerberosBeanInitializationCondition implements Condition {
	static final String SSOTYPE = "Kerberos";
	
	/**
	 * Used to initiate bean based on AUTH_TYPE of Authentication protocol
	 *
	 */
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return SSOTYPE.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol());
	}
}
