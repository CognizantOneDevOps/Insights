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
import { Observable } from 'rxjs';


export interface IBusinessMappingService {
    loadToolsAndCategories(): Promise<any>
}




@Injectable()
export class BusinessMappingService implements IBusinessMappingService {
    restHandler: any;
    constructor(private restCallHandlerService: RestCallHandlerService) {
        this.restHandler = this.restCallHandlerService;
    }

    loadToolsAndCategories(): Promise<any> {
        return this.restHandler.get("DATA_DICTIONARY_TOOLS_AND_CATEGORY");
    }

    loadToolProperties(labelName: string, categoryName: string): Promise<any> {
        return this.restHandler.get("DATA_DICTIONARY_TOOL_PROPERTIES", { 'labelName': labelName, 'categoryName': categoryName });
    }

    saveToolMapping(agentMappingJson: String) {
        return this.restHandler.postWithData("SAVE_TOOL_MAPPING", agentMappingJson, "", { 'Content-Type': 'application/json' }).toPromise();;
    }

    getToolMapping(agentName: String) {
        return this.restHandler.get("GET_TOOL_MAPPING", { 'agentName': agentName }, { 'Content-Type': 'application/x-www-form-urlencoded' });
    }

    editToolMapping(agentMappingJson: String) {
        return this.restHandler.postWithData("EDIT_TOOL_MAPPING", agentMappingJson, "", { 'Content-Type': 'application/json' }).toPromise();;
    }

    deleteToolMapping(uuid: string) {
        return this.restHandler.postWithParameter("DELETE_TOOL_MAPPING", { 'uuid': uuid }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();

    }

}

