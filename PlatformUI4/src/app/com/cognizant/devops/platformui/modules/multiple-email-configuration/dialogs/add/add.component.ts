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
import { Component, Inject, OnInit } from "@angular/core";
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from "@angular/forms";
import {  MatDialogRef,MAT_DIALOG_DATA } from "@angular/material/dialog";
import { ActivatedRoute, NavigationExtras, Router } from "@angular/router";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DashboardListComponent } from "@insights/app/modules/dashboard-pdf-download/dashboard-list/dash-list.component";
import { ReportManagementService } from "@insights/app/modules/reportmanagement/reportmanagement.service";
import { DataSharedService } from "@insights/common/data-shared-service";

import { MultipleEmailConfigService } from "@insights/app/modules/multiple-email-configuration/multiple-email-config.service";
import { MultipleEmailConfigurationComponent } from "@insights/app/modules/multiple-email-configuration/multiple-email-configuration.component";

@Component({
  selector: "app-add",
  templateUrl: "./add.component.html",
  styleUrls: ["./add.component.scss", "./../../../home.module.scss"],
})
export class AddComponent implements OnInit {
  type: string;
  onEdit: boolean = false;
  batchName: string= "";
  schedule: string= "";
  receiverEmailAddress: string = "";
  receiverCCEmailAddress: string= "";
  receiverBCCEmailAddress: string= "";
  mailSubject: string= "";
  mailBodyTemplate: string= "";
  scheduleList = ["ONETIME", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"];
  emailConfigForm: FormGroup;
  reponseForschedule: any;
  responseForDashList: any;
  reportNameArr: any;
  reportIds: [];
  source: any;
  limit:boolean=false;
  reportsWithTitle: any;
  emptyReport = this.formBuilder.group({
    report: [{}, Validators.required],
  });  
  scriptReg: string = "<script>";
  messageDialogText:any;
  emailConfigStatus :string="";
  emailConfigStatusCode: string="";

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public multiEmailConfigService: MultipleEmailConfigService,
    public dialogRef: MatDialogRef<AddComponent>,
    public route: ActivatedRoute,
    public router: Router,
    public reportmanagementservice: ReportManagementService,
    public messageDialog: MessageDialogService,
    public dataShare: DataSharedService,
    public formBuilder: FormBuilder,
    public multipleEmailService: MultipleEmailConfigService,
   // public multipleEmailComponent:MultipleEmailConfigurationComponent
  ) {}

