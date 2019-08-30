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

import { NgModule, ModuleWithProviders } from '@angular/core';

import { CookieService } from 'ngx-cookie-service';
import { LoginService } from '@insights/app/login/login.service';
import { RestAPIurlService } from '@insights/common/rest-apiurl.service'
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service'
import { InsightsInitService } from '@insights/common/insights-initservice';
import { ImageHandlerService } from '@insights/common/imageHandler.service';
import { LogService } from '@insights/common/log-service';
import { DataSharedService } from '@insights/common/data-shared-service';
import { AuthInterceptor } from '@insights/common/rest-api-setting';
import { AuthService } from '@insights/common/auth-service.ts'
import { AuthGuardService } from '@insights/common/auth-guard.service.ts'

@NgModule({
  declarations: [
  ],
  exports: [
  ]
})
export class SharedServices {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: SharedServices,
      providers: [
        LoginService,
        RestAPIurlService,
        RestCallHandlerService,
        ImageHandlerService,
        InsightsInitService,
        CookieService,
        LogService,
        DataSharedService,
        AuthService,
        AuthInterceptor,
        AuthGuardService
      ]
    };
  }
}