/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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

module ISightApp {
	export class AgentManagementController {
		static $inject = ['agentService', 'restAPIUrlService', 'iconService', '$sce', '$mdDialog', '$cookies', 'toolConfigService', 'restEndpointService'];
		constructor(

			private agentService: IAgentService,
			private restAPIUrlService: IRestAPIUrlService,
			private iconService: IIconService,
			private $sce,
			private $mdDialog, private $cookies, private toolConfigService: IToolConfigService, private restEndpointService: IRestEndpointService) {

			var elem = document.querySelector('#homePageTemplateContainer');
			var homePageControllerScope = angular.element(elem).scope();
			var homePageController = homePageControllerScope['homePageController'];
			this.homeController = homePageController;

			var self = this;
			self.showMessage = "Please select version & tools";
			self.getOsVersionTools("");
			self.getSelectedAgentDetails();
			self.showConfig = false;
			self.showTrackingJsonUploadButton = self.homeController.showTrackingJsonUploadButton;

			if (self.editAgentDetails['type'] == "update") {
				self.getDbAgentConfig(self.editAgentDetails['agentid']);
				self.btnValue = "Update";
				self.buttonDisableStatus = false;
			} else {
				self.btnValue = "Register";
				self.validationArr = self.editAgentDetails['detailedArr'];
			}

			self.osLists = {
				"windows": "Windows",
				"linux": "Linux",
				"ubuntu": "Ubuntu",
			};
			self.getOsList();
		}

		datatypeVal: boolean;
		validationArr = {};
		osLists = {};
		configDesc = {};
		configAbbr = [];
		buttonDisableStatus: boolean = true;
		isRegisteredTool: boolean = false;
		btnValue: string;
		dynamicData: string;
		homeController: HomePageController;
		selectedTool: string;
		selectedOS: string;
		selectedVersion: string;
		showMessage: string;
		showConfig: boolean = false;
		showThrobber: boolean;
		versionList = [];
		toolsArr = [];
		response = {};
		editAgentDetails = {};
		headerData = [];
		updatedConfigdata = {};
		configData: string;
		item = {};
		defaultConfigdata = {};
		tempConfigdata: string;
		versionChangeddata = {};
		addButtIcon: string = "dist/icons/svg/actionIcons/Add_icon_disabled.svg";
		deleteButtIcon: string = "dist/icons/svg/actionIcons/Delete_icon_disabled.svg";
		editButtIcon: string = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
		saveButtonIcon: string = "dist/icons/svg/actionIcons/Save_icon_Disabled.svg";
		uploadedFile: File;
		isTypeError: string = "";
		files = [];
		fileUploadSuccessMessage: boolean = false;
		trackingUploadedFileContentStr: string = "";
		showTrackingJsonUploadButton: boolean;

		getOsList() {
			var self = this;
			var agentsListFromUiConfig = self.homeController.agentsOsList;
			if (agentsListFromUiConfig !== undefined)
				self.osLists = agentsListFromUiConfig;
		}

		getOsVersionTools(Selversion): void {

			var self = this;
			self.toolsArr = [];
			self.agentService.getDocRootAgentVersionTools()
				.then(function (data) {

					if (data.status == "success") {
						self.response = data.data;

						if (Selversion) {
							self.toolsArr = self.response[Selversion];
						} else {
							for (var key in self.response) {
								self.versionList.push(key);
							}
						}
					} else {
						self.showMessage = "Problem with Docroot URL (or) Platform service. Please try again";
					}

				})
				.catch(function (data) {
					self.showMessage = "Something wrong with service, Please try again";
				});
		}

		findDataType(key, arr): string {
			return typeof (arr[key]);
		}

		getSelectedAgentDetails(): void {
			this.editAgentDetails = this.homeController.selectedAgentID;
		}

		versionOnChange(key, type): void {
			var self = this;

			if (type == "validate") {
				if (self.selectedVersion === undefined || self.selectedTool === undefined || self.selectedOS === undefined) {
					self.buttonDisableStatus = true;
				}
				else { self.buttonDisableStatus = false; }
			}
			else if (type == "Update") {

				self.showConfig = false;
				self.showThrobber = true;
				self.showMessage = "";

				self.defaultConfigdata = JSON.parse(self.tempConfigdata);
				self.agentService.getDocrootAgentConfig(key, self.selectedTool)
					.then(function (vdata) {
						self.showConfig = true;
						self.showThrobber = false;
						self.versionChangeddata = JSON.parse(vdata.data);
						self.concatConfigelement(self.versionChangeddata);
						self.removeConfigelement(self.versionChangeddata);
						self.configLabelMerge();

					})
					.catch(function (vdata) {
						self.showThrobber = false;
						self.showMessage = "Something wrong with service, Please try again";
					});

			} else {
				self.buttonDisableStatus = true;
				self.selectedTool = "";
				self.toolsArr = [];
				self.toolsArr = self.response[key];
			}
		}

