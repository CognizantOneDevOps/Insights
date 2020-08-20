/*********************************************************************************
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
 *******************************************************************************/
import { Component, OnInit, Inject, ViewEncapsulation } from '@angular/core';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { HealthCheckService } from '@insights/app/modules/healthcheck/healthcheck.service';
import { MatTableDataSource, MatSort, MatPaginator, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ShowDetailsDialog } from '@insights/app/modules/healthcheck/healthcheck-show-details-dialog';
import { CommonModule, DatePipe } from '@angular/common';
import { DataSharedService } from '@insights/common/data-shared-service';
import { QueryBuilderService } from '../blockchain/custom-report/custom-report-service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { saveAs as importedSaveAs } from "file-saver";


@Component({
  selector: 'app-healthcheck',
  templateUrl: './healthcheck.component.html',
  styleUrls: ['./healthcheck.component.css', './../home.module.css']
})
export class HealthCheckComponent implements OnInit {

  agentsStatusResposne: any;
  agentNodes = [];
  agentToolsIcon = {};
  showContent: boolean = false;
  showThrobber: boolean = false;
  showContentAgent: boolean = false;
  showThrobberAgent: boolean = false;
  serverStatus = [];
  displayedAgentColumns: string[];
  dataComponentColumns: string[];
  servicesColumns: string[];
  agentDataSource = [];
  agentListDatasource = [];
  dataComponentDataSource = [];
  dataListDatasource = [];
  servicesDataSource = [];
  servicesListDatasource = [];
  timeZone: string = "";
  healthResponse: any;
  agentResponse: any;
  agentNameList: any = [];
  selectAgentTool: any;
  showMessage: string;
  reportLogsColumns: string[];
  reportLogsDataSource = new MatTableDataSource<any>();
  showReportLog = false;
  constructor(private healthCheckService: HealthCheckService, private dialog: MatDialog,
    public dataShare: DataSharedService, private queryBuilderService: QueryBuilderService,
    private messageDialog: MessageDialogService, private config: InsightsInitService, ) {
    this.loadAgentCheckInfo();
    this.loadOtherHealthCheckInfo();
    if (InsightsInitService.showAuditReporting) {
      this.loadReportsLogs();
    }
    this.showReportLog = InsightsInitService.showAuditReporting;
  }

  ngOnInit() {

    this.timeZone = this.dataShare.getTimeZone()
  }

  async loadAgentCheckInfo() {
    try {
      this.showThrobberAgent = true;
      this.showContentAgent = !this.showThrobberAgent;
      this.agentResponse = await this.healthCheckService.loadServerAgentConfiguration();
      if (this.agentResponse != null) {
        this.showThrobberAgent = false;
        this.showContentAgent = !this.showThrobberAgent;
        for (var key in this.agentResponse.data) {
          var element = this.agentResponse.data[key];
          element.serverName = key;
          if (element.type == 'Agents') {
            this.agentNodes = element.agentNodes;
            this.agentDataSource = this.agentNodes;
          }
        }
        this.agentNameList.push("All");
        for (var data of this.agentDataSource) {
          if (this.agentNameList.indexOf(data.toolName) == -1) {
            this.agentNameList.push(data.toolName);

          }
        }
        this.selectToolAgent("All");
        this.displayedAgentColumns = ['toolName', 'agentKey', 'category', 'inSightsTimeX', 'status', 'details'];
      }
    } catch (error) {
      this.showContentAgent = false;
      console.log(error);
    }

  }

  selectToolAgent(ToolSelect) {
    var agentListDatasourceSelected = [];
    if (ToolSelect != "All") {
      this.agentDataSource.filter(x => {
        if (x.toolName == ToolSelect) {
          agentListDatasourceSelected.push(x)
        }
      }
      )
    } else {
      agentListDatasourceSelected = this.agentDataSource;
    }
    this.agentListDatasource = agentListDatasourceSelected;
  }

