/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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


export interface IDataArchiving {
   
}

@Injectable()
export class DataArchivingService implements IDataArchiving {

    constructor(private restCallHandlerService: RestCallHandlerService) {
    }

    saveArchivalRecord(archivalDetails: string): Promise<any> {
        return this.restCallHandlerService.postWithData("SAVE_ARCHIVE_DETAILS", archivalDetails, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    listArchivedRecord(): Promise<any> {
        return this.restCallHandlerService.get("ARCHIVED_DATA_LIST");
    }

    listActiveArchivedRecord(): Promise<any> {
        return this.restCallHandlerService.get("ACTIVE_ARCHIVED_DATA_LIST");
    }

    deleteArchivedData(archivalName: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("DELETE_ARCHIVED_DATA", { 'archivalName': archivalName }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    inactivateArchivedData(archivalName: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("INACTIVATE_RECORD", { 'archivalName': archivalName }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    activateArchivedData(archivalName: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("ACTIVATE_RECORD", { 'archivalName': archivalName }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
 
    updateDataSourceURL(archivalURLDetails: string): Promise<any> {
        return this.restCallHandlerService.postWithData("UPDATE_DATASOURCE_URL", archivalURLDetails,"",{ 'Content-Type': 'application/json' }).toPromise();
    }
} 
