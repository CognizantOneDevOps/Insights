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

import { Component, ViewChild, HostBinding, Input, ElementRef, ViewEncapsulation, AfterViewInit, OnInit } from '@angular/core';
import { GrafanaAuthenticationService } from '@insights/common/grafana-authentication-service';
import { CookieService } from 'ngx-cookie-service';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { Router, NavigationExtras } from '@angular/router';
import { NavItem } from '@insights/app/modules/home/nav-item';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { MatTableModule } from '@angular/material/table';
import { DataSharedService } from '@insights/common/data-shared-service';
import { AboutDialog } from '@insights/app/modules/about/about-show-popup';
//import { ShowDetailsDialog } from '@insights/app/modules/healthcheck/healthcheck-show-details-dialog'
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';



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
  isToolbarDisplay: boolean = true;
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
  currentUserOrgs: any;
  userResponse: any;
  insightsCustomerLogo: any;
  aboutPageURL = "https://onedevops.atlassian.net/wiki/spaces/OI/pages/218936/Release+Notes";
  helpPageURL = "https://onedevops.atlassian.net/wiki/spaces/OI/overview";
  ngOnInit() {
    this.insightsCustomerLogo = this.dataShare.getCustomerLogo();
    if (this.insightsCustomerLogo == "DefaultLogo") {
      this.insightsCustomerLogo = "";//icons/svg/homePage/Customer_Logo.png
    }
  }

  constructor(private grafanaService: GrafanaAuthenticationService,
    private cookieService: CookieService, private config: InsightsInitService,
    public router: Router, private dataShare: DataSharedService, private dialog: MatDialog) {
    //console.log("in home on constructor init ");
    //router.onSameUrlNavigation = 'reload';
    this.displayLandingPage = true;
    if (this.depth === undefined) {
      this.depth = 0;
    }
    this.grafanaService.validateSession();
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

  onMenuClick() {
    this.isExpanded = !this.isExpanded
  }

  public async getInformationFromGrafana() {
    let currentUserResponce: any;
    let self = this;
    this.userResponse = await this.grafanaService.getUsers()
    //console.log(" In user response " + JSON.stringify(this.userResponse));
    if (this.userResponse.data != undefined) {
      self.userName = self.userResponse.data.name != undefined ? self.userResponse.data.name.replace(/['"]+/g, '') : "";
      self.userCurrentOrg = self.userResponse.data.orgId;
      self.dataShare.setUserName(self.userName);
    }
    this.currentUserOrgs = await this.grafanaService.getCurrentUserOrgs();
    //console.log("In load organization " + JSON.stringify(this.currentUserOrgs));
    if (this.currentUserOrgs.data != undefined) {
      for (let orgData of this.currentUserOrgs.data) {
        if (orgData.orgId == self.userCurrentOrg) {
          self.selectedOrg = orgData.name;
          self.userRole = orgData.role;
          self.selectedOrgName = this.getSelectedOrgName(self.selectedOrg);
        }
      }
      self.dataShare.setOrgAndRole(self.selectedOrg, self.userCurrentOrg, self.userRole);
      //console.log(self.userRole.toString() + "   " + self.userCurrentOrg);
      self.cookieService.set('grafanaRole', self.userRole.toString());
      self.cookieService.set('grafanaOrg', self.userCurrentOrg);
    } else {
      //console.log(" in else of this.currentUserOrgs.data != undefined ")
      self.router.navigate(['/login']);
    }
    if (self.userRole === 'Admin') {
      self.showAdminTab = true;
    } else {
      self.showAdminTab = false;
    }
    this.loadorganizations();
    this.loadMenuItem();
  }



  public async loadorganizations() {
    var self = this;

    if (this.currentUserOrgs.data != undefined) {
      var orgDataArray = this.currentUserOrgs.data;
      this.orgList = orgDataArray;
      if (this.userResponse.data != undefined) {
        var grafanaOrgId = this.userResponse.data.orgId;
        //console.log(grafanaOrgId);
      }
      for (var key in this.orgList) {
        var orgDtl = this.orgList[key];
        var navItemobj = new NavItem();
        navItemobj.displayName = orgDtl.name;
        navItemobj.iconName = 'grafanaOrg';
        navItemobj.route = 'InSights/Home/grafanadashboard/' + orgDtl.orgId;
        navItemobj.isToolbarDisplay = true;//false
        navItemobj.showIcon = false;
        navItemobj.isAdminMenu = false;
        navItemobj.orgId = orgDtl.orgId;
        navItemobj.title = orgDtl.name;
        this.navOrgList.push(navItemobj);
      }
    }
  }


  onItemSelected(item: NavItem) {
    this.selectedItem = item;
    this.displayLandingPage = false;
    this.isToolbarDisplay = item.isToolbarDisplay

    if (!item.children || !item.children.length) {
      if (item.iconName == 'grafanaOrg') {
        this.selectedOrg = (this.selectedItem == undefined ? '' : this.selectedItem.displayName);
        this.selectedOrgName = this.getSelectedOrgName(this.selectedOrg);
        this.switchOrganizations(item.orgId, item.route, this.selectedOrgName);
      } else if (item.displayName == 'About') {
        // window.open(this.aboutPageURL, "_blank");
        let aboutDialogRef = this.dialog.open(AboutDialog, {
          panelClass: 'healthcheck-show-details-dialog-container',
          height: '50%',
          width: '30%',
          disableClose: true,
        });
        /* aboutDialogRef.afterClosed().subscribe(result => {
           if (result == 'yes') {
             this.router.navigateByUrl('InSights/Home/healthcheck', { skipLocationChange: true });
           }
         });*/
      } else if (item.displayName == 'Help') {
        window.open(this.helpPageURL, "_blank");
      } else if (item.displayName == 'Logout') {
        this.logout();
      } else {
        this.router.navigateByUrl(item.route, { skipLocationChange: true });
      }
    } /*else {
      if (item.displayName == 'grafana') {
        console.log("in grafana");
      }
    }*/
  }

  public loadMenuItem() {
    this.navItems = [
      {
        displayName: 'Dashboard Groups',
        iconName: 'feature',
        isAdminMenu: false,
        showMenu: true,
        title: "Click on Organization to see various Org's Dashboards",
        isToolbarDisplay: true,//false
        children: this.navOrgList
        /*[
          {
            displayName: 'Grafana: ',
            iconName: 'grafana',
            isAdminMenu: false,
            isToolbarDisplay: false,
            showMenu: true,
            children: this.navOrgList
          },
          {
            displayName: 'ML Capabilities',
            iconName: 'feature',
            route: 'InSights/Home/grafanadashboard/100',
            isToolbarDisplay: true,
            isAdminMenu: true
          },
          {
            displayName: 'InSights',
            iconName: 'feature',
            route: 'InSights/Home/grafanadashboard/900',
            isToolbarDisplay: true,
            isAdminMenu: true
          },
          {
            displayName: 'DevOps Maturity',
            iconName: 'feature',
            route: 'InSights/Home/grafanadashboard/300',
            isToolbarDisplay: true,
            isAdminMenu: true
          }
        ]*/
      },
      {
        displayName: 'Audit Reporting',
        iconName: 'feature',
        route: 'InSights/Home/blockchain',
        isToolbarDisplay: true,
        showMenu: InsightsInitService.showAuditReporting,
        title: "Audit Reporting",
        isAdminMenu: true
      },
      {
        displayName: 'Playlist',
        iconName: 'feature',
        route: 'InSights/Home/playlist',
        isToolbarDisplay: true,//false
        showMenu: true,
        title: "Playlist",
        isAdminMenu: false
      },
      {
        displayName: 'Data Dictionary',
        iconName: 'datadictionary',
        route: 'InSights/Home/datadictionary',
        isToolbarDisplay: true,
        showMenu: true,
        title: "Data Dictionary",
        isAdminMenu: false
      },
      {
        displayName: 'Health Check',
        iconName: 'feature',
        route: 'InSights/Home/healthcheck',
        isToolbarDisplay: true,
        showMenu: true,
        title: "Health Check",
        isAdminMenu: true
      },
      {
        displayName: 'Configuration',
        iconName: 'admin',
        isToolbarDisplay: true,
        isAdminMenu: true,
        title: "Configuration",
        showMenu: true,
        children: [
          {
            displayName: 'Agent Management',
            iconName: 'feature',
            route: 'InSights/Home/agentmanagement',
            isToolbarDisplay: true,
            showMenu: true,
            title: "Agent Management",
            isAdminMenu: true
          },
          {
            displayName: 'Business Mapping',
            iconName: 'feature',
            route: 'InSights/Home/businessmapping',
            isToolbarDisplay: true,
            title: "Business Mapping",
            isAdminMenu: true
          },
          {
            displayName: 'Group & Users',
            iconName: 'feature',
            route: 'InSights/Home/accessGroupManagement',
            isToolbarDisplay: true,
            showMenu: true,
            title: "Group & Users",
            isAdminMenu: true
          },
          {
            displayName: 'Logo Setting',
            iconName: 'feature',
            route: 'InSights/Home/logoSetting',
            isToolbarDisplay: true,
            showMenu: true,
            title: "Logo Setting",
            isAdminMenu: true
          },
          {
            displayName: 'Data Archival',
            iconName: 'feature',
            route: 'InSights/Home/dataarchiving',
            isToolbarDisplay: true,
            showMenu: true,
            title: "Data Archival",
            isAdminMenu: true
          }
        ]
      }
    ];
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
        route: 'login', //loggedout
        isToolbarDisplay: false,
        showIcon: true,
        title: "Logout",
        isAdminMenu: false
      }
    ];
    //console.log(this.navItems);
  }

  getNavItemsByFilter() {
    return this.navItems.filter(x => x.showMenu == true);
  }

  public logout(): void {
    var self = this;
    var uniqueString = "grfanaLoginIframe";
    var iframe = document.createElement("iframe");
    iframe.id = uniqueString;
    document.body.appendChild(iframe);
    iframe.style.display = "none";
    iframe.contentWindow.name = uniqueString;
    // construct a form with hidden inputs, targeting the iframe
    var form = document.createElement("form");
    form.target = uniqueString;
    this.config.getGrafanaHost1().then(function (response) {
      form.action = InsightsInitService.grafanaHost + "/logout";
      form.method = "GET";
      document.body.appendChild(form);
      form.submit();
    });
    this.grafanaService.logout()
      .then(function (data) {
        //console.log(data);
      });
    this.deleteAllPreviousCookies();
    this.router.navigate(['/login']);
  }

  switchOrganizations(orgId, route, orgName) {
    var self = this;
    //console.log("In switch organization " + JSON.stringify(this.currentUserOrgs));
    self.defaultOrg = orgId;
    self.grafanaService.switchUserOrg(orgId).then(function (switchorgResponseData) {
      if (switchorgResponseData != null && switchorgResponseData.status === 'success') {
        var grafanaCurrentOrgRole;
        for (let orgData of self.currentUserOrgs.data) {
          if (orgData.orgId == orgId) {
            grafanaCurrentOrgRole = orgData.role;
            self.userRole = orgData.role
          }
        }
        //console.log(" grafanaCurrentOrgRole " + grafanaCurrentOrgRole + " orgId " + orgId);
        if (grafanaCurrentOrgRole === 'Admin') {
          self.showAdminTab = true;
        } else {
          self.showAdminTab = false;
        }
        self.dataShare.setOrgAndRole(orgName, orgId, self.userRole);
        self.cookieService.set('grafanaRole', grafanaCurrentOrgRole);
        self.cookieService.set('grafanaOrg', orgId);

        self.router.navigateByUrl(route, { skipLocationChange: true });
      }
    });
  }

  deleteAllPreviousCookies(): void {
    let allCookies = this.cookieService.getAll();
    for (let key of Object.keys(allCookies)) {
      this.cookieService.delete(key);
    }
  }

  showLandingPage() {
    // console.log("ByUrl " + this.router.url);
    // console.log(this.router.isActive(this.router.url, true))
    this.router.navigate(['InSights/Home'], { skipLocationChange: true });
    this.displayLandingPage = true;
    this.isToolbarDisplay = true;
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
