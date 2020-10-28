import { Component, OnInit } from '@angular/core';
import { BulkUploadService } from '../bulkupload/bulkupload.service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material';
import { KpiListDialog } from '../kpiList-Dialog/kpiList-Dialog.component';
import { KpiService } from '../kpi-addition/kpi-service';
import { ContentService } from '../content-config-list/content-service';

@Component({
  selector: 'app-content-config-addition',
  templateUrl: './content-config-add.component.html',
  styleUrls: ['./content-config-add.component.css', './../home.module.css']
})
export class ContentConfigAddition implements OnInit {

  categoryDetail = [];
  dataSourceDetail = [];
  toolsDetail = [];
  labelsArr = [];
  toolsArr = [];
  contentId: any;
  kpiId: any;
  contentName: any;
  isActive: any;
  type: string;
  kpiList: any;
  onEdit: boolean = false;
  message: any;
  expectedTrend: any;
  directionThreshold: any;
  resultField: any;
  threshold: any;
  thresholds: any;
  category: any;
  action: any;
  trend = ["UPWARDS", "DOWNWARDS"];
  thresholdDir = ["ABOVE", "BELOW"];
  actionDetail: any[];
  isDataValid: boolean;
  constructor(public router: Router, public dialog: MatDialog,
    public route: ActivatedRoute, public messageDialog: MessageDialogService,
    private bulkuploadService: BulkUploadService, public kpiService: KpiService, public contentService: ContentService) {
  }

  ngOnInit() {
    this.type = this.contentService.getType();
    this.kpiService.setKpiSubject.subscribe(res => {
      this.kpiId = res.kpiId;
      this.category = res.category;
    })
    this.route.queryParams.subscribe(params => {
      if (params) {
        this.contentId = params.contentId,
          this.contentName = params.contentName,
          this.kpiId = params.kpiId,
          this.expectedTrend = params.expectedTrend,
          this.directionThreshold = params.directionThreshold,
          this.resultField = params.resultField,
          this.action = params.action,
          this.isActive = params.isActive,
          this.message = params.message,
          this.threshold = params.threshold,
          this.thresholds = params.thresholds,
          this.action = params.action;
        this.category = params.category;

      }
    });
    if (this.type === 'EDIT') {
      this.onEdit = true;
      document.getElementById("contentId").style.backgroundColor = "#e1e3e7";
    }
    this.getActions();
  }
  async getActions() {
    var self = this;
    try {
      self.actionDetail = [];
      let actionlabelresponse = await this.contentService.loadActions()
      if (actionlabelresponse.status == "success") {
        this.actionDetail = actionlabelresponse.data;
      }
    }
    catch (error) {
      console.log(error);
    }
  }


  validateKpiData() {
    let cat = this.category;
    var isValidated = true;
    if ((this.contentId === "" || this.contentId === undefined) || (this.contentName === "" || this.contentName === undefined)
      || (this.isActive === "" || this.isActive === undefined) || (this.expectedTrend === "" || this.expectedTrend === undefined)
      || (this.kpiId === "" || this.kpiId === undefined) || (this.message === "" || this.message === undefined)
      || (this.resultField === "" || this.resultField === undefined)) {
      isValidated = false;
    }
    if (this.category === "THRESHOLD") {
      if ((this.threshold === "" || this.threshold === undefined) ||
        (this.directionThreshold === "" || this.directionThreshold === undefined)) {
        isValidated = false
      }
    }
    if (this.category === "THRESHOLD_RANGE") {
      if ((this.thresholds === "" || this.thresholds === undefined) ||
        (this.directionThreshold === "" || this.directionThreshold === undefined)) {
        isValidated = false
      }
    }
    if (isValidated) {
      this.validateCategory();
    } else {
      this.messageDialog.showApplicationsMessage("Please fill mandatory fields", "ERROR");
    }
  }
  validateCategory() {
    this.isDataValid = this.validateJson(this.message);
    if (this.isDataValid) {
      switch (this.category) {
        case 'STANDARD':
          this.valStandardCategory() ? this.onClickSave() : this.messageDialog.showApplicationsMessage("Message should contain Content and Neutral Messages", "ERROR");
          break;
        case 'COMPARISON':
          this.valComparisonCategory() ? this.onClickSave() : this.messageDialog.showApplicationsMessage("Message should contain Positive,Negative and Neutral Messages", "ERROR");
          break;
        case 'THRESHOLD':
          if (this.validateJson(this.threshold)) {
            let actionArr = ['COUNT', 'PERCENTAGE', 'AVERAGE'];
            if (actionArr.includes(this.action)) {
              this.valStandardCategory() ? this.onClickSave() : this.messageDialog.showApplicationsMessage("Message should contain Content and Neutral Messages", "ERROR");
            } else {
              this.messageDialog.showApplicationsMessage("Action should be Count,Percentage or Average", "ERROR");
            }
          } else {
            this.messageDialog.showApplicationsMessage('Message should be in JSON Format', 'ERROR');
          }
          break;
        case 'THRESHOLD_RANGE':
          if (this.validateJson(this.thresholds)) {
            if (this.action === "COUNT" || this.action === "PERCENTAGE") {
              this.validateThresholdRangeCategory() ? this.valStandardCategory() ?
                this.onClickSave() : this.messageDialog.showApplicationsMessage("Message should contain Content and Neutral Messages", "ERROR")
                : this.messageDialog.showApplicationsMessage("Thresholds should contain red,amber and green values", "ERROR");
            } else {
              this.messageDialog.showApplicationsMessage("Action should be either Count or Percentage", "ERROR");
            }
          } else {
            this.messageDialog.showApplicationsMessage('Message should be in JSON Format', 'ERROR');
          }
          break;
        case 'MINMAX':
          if (this.action === 'MIN' || this.action === 'MAX') {
            this.valStandardCategory() ? this.onClickSave() : this.messageDialog.showApplicationsMessage("Message should contain Content and Neutral Messages", "ERROR");
          } else {
            this.messageDialog.showApplicationsMessage("Action should be Min or Max", "ERROR");
          }
          break;
        case 'TREND':
          if (this.action === 'COUNT' || this.action === 'AVERAGE') {
            this.valStandardCategory() ? this.onClickSave() : this.messageDialog.showApplicationsMessage("Message should contain Content and Neutral Messages", "ERROR");
          } else {
            this.messageDialog.showApplicationsMessage("Action should be Count or Average", "ERROR");
          }
          break;
      }
    } else {
      this.messageDialog.showApplicationsMessage('Message should be in JSON Format', 'ERROR');
    }
  }
  valArrayEquality(arr1, arr2) {
    const containsAll = (arr1, arr2) =>
      arr2.every(arr2Item => arr1.includes(arr2Item))

    const sameMembers = (arr1, arr2) =>
      containsAll(arr1, arr2) && containsAll(arr2, arr1);

    return sameMembers(arr1, arr2);
  }
  validateJson(message) {
    try {
      JSON.parse(message);
    } catch (e) {
      return false;
    }
    return true;
  }
  valStandardCategory() {
    let stdMsg = ['contentMessage', 'neutralMessage'];
    let msgArr = Object.keys(JSON.parse(this.message));
    return this.valArrayEquality(stdMsg, msgArr);
  }
  valComparisonCategory() {
    let compMsg = ['positive', 'negative', 'neutral'];
    let msgArr = Object.keys(JSON.parse(this.message));
    return this.valArrayEquality(compMsg, msgArr);
  }
  validateThresholdRangeCategory() {
    let thresholds = ['red', 'amber', 'green'];
    let msgArr = Object.keys(JSON.parse(this.thresholds));
    return this.valArrayEquality(thresholds, msgArr);
  }
  validateContentData() {
    let valid = false;
    switch (this.category) {
      case "Standard":
        valid = this.valStandardCategory();
        break;
      case "Comparision":
        valid = this.valComparisonCategory();
        break;
    }
  }
  openKpiDialog() {
    this.dialog.open(KpiListDialog, {
      panelClass: 'showjson-dialog-container',
      height: "500px",
      width: "550px",
      disableClose: true,

    });

  }
  defaultStop(event) {
    if (event.which != 8 && event.which != 0 && event.which < 48 || event.which > 57) {
      event.preventDefault();
    }
  }
  constructData() {
    var self = this;
    var contentRequestJson = {};
    contentRequestJson['contentId'] = this.contentId;
    contentRequestJson['isActive'] = this.isActive;
    contentRequestJson['expectedTrend'] = this.expectedTrend;
    contentRequestJson['contentName'] = this.contentName;
    contentRequestJson['kpiId'] = this.kpiId;
    contentRequestJson['resultField'] = this.resultField
    contentRequestJson['noOfResult'] = 15;
    contentRequestJson['threshold'] = this.threshold;
    contentRequestJson['action'] = this.action;
    contentRequestJson['directionOfThreshold'] = this.directionThreshold;
    contentRequestJson['message'] = JSON.parse(this.message);
    if (this.category === 'THRESHOLD_RANGE')
      contentRequestJson['thresholds'] = JSON.parse(this.thresholds);

    return contentRequestJson;
  }
  onClickSave() {
    if (this.type === "EDIT") {
      this.updateContentData();
    } else {
      this.saveContentData();
    }
  }
  updateContentData() {
    var self = this;
    var dialogmessage = " You have updated a Content <b>" + this.contentId + "</b> .Do you want continue? "
    var title = "Update Content ";
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.contentService.updateDataforContent(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              self.messageDialog.showApplicationsMessage("<b>" + response.data.message, "SUCCESS");
              self.router.navigateByUrl('InSights/Home/contentConfig');
              self.type = "EDIT";
            } else {
              self.messageDialog.showApplicationsMessage("<b>" + response.data.message, "ERROR");
            }
          })
      }
    });
  }

  saveContentData() {
    var self = this;
    var dialogmessage = " You have created a new Content <b>" + this.kpiId + "</b> .Do you want continue? "
    var title = "Save Content ";
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.contentService.saveDataforContent(JSON.stringify(this.constructData()))
          .then(function (response) {
            let res = response;
            if (response.status == "success") {
              setTimeout(() => { self.messageDialog.showApplicationsMessage("<b>" + "Kpi with id " + self.kpiId + "</b> created successfully.", "SUCCESS") }, 500);
              self.router.navigateByUrl('InSights/Home/contentConfig');
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
    this.contentId = '';
    this.kpiId = '';
    this.contentName = '';
    this.isActive = '';
    this.message = '';
    this.expectedTrend = '';
    this.directionThreshold = '';
    this.resultField = '';
    this.threshold = '';
    this.thresholds = '';
    this.category = '';
    this.action = '';
  }
}
