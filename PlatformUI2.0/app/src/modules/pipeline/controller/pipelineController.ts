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

/// <reference path="../../../_all.ts" />


module ISightApp {
	export class PipelineController {
		static $inject = ['pipelineService', '$mdDialog'];
		constructor(private pipelineService: IPipelineService, private $mdDialog) {
			var self = this;
			this.pipelineService
				.loadPipelineData()
				.then(function (data) {
					self.pipelineDataArray = data;
				});
		}

		pipelineDataArray: PipelineData[];

		message: string = 'This is test';

		fonts = [
			{ iconSrc: 'dist/icons/svg/SCM-new.svg', name: 'SCM' },
			{ iconSrc: 'dist/icons/svg/CI-new.svg', name: 'CI' },
			{ iconSrc: 'dist/icons/svg/code-quality-new.svg', name: 'Code Quality' },
			{ iconSrc: 'dist/icons/svg/deployment-new.svg', name: 'Deployment' },
		];

		showTabDialog(params, userData, appData): void {
			var self = this;
			this.$mdDialog.show({
				controller: DialogController,
				controllerAs: 'dialog',
				templateUrl: './dist/modules/pipeline/view/tabDialog.tmpl.html',
				parent: angular.element(document.body),
				targetEvent: params,
				clickOutsideToClose: true,
				locals :{
					userData : userData,
					appData : appData
				},
				bindToController: true
			})
		}

		hide(): void {
			this.$mdDialog.hide();
		}

		cancel(): void {
			this.$mdDialog.cancel();
		}

		getToolDataLength(obj) : number {
      return Object.keys(obj).length;
    }

	}
}
