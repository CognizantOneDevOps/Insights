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

import { Component, ViewChild, HostBinding, Input, ElementRef, OnInit } from '@angular/core';
import { GrafanaAuthenticationService } from '@insights/common/grafana-authentication-service';
import { CookieService } from 'ngx-cookie-service';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { Router } from '@angular/router';
import { NavItem } from '@insights/app/modules/home/nav-item';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { DataSharedService } from '@insights/common/data-shared-service';
import { AboutDialog } from '@insights/app/modules/about/about-show-popup';
import { MatDialog } from '@angular/material';
import { ImageHandlerService } from '@insights/common/imageHandler.service';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  animations: [
    trigger('indicatorRotate', [
      state('collapsed', style({ transform: 'rotate(0deg)' })),
      state('expanded', style({ transform: 'rotate(180deg)' })),
      transition('expanded <=> collapsed',
        animate('225ms cubic-bezier(0.4,0.0,0.2,1)')
      ),
    ])
  ]
})

export class HomeComponent implements OnInit {

  @ViewChild('sidenav') sidenav: ElementRef;
  isExpanded = true;
  element: HTMLElement;
  userName: String = '';
  userRole: String = '';
  userCurrentOrg: string = '';
  showAdminTab: boolean = false;
  isToolbarDisplay: boolean = InsightsInitService.enableInsightsBranding;
  showBusinessMapping: boolean = false;
  isValidUser: boolean = false;
  iframeStyle = 'width:100%; height:500px;';
  iframeWidth = window.innerWidth - 20;
  iframeHeight = window.innerHeight;
  grafanaResponse: any;
  expanded: boolean;
  @HostBinding('attr.aria-expanded') ariaExpanded = this.expanded;
  @Input() item: NavItem;
  @Input() depth: number;
  navItems: NavItem[] = [];
  navItemsBottom: NavItem[] = [];
  navOrgList: NavItem[] = [];
  selectedItem: NavItem;
  orgList = [];
  defaultOrg: number;
  selectedOrg: String;
  selectedOrgName: String;
  sidenavWidth: number = 14;
  framesize: any;
  leftNavWidthInPer: number;
  leftNavMinWidthInPer: number;
  leftNavWidthpx: number;
  displayLandingPage: boolean = false;
  currentUserOrgsArray = []
  currentUserWithOrgs: any;
  insightsCustomerLogo: any;
  aboutPageURL = "https://onedevops.atlassian.net/wiki/spaces/OI/pages/218936/Release+Notes";
  helpPageURL = "https://onedevops.atlassian.net/wiki/spaces/OI/overview";

  constructor(private grafanaService: GrafanaAuthenticationService,
    private cookieService: CookieService, private config: InsightsInitService,
    public router: Router, private dataShare: DataSharedService,
    private dialog: MatDialog, private imageHandeler: ImageHandlerService,
    public messageDialog: MessageDialogService ) {
    console.log("Home page constructer ");
    this.displayLandingPage = true;
    if (this.depth === undefined) {
      this.depth = 0;
    }
    
    this.isValidUser = true;
    this.framesize = window.frames.innerHeight;
    this.leftNavWidthInPer = 20;
    this.leftNavMinWidthInPer = 6;
    this.leftNavWidthpx = (window.frames.innerWidth * this.leftNavWidthInPer) / 100;
    var receiveMessage = function (evt) {
      var height = parseInt(evt.data);
      if (!isNaN(height)) {
        this.framesize = (evt.data + 20);
      }
    }
    var otherMenu = ((45 / 100) * this.framesize);
    this.framesize = this.framesize - otherMenu; //bottom nav 106 px + tap fix content 110 236
    window.addEventListener('message', receiveMessage, false);
    this.getInformationFromGrafana();
  }

  ngOnInit() {
    console.log("Home page constructer nginit ");
    this.loadCustomerLogo();
  }

  onMenuClick() {
    this.isExpanded = !this.isExpanded
  }

  loadCustomerLogo() {
    console.log("In Customer logo method");
    this.insightsCustomerLogo = this.dataShare.getCustomerLogo();
    //console.log(this.insightsCustomerLogo);
    if (this.insightsCustomerLogo == "DefaultLogo") {
      this.insightsCustomerLogo = "";
    }
    if (this.insightsCustomerLogo == undefined) {
      this.getLogoImage();
    }
  }


  async getLogoImage() {
    console.log(" Inside getLogoImage ");
    try {
      var self = this;
      this.grafanaService.getLogoImage().then(
        function (resourceImage) {
          self.dataShare.removeCustomerLogoFromSesssion();
          if (resourceImage.data.encodedString.length > 0) {
            var imageSrc = 'data:image/jpg;base64,' + resourceImage.data.encodedString;
            self.imageHandeler.addImage("customer_logo_uploded", imageSrc);
            self.insightsCustomerLogo = imageSrc;
            self.dataShare.uploadOrFetchLogo(imageSrc);
          }
        }
      )
    } catch (error) {
      console.log(error);
    }
  }

  public async getInformationFromGrafana() {
    let currentUserResponce: any;
    let self = this;
    this.loadBottomMenuItem();
    this.currentUserWithOrgs = await this.grafanaService.getCurrentUserWithOrgs();
    //console.log(this.currentUserWithOrgs);
    if (this.currentUserWithOrgs != undefined && this.currentUserWithOrgs.data != undefined) {
      if (InsightsInitService.ssoEnabled) {
        this.userName = self.dataShare.getSSOUserName();
      } else {
        this.userName = self.currentUserWithOrgs.data.userDetail.name != undefined ? self.currentUserWithOrgs.data.userDetail.name.replace(/['"]+/g, '') : "";
      }
      this.dataShare.setUserName(this.userName);
      this.userCurrentOrg = this.currentUserWithOrgs.data.userDetail.orgId;
      this.currentUserOrgsArray = this.currentUserWithOrgs.data.orgArray;
      //console.log(this.currentUserOrgsArray);
      for (let orgData of this.currentUserOrgsArray) {
        if (orgData.orgId == this.userCurrentOrg) {
          this.selectedOrg = orgData.name;
          this.userRole = orgData.role;
          this.selectedOrgName = this.getSelectedOrgName(this.selectedOrg);
        }
      }
      this.dataShare.setOrgAndRole(self.selectedOrg, self.userCurrentOrg, self.userRole);
      //console.log(self.userRole.toString() + "   " + self.userCurrentOrg);
      this.cookieService.set('grafanaRole', self.userRole.toString());
      this.cookieService.set('grafanaOrg', self.userCurrentOrg);
      this.loadorganizations();
    } else {
      console.log(" user and user organization data is not valid  ")
    }
    if (this.userRole === 'Admin') {
      this.showAdminTab = true;
    } else {
      this.showAdminTab = false;
    }
    this.loadMenuItem();
  }



  public async loadorganizations() {
    var self = this;

    if (this.currentUserOrgsArray != undefined) {
      var orgDataArray = this.currentUserOrgsArray;
      this.orgList = orgDataArray;
      for (var key in this.orgList) {
        var orgDtl = this.orgList[key];
        var navItemobj = new NavItem();
        navItemobj.displayName = orgDtl.name;
        navItemobj.iconName = 'grafanaOrg';
        navItemobj.route = 'InSights/Home/grafanadashboard/' + orgDtl.orgId;
        navItemobj.isToolbarDisplay = false;
        navItemobj.showIcon = false;
        navItemobj.isAdminMenu = false;
        navItemobj.orgId = orgDtl.orgId;
        navItemobj.title = orgDtl.name;
        this.navOrgList.push(navItemobj);
      }

      var navItemobj = new NavItem();
      navItemobj.displayName = 'Traceability Dashboard';
      navItemobj.iconName = 'traceability';
      navItemobj.route = 'InSights/Home/traceability';
      navItemobj.isToolbarDisplay = InsightsInitService.enableInsightsBranding;
      navItemobj.showIcon = true;
      navItemobj.isAdminMenu = false;
      navItemobj.title = "traceability";

      this.navOrgList.push(navItemobj);
    }
  }


  onItemSelected(item: NavItem) {
    this.selectedItem = item;
    this.isToolbarDisplay = item.isToolbarDisplay;
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      if (!item.children || !item.children.length) {
        if (item.iconName == 'grafanaOrg') {
          this.displayLandingPage = false;
          this.switchOrganizations(item.orgId, item.route, this.selectedOrgName, this.selectedItem);
        } else if (item.displayName == 'About') {
          this.about();
        } else if (item.displayName == 'Help') {
          this.displayLandingPage = false;
          window.open(this.helpPageURL, "_blank");
        } else if (item.displayName == 'Logout') {
          this.displayLandingPage = false;
          this.logout();
        } else {
          this.displayLandingPage = false;
          this.router.navigateByUrl(item.route, { skipLocationChange: true });
        }
      }
    }
  }

  public loadMenuItem() {
    this.navItems = [
      {
        displayName: 'Dashboard Groups',
        iconName: 'feature',
        isAdminMenu: false,
        showMenu: true,
        title: "Click on Organization to see various Org's Dashboards",
        isToolbarDisplay: InsightsInitService.enableInsightsBranding,
        children: this.navOrgList
      },
      {
        displayName: 'Audit Reporting',
        iconName: 'feature',
        isAdminMenu: true,
        showMenu: InsightsInitService.showAuditReporting,
        title: "Audit Report and search assets",
        isToolbarDisplay: InsightsInitService.enableInsightsBranding,
        children: [
          {
            displayName: 'Search Assets',
            iconName: 'feature',
            route: 'InSights/Home/blockchain',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: InsightsInitService.showAuditReporting,
            title: "Search Assets",
            isAdminMenu: true
          },
          {
            displayName: 'Query Builder',
            iconName: 'feature',
            route: 'InSights/Home/querybuilder',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: InsightsInitService.showAuditReporting,
            title: "Query Builder",
            isAdminMenu: true
          }

        ]
      },
      {
        displayName: 'Playlist',
        iconName: 'feature',
        route: 'InSights/Home/playlist',
        isToolbarDisplay: false,
        showMenu: true,
        title: "Playlist",
        isAdminMenu: false
      },
      {
        displayName: 'Data Dictionary',
        iconName: 'datadictionary',
        route: 'InSights/Home/datadictionary',
        isToolbarDisplay: InsightsInitService.enableInsightsBranding,
        showMenu: true,
        title: "Data Dictionary",
        isAdminMenu: false
      },
      {
        displayName: 'Health Check',
        iconName: 'feature',
        route: 'InSights/Home/healthcheck',
        isToolbarDisplay: InsightsInitService.enableInsightsBranding,
        showMenu: true,
        title: "Health Check",
        isAdminMenu: true
      },

      {
        displayName: 'Configuration',
        iconName: 'admin',
        isToolbarDisplay: InsightsInitService.enableInsightsBranding,
        isAdminMenu: true,
        title: "Configuration",
        showMenu: true,
        children: [
          {
            displayName: 'Agent Management',
            iconName: 'feature',
            route: 'InSights/Home/agentmanagement',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: true,
            title: "Agent Management",
            isAdminMenu: true
          },
          {
            displayName: 'Bulk Upload',
            iconName: 'feature',
            route: 'InSights/Home/bulkupload',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: true,
            title: "Bulk Upload",
            isAdminMenu: true
          },
          {
            displayName: 'Webhook Configuration',
            iconName: 'feature',
            route: 'InSights/Home/webhook',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: InsightsInitService.showWebhookConfiguration,
            title: "WebHook",
            isAdminMenu: true
          },
          {
            displayName: 'Business Mapping',
            iconName: 'feature',
            route: 'InSights/Home/businessmapping',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: InsightsInitService.showBusinessMapping,
            title: "Business Mapping",
            isAdminMenu: true
          },
          {
            displayName: 'Group & Users',
            iconName: 'feature',
            route: 'InSights/Home/accessGroupManagement',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: true,
            title: "Group & Users",
            isAdminMenu: true
          },
          {
            displayName: 'Co-Relation Builder',
            iconName: 'feature',
            route: 'InSights/Home/relationship-builder',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: true,
            title: "Relationship-Builder",
            isAdminMenu: true
          },
          {
            displayName: 'Logo Setting',
            iconName: 'feature',
            route: 'InSights/Home/logoSetting',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: true,
            title: "Logo Setting",
            isAdminMenu: true
          },
          {
            displayName: 'Data Archival',
            iconName: 'feature',
            route: 'InSights/Home/dataarchiving',
            isToolbarDisplay: InsightsInitService.enableInsightsBranding,
            showMenu: true,
            title: "Data Archival",
            isAdminMenu: true
          }
        ]
      }
    ];
    //console.log(this.navItems);
  }

  loadBottomMenuItem() {
    this.navItemsBottom = [
      {
        displayName: 'About',
        iconName: 'info',
        isToolbarDisplay: false,
        showIcon: false,
        title: "About",
        isAdminMenu: false
      }, {
        displayName: 'Help',
        iconName: 'help',
        isToolbarDisplay: false,
        showIcon: false,
        title: "Help",
        isAdminMenu: false
      }, {
        displayName: 'Logout',
        iconName: 'logout',
        route: 'login',
        isToolbarDisplay: false,
        showIcon: true,
        title: "Logout",
        isAdminMenu: false
      }
    ];
  }

  getNavItemsByFilter() {
    return this.navItems.filter(x => x.showMenu == true);
  }

  public logout(): void {
    if(InsightsInitService.ssoEnabled){
      this.router.navigate(['/logout/2']);
    }else{
      this.router.navigate(['/logout/1']);
    }
  }

  public about(): void {
    var self = this;
    let aboutDialogRef = this.dialog.open(AboutDialog, {
      panelClass: 'healthcheck-show-details-dialog-container',
      height: '50%',
      width: '30%',
      disableClose: true,
    });
  }

  switchOrganizations(orgId, route, orgName, selectedItem) {
    var self = this;
    //console.log("In switch organization " + JSON.stringify(this.currentUserOrgs));
    self.defaultOrg = orgId;
    self.grafanaService.switchUserOrg(orgId).then(function (switchorgResponseData) {
      if (switchorgResponseData != null && switchorgResponseData.status == 'success') {
        self.selectedOrg = (selectedItem == undefined ? '' : selectedItem.displayName);
        self.selectedOrgName = self.getSelectedOrgName(self.selectedOrg);
        var grafanaCurrentOrgRole;
        for (let orgData of self.currentUserOrgsArray) {
          if (orgData.orgId == orgId) {
            grafanaCurrentOrgRole = orgData.role;
            self.userRole = orgData.role
          }
        }
        console.log(" grafanaCurrentOrgRole " + grafanaCurrentOrgRole + " orgId " + orgId);
        if (grafanaCurrentOrgRole === 'Admin') {
          self.showAdminTab = true;
        } else {
          self.showAdminTab = false;
        }
        self.dataShare.setOrgAndRole(orgName, orgId, self.userRole);
        self.cookieService.set('grafanaRole', grafanaCurrentOrgRole);
        self.cookieService.set('grafanaOrg', orgId);
        self.router.navigateByUrl(route, { skipLocationChange: true });
      } else {
        this.messageDialog.showApplicationsMessage(" Error while Organizantion change ,Please try again later ", "ERROR");
      }
    });
  }

  showLandingPage() {
    // console.log("ByUrl " + this.router.url);
    // console.log(this.router.isActive(this.router.url, true))
    this.router.navigate(['InSights/Home'], { skipLocationChange: true });
    this.displayLandingPage = true;
    this.isToolbarDisplay = InsightsInitService.enableInsightsBranding;
  }

  getSelectedOrgName(orgSelectedName): String {
    var orgName: String = "";
    if (orgSelectedName != undefined && orgSelectedName.length > 16) {
      orgName = (orgSelectedName.substring(0, 16)) + '..';
    } else {
      orgName = (orgSelectedName);
    }
    return orgName;
  }
}
