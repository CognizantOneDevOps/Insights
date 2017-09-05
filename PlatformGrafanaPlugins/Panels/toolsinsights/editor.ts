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
import {ToolsInsightModel} from './toolsInsightModel';

export class ToolsInsightEditorCtrl {
  toolsInsightsPanel: any;
  toolsInsightsPanelCtrl: any;
  toolsInsightsPanelMetaData: any;
  dataSourceResponse: any;
  toolListData = [];
  selectedToolSeq = [];
  addColumnSegment: any;
  fieldList: any;
  selectedFieldList = [];
  toolsInsightModel: ToolsInsightModel[];

  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    //super($scope, $q, uiSegmentSrv);
    var self = this;
    self.toolsInsightsPanelCtrl = $scope.ctrl;
    self.selectedToolSeq = self.toolsInsightsPanelCtrl.pipelineToolsArray;
    if (self.selectedToolSeq === undefined) {
      self.selectedToolSeq = [];
    }
    self.toolListData = self.toolsInsightsPanelCtrl.toolsList;
    self.fieldList = self.toolsInsightsPanelCtrl.toolDetails;
    self.toolsInsightsPanel = self.toolsInsightsPanelCtrl.panel.toolsInsightsPanelCtrl;
    self.addColumnSegment = this.uiSegmentSrv.newPlusButton();
    self.render();
  }

  //Use render method for refreshing the view.
  render() {
    this.toolsInsightsPanelCtrl.render();
  }

  getToolOptions() {
    let toolList = this.toolListData;
    let segments = _.map(toolList, (c) => this.uiSegmentSrv.newSegment({ value: c }));
    return this.$q.when(segments);
  }

  addTool() {
    let toolName = this.addColumnSegment.value;
    var idx = this.selectedToolSeq.indexOf(toolName);
    if (idx === -1) {
      this.selectedToolSeq.push(toolName);
    }
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.render();
  }

  removeTool(tool) {
    this.selectedToolSeq = _.without(this.selectedToolSeq, tool);
    this.render();
  }

  getFieldOptions() {
    this.fieldList = this.toolsInsightsPanelCtrl.toolDetails;
    let fieldList = this.fieldList;
    let segments = _.map(fieldList, (c) => this.uiSegmentSrv.newSegment({ value: c }));
    return this.$q.when(segments);
  }

  addFields() {
    let fieldName = this.addColumnSegment.value;
    var idx = this.selectedFieldList.indexOf(fieldName);
    if (idx === -1) {
      this.selectedFieldList.push(fieldName);
    }
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.render();
  }

  removeField(field) {
    this.selectedFieldList = _.without(this.selectedFieldList, field);
    this.render();
  }

  onSubmitAction(): void {
    this.toolsInsightsPanel["selectedToolsSeq"] = this.selectedToolSeq;
    this.toolsInsightsPanel["message"] = '';
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
