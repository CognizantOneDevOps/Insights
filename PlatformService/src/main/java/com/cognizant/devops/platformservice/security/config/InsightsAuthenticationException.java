/*********************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
 *******************************************************************************/
package com.cognizant.devops.platformservice.security.config;

import org.springframework.security.core.AuthenticationException;


public class InsightsAuthenticationException extends AuthenticationException {
		private static final long serialVersionUID = 147674667467L;
		/**
		 * Constructs an <code>InsightsAuthenticationException</code> with the
		 * specified message.
		 *
		 * @param msg the detail message
		 */
		public InsightsAuthenticationException(String msg) {
			super(msg);
		}

		/**
		 * Constructs an <code>InsightsAuthenticationException</code> with the
		 * specified message and root cause.
		 *
		 * @param msg the detail message
		 * @param t root cause
		 */
		public InsightsAuthenticationException(String msg, Throwable t) {
			super(msg, t);
		}
}
