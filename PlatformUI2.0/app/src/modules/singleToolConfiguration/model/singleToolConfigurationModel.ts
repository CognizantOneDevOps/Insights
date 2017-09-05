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

module ISightApp {
    export class ToolConfigurationPageModel {
        toolsConfigRows: ToolConfigurationDetail[] = [];
    }
    export class ToolConfigurationDetail {
        agentId: number = 0;
        category: string = '';
        toolName: string = '';
        ToolConfigurationDataModel : ToolConfigurationDataModel;
    }
    export class ToolConfigurationDataModel {
        startFromDate: string = '';
        toolUrl: string = '';
        runSchedule: number = 5;
        selectedAuthMtd: string = 'Auth Token';
        authToken: string = '';
        userId: string = '';
        password: string = '';
        toolsTimeZone: string = ''
        timeStampField: string = '';
        timeStampFormat: string = '';
       /* responsetemplate: Object = {

        };
        useResponseTemplate: boolean = false;
        configs = {

        }*/
    }
}