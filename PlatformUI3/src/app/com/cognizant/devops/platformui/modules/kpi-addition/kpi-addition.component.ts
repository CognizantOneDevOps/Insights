import { Component, OnInit } from '@angular/core';
import { KpiService } from './kpi-service';
import { BulkUploadService } from '../bulkupload/bulkupload.service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { ActivatedRoute, Router } from '@angular/router';
import { THIS_EXPR } from '@angular/compiler/src/output/output_ast';
import { ModelManagementService } from '@insights/app/modules/model-management/model-management.service';


@Component({
  selector: 'app-kpi-addition',
  templateUrl: './kpi-addition.component.html',
  styleUrls: ['./kpi-addition.component.css', './../home.module.css']
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
  selectedUsecase: string = '';
  usecaseDetails = [];
  usecaseList = [];
  usecaseResponse: any;
  resultField: any;
  isForecast: boolean = false;
  outputDatasource = [];
  dataSourceOutput: any;

  constructor(public router: Router, public route: ActivatedRoute, public kpiService: KpiService, public messageDialog: MessageDialogService, 
    private bulkuploadService: BulkUploadService, public modelManagementService: ModelManagementService) {
  }

  ngOnInit() {
    this.type = this.kpiService.getType();
    if (this.type === 'EDIT') {
      this.onEdit = true;
      document.getElementById("kpiId").style.backgroundColor = "#e1e3e7";
    }
    this.route.queryParams.subscribe(params => {
      if (params) {
        this.kpiId = params.kpiId;
        this.kpiName = params.kpiName;
        this.selectedTool = params.selectedTool;
        this.category = params.category;
        this.groupName = params.groupName;
        this.dataSource = params.dataSource;
        this.dbQuery = params.dbQuery;
        this.isActive = params.isActive;
        this.resultField = params.resultField;
        this.dataSourceOutput = params.outputDatasource;
        this.selectedUsecase = params.usecase; 
        if(params.usecase == undefined) {
          this.selectedUsecase = "";
        }
        if(this.category == 'PREDICTION') {
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
    }
    catch (error) {
      console.log(error);
    }
  }

  async getKpiDataSource() {
    let dataSourceRes = await this.kpiService.loadKpiDataSource();
    if (dataSourceRes.status == "success") {
      this.dataSourceDetail = dataSourceRes.data;
      this.dataSourceDetail.forEach(element => {
        if(element != 'HYPERLEDGER') {
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
      let toollabelresponse = await this.bulkuploadService.loadUiServiceLocation()
      if (toollabelresponse.status == "success") {
        this.toolsDetail = toollabelresponse.data;
      }
      for (var element of this.toolsDetail) {
        var toolName = (element.toolName);
        var labelName = (element.label);
        this.toolsArr.push(toolName);
        console.log("ToolsArr" + this.toolsArr);
        this.labelsArr.push(labelName);
      }
    }
    catch (error) {
      console.log(error);
    }
  }

  async loadForecastUsecase() {
    this.usecaseResponse = await this.modelManagementService.loadForecastUsecase();
    console.log(this.usecaseResponse);
    if (this.usecaseResponse.status == "success") {
      this.usecaseDetails = this.usecaseResponse.data;
      this.usecaseDetails.forEach(element => {
        this.usecaseList.push(element.usecaseName);
      });
      console.log(this.usecaseList);
    }
  }

  validateKpiData() {
    var isValidated = true;
    if ((this.kpiId === "" || this.kpiId === undefined) || (this.kpiName === "" || this.kpiName === undefined) || (this.selectedTool === "" || this.selectedTool === undefined) ||
      (this.category === "" || this.category === undefined) || (this.groupName === "" || this.groupName === undefined) || (this.dataSource === "" || this.dataSource === undefined) ||
      (this.dbQuery === "" || this.dbQuery === undefined) || (this.isActive === "" || this.isActive === undefined)
      || (this.resultField === "" || this.resultField === undefined)) {
      isValidated = false;
      this.messageDialog.showApplicationsMessage("Please fill mandatory fields", "ERROR");
    }
    else if(this.category == 'PREDICTION' && (this.selectedUsecase === "" || this.selectedUsecase === undefined)) {
      this.messageDialog.showApplicationsMessage("Please select usecase for prediction.", "ERROR");
      isValidated = false;
    }
    if (isValidated) {
      this.onClickSave();
    }
  }
  constructData() {
    var self = this;
    var kpiAPIRequestJson = {};
    kpiAPIRequestJson['kpiId'] = this.kpiId;
    kpiAPIRequestJson['name'] = this.kpiName;
    kpiAPIRequestJson['group'] = this.groupName;
    kpiAPIRequestJson['category'] = this.category;
    kpiAPIRequestJson['toolName'] = this.selectedTool;
    kpiAPIRequestJson['DBQuery'] = this.dbQuery;
    kpiAPIRequestJson['datasource'] = this.dataSource;
    kpiAPIRequestJson['isActive'] = this.isActive
    kpiAPIRequestJson['resultField'] = this.resultField;
    kpiAPIRequestJson['usecase'] = this.selectedUsecase;
    kpiAPIRequestJson['outputDatasource'] = "";
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
    var dialogmessage = " You have updated a KPI <b>" + this.kpiId + "</b> .Do you want continue? "
    var title = "Update KPI ";
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.kpiService.updateDataforKpi(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              self.messageDialog.showApplicationsMessage("<b>" + "Kpi defination updated for KpiId " + self.kpiId, "SUCCESS");
              self.router.navigateByUrl('InSights/Home/kpicreation',{skipLocationChange:true});
              self.type = "EDIT";
            } else {
              self.messageDialog.showApplicationsMessage("<b>" + response.message, "ERROR");
            }
          })
      }
    });
  }
  defaultStop(event) {
    if (event.which != 8 && event.which != 0 && event.which < 48 || event.which > 57) {
      event.preventDefault();
    }
  }

  saveKpiData() {
    var self = this;
    var dialogmessage = " You have created a new KPI <b>" + this.kpiId + "</b> .Do you want continue? "
    var title = "Save KPI ";
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.kpiService.saveDataforKpi(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              setTimeout(() => { self.messageDialog.showApplicationsMessage("<b>" + "Kpi with id " + self.kpiId + "</b> created successfully.", "SUCCESS") }, 500);
              self.router.navigateByUrl('InSights/Home/kpicreation',{skipLocationChange:true});
              self.type = "EDIT";
            } else if (response.message === "KPI already exists") {
              self.messageDialog.showApplicationsMessage("<b>" + "Kpi Id" + self.kpiId + "</b> already exists. Please try again with a new Id.", "ERROR");
            } else if (response.message === "kpi Definition does not have some mandatory field") {
              self.messageDialog.showApplicationsMessage("Kpi Definition does not have some mandatory field.", "ERROR");
            } else {
              self.messageDialog.showApplicationsMessage("Failed to save the Kpi.Please check logs.", "ERROR");
            }
          })
      }
    })


  }
  refreshData() {
    this.type = "ADD";
    this.kpiId = '';
    this.kpiName = '';
    this.selectedTool = '';
    this.category = '';
    this.groupName = '';
    this.dataSource = '';;
    this.dbQuery = '';;
    this.isActive = '';;
    this.selectedUsecase = "";
    this.resultField = "";
    this.isForecast = false;
  }

  categorySelected(selected: string) {
    if(selected == 'PREDICTION' && this.usecaseList.length == 0) {
      this.messageDialog.showApplicationsMessage("No Mojo_deployed usecase found for prediction.", "ERROR");
      this.category = "";
      this.enableUsecase = false;
    } else if(selected == 'PREDICTION') {
      this.enableUsecase = true;
    } else {
      this.selectedUsecase = "";
      this.enableUsecase = false;
      this.isForecast = false;
    }
  }

  usecaseSelected(selected: string) {
    this.isForecast = true;
    var usecase = this.usecaseDetails.find(({usecaseName}) => usecaseName === selected);
    this.resultField = usecase.predictionColumn;
  }

}
