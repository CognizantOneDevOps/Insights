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

import { InsightsInitService } from '@insights/common/insights-initservice';
import { NavItem } from '@insights/app/modules/home/nav-item';
import { HomeComponent } from '@insights/app/modules/home/home.component';
import { Injectable } from '@angular/core';
import { DataSharedService } from '@insights/common/data-shared-service';

export class MenuItem {
  navItems: NavItem[] = [];
  navOrgList: NavItem[] = [];
  homeController: any;
  isServerConfigAvailable = true;
  navItemsBottom: NavItem[] = [];

  constructor(public dataShare: DataSharedService) {

  }

  public async loadorganizations() {
    var self = this;
    let currentUserOrgsArray = this.dataShare.getUserOrgArray();
    if (currentUserOrgsArray != undefined) {
      var orgDataArray = currentUserOrgsArray;
      for (var key in currentUserOrgsArray) {
        var orgDtl = currentUserOrgsArray[key];
        var navItemobj = new NavItem();
        navItemobj.displayName = orgDtl.name;
        navItemobj.iconName = 'grafanaOrg';
        navItemobj.route = 'InSights/Home/landingPage/' + orgDtl.orgId;
        navItemobj.isToolbarDisplay = InsightsInitService.enableInsightsToolbar;
        navItemobj.showIcon = false;
        navItemobj.orgId = orgDtl.orgId;
        navItemobj.title = orgDtl.name;
        navItemobj.showMenu = this.getMenuShowAdmin(['Admin', 'Editor', 'Viewer']),
        navItemobj.parentMenuId ='DashboardGroups';
        navItemobj.menuId = orgDtl.orgId;
        this.navOrgList.push(navItemobj)
        
      }

      var navItemobj = new NavItem();
      navItemobj.displayName = 'Traceability Dashboard';
      navItemobj.iconName = 'traceability';
      navItemobj.route = 'InSights/Home/traceability';
      navItemobj.isToolbarDisplay = InsightsInitService.enableInsightsToolbar;
      navItemobj.showIcon = false;
      navItemobj.showMenu = this.getMenuShowAdmin(['Admin', 'Editor', 'Viewer']),
      navItemobj.title = "traceability",
      navItemobj.parentMenuId ='DashboardGroups';
      navItemobj.menuId = "Traceability";

      this.navOrgList.push(navItemobj);
    }
  }

  getMenuShowAdmin(roleArry: any): boolean {
    var menuShow = false;
    this.dataShare.getUserRole()
    for (let role of roleArry) {
      if (this.dataShare.getUserRole() == role) {
        return true;
      }
    }
    return menuShow;
  }

  public loadMenuItem(): any {
    console.log(" this.isServerConfigAvailable " + this.isServerConfigAvailable)
    this.removeParentClassCSS();
    if (!this.isServerConfigAvailable) {
      this.navItems = [
        {
          displayName: 'Server Configuration',
          iconName: 'feature',
          route: 'InSights/Home/server-configuration',
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          showMenu: this.getMenuShowAdmin(['Admin']),
          title: "Server Configuration",
          menuId : "ServerConfiguration",
          parentMenuId :''
        }
      ];
    } else {
      this.navItems = [
        {
          displayName: 'Dashboard Groups',
          iconName: 'dash_Menu',
          showMenu: this.getMenuShowAdmin(['Admin', 'Editor', 'Viewer']),
          title: "Click on Organization to see various Org's Dashboards",
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          children: this.navOrgList,
          showIcon :true,
          menuId : "DashboardGroups",
          parentMenuId :''
        },
        {
          displayName: 'Audit Reporting',
          iconName: 'audit_Menu',
          showMenu: (InsightsInitService.showAuditReporting && this.getMenuShowAdmin(['Admin'])),
          title: "Audit Report and search assets",
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          showIcon :true,
          menuId : "AuditReporting",
          parentMenuId :'',
          children: [
            {
              displayName: 'Search Assets',
              iconName: 'feature',
              route: 'InSights/Home/blockchain',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: (InsightsInitService.showAuditReporting && this.getMenuShowAdmin(['Admin'])),
              title: "Search Assets",
              menuId : "SearchAssets",
              parentMenuId :'AuditReporting'
            }
          ]
        },
        {
          displayName: 'Playlist',
          iconName: 'playlist_Menu',
          route: 'InSights/Home/playlist',
          showIcon :true,
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          showMenu: this.getMenuShowAdmin(['Admin', 'Editor', 'Viewer']),
          title: "Playlist",
          menuId : "Playlist",
          parentMenuId :''
        },
        {
          displayName: 'Report Management',
          iconName: 'report_mgmt_Menu',
          showIcon :true,
          route: 'InSights/Home/reportmanagement',
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
          title: "Report Management",
          menuId : "ReportManagement",
          parentMenuId :''
        },
        {
          displayName: 'Data Dictionary',
          iconName: 'data_dictionary_Menu',
          showIcon :true,
          route: 'InSights/Home/datadictionary',
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
          title: "Data Dictionary",
          menuId : "DataDictionary",
          parentMenuId :''
        },
        {
          displayName: 'Health Check',
          iconName: 'health_Menu',
          showIcon :true,
          route: 'InSights/Home/healthcheck',
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          showMenu: this.getMenuShowAdmin(['Admin']),
          title: "Health Check",
          menuId : "HealthCheck",
          parentMenuId :''
        },
        {
          displayName: 'Dashboard Report Download',
          iconName: 'dash_report_Menu',
          showIcon :true,
          route: 'InSights/Home/dash-pdf-download',
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
          title: "Dashboard Report Download",
          menuId : "DashboardReportDownload",
          parentMenuId :''
        },
        {
          displayName: 'Configuration',
          iconName: 'config_Menu',
          isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
          title: "Configuration",
          showIcon :true,
          showMenu: this.getMenuShowAdmin(['Admin', 'Editor', 'Viewer']),
          menuId : "Configuration",
          parentMenuId :'',
          children: [
            {
              displayName: 'Agent Management',
              iconName: 'feature',
              route: 'InSights/Home/agentmanagement',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Agent Management",
              menuId : "AgentManagement",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Webhook Configuration',
              iconName: 'feature',
              route: 'InSights/Home/webhook',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: (InsightsInitService.showWebhookConfiguration && this.getMenuShowAdmin(['Admin'])),
              title: "WebHook",
              menuId : "WebHook",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Bulk Upload',
              iconName: 'feature',
              route: 'InSights/Home/bulkupload',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Bulk Upload",
              menuId : "BulkUpload",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Server Configuration',
              iconName: 'feature',
              route: 'InSights/Home/server-configuration',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Server Configuration",
              menuId : "ServerConfiguration",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Configuration File Management',
              iconName: 'feature',
              route: 'InSights/Home/filesystem',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Configuration File Management",
              menuId : "ConfigurationFileManagement",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Task Management',
              iconName: 'feature',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              menuId : "TaskManagement",
              title: "Task Management",
              parentMenuId :'Configuration',
              children: [
                      {
                        displayName: 'Workflow Task Management',
                        iconName: 'feature',
                        route: 'InSights/Home/workflow-task-management',
                        isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
                        showMenu: this.getMenuShowAdmin(['Admin']),
                        title: "Workflow Task Management",
                        menuId : "WorkflowTaskManagement",
                        parentMenuId :'TaskManagement'
                      },
                      {
                        displayName: 'Schedule Task Management',
                        iconName: 'ScheduleTaskManagement',
                        route: 'InSights/Home/taskManagement',
                        isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
                        showMenu: this.getMenuShowAdmin(['Admin']),
                        title: "Schedule Task Management",
                        menuId : "ScheduleTaskManagement",
                        parentMenuId :'TaskManagement'
                      } 
                ]
            },
            {
              displayName: 'Group & Users',
              iconName: 'feature',
              route: 'InSights/Home/accessGroupManagement',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Group & Users",
              menuId : "GroupAndUsers",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Co-Relation Builder',
              iconName: 'feature',
              route: 'InSights/Home/relationship-builder',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Relationship-Builder",
              menuId : "Relationship-Builder",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Business Mapping',
              iconName: 'feature',
              route: 'InSights/Home/businessmapping',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Business Mapping",
              menuId : "BusinessMapping",
              parentMenuId :'Configuration'
            },
           {
              displayName: 'ROI',
              iconName: 'feature',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
              menuId : "ROI",
              title: "ROI",
              parentMenuId :'Configuration',
              children: [
                {
                  displayName: 'OutCome Config',
                  iconName: 'feature',
                  route: 'InSights/Home/fetchOutcome',
                  isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
                  showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
                  title: "OutCome Config",
                  menuId : "OutComeConfig",
                  parentMenuId :'ROI'
                },
                {
                  displayName: 'MileStone Config',
                  iconName: 'feature',
                  route: 'InSights/Home/fetchMileStone',
                  isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
                  showMenu: this.getMenuShowAdmin(['Admin']),
                  title: "MileStone Config",
                  menuId : "MileStoneConfig",
                  parentMenuId :'ROI'
                }
              ]
            }, 
            {
              displayName: 'Report Configuration',
              iconName: 'feature',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
              menuId : "ReportConfiguration",
              title: "Report Configuration",
              parentMenuId :'Configuration',
              children: [
                {
                  displayName: 'Kpi Configuration',
                  iconName: 'feature',
                  route: 'InSights/Home/kpicreation',
                  isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
                  showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
                  title: "Kpi Creation",
                  menuId : "KpiCreation",
                  parentMenuId :'ReportConfiguration'
                },
                {
                  displayName: 'Content Configuration',
                  iconName: 'feature',
                  route: 'InSights/Home/contentConfig',
                  isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
                  showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
                  title: "Content Configuration",
                  menuId : "ContentConfiguration",
                  parentMenuId :'ReportConfiguration'
                },
                {
                  displayName: 'Report Template Configuration',
                  iconName: 'feature',
                  route: 'InSights/Home/reportTemplate',
                  isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
                  showMenu: this.getMenuShowAdmin(['Admin', 'Editor']),
                  title: "Report Template Configuration",
                  menuId : "ReportTemplateConfiguration",
                  parentMenuId :'ReportConfiguration'
                }
              ]
            },
            {
              displayName: 'Logo Setting',
              iconName: 'feature',
              route: 'InSights/Home/logoSetting',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Logo Setting",
              menuId : "LogoSetting",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Data Archival',
              iconName: 'feature',
              route: 'InSights/Home/dataarchiving',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Data Archival",
              menuId : "DataArchival",
              parentMenuId :'Configuration'
            },
            {
              displayName: 'Forecasting',
              iconName: 'feature',
              route: 'InSights/Home/modelmanagement',
              isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
              showMenu: this.getMenuShowAdmin(['Admin']),
              title: "Forecasting",
              menuId : "Forecasting",
              parentMenuId :'Configuration'
            }
          ]
        }
      ];
    }
    this.navItems.forEach((item) => {
       var count = this.showChildMenu(item)
    });
    return this.navItems;
  }

  public removeParentClassCSS() {
    let allParentCSSElement = document.getElementsByClassName("mat-list-item-parent");
    console.log("All active element ");
    for (var i = 0; i < allParentCSSElement.length; i++) {
      var elementParent = allParentCSSElement[i];
      let NameCSSParent = document.getElementById(elementParent.id + "Name");
      elementParent.classList.remove('mat-list-item-parent');
      NameCSSParent.classList.remove('displayNameParentMenuActive');
    }
  }

  public showChildMenu(item): any {
      var count = 0;
      if (item.hasOwnProperty("children")) {
        item.children.forEach((itemChild) => {
          if (itemChild.hasOwnProperty("children")) {
            count =  count + this.showChildMenu(itemChild)
          } else if (itemChild.showMenu) {
            count = count + 1;
            itemChild.activeCount = 1; 
          }
        })
        item.activeCount = count;
      } else if (item.showMenu) {
        count = count + 1;
        item.activeCount = 1;
      }
      return count; 
  }

  loadBottomMenuItem(): any {
    this.navItemsBottom = [
      {
        displayName: 'About',
        iconName: 'info',
        isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
        showIcon: false,
        title: "About",
        showMenu: true,
        menuId : "About"
      }, {
        displayName: 'Help',
        iconName: 'help',
        isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
        showIcon: false,
        title: "Help",
        showMenu: true,
        menuId : "Help"
      }, {
        displayName: 'Logout',
        iconName: 'logout',
        route: 'login',
        isToolbarDisplay: InsightsInitService.enableInsightsToolbar,
        showIcon: this.dataShare.getlogoutDisplay(),
        title: "Logout",
        showMenu: this.dataShare.getlogoutDisplay(),
        menuId : "Logout"
      }
    ];
    return this.navItemsBottom;
  }

}
