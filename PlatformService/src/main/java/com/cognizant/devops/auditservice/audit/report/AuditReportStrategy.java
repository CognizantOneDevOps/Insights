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
package com.cognizant.devops.auditservice.audit.report;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Deprecated
public abstract class AuditReportStrategy {

	private static final Logger log = LoggerFactory.getLogger(AuditReportStrategy.class.getName());

	public abstract boolean executeQuery(String content, String reportname, String subscribers);

	/**
	 * Checks keywords in cypher query before executing.
	 * @param cypherQuery
	 * @return boolean
	 */
	public boolean keywordCheck(String cypherQuery){
		log.info("keywordCheck for --   -"+cypherQuery);
		List<String> myList = Arrays.asList("create", "merge", "delete", "remove", "detach", "drop", "set");
		return myList.stream()
				.filter(keywords -> cypherQuery.toUpperCase().contains(keywords.toUpperCase()))
				.findAny().isPresent();

	}

}
