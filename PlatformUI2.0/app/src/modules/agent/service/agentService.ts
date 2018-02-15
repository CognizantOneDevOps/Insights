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
    export interface IAgentService {
        loadGlobalHealthConfigurations(): ng.IPromise<any>;
        loadHealthConfigurations(toolName: string, toolCategory:string ): ng.IPromise<any>;
		loadServerHealthConfiguration(ServerName: string): ng.IPromise<any>;		
		getAgentversionTools(): ng.IPromise<any>;
		getAgentToolConfig(Version: string, toolName:string): ng.IPromise<any>;
    }

    export class AgentService implements IAgentService {
        static $inject = ['$resource', '$q', '$cookies', 'restEndpointService', 'restCallHandlerService'];
        constructor(private $resource, private $q, private $cookies, private restEndpointService: IRestEndpointService, private restCallHandlerService: IRestCallHandlerService) {
        }

        loadGlobalHealthConfigurations(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("HEALTH_GLOBAL");
        }

        loadHealthConfigurations(toolName: string, toolCategory:string ): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("HEALTH_TOOL",{'tool':toolName,'category':toolCategory});
           
        }
		
		loadServerHealthConfiguration(ServerName: string): ng.IPromise<any> {
			var restHandler = this.restCallHandlerService;			
			return restHandler.get(ServerName);			
		}
		
		getAgentversionTools():ng.IPromise<any> {
			var restHandler = this.restCallHandlerService;			
			return restHandler.get("GET_AGENT_VERSION_TOOLS");			
		}
		
		getAgentToolConfig(Version: string, toolName:string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("GET_AGENT_TOOL_CONFIG",{'version':Version,'tool':toolName},{'Content-Type': 'application/x-www-form-urlencoded'});
           
        }

			
    }
}
