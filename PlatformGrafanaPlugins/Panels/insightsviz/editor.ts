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

import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import angular from 'angular';
import kbn from 'app/core/utils/kbn';
import { InsightsVizOptionsRenderer } from './renderer';
import { InsightsVizTransformer } from './transformer';

export class InsightsVizEditorCtrl {
  insightsVizPanel: any;
  insightsVizPanelCtrl: any;
  insightsVizPanelMetaData: any;
  dataSourceResponse: any;
  parsedData: any;
  vizOptions: any = new InsightsVizOptionsRenderer().loadVizOptions();
  transformFunctionDefError: boolean;
  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    this.insightsVizPanelCtrl = $scope.ctrl;
    this.insightsVizPanel = this.insightsVizPanelCtrl.panel;
    this.insightsVizPanelMetaData = this.insightsVizPanel.insightsVizPanelMetaData;
    this.dataSourceResponse = this.insightsVizPanelCtrl.dataSourceResponse;
    this.parsedData = new InsightsVizTransformer().neo4jDataParser(this.dataSourceResponse);
  }

 //Use render method for refreshing the view.
  render() {
    //this.insightsVizPanelCtrl.render();
  }
}

/** @ngInject */
export function insightsVizEditorCtrl($q, uiSegmentSrv) {
  'use strict';
  return {
    restrict: 'E',
    scope: true,
    templateUrl: 'public/plugins/insightsviz/editor.html',
    controller: InsightsVizEditorCtrl,
    controllerAs : 'insightsVizEditorCtrl'
  };
}
