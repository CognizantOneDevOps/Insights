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

import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { ImageHandlerService } from '@insights/common/imageHandler.service';
import { LogService } from '@insights/common/log-service';

@Injectable()
export class InsightsInitService {

    location: Location;
    static serviceHost: String;
    static grafanaHost: String;
    static agentsOsList = {};
    static configDesc = {};
    static showAuditReporting = false;

    constructor(location: Location, private http: HttpClient,
        private cookieService: CookieService, private imageHandler: ImageHandlerService,
        private logger: LogService) {
    }

    public async initMethods() {
        const result1 = await this.loadUiServiceLocation();
        const result2 = await this.loadAgentConfigDesc();
        const result3 = await this.loadImageHandler();
    }

    private async loadAgentConfigDesc() {
        var self = this;
        var agentConfigJsonUrl = "config/configDesc.json"
        let gentConfigResponse = await this.getJSONUsingObservable(agentConfigJsonUrl).toPromise();
        InsightsInitService.configDesc = gentConfigResponse.desriptions;

    }

    private async loadUiServiceLocation() {
        var self = this;
        var uiConfigJsonUrl = "config/uiConfig.json"
        let UIConfigResponse = await this.getJSONUsingObservable(uiConfigJsonUrl).toPromise();

        if (UIConfigResponse.serviceHost == undefined && InsightsInitService.serviceHost == undefined) {
            InsightsInitService.serviceHost = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
        } else {
            InsightsInitService.serviceHost = UIConfigResponse.serviceHost;
        }

        if (UIConfigResponse.grafanaHost == undefined && InsightsInitService.grafanaHost == undefined) {
            InsightsInitService.grafanaHost = window.location.protocol + "//" + window.location.hostname + ":3000";
        } else {
            InsightsInitService.grafanaHost = UIConfigResponse.grafanaHost;
        }

        InsightsInitService.agentsOsList = UIConfigResponse.agentsOsList;
        InsightsInitService.showAuditReporting = UIConfigResponse.showAuditReporting;
    }

    private loadImageHandler() {
        this.imageHandler.initializeImageIcons();
        this.imageHandler.addPathIconRegistry();
    }

    public static getServiceHost(): String {
        return InsightsInitService.serviceHost;
    }

    public getConfigDesc() {
        return InsightsInitService.configDesc;
    }

    public getGrafanaHost(): String {
        return InsightsInitService.grafanaHost;
    };

    public getAgentsOsList(): any {
        return InsightsInitService.agentsOsList;
    }

    public getGrafanaHost1(): Promise<any> {
        var self = this;
        var resource;
        var authToken = this.cookieService.get('Authorization');
        var defaultHeader = { 'Authorization': authToken };
        var restcallUrl = InsightsInitService.getServiceHost() + "/PlatformService/configure/grafanaEndPoint";
        return this.http.get(restcallUrl).toPromise();
    }

    public getJSONUsingObservable(url): Observable<any> {
        return this.http.get(url)
    }
}