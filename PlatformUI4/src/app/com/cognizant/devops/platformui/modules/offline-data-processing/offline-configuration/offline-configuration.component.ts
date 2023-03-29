/*
 *******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
 *******************************************************************************/
import { OfflineService } from "@insights/app/modules/offline-data-processing/offline-service";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { Component, OnInit } from "@angular/core";
import { Router, NavigationExtras, ActivatedRoute } from "@angular/router";

@Component({
  selector: "app-offline-configuration",
  templateUrl: "./offline-configuration.component.html",
  styleUrls: [
    "./offline-configuration.component.scss",
    "./../../home.module.scss",
  ],
})
export class OfflineConfigurationComponent implements OnInit {
  toolName: any;
  queryName: any;
  cypherQuery: any;
  type: string;
  onEdit: boolean = false;
  inputDataJson: any;
  cronSchedule: any;
  dataSource: any;
  toolsArr = [];
  toolsDetail = [];
  offlineList: any;

  constructor(
    public router: Router,
    public messageDialog: MessageDialogService,
    public offlineService: OfflineService,
    public route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.type = this.offlineService.getType();
    if (this.type != "EDIT") {
      this.getAllQueryName();
    }
    if (this.type === "EDIT") {
      this.onEdit = true;
    }
    this.route.queryParams.subscribe((params) => {
      if (params) {
        this.inputDataJson = params;
        this.toolName = params.toolName;
        this.queryName = params.queryName;
        this.cronSchedule = params.cronSchedule;
        this.cypherQuery = params.cypherQuery;
      }
    });
    this.getLabelTools();
  }
  redirectToLandingPage() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
    };
    this.router.navigate(["InSights/Home/offlineDataList"], navigationExtras);
  }

  public async getAllQueryName() {
    this.offlineList = await this.offlineService.loadOfflineList();
  }

  async getLabelTools() {
    var self = this;
    try {
      self.toolsDetail = [];
      let toollabelresponse = await this.offlineService.loadToolNameArr();
      if (toollabelresponse.status == "success") {
        this.toolsDetail = toollabelresponse.data;
      }
      for (var element of this.toolsDetail) {
        var toolName = element.toolName;
        this.toolsArr.push(toolName);
      }
      this.toolsArr.sort((n1, n2) => (n1 > n2 ? 1 : -1));
    } catch (error) {
      console.log(error);
    }
  }

  validateOfflineData() {
    var isValidated = true;
    if (this.dataSource !== "HYPERLEDGER") {
      if (
        this.toolName === "" ||
        this.toolName === undefined ||
        this.queryName === "" ||
        this.queryName === undefined ||
        this.cypherQuery === "" ||
        this.cypherQuery === undefined ||
        this.cronSchedule === "" ||
        this.cronSchedule === undefined
      ) {
        isValidated = false;
        this.messageDialog.openSnackBar(
          "Please fill mandatory fields",
          "error"
        );
      }
    } else {
      if (
        this.toolName === "" ||
        this.toolName === undefined ||
        this.queryName === "" ||
        this.queryName === undefined ||
        this.cypherQuery === "" ||
        this.cypherQuery === undefined ||
        this.cronSchedule === "" ||
        this.cronSchedule === undefined
      ) {
        isValidated = false;
        this.messageDialog.openSnackBar(
          "Please fill mandatory fields",
          "error"
        );
      }
    }

    if (
      this.offlineList != null &&
      this.offlineList.status == "success" &&
      this.type != "EDIT"
    ) {
      this.offlineList.data.forEach((offlineData) => {
        if (offlineData.queryName == this.queryName) {
          isValidated = false;
          this.messageDialog.openSnackBar("Query name already exits", "error");
        }
      });
    }

    if (this.cypherQuery.match(/INDEX/i)) {
      isValidated = false;
      this.messageDialog.openSnackBar(
        "Please provide valid cypher query",
        "error"
      );
    }

    if (isValidated) {
      this.onClickSave();
    }
  }

  onClickSave() {
    if (this.type === "EDIT") {
      this.updateOfflineData();
    } else {
      this.saveOfflineData();
    }
  }

  constructData() {
    var self = this;
    var offlineAPIRequestJson = {};
    offlineAPIRequestJson["toolName"] = this.toolName;
    offlineAPIRequestJson["queryName"] = this.queryName;
    offlineAPIRequestJson["cronSchedule"] =
      this.cronSchedule === undefined ? "" : this.cronSchedule;
    offlineAPIRequestJson["cypherQuery"] =
      this.cypherQuery === undefined ? "" : this.cypherQuery;
    offlineAPIRequestJson["lastExecutionTime"] = "";
    return offlineAPIRequestJson;
  }

  saveOfflineData() {
    var self = this;
    var dialogmessage =
      " You have created a new offline data query <b>" +
      this.queryName +
      "</b>. Do you want continue? ";
    var title = "Save Offline Query ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.offlineService
          .saveDataforOffline(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              setTimeout(() => {
                self.messageDialog.openSnackBar(
                  "Query with query name " +
                    "<b>" +
                    self.queryName +
                    "</b> created successfully.",
                  "success"
                );
              }, 500);
              self.router.navigateByUrl("InSights/Home/offlineDataList", {
                skipLocationChange: true,
              });
              self.type = "EDIT";
            } else if (response.message === "Query already exists") {
              self.messageDialog.openSnackBar(
                " Offline query -" +
                  "<b>" +
                  self.queryName +
                  "</b> already exists. Please try again with a new query name.",
                "error"
              );
            } else if (
              response.message ===
              "Offline Query Definition does not have some mandatory field"
            ) {
              self.messageDialog.openSnackBar(
                "Offline query definition does not have some mandatory field.",
                "error"
              );
            } else if (response.message === "Cron Expression is invalid") {
              self.messageDialog.openSnackBar(
                "Please enter a valid Cron Expression",
                "error"
              );
            } else {
              self.messageDialog.openSnackBar(
                "Failed to save the offline query. Please check logs.",
                "error"
              );
            }
          });
      }
    });
  }

  updateOfflineData() {
    var self = this;
    var dialogmessage =
      " You have updated an Offline data <b>" +
      this.queryName +
      "</b>. Do you want continue? ";
    var title = "Update Offline Query ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.offlineService
          .updateDataforOffline(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              self.messageDialog.openSnackBar(
                "Offline data updated for query name " +
                  "<b>" +
                  self.queryName +
                  "</b>",
                "success"
              );
              self.router.navigateByUrl("InSights/Home/offlineDataList", {
                skipLocationChange: true,
              });
              self.type = "EDIT";
            } else if (response.message === "Cron Expression is invalid") {
              self.messageDialog.openSnackBar(
                "Please enter a valid Cron Expression",
                "error"
              );
            } else {
              self.messageDialog.openSnackBar(
                "<b>" + response.message,
                "error"
              );
            }
          });
      }
    });
  }

  refreshData() {
    this.type = "ADD";
    this.toolName = "";
    this.queryName = "";
    this.cronSchedule = "";
    this.cypherQuery = "";
  }

  reset() {
    this.toolName = this.inputDataJson.toolName;
    this.queryName = this.inputDataJson.queryName;
    this.cronSchedule = this.inputDataJson.cronSchedule;
    this.cypherQuery = this.inputDataJson.cypherQuery;
  }
}
