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
import { Component, OnInit, ViewChild } from "@angular/core";
import { RestCallHandlerService } from "@insights/common/rest-call-handler.service";
import {
  DomSanitizer,
  BrowserModule,
  SafeUrl,
  SafeResourceUrl,
} from "@angular/platform-browser";
import { UserOnboardingService } from "@insights/app/modules/user-onboarding/user-onboarding-service";
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatSort, Sort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { ConfirmationMessageDialog } from "@insights/app/modules/application-dialog/confirmation-message-dialog";
import { AddGroupMessageDialog } from "@insights/app/modules/user-onboarding/add-group-message-dialog";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import {
  Router,
  ActivatedRoute,
  ParamMap,
  NavigationExtras,
} from "@angular/router";
import {
  FormGroup,
  FormBuilder,
  Validators,
  FormControl,
  FormArray,
  NgForm,
} from "@angular/forms";
import { HomeComponent } from "@insights/app/modules/home/home.component";
import { InsightsUtilService } from "@insights/common/insights-util.service";

@Component({
  selector: "app-user-onboarding",
  templateUrl: "./user-onboarding.component.html",
  styleUrls: ["./user-onboarding.component.scss", "./../home.module.scss"],
})
export class UserOnboardingComponent implements OnInit {
  mainContentMinHeightWoSbTab: string =
    "min-height:" + (window.innerHeight - 146 - 48) + "px";
  iframeStyleAdd = "{'height': 1500 +'px '+ '!important' }";

  framesize: any;
  adduserSaveEnable: boolean = false;
  assignuserSaveEnable: boolean = false;
  showAddUserDetail: boolean = false;
  showAssignUserDetail: boolean = false;
  showCancel: boolean = false;
  showThrobber: boolean = false;
  adminOrgDataArray = [];
  orgNameArray = [];
  orgIdArray = [];
  userRolesArray = [];
  readOnlyOrg: boolean = false;
  assignUserData = {};
  role: any;
  descp: string;
  usrnme: string;
  searchUser: string;
  newresponse: string = "";
  email: string;
  names: string;
  isEmailIncorrect: boolean = false;
  isNameIncorrect: boolean = false;
  isUsernameIncorrect: boolean = false;
  isPasswordIncorrect: boolean = false;
  isRoleIncorrect: boolean = false;
  isOrgIncorrect: boolean = false;
  selectedUser: any;
  oldSelectedUser: any;
  addSelected: boolean = false;
  assignSelected: boolean = false;
  listFilter: any;
  searchValue: string = "";
  @ViewChild(MatPaginator) paginator: MatPaginator;
  userDataSource = new MatTableDataSource<any>();
  MAX_ROWS_PER_TABLE = 10;
  displayedColumns = [];
  showDetail2: boolean = true;
  addRadioSelected: boolean = false;
  assignRadioSelected: boolean = false;
  isbuttonenabled: boolean = false;
  isSaveEnable: boolean = false;
  showDetail: boolean = false;
  displayAccessGroupDetail: boolean = false;
  accessGroupName: String = "";
  showApplicationMessage: String = "";
  selectedAdminOrg: any;
  isSelectedUserId: any = -1;
  searchInput: any;
  usernamestore: any;
  rowcss: boolean = true;
  addForm: FormGroup;
  rows: FormArray;
  itemForm: FormGroup;
  searchOrgForUser: string;
  usernameRegex = new RegExp("^[a-zA-Z0-9_]*$");
  regex = new RegExp("(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$)");
  additionalProperties = [
    "name",
    "email",
    "username",
    "password",
    "role",
    "org",
  ];
  roleRecord = [
    { value: "Editor", name: "Editor" },
    { value: "Admin", name: "Admin" },
    { value: "Viewer", name: "Viewer" },
  ];
  emailRegex = /(\w{1})[\w.-]+@([\w.]+\w)/;
  loginRegex = /\S(?=\S{2})/g;
  dataValue :any = {};

  constructor(
    private fb: FormBuilder,
    public router: Router,
    private userOnboardingService: UserOnboardingService,
    private sanitizer: DomSanitizer,
    public dialog: MatDialog,
    public messageDialog: MessageDialogService,
    private dataShare: DataSharedService,
    public insightsUtil : InsightsUtilService,
  ) {
    var self = this;
    this.userForm();
    this.framesize = window.frames.innerHeight;
    var orgId2 = this.dataShare.getStoragedProperty("orgId");
    var receiveMessage = function (evt) {
      var height = parseInt(evt.data);
      if (!isNaN(height)) {
        self.framesize = evt.data + 20;
      }
    };
    window.addEventListener("message", receiveMessage, false);
    this.getApplicationDetail();
  }
  private newMethod() {}
  ngOnInit() {}
  onAddRow() {
    this.rows.push(this.createItemFormGroup(this.rowcss));
  }
  createItemFormGroup(rowcsss): FormGroup {
    this.rowcss = rowcsss;
    return this.fb.group({
      org: null,
      role: null,
    });
  }

  ngAfterViewInit() {
    this.userDataSource.paginator = this.paginator;
  }

  async getApplicationDetail() {
    this.adminOrgDataArray = [];
    let adminOrgsResponse =
      await this.userOnboardingService.getCurrentUserOrgs();
    if (
      adminOrgsResponse.data != undefined &&
      adminOrgsResponse.status == "success"
    ) {
      var orgId2 = this.dataShare.getStoragedProperty("orgId");

      for (var orgData of adminOrgsResponse.data) {
        if (orgData.role === "Admin") {
          this.adminOrgDataArray.push(orgData);
          if (orgId2 == orgData.orgId) {
            var record = orgData;

            this.selectedAdminOrg = record;
          }
        }
      }
      this.isSaveEnable = false;
    }

    this.loadUsersInfo(this.selectedAdminOrg);
  }

  loadUsersInfo(selectedAdminOrg) {
    this.isSaveEnable = false;
    this.showThrobber = true;
    var self = this;
    self.userDataSource = new MatTableDataSource();
    this.userOnboardingService
      .getOrganizationUsers(selectedAdminOrg.orgId)
      .then(function (usersResponseData) {
        if (
          usersResponseData.data != undefined &&
          usersResponseData.status == "success"
        ) {
          let decodedData = self.dataShare.decryptedData( usersResponseData.data);
          self.dataValue["data"] = JSON.parse(decodedData);

          self.showDetail = true;
          self.showThrobber = false;
          self.displayedColumns = [
            "radio",
            "UserName",
            "Login",
            "Email",
            "Role",
          ];

          self.userDataSource.data = self.dataValue["data"]; //new MatTableDataSource( )
          self.userDataSource.paginator = self.paginator;
        } else if (
          usersResponseData.message ==
          "Unable to get current org users,Permission denide "
        ) {
          const dialogRef = self.messageDialog.showApplicationsMessage(
            "User needs to be Grafana Admin to view this page.",
            "WARN"
          );
          self.showThrobber = false;
          dialogRef.afterClosed().subscribe((result) => {
            self.router.navigateByUrl(
              "/InSights/Home/landingPage/" + self.dataShare.getOrgId(),
              { skipLocationChange: true }
            );
          });
        } else {
          self.messageDialog.showApplicationsMessage(
            "Unable to load data",
            "WARN"
          );
        }
      });
  }

  statusEdit(element) {
    if (element != undefined) {
      this.oldSelectedUser = this.selectedUser;
      if (this.isSaveEnable) {
        var title = "Cancel Changes";
        var dialogmessage = "Are you sure you want to discard your changes?";
        const dialogRef = this.messageDialog.showConfirmationMessage(
          title,
          dialogmessage,
          "",
          "ALERT",
          "35%"
        );
        dialogRef.afterClosed().subscribe((result) => {
          if (result == "yes") {
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

  userForm() {
    this.rows = this.fb.array([]);
    for (let number of [1, 2, 3, 4, 5]) {
      if (number % 2 == 0) {
        this.rowcss = false;
      } else {
        this.rowcss = true;
      }
      this.rows.push(this.createItemFormGroup(this.rowcss));
    }
  }

  searchUserInAssign(searchOrgForUserAssign) {
    var self = this;
    this.userOnboardingService
      .getUsersOrganisation(searchOrgForUserAssign)
      .then(function (usersResponseData1) {
        if (usersResponseData1.data.length > 0) {
          for (var element of usersResponseData1.data) {
            self.newresponse = self.newresponse + element.name + ", ";
          }
          self.newresponse = self.newresponse.substring(
            0,
            self.newresponse.length - 2
          );
          self.newresponse =
            "User found and Organisations name are : " + self.newresponse;
        } else {
          self.newresponse =
            "User Found and not yet assigned to any Organization.";
        }

        if (usersResponseData1.data == "User Not Found") {
          self.messageDialog.openSnackBar("User not found", "error");
          self.newresponse = "";
        } else {
          self.messageDialog.openSnackBar(self.newresponse, "success");
          self.newresponse = "";
        }
      });
  }

  searchUserInAddUser(searchOrgForUserAssign) {
    var self = this;
    this.userOnboardingService
      .getUsersOrganisation(searchOrgForUserAssign)
      .then(function (usersResponseData1) {
        if (usersResponseData1.data.length > 0) {
          for (var element of usersResponseData1.data) {
            self.newresponse = self.newresponse + element.name + ", ";
          }
          self.newresponse = self.newresponse.substring(
            0,
            self.newresponse.length - 2
          );
          self.newresponse =
            "User Found.Please use Assign User Functionality for adding the user to different Orgs.";
        } else {
          self.newresponse =
            "User Found.Please use Assign User Functionality for adding the user to different Orgs.";
        }

        if (usersResponseData1.data == "User Not Found") {
          self.messageDialog.openSnackBar("User not found", "error");
          self.newresponse = "";
        } else {
          self.messageDialog.openSnackBar(self.newresponse, "success");
          self.newresponse = "";
        }
      });
  }

  clubProperties(jsonData, isArray) {
    if (isArray) {
      var length = jsonData.length;
      for (let i = 0; i < length; i++) {
        let propString = undefined;
        for (let key of Object.keys(jsonData[i])) {
          if (this.additionalProperties.indexOf(key) > -1) {
          } else {
            if (propString == undefined) {
              propString = key + " <b> : </b>" + jsonData[i][key];
            } else {
              propString +=
                "" + "<br>" + key + " <b> : </b>" + jsonData[i][key];
            }
          }
        }
        jsonData[i]["propertiesString"] = propString;
      }
    } else {
      let propString = undefined;
      for (let key of Object.keys(jsonData)) {
        if (this.additionalProperties.indexOf(key) > -1) {
        } else {
          if (propString == undefined) {
            propString = key + " <b> : </b>" + jsonData[key];
          } else {
            propString += "" + "<br>" + key + " <b> : </b>" + jsonData[key];
          }
        }
      }
      jsonData["propertiesString"] = propString;
    }
    return jsonData;
  }

  saveUser() {
    this.isEmailIncorrect = false;
    this.isUsernameIncorrect = false;
    this.isPasswordIncorrect = false;
    this.isNameIncorrect = false;
    this.isRoleIncorrect = false;

    var userBMparameter;
    var userPropertyList = {};

    userPropertyList["name"] = this.names;
    userPropertyList["email"] = this.email;
    userPropertyList["userName"] = this.usrnme;
    userPropertyList["password"] = this.descp;
    userPropertyList["role"] = this.role;
    userPropertyList["orgName"] = this.selectedAdminOrg.name;
    userPropertyList["orgId"] = this.selectedAdminOrg.orgId;

    userBMparameter = JSON.stringify(userPropertyList);

    var checkname = this.regex.test(this.email);
    var checkid = this.usernameRegex.test(this.usrnme);
    if (!checkid) {
      this.isUsernameIncorrect = true;
    }
    if (!checkname) {
      this.isEmailIncorrect = true;
    }
    if (this.usrnme == undefined) {
      this.isUsernameIncorrect = true;
    }
    if (this.descp == undefined) {
      this.isPasswordIncorrect = true;
    }
    if (this.names == undefined) {
      this.isNameIncorrect = true;
    }
    if (this.role == undefined) {
      this.isRoleIncorrect = true;
    }
    if (
      !this.isRoleIncorrect &&
      !this.isNameIncorrect &&
      !this.isPasswordIncorrect &&
      !this.isUsernameIncorrect &&
      !this.isEmailIncorrect
    ) {
      this.userOnboardingService
        .addUserInOrg(userBMparameter)
        .subscribe((data) => {
          if (data.status == "success") {
            var userResponse = JSON.parse(data.data).message;

            if (
              userResponse == "User created" ||
              userResponse == "Organization user updated" ||
              userResponse == "User added to organization"
            ) {
              this.messageDialog.openSnackBar(
                "User has been added.",
                "success"
              );
            } else if (
              userResponse == "Email already exists" ||
              userResponse == "Username already exists" ||
              userResponse == "User exists in currrent org with same role" ||
              userResponse == "Password is missing or too short"
            ) {
              this.messageDialog.openSnackBar(userResponse, "error");
            } else if (userResponse == "failed to create user") {
              this.messageDialog.openSnackBar(
                "Failed to create User.Please try again",
                "error"
              );
            } else if (
              (userResponse = "User exists in currrent org with different role")
            ) {
              var title = "ERROR";

              var dialogmessage =
                userResponse + ". Are you sure you want to update the role?";
              const dialogRef = this.messageDialog.showConfirmationMessage(
                title,
                dialogmessage,
                this.role,
                "ALERT",
                "45%"
              );
              dialogRef.afterClosed().subscribe((result) => {
                if (result == "yes") {
                  this.showDetail = true;
                  this.showAddUserDetail = false;
                }
              });
            }
          } else {
            this.messageDialog.openSnackBar(
              "Failed to create user.Please try again.",
              "error"
            );
          }
        });
    }
  }
  assignUser() {
    var requestjson = [];
    var orgArray = [];
    var repeatedOrgCount = 0;
    var missingRole = 0;
    var repeatedOrg = "";
    var userBMparameter;
    if (this.searchOrgForUser == undefined) {
      this.messageDialog.openSnackBar(
        "Please enter a Username or Login ID",
        "error"
      );
    } else {
      var orgcount = 0;
      for (let data of this.rows.value) {
        if (data.org != null) {
          orgcount = orgcount + 1;
          if (data.role != null) {
            orgArray.push(data.org.orgId);

            var firstindex = orgArray.indexOf(data.org.orgId);
            var lastindex = orgArray.lastIndexOf(data.org.orgId);
            if (lastindex == firstindex) {
              var orgAssignData = {};
              orgAssignData["orgName"] = data.org.name;
              orgAssignData["orgId"] = data.org.orgId;
              orgAssignData["roleName"] = data.role;
              orgAssignData["userName"] = this.searchOrgForUser;
              requestjson.push(orgAssignData);
            } else {
              repeatedOrg = repeatedOrg + data.org.name + ", ";
              repeatedOrgCount = repeatedOrgCount + 1;
            }
          } else {
            missingRole = missingRole + 1;
          }
        }
      }

      if (orgcount == 0) {
        this.messageDialog.openSnackBar("No Organisation selected", "error");
      } else if (missingRole > 0) {
        this.messageDialog.openSnackBar(
          "Please select Role for Organisations ",
          "error"
        );
      } else if (repeatedOrgCount > 0) {
        repeatedOrg = repeatedOrg.slice(0, -2);
        this.messageDialog.openSnackBar(
          "Repeated selection of Organisations : " + repeatedOrg,
          "error"
        );
      } else if (orgcount > 0 && repeatedOrgCount == 0 && missingRole == 0) {
        userBMparameter = JSON.stringify(requestjson);
        this.userOnboardingService
          .assignUser(userBMparameter)
          .subscribe((data) => {
            if (data.status == "success") {
              this.messageDialog.openSnackBar(data.data,"success");
            } else {
              this.messageDialog.openSnackBar(data.message, "error")
            }
          });
      }
    }
  }
  Refresh() {
    this.isSaveEnable = false;
    this.isbuttonenabled = false;
    this.adduserSaveEnable = false;
    this.addSelected = false;
    this.assignSelected = false;
    this.isEmailIncorrect = false;
    this.isNameIncorrect = false;
    this.isOrgIncorrect = false;
    this.isPasswordIncorrect = false;
    this.isUsernameIncorrect = false;
    this.isRoleIncorrect = false;
    this.showDetail2 = true;
    this.addRadioSelected = false;
    this.assignRadioSelected = false;
    this.assignuserSaveEnable = false;
    //this.descp = null;
    this.usrnme = null;
    this.email = null;
    this.names = null;
    this.role = null;
    this.searchOrgForUser = null;
    this.searchUser = null;
    this.showCancel = false;
    this.userForm();
  }
  adduserenableSave() {
    this.showAddUserDetail = true;
    this.adduserSaveEnable = true;
    this.addSelected = true;
    this.assignSelected = false;
    this.showDetail2 = true;
    this.addRadioSelected = true;
    this.assignRadioSelected = false;
    this.assignuserSaveEnable = false;
    this.searchOrgForUser = null;
    this.showCancel = true;
  }
  assignuserenableSave() {
    this.adduserSaveEnable = false;
    this.showAssignUserDetail = true;
    this.assignuserSaveEnable = true;
    this.assignSelected = true;
    this.addSelected = false;
    this.showDetail2 = true;
    this.assignRadioSelected = true;
    this.addRadioSelected = false;
    this.descp = undefined;
    this.usrnme = null;
    this.email = null;
    this.names = null;
    this.searchUser = null;
    this.showCancel = true;
  }

  searchData(searchUser, selectedAdminOrg) {
    var count = 0;
    var self = this;
    self.userDataSource = new MatTableDataSource();
    if (this.assignRadioSelected == true) {
      selectedAdminOrg.orgId = 1;
    }
    this.userOnboardingService
      .getOrganizationUsers(selectedAdminOrg.orgId)
      .then(function (usersResponseData) {
        if (
          usersResponseData.data != undefined &&
          usersResponseData.status == "success"
        ) {
          self.userDataSource.data = usersResponseData.data; //new MatTableDataSource( )
          self.userDataSource.paginator = self.paginator;
          for (var element of self.userDataSource.data) {
            var emailcheck = element.email;
            var usernamecheck = element.login;
            self.searchInput = searchUser;
            if (self.searchInput == emailcheck) {
              count = count + 1;
              break;
            } else if (self.searchInput == usernamecheck) {
              count = count + 1;
              break;
            }
          }
          if (count == 1) {
            var dialogmessage = " User already exists." + "<b>";
            self.messageDialog.openSnackBar(dialogmessage, "success");
          } else {
            self.messageDialog.openSnackBar("No User Found.", "error");
          }
        } else {
          self.messageDialog.showApplicationsMessage(
            "Unable to load data",
            "WARN"
          );
        }
      });
  }
  editUserData() {
    this.isSaveEnable = true;
    this.isbuttonenabled = false;
    this.isSelectedUserId = this.selectedUser.userId;
  }
  deleteOrgUser() {
    if (this.selectedUser != undefined) {
      var self = this;
      var title = "Delete User";
      var dialogmessage =
        "Are you sure we want to delete this <b> " +
        this.selectedUser.login +
        " </b> user from organization ?";
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "DELETE",
        "35%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          self.userOnboardingService
            .deleteUserOrg(
              this.selectedUser.orgId,
              this.selectedUser.userId,
              this.selectedUser.role
            )
            .then(function (deleteResponse) {
              if ((deleteResponse.message = "User removed from organization")) {
                self.isSaveEnable = false;
                self.showApplicationMessage = deleteResponse.message;
                self.messageDialog.openSnackBar(
                  deleteResponse.message,
                  "success"
                );
              } else {
                self.showApplicationMessage = "Unable to update user Data";
                self.messageDialog.showApplicationsMessage(
                  "Unable to Delete user Data",
                  "WARN"
                );
              }
            });
          this.redirectToLandingPage();
          self.loadUsersInfo(this.selectedAdminOrg);
        }
      });
    }
  }

  async saveData() {
    let editResponse = await this.userOnboardingService.editUserOrg(
      this.selectedUser.orgId,
      this.selectedUser.userId,
      this.selectedUser.role
    );
    if ((editResponse.message = "Organization user updated")) {
      this.isSaveEnable = false;
      this.showApplicationMessage =
        " Role of <b> " +
        this.selectedUser.login +
        " </b> have been updated successfully to <b> " +
        this.selectedUser.role +
        " </b> in <b> " +
        this.selectedAdminOrg.name +
        " </b>";
      this.messageDialog.openSnackBar(
        "" + this.showApplicationMessage,
        "success"
      );
    } else {
      this.showApplicationMessage = "Unable to update user Data";
      this.messageDialog.showApplicationsMessage(
        this.showApplicationMessage,
        "WARN"
      );
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
      var isSessionExpired = this.dataShare.validateSession();
      if (!isSessionExpired) {
        const dialogRef = this.dialog.open(AddGroupMessageDialog, {
          panelClass: "custom-dialog-container",
          height: "75%",
          width: "70%",
          disableClose: true,
          data: {},
        });

        dialogRef.afterClosed().subscribe((result) => {
          if (result != undefined && result != "no") {
            //'yes'
            self.userOnboardingService
              .createOrg(result)
              .then(function (createOrgResponse) {
                if ((createOrgResponse.message = "Organization created")) {
                  self.isSaveEnable = false;
                  self.showApplicationMessage = createOrgResponse.message;
                  self.messageDialog.openSnackBar(
                    createOrgResponse.message,
                    "success"
                  );
                  self.getApplicationDetail();
                } else {
                  self.showApplicationMessage = "Unable create Organization";
                  self.messageDialog.showApplicationsMessage(
                    "Unable to Create Organization",
                    "WARN"
                  );
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
    this.redirectToLandingPage();
  }
  addGlobalUser() {
    this.showAssignUserDetail = true;
    this.showAddUserDetail = true;
    this.showDetail = false;
    this.showDetail2 = false;
  }

  /*Method to redirect to Configuration | Group & Users Management page*/
  redirectToLandingPage() {
    this.Refresh();
    this.loadUsersInfo(this.selectedAdminOrg);
    this.showAssignUserDetail = false;
    this.showAddUserDetail = false;
    this.showDetail = true;
    this.showDetail2 = true;
    this.showCancel = false;
  }

  trackEvent = function (event) {
    if (event.key === "Enter") {
      this.searchData(this.searchUser, this.selectedAdminOrg);
    }
  };

  trackEvent1 = function (event) {
    if (event.key === "Enter") {
      this.searchData(this.searchOrgForUser, this.selectedAdminOrg);
    }
  };


  onChange(newVal) {
    this.descp = newVal;
  }

  sortData(sort: Sort) {
    const data = this.dataValue["data"].slice();
    if (!sort.active || sort.direction === '') {
      this.userDataSource.data = data;
      return;
    }

    this.userDataSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      return this.insightsUtil.compare(a[sort.active], b[sort.active], isAsc)
    });
    this.userDataSource.paginator = this.paginator;
  }
}
