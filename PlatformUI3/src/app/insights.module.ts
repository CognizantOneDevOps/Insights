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

import { DomSanitizer, BrowserModule, SafeUrl } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule, HttpHeaders, HttpClientXsrfModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { APP_INITIALIZER } from '@angular/core';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { InsightsModuleRouting } from '@insights/app/insights.routing';
import { MaterialModule } from '@insights/app/material.module';
import { HomeModules } from '@insights/app/modules/home.modules';
import { SharedServices } from '@insights/app/shared.services';
import { InsightsInitService } from '@insights/common/insights-initservice';

import { InsightsAppComponent } from '@insights/app/insights.component';
import { LoginComponent } from '@insights/app/login/login.component';
import { AuthInterceptor } from '@insights/common/rest-api-setting';
import { SSOLoginComponent } from './com/cognizant/devops/platformui/ssologin/ssologin.component';
import { CookieModule } from 'ngx-cookie';
import { LogoutHandlerComponent } from '@insights/app/com/cognizant/devops/platformui/logout-handler/logout-handler.component.ts';


export function initializeApp(initConfig: InsightsInitService) {
  return () => initConfig.initMethods();
}

@NgModule({
  declarations: [
    InsightsAppComponent,
    LoginComponent,
    SSOLoginComponent,
    LogoutHandlerComponent
  ],
  imports: [
    InsightsModuleRouting,
    BrowserModule,
    HttpClientModule,
    CommonModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    FormsModule,
    HomeModules,
    MaterialModule,
    SharedServices.forRoot(),
    HttpClientXsrfModule.withOptions({ cookieName: 'XSRF-TOKEN', headerName: 'XSRF-TOKEN' }),
    CookieModule.forRoot()
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [InsightsInitService], multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [InsightsAppComponent]
})

export class InsightsAppModule { }