		concatConfigelement(addObj): void {
			var self = this;

			for (var vkeys in addObj) {

				if (self.findDataType(vkeys, addObj) == 'object' && vkeys != "dynamicTemplate") {

					if (!self.defaultConfigdata.hasOwnProperty(vkeys)) {
						self.defaultConfigdata[vkeys] = addObj[vkeys];
					}

					for (var vkeys1 in addObj[vkeys]) {

						if (!self.defaultConfigdata[vkeys].hasOwnProperty(vkeys1)) {
							self.defaultConfigdata[vkeys][vkeys1] = addObj[vkeys][vkeys1];
						}
					}

				} else {

					if (!self.defaultConfigdata.hasOwnProperty(vkeys)) {
						self.defaultConfigdata[vkeys] = addObj[vkeys];
					}
				}

			}
		}

		removeConfigelement(remObj): void {
			var self = this;

			for (var dkeys in self.defaultConfigdata) {

				if (self.findDataType(dkeys, self.defaultConfigdata) == 'object' && dkeys != "dynamicTemplate") {

					if (!remObj.hasOwnProperty(dkeys)) {
						delete self.defaultConfigdata[dkeys];
					}

					for (var dkeys1 in self.defaultConfigdata[dkeys]) {
						if (!remObj[dkeys].hasOwnProperty(dkeys1)) {
							delete self.defaultConfigdata[dkeys][dkeys1];
						}
					}

				} else {
					if (!remObj.hasOwnProperty(dkeys)) {
						delete self.defaultConfigdata[dkeys];
					}
				}
			}
		}

		getDocRootAgentConfig(version, toolName): void {
			var self = this;
			self.isRegisteredTool = false;
			self.checkValidation();

			if (!self.isRegisteredTool) {

				self.showConfig = false;
				self.showThrobber = true;
				self.showMessage = "";

				self.agentService.getDocrootAgentConfig(version, toolName)
					.then(function (data) {

						self.showThrobber = false;

						if (data.status == "success") {
							self.showConfig = true;
							self.defaultConfigdata = JSON.parse(data.data);
							self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamicTemplate'], undefined, 4);
							self.configLabelMerge();

							if (self.selectedOS === undefined || self.dynamicData == '') {
								self.buttonDisableStatus = true;
							} else {
								self.buttonDisableStatus = false;
							}

						} else {
							self.buttonDisableStatus = true;
							self.showMessage = "Something wrong with service, Please try again";
						}

					})
					.catch(function (data) {
						self.showThrobber = false;
						self.showMessage = "Something wrong with service, Please try again";
					});

			} else {
				self.buttonDisableStatus = true;
				self.showConfig = false;
				self.showMessage = toolName.charAt(0).toUpperCase() + toolName.slice(1) + " agent is already registered, Please select other tool.";
			}
		}

		getDbAgentConfig(agentId): void {
			var self = this;
			self.showConfig = false;
			self.showThrobber = true;
			self.showMessage = "";

			self.agentService.getDbAgentConfig(agentId)
				.then(function (data) {

					self.showConfig = true;
					self.showThrobber = false;
					self.tempConfigdata = data.data.agentJson;

					self.defaultConfigdata = JSON.parse(self.tempConfigdata);
					self.selectedVersion = data.data.agentVersion;
					self.selectedOS = data.data.osVersion;
					self.getOsVersionTools(self.selectedVersion);
					self.selectedTool = data.data.toolName;
					self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamicTemplate'], undefined, 4);
					self.configLabelMerge();

				})
				.catch(function (data) {
					self.showThrobber = false;
					self.showMessage = "Something wrong with service, Please try again";
				});

			if (self.dynamicData == '') {
				self.buttonDisableStatus = true;
			}

		}

