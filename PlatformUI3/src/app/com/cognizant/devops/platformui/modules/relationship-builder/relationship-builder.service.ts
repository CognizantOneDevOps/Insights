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
import { Injectable, EventEmitter } from '@angular/core';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { HttpClient } from '@angular/common/http';

export interface IRelationshipBuilderService {
    loadToolsAndCategories(): Promise<any>;
    loadToolProperties(toolName: string, categoryName: string, labelName: string): Promise<any>;
    loadToolsRelationshipAndProperties(startToolName: string, startToolCategory: string, startLabelName: string, endToolName: string, endToolCatergory: string, endtLabelName: string): Promise<any>;
}

@Injectable()
export class RelationshipBuilderService implements IRelationshipBuilderService {

    constructor(private restCallHandlerService: RestCallHandlerService, private http: HttpClient) {
    }

    loadUiServiceLocation(): Promise<any> {

        return this.restCallHandlerService.get("CO_RELATIONSHIP_JSON");
    }

    loadToolsAndCategories(): Promise<any> {

        return this.restCallHandlerService.get("DATA_DICTIONARY_TOOLS_AND_CATEGORY");
    }

    saveCorrelationConfig(config: String): Promise<any> {

        return this.restCallHandlerService.postWithData("SAVE_RELATIONSHIP_JSON", config, "", { 'Content-Type': 'application/json' }).toPromise();
    }
    updateCorrelation(config: String): Promise<any> {

        return this.restCallHandlerService.postWithData("UPDATE_RELATIONSHIP", config, "", { 'Content-Type': 'application/json' }).toPromise();
    }
    deleteCorrelation(config: String): Promise<any> {

        return this.restCallHandlerService.postWithData("DELETE_RELATIONSHIP", config, "", { 'Content-Type': 'application/json' }).toPromise();
    }

    loadToolProperties(labelName: string, categoryName: string): Promise<any> {

        return this.restCallHandlerService.get("DATA_DICTIONARY_TOOL_PROPERTIES", { 'labelName': labelName, 'categoryName': categoryName });
    }

    loadToolsRelationshipAndProperties(startToolCategory: string, startLabelName: string, endToolCatergory: string, endLabelName: string): Promise<any> {

        return this.restCallHandlerService.get("DATA_DICTIONARY_TOOLS_RELATIONSHIPS", { 'startLabelName': startLabelName, 'startToolCategory': startToolCategory, 'endLabelName': endLabelName, 'endToolCatergory': endToolCatergory });
    }



}
