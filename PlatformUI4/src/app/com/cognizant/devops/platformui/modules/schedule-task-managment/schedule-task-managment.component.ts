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

import { Component, OnInit, ViewChild } from "@angular/core";
import { MatTableDataSource } from "@angular/material/table";
import { MatPaginator } from "@angular/material/paginator";
import { TaskManagementService } from "@insights/app/modules/schedule-task-managment/task-management-service";
import { TaskHistoryDetailsDialog } from "@insights/app/modules/schedule-task-managment/task-history-details/task-history-details-dialog";
import { MatDialog } from "@angular/material/dialog";
import { DataSharedService } from "@insights/common/data-shared-service";
import { MatRadioChange } from "@angular/material/radio";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { Validators, FormGroup, FormBuilder } from "@angular/forms";
import cronstrue from "cronstrue";

@Component({
  selector: "app-schedule-task-managment",
  templateUrl: "./schedule-task-managment.component.html",
  styleUrls: [
    "./schedule-task-managment.component.scss",
    "./../home.module.scss",
  ],
})
export class ScheduleTaskManagmentComponent implements OnInit {
  taskuserDataSource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  showPagination: boolean = true;
  displayedColumns: string[];
  displayedToolColumnsEdit: string[];
  showThrobber: boolean = false;
  selectedTask: any;
  isEdit: boolean = false;
  showListView: boolean = true;
  taskForm: FormGroup;
  disableRadioChangeButton: boolean = true;
  selectedIndex = -1;
  seletedAction = "NOT_STARTED";
  nameValidationRegex = new RegExp("^[a-zA-Z0-9_]*$");
  currentPageValue: number;
  MAX_ROWS_PER_TABLE = 5;
  currentPageIndex: number = -1;
  totalPages: number = -1;
  feature: string = "Add";

  constructor(
    public timerTaskService: TaskManagementService,
    private dialog: MatDialog,
    public dataShare: DataSharedService,
    public messageDialog: MessageDialogService,
    private formBuilder: FormBuilder
  ) {
    this.displayedColumns = [
      "select",
      "componentName",
      "componentClassDetail",
      "schedule",
      "status",
      "lastrun",
      "details",
    ];
    this.displayedToolColumnsEdit = [
      "componentName",
      "componentClassDetail",
      "schedule",
    ];
    this.loadTaskList();
    this.taskuserDataSource.paginator = this.paginator;
  }

  ngOnInit(): void {
    this.taskForm = this.formBuilder.group({
      componentName: [{ value: "", disabled: false }, [Validators.required]],
      componentClassDetail: ["", [Validators.required]],
      schedule: ["", [Validators.required]],
    });
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  async loadTaskList() {
    var detailedRecords = [];
    this.showListView = true;
    this.selectedIndex = -1;
    this.disableRadioChangeButton = true;
    let userDataSourceFromService = await this.timerTaskService.getTaskList();
    console.log(userDataSourceFromService);
    var dataArray = userDataSourceFromService.data;
    dataArray.forEach((key) => {
      var obj = key;

      if (obj["lastrun"] === "" || !obj.hasOwnProperty("lastrun")) {
        obj["lastrun"] = "-";
      } else {
        obj["lastrun"] = obj["lastrun"];
        var dateObj = new Date(obj["lastrun"]);
        obj["lastrun"] = this.dataShare.convertDateToSpecificDateFormat(
          dateObj,
          "yyyy-MM-dd HH:mm:ss"
        );
      }

      detailedRecords.push(obj);
    });
    this.taskuserDataSource.data = detailedRecords;
    this.taskuserDataSource.paginator = this.paginator;
    this.totalPages = Math.ceil(
      this.taskuserDataSource.data.length / this.MAX_ROWS_PER_TABLE
    );
  }

  addTask() {
    this.showListView = false;
    this.isEdit = false;
    this.feature = "Add";
    this.taskForm.reset();
  }

  showTaskExecutionHistoryDetailsDialog(
    componentName: String,
    workflowId: String
  ) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(TaskHistoryDetailsDialog, {
        panelClass: "custom-dialog-container",
        height: "80%",
        width: "90%",
        disableClose: true,
        data: {
          componentName: componentName,
        },
      });
    }
  }

  radioChange(event: MatRadioChange, index) {
    console.log(" Radio change ");
    console.log(this.selectedTask);
    console.log(event.value);
    this.disableRadioChangeButton = false;
    this.selectedIndex = index + this.currentPageValue;
    this.seletedAction = event.value.action;
    console.log(event.value.action);
  }

  editTaskConfiguration() {
    this.isEdit = true;
    this.feature = "Edit";
    console.log("Inside editTaskConfiguration " + this.isEdit);
    this.validateSeletedData();
    console.log(this.selectedTask);

    this.showListView = false;
    this.taskForm.patchValue(this.selectedTask);
    this.taskForm.controls["componentName"].disable();
  }

  async saveData() {
    console.log("Inside save date ");
    console.log(this.taskForm.value);
  }

  async onSubmit() {
    console.log(this.taskForm.value);
    console.log(this.selectedTask);
    if (this.isEdit) {
      this.taskForm.controls["componentName"].enable();
      this.taskForm.patchValue({
        componentName: this.selectedTask.componentName,
      });
    }
    if (this.validateData(this.taskForm.value)) {
      var taskJson = {};
      taskJson["componentName"] = this.taskForm.value.componentName;
      taskJson["componentClassDetail"] =
        this.taskForm.value.componentClassDetail;
      taskJson["schedule"] = this.taskForm.value.schedule;
      this.isCronValid(this.taskForm.value.schedule);
      var recordStatus =
        await this.timerTaskService.saveOrUpdateTaskDefinitionRecords(
          JSON.stringify(taskJson)
        );
      console.log(recordStatus);
      if (recordStatus.status == "success") {
        this.messageDialog.openSnackBar("Task created successfully", "success");
        this.loadTaskList();
      } else {
        console.error(recordStatus);
        this.messageDialog.openSnackBar(
          "Task creation/Edit has issue, Please check service log for more detail.",
          "error"
        );
      }
    }
  }

  validateData(formDataValue): boolean {
    var validated: boolean = true;
    var message = "";
    var checkname = this.nameValidationRegex.test(formDataValue.componentName);
    if (
      formDataValue.componentName == "" ||
      formDataValue.componentName == undefined
    ) {
      message == "Please fill componentName detail";
      validated = false;
    } else if (!checkname) {
      message =
        "Please enter valid TASK name, and it contains only alphanumeric character and underscore ";
      validated = false;
    }
    if (!validated) {
      this.messageDialog.openSnackBar(message, "error");
    }
    return validated;
  }

  async deleteTask() {
    console.log("Inside deleteTask ");
    self = this;
    this.validateSeletedData();
    var self = this;
    var title = "Delete Report";
    var dialogmessage =
      "Do you want to delete <b>" +
      this.selectedTask.componentName +
      "</b> ? <br> <b> Please note: </b> The action of deleting " +
      "<b>" +
      this.selectedTask.componentName +
      "</b> cannot be undone. Do you want to continue ? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedTask.componentName,
      "DELETE",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        console.log(self.selectedTask);
        var taskJson = {};
        taskJson["componentName"] = self.selectedTask.componentName;
        var recordStatus = self.timerTaskService
          .deleteTaskDefinitionUpdate(JSON.stringify(taskJson))
          .then(function (recordStatus) {
            console.log(recordStatus);
            if (recordStatus.status == "success") {
              self.messageDialog.openSnackBar(
                "Task deleted successfully",
                "success"
              );
              self.loadTaskList();
            } else {
              console.error(recordStatus);
              self.messageDialog.openSnackBar(
                "Task deleted has issue, Please check service log for more detail.",
                "error"
              );
            }
          })
          .catch(function (data) {});
      }
    });
  }

  validateSeletedData() {
    if (this.selectedTask === undefined) {
      this.messageDialog.openSnackBar(
        "Please select a record to edit",
        "error"
      );
      return;
    }
  }

  refresh() {
    this.selectedIndex = -1;
    this.showListView = false;
    this.taskForm.reset();
    this.loadTaskList();
  }

  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  async statusUpdate(action) {
    console.log("Inside statusUpdate " + action);
    this.validateSeletedData();
    console.log(this.selectedTask);
    var taskJson = {};
    taskJson["componentName"] = this.selectedTask.componentName;
    taskJson["action"] = action;
    var recordStatus = await this.timerTaskService.updateStatuOfTaskDefinition(
      JSON.stringify(taskJson)
    );
    console.log(recordStatus);
    if (recordStatus.status == "success") {
      this.messageDialog.openSnackBar(
        "Task action ( <b> " + action + " </b>) updated successfully",
        "success"
      );
      this.loadTaskList();
    } else {
      console.error(recordStatus);
      this.messageDialog.openSnackBar(
        "Task update has issue, Please check service log for more detail.",
        "error"
      );
    }
  }

  isCronValid(cornExpression) {
    var cronregex = new RegExp(
      /^(\*|([0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])|\*\/([0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])) (\*|([0-9]|1[0-9]|2[0-3])|\*\/([0-9]|1[0-9]|2[0-3])) (\*|([1-9]|1[0-9]|2[0-9]|3[0-1])|\*\/([1-9]|1[0-9]|2[0-9]|3[0-1])) (\*|([1-9]|1[0-2])|\*\/([1-9]|1[0-2])) (\*|([0-6])|\*\/([0-6]))$/
    );
    var isCronValid = cronregex.test(cornExpression);
    console.log(
      " Is corn valid cornExpression " +
        cornExpression +
        "  isCronValid " +
        isCronValid
    );
    return isCronValid;
  }

  getHistoryCSS(rowno) {
    if (this.selectedIndex == rowno) {
      return false;
    } else {
      return true;
    }
  }

  getTitle(element) {
    return cronstrue.toString(element);
  }

  goToNextPage() {
    this.selectedIndex = -1;
    this.paginator.nextPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  goToPrevPage() {
    this.selectedIndex = -1;
    this.paginator.previousPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
}