		configLabelMerge(): void {

			var self = this;
			self.configDesc = self.restEndpointService.getConfigDesc();
			for (var key in self.defaultConfigdata) {
				if (self.configDesc.hasOwnProperty(key)) {
					self.configAbbr[key] = self.configDesc[key];
				} else {
					self.configAbbr[key] = key;
				}
			}
		}

		sendStatusMsg(Msg): void {
			this.homeController.showConfirmMessage = Msg;
			this.homeController.templateName = 'agentList';
		}

		checkDatatype(dataVal) {

			if (typeof (dataVal) == "boolean") { return dataVal; }
			else if (isNaN(dataVal)) {

				if (dataVal == "true") { this.datatypeVal = true; return this.datatypeVal; }
				else if (dataVal == "false") { this.datatypeVal = false; return this.datatypeVal; }
				else { return dataVal; }

			}
			else {
				return parseInt(dataVal);
			}
		}

		getUpdatedConfigData(actionType): void {
			var self = this;
			self.updatedConfigdata = {};

			for (var key in self.defaultConfigdata) {

				if (key != "dynamicTemplate" && self.findDataType(key, self.defaultConfigdata) == "object") {

					self.item = {};

					for (var value in self.defaultConfigdata[key]) {
						self.item[value] = self.checkDatatype(self.defaultConfigdata[key][value]);
					}

					self.updatedConfigdata[key] = self.item;

					if (key == "communication" && self.dynamicData != "") {
						self.updatedConfigdata["dynamicTemplate"] = JSON.parse(self.dynamicData);
					}

				} else if (key != "dynamicTemplate") {
					self.updatedConfigdata[key] = self.checkDatatype(self.defaultConfigdata[key]);
				}
			}

			if (self.updatedConfigdata) {

				self.configData = "";
				self.configData = encodeURIComponent(JSON.stringify(self.updatedConfigdata));

				if (actionType == "Update") {

					self.agentService.updateAgent(self.editAgentDetails['agentid'], self.configData, self.selectedTool, self.selectedVersion, self.selectedOS)
						.then(function (data) {

							if (data.status == "success") {
								self.sendStatusMsg("updated");
							} else {
								self.sendStatusMsg("update");
							}
						})
						.catch(function (data) {
							self.sendStatusMsg("service_error");
						});


				} else {

					self.agentService.registerAgent(self.selectedTool, self.selectedVersion, self.selectedOS, self.configData, self.trackingUploadedFileContentStr)
						.then(function (data) {
							console.log(data);
							if (data.status == "success") {
								self.sendStatusMsg("registered");
							} else {
								self.sendStatusMsg("register");
							}
						})
						.catch(function (data) {
							self.sendStatusMsg("service_error");
						});

				}

			}
		}

		checkValidation(): void {
			var self = this;
			for (var key in self.validationArr) {
				if (self.validationArr[key]['tool'] == self.selectedTool) {
					self.isRegisteredTool = true;
					self.selectedTool = "";
				}
			}
		}

		uploadFile() {
			var self = this;
			var inputFileById = (<HTMLInputElement>document.getElementById("fileInp"));
			var uploadedFile = inputFileById.files[0];
			var testFileExt = self.checkFile(uploadedFile, ".json");
			if (!testFileExt) {
				self.isTypeError = "showError";
				setTimeout(function () {
					self.isTypeError = "";
				}, 500);
			}
			if (testFileExt) {
				// Turn the FileList object into an Array
				self.files = []
				for (var i = 0; i < inputFileById.files.length; i++) {
					self.files.push(inputFileById.files[i])
				}
				self.getTrackingFileContentToString(self.files[0]);
				self.fileUploadSuccessMessage = true;
			}
		}

		checkFile(sender, validExts) {
			if (sender) {
				var fileExt = sender.name;
				fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
				if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
					(<HTMLInputElement>document.getElementById("fileInp")).value = "";
					return false;
				}
				else return true;
			}
		}

		getTrackingFileContentToString(trackingJsonFileArray) {
			let reader: any = new FileReader();
			reader.readAsText(trackingJsonFileArray);

			reader.onload = (e) => {
				let json: string = reader.result;
				this.trackingUploadedFileContentStr = json;
			}
		}

		cancelFileUpload() {
			this.trackingUploadedFileContentStr = "";
			this.files = [];
			this.isTypeError = "";
			this.fileUploadSuccessMessage = false;
		}

	}

}
