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

import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { DomSanitizer, BrowserModule, SafeUrl, SafeResourceUrl } from '@angular/platform-browser';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { GrafanaDashboardService } from '@insights/app/modules/grafana-dashboard/grafana-dashboard-service';
import { GrafanaAuthenticationService } from '@insights/common/grafana-authentication-service';
import { GrafanaDashboardMode } from '@insights/app/modules/grafana-dashboard/grafana-dashboard-model';
import { CookieService } from 'ngx-cookie-service';
import { HomeComponent } from '@insights/app/modules/home/home.component';


@Component({
    selector: 'app-grafana-dashboard',
    templateUrl: './grafana-dashboard.component.html',
    styleUrls: ['./grafana-dashboard.component.css', './../home.module.css']
})
export class GrafanaDashboardComponent implements OnInit {
    orgId: string;
    routeParameter: Observable<any>;
    dashboardUrl: SafeResourceUrl;
    iSightDashboards = [];
    dashboardTitle: string;
    selectedOrgUrl: string;
    defaultOrg: number;
    selectedApp: string;
    framesize: any;
    offset: number;
    selectedDashboardUrl: string = '';
    selectedDashboard: GrafanaDashboardMode;
    dashboards = [];
    constructor(private route: ActivatedRoute, private router: Router,
        private sanitizer: DomSanitizer, private grafanadashboardservice: GrafanaDashboardService, private cookieService: CookieService) {
        var self = this;
        if(InsightsInitService.enableInsightsToolbar) {
            this.offset = 55; // 5px is app-grafana-dashboard height true 5
        } else {
            this.offset = 0;
        }
        this.framesize = window.frames.innerHeight - this.offset
        console.log(" self.framesize inner height " + self.framesize);
        var receiveMessage = function (evt) {
            var height = parseInt(evt.data);
            if (!isNaN(height)) {
                self.framesize = (evt.data + 20);
            }
        }
        console.log(" self.framesize " + self.framesize);
        window.addEventListener('message', receiveMessage, false);
    }

    ngOnInit() {
        var url;
        this.route.queryParams.subscribe(params => {
            url = params["dashboardURL"];
        });
        this.dashboardUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    }

    setScrollBarPosition() {
        var self = this;
        console.log("In scroll function ");
        this.framesize = window.frames.innerHeight;
        var receiveMessage = function (evt) {
            var height = parseInt(evt.data);
            if (!isNaN(height)) {
                self.framesize = (evt.data + 20);
            }
        }
        window.addEventListener('message', receiveMessage, false);
        setTimeout(function () {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }, 1000);
    }

    async parseDashboards() {
        var dashboardslist = await this.grafanadashboardservice.searchDashboard();
        var self = this;
        var dataArray = dashboardslist.dashboards;
        console.log("dashboards...list in grafana", dashboardslist);
        if (dashboardslist != undefined && dataArray != undefined) {
            if (dataArray.length > 0) {
                var model = [];
                dataArray.forEach(element => {
                    model.push(new GrafanaDashboardMode(element.title, element.id, element.url, null, element.title, false));
                });
                self.dashboards = model;
                self.setSelectedDashboard(model[0]);
                if (self.selectedDashboardUrl && self.selectedDashboardUrl.trim().length != 0) {
                    var dashbmodel = new GrafanaDashboardMode(null, null, self.selectedDashboardUrl, null, null, false);
                    self.setSelectedDashboard(dashbmodel);
                }
                if (self.selectedDashboard) {
                    self.dashboardTitle = self.selectedDashboard.title;
                }
                console.log(self.dashboardTitle + "   " + self.selectedDashboard.title);
            } else {
                console.log("No dashboard  Array found");
            }
        } else {
            console.log("No dashboard found");
        }
        console.log("parseDashboards complate 1")

        if (this.selectedDashboard != undefined) {
            this.selectedDashboard.iframeUrl = this.selectedDashboard.iframeUrl.replace("iSight.js", "iSight_ui3.js");
            this.dashboardUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.selectedDashboard.iframeUrl);
        } else {
            this.dashboardUrl = this.sanitizer.bypassSecurityTrustResourceUrl(InsightsInitService.grafanaHost + '/dashboard/script/iSight_ui3.js?url=' + InsightsInitService.grafanaHost + '/?orgId=' + this.orgId);// 1/?orgId=3 3/d/DrPYuKJmz/dynatrace-data?orgId=
            console.log("No dashboard found,set default dashboardUrl");
        }
    }

    private setSelectedDashboard(dashboard) {
        var self = this;
        self.selectedDashboard = dashboard;
        self.dashboardTitle = dashboard.title;
        //console.log(self.dashboardTitle);
        //if (dashboard.dashboardUrl) {
        self.selectedDashboard.iframeUrl = dashboard.iframeUrl;
        //self.setScrollBarPosition();
        //}
        //console.log(self.selectedDashboard.iframeUrl);
    };

}
