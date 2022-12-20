/*******************************************************************************
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
 ******************************************************************************/
import {
  Component,
  ElementRef,
  OnInit,
  Renderer2,
  ViewChild,
} from "@angular/core";
import { FormBuilder, FormControl, FormGroup } from "@angular/forms";
import { GrafanaAuthenticationService } from "@insights/common/grafana-authentication-service";
import { LandingPageService } from "../../landing-page/landing-page.service";
import { ReportManagementService } from "../../reportmanagement/reportmanagement.service";
import { MessageDialogService } from "../../application-dialog/message-dialog-service";
import { MatDialog } from "@angular/material/dialog";
import { DashboardPreviewConfigDialog } from "./../dashboard-preview-configuration-dialog";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { ActivatedRoute, Router } from "@angular/router";
import { MatOption } from "@angular/material/core";
import { TimeRange } from "./../timeRangeJson";
import { EmailConfigurationDialog } from "../../reportmanagement/report-configuration/email-configuration-dialog";
import { DataSharedService } from "@insights/common/data-shared-service";

export interface queryData {
  index: any;
  query: any;
}

@Component({
  selector: "edit-dashboard",
  templateUrl: "./edit-dashboard.component.html",
  styleUrls: [
    "./edit-dashboard.component.scss",
    "./../dashboard-pdf-download.component.scss",
    "./../../home.module.scss",
  ],
  providers: [TimeRange],
})
export class EditDashboardComponent implements OnInit {
  dashboardForm: FormGroup;
  orgArr = [];
  asyncResult: any;
  dashboardList: any;
  dashOptions = [];
  optionRes: any;
  asyncOptions: any;
  globalMap = new Map<String, String[]>();
  recentdashIds: number[];
  repsonseFromGrafana: any;
  listOfSchedule = [];
  reponseForschedule: any;
  emailDetails: any;
  templateVariableArr: any = [];
  $portfolio: any;
  timeRange: any;
  relativeRange = [];
  otherRelativeRange = [];
  queryArr: any = [];
  variableForm: FormGroup;
  timeValue: any;
  range = new FormGroup({
    start: new FormControl(),
    end: new FormControl(),
  });
  organisation: any;
  dashboard: any;
  frequency: any;
  emailAdd: any;
  mailSubject: any;
  mailBody: any;
  disableSave: boolean = true;
  dashUrl: string;
  urlArray: any = [];
  urlString: string = "";
  pdfType: any;
  saveUrl: string;
  totalMap = new Map<String, String[]>();
  @ViewChild("allSelected") allSelected: MatOption;
  type: any;
  editData: any;
  filterMap = new Map<any, String[]>();
  relativeValue: any;
  otherValue: any;
  isDatainProgress: boolean = false;
  showTimePicker: boolean = true;
  scheduleList = ["ONETIME", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"];
  loadTime: any;
  title: string;
  enableEmail: boolean;
  userName: string;
  theme: string;
  themes: any;
  inputDataJson:any;

  constructor(
    public router: Router,
    private grafanaService: GrafanaAuthenticationService,
    private landingService: LandingPageService,
    public reportmanagementservice: ReportManagementService,
    public messageDialog: MessageDialogService,
    private dialog: MatDialog,
    private el: ElementRef,
    private renderer: Renderer2,
    private formBuilder: FormBuilder,
    public relativeTime: TimeRange,
    public route: ActivatedRoute,
    public dataShare: DataSharedService
  ) {}

  ngOnInit() {
    this.type = "";
    this.disableSave = true;
    this.getOrgs();
    this.themes = {
      dark: "dark",
      light: "light",
    };
    this.relativeRange = this.relativeTime.relativeJson;
    this.otherRelativeRange = this.relativeTime.otherRelativeJson;
    this.route.queryParams.subscribe((params) => {
      this.initialiseVariables(params);
      this.getDashboardJson(params.dashboard);
      this.userName = params.userName;
    });
    this.grafanaService.onOkSubject.subscribe((res) => {
      if (res === "OK") {
        this.disableSave = false;
      }
    });
    this.grafanaService.iconClkSubject.subscribe((res) => {
      if (res === "CLICK") {
        this.disableSave = true;
      }
    });
    this.grafanaService.getEmailConfigurationStatus().then((response) => {
      console.log(response.data);
      this.enableEmail = response.data;
    });
  }
  initialiseVariables(params) {
    this.getDashboardsByOrg(Number(params.organisation));
    this.inputDataJson=params;
    this.loadTime = params.loadTime == undefined ? "30" : params.loadTime;
    this.type = params.type;
    this.editData = params;
    this.organisation = params.organisation;
    this.pdfType = params.pdfType;
    this.frequency = params.scheduleType;
    this.timeRange = params.range;
    this.title = params.title;
    this.theme = params.theme;
    if (this.timeRange === "absolute") {
      this.range.controls["start"].setValue(params.from);
      this.range.controls["end"].setValue(params.to);
    }
    if (this.timeRange === "relative") {
      this.relativeValue = params.rangeText;
    }
    if (this.timeRange === "other") {
      this.otherValue = params.rangeText;
    }

    if (params.emailDetails != null) {
      this.emailDetails = JSON.parse(params.emailDetails);
      console.log(this.emailDetails);
    }

    this.filterMap = new Map<any, String[]>();
    let varArr = [];
    varArr = this.editData.variables.split(",");
    varArr.forEach((value) => {
      let keyVal = [];
      keyVal = value.split("=");
      if (this.filterMap.has(keyVal[0])) {
        this.filterMap.get(keyVal[0]).push(keyVal[1]);
      } else {
        if (keyVal[0] !== undefined && keyVal[1] !== undefined)
          this.filterMap.set(keyVal[0], [keyVal[1]]);
      }
    });
  }
  public async getOrgs() {
    let currentUserWithOrgs = this.dataShare.getUserOrgArray();
    if (currentUserWithOrgs !== undefined) {
      this.orgArr = currentUserWithOrgs.filter((org) => org.role === "Admin");
    }
  }
  async loadSchedule() {
    this.reponseForschedule = await this.reportmanagementservice.getSchedule();
    if (
      this.reponseForschedule != null &&
      this.reponseForschedule.status == "success"
    ) {
      this.scheduleList = this.reponseForschedule.data;
    } else {
      this.messageDialog.openSnackBar(
        "Failed to load the schedules.Please check logs for more details.",
        "error"
      );
    }
  }
  async getDashboardsByOrg(orgId) {
    this.dashboardList = [];
    this.dashOptions = [];
    console.log(orgId);
    this.repsonseFromGrafana = await this.landingService.getDashboardList(
      orgId
    );

    if (this.repsonseFromGrafana.status == "success") {
      this.dashboardList = this.repsonseFromGrafana.data;
      this.dashboardList.forEach((res) => {
        this.dashOptions.push(res);
      });
    }
    this.dashboard = this.dashOptions.filter(
      (dash) => dash.uid === this.editData.dashboard
    )[0].uid;
  }
  public async onChangeVariables(event, data) {
    this.isDatainProgress = true;
    if (data.multi) {
      let arr = [];
      arr = this.variableForm.controls[data.name].value;
      let index = this.variableForm.controls[data.name].value.indexOf("All");
      this.variableForm.controls[data.name].setValue(arr);
    }
    let queryArr = [];
    this.templateVariableArr.forEach((list, index) => {
      if (list.type === "query") {
        let queryObj = {
          index: index,
          query: list.query,
        };
        queryArr.push(queryObj);
      }
    });
    for (let [key, value] of Object.entries(this.variableForm.value)) {
      if (Array.isArray(value)) {
        if (value[0] === "All") {
          if (this.totalMap.has("$" + key))
            this.globalMap.set("$" + key, this.totalMap.get("$" + key));
        } else {
          if (this.globalMap.has("$" + key)) {
            if (value.length > 0) {
              this.globalMap.set(
                "$" + key,
                this.variableForm.controls[key].value
              );
            } else {
              if (this.totalMap.has("$" + key))
                this.globalMap.set("$" + key, this.totalMap.get("$" + key));
            }
          }
        }
      } else {
        if (value === "All") {
          if (this.totalMap.has("$" + key))
            this.globalMap.set("$" + key, this.totalMap.get("$" + key));
        } else {
          if (this.globalMap.has("$" + key)) {
            if (value !== "") {
              this.globalMap.set(
                "$" + key,
                this.variableForm.controls[key].value
              );
            } else {
              if (this.totalMap.has("$" + key))
                this.globalMap.set("$" + key, this.totalMap.get("$" + key));
            }
          }
        }
      }
    }
    for (let query of queryArr) {
      if (query.query.includes("$")) {
        let optionData = [];
        for (let [key, value] of this.globalMap.entries()) {
          if (!Array.isArray(value)) {
            query.query = query.query.replace(
              key,
              "[" + JSON.stringify(value) + "]"
            );
          } else {
            query.query = query.query.replace(key, JSON.stringify(value));
          }
        }
        this.asyncOptions = await this.grafanaService.getTemplateByQuery({
          query: query.query,
        });
        this.asyncOptions.results[0].data.forEach((element) => {
          optionData.push(element.row[0]);
        });
        this.templateVariableArr[query.index].options = optionData;
      }
    }
    this.isDatainProgress = false;
  }
  validatePreview() {
    let filtArr = [];
    let containsNull: boolean;
    let valid = true;
    if (this.variableForm !== undefined) {
      filtArr = Object.values(this.variableForm.value);
      containsNull = filtArr.some(function (el) {
        return el === "";
      });
      if (containsNull === true) {
        valid = false;
        this.messageDialog.openSnackBar(
          "Please select filter details.",
          "error"
        );
        return;
      }
    }
    if (
      this.organisation == undefined ||
      this.pdfType == undefined ||
      this.dashboard == undefined ||
      this.frequency == undefined ||
      this.title == undefined ||
      this.organisation === "" ||
      this.pdfType === "" ||
      this.dashboard === "" ||
      this.frequency === "" ||
      this.title === "" ||
      this.loadTime === ""
    ) {
      valid = false;
      this.messageDialog.openSnackBar("Please fill mandatory fields", "error");
    }
    return valid;
  }
  public async getDashboardJson(uuid) {
    this.globalMap.clear();
    this.totalMap.clear();
    this.templateVariableArr = [];
    this.asyncResult = "";
    this.queryArr = [];
    this.asyncResult = await this.grafanaService.getDashboardByUid(
      uuid,
      this.organisation
    );
    if (this.asyncResult.status === "success") {
      this.templateVariableArr = [];
      this.showTimePicker = this.asyncResult.data.dashboard.timepicker.hidden;
      this.templateVariableArr =
        this.asyncResult.data.dashboard.templating.list;
      this.templateVariableArr.forEach((ele, i) => {
        if (!ele.multi) {
          this.templateVariableArr[i]["selectedValue"] = "";
        }
      });
      let selectJson = {};
      this.mailSubject = this.asyncResult.data.meta.slug;
      this.templateVariableArr.forEach((element) => {
        if (element.hide !== 2) {
          if (element.type === "textbox") {
            selectJson[element.name] = [element.options[0].value];
          } else {
            selectJson[element.name] = [""];
          }
        }
      });
      this.variableForm = this.formBuilder.group(selectJson);
      if (this.templateVariableArr.length > 0) {
        this.templateVariableArr.forEach((list, index) => {
          if (list.type === "query") {
            let queryObj = {
              index: index,
              query: list.query,
            };
            this.queryArr.push(queryObj);
          } else {
            let customArray;
            if (list.query.includes(",")) {
              customArray = list.query.split(",");
            } else {
              customArray = list.query;
            }

            this.globalMap.set(
              "$" + this.templateVariableArr[index].name,
              customArray
            );
            this.totalMap.set(
              "$" + this.templateVariableArr[index].name,
              customArray
            );
          }
        });
        this.getOptionsByQuery();
      }
    }
    for (let [key, value] of this.filterMap.entries()) {
      if (key !== "from" && key !== "to") {
        if (value !== undefined)
          this.variableForm.controls[key].patchValue(value);
      }
    }
    this.templateVariableArr.forEach((element, index) => {
      if (!element.multi && element.hide != 2) {
        this.templateVariableArr[index]["selectedValue"] =
          this.variableForm.get(element.name).value;
      }
    });
    this.getUrlArray();
    return this.asyncResult;
  }
  public async getOptionsByQuery() {
    for (let query of this.queryArr) {
      if (!query.query.includes("$")) {
        let optionData = [];
        let asyncOptions: any;
        asyncOptions = await this.grafanaService.getTemplateByQuery({
          query: query.query,
        });
        asyncOptions.results[0].data.forEach((element) => {
          optionData.push(element.row[0]);
        });
        this.templateVariableArr[query.index].options = optionData;
        this.globalMap.set(
          "$" + this.templateVariableArr[query.index].name,
          optionData
        );
        this.totalMap.set(
          "$" + this.templateVariableArr[query.index].name,
          optionData
        );
      } else {
        let optionData = [];
        let asyncOptions: any;
        for (let [key, value] of this.globalMap.entries()) {
          query.query = query.query.replace(key, JSON.stringify(value));
        }
        asyncOptions = await this.grafanaService.getTemplateByQuery({
          query: query.query,
        });
        asyncOptions.results[0].data.forEach((element) => {
          optionData.push(element.row[0]);
        });
        this.templateVariableArr[query.index].options = optionData;
        this.globalMap.set(
          "$" + this.templateVariableArr[query.index].name,
          optionData
        );
        this.totalMap.set(
          "$" + this.templateVariableArr[query.index].name,
          optionData
        );
      }
    }
  }
  addEmailConfig() {
    console.log(this.enableEmail);
    if (!this.enableEmail) {
      return;
    }
    var self = this;
    const dialogRef = self.dialog.open(EmailConfigurationDialog, {
      panelClass: "custom-dialog-container",
      width: "70%",
      disableClose: true,
      data: {
        screen: "dashboard",
        subject: this.title,
        emailDetails: this.emailDetails,
        type: "edit",
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        self.emailDetails = result;
      }
    });
  }
  async queryForRecentDashboards(orgId) {
    let impressions =
      (await window.localStorage[this.impressionKey(orgId)]) || "[]";
    impressions = JSON.parse(impressions);
    impressions = impressions.filter(this.isNumber);
    this.recentdashIds = impressions;
  }
  list() {
    this.router.navigate(["InSights/Home/dash-pdf-download"], {
      skipLocationChange: true,
    });
  }
  isNumber(element) {
    if (typeof element === "number") {
      return true;
    } else {
      return false;
    }
  }

  impressionKey(orgId) {
    return "dashboard_impressions-" + orgId;
  }
  onRangeChange(event) {
    if (event.value === "relative" || event.value === "other") {
      this.range.reset();
    }
    this.timeValue = "";
  }
  getUrlArray() {
    this.urlArray = [];
    let variables;
    variables = this.urlString;
    let dashboard = this.asyncResult.data.dashboard;
    if (dashboard.panels.length > 0) {
      dashboard.panels.forEach((x) => {
        if (x.type !== "row" && x.type !== "text") {
          this.urlArray.push(
            "GRAFANA_URL" +
              "/d/" +
              dashboard.uid +
              "/" +
              this.asyncResult.data.meta.slug +
              "?" +
              "viewPanel=" +
              x.id +
              "&" +
              variables +
              "&theme=" +
              this.theme
          );
        } else if (x.type === "row" && x.collapsed) {
          if (Array.isArray(x.panels)) {
            if (x.type !== "text")
              x.panels.forEach((x) => {
                this.urlArray.push(
                  "GRAFANA_URL" +
                    "/d/" +
                    dashboard.uid +
                    "/" +
                    this.asyncResult.data.meta.slug +
                    "?" +
                    "viewPanel=" +
                    "&" +
                    variables +
                    "&theme=" +
                    this.theme
                );
              });
          }
        }
      });
    } else {
      this.messageDialog.openSnackBar("No Panels to Download!", "error");
      return;
    }
  }
  onPreviewClick() {
    if (this.validatePreview() === true) {
      this.previewDashboard();
    }
  }
  previewDashboard() {
    this.dashUrl = "";
    this.urlString = "";
    let rangeData = this.range.value;
    let absStartDt = new Date(rangeData.start).getTime();
    let absEndDt = new Date(rangeData.end).getTime();
    let previewData = this.variableForm.value;
    for (let [key, value] of Object.entries(previewData)) {
      if (Array.isArray(value)) {
        value.forEach((val) => {
          this.urlString = this.urlString + "var-" + key + "=" + val + "&";
        });
      } else {
        this.urlString = this.urlString + "var-" + key + "=" + value + "&";
      }
    }
    if (absStartDt === 0 && absEndDt === 0) {
      if (this.timeRange === "relative") {
        this.timeValue = this.relativeRange.filter(
          (val) => val.text === this.relativeValue
        );
      }
      if (this.timeRange === "other") {
        this.timeValue = this.otherRelativeRange.filter(
          (val) => val.text === this.otherValue
        );
      }
      if (this.timeValue === undefined || this.timeValue === "") {
        this.urlString =
          this.urlString +
          "from=" +
          this.asyncResult.data.dashboard.time.from +
          "&to=" +
          this.asyncResult.data.dashboard.time.to;
      } else {
        this.urlString =
          this.urlString +
          "from=" +
          this.timeValue[0].from +
          "&to=" +
          this.timeValue[0].to;
      }
    } else {
      this.urlString =
        this.urlString + "from=" + absStartDt + "&to=" + absEndDt;
    }
    var self = this;
    var decode =
      InsightsInitService.grafanaHost +
      "/dashboard/script/iSight_ui3.js?url=" +
      InsightsInitService.grafanaHost +
      "/dashboard/db/" +
      this.asyncResult.data.meta.slug +
      "?" +
      this.urlString;
    console.log("Preview dashboard url ", decode);
    var dashboardUri = this.asyncResult.data.meta.url;
    dashboardUri = dashboardUri.substring(dashboardUri.indexOf("/d"));
    this.dashUrl =
      InsightsInitService.grafanaHost +
      "/dashboard/script/iSight_ui3.js?url=" +
      encodeURIComponent(
        InsightsInitService.grafanaHost +
          dashboardUri +
          "?orgId=" +
          this.organisation +
          "&" +
          this.urlString +
          "&theme=" +
          this.theme
      );
    this.saveUrl =
      "GRAFANA_URL" +
      "/dashboard/db/" +
      this.asyncResult.data.meta.slug +
      "?orgId=" +
      this.organisation +
      "&" +
      this.urlString +
      "&theme=" +
      this.theme;
    this.getUrlArray();
    const dialogRef = self.dialog.open(DashboardPreviewConfigDialog, {
      panelClass: "custom-dialog-container",
      height: "85%",
      width: "86%",
      disableClose: true,
      data: {
        route: this.dashUrl,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result == "confirm") {
        this.save();
      }
    });
  }
  onTimeChange(event) {
    this.timeValue = event.value;
  }
  save() {
    this.isDatainProgress = true;
    this.getUrlArray();
    let variables = "";
    let metaObj = [] as any;
    let saveObj = {};
    metaObj[0] = { testDB: "false" };
    let rangeData = this.range.value;
    let absStartDt = new Date(rangeData.start).getTime();
    let absEndDt = new Date(rangeData.end).getTime();
    for (let [key, value] of Object.entries(this.variableForm.value)) {
      if (Array.isArray(value)) {
        value.forEach((val) => {
          variables = variables + key + "=" + val + ",";
        });
      } else {
        variables = variables + key + "=" + value + ",";
      }
    }
    if (!this.showTimePicker) {
      if (absStartDt === 0 && absEndDt === 0) {
        saveObj["from"] = this.timeValue[0].from;
        saveObj["to"] = this.timeValue[0].to;
        saveObj["rangeText"] = this.timeValue[0].text;
        variables =
          variables +
          "from=" +
          this.timeValue[0].from +
          ",to=" +
          this.timeValue[0].to;
      } else {
        saveObj["from"] = rangeData.start;
        saveObj["to"] = rangeData.end;
        variables = variables + "from=" + absStartDt + ",to=" + absEndDt;
      }
    }
    saveObj["loadTime"] = this.loadTime;
    saveObj["id"] = this.editData.id;
    saveObj["source"] = "PLATFORM";
    saveObj["workflowId"] = this.editData.workflowId;
    saveObj["title"] = this.title;
    saveObj["pdfType"] = [this.pdfType];
    saveObj["variables"] = variables;
    saveObj["dashUrl"] = this.saveUrl;
    saveObj["panelUrls"] = this.urlArray;
    saveObj["metadata"] = metaObj;
    saveObj["range"] = this.timeRange;
    saveObj["scheduleType"] = this.frequency;
    saveObj["organisation"] = this.organisation;
    saveObj["dashboard"] = this.dashboard;
    saveObj["edit"] = true;
    saveObj["userName"] = this.userName;
    saveObj["theme"] = this.theme;
    if (this.emailDetails != null) {
      saveObj["emailDetails"] = this.emailDetails;
    }

    let requestObj = JSON.stringify(saveObj);
    var self = this;
    var dialogmessage = " Do you want to save the changes? ";
    var title = "Edit Dashboard Configuration ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.grafanaService
          .updateDashboardAsPDF(saveObj)
          .then(function (response) {
            if (response.status === "success") {
              self.disableSave = true;
              self.messageDialog.openSnackBar(
                "Updated Successfully",
                "SUCCESS"
              );
              self.router.navigateByUrl("InSights/Home/dash-pdf-download", {
                skipLocationChange: true,
              });
            } else if (response.status === "failure") {
              self.messageDialog.openSnackBar(response.message, "ERROR");
            }
          });
      }
    });
    this.isDatainProgress = true;
  }
  reset() {
    this.disableSave = true;
    this.organisation = "";
    this.pdfType = "";
    this.loadTime = "";
    this.dashboard = "";
    this.frequency = "";
    this.emailAdd = "";
    this.mailSubject = "";
    this.mailBody = "";
    this.templateVariableArr = [];
    if (this.variableForm !== undefined) this.variableForm.reset();
    if (this.range !== undefined) this.range.reset();
    this.showTimePicker = true;
  }
  dataReset(){
    console.log(this.inputDataJson);
      this.pdfType = this.inputDataJson.pdfType;
      this.frequency = this.inputDataJson.scheduleType;;
      this.loadTime = this.inputDataJson.loadTime == undefined ? "30" :  this.inputDataJson.loadTime;;
      this.theme = this.inputDataJson.theme;
      this.timeRange = this.inputDataJson.range;
  }

  resetOnDashboardChange() {
    this.disableSave = true;
    this.frequency = "";
    this.emailAdd = "";
    this.mailSubject = "";
    this.mailBody = "";
    this.templateVariableArr = [];
    if (this.variableForm !== undefined) this.variableForm.reset();
    if (this.range !== undefined) this.range.reset();
    this.showTimePicker = true;
  }


}
