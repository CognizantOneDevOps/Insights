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

	export class DataPurgingController {
		static $inject = ['appSettingsService', '$sce', '$cookies'];
		constructor(
			private appSettingsService,
			private $sce,
			private $cookies) {

			var self = this;
			var elem = document.querySelector('#homePageTemplateContainer');
			var homePageControllerScope = angular.element(elem).scope();
			var homePageController = homePageControllerScope['homePageController'];
			self.homeController = homePageController;

			self.showThrobber = true;
			self.showConfirmMessage = "";

			self.listData();

		}

		homeController: HomePageController;
		backupDatatype: string;
		fileLocation: string;
		rowLimit: string;
		fileFormat: string = "JSON";
		fileName: string;
		retention: string;
		settingsType: string;
		listView: boolean = true;
		saveView: boolean = false;
		showConfirmMessage: string;
		showThrobber: boolean;
		datalist = {};
		settingData = {};
		nextRunTime: string;
		lastRunTime: string;
		settingJsonstring: string;
		settingJsonObj = {};
		activeFlag: string;
		lastModifiedByUser: string;
		editIconSrc = "dist/icons/svg/userOnboarding/Edit_icon_MouseOver.svg";
		showTble: boolean = true;
		dataFreq: string;

		checkData(event, myValue, txtName) {

			if (myValue == undefined) { return '' }
			else {
				while (myValue.charAt(0) === '0') {
					myValue = myValue.substr(1);
				}
			}
			if (txtName == 'retention') { this.retention = myValue; }
		}

		addData(): void {
			this.listView = false;
			this.saveView = true;
		}

		saveData(): void {

			var self = this;
			self.listView = true;
			self.saveView = false;

			self.settingsType = "DATAPURGING";
			self.activeFlag = "Y";
			self.lastModifiedByUser = self.homeController.userName;

			/* if( self.backupDatatype.indexOf(',') >= 0){
				self.dataTypelabel = self.backupDatatype.split(",");				
			}else {
				self.dataTypelabel = [self.backupDatatype];
			} */

			self.settingJsonObj = {
				"backupRetentionInDays": self.retention,
				"rowLimit": self.rowLimit,
				"backupFileLocation": self.fileLocation,
				"backupFileFormat": self.fileFormat,
				"dataArchivalFrequency": self.dataFreq,
				"lastRunTime": '',
				"nextRunTime": ''
			}

			self.settingJsonstring = encodeURIComponent(JSON.stringify(self.settingJsonObj));

			self.appSettingsService.saveDatapurging(self.settingsType, self.activeFlag, self.lastModifiedByUser, self.settingJsonstring)
				.then(function (data) {

					if (data.status == "success") {
						self.showConfirmMessage = "Settings saved successfully";
					} else {
						self.showConfirmMessage = "Failed to save settings";
					}
					self.listData();
				})
				.catch(function (data) {
					self.listView = false;
					self.saveView = true;
					self.showConfirmMessage = "Failed to save settings";
					self.listData();
				});




		}

		listData(): void {
			var self = this;

			self.listView = true;
			self.saveView = false;

			self.appSettingsService.listDatapurgingdata("DATAPURGING")
				.then(function (response) {

					self.showThrobber = false;
					if (response.status == "success") {

						if (response.hasOwnProperty('data')) {
							self.showTble = false;
							self.datalist = response.data;
							self.settingData = JSON.parse(self.datalist['settingsJson']);
						} else {
							self.showTble = true;
						}
					}
					else {
						self.showConfirmMessage = "Something wrong with service, please try again";
					}

				})
				.catch(function (response) {
					self.showThrobber = false;
					self.showConfirmMessage = "Something wrong with service, please try again";
				});

			setTimeout(function () {
				self.showConfirmMessage = "";
				document.getElementById('confrmMsg').innerHTML = "";
			}, 3500);
		}

		showData(): void {
			var self = this;

			self.listView = false;
			self.saveView = true;
			self.retention = self.settingData['backupRetentionInDays'];
			self.rowLimit = self.settingData['rowLimit'];
			self.fileLocation = self.settingData['backupFileLocation'];
			self.fileFormat = self.settingData['backupFileFormat'];
			self.dataFreq = self.settingData['dataArchivalFrequency'];
			self.lastRunTime = self.settingData['lastRunTime'];
			self.nextRunTime = self.settingData['nextRunTime'];

		}

	}

}
