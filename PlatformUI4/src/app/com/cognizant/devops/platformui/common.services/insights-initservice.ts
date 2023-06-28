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
import { ImageHandlerService } from '@insights/common/imageHandler.service';


@Injectable()
export class InsightsInitService {

    location: Location;
    autheticationProtocolList = ["SAML", "NativeGrafana", "JWT"]
    static serviceHost: String;
    static grafanaHost: String;
    static webhookHost: String;
    static agentsOsList = {};
    static configDesc = {};   
    static showWebhookConfiguration = false;
    static autheticationProtocol = "NativeGrafana";
    static singleSignOnConfig;
    static enableInsightsToolbar = true;
    static isDebugModeEnable = false;
    static enableLogoutButton = true;
    static mlDataTypes = {};
    static version;
    static serverConfig = {};
    static jwtTokenOriginServerURL ={};
    static jwtOriginServerLoginURL : any;
    static configDescResponse : any;
    static product ={};
    

    constructor(location: Location, private http: HttpClient,
        private imageHandler: ImageHandlerService) {
    }

    public async initMethods() {
        const result1 = await this.loadAgentConfigDesc();
        const result2 = await this.loadUiServiceLocation();
        const result3 = await this.loadImageHandler();
    }

    private async loadAgentConfigDesc() {
        var self = this;
        var agentConfigJsonUrl = "config/configDesc.json"
        let gentConfigResponse = await this.getJSONUsingObservable(agentConfigJsonUrl).toPromise();
        var defaultConfigdata = JSON.stringify(gentConfigResponse);
        InsightsInitService.configDescResponse=JSON.parse(defaultConfigdata);
        InsightsInitService.configDesc = InsightsInitService.configDescResponse.desriptions;
        InsightsInitService.mlDataTypes = InsightsInitService.configDescResponse.mlDataTypes;
        InsightsInitService.serverConfig = InsightsInitService.configDescResponse.serverConfig;
        InsightsInitService.agentsOsList = InsightsInitService.configDescResponse.agentsOsList;
        InsightsInitService.product = InsightsInitService.configDescResponse.product;


    }
    private async loadUiServiceLocation() {
        var self = this; 
        var versionJsonUrl = "config/version.json"
        let versionJsonResponse = await this.getJSONUsingObservable(versionJsonUrl).toPromise();
        InsightsInitService.version=versionJsonResponse.uiVersion;

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

        if (UIConfigResponse.webhookHost == undefined && InsightsInitService.webhookHost == undefined) {
            InsightsInitService.webhookHost = window.location.protocol;
        } else {
            InsightsInitService.webhookHost = UIConfigResponse.webhookHost;
        }

        
        InsightsInitService.showWebhookConfiguration = UIConfigResponse.showWebhookConfiguration;
        InsightsInitService.jwtTokenOriginServerURL = UIConfigResponse.jwtTokenOriginServerURL;
        InsightsInitService.jwtOriginServerLoginURL = UIConfigResponse.jwtOriginServerLoginURL;
        console.info("Selected authetication Protocol is " + UIConfigResponse.autheticationProtocol);
        if (this.autheticationProtocolList.indexOf(UIConfigResponse.autheticationProtocol) < 0) {
            console.error("Please provide valid authetication Protocol from list " + String(this.autheticationProtocolList));
        }
        InsightsInitService.autheticationProtocol = UIConfigResponse.autheticationProtocol;
        InsightsInitService.singleSignOnConfig = InsightsInitService.configDescResponse[UIConfigResponse.autheticationProtocol];
        InsightsInitService.enableInsightsToolbar = UIConfigResponse.enableInsightsToolbar
        InsightsInitService.enableLogoutButton = UIConfigResponse.enableLogoutButton
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

    public getWebhookHost(): String {
        console.log(InsightsInitService.webhookHost);
        return InsightsInitService.webhookHost;
    };

    public getAgentsOsList(): any {
        return InsightsInitService.agentsOsList;
    }

    public getJSONUsingObservable(url): Observable<any> {
        return this.http.get(url)
    }

    public getMLDataTypes(): any {
        return InsightsInitService.mlDataTypes;
    }

    public getServerConfig(): any {
        return InsightsInitService.serverConfig;
    }
    
    public getJwtOriginServerLoginURL(): any {
        return InsightsInitService.jwtOriginServerLoginURL;
    }
}