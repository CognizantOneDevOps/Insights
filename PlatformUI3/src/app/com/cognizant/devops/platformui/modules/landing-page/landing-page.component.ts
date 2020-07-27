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
import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { SafeResourceUrl, DomSanitizer } from '@angular/platform-browser';
import { GrafanaDashboardMode } from '../grafana-dashboard/grafana-dashboard-model';
import { GrafanaDashboardService } from '../grafana-dashboard/grafana-dashboard-service';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { LandingPageService } from './landing-page.service';
import { RouterLinkWithHref, NavigationExtras, Router, NavigationStart, ActivatedRoute, ParamMap } from '@angular/router';
import { Event } from '@angular/router';
import { DataSharedService } from '@insights/common/data-shared-service';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css', './../home.module.css']
})
export class LandingPageComponent implements OnInit {
  @Input() isExpanded: boolean = false;
  breakpoint: number;
  recentdashIds: number[]
  orgId: string;
  dataArrayMasterList: any = {};
  generalDashboardLists = [];
  starredDashboardLists = [];
  recentDashboardLists = [];
  routeParameter: Observable<any>;
  dashboardUrl: SafeResourceUrl;
  iSightDashboards = [];
  dashboardTitle: string;
  selectedOrgUrl: string;
  grafanaEnable: boolean = false;
  showLandingPage: boolean = false;
  defaultOrg: number;
  selectedApp: string;
  framesize: any;
  selectedDashboardUrl: string = '';
  selectedDashboard: GrafanaDashboardMode;
  dashboards = [];
  isGeneralEmpty: boolean = false;
  isStarredEmpty: boolean = false;
  isRecentEmpty: boolean = false;
  isFolderListEmpty: boolean = false;
  showThrobber: boolean = false;
  enableMarginForStarred: boolean = false;
  enableMarginForRecent: boolean = false;
  dashboardslist: any;
  repsonseFromGrafana: any;
  isStarredMore: boolean = false;
  isRecentMore: boolean = false;
  folderList: any = {};
  showDashboards: boolean = true;
  folderListArray = [];
  allData = [];
  recentDashboardResponse: any;
  rowCountOfStarred: number;
  rowCountOfRecent: number;
  itemNew: any;
  constructor(private activeroute: ActivatedRoute, public router: Router, private grafanadashboardservice: LandingPageService, private dataShare: DataSharedService) {
  }

  ngOnInit() {
    var self = this;
    self.activeroute.paramMap.subscribe(async (params: ParamMap) => {
      self.orgId = params.get('id');
      self.parseDashboards();
    });
  }

  onResize(event) {
    this.breakpoint = (event.target.innerWidth <= 400) ? 1 : 6;
  }
  async parseDashboards() {
    this.queryForRecentDashboards();
    this.showThrobber = true;
    this.isStarredEmpty = false;
    this.isRecentEmpty = false;
    this.isGeneralEmpty = false;
    this.isFolderListEmpty = false;
    this.isStarredMore = false;
    this.isRecentMore = false;
    this.repsonseFromGrafana = await this.grafanadashboardservice.searchDashboard();
    if (this.repsonseFromGrafana.status == "success") {
      this.dashboardslist = this.repsonseFromGrafana.data;
      if (Object.keys(this.dashboardslist).length == 0) {
        this.showDashboards = false;
        this.showLandingPage = true;
        this.showThrobber = false;
      }
      else {
        this.showDashboards = true;
        this.showLandingPage = false;
        this.showThrobber = false;
        this.generalDashboardLists = this.dashboardslist.general;
        this.starredDashboardLists = this.dashboardslist.starred;
        if (this.starredDashboardLists.length > 6) {
          this.starredDashboardLists.splice(6, this.starredDashboardLists.length - 1)
        }
        this.dataArrayMasterList = this.dashboardslist;
        this.folderList = JSON.parse(JSON.stringify(this.dataArrayMasterList));
        if (Object.keys(this.folderList).length == 0) {
          this.isFolderListEmpty = true;
        } else {
          delete this.folderList['general']
          delete this.folderList['starred']
          this.folderListArray = [];
          this.allData = [];
          for (let key in this.folderList) {
            this.folderListArray.push({ key: key, value: this.folderList[key] });
            this.allData.push(this.folderList[key]);
          }
        }
        this.recentDashboardLists = [];
        if (this.recentdashIds.length != 0) {
          this.allData.push(this.generalDashboardLists);
          if (this.recentdashIds.length > 6) {
            this.recentdashIds.splice(6, this.recentdashIds.length - 1)
          }
          for (let i of this.recentdashIds) {
            for (let data of this.allData) {
              var existingValue;
              existingValue = data.find(x => x.id == i);
              if (existingValue != undefined) {
                this.recentDashboardLists.push(existingValue)
                break;
              }
            }
          }
        }
        this.checkList();
      }

    }
    else {
      this.showLandingPage = true;
      this.showDashboards = false;
      this.showThrobber = false;
    }
  }

  async queryForRecentDashboards() {
    let impressions = await window.localStorage[this.impressionKey()] || '[]';
    impressions = JSON.parse(impressions);
    impressions = impressions.filter(this.isNumber);
    this.recentdashIds = impressions;
  }
  isNumber(element) {
    if (typeof element === 'number') {
      return true;
    }
    else {
      return false;
    }
  }

  impressionKey() {
    return 'dashboard_impressions-' + this.dataShare.getOrgId();
  }

  calculateRowsForStarred(rowCountArray) {
    var myArray = JSON.parse(JSON.stringify(rowCountArray))
    var results = [];
    while (myArray.length) {
      results.push(myArray.splice(0, 4));
    }
    return results.length;
  }
  calculateRowsForRecent(rowCountArray) {
    var myArray = JSON.parse(JSON.stringify(rowCountArray))
    var results = [];
    while (myArray.length) {
      results.push(myArray.splice(0, 3));
    }
    return results.length;
  }
  checkList() {
    this.rowCountOfStarred = this.calculateRowsForStarred(this.starredDashboardLists);
    this.rowCountOfRecent = this.calculateRowsForRecent(this.recentDashboardLists);
    if (this.rowCountOfStarred > this.rowCountOfRecent) {
      this.isStarredMore = true;
      this.isRecentMore = false;
    }
    else if (this.rowCountOfStarred < this.rowCountOfRecent) {
      this.isRecentMore = true;
      this.isStarredMore = false;
    }
    if (this.generalDashboardLists == undefined || this.generalDashboardLists.length == 0) {
      this.isGeneralEmpty = true;
    }
    if (this.starredDashboardLists == undefined || this.starredDashboardLists.length == 0) {
      this.isStarredEmpty = true;
    }
    if (this.recentDashboardLists == undefined || this.recentDashboardLists.length == 0) {
      this.isRecentEmpty = true;
    }
    if (this.folderListArray == undefined || this.folderListArray.length == 0) {
      this.isFolderListEmpty = true;
    }

  }

  onClickHere() {
    this.showLandingPage = false;
    this.showDashboards = false;
    this.orgId = this.dataShare.getOrgId();
    var url = InsightsInitService.grafanaHost + '/dashboard/script/iSight_ui3.js?url=' + InsightsInitService.grafanaHost + '/?orgId=' + this.orgId;
    var route = 'InSights/Home/grafanadashboard';
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "dashboardURL": url
      }
    };
    //console.log(navigationExtras);
    this.router.navigate([route], navigationExtras);
  }
  onRowClicked(item) {
    this.grafanaEnable = true;
    this.showDashboards = false;
    this.showLandingPage = false;
    //window.open(item.url, '_blank'); 
    var route = 'InSights/Home/grafanadashboard';
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "dashboardURL": item.url
      }
    };
    this.router.navigate([route], navigationExtras);
  }

  checkEmptyColRequirements() {
    if (this.isStarredEmpty && !this.isRecentEmpty) {
      return true;
    }
    else {
      return false;
    }
  }
  checkEmptyColForRecent() {
    if (!this.isStarredEmpty && this.isRecentEmpty) {
      return true;
    }
    else {
      return false;
    }
  }
  checkNonEmpty() {
    if (!this.isStarredEmpty && !this.isRecentEmpty) {
      return true;
    }
    else {
      return false;
    }
  }
  checkEmptyStarredRecent() {
    if (this.isStarredEmpty && this.isRecentEmpty) {
      return true;
    }
    else {
      return false;
    }
  }

}
