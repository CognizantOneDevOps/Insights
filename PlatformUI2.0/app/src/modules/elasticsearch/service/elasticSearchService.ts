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

/// <reference path="../../../_all.ts" />

module ISightApp {
	export interface IElasticSearchService {
		loadKibanaIndex(): ng.IPromise<any>;
		queryNeo4jData(queryTerm : string) : ng.IPromise<any>;
	}

	export class ElasticSearchService implements IElasticSearchService {
		static $inject = ['$resource', '$q', '$cookies', 'restCallHandlerService', 'restEndpointService'];

		constructor(private $resource, private $q, private $cookies, private restCallHandlerService: IRestCallHandlerService, private restEndpointService: IRestEndpointService) { }

		loadKibanaIndex(): ng.IPromise<any> {
			var restHandler = this.restCallHandlerService;
            return restHandler.get("SEARCH_DASHBOARD");
		}

		queryNeo4jData(queryTerm : string) : ng.IPromise<any>{
			var elasticSearchResource = this.$resource(this.restEndpointService.getelasticSearchServiceHost()+'/neo4j-index/_search?from=0&size=100&q=*'+queryTerm+'*',
				{},
				{
					get: {
						method: 'GET',
					}
				});
			return elasticSearchResource.get().$promise;
		}
	}
}