  ngOnInit() {
    this.type = this.data["type"];
    //this.reports.push(this.emptyReport);
    if (this.type === "EDIT") {
      this.onEdit = true;
      let selectedEmailConfig = this.data["selectedEmailConfig"];
      console.log(selectedEmailConfig);

      this.batchName = selectedEmailConfig.batchName;
      this.schedule = selectedEmailConfig.schedule;
      this.receiverEmailAddress =
        selectedEmailConfig.emailDetails.receiverEmailAddress;
      this.receiverCCEmailAddress =
        selectedEmailConfig.emailDetails.receiverCCEmailAddress;
      this.receiverBCCEmailAddress =
        selectedEmailConfig.emailDetails.receiverBCCEmailAddress;
      this.mailSubject = selectedEmailConfig.emailDetails.mailSubject;
      this.mailBodyTemplate = selectedEmailConfig.emailDetails.mailBodyTemplate;
      this.reportNameArr = selectedEmailConfig.reports;

      let selected = JSON.parse(this.reportNameArr);

      this.emailConfigForm = this.formBuilder.group({
        batchName: [
          { value: this.batchName, disabled: this.onEdit },
          [Validators.required],
        ],
        schedule: [this.schedule, [Validators.required]],
        receiverEmailAddress: [
          this.receiverEmailAddress,
          [Validators.required],
        ],
        receiverCCEmailAddress: [this.receiverCCEmailAddress],
        receiverBCCEmailAddress: [this.receiverBCCEmailAddress],
        mailSubject: [this.mailSubject, [Validators.required]],
        mailBodyTemplate: [this.mailBodyTemplate, [Validators.required]],
        reports: this.formBuilder.array([])
      });
      this.addReportInitially(selected)
    } else {
      this.emailConfigForm = this.formBuilder.group({
        batchName: ["", [Validators.required]],
        schedule: ["", [Validators.required]],
        receiverEmailAddress: ["", [Validators.required]],
        receiverCCEmailAddress: [""],
        receiverBCCEmailAddress: [""],
        mailSubject: ["", [Validators.required]],
        mailBodyTemplate: ["", [Validators.required]],
        reports: this.formBuilder.array([], [Validators.required]),
      });
      this.reports.push(this.emptyReport);
    }
    
    this.getAllReportList();
    this.source = this.data.source;
  }
  sendMail(){
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        source:"Report",
      },
    };
    this.router.navigate(["InSights/Home/email-configuration"], navigationExtras)
  }

  get reports() {
    if(this.emailConfigForm.value["reports"].length>9)
    this.limit=true;
    else
    this.limit=false;
    return this.emailConfigForm.controls["reports"] as FormArray;
  }

  addReport() {
    let report = this.formBuilder.group({
      report: [{}, Validators.required],
    });
    this.reports.push(report);
    
  }

  addReportInitially(reportsArr) {
    reportsArr.forEach(element => {
      let report = this.formBuilder.group({
        report: [{...element}, Validators.required],
      });
      this.reports.push(report);
    });
  
  }

  removeReport(reportIndex: number) {
    this.reports.removeAt(reportIndex);
  }

  async getAllReportList() {
    this.responseForDashList =
      await this.multiEmailConfigService.fetchReportList(
        this.dataShare.getLoginName(),
        this.data.source
      );
    this.reportsWithTitle = this.responseForDashList.data;
    this.reportsWithTitle = this.reportsWithTitle.sort((a, b) =>
      a.reportName > b.reportName ? 1 : -1
    );
    console.log(this.reportsWithTitle);
    
  }

  hasDuplicates(values){
    return values.map(v => v.report).length > new Set(values.map(v => v.report.id)).size;
  }

  getReportName(){
    console.log("Inside getReportName");
       
    for(var data of this.reportsWithTitle){
      console.log("data:",data);
      
    }
  }

  compareReports(report1:any, report2: any){
    return report1 && report2 && report1.id === report2.id  
  }

  closeEmailConfigDialog() {
    this.dialogRef.close(null);
  }

  async getAssesmentReport() {}

  onSubmit() {
    if (this.emailConfigForm.valid) {
      let self = this;
      let requestObj = {};
      let emailDetails = {};
      console.log(this.reports.value);
      
      
      let selectedReports  = this.emailConfigForm.value["reports"].map((report) => report.report);
      requestObj["batchName"] = this.emailConfigForm.value["batchName"];
      requestObj["source"] = this.source;
      requestObj["schedule"] = this.emailConfigForm.value["schedule"];
      requestObj["reports"] = selectedReports;
      requestObj["schedule"] = this.emailConfigForm.value["schedule"];

      emailDetails["receiverEmailAddress"] =
        this.emailConfigForm.value["receiverEmailAddress"];
      emailDetails["receiverCCEmailAddress"] = this.emailConfigForm.value[
        "receiverCCEmailAddress"
      ]
        ? this.emailConfigForm.value["receiverCCEmailAddress"]
        : "";
      emailDetails["receiverBCCEmailAddress"] = this.emailConfigForm.value[
        "receiverBCCEmailAddress"
      ]
        ? this.emailConfigForm.value["receiverBCCEmailAddress"]
        : "";
      emailDetails["mailSubject"] = this.emailConfigForm.value["mailSubject"];
      emailDetails["mailBodyTemplate"] =
        this.emailConfigForm.value["mailBodyTemplate"];

      requestObj["emailDetails"] = emailDetails;

      console.log(requestObj);
      var dialogmessage = " Do you want to save the changes? ";
      var title = "Save Email Configuration ";
      const dialogReff = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "30%"
      );

      dialogReff.afterClosed().subscribe((result) => {
        
        if (result == "yes") {
          self.multiEmailConfigService
            .saveMultiEmailConfig(requestObj)
            .then(function (response) {
              console.log(response.status);
              
              if (response.status == "success") {
                self.emailConfigStatus = "Email Configuration Saved Successfully";
                self.emailConfigStatusCode = "success"; 
                self.dialogRef.close(requestObj);
              self.messageDialog.openSnackBar(
                self.emailConfigStatus,
                self.emailConfigStatusCode
              );
              this.router.navigateByUrl("InSights/Home/email-configuration", {
                skipLocationChange: true,
              });
            
                
              } else if(response.status == "failure") {
                if(response.message == "GroupEmailConfiguration with the given Batch name already exists"){
                  self.emailConfigStatus = "Batch Name already exists!";
                }
                else{
                  self.emailConfigStatus = "Email Configuration Registration failed. Please check the logs for more details.";
                }
                self.emailConfigStatusCode = "failure"; 
                self.messageDialog.openSnackBar(self.emailConfigStatus, "error");
              }
            });
        }     
      });
     }
  }

  reset() {
    this.emailConfigForm.reset();
  }

  onPreviewClick() {
    if (this.validatePreview() === true && this.onEdit == false) {
      console.log("Submut");
      this.onSubmit();
    } else if (this.validatePreview() === true && this.onEdit == true) {
      console.log("update")
      this.updateEmailConfig();
    }
    else{
      console.log("error")
      this.messageDialog.openSnackBar(this.messageDialogText,"error");
    }
  }

  updateEmailConfig() {
    let self = this;
    let requestObj = {};
    let emailDetails = {};
    if (this.emailConfigForm.valid) {
      
      this.reportIds = this.emailConfigForm.value["reports"].map((report) => report.report);
      requestObj["id"]=this.data["selectedEmailConfig"].groupTemplateId;
      requestObj["source"] = this.source;
      requestObj["reports"] = this.reportIds;
      emailDetails["receiverEmailAddress"] =
        this.emailConfigForm.value["receiverEmailAddress"];
      emailDetails["receiverCCEmailAddress"] =
        this.emailConfigForm.value["receiverCCEmailAddress"];
      emailDetails["receiverBCCEmailAddress"] =
        this.emailConfigForm.value["receiverBCCEmailAddress"];
      emailDetails["mailSubject"] = this.emailConfigForm.value["mailSubject"];
      emailDetails["mailBodyTemplate"] =
        this.emailConfigForm.value["mailBodyTemplate"];

      requestObj["emailDetails"] = emailDetails;

      console.log(requestObj);
      var dialogmessage = " Do you want to save the changes? ";
      var title = "Edit Email Configuration ";
      const dialogReff = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "30%"
      );
      dialogReff.afterClosed().subscribe((result) => {
        if (result == "yes") {
          self.multiEmailConfigService
            .editMultiEmailConfig(requestObj)
            .then(function (response) {
              console.log(response.status);
              if (response.status == "success") {
                self.emailConfigStatus = "Email Configuration Updated Successfully";
                self.emailConfigStatusCode = "success"; 
                self.dialogRef.close(requestObj);
              self.messageDialog.openSnackBar(
                self.emailConfigStatus,
                self.emailConfigStatusCode
              );
              this.router.navigateByUrl("InSights/Home/email-configuration", {
                skipLocationChange: true,
              });
            
                
              } else if(response.status == "failure") {
                
                  self.emailConfigStatus = "Email Configuration update failed. Please check the logs for more details.";
            
                self.emailConfigStatusCode = "failure"; 
                self.messageDialog.openSnackBar(self.emailConfigStatus, "error");
              }
            });
            
        }
        else{
          if(this.onEdit){
            self.emailConfigForm.controls['batchName'].disable();
          }
        }
      });
    }
    
  }
  
  validatePreview1() {
    if(this.onEdit){
      this.emailConfigForm.controls['batchName'].enable();
    }
    let valid: boolean = true;
    if (
      this.batchName == "" ||
      this.schedule == "" ||
      this.emailConfigForm.value.receiverEmailAddress == "" ||
      this.emailConfigForm.value.mailSubject === "" ||
      this.emailConfigForm.value.mailBodyTemplate === "" ||
      this.emailConfigForm.value.batchName == undefined ||
      this.emailConfigForm.value.schedule == undefined ||
      this.emailConfigForm.value.receiverEmailAddress == undefined ||
      this.emailConfigForm.value.mailSubject === undefined ||
      this.emailConfigForm.value.mailBodyTemplate === undefined
    ) {
      valid = false;
      this.messageDialog.openSnackBar("Please fill mandatory fields.", "error");
      return;
    }
    return valid;
  }

  validatePreview(){
    let isValidated:boolean = true;
    let self = this;
    var values=this.emailConfigForm.value["reports"];
     if(this.emailConfigForm.value.batchName == ""||
      this.emailConfigForm.value.schedule == ""){
      isValidated = false;
      this.messageDialogText = "Please fill mandatory fields";
    }else if(this.reports.length<1){
      isValidated = false;
      this.messageDialogText = "Please add at least one report"; 
    }
    else if(this.hasDuplicates(values)){
      isValidated=false;
      this.messageDialogText="Duplicate reports are not allowed";
    }
    else if (
      !this.emailConfigForm.value.receiverEmailAddress &&
      !this.emailConfigForm.value.receiverCCEmailAddress &&
      !this.emailConfigForm.value.receiverBCCEmailAddress
    ) {
      isValidated = false;
      this.messageDialogText = "Please enter one of the recipient email address";
    } else if (
      this.emailConfigForm.value.mailSubject === "" ||
      this.emailConfigForm.value.mailBodyTemplate === "" 
    ) {
      isValidated = false;
      this.messageDialogText = "Please fill mandatory fields";
    }
     else {
      if (this.emailConfigForm.value.receiverEmailAddress) {
        isValidated = this.dataShare.validateEmailAddresses(
          this.emailConfigForm.value.receiverEmailAddress
        );
        if (!isValidated) {
          this.messageDialogText = "Error in To email address format.";
        }
      }
      if (isValidated == true) {
        if (this.emailConfigForm.value.receiverCCEmailAddress) {
          isValidated = this.dataShare.validateEmailAddresses(
            this.emailConfigForm.value.receiverCCEmailAddress
          );
          if (!isValidated) {
            this.messageDialogText = "Error in Cc email address format.";
          }
        }
      }
      if (isValidated == true) {
        if (this.emailConfigForm.value.receiverBCCEmailAddress) {
          isValidated = this.dataShare.validateEmailAddresses(
            this.emailConfigForm.value.receiverBCCEmailAddress
          );
          if (!isValidated) {
            this.messageDialogText = "Error in Bcc email address format.";
          }
        }
      }
      if (isValidated == true) {
        if (this.emailConfigForm.value.batchName) {
          isValidated = this.validateBatchName(
            this.emailConfigForm.value.batchName
          );
          if (!isValidated) {
            this.messageDialogText = "Please enter a valid Batch Name, it cannot be blank";
          }
        }
      }
    }
    if (this.mailBodyTemplate.search(this.scriptReg) !== -1) {
      isValidated = false;
      this.messageDialogText = "Mail body templates cannot have script tags.";
    }
    if(this.emailConfigForm.value.reports.length>0){
      for(let i=0;i<this.emailConfigForm.value.reports.length;i++){
        if(this.emailConfigForm.value.reports[i].report.reportName==""||
        this.emailConfigForm.value.reports[i].report.reportName==undefined){
          console.log("Objecttt"+this.emailConfigForm.value.reports[i].report.reportName);
          isValidated = false;
          this.messageDialogText = "Please select the report"; 
        }
      }
    }
    
   return isValidated;
  }
  
  getReportTitle(id) {
    return this.reportsWithTitle.find((report) => report.id == id).reportName;
  }

  validateBatchName(batchString:string): boolean {
    let batchRegEx = /^[a-zA-Z0-9]*$/;
    let isValid = true;
    for (var batches of batchString) {
      if (!batchRegEx.test(batches)) {
        isValid = false;
        break;
      }
    }
    return isValid;
  }

}