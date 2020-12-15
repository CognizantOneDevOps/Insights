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
import { Component, OnInit, PipeTransform, Pipe, ViewChild, ElementRef } from '@angular/core';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { ServerConfigurationService } from '@insights/app/modules/server-configuration/server-configuration-service';
import { Router, ActivatedRoute, ParamMap, NavigationExtras } from '@angular/router';
import { ServerConfigItem } from '@insights/app/modules/server-configuration/serverConfigItem';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { DataSharedService } from '@insights/common/data-shared-service';
import { GrafanaAuthenticationService } from '../../common.services/grafana-authentication-service';

@Component({
  selector: 'app-server-configuration',
  templateUrl: './server-configuration.component.html',
  styleUrls: ['./server-configuration.component.css', './../home.module.css']
})
export class ServerConfigurationComponent implements OnInit {
  datatypeVal: boolean;
  validationArr = {};
  serverConfig = {};
  configAbbr = [];
  toolsArr = [];
  serverConfigData: any;
  showMessage: string;
  showConfig: boolean = false;
  showThrobber: boolean = false;
  headerData = [];
  updatedConfigdata = {};
  updatedConfigParamdata = {};
  configData: string;
  item = {};
  defaultConfigdata = {};
  tempConfigdata: string;
  receivedParam: any;
  ServerConfigItems: ServerConfigItem[] = [];
  isConfigAvailable: boolean = false;
  authenticationProtocolList = ["SAML", "Kerberos", "NativeGrafana", "JWT"];
  requiredProperties = ["isVaultEnable", "vaultEndPoint", "secretEngine", "vaultToken", "endpoint", "authToken", "boltEndPoint", "grafanaEndpoint",
    "grafanaDBEndpoint", "userName", "password", "insightsDBUrl", "grafanaDBUrl", "host", "user", "password", "insightsServiceURL", "trustedHosts",
    "autheticationProtocol", "adminUserPassword", "adminUserName"]
  secretProperties = ["authToken", "password", "adminUserPassword", "smtpPassword"];
  constructor(public config: InsightsInitService, public serverconfigService: ServerConfigurationService,
    private router: Router, private route: ActivatedRoute,
    public messageDialog: MessageDialogService, private dataShare: DataSharedService, private grafanaService: GrafanaAuthenticationService) {
    this.initializeVariable()
  }

  ngOnInit() {

  }

  async initializeVariable() {
    let configresponse = await this.serverconfigService.loadServerConfigurations()
    if (configresponse.status == 'success') {
      this.isConfigAvailable = true;
      var auth_uuid = configresponse.data.substring(0, 15);
      var data = configresponse.data.substring(15, configresponse.data.length);
      var dataValue = this.dataShare.decryptedData(auth_uuid, data);
      this.serverConfigData = dataValue;

      this.getconfigDataParsed(this.serverConfigData);
    } else {
      this.showErrorLogs(configresponse.message);
    }

  }

  getconfigDataParsed(data) {
    let number = 1;
    if (data != undefined) {
      for (let configDatakey of Object.keys(data)) {
        let serverConfig = new ServerConfigItem();
        let serverConfigChilds: ServerConfigItem[] = [];
        let value = data[configDatakey];
        if (value == undefined || value == null) {
          value = "";
        }
        var isRequiredField = this.requiredProperties.find(element => element === configDatakey) ? true : false;
        var inputType = this.secretProperties.find(element => element === configDatakey) ? 'password' : 'text';
        number = number + 1;
        if (typeof (data[configDatakey]) == 'object' && configDatakey != 'trustedHosts' && configDatakey != 'applicationLogLevel') {
          for (let configinnerDatakey of Object.keys(value)) {
            number = number + 1
            let serverConfigChild = new ServerConfigItem();
            var isRequiredChildField = this.requiredProperties.find(element => element === configinnerDatakey) ? true : false;
            var childKeyInputType = this.secretProperties.find(element => element === configinnerDatakey) ? 'password' : 'text';
            serverConfigChild.setData(number, configinnerDatakey, value[configinnerDatakey], typeof (value[configinnerDatakey]), isRequiredChildField, childKeyInputType);
            serverConfigChilds.push(serverConfigChild);
          }
          serverConfig.setData(number, configDatakey, value, typeof (value), isRequiredField, inputType, serverConfigChilds);
        } else if (configDatakey == 'trustedHosts' || configDatakey == 'applicationLogLevel') {
          serverConfig.setData(number, configDatakey, JSON.stringify(value, undefined, 4), typeof (value), isRequiredField, inputType);
        } else {
          if (configDatakey == 'password') {
            isRequiredField = false;
            inputType = 'text';
          }
          serverConfig.setData(number, configDatakey, value, typeof (value), isRequiredField, inputType);
        }
        console.log(serverConfig);
        this.ServerConfigItems.push(serverConfig);
      }
    }

    this.configLabelMerge();
  }

  getServerConfigItems(filtername: any) {
    if (filtername == 'object') {
      return this.ServerConfigItems.filter(item => (item.type == filtername && item.key != 'applicationLogLevel' && item.key != 'trustedHosts' && item.key != 'vault'));
    } else if (filtername == 'textArea') {
      return this.ServerConfigItems.filter(item => (item.key == 'trustedHosts' || item.key == 'applicationLogLevel'));
    } else {
      return this.ServerConfigItems.filter(item => item.type != 'object');
    }
  }

  async saveData(configDetails) {
    var self = this;
    var message = "";
    this.updatedConfigParamdata = {};
    if (this.validateServerConfigData(configDetails)) {
      for (let configParamData of this.ServerConfigItems) {
        if (configParamData.key != "trustedHosts" && configParamData.key != "applicationLogLevel" && configParamData.type == "object") {
          this.item = {};
          for (let configinnerData of configParamData.children) {
            this.item[configinnerData.key] = this.checkDatatype(configinnerData.value);
          }
          this.updatedConfigParamdata[configParamData.key] = this.item;
        } else if (configParamData.key != "trustedHosts" && configParamData.key != "applicationLogLevel" && configParamData.type != "object") {
          this.updatedConfigParamdata[configParamData.key] = this.checkDatatype(configParamData.value);
        } else if (configParamData.key == "trustedHosts" || configParamData.key == "applicationLogLevel") {
          this.updatedConfigParamdata[configParamData.key] = JSON.parse(configParamData.value);
        }
      }
      if (this.updatedConfigParamdata["autheticationProtocol"] != this.serverConfigData.autheticationProtocol ||
        this.updatedConfigParamdata["grafana"]["grafanaEndpoint"] != this.serverConfigData.grafana.grafanaEndpoint) {
        message = "<br/> <b>Please note:</b> You have changed some of the basic properties of server config which needs server restart. ";
      }
      console.log(this.updatedConfigParamdata)
      var dialogmessage = "Are you sure you want to save Server Config details ?" + message;
      var title = "Save Server Config ";
      const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
      dialogRef.afterClosed().subscribe(result => {
        if (result == "yes") {
          this.serverconfigService.saveServerConfigurations(JSON.stringify(this.updatedConfigParamdata))
            .then(function (response) {
              if (response.status == "success") {
                const dialogref = self.messageDialog.showApplicationsMessage("Server config details saved successfully.", "SUCCESS");
                dialogref.afterClosed().subscribe(res => {
                  self.grafanaService.serverConfigSubject.next('RELOAD_MENU');
                  self.redirectToLandingPage();
                })
              } else {
                self.showErrorLogs(response.message);
              }
            })
        }
      });
    }

  }

  validateServerConfigData(configDetails) {
    if (configDetails.graph_endpoint.length == 0 || configDetails.graph_authToken.length == 0
      || configDetails.graph_boltEndPoint.length == 0) {
      this.messageDialog.showApplicationsMessage("Please fill Neo4j details. ", "ERROR");
    } else if (configDetails.grafana_grafanaEndpoint.length == 0 || configDetails.grafana_grafanaDBEndpoint.length == 0) {
      this.messageDialog.showApplicationsMessage("Please fill Grafana details. ", "ERROR");
    } else if (configDetails.postgre_insightsDBUrl.length == 0 || configDetails.postgre_grafanaDBUrl.length == 0
      || configDetails.postgre_password.length == 0 || configDetails.postgre_userName.length == 0) {
      this.messageDialog.showApplicationsMessage("Please fill Postgre details. ", "ERROR");
    } else if (configDetails.messageQueue_host.length == 0 || configDetails.messageQueue_user.length == 0
      || configDetails.messageQueue_password.length == 0) {
      this.messageDialog.showApplicationsMessage("Please fill Message Queue Config details. ", "ERROR");
    } else if (configDetails.trustedHosts.length == 0 || JSON.parse(configDetails.trustedHosts).length == 0) {
      this.messageDialog.showApplicationsMessage("Please fill trusted hosts details, it cannot be empty. ", "ERROR");
    } else if (configDetails.insightsServiceURL.length == 0) {
      this.messageDialog.showApplicationsMessage("Please fill insightsServiceURL, it cannot be empty. ", "ERROR");
    } else if (configDetails.autheticationProtocol.length == 0) {
      this.messageDialog.showApplicationsMessage("Please fill authentication protocol. ", "ERROR");
    } else if (!(this.authenticationProtocolList.find(element => element === configDetails.autheticationProtocol))) {
      this.messageDialog.showApplicationsMessage("Please enter a valid authentication protocol. ", "ERROR");
    } else {
      return true;
    }

  }



  sendStatusMsg(Msg): void {
    this.showMessage = Msg
  }

  checkDatatype(dataVal) {

    if (typeof (dataVal) == "boolean") {
      return dataVal;
    } else if (isNaN(dataVal)) {
      if (dataVal == "true") {
        this.datatypeVal = true;
        return this.datatypeVal;
      } else if (dataVal == "false") {
        this.datatypeVal = false;
        return this.datatypeVal;
      } else {
        return dataVal;
      }
    } else {
      return parseInt(dataVal);
    }
  }


  configLabelMerge(): void {
    this.serverConfig = InsightsInitService.serverConfig;
    for (let configParamData of this.ServerConfigItems) {
      if (this.serverConfig.hasOwnProperty(configParamData.key)) {
        this.configAbbr[configParamData.key] = this.serverConfig[configParamData.key];
      } else {
        this.configAbbr[configParamData.key] = configParamData.key;
      }
    }
  }

  findDataType(key, arr): string {
    return typeof (arr[key]);
  }

  showErrorLogs(message) {
    if (message.includes("Vault_sealed")) {
      this.messageDialog.showApplicationsMessage(" Vault is sealed, Please unsealed it and try again. ", "ERROR");
    } else if (message.includes("ConnectException")) {
      this.messageDialog.showApplicationsMessage("Unable to connect to Vault server, Please restart Vault server. ", "ERROR");
    } else if (message.includes("Error while saving server config file")) {
      this.messageDialog.showApplicationsMessage("Failed to save server config details, Please check logs.", "ERROR");
    } else {
      this.messageDialog.showApplicationsMessage("Failed to load server config details, Please check logs.", "ERROR");
    }
  }

  redirectToLandingPage() {
    var self = this;
    self.router.navigateByUrl('/InSights/Home/landingPage/' + self.dataShare.getOrgId(), { skipLocationChange: true });

  }

}
