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
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface IWebHookService {

}

@Injectable()
export class WebHookService implements IWebHookService {
    constructor(private restCallHandlerService: RestCallHandlerService) {
    }

    /*  saveDataforWebHook(webhookname: string, toolName: string, eventname: string, dataformat: string, mqchannel: string, subscribestatus: boolean, responseTemplate: string): Promise<any> {
         return this.restCallHandlerService.postWithParameter("SAVE_DATA_WEBHOOK_CONFIG", { 'webhookname': webhookname, 'toolName': toolName, 'eventname': eventname, 'dataformat': dataformat, 'mqchannel': mqchannel, 'subscribestatus': subscribestatus, 'responseTemplate': responseTemplate }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
     } */
    saveDataforWebHook(webhookMappingJson: string): Promise<any> {
        return this.restCallHandlerService.postWithData("SAVE_DATA_WEBHOOK_CONFIG", webhookMappingJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();

    }
    updateforWebHook(webhookMappingJson: string): Promise<any> {
        return this.restCallHandlerService.postWithData("UPDATE_WEBHOOK", webhookMappingJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
    loadwebhookServices(): Promise<any> {
        return this.restCallHandlerService.get("LIST_WEBHOOK");
    }
    webhookUninstall(webhookname: string): Promise<any> {
        console.log("entered")
        return this.restCallHandlerService.postWithParameter("DELETE_WEBHOOK", { 'webhookname': webhookname }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    getSampleJSONResponse(): string {
        //,{\"wid\":0,\"operationName\":\"dataEnrichment\",\"operationFields\":{\"sourceProperty\":\"\",\"keyPattern\":\"\",\"targetProperty\":\"\"},\"webhookName\":\"\"},
        //{\"wid\":0,\"operationName\":\"timeFieldSeriesMapping\",\"operationFields\":{\"mappingTimeField\":\"\",\"epochTime\":false,\"mappingTimeFormat\":\"\"},\"webhookName\":\"\"}
        let sampleStrJson = "[{\"wid\":-1,\"operationName\":\"insightsTimex\",\"operationFields\":{\"timeField\":\"\",\"epochTime\":false,\"timeFormat\":\"\"},\"webhookName\":\"\"}]";
        return sampleStrJson;
    }

    updateforWebHookStatus(webhookMappingJson: string): Promise<any> {
        return this.restCallHandlerService.postWithData("UPDATE_WEBHOOK_STATUS", webhookMappingJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
}