/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, NavigationExtras, Router } from "@angular/router";
import { MessageDialogService } from "../../application-dialog/message-dialog-service";
import { WorkflowTaskManagementService } from ".././workflow-task-management.service";

@Component({
  selector: "app-add-workflow-task",
  templateUrl: "./add-workflow-task.component.html",
  styleUrls: ["./add-workflow-task.component.scss", "./../../home.module.scss"],
})
export class AddWorkflowTaskComponent implements OnInit {
  dependency: number;
  mqChannel: string;
  description: string;
  componentName: string;
  disableInputFields: boolean = false;
  feature: string;
  status: boolean = true;
  workflowType: string;
  workflowTypeList = [];
  workflow: any;
  receivedParam: any;
  btnValue: string;
  subTitleName: string;
  taskId: number;
  regexDes = new RegExp("^[a-zA-Z_0-9]*$");
  regexCN = new RegExp("^[a-zA-Z.]*$");
  regexMQ = new RegExp("^[a-zA-Z_.]*$");
  taskData: any;

  constructor(
    public router: Router,
    private route: ActivatedRoute,
    private managementService: WorkflowTaskManagementService,
    private messageDialog: MessageDialogService
  ) {
    this.loadData();
  }

  ngOnInit(): void {}
  async loadData() {
    this.route.queryParams.subscribe((params) => {
      this.receivedParam = JSON.parse(params["taskParameter"]);
      if (this.receivedParam.type == "update") {
        this.feature = "Edit ";
        this.disableInputFields = true;
        this.setParams();
      } else {
        this.feature = "Add ";
      }
    });
    this.managementService.getWorkFlowType().then((response) => {
      this.workflowTypeList = response;
    });
    let taskList = await this.managementService.getWorkFlowTask();
    this.taskData = taskList.data;
  }
  async setParams() {
    if (this.receivedParam.detailedArr != null) {
      this.description = this.receivedParam.detailedArr.description;
      this.mqChannel = this.receivedParam.detailedArr.mqchannel;
      this.componentName = this.receivedParam.detailedArr.componentname;
      this.dependency = this.receivedParam.detailedArr.dependency;
      this.workflowType = this.receivedParam.detailedArr.workflowtype;
      this.taskId = this.receivedParam.detailedArr.taskId;
    }
  }
  async validateTaskDetails() {
    var checknameDes = this.regexDes.test(this.description);
    var checknameCn = this.regexCN.test(this.componentName);
    var checknameMQ = this.regexMQ.test(this.mqChannel);
    var isValidated = false;
    let messageDialogText;
    if (
      this.dependency === undefined ||
      this.mqChannel === "" ||
      this.description === "" ||
      this.componentName === "" ||
      this.workflowType === ""
    ) {
      isValidated = false;
      messageDialogText = "Please fill mandatory fields";
    } else if (!checknameDes) {
      isValidated = false;
      messageDialogText =
        "Please enter valid description, it contains only alphanumeric character and underscore.";
    } else if (!checknameCn) {
      isValidated = false;
      messageDialogText =
        "Please enter valid component name, it contains only alphabetical character and period.";
    } else if (!checknameMQ) {
      isValidated = false;
      messageDialogText =
        "Please enter valid MqChannel, it contains only alphabetical character, period(.) and underscore.";
    } else {
      isValidated = this.validateUnique();
      if (isValidated == false) {
        messageDialogText = "Task already exist";
      }
    }
    if (isValidated) {
      this.editAndSaveData();
    } else {
      this.messageDialog.openSnackBar(messageDialogText, "error");
    }
  }
  validateUnique() {
    if (this.receivedParam.type == "update") return true;
    for (let task of this.taskData) {
      if (
        task.description === this.description &&
        task.componentname === this.componentName
      ) {
        return false;
      }
    }
    return true;
  }
  async editAndSaveData() {
    if (this.receivedParam.type == "update") {
      var self = this;
      var workFlowAPIRequestJson = {};
      workFlowAPIRequestJson["taskId"] = self.taskId;
      workFlowAPIRequestJson["description"] = self.description;
      workFlowAPIRequestJson["mqChannel"] = self.mqChannel;
      workFlowAPIRequestJson["componentName"] = self.componentName;
      workFlowAPIRequestJson["dependency"] = self.dependency;
      workFlowAPIRequestJson["workflowType"] = self.workflowType;
      var dialogmessage =
        " You have updated the workflow task <b>" +
        self.description +
        ".<br>Do you want continue? ";
      let title = "Update workflow task " + this.description;
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "35%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.managementService
            .updateDataforWorkflow(JSON.stringify(workFlowAPIRequestJson))
            .then(function (response) {
              if (response.status == "success") {
                self.messageDialog.openSnackBar(
                  "<b>" + self.description + "</b> update successfully.",
                  "success"
                );
                self.refresh();
              } else {
                self.messageDialog.openSnackBar(
                  "Failed to save the workflow task.Please check logs.",
                  "error"
                );
              }
            });
        }
      });
    } else {
      var self = this;
      var workFlowAPIRequestJson = {};
      workFlowAPIRequestJson["description"] = self.description;
      workFlowAPIRequestJson["mqChannel"] = self.mqChannel;
      workFlowAPIRequestJson["componentName"] = self.componentName;
      workFlowAPIRequestJson["dependency"] = self.dependency;
      workFlowAPIRequestJson["workflowType"] = self.workflowType;
      var dialogmessage =
        " You have created a new workflow task <b>" +
        self.description +
        "</b> .Do you want continue? ";
      let title = "Save workflow task " + this.description;
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "35%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.managementService
            .saveDataforWorkflow(JSON.stringify(workFlowAPIRequestJson))
            .then(function (response) {
              if (response.status == "success") {
                self.messageDialog.openSnackBar(
                  "<b>" + self.description + "</b> saved successfully.",
                  "success"
                );
                self.refresh();
              } else {
                self.messageDialog.openSnackBar(
                  "Failed to save the workflow task.Please check logs.",
                  "error"
                );
              }
            });
        }
      });
    }
  }

  reset() {
    if (this.receivedParam.type == "update") {
      this.setParams();
    } else {
      this.dependency = null;
      this.mqChannel = "";
      this.description = "";
      this.componentName = "";
      this.workflowType = "";
    }
  }

  refresh() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {},
    };
    this.router.navigate(
      ["InSights/Home/workflow-task-management"],
      navigationExtras
    );
  }
}
