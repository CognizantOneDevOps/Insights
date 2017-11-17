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
package com.cognizant.devops.platforminsights.core.avg;

import org.apache.spark.api.java.function.Function2;

public class CombineAverage implements Function2<Average, Average, Average>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3620814382510494699L;

	@Override
	public Average call(Average a, Average b) {
		a.total_ += b.total_;
		a.num_ += b.num_;
		return a;
	}
}
