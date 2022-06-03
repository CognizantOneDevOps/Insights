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
import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { Router, NavigationExtras, ActivatedRoute } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { MLWizardService } from "@insights/app/modules/model-management/mlwizard/mlwizard.service";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { ReportManagementService } from "@insights/app/modules/reportmanagement/reportmanagement.service";

export interface dataNtype {
  FieldName: string;
  DataType: string;
  EnableNLP: boolean;
}

@Component({
  selector: "app-mlwizard",
  templateUrl: "./mlwizard.component.html",
  styleUrls: ["./mlwizard.component.scss", "../../home.module.scss"],
})
export class MLWizardComponent implements OnInit {
  isNlpDisabled = [];
  @ViewChild("fileInput", { static: true }) csvFileDiv: ElementRef;
  disableparsebutton: boolean = false;
  buttonEnabled: boolean = false;
  file: File = null;
  fileDetails: any = null;
  dtypes = [];
  headers: any;
  count: number;
  isSmallTableVisible: boolean = false;
  usecaseid: string = null;
  dataAndType = [];
  len: number;
  dataSource = new MatTableDataSource<dataNtype>([]);
  displayedColumns: string[] = ["colIndex", "colName", "colType", "W2V"];
  displayProgressSpinner: boolean = false;
  hideNav: boolean = true;
  //variables preserved for navigation
  sratio: number;
  checked: string;
  noOfModels: number = null;
  hideLeaderboardbtn: boolean = true;
  responseOfTasklist: any;
  listOftasks = [];
  tasklist = [];
  task: string = null;
  splitRatio: number = null;
  target: string = null;
  ptype: string = null;
  regex = new RegExp("^[a-zA-Z0-9_]*$");
  enablesavebutton: boolean = false;
  predictionTypes = [];
  @ViewChild(MatPaginator) paginator: MatPaginator;
  selectedIndex: number;
  totalPages: number = -1;
  currentPageIndex: number = -1;
  fileName: string;
  MAX_ROWS_PER_TABLE = 10;

  constructor(
    private mlwizardService: MLWizardService,
    public router: Router,
    private route: ActivatedRoute,
    public messageDialog: MessageDialogService,
    public config: InsightsInitService,
    public reportmanagementService: ReportManagementService
  ) {
    this.getTaskList();
  }

