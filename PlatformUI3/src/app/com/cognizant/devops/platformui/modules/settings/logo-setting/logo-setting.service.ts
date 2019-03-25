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
import { Observable } from 'rxjs'

export interface ILogoSettingService {
    uploadLogo(imageFile:any): Observable<any>;
}




@Injectable()
export class LogoSettingService implements ILogoSettingService {
    
    constructor(private restCallHandlerService: RestCallHandlerService) {
    }

    uploadLogo(imageFile:any): Observable<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.postWithImage("UPLOAD_IMAGE",imageFile);
    }
}

