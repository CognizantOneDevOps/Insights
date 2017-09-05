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

import angular from 'angular';
import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import * as FileExport from 'app/core/utils/file_export';
import { MetricsPanelCtrl } from 'app/plugins/sdk';
import { insightsVizEditorCtrl } from './editor';
import { InsightsVizTransformer } from './transformer';
import { InsightsVizOptionsRenderer } from './renderer';
import * as d3 from 'd3';

class InsightsVizPanelCtrl extends MetricsPanelCtrl {
  static templateUrl = 'module.html';

  dataSourceResponse: any;
  panelDefaults = {
    insightsVizPanelMetaData: {}
  };
  options: any;
  data: any;
  vizOptions: any = new InsightsVizOptionsRenderer().loadVizOptions();

  /** @ngInject */
  constructor($scope, $injector, private annotationsSrv, private $sanitize, private $window) {
    super($scope, $injector);
    _.defaults(this.panel, this.panelDefaults);
    this.events.on('data-received', this.onDataReceived.bind(this));
    this.events.on('data-error', this.onDataError.bind(this));
    this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
    this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
  }

  onInitEditMode() {
    this.addEditorTab('Options', insightsVizEditorCtrl, 2);
  }

  onInitPanelActions(actions) {
    actions.push({ text: 'Export CSV', click: 'ctrl.exportCsv()' });
  }

  issueQueries(datasource) {
    return super.issueQueries(datasource);
  }

  onDataError(err) {
    this.dataSourceResponse = [];
    this.render();
  }

  onDataReceived(dataList) {
    this.dataSourceResponse = dataList;
    this.render();
  }

  render() {
    var selectedVizOptions = this.vizOptions[this.panel.insightsVizPanelMetaData['selectedVizOption']];
    if (selectedVizOptions) {
      this.options = selectedVizOptions.defaultOptions;
      this.options.chart.height = parseInt(this.row.height.replace('px', ''));
      var parsedData = new InsightsVizTransformer().neo4jDataParser(this.dataSourceResponse);
      var fieldMapping = this.panel.insightsVizPanelMetaData.fieldMapping;
      if (fieldMapping) {
        var rawData = parsedData['data'];
        if (rawData){
          this.data = selectedVizOptions.dataFormatMapper(rawData, fieldMapping);
        }
      }
    }
    return super.render(this.dataSourceResponse);
  }

  //Essential
  link(scope, elem, attrs, ctrl) {
    var data;
    var panel = ctrl.panel;
    var pageCount = 0;
    var formaters = [];
    function renderPanel() {
    }

    ctrl.events.on('render', function (renderData) {
      data = renderData || data;
      if (data) {
        renderPanel();
      }
      ctrl.renderingCompleted();
    });
  }
}

export {
  InsightsVizPanelCtrl,
  InsightsVizPanelCtrl as PanelCtrl
};