  ngOnInit() {
    this.dtypes = this.config.getMLDataTypes();
    this.route.queryParams.subscribe((params) => {
      console.log(params);
      if (params.hasOwnProperty("tableObject")) {
        this.disableparsebutton = true;
        let parsedObj = JSON.parse(params.tableObject);
        this.dataSource.data = parsedObj;
        console.log(this.dataSource.data);
        this.dataAndType = parsedObj;
        this.usecaseid = params.usecaseid;
        this.headers = params.headers;
        this.len = this.headers.length;
        this.isSmallTableVisible = params.isSmallTableVisible;
        this.hideNav = !this.isSmallTableVisible;
        this.sratio = params.sratio;
        this.target = params.target;
        this.noOfModels = params.noOfModels;
        this.hideLeaderboardbtn = params.hideLeaderboardbtn;
        this.dataSource.paginator = this.paginator;
        this.getTaskList();
        console.log(this.sratio);
      }
    });
    this.getPredictionTypes();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  verifyId(caseid: string): Boolean {
    this.usecaseid = caseid;
    this.isUsecaseIdPopulated();
    var flag: Boolean = null;
    this.mlwizardService
      .validateUsecaseID(this.usecaseid)
      .subscribe((event) => {
        console.log(event);
        if (event.status == "success") {
          if (event.data.UniqueUsecase) {
            this.messageDialog.openSnackBar(
              "A usecase already exists with the given ID. Please try with a different one.",
              "ERROR"
            );
            flag = false;
          } else flag = true;
        } else if (event.status == "failure") {
          this.messageDialog.openSnackBar(
            "Couldn't verify usecase ID with database. Please try again.",
            "ERROR"
          );
          flag = false;
        }
      });
    return flag;
  }

  onSelectCsv(event) {
    if (event.target.files && event.target.files[0]) {
      this.fileDetails = this.csvFileDiv.nativeElement.files[0];
      this.file = event.target.files.item(0);
      console.log(this.file);
      this.fileName = this.file.name;
      this.headers = [];
      var reader = new FileReader();
      reader.readAsText(event.target.files[0]); // read file
      reader.onload = (event: any) => {
        // called once readAsText is completed
        var inputData = event.target.result;
        this.headers = inputData.split(/\r\n|\n/)[0].split(",");
        let tempObj = [];
        this.headers.forEach((headers, index) => {
          tempObj.push({
            Index: index,
            FieldName: headers,
            EnableNLP: false,
            DataType: null,
          });
        });
        console.log(this.headers);
        console.log(tempObj);
        this.dataSource.data = tempObj;
        this.mlwizardService.sendHeaders.next(tempObj);
        this.totalPages = Math.ceil(
          this.dataSource.data.length / this.MAX_ROWS_PER_TABLE
        );
        this.len = this.headers.length;
        for (let i = 0; i < this.len; i++) this.isNlpDisabled[i] = true;

        this.buttonEnabled = true;
      };
    }
    this.uploadCsv();
  }

  uploadCsv() {
    var dummy = <HTMLInputElement>document.getElementById("file");
    var bytes = this.fileDetails["size"];
    var fileName = this.fileDetails["name"];

    var testFileExt = this.checkFile(fileName, ".csv");
    if (this.isUsecaseIdPopulated()) {
      if (!testFileExt) {
        this.messageDialog.openSnackBar(
          "Please select a valid .csv file",
          "ERROR"
        );
        this.buttonEnabled = false;
        dummy.value = "";
      } else {
        this.isSmallTableVisible = true;
        this.buttonEnabled = false;
        this.disableparsebutton = false;
        this.enablesavebutton = true;
      }
    }
  }

  checkFile(fileName: string, validExt: string) {
    if (fileName) {
      var fileExt = fileName.substring(fileName.lastIndexOf("."));
      fileExt = fileExt.toLowerCase();

      if (validExt.indexOf(fileExt) < 0 && fileExt != "") {
        return false;
      } else {
        return true;
      }
    }
  }

  async getPredictionTypes() {
    var self = this;
    try {
      self.predictionTypes = [];
      let predictionTypeList = await this.mlwizardService.getPredictionTypes();

      if (predictionTypeList.status == "success") {
        this.predictionTypes = predictionTypeList.data;
        console.log("Prediction Types=========>" + this.predictionTypes);
      }
    } catch (error) {
      console.log(error);
    }
  }

  async getTaskList() {
    this.responseOfTasklist = await this.reportmanagementService.getTasksList(
      "AUTOML"
    );
    console.log(this.responseOfTasklist.data);
    this.count = 0;
    this.listOftasks = this.responseOfTasklist.data;
    console.log(this.listOftasks);
    this.listOftasks.forEach((element) => {
      if (this.count < this.responseOfTasklist.data.length) {
        console.log(element.taskId);
        let taskObj = new Object();
        taskObj["taskId"] = element.taskId;
        taskObj["sequence"] = this.count;
        this.tasklist.push(taskObj);
        console.log(this.tasklist);
        this.count += 1;
      }
    });
    console.log(this.tasklist);
    this.task = JSON.stringify(this.tasklist);
    console.log(this.task);
  }

  uploadData() {
    var dialogmessage =
      "You have created a new Usecase <b>" +
      this.usecaseid +
      "</b>. Do you want to continue ?";
    var title = "Save " + this.usecaseid;
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.mlwizardService
          .uploadDataWithConfig(
            this.usecaseid,
            this.file,
            this.dataAndType,
            this.splitRatio,
            this.target,
            this.noOfModels.toString(),
            this.task,
            this.ptype
          )
          .subscribe((response) => {
            if (response.status == "success") {
              this.messageDialog.openSnackBar(
                "<b>" + this.usecaseid + "</b> saved successfully.",
                "SUCCESS"
              );
              let navigationExtras: NavigationExtras = {
                skipLocationChange: true,
                queryParams: {},
              };
              this.router.navigate(
                ["InSights/Home/modelmanagement"],
                navigationExtras
              );
            } else if (response.status == "failure") {
              this.messageDialog.openSnackBar(response.message, "ERROR");
            } else {
              this.messageDialog.openSnackBar(
                "Failed to save Usecase.Please check logs.",
                "ERROR"
              );
            }
          });
      }
    });
  }

  validateData() {
    var checkname = this.regex.test(this.usecaseid);
    if (!checkname) {
      this.messageDialog.openSnackBar(
        "Please enter valid Usecase name, it contains only alphanumeric character and underscore",
        "ERROR"
      );
    } else if (this.usecaseid == "") {
      this.messageDialog.openSnackBar(
        "Please provide a valid Usecase name, it cannot be blank",
        "ERROR"
      );
    } else if (this.splitRatio == null)
      this.messageDialog.openSnackBar(
        "Please provide a valid integer between 10 to 90 as split ratio",
        "ERROR"
      );
    else if (this.target == null)
      this.messageDialog.openSnackBar(
        "Please select target column for the AutoML",
        "ERROR"
      );
    else if (this.noOfModels == null)
      this.messageDialog.openSnackBar(
        "Please select Max number of models for the AutoML",
        "ERROR"
      );
    else if (this.ptype == null) {
      this.messageDialog.openSnackBar(
        "Please select prediction type for the AutoML",
        "ERROR"
      );
    } else if (this.task == null) {
      this.messageDialog.openSnackBar(
        "Please add task for AutoML before saving record",
        "ERROR"
      );
    } else if (Object.keys(this.dataAndType).length != this.len) {
      this.displayProgressSpinner = false;
      this.messageDialog.openSnackBar(
        "Please select datatypes for all the columns",
        "ERROR"
      );
      this.disableparsebutton = false;
    } else {
      this.uploadData();
    }
  }

  report(selectedType: string, ind: number, checked: string) {
    console.log(ind);
    var FieldName = this.headers[ind];
    if (this.dataAndType[ind] == null)
      this.dataAndType[ind] = {
        FieldName: FieldName,
        DataType: "",
        EnableNLP: false,
      };
    if (checked == "mat-select") {
      this.dataAndType[ind].DataType = selectedType;
      if (selectedType == "String") {
        this.isNlpDisabled[ind] = false;
      } else {
        this.isNlpDisabled[ind] = true;
      }
    }
    if (checked != "mat-select") {
      if (this.dataAndType[ind].DataType == "String") {
        this.dataAndType[ind].EnableNLP = true;
      } else {
        this.dataAndType[ind].EnableNLP = false;
      }
    }

    console.log(this.dataAndType);
  }

  refresh() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {},
    };
    this.router.navigate(["InSights/Home/modelmanagement"], navigationExtras);
  }

  navigate() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        //userinput: this.file,
        usecaseid: this.usecaseid,
        headers: this.headers,
        tableObject: JSON.stringify(this.dataAndType),
        sratio: this.sratio,
        target: this.target,
        hideLeaderboardbtn: this.hideLeaderboardbtn,
        noOfModels: this.noOfModels,
      },
    };
    this.router.navigate(["InSights/Home/automl"], navigationExtras);
  }

  cancelUpload() {
    var dt = <HTMLInputElement>document.getElementById("file");
    dt.value = "";
    this.buttonEnabled = false;
    this.isSmallTableVisible = false;
    this.usecaseid = "";
    this.target = "";
    this.splitRatio = null;
    this.noOfModels = null;
    this.enablesavebutton = false;
    this.ptype = "";
    this.fileName = "";
  }

  isUsecaseIdPopulated() {
    if (this.usecaseid == null || this.usecaseid == "") {
      this.messageDialog.openSnackBar(
        "Please provide a valid id for this usecase",
        "ERROR"
      );
      return false;
    } else return true;
  }
  goToNextPage() {
    this.paginator.nextPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  goToPrevPage() {
    this.paginator.previousPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
}
