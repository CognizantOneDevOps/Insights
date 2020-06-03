/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
import { Injectable } from '@angular/core';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { Observable, throwError } from 'rxjs';

export interface IAgentService {
    loadGlobalHealthConfigurations(): Promise<any>;
    loadHealthConfigurations(toolName: string, toolCategory: string): Promise<any>;
    loadServerHealthConfiguration(ServerName: string): Promise<any>;
    getDocRootAgentVersionTools(): Promise<any>;
    getDocrootAgentConfig(Version: string, toolName: string): Promise<any>;
    getDbAgentConfig(agentId: string): Promise<any>;
    loadAgentServices(ServerName: string): Promise<any>;
    registerAgent(toolName: string, toolVersion: string, osName: string, configData: string, trackingDetails: string): Promise<any>;
    updateAgent(agentId: string, configData: string, toolName: string, toolVersion: string, osName: string): Promise<any>;
    agentStartStop(agentId: string, toolName: string, osName: string, actionType: string): Promise<any>;
    agentUninstall(agentId: string, toolName: string, osversion: string): Promise<any>
}

@Injectable()
export class AgentService implements IAgentService {

    constructor(private restCallHandlerService: RestCallHandlerService) {

    }

    loadGlobalHealthConfigurations(): Promise<any> {
        return this.restCallHandlerService.get("HEALTH_GLOBAL");
    }

    loadHealthConfigurations(toolName: string, toolCategory: string): Promise<any> {
        return this.restCallHandlerService.get("HEALTH_TOOL", { 'tool': toolName, 'category': toolCategory });
    }

    loadServerHealthConfiguration(ServerName: string): Promise<any> {
        return this.restCallHandlerService.get(ServerName);
    }

    getDocRootAgentVersionTools(): Promise<any> {
        return this.restCallHandlerService.get("DOCROOT_AGENT_VERSION_TOOLS");
    }

    getDocrootAgentConfig(Version: string, toolName: string): Promise<any> {
        return this.restCallHandlerService.get("DOCROOT_AGENT_TOOL_CONFIG_DETAILS", { 'version': Version, 'tool': toolName });
    }

    getDbAgentConfig(agentId: string): Promise<any> {
        return this.restCallHandlerService.get("DB_AGENT_CONFIG_DETAILS", { 'agentId': agentId });
    }

    loadAgentServices(ServerName: string): Promise<any> {
        return this.restCallHandlerService.get(ServerName);
    }

    registerAgent(toolName: string, toolVersion: string, osName: string, configData: string, trackingDetails: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("AGENT_REGISTER", { 'toolName': toolName, 'agentVersion': toolVersion, 'osversion': osName, 'configDetails': configData, 'trackingDetails': trackingDetails }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    updateAgent(agentId: string, configData: string, toolName: string, toolVersion: string, osName: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("AGENT_UPDATE", { 'agentId': agentId, 'configJson': configData, 'toolName': toolName, 'agentVersion': toolVersion, 'osversion': osName }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    agentStartStop(agentId: string, toolName: string, osName: string, actionType: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("AGENT_START_STOP", { 'agentId': agentId, 'toolName': toolName, 'osversion': osName, 'action': actionType }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    agentUninstall(agentId: string, toolName: string, osversion: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("AGENT_UNINSTALL", { 'agentId': agentId, 'toolName': toolName, 'osversion': osversion }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    registerAgentV2(registerAgentJson: string): Promise<any> {
        return this.restCallHandlerService.postWithAgentData("AGENT_REGISTERV2", registerAgentJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    updateAgentV2(agentMappingJson: string): Promise<any> {
        return this.restCallHandlerService.postWithAgentData("AGENT_UPDATEV2", agentMappingJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();;
    }

}