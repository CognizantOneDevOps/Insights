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
import { Component, OnInit, Input } from "@angular/core";
import { Observable } from "rxjs";
import { SafeResourceUrl } from "@angular/platform-browser";
import { GrafanaDashboardMode } from "../grafana-dashboard/grafana-dashboard-model";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { LandingPageService } from "./landing-page.service";
import {
  NavigationExtras,
  Router,
  ActivatedRoute,
  ParamMap,
} from "@angular/router";
import { DataSharedService } from "@insights/common/data-shared-service";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatButtonToggleModule } from "@angular/material/button-toggle";

@Component({
  selector: "app-landing-page",
  templateUrl: "./landing-page.component.html",
  styleUrls: ["./landing-page.component.scss", "./../home.module.scss"],
})
export class LandingPageComponent implements OnInit {
  recentdashIds: number[];
  orgId: string;
  recentDashboardLists = [];
  showLandingPage: boolean = false;
  isFolderListEmpty: boolean = false;
  showThrobber: boolean = false;
  dashboardslist: any;
  repsonseFromGrafana: any;
  folderList: any = {};
  showDashboards: boolean = true;
  folderListArrayMasterData = new Map<string, any>();
  folderListArrayFiltered = [];
  folderListArrayFilteredMap = new Map<string, any>();
  allData = [];
  starredDashbaordKey: string = "starred";
  recentDashboardKey: string = "recent";
  gridWidth: any;
  gridHeight: any;
  isListView: boolean = false;
  isExpand: boolean = false;
  orgName: string;
  gutter: string;

  constructor(
    private activeroute: ActivatedRoute,
    public router: Router,
    private grafanadashboardservice: LandingPageService,
    private dataShare: DataSharedService
  ) {}

  ngOnInit() {
    this.orgName = this.dataShare.getOrgName();
    this.dataShare.currOrgTitle.subscribe((title) => {
      this.orgName = title;
    });
    var self = this;
    self.activeroute.paramMap.subscribe(async (params: ParamMap) => {
      self.orgId = params.get("id");
      self.parseDashboards(false);
    });
  }

  async parseDashboards(isReload: Boolean) {
    this.queryForRecentDashboards();
    this.showThrobber = true;
    this.isFolderListEmpty = false;
    this.isExpand = false;
    this.repsonseFromGrafana =
      await this.grafanadashboardservice.searchDashboard();
    if (this.repsonseFromGrafana.status == "success") {
      this.dashboardslist = this.repsonseFromGrafana.data;
      if (Object.keys(this.dashboardslist).length == 0) {
        this.showDashboards = false;
        this.showLandingPage = true;
        this.showThrobber = false;
      } else {
        this.showDashboards = true;
        this.showLandingPage = false;
        this.showThrobber = false;
        this.folderList = JSON.parse(JSON.stringify(this.dashboardslist));
        if (Object.keys(this.folderList).length == 0) {
          this.isFolderListEmpty = true;
        } else {
          this.folderListArrayMasterData = new Map<string, any>();
          this.allData = [];
          for (let key in this.folderList) {
            if (key == "general") {
              key = "General Dashboards";
              this.folderListArrayMasterData.set(
                key,
                this.folderList["general"]
              );
            } else {
              this.folderListArrayMasterData.set(key, this.folderList[key]);
            }
            this.allData.push(this.folderList[key]);
          }
        }
        this.recentDashboardLists = [];
        if (this.recentdashIds.length != 0) {
          for (let i of this.recentdashIds) {
            this.folderListArrayMasterData.forEach(
              (value: any, key: string) => {
                let existingValue = value.find((x) => x.id == i);
                if (
                  existingValue != undefined &&
                  this.recentDashboardLists.filter((e) => e.id == i).length == 0
                ) {
                  this.recentDashboardLists.push(existingValue);
                }
              }
            );
          }
        }
        if (this.recentDashboardLists.length > 0) {
          this.folderListArrayMasterData.set(
            "recent",
            this.recentDashboardLists
          );
        }
        this.folderListArrayFilteredMap = new Map(
          this.folderListArrayMasterData
        );
        this.gridListToggleLoad("list");
      }
    } else {
      this.showLandingPage = true;
      this.showDashboards = false;
      this.showThrobber = false;
    }
  }

  gridListToggleLoad(value) {
    var listElement = document.getElementById("listColor");
    var gridElement = document.getElementById("gridColor");
    if (value == "grid") {
      document.getElementById("listColor").style.color = "var(--text-clr9)";
      document.getElementById("gridColor").style.color = "#296dfa";
      listElement.classList.remove("bgCss");
      gridElement.classList.add("bgCss");
      this.gridWidth = "236px";
      this.gridHeight = "2:1";
      this.gutter = "15px";
      this.isListView = false;
    } else {
      document.getElementById("gridColor").style.color = "var(--text-clr9)";
      document.getElementById("listColor").style.color = "#296dfa";
      listElement.classList.add("bgCss");
      gridElement.classList.remove("bgCss");
      this.gridWidth = "800px";
      this.gridHeight = "40px";
      this.gutter = "8px";
      this.isListView = true;
    }
  }

  getDashboardList(dashboardType: any) {
    if (
      !this.folderListArrayFilteredMap.has(dashboardType) &&
      dashboardType != "all"
    ) {
      return [];
    }
    if (dashboardType == this.starredDashbaordKey) {
      let key = Array.from(this.folderListArrayFilteredMap.keys()).filter(
        (item) => item == this.starredDashbaordKey
      );
      let starredArray = this.folderListArrayFilteredMap.get(key[0]);
      return starredArray;
    } else if (dashboardType == this.recentDashboardKey) {
      let key = Array.from(this.folderListArrayFilteredMap.keys()).filter(
        (item) => item == this.recentDashboardKey
      );
      let recentArray = this.folderListArrayFilteredMap.get(key[0]);
      return recentArray;
    } else if (dashboardType == "all") {
      return this.folderListArrayFilteredMap;
    }
  }

  showAllDashbordKey(folder): boolean {
    if (
      folder.key == this.starredDashbaordKey ||
      folder.key == this.recentDashboardKey ||
      folder.value.length == 0
    ) {
      return false;
    } else {
      return true;
    }
  }

  async queryForRecentDashboards() {
    let impressions = (await window.localStorage[this.impressionKey()]) || "[]";
    impressions = JSON.parse(impressions);
    impressions = impressions.filter(this.isNumber);
    this.recentdashIds = impressions;
  }
  isNumber(element) {
    if (typeof element === "number") {
      return true;
    } else {
      return false;
    }
  }

  impressionKey() {
    return "dashboard_impressions-" + this.dataShare.getOrgId();
  }

  onClickNoDashboard() {
    this.showLandingPage = false;
    this.showDashboards = false;
    this.orgId = this.dataShare.getOrgId();
    var url =
      InsightsInitService.grafanaHost +
      "/dashboard/script/iSight_ui3.js?url=" +
      InsightsInitService.grafanaHost +
      "/?orgId=" +
      this.orgId;
    var route = "InSights/Home/grafanadashboard";
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        dashboardURL: url,
      },
    };
    this.router.navigate([route], navigationExtras);
  }

  onRowClicked(item) {
    this.showDashboards = false;
    this.showLandingPage = false;
    var route = "InSights/Home/grafanadashboard";
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        dashboardURL: item.url.replace(
          "INSIGHTS_GRAFANA_HOST",
          InsightsInitService.grafanaHost.toString()
        ),
      },
    };
    this.router.navigate([route], navigationExtras);
  }

  applyFilter(filterValue: string) {
    if (filterValue != "" && filterValue != undefined) {
      console.log(filterValue);
      this.isExpand = true;
      this.folderListArrayFilteredMap = new Map(this.folderListArrayMasterData);
      this.folderListArrayFilteredMap.forEach((value: any, key: string) => {
        let filterList = value.filter((o) => {
          return Object.keys(o).some((k) => {
            if (typeof o[k] === "string" && k === "title")
              return o[k].toLowerCase().includes(filterValue.toLowerCase());
          });
        });
        this.folderListArrayFilteredMap.set(key, filterList);
      });
    } else {
      this.isExpand = false;
      this.folderListArrayFilteredMap = new Map(this.folderListArrayMasterData);
    }
  }
}
