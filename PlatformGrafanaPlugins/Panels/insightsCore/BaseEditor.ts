/********************************************************************************
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


///<reference path="../../../headers/common.d.ts" />
import { BaseParser } from './BaseParser';

export class BaseEditorCtrl {
  panel: any;
  panelCtrl: any;
  insightsPanelData: any;
  dataSourceResponse: any;
  responseParser: BaseParser;
  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    this.panelCtrl = $scope.ctrl;
    this.panel = this.panelCtrl.panel;
    this.insightsPanelData = this.panel.insightsPanelData;
    this.dataSourceResponse = this.panelCtrl.dataSourceResponse;
    this.responseParser = this.panelCtrl.responseParser;
  }

  /** @ngInject */
  static loadEditorCtrl($q, uiSegmentSrv) {
    'use strict';
    return {
      restrict: 'E',
      scope: true,
      templateUrl: this['templateUrl'],
      controller: this['controller'],
      controllerAs: this['controllerAs']
    };
  }

  protected getPanel() {
    return this.panel;
  }

  protected getDataSourceRespone() {
    return this.dataSourceResponse;
  }

  protected getInsightsPanelData() {
    return this.insightsPanelData;
  }

  protected getResponseParser() {
    return this.responseParser;
  }

  protected getDatasourceType(){
    return this.panelCtrl.getDatasourceType();
  }

  render() {
    this.panelCtrl.render();
  }
}
