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
import { MatDialog } from '@angular/material/dialog';
import { ImageHandlerService } from '@insights/common/imageHandler.service';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { ServerConfigurationService } from '@insights/app/modules/server-configuration/server-configuration-service';
import { MenuItem } from '@insights/app/modules/home/menu-item';
import { OverlayContainer } from '@angular/cdk/overlay';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
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

  @ViewChild('sidenav', { static: true }) sidenav: ElementRef;
  isExpanded = true;
  element: HTMLElement;
  userName: String = '';
  userDisplayName: String = '';
  timeZone: String = '';
  userRole: String = '';
  userCurrentOrg: string = '';
  isToolbarDisplay: boolean = InsightsInitService.enableInsightsToolbar;
  isValidUser: boolean = false;
  showLogoutButton: boolean = true;
  iframeStyle = 'width:100%; height:500px;';
  iframeWidth = window.innerWidth - 20;
  iframeHeight = window.innerHeight;
  grafanaResponse: any;
  expanded: boolean = false;
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
  currentUserDetail: any;
  year: any;
  aboutPageURL = "https://onedevops.atlassian.net/wiki/spaces/OI/pages/218936/Release+Notes";
  helpPageURL = "https://onedevops.atlassian.net/wiki/spaces/OI/overview";
  isServerConfigAvailable: boolean = false;
  menuItem : MenuItem;
  loginName: any;
  toggleDark: string = "Dark";
  themeOptions: string = "theme-light";
  storedTheme: any = this.dataShare.getTheme();
  isValid = true;
  isLight: boolean = false;

  selectedMenu:NavItem ;

  constructor(private grafanaService: GrafanaAuthenticationService,
    private cookieService: CookieService, private config: InsightsInitService,
    public router: Router, private dataShare: DataSharedService,public overlayContainer:OverlayContainer,
    private dialog: MatDialog, private imageHandeler: ImageHandlerService,
    public messageDialog: MessageDialogService, public serverconfigService: ServerConfigurationService
    ) {
    console.log("Home page constructer ");
    if (this.depth === undefined) {
      this.depth = 0;
    }

    this.menuItem = new MenuItem(dataShare);
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
    this.showLogoutButton = this.dataShare.getlogoutDisplay();
    console.log(" showLogoutButton " + this.showLogoutButton)

    this.getInformationFromGrafana();
    this.dataShare.setTheme(this.themeOptions);
    this.storedTheme = this.dataShare.getTheme();
    document.documentElement.setAttribute('data-theme', this.themeOptions);
  }

  ngOnInit() {
    console.log("Home page constructer nginit ");
    this.loadCustomerLogo();
    const overlayClasses=this.overlayContainer.getContainerElement().classList;
    const classToRemove=Array.from(overlayClasses).filter((item:String) => item.includes('theme'));
    overlayClasses.add(this.storedTheme);
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow.split(/\s/).reduce((response, word) => response += word.slice(0, 1), '')
    this.year = this.dataShare.getCurrentYear();
    this.grafanaService.serverConfigSubject.subscribe(res => {
      if (res === 'RELOAD_MENU') {
        this.getServerConfigInfo();
      }
    })
  }

  onMenuClick() {
    this.isExpanded = !this.isExpanded
  }

  loadCustomerLogo() {
    console.log("In Customer logo method");
    this.insightsCustomerLogo = this.dataShare.getCustomerLogo();
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
    this.navItemsBottom = this.menuItem.loadBottomMenuItem();
    this.currentUserWithOrgs = await this.grafanaService.getCurrentUserWithOrgs();
    if (this.currentUserWithOrgs != undefined && this.currentUserWithOrgs.data != undefined) {
      var userDetail = self.currentUserWithOrgs.data.userDetail.name;
      this.dataShare.setLoginName(self.currentUserWithOrgs.data.userDetail.login);
      if(userDetail == undefined || userDetail == ""){
        userDetail = this.dataShare.getLoginName();
      }
      this.dataShare.setUserName(userDetail);

      this.userDisplayName = this.dataShare.getCustomizeName(userDetail);
      this.userCurrentOrg = this.currentUserWithOrgs.data.userDetail.orgId;
      this.currentUserOrgsArray = this.currentUserWithOrgs.data.orgArray
      this.dataShare.setUserOrgArray(this.currentUserOrgsArray);
      for (let orgData of this.currentUserOrgsArray) {
        if (orgData.orgId == this.userCurrentOrg) {
          this.selectedOrg = orgData.name;
          this.userRole = orgData.role;
          this.selectedOrgName = this.dataShare.getCustomizeName(this.selectedOrg);
        }
      }
      this.dataShare.setOrgAndRole(self.selectedOrg, self.userCurrentOrg, self.userRole);
      this.cookieService.set('grafanaRole', self.userRole.toString());
      this.cookieService.set('grafanaOrg', self.userCurrentOrg);

      this.menuItem.loadorganizations();
    } else {
      console.log(" user and user organization data is not valid  ")
    }
    this.getServerConfigInfo();
  }

   getServerConfigInfo() {
    var self = this;
     this.serverconfigService.getServerConfigStatus().then( function (serverConfigResponse) {
      console.log(serverConfigResponse);
      self.isServerConfigAvailable = serverConfigResponse.data.isServerConfigAvailable;
      self.navItems = self.menuItem.loadMenuItem();
      console.log( self.navItems)
      if(self.isServerConfigAvailable){
        self.loadCustomerLogo();
        self.router.navigateByUrl('/InSights/Home/landingPage/' + self.dataShare.getOrgId(), { skipLocationChange: true, replaceUrl: true });
        let org=self.dataShare.getOrgName();
        self.dataShare.setCurrOrg(self.dataShare.getOrgName())
      }else{
        self.router.navigateByUrl('/InSights/Home/server-configuration' , { skipLocationChange: true, replaceUrl: true });
      }
    });
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

  getNavItemsByFilter() {
    return this.navItems.filter(x => x.showMenu == true);
  }

  getNavItemsBottomByFilter() {
    return this.navItemsBottom.filter(x => x.showMenu == true);
  }

  public logout(): void {
    this.dataShare.logoutInitilize();
  }

  public about(): void {
    var self = this;
    let aboutDialogRef = this.dialog.open(AboutDialog, {
      panelClass: 'custom-dialog-container',
      height: '55%',
      width: '60%',
      disableClose: true,
    });
  }

  switchOrganizations(orgId, route, orgName, selectedItem) {
    var self = this;
    self.defaultOrg = orgId;
    self.grafanaService.switchUserOrg(orgId).then(function (switchorgResponseData) {
      console.log(switchorgResponseData);
      if (switchorgResponseData != null && switchorgResponseData.status == 'success') {
        self.selectedOrg = (selectedItem == undefined ? '' : selectedItem.displayName);
        self.selectedOrgName = self.dataShare.getCustomizeName(self.selectedOrg);
        var token = switchorgResponseData.data.jtoken;
        if(token != ""){
          console.log(" previous token "+self.dataShare.getAuthorizationToken());
          console.log(" new token "+token);
          self.dataShare.setAuthorizationToken(token);
        }
        var grafanaCurrentOrgRole;
        for (let orgData of self.currentUserOrgsArray) {
          if (orgData.orgId == orgId) {
            grafanaCurrentOrgRole = orgData.role;
            self.userRole = orgData.role
          }
        }
        console.log(" grafanaCurrentOrgRole " + grafanaCurrentOrgRole + " orgId " + orgId);
        self.dataShare.setOrgAndRole(orgName, orgId, grafanaCurrentOrgRole);
        self.cookieService.set('grafanaRole', grafanaCurrentOrgRole);
        self.cookieService.set('grafanaOrg', orgId);
        self.navItems = self.menuItem.loadMenuItem();
        //console.log( self.navItems)
        self.router.navigateByUrl(route, { skipLocationChange: true });
      } else {
        this.messageDialog.openSnackBar(" Error while Organizantion change ,Please try again later ", "error");
      }
    });
  }

  showLandingPage() {
    var self = this;
    self.router.navigateByUrl('/InSights/Home/landingPage/' + self.dataShare.getOrgId(), { skipLocationChange: true });
    self.displayLandingPage = false;
    self.isToolbarDisplay = InsightsInitService.enableInsightsToolbar;
    this.menuItem.removeParentClassCSS();
  }

  onTogglingTheme(): void{
    if(this.toggleDark == 'Light'){
      this.overlayContainer.getContainerElement().classList.remove('theme-dark');
      this.overlayContainer.getContainerElement().classList.add('theme-light');
      this.toggleDark = 'Dark';
      this.themeOptions = 'theme-light';
      console.log("Inside dark mode setter theme " + this,this.themeOptions);
    }else{
      this.overlayContainer.getContainerElement().classList.remove('theme-light');
      this.overlayContainer.getContainerElement().classList.add('theme-dark');
      this.toggleDark = 'Light';
      this.themeOptions = 'theme-dark';
      console.log("Inside Light mode setter theme " + this,this.themeOptions);
    }
    this.dataShare.setTheme(this.themeOptions);
    this.storedTheme = this.dataShare.getTheme();
  }
}
