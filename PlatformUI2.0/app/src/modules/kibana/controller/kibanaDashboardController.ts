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
	export class KibanaDashboardController {
		static $inject = ['elasticSearchService', '$sce'];

		constructor(private elasticSearchService: IElasticSearchService, private $sce) {
			var self = this;
			this.elasticSearchService
				.loadKibanaIndex()
				.then(function(data) {
					var model = [];
					var dataArray = data.dashboards;
					dataArray.forEach(element => {
						model.push(new KibanaDashboardModel(element.title,
							element.id,
							element.url));
					});
					self.kibanaDashboards = model;
					self.setSelectedDashboard(model[0]);
				});
		}

		icon = {
			iconSrc: 'dist/icons/svg/ic_dashboard_24px.svg',name:'SCM'
		}

		originatorEv;

		kibanaDashboards: KibanaDashboardModel[];

		selectedDashboard : KibanaDashboardModel;

		openMenu($mdOpenMenu, ev){
			//this.originatorEv = ev;
      		$mdOpenMenu(ev);
		}

		setSelectedDashboard(dashboard: KibanaDashboardModel) : void{
			this.selectedDashboard = dashboard;
			dashboard.url = this.$sce.trustAsResourceUrl(dashboard.url);
		}

		loadDashboard(dashboard : KibanaDashboardModel){
			this.setSelectedDashboard(dashboard);
		}
	}
}
