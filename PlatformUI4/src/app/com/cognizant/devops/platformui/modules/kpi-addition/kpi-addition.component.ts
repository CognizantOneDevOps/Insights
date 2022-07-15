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

import { Component, OnInit } from '@angular/core';
import { KpiService } from './kpi-service';
import { BulkUploadService } from '../bulkupload/bulkupload.service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { ActivatedRoute, Router, NavigationExtras } from '@angular/router';
import { ModelManagementService } from '@insights/app/modules/model-management/model-management.service';


@Component({
  selector: "app-kpi-addition",
  templateUrl: "./kpi-addition.component.html",
  styleUrls: ["./kpi-addition.component.scss", "./../home.module.scss"],
})
export class KpiAdditionComponent implements OnInit {
  categoryDetail = [];
  dataSourceDetail = [];
  toolsDetail = [];
  labelsArr = [];
  toolsArr = [];
  kpiId: any;
  kpiName: any;
  selectedTool: any;
  category: any;
  groupName: any;
  dataSource: any;
  dbQuery: any;
  isActive: any;
  type: string;
  kpiList: any;
  onEdit: boolean = false;
  enableUsecase: boolean = false;
  selectedUsecase: string = "";
  usecaseDetails = [];
  usecaseList = [];
  usecaseResponse: any;
  resultField: any;
  isForecast: boolean = false;
  outputDatasource = [];
  dataSourceOutput: any;
  inputDataJson:any;

  constructor(
    public router: Router,
    public route: ActivatedRoute,
    public kpiService: KpiService,
    public messageDialog: MessageDialogService,
    private bulkuploadService: BulkUploadService,
    public modelManagementService: ModelManagementService
  ) {}

  ngOnInit() {
    this.type = this.kpiService.getType();
    if (this.type === "EDIT") {
      this.onEdit = true;
    }
    this.route.queryParams.subscribe((params) => {
      if (params) {
        this.inputDataJson=params;
        this.kpiId = params.kpiId;
        this.kpiName = params.kpiName;
        this.selectedTool = params.selectedTool;
        this.category = params.category;
        this.groupName = params.groupName;
        this.dataSource = params.dataSource;
        this.dbQuery = params.dbQuery;
        this.isActive = true;
        this.resultField = params.resultField;
        this.dataSourceOutput = params.outputDatasource;
        this.selectedUsecase = params.usecase;
        if (params.usecase == undefined) {
          this.selectedUsecase = "";
        }
        if (this.category == "PREDICTION") {
          this.isForecast = true;
          this.enableUsecase = true;
        }
        console.log(this.selectedUsecase);
      }
    });
    this.getLabelTools();
    this.getKpiCategory();
    this.getKpiDataSource();
    this.loadForecastUsecase();
  }
  async getKpiCategory() {
    var self = this;
    try {
      self.categoryDetail = [];
      let categorylabelresponse = await this.kpiService.loadKpiCategory();
      if (categorylabelresponse.status == "success") {
        this.categoryDetail = categorylabelresponse.data;
      }
    } catch (error) {
      console.log(error);
    }
  }

  async getKpiDataSource() {
    let dataSourceRes = await this.kpiService.loadKpiDataSource();
    if (dataSourceRes.status == "success") {
      this.dataSourceDetail = dataSourceRes.data;
      this.dataSourceDetail.forEach((element) => {
        if (element != "HYPERLEDGER") {
          this.outputDatasource.push(element);
        }
      });
      console.log(this.outputDatasource, this.dataSourceDetail);
    }
  }
  async getLabelTools() {
    var self = this;
    try {
      self.toolsDetail = [];
      let toollabelresponse =
        await this.bulkuploadService.loadUiServiceLocation();
      if (toollabelresponse.status == "success") {
        this.toolsDetail = toollabelresponse.data;
      }
      for (var element of this.toolsDetail) {
        var toolName = element.toolName;
        var labelName = element.label;
        this.toolsArr.push(toolName);
        console.log("ToolsArr" + this.toolsArr);
        this.labelsArr.push(labelName);
      }
    } catch (error) {
      console.log(error);
    }
  }

  async loadForecastUsecase() {
    this.usecaseResponse =
      await this.modelManagementService.loadForecastUsecase();
    console.log(this.usecaseResponse);
    if (this.usecaseResponse.status == "success") {
      this.usecaseDetails = this.usecaseResponse.data;
      this.usecaseDetails.forEach((element) => {
        this.usecaseList.push(element.usecaseName);
      });
      console.log(this.usecaseList);
    }
  }

