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
import { Component, OnInit, ViewChild } from '@angular/core';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { DomSanitizer, BrowserModule, SafeUrl, SafeResourceUrl } from '@angular/platform-browser';
import { UserOnboardingService } from '@insights/app/modules/user-onboarding/user-onboarding-service';
import { MatTableDataSource, MatSort, MatPaginator, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ConfirmationMessageDialog } from '@insights/app/modules/application-dialog/confirmation-message-dialog';
import { AddGroupMessageDialog } from '@insights/app/modules/user-onboarding/add-group-message-dialog';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';

@Component({
  selector: 'app-user-onboarding',
  templateUrl: './user-onboarding.component.html',
  styleUrls: ['./user-onboarding.component.css', './../home.module.css']
})
export class UserOnboardingComponent implements OnInit {

  mainContentMinHeightWoSbTab: string = 'min-height:' + (window.innerHeight - 146 - 48) + 'px';
  iframeStyleAdd = "{'height': 1500 +'px '+ '!important' }";
  userListUrl: SafeResourceUrl;
  framesize: any;
  showThrobber: boolean = false;
  adminOrgDataArray = [];
  readOnlyOrg: boolean = false;
  selectedUser: any;
  oldSelectedUser: any;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  //userDataSource: any = [];
  userDataSource = new MatTableDataSource<any>();
  MAX_ROWS_PER_TABLE = 10;
  displayedColumns = [];
  isbuttonenabled: boolean = false;
  isSaveEnable: boolean = false;
  showDetail: boolean = false;
  displayAccessGroupDetail: boolean = false;
  accessGroupName: String = "";
  grafanaUrl: String = "";
  showApplicationMessage: String = "";
  selectedAdminOrg: any;
  isSelectedUserId: any = -1;
  roleRecord = [
    { value: 'Editor', name: 'Editor' },
    { value: 'Admin', name: 'Admin' },
    { value: 'Viewer', name: 'Viewer' }
  ];

  constructor(private userOnboardingService: UserOnboardingService, private sanitizer: DomSanitizer,
    public dialog: MatDialog, public messageDialog: MessageDialogService) {
    var self = this;

    this.framesize = window.frames.innerHeight;

    var receiveMessage = function (evt) {
      var height = parseInt(evt.data);
      if (!isNaN(height)) {
        self.framesize = (evt.data + 20);
      }
    }
    window.addEventListener('message', receiveMessage, false);
    this.getApplicationDetail();
  }

  ngOnInit() {
  }

  ngAfterViewInit() {
    //console.log("In ngAfterViewInit")
    this.userDataSource.paginator = this.paginator;
  }

  async getApplicationDetail() {
    this.adminOrgDataArray = [];

    let adminOrgsResponse = await this.userOnboardingService.getCurrentUserOrgs();
    //console.log(adminOrgsResponse);
    if (adminOrgsResponse.data != undefined && adminOrgsResponse.status == "success") {
      for (var org in adminOrgsResponse.data) {
        if ((adminOrgsResponse.data[org].role) === 'Admin') {
          this.adminOrgDataArray.push(adminOrgsResponse.data[org]);
        }
      }
      this.isSaveEnable = false;
    }
  }

  loadUsersInfo(selectedAdminOrg) {
    this.isSaveEnable = false;
    this.showThrobber = true;
    var self = this;
    self.userDataSource = new MatTableDataSource();
    this.userOnboardingService.getOrganizationUsers(selectedAdminOrg.orgId).then(function (usersResponseData) {
      if (usersResponseData.data != undefined && usersResponseData.status == "success") {
        //console.log(usersResponseData.data);
        self.showDetail = true;
        self.showThrobber = false;
        self.displayedColumns = ['radio', 'Login', 'Email', 'Seen', 'Role'];
        //console.log(usersResponseData.data);
        self.userDataSource.data = usersResponseData.data; //new MatTableDataSource( )
        self.userDataSource.paginator = self.paginator;
        //console.log(self.userDataSource);
        //console.log(self.userDataSource.data);
      } else {
        self.messageDialog.showApplicationsMessage("Unable to load data", "WARN");
      }
    });
  }

  statusEdit(element) {
    //console.log("After radio check " + JSON.stringify(element) + "" + this.isSaveEnable);
    if (element != undefined) {
      this.oldSelectedUser = this.selectedUser;
      if (this.isSaveEnable) {
        var title = "Cancel Changes";
        var dialogmessage = "Are you sure you want to discard your changes?";
        const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "30%");
        dialogRef.afterClosed().subscribe(result => {
          if (result == 'yes') {
            this.selectedUser = undefined;
            this.isSelectedUserId = -1;
            this.loadUsersInfo(this.selectedAdminOrg);
          } else {
            this.selectedUser = this.oldSelectedUser;
          }
        });
      } else {
        this.isbuttonenabled = true;
      }
    }
  }

  editUserData() {
    //console.log(this.selectedUser.userId);
    this.isSaveEnable = true;
    this.isSelectedUserId = this.selectedUser.userId;
  }

  deleteOrgUser() {
    //console.log("result " + this.selectedUser.login);
    if (this.selectedUser != undefined) {
      var self = this;
      var title = "Delete User";
      var dialogmessage = "Are you sure we want to delete this <b> " + this.selectedUser.login + " </b> user from organization ?";
      const dialogRef = self.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "30%");
      dialogRef.afterClosed().subscribe(result => {
        //console.log(result);
        if (result == 'yes') {
          self.userOnboardingService.deleteUserOrg(this.selectedUser.orgId, this.selectedUser.userId, this.selectedUser.role)
            .then(function (deleteResponse) {
              if (deleteResponse.message = "User removed from organization") {
                self.isSaveEnable = false;
                self.showApplicationMessage = deleteResponse.message;
                self.messageDialog.showApplicationsMessage(deleteResponse.message, "SUCCESS");
              } else {
                self.showApplicationMessage = "Unable to update user Data";
                self.messageDialog.showApplicationsMessage("Unable to Delete user Data", "WARN");
              }
            });
          self.loadUsersInfo(this.selectedAdminOrg);
        }
        this.loadUsersInfo(this.selectedAdminOrg);
      });
    }
  }

  async saveData() {
    //console.log(this.selectedUser);
    //console.log(" Organization " + "  " + this.selectedAdminOrg)
    let editResponse = await this.userOnboardingService.editUserOrg(this.selectedUser.orgId, this.selectedUser.userId, this.selectedUser.role);
    if (editResponse.message = "Organization user updated") {
      this.isSaveEnable = false;
      this.showApplicationMessage = " Role of <b> " + this.selectedUser.login + " </b> have been updated successfully to <b> " + this.selectedUser.role + " </b> in <b> " + this.selectedAdminOrg.name + " </b>";
      this.messageDialog.showApplicationsMessage(this.showApplicationMessage, "SUCCESS");
    } else {
      this.showApplicationMessage = "Unable to update user Data";
      this.messageDialog.showApplicationsMessage(this.showApplicationMessage, "WARN");
    }
    this.selectedUser = undefined;
    this.isSelectedUserId = -1;
  }

  applyFilter(filterValue: string) {
    this.userDataSource.filter = filterValue.trim().toLowerCase();
  }

  displayaccessGroupCreateField() {
    this.displayAccessGroupDetail = !this.displayAccessGroupDetail;
    if (this.accessGroupName != undefined) {
      var self = this;
      const dialogRef = this.dialog.open(AddGroupMessageDialog, {
        panelClass: 'DialogBox',
        width: '50%',
        height: '50%',
        disableClose: true,
        data: {
        }
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result != undefined && result != 'no') { //'yes'
          self.userOnboardingService.createOrg(result)
            .then(function (createOrgResponse) {
              if (createOrgResponse.message = "Organization created") {
                self.isSaveEnable = false;
                self.showApplicationMessage = createOrgResponse.message;
                self.messageDialog.showApplicationsMessage(createOrgResponse.message, "SUCCESS");
              } else {
                self.showApplicationMessage = "Unable create Organization";
                self.messageDialog.showApplicationsMessage("Unable to Create Organization", "WARN");
              }
            });
          self.loadUsersInfo(this.selectedAdminOrg);
          setTimeout(() => {
            self.showApplicationMessage = "";
          }, 2000);
        }
      });
    }
    setTimeout(() => {
      this.showApplicationMessage = "";
    }, 2000);
  }

  addGlobalUser() {

  }
}