  async loadOtherHealthCheckInfo() {
    try {
      // Loads Data Component and Services
      this.showThrobber = true;
      this.showContent = !this.showThrobber;
      this.healthResponse = await this.healthCheckService.loadServerHealthConfiguration();
      if (this.healthResponse != null) {
        //console.log(this.healthResponse);
        this.showThrobber = false;
        this.showContent = !this.showThrobber;
        for (var key in this.healthResponse.data) {
          var element = this.healthResponse.data[key];
          element.serverName = key;
          if (element.type == 'Service') {
            if (element.serverName.indexOf('Webhook') >= 0) {
              if (InsightsInitService.showWebhookConfiguration) {
                this.servicesDataSource.push(element);
              }
            } else {
              this.servicesDataSource.push(element);
            }
          } else if (element.type == 'Database') {
            this.dataComponentDataSource.push(element);
          }
        }
        this.dataComponentColumns = ['serverName', 'ipAddress', 'version', 'info', 'status'];
        this.servicesColumns = ['serverName', 'ipAddress', 'version', 'status', 'details'];
      }
    } catch (error) {
      this.showContent = false;
      console.log(error);
    }
  }
  // Displays Show Details dialog box when Details column is clicked
  showDetailsDialog(toolName: string, categoryName: string, agentId: string) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var rcategoryName = categoryName.replace(/ +/g, "");
      if (toolName == "-") {
        if(categoryName == 'Platform DataArchivalEngine'){
          var filePath = "${INSIGHTS_HOME}/logs/PlatformEngine/platformEngine.log";
          var detailType = categoryName;
        }else{
          var filePath = "${INSIGHTS_HOME}/logs/" + rcategoryName + "/" + rcategoryName + ".log";
          var detailType = categoryName;
        }
      }
      else {
        var rtoolName = toolName.charAt(0).toUpperCase() + toolName.slice(1).toLowerCase();
        var filePath = "${INSIGHTS_HOME}/logs/PlatformAgent/log_" + agentId + "Agent.log";
        var detailType = rtoolName;
      }
      let showDetailsDialog = this.dialog.open(ShowDetailsDialog, {
        panelClass: 'healthcheck-show-details-dialog-container',
        height: '500px',
        width: '900px',
        disableClose: true,
        data: { toolName: toolName, categoryName: categoryName, pathName: filePath, detailType: detailType, agentId: agentId, timeZone: this.timeZone },
      });
    }
    else {
      //console.log("Heathcheck")
    }
  }

  //Transfers focus of Heath Check page as per User's selection
  goToSection(source: string, target: string) {
    // Changes the selected section color in the title
    this.changeSelectedSectionColor(source);
    let element = document.querySelector("#" + target);
    if (element) {
      element.scrollIntoView();
    }
  }

  // Changes the selected section color in the title
  changeSelectedSectionColor(source: string) {
    if (source == 'agentTxt') {
      document.getElementById(source).style.color = "#00B140";
      document.getElementById('dataCompTxt').style.color = "#0033A0";
      document.getElementById('servicesTxt').style.color = "#0033A0";
    } else if (source == 'dataCompTxt') {
      document.getElementById(source).style.color = "#00B140";
      document.getElementById('agentTxt').style.color = "#0033A0";
      document.getElementById('servicesTxt').style.color = "#0033A0";
    } else if (source == 'servicesTxt') {
      document.getElementById(source).style.color = "#00B140";
      document.getElementById('dataCompTxt').style.color = "#0033A0";
      document.getElementById('agentTxt').style.color = "#0033A0";
    }

  }

  //When user clicks on Back to Top button, it scrolls to Health Check page
  goToHealthCheckTitle() {
    let element = document.querySelector("#healthCheckTitle");
    if (element) {
      element.scrollIntoView();
    }
  }

  async loadReportsLogs() {
    this.reportLogsDataSource = new MatTableDataSource();
    let custReportList = await this.queryBuilderService.fetchQueries();
    //console.log("custReportList---", custReportList);

    if (custReportList != null && custReportList.data.length > 0) {
      let logList = [];

      this.showThrobber = false;
      this.reportLogsColumns = ['reportName', 'logFile'];
      this.reportLogsDataSource.data = custReportList.data;
      //this.reportLogsDataSource.sort = this.sort;
      //this.reportLogsDataSource.paginator = this.paginator;
    } else {
      //this.showMessage = "Something wrong with Service, Please try again.";
      //this.messageDialog.showApplicationsMessage("No Report available !", "ERROR");
    }
  }

  downloadLog(logfile) {
    console.log("download starts for ", logfile);
    this.healthCheckService.downloadLog(logfile).subscribe((data) => {
      //console.log(data);
      //importedSaveAs(data, logfile);
      console.log(data);
      if (data.size > 0) {
        importedSaveAs(data, logfile);
      } else {
        alert("Please run the corresponding report once!");
      }
    }, error => {
      console.log(error);
    });
  }

}