  validateKpiData() {
    var isValidated = true;
    if (this.dataSource !== "HYPERLEDGER") {
      if (
        this.kpiId === "" ||
        this.kpiId === undefined ||
        this.kpiName === "" ||
        this.kpiName === undefined ||
        this.selectedTool === "" ||
        this.selectedTool === undefined ||
        this.category === "" ||
        this.category === undefined ||
        this.groupName === "" ||
        this.groupName === undefined ||
        this.dataSource === "" ||
        this.dataSource === undefined ||
        this.dbQuery === "" ||
        this.dbQuery === undefined ||
        this.isActive === "" ||
        this.isActive === undefined ||
        this.resultField === "" ||
        this.resultField === undefined
      ) {
        isValidated = false;
        this.messageDialog.openSnackBar(
          "Please fill mandatory fields",
          "error"
        );
      } else if (
        this.category == "PREDICTION" &&
        (this.selectedUsecase === "" || this.selectedUsecase === undefined)
      ) {
        this.messageDialog.openSnackBar(
          "Please select usecase for prediction.",
          "error"
        );
        isValidated = false;
      }
    } else {
      if (
        this.kpiId === "" ||
        this.kpiId === undefined ||
        this.kpiName === "" ||
        this.kpiName === undefined ||
        this.selectedTool === "" ||
        this.selectedTool === undefined ||
        this.category === "" ||
        this.category === undefined ||
        this.groupName === "" ||
        this.groupName === undefined ||
        this.dataSource === "" ||
        this.dataSource === undefined ||
        this.isActive === "" ||
        this.isActive === undefined ||
        this.resultField === "" ||
        this.resultField === undefined
      ) {
        isValidated = false;
        this.messageDialog.openSnackBar(
          "Please fill mandatory fields",
          "error"
        );
      } else if (
        this.category == "PREDICTION" &&
        (this.selectedUsecase === "" || this.selectedUsecase === undefined)
      ) {
        this.messageDialog.openSnackBar(
          "Please select usecase for prediction.",
          "error"
        );
        isValidated = false;
      }
    }
    if (isValidated) {
      this.onClickSave();
    }
  }
  constructData() {
    var self = this;
    var kpiAPIRequestJson = {};
    kpiAPIRequestJson["kpiId"] = this.kpiId;
    kpiAPIRequestJson["name"] = this.kpiName;
    kpiAPIRequestJson["group"] = this.groupName;
    kpiAPIRequestJson["category"] = this.category;
    kpiAPIRequestJson["toolName"] = this.selectedTool;
    kpiAPIRequestJson["DBQuery"] =
      this.dbQuery === undefined ? "" : this.dbQuery;
    kpiAPIRequestJson["datasource"] = this.dataSource;
    kpiAPIRequestJson["isActive"] = this.isActive;
    kpiAPIRequestJson["resultField"] = this.resultField;
    kpiAPIRequestJson["usecase"] = this.selectedUsecase;
    kpiAPIRequestJson["outputDatasource"] = "";
    return kpiAPIRequestJson;
  }
  onClickSave() {
    console.log(this.constructData());
    if (this.type === "EDIT") {
      this.updateKpiData();
    } else {
      this.saveKpiData();
    }
  }

  updateKpiData() {
    var self = this;
    var dialogmessage =
      " You have updated a KPI <b>" +
      this.kpiId +
      "</b> .Do you want continue? ";
    var title = "Update KPI ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.kpiService
          .updateDataforKpi(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              self.messageDialog.openSnackBar(
                "<b>" + "Kpi defination updated for KpiId " + self.kpiId,
                "success"
              );
              self.router.navigateByUrl("InSights/Home/kpicreation", {
                skipLocationChange: true,
              });
              self.type = "EDIT";
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
  defaultStop(event) {
    if (
      (event.which != 8 && event.which != 0 && event.which < 48) ||
      event.which > 57
    ) {
      event.preventDefault();
    }
  }

  saveKpiData() {
    var self = this;
    var dialogmessage =
      " You have created a new KPI <b>" +
      this.kpiId +
      "</b> .Do you want continue? ";
    var title = "Save KPI ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.kpiService
          .saveDataforKpi(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              setTimeout(() => {
                self.messageDialog.openSnackBar(
                  "<b>" +
                    "Kpi with id " +
                    self.kpiId +
                    "</b> created successfully.",
                  "success"
                );
              }, 500);
              self.router.navigateByUrl("InSights/Home/kpicreation", {
                skipLocationChange: true,
              });
              self.type = "EDIT";
            } else if (response.message === "KPI already exists") {
              self.messageDialog.openSnackBar(
                "<b>" +
                  "Kpi Id" +
                  self.kpiId +
                  "</b> already exists. Please try again with a new Id.",
                "error"
              );
            } else if (
              response.message ===
              "kpi Definition does not have some mandatory field"
            ) {
              self.messageDialog.openSnackBar(
                "Kpi Definition does not have some mandatory field.",
                "error"
              );
            } else {
              self.messageDialog.openSnackBar(
                "Failed to save the Kpi.Please check logs.",
                "error"
              );
            }
          });
      }
    });
  }
  reset(){
    this.resultField = this.inputDataJson.resultField;
    this.dataSource = this.inputDataJson.dataSource;
    this.dbQuery = this.inputDataJson.dbQuery; 
  }
  refreshData() {
    this.type = "ADD";
    this.kpiId = "";
    this.kpiName = "";
    this.selectedTool = "";
    this.category = "";
    this.groupName = "";
    this.dataSource = "";
    this.dbQuery = "";
    this.isActive = "";
    this.selectedUsecase = "";
    this.resultField = "";
    this.isForecast = false;
  }

  categorySelected(selected: string) {
    if (selected == "PREDICTION" && this.usecaseList.length == 0) {
      this.messageDialog.openSnackBar(
        "No Mojo_deployed usecase found for prediction.",
        "error"
      );
      this.category = "";
      this.enableUsecase = false;
    } else if (selected == "PREDICTION") {
      this.enableUsecase = true;
    } else {
      this.selectedUsecase = "";
      this.enableUsecase = false;
      this.isForecast = false;
    }
  }

  usecaseSelected(selected: string) {
    this.isForecast = true;
    var usecase = this.usecaseDetails.find(
      ({ usecaseName }) => usecaseName === selected
    );
    this.resultField = usecase.predictionColumn;
  }

  redirectToLandingPage() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
    };
    this.router.navigate(["InSights/Home/kpicreation"], navigationExtras);
  }
}
