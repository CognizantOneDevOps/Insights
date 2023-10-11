/*
 *******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
import { FormBuilder, FormControl, FormGroup } from "@angular/forms";
import { OfflineService } from "@insights/app/modules/offline-data-processing/offline-service";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { Component, OnInit } from "@angular/core";
import { Router, NavigationExtras, ActivatedRoute } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { OfflineAlertingService } from "@insights/app/modules/offline-alerting/offline-alerting-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { EmailConfigurationDialog } from "@insights/app/modules/reportmanagement/report-configuration/email-configuration-dialog";
import { TimeRange } from "@insights/app/modules/offline-alerting/timeRangeJson";

@Component({
  selector: "app-offline-alert-configuration",
  templateUrl: "./offline-alert-configuration.component.html",
  styleUrls: [
    "./offline-alert-configuration.component.scss",
    "./../../home.module.scss",
  ],
  providers: [TimeRange],
})
export class OfflineAlertConfigurationComponent implements OnInit {
  organisation: any;
  organisationId: number;
  dashboard: any;
  dashUUID: any;
  panelQuery: any;
  rawQuery: any;
  panelName: string;
  panel: any;
  cypherQuery: string;
  type: string;
  onEdit: boolean = false;
  inputDataJson: any;
  orgArr: [];
  dashboardList: any;
  panelList: any;
  dashOptions = [];
  repsonseFromGrafana: any;
  totalMap = new Map<String, String[]>();
  editTotalMap = new Map<String, String[]>();
  templateVariableArr: any = [];
  editTemplateVariableArr: any = [];
  finalTemplateVariableArr: any = [];
  asyncResult: any;
  queryArr: any = [];
  urlArray: any = [];
  panelUrlArray = [];
  urlString: string = "";
  theme: string;
  frequency: string;
  globalMap = new Map<String, String[]>();
  queryVaribles: any[];
  expectedTrend: any;
  scheduleType: any;
  trend = ["ABOVE", "BELOW"];
  scheduleList = [
    "HOURLY",
    "EVERY_6_HOUR",
    "EVERY_12_HOUR",
    "DAILY",
    "WEEKLY",
    "MONTHLY",
    "QUARTERLY",
  ];
  threshold: string;
  emailDetails: any;
  enableEmail: boolean;
  title: string;
  mailSubject: any;
  variableForm: FormGroup;
  isDatainProgress: boolean = false;
  asyncOptions: any;
  mailIconDisable: boolean = true;
  showTimePicker: boolean = true;
  alertName: string;
  panelFilterFlag: boolean = false;
  dbQuery: any;
  dispalyQueryPanel: boolean = false;
  selectedValues: { [key: string]: any[] } = {};
  selectedRadio: { [key: string]: string } = {};
  showConfirmMessage: string;
  filterMap = new Map<any, String[]>();
  editData: any;
  dashName: any;
  editTimeValue: any = [
    {
      text: "",
      from: "",
    },
  ];
  timeValue: any = [
    {
      text: "",
      from: "",
    },
  ];
  regexDigits = new RegExp("^\\d+$");
  regexAlpha = new RegExp("^[a-zA-Z0-9_]*$");
  relativeRange = [];
  showTimeRangeFlag: boolean = false;
  relativeValue: any;
  scheduleDateTime: any;
  dateObj: Date;

  constructor(
    public router: Router,
    public messageDialog: MessageDialogService,
    public offlineService: OfflineService,
    public offlineAlertingService: OfflineAlertingService,
    public route: ActivatedRoute,
    public dataShare: DataSharedService,
    private dialog: MatDialog,
    private formBuilder: FormBuilder,
    public relativeTime: TimeRange
  ) {}

  ngOnInit() {
    this.getOrgs();
    this.type = this.offlineAlertingService.getType();
    this.relativeRange = this.relativeTime.relativeJson;
    if (this.type === "EDIT") {
      this.onEdit = true;

      this.route.queryParams.subscribe((params) => {
        if (params) {
          this.initialiseVariables(params);
          this.getDashboardJson(params.dashUUID);
        }
      });
    }
    this.offlineAlertingService
      .getEmailConfigurationStatus()
      .then((response) => {
        this.enableEmail = response.data;
      });
  }

  public async getOrgs() {
    let currentUserWithOrgs = this.dataShare.getUserOrgArray();
    if (currentUserWithOrgs !== undefined) {
      this.orgArr = currentUserWithOrgs.filter((org) => org.role === "Admin");
    }
  }

  initialiseVariables(params) {
    this.getDashboardsByOrg(Number(params.orgId));
    this.inputDataJson = params;
    this.editData = params;
    this.organisation = params.orgId;
    this.getPanelByPanelName(params.panelName, params.dashUUID);
    this.alertName = params.alertName;
    this.threshold = params.threshold;
    this.frequency = params.frequency;
    this.expectedTrend = params.trend;
    this.scheduleType = params.schedule;
    this.dateObj = new Date(params.scheduleDateTime * 1000);
    this.scheduleDateTime = this.dataShare.convertDateToSpecificDateFormat(
      this.dateObj,
      "yyyy-MM-ddTHH:mm"
    );

    if (params.timeRangeText != undefined) {
      this.relativeValue = params.timeRangeText;
      this.showTimeRangeFeild(params.rawQuery);
      let timeRangeObj = {
        text: params.timeRangeText,
        from: params.from,
      };
      this.editTimeValue = [];
      this.editTimeValue.push(timeRangeObj);
    }
    if (params.emailDetails != null) {
      this.emailDetails = JSON.parse(params.emailDetails);
    }

    this.filterMap = new Map<any, String[]>();
    let varArr = [];
    varArr = this.editData.filters.split(",");
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

  async getDashboardsByOrg(orgId) {
    this.dashboardList = [];
    this.dashOptions = [];
    this.panelUrlArray = [];
    this.organisationId = orgId;
    this.dispalyQueryPanel = false;
    this.panelFilterFlag = false;

    this.repsonseFromGrafana =
      await this.offlineAlertingService.getDashboardList(orgId);

    if (this.repsonseFromGrafana.status == "success") {
      this.dashboardList = this.repsonseFromGrafana.data;
      this.dashboardList.forEach((res) => {
        this.dashOptions.push(res);
      });
      this.dashOptions.sort((d1, d2) => (d1.title > d2.title ? 1 : -1));
    }

    if (this.onEdit === true && this.editData !== undefined) {
      this.dashboard = this.dashOptions.filter(
        (dash) => dash.uid === this.editData.dashUUID
      )[0].uid;
      this.dashOptions.sort((d1, d2) => (d1.title > d2.title ? 1 : -1));
    }
  }

  getCurrentDateTime() {
    return new Date().toISOString().slice(0, -8);
  }

  onTimeChange(event) {
    if (!this.onEdit) {
      this.timeValue = [];
      this.timeValue = this.relativeRange.filter(
        (variable) => event.value === variable.text
      );
    } else {
      this.editTimeValue = [];
      this.editTimeValue = this.relativeRange.filter(
        (variable) => event.value === variable.text
      );
    }
  }

  public async getDashboardJson(dashboardUUID) {
    this.dashUUID = dashboardUUID;
    this.panel = "";
    this.dashName = "";
    this.panelFilterFlag = false;
    this.dispalyQueryPanel = false;
    this.totalMap.clear();
    this.templateVariableArr = [];
    this.asyncResult = "";
    this.queryArr = [];
    this.asyncResult = await this.offlineAlertingService.getDashboardByUid(
      dashboardUUID,
      this.organisation
    );

    if (this.asyncResult.status === "success" && this.onEdit != true) {
      if (this.enableEmail) {
        this.mailIconDisable = false;
      }
      this.dashName = this.asyncResult.data.dashboard.title;

      if ("message" in this.asyncResult.data) {
        this.messageDialog.showApplicationsMessage(
          " Dashbaord Json have some issue  : " + this.asyncResult.data.message,
          "ERROR"
        );
      } else {
        this.showTimePicker = this.asyncResult.data.dashboard.timepicker.hidden;
        this.templateVariableArr = [];
        this.templateVariableArr =
          this.asyncResult.data.dashboard.templating.list;
        this.finalTemplateVariableArr = this.templateVariableArr;
      }
    } else if (this.asyncResult.status === "success" && this.onEdit === true) {
      this.editTemplateVariableArr = [];
      this.showTimePicker = this.asyncResult.data.dashboard.timepicker.hidden;
      this.editTemplateVariableArr =
        this.asyncResult.data.dashboard.templating.list;
      this.editTemplateVariableArr.forEach((ele, i) => {
        if (!ele.multi) {
          this.editTemplateVariableArr[i]["selectedValue"] = "";
        }
      });
      let selectJson = {};
      this.mailSubject = this.asyncResult.data.meta.slug;
      this.editTemplateVariableArr.forEach((element) => {
        if (element.hide !== 2) {
          if (element.type === "textbox") {
            selectJson[element.name] = [element.options[0].value];
          } else {
            selectJson[element.name] = [""];
          }
        }
      });
      this.variableForm = this.formBuilder.group(selectJson);
      if (this.editTemplateVariableArr.length > 0) {
        this.editTemplateVariableArr.forEach((list, index) => {
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
            this.editTotalMap.set(
              "$" + this.editTemplateVariableArr[index].name,
              customArray
            );
          }
        });
        this.editPanelFilter(this.editTemplateVariableArr, this.filterMap);
        this.getOptionsByQuery();
      }

      for (let [key, value] of this.filterMap.entries()) {
        if (key !== "from" && key !== "to") {
          if (value !== undefined)
            this.variableForm.controls[key].patchValue(value);
        }
      }

      this.editTemplateVariableArr.forEach((element, index) => {
        if (!element.multi && element.hide != 2) {
          this.editTemplateVariableArr[index]["selectedValue"] =
            this.variableForm.get(element.name).value;
        }
      });

      let editPanelFlag = false;
      if (this.filterMap.size != 0) {
        for (let [key] of this.filterMap.entries()) {
          let search = "$" + key;
          if (this.editData.rawQuery.includes(search)) {
            editPanelFlag = true;
          }
        }
      } else {
        editPanelFlag = true;
      }
      if (editPanelFlag) {
        this.dbQuery = this.editData.cypherQuery;
        this.dispalyQueryPanel = true;
      }
    }
    this.getUrlArray();
    return this.asyncResult;
  }

  public async getOptionsByQuery() {
    if (this.onEdit != true) {
      for (let query of this.queryArr) {
        if (!query.query.includes("$")) {
          let optionData = [];
          let asyncOptions: any;
          asyncOptions = await this.offlineAlertingService.getTemplateByQuery({
            query: query.query,
          });
          asyncOptions.results[0].data.forEach((element) => {
            optionData.push(element.row[0]);
          });
          this.templateVariableArr[query.index].options = optionData;
          this.totalMap.set(
            "$" + this.templateVariableArr[query.index].name,
            optionData
          );
        } else {
          let optionData = [];
          let asyncOptions: any;

          asyncOptions = await this.offlineAlertingService.getTemplateByQuery({
            query: query.query,
          });
          asyncOptions.results[0].data.forEach((element) => {
            optionData.push(element.row[0]);
          });
          this.templateVariableArr[query.index].options = optionData;
        }
      }
    } else {
      for (let query of this.queryArr) {
        if (!query.query.includes("$")) {
          let optionData = [];
          let asyncOptions: any;
          asyncOptions = await this.offlineAlertingService.getTemplateByQuery({
            query: query.query,
          });
          asyncOptions.results[0].data.forEach((element) => {
            optionData.push(element.row[0]);
          });
          this.editTemplateVariableArr[query.index].options = optionData;
          this.editTotalMap.set(
            "$" + this.editTemplateVariableArr[query.index].name,
            optionData
          );
        } else {
          let optionData = [];
          let asyncOptions: any;

          asyncOptions = await this.offlineAlertingService.getTemplateByQuery({
            query: query.query,
          });
          asyncOptions.results[0].data.forEach((element) => {
            optionData.push(element.row[0]);
          });
          this.editTemplateVariableArr[query.index].options = optionData;
        }
      }
    }
  }

  getUrlArray() {
    this.urlArray = [];
    this.panelUrlArray = [];
    let variables;
    variables = this.urlString;
    let dashboard = this.asyncResult.data.dashboard;
    let cypherQuery: string = "";
    var i: number = 0;
    var j: number = 0;
    let flag = false;
    if (dashboard.panels.length > 0) {
      dashboard.panels.forEach((x) => {
        if (x.type === "stat") {
          flag = true;
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
          if (x.targets[0].cypherQuery == undefined) {
            cypherQuery = x.targets[0].queryText;
          } else {
            cypherQuery = x.targets[0].cypherQuery;
          }

          this.panelArray(
            this.urlArray[i],
            x.type,
            cypherQuery,
            x.title,
            x.datasource.type
          );
          i = i + 1;
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

    if (!flag) {
      this.messageDialog.openSnackBar("No stat panel found!", "error");
    }

    this.queryVaribles = dashboard.templating.list;
  }

  async showTimeRangeFeild(rawQuery) {
    if (
      rawQuery.toUpperCase().includes("?START_TIME?") ||
      rawQuery.toUpperCase().includes("?END_TIME?")
    ) {
      this.showTimeRangeFlag = true;
    } else {
      this.showTimeRangeFlag = false;
    }
  }

  async getPanelByPanelName(panelName, dashUUID) {
    let asyncResult = await this.offlineAlertingService.getDashboardByUid(
      dashUUID,
      this.organisation
    );

    let dashboard = asyncResult.data.dashboard;
    if (dashboard.panels.length > 0) {
      dashboard.panels.forEach((x) => {
        if (x.title === panelName) {
          this.panel = x.title;
        }
      });
    }
  }

  panelArray(url, type, query, title, datasourceType) {
    let panelInfoJson = {};
    if (datasourceType.search(/neo/i) !== -1) {
      panelInfoJson["panelURL"] = url;
      panelInfoJson["type"] = type;
      panelInfoJson["query"] = query;
      panelInfoJson["title"] = title;
      this.panelUrlArray.push(panelInfoJson);
    }
    if (this.panelUrlArray.length == 0) {
      this.messageDialog.openSnackBar(
        "No stat panel with neo4j datasource found!",
        "error"
      );
    }
    this.panelUrlArray.sort((p1, p2) => (p1.title > p2.title ? 1 : -1));
  }

  getPanelJson(panel) {
    this.dispalyQueryPanel = false;
    let panelFlag = false;
    this.panelUrlArray.forEach((element) => {
      if (element.title === panel) {
        this.panelName = element.title;
        this.rawQuery = element.query != undefined ? element.query : "";
        this.panelQuery = this.rawQuery;
      }
    });
    if (
      this.rawQuery.toUpperCase().includes("?START_TIME?") ||
      this.rawQuery.toUpperCase().includes("?END_TIME?")
    ) {
      this.showTimeRangeFlag = true;
    } else {
      this.showTimeRangeFlag = false;
    }

    if (this.queryVaribles.length != 0) {
      this.queryVaribles.forEach((element) => {
        let search = "$" + element.name;
        if (!this.panelQuery.includes(search)) {
          panelFlag = true;
        }
      });
    } else {
      panelFlag = true;
    }

    if (panelFlag) {
      this.dbQuery = this.panelQuery;
      this.dispalyQueryPanel = true;
    }

    this.filterTemplateVariables();

    if (this.onEdit != true) {
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

  filterTemplateVariables() {
    this.panelFilterFlag = true;
    const regex = /\$([a-zA-Z]+)/g;
    let match;
    const filterNames = [];

    while ((match = regex.exec(this.panelQuery)) !== null) {
      filterNames.push(match[1]);
    }

    this.templateVariableArr = this.finalTemplateVariableArr.filter(
      (variable) => filterNames.includes(variable.name)
    );
  }

  editPanelFilter(editTemplateVariableArr, filterMap) {
    this.panelFilterFlag = true;
    editTemplateVariableArr.forEach((element) => {
      if (filterMap.has(element.name)) {
        this.templateVariableArr.push(element);
      }
    });
  }

  public modifiedQuery() {
    let query: any;
    let qflag: boolean = false;
    if (this.onEdit != true) {
      query = this.panelQuery;
      this.queryVaribles.forEach((element) => {
        let search = "$" + element.name;
        if (query.includes(search)) {
          let str = "";
          if (typeof element.selectedValue === "string") {
            str = "'" + element.selectedValue + "'";
          } else if (typeof element.selectedValue != "string") {
            if (element.selectedValue === undefined) {
              let avoidDuplicateFlag = true;
              for (let [key, value] of this.globalMap.entries()) {
                if (search === key) {
                  var selectedValueArr = value;
                }
              }
              selectedValueArr.forEach((e) => {
                if (e.includes("$__all") || e.includes("All")) {
                  avoidDuplicateFlag = false;

                  for (let [key, value] of this.totalMap.entries()) {
                    if (search === key) {
                      let values = value;
                      values.forEach((z) => {
                        str += "'" + z + "',";
                      });
                    }
                  }
                } else if (avoidDuplicateFlag) {
                  str += "'" + e + "',";
                }
              });
            } else {
              let avoidDuplicateFlag = true;
              element.selectedValue.forEach((x) => {
                if (x.includes("$__all") || x.includes("All")) {
                  avoidDuplicateFlag = false;

                  for (let [key, value] of this.totalMap.entries()) {
                    if (search === key) {
                      let values = value;
                      values.forEach((z) => {
                        str += "'" + z + "',";
                      });
                    }
                  }
                } else if (avoidDuplicateFlag) {
                  str += "'" + x + "',";
                }
              });
            }
            str = str.slice(0, -1);
          }
          query = query.replaceAll(search, "[ " + str + " ]");
        }
      });
    } else {
      query = this.editData.rawQuery;
      this.queryVaribles.forEach((element) => {
        let search = "$" + element.name;
        if (query.includes(search)) {
          let str = "";
          if (typeof element.selectedValue === "string") {
            str = "'" + element.selectedValue + "'";
          }
          if (typeof element.selectedValue != "string") {
            if (element.selectedValue === undefined) {
              let avoidDuplicateFlag = true;
              if (this.globalMap.size > 0) {
                for (let [key, value] of this.globalMap.entries()) {
                  if (search === key) {
                    var selectedValueArr = value;
                  }
                }
                selectedValueArr.forEach((e) => {
                  if (e.includes("$__all") || e.includes("All")) {
                    avoidDuplicateFlag = false;

                    for (let [key, value] of this.editTotalMap.entries()) {
                      if (search === key) {
                        let values = value;
                        values.forEach((z) => {
                          str += "'" + z + "',";
                        });
                      }
                    }
                  } else if (avoidDuplicateFlag) {
                    str += "'" + e + "',";
                  }
                });
              } else {
                qflag = true;
              }
            } else {
              let avoidDuplicateFlag = true;
              element.selectedValue.forEach((x) => {
                if (x.includes("$__all") || x.includes("All")) {
                  avoidDuplicateFlag = false;

                  for (let [key, value] of this.editTotalMap.entries()) {
                    if (search === key) {
                      let values = value;
                      values.forEach((z) => {
                        str += "'" + z + "',";
                      });
                    }
                  }
                } else if (avoidDuplicateFlag) {
                  str += "'" + x + "',";
                }
              });
            }
            str = str.slice(0, -1);
          }
          if (qflag) {
            query = this.editData.cypherQuery;
          } else {
            query = query.replaceAll(search, "[ " + str + " ]");
          }
        }
      });
    }

    return query.trim();
  }

  saveAlert() {
    this.isDatainProgress = true;
    let saveObj = {};
    let variables = "";
    for (let [key, value] of Object.entries(this.variableForm.value)) {
      if (Array.isArray(value)) {
        value.forEach((val) => {
          variables = variables + key + "=" + val + ",";
        });
      } else {
        variables = variables + key + "=" + value + ",";
      }
    }

    saveObj["orgId"] = this.organisationId;
    saveObj["dashUUID"] = this.dashUUID;
    saveObj["dashName"] = this.dashName;
    saveObj["panelName"] = this.panelName;
    saveObj["alertName"] = this.alertName;
    saveObj["rawQuery"] = this.rawQuery.trim();
    saveObj["cypherQuery"] = this.modifiedQuery();
    saveObj["trend"] = this.expectedTrend;
    saveObj["scheduleType"] = this.scheduleType;
    saveObj["scheduleDateTime"] =
      new Date(this.scheduleDateTime).getTime() / 1000;
    saveObj["threshold"] = this.threshold;
    saveObj["frequency"] = this.frequency;
    saveObj["timeRangeText"] = this.timeValue[0].text;
    saveObj["from"] = this.timeValue[0].from;
    saveObj["filters"] = variables;

    if (this.emailDetails != null) {
      saveObj["emailDetails"] = this.emailDetails;
    }

    var self = this;
    var dialogmessage = " Do you want to save the changes? ";
    var title = "Save Alert Configuration ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.offlineAlertingService
          .saveAlertConfig(saveObj)
          .then(function (response) {
            if (response.status === "success") {
              self.messageDialog.openSnackBar("Saved Successfully", "SUCCESS");
              self.router.navigateByUrl("InSights/Home/offlineAlertingList", {
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
            self.messageDialog.openSnackBar("Unable to Save Details", "ERROR");
          });
      }
    });
    this.isDatainProgress = false;
  }

  updateAlert() {
    this.isDatainProgress = true;
    let saveObj = {};
    let variables = "";
    for (let [key, value] of Object.entries(this.variableForm.value)) {
      if (key && value) {
        if (Array.isArray(value)) {
          value.forEach((val) => {
            variables = variables + key + "=" + val + ",";
          });
        } else {
          variables = variables + key + "=" + value + ",";
        }
      }
    }

    saveObj["alertName"] = this.alertName;
    saveObj["cypherQuery"] = this.modifiedQuery();
    saveObj["trend"] = this.expectedTrend;
    saveObj["scheduleType"] = this.scheduleType;
    saveObj["threshold"] = this.threshold;
    saveObj["frequency"] = this.frequency;
    saveObj["timeRangeText"] = this.editTimeValue[0].text;
    saveObj["from"] = this.editTimeValue[0].from;
    saveObj["filters"] = variables;

    if (this.emailDetails != null) {
      saveObj["emailDetails"] = this.emailDetails;
    }

    var self = this;
    var dialogmessage = " Do you want to save the changes? ";
    var title = "Save Alert Configuration ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.offlineAlertingService
          .updateAlertConfig(saveObj)
          .then(function (response) {
            if (response.status === "success") {
              self.messageDialog.openSnackBar(
                "Updated Successfully",
                "SUCCESS"
              );
              self.router.navigateByUrl("InSights/Home/offlineAlertingList", {
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
            self.messageDialog.openSnackBar("Unable to Save Details", "ERROR");
          });
      }
    });
    this.isDatainProgress = false;
  }

  public async onChangeVariables(event, data) {
    this.globalMap.clear;
    if (data.multi) {
      let arr = [];
      arr = this.variableForm.controls[data.name].value;
      this.variableForm.controls[data.name].setValue(arr);
    }

    for (let [key, value] of Object.entries(this.variableForm.value)) {
      if (Array.isArray(value)) {
        if (this.onEdit != true) {
          if (this.totalMap.has("$" + key))
            this.globalMap.set(
              "$" + key,
              this.variableForm.controls[key].value
            );
        } else {
          if (this.editTotalMap.has("$" + key))
            this.globalMap.set(
              "$" + key,
              this.variableForm.controls[key].value
            );
        }
      }
    }

    this.templateVariableArr.every(
      (filter) => filter.selectedValues && filter.selectedValues.length > 0
    );

    this.selectedValues[data.name] = event.value;

    const allFiltersSelected = this.templateVariableArr.every(
      (filter) =>
        this.selectedValues[filter.name] &&
        this.selectedValues[filter.name].length > 0
    );

    if (allFiltersSelected && this.onEdit != true) {
      this.dispalyQueryPanel = true;
      this.dbQuery = this.modifiedQuery();
    } else if (this.onEdit === true) {
      this.dispalyQueryPanel = true;
      this.dbQuery = this.modifiedQuery();
    } else {
      this.dispalyQueryPanel = false;
    }
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

  onPreviewClick() {
    if (this.type === "ADD") {
      if (this.validatePreview() === true) {
        this.saveAlert();
      }
    } else {
      if (this.validateEditPreview() === true) {
        this.updateAlert();
      }
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

    if (this.emailDetails == null || this.emailDetails == undefined) {
      valid = false;
      this.messageDialog.openSnackBar(
        "Please configure mailing details.",
        "error"
      );
      return;
    }
    if (
      this.organisation == undefined ||
      this.dashboard == undefined ||
      this.panelName === undefined ||
      this.alertName === undefined ||
      this.frequency == undefined ||
      this.threshold == undefined ||
      this.trend == undefined ||
      this.scheduleType == undefined ||
      this.scheduleDateTime == undefined ||
      this.organisation === "" ||
      this.dashboard === "" ||
      this.panelName === "" ||
      this.alertName === "" ||
      this.frequency === "" ||
      this.threshold === "" ||
      this.scheduleType == "" ||
      this.trend === null
    ) {
      valid = false;
      this.messageDialog.openSnackBar("Please fill mandatory fields.", "error");
      return;
    }

    var checkAlert = this.regexAlpha.test(this.alertName);
    var checkFreq = new RegExp("^[1-9][0-9]*$").test(this.frequency);
    var checkThresh = this.regexDigits.test(this.threshold);

    if (!checkAlert) {
      valid = false;
      this.messageDialog.openSnackBar(
        "Alert name should be alpha numeric & can contain '_'",
        "error"
      );
    }

    if (!checkFreq) {
      valid = false;
      this.messageDialog.openSnackBar(
        "Frequency should be greater than 0.",
        "error"
      );
    }

    if (!checkThresh) {
      valid = false;
      this.messageDialog.openSnackBar(
        "Threshold should be a +ve digits.",
        "error"
      );
    }

    return valid;
  }

  validateEditPreview() {
    let valid = true;
    var checkFreq = new RegExp("^[1-9][0-9]*$").test(this.frequency);
    var checkThresh = this.regexDigits.test(this.threshold);

    if (!checkFreq) {
      valid = false;
      this.messageDialog.openSnackBar(
        "Frequency should be greater than 0.",
        "error"
      );
    }

    if (!checkThresh) {
      valid = false;
      this.messageDialog.openSnackBar(
        "Threshold should be a +ve digits.",
        "error"
      );
    }

    return valid;
  }

  redirectToLandingPage() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
    };
    this.router.navigate(
      ["InSights/Home/offlineAlertingList"],
      navigationExtras
    );
  }

  refreshData() {
    this.organisation = "";
    this.dashboard = "";
    this.dashName = "";
    this.alertName = "";
    this.frequency = "";
    this.scheduleType = "";
    this.scheduleDateTime = "";
    this.templateVariableArr = [];
    this.queryVaribles = [];
    this.panelUrlArray = [];
    this.dispalyQueryPanel = false;
    this.panelFilterFlag = false;
    this.threshold = "";
    this.expectedTrend = "";
    this.emailDetails = "";
  }

  reset() {
    this.frequency = this.editData.frequency;
    this.scheduleType = this.editData.schedule;
    this.relativeValue = this.editData.timeRangeText;
    this.threshold = this.editData.threshold;
    this.expectedTrend = this.editData.trend;
    this.mailIconDisable = true;
    this.showTimePicker = true;
  }
}
