/********************************************************************************
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
import {
  Component,
  ElementRef,
  OnInit,
  Renderer2,
  ViewChild,
} from "@angular/core";
import { FormBuilder, FormControl, FormGroup } from "@angular/forms";
import { GrafanaAuthenticationService } from "@insights/common/grafana-authentication-service";
import { LandingPageService } from "../landing-page/landing-page.service";
import { ReportManagementService } from "../reportmanagement/reportmanagement.service";
import { MessageDialogService } from "../application-dialog/message-dialog-service";
import { MatDialog } from "@angular/material/dialog";
import { DashboardPreviewConfigDialog } from "./dashboard-preview-configuration-dialog";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { ActivatedRoute, Router } from "@angular/router";
import { MatOption } from "@angular/material/core";
import { TimeRange } from "./timeRangeJson";
import { EmailConfigurationDialog } from "../reportmanagement/report-configuration/email-configuration-dialog";
import { DataSharedService } from "@insights/common/data-shared-service";

export interface queryData {
  index: any;
  query: any;
}

@Component({
  selector: "app-dashboard-pdf-download",
  templateUrl: "./dashboard-pdf-download.component.html",
  styleUrls: [
    "./dashboard-pdf-download.component.scss",
    "./../home.module.scss",
  ],
  providers: [TimeRange],
})
export class DashboardPdfDownloadComponent implements OnInit {
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
  scheduleList = ["ONETIME", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"];
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
  theme: string;
  frequency: any;
  emailAdd: any;
  mailSubject: any;
  mailBody: any;
  disableSave: boolean;
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
  isDatainProgress: boolean = false;
  showTimePicker: boolean = true;
  mailIconDisable: boolean = true;
  loadTime: any;
  title: string;
  enableEmail: boolean;
  themes: any;
  userName: string;

  constructor(
    public router: Router,
    private grafanaService: GrafanaAuthenticationService,
    private landingService: LandingPageService,
    public reportmanagementservice: ReportManagementService,
    public messageDialog: MessageDialogService,
    private dialog: MatDialog,
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
    this.route.queryParams.subscribe((params) => {
      this.userName = params.userName;
    });
    this.theme = this.themes.dark;
    this.relativeRange = this.relativeTime.relativeJson;
    this.otherRelativeRange = this.relativeTime.otherRelativeJson;
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
      this.enableEmail = response.data;
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
  }
  setAll(val, data) {
    let value = [];
    value.push("All");
    this.variableForm.controls[data.name].setValue(value);
  }
  setOption(val, data) {
    let value = [];
    value.push(this.variableForm.controls[data.name].value);
    let index = value.indexOf("All");
    if (index > -1) {
      value.splice(index, 1);
    }
    this.variableForm.controls[data.name].setValue(value);
  }

  public async onChangeVariables(event, data) {
    console.log(data);
    let eve = event;
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
  public async getDashboardJson(dashboardUUID) {
    this.globalMap.clear();
    this.totalMap.clear();
    this.resetOnDashboardChange();
    this.templateVariableArr = [];
    this.asyncResult = "";
    this.queryArr = [];
    console.log("Selected Dashboard UUID " + dashboardUUID);
    this.asyncResult = await this.grafanaService.getDashboardByUid(
      dashboardUUID,
      this.organisation
    );
    console.log(this.asyncResult);
    if (this.asyncResult.status === "success") {
      if (this.enableEmail) {
        this.mailIconDisable = false;
      }
      console.log(typeof this.asyncResult.data);
      if ("message" in this.asyncResult.data) {
        console.log("has property message");
        this.messageDialog.showApplicationsMessage(
          " Dashbaord Json have some issue  : " + this.asyncResult.data.message,
          "ERROR"
        );
      } else {
        this.showTimePicker = this.asyncResult.data.dashboard.timepicker.hidden;
        this.templateVariableArr = [];
        this.templateVariableArr =
          this.asyncResult.data.dashboard.templating.list;
        console.log("Check:", this.templateVariableArr);
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
    }
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
        console.log(this.templateVariableArr);
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
  editEmailConfig() {
    var self = this;
    const dialogRef = self.dialog.open(EmailConfigurationDialog, {
      panelClass: "custom-dialog-container",
      height: "600px !important",
      width: "900px",
      disableClose: true,
      data: {
        screen: "dashboard",
        subject: this.mailSubject,
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
  addEmailConfig() {
    if (!this.enableEmail) {
      return;
    }
    var self = this;
    const dialogRef = self.dialog.open(EmailConfigurationDialog, {
      panelClass: "custom-dialog-container",
      height: "25% !important",
      width: "70%",
      disableClose: true,
      data: {
        screen: "dashboard",
        subject: this.title,
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
    this.range.reset();
    this.timeValue = "";
    if (event.value === "absolute") {
    }
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
            InsightsInitService.grafanaHost +
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
                  InsightsInitService.grafanaHost +
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
              });
          }
        }
      });
    } else {
      this.organisation = "";
      this.asyncResult = "";
      this.dashboard = "";
      this.templateVariableArr = [];
      this.messageDialog.showApplicationsMessage(
        "No Panels to Download!",
        "WARN"
      );
      return;
    }
  }
  onPreviewClick() {
    if (this.validatePreview() === true) {
      this.previewDashboard();
    }
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
      this.messageDialog.openSnackBar("Please fill mandatory fields.", "error");
      return;
    }
    if (!this.asyncResult.data.dashboard.timepicker.hidden) {
      if (
        !(
          (this.range.controls["start"].value !== null &&
            this.range.controls["end"].value !== null) ||
          this.timeValue !== ""
        )
      ) {
        valid = false;
        this.messageDialog.openSnackBar(
          "Please fill Data Time Range.",
          "error"
        );
        return;
      }
    }
    return valid;
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
          this.timeValue.from +
          "&to=" +
          this.timeValue.to;
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
      InsightsInitService.grafanaHost +
      dashboardUri +
      "?orgId=" +
      this.organisation +
      "&" +
      this.urlString +
      "&theme=" +
      this.theme;
    console.log(dashboardUri);
    console.log(this.dashUrl, this.saveUrl);

    this.getUrlArray();
    const dialogRef = self.dialog.open(DashboardPreviewConfigDialog, {
      panelClass: "custom-dialog-container",
      height: "70%",
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

  checkfrequency(val) {
    if (val === "ONETIME") {
    }
  }

  changePdftype(val) {
    if (val === "Dashboard") {
    }
  }

  onTimeChange(event) {
    this.timeValue = event.value;
  }
  save() {
    console.log(this.title);
    this.isDatainProgress = true;
    this.getUrlArray();
    let variables = "";
    let saveObj = {};
    let metaObj = [] as any;
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
    if (absStartDt === 0 && absEndDt === 0) {
      saveObj["from"] = this.timeValue.from;
      saveObj["to"] = this.timeValue.to;
      variables =
        variables + "from=" + this.timeValue.from + ",to=" + this.timeValue.to;
    } else {
      saveObj["from"] = rangeData.start;
      saveObj["to"] = rangeData.end;
      variables = variables + "from=" + absStartDt + ",to=" + absEndDt;
    }
    saveObj["loadTime"] = this.loadTime;
    saveObj["title"] = this.title;
    saveObj["source"] = "PLATFORM";
    saveObj["pdfType"] = [this.pdfType];
    saveObj["variables"] = variables;
    saveObj["dashUrl"] = this.saveUrl;
    saveObj["panelUrls"] = this.urlArray;
    saveObj["metadata"] = metaObj;
    saveObj["range"] = this.timeRange;
    saveObj["scheduleType"] = this.frequency;
    saveObj["organisation"] = this.organisation;
    saveObj["dashboard"] = this.dashboard;
    saveObj["userName"] = this.userName;
    saveObj["theme"] = this.theme;
    if (this.emailDetails != null) {
      saveObj["emailDetails"] = this.emailDetails;
    }
    if (this.timeRange === "relative" || this.timeRange === "other")
      saveObj["rangeText"] = this.timeValue.text;
    let requestObj = JSON.stringify(saveObj);
    var self = this;
    var dialogmessage = " Do you want to save the changes? ";
    var title = "Save Dashboard Configuration ";
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
          .saveDashboardAsPDF(saveObj)
          .then(function (response) {
            if (response.status === "success") {
              self.disableSave = true;
              self.messageDialog.openSnackBar("Saved Successfully", "SUCCESS");
              self.router.navigateByUrl("InSights/Home/dash-pdf-download", {
                skipLocationChange: true,
              });
            } else if (response.status === "failure") {
              self.messageDialog.openSnackBar(response.message, "ERROR");
            } else {
              self.messageDialog.openSnackBar(
                "Unable to Save Details",
                "ERROR"
              );
            }
          })
          .catch(function (error) {
            console.log(error);
            self.messageDialog.openSnackBar("Unable to Save Details", "ERROR");
          });
      }
    });
    this.isDatainProgress = false;
  }
  reset() {
    this.disableSave = true;
    this.organisation = "";
    this.pdfType = "";
    this.dashboard = "";
    this.frequency = "";
    this.emailAdd = "";
    this.mailSubject = "";
    this.mailBody = "";
    this.asyncResult = "";
    this.templateVariableArr = [];
    if (this.variableForm !== undefined) this.variableForm.reset();
    if (this.range !== undefined) this.range.reset();
    this.timeRange = "";
    this.timeValue = "";
    this.emailDetails = "";
    this.mailIconDisable = true;
    this.showTimePicker = true;
  }
  resetOnDashboardChange() {
    this.timeValue = "";
    this.timeRange = "";
    this.disableSave = true;
    this.frequency = "";
    this.emailAdd = "";
    this.mailSubject = "";
    this.mailBody = "";
    this.templateVariableArr = [];
    if (this.variableForm !== undefined) this.variableForm.reset();
    if (this.range !== undefined) this.range.reset();
    this.mailIconDisable = true;
    this.showTimePicker = true;
  }
}
