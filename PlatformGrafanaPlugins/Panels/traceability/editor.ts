///<reference path="../../../headers/common.d.ts" />

import { BaseEditorCtrl } from '../insightsCore/BaseEditor';
import { BaseCharts } from '../insightsCore/BaseCharts';
import { BaseParser, ParsedResponse } from '../insightsCore/BaseParser';
import { ChartModel, ChartData, ColumnModel, ContainerModel } from '../insightsCore/ChartModel';
import { InsightsChartEditorModel, InsightsChartTargetModel } from '../insightscharts/models';
import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import angular from 'angular';
import kbn from 'app/core/utils/kbn';

export class ToolsInsightEditorCtrl extends BaseEditorCtrl {
  toolsInsightsPanel: any;
  toolsInsightsPanelCtrl: any;
  toolsInsightsPanelMetaData: any;
  dataSourceResponse: any;
  showDagreGraph: boolean = false;
  checkboxSelArr = [];
  checkboxSel: boolean = false;

  /** @ngInject */
  constructor($scope, $q, uiSegmentSrv) {
    super($scope, $q, uiSegmentSrv);
    var self = this;
    self.toolsInsightsPanelCtrl = $scope.ctrl;
    self.toolsInsightsPanel = self.toolsInsightsPanelCtrl.panel.toolsInsightsPanelCtrl;
    self.render();
  }

  //Use render method for refreshing the view.
  render() {
    this.toolsInsightsPanelCtrl.render();
  }

  newVersion(): void {
    var idx = this.checkboxSelArr.indexOf("selected");
    if (idx > -1) {
      this.checkboxSelArr.splice(idx, 1);
      this.toolsInsightsPanel["showDagreGraph"] = false;
    }else {
      this.checkboxSelArr.push("selected");
      this.toolsInsightsPanel["showDagreGraph"] = true;
    }
    this.render();
  }

  oldVersion(): void {
    this.toolsInsightsPanel["showDagreGraph"] = false;
    this.render();
  }
}

/** @ngInject */
export function toolsInsightEditor($q, uiSegmentSrv) {
  'use strict';
  return {
    restrict: 'E',
    scope: true,
    templateUrl: 'public/app/plugins/panel/toolsinsights/editor.html',
    controller: ToolsInsightEditorCtrl,
    controllerAs: 'toolsInsightEditorCtrl'
  };
}
