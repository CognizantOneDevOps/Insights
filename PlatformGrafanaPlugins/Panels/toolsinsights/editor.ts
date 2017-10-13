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
import { ToolsInsightModel, Fields } from './toolsInsightModel';

export class ToolsInsightEditorCtrl {
  toolsInsightsPanel: any;
  toolsInsightsPanelCtrl: any;
  dataSourceResponse: any;
  toolListData = [];
  selectedToolSeq = [];
  addColumnSegment: any;
  fieldList = [];
  selectedFieldList = [];
  toolsInsightModel: ToolsInsightModel[];
  toolDataJson = {};
  selectedToolsDetailJson = {};
  defaultJson = {};
  showFieldDetails: boolean = false;
  fieldVal: Fields[];
  defaultToolSequence = [];
  lastSelectedToolSequence = [];
  lastSelectedToolsJson = {};
  isDefaultValue: number = 1;
  isCustomValue: number;

  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    //super($scope, $q, uiSegmentSrv);
    var self = this;
    self.toolsInsightsPanelCtrl = $scope.ctrl;
    //console.log(self.toolsInsightsPanelCtrl.dataSourceResponse);
    if (self.toolsInsightsPanelCtrl.dataSourceResponse === undefined) {
      self.toolsInsightsPanelCtrl.dataSourceResponse = [];
    }
    if (self.toolsInsightsPanelCtrl.dataSourceResponse !== undefined && self.toolsInsightsPanelCtrl.dataSourceResponse.length !== 0) {
      var toolDataArray = self.toolsInsightsPanelCtrl.dataSourceResponse.results[0].data[0].row[0];
      self.getToolListOptions(toolDataArray);
    }
    self.selectedToolSeq = self.toolsInsightsPanelCtrl.pipelineToolsArray;
    self.defaultJson = self.toolsInsightsPanelCtrl.defaultMapping;
    self.defaultToolSequence = self.toolsInsightsPanelCtrl.pipelineToolsArrayDefault;
    if (self.toolsInsightsPanelCtrl.lastSelectedDetailJson !== undefined) {
      self.lastSelectedToolSequence = self.toolsInsightsPanelCtrl.lastSelectedSeq;
      self.lastSelectedToolsJson = self.toolsInsightsPanelCtrl.lastSelectedDetailJson;
    }
    if (self.selectedToolSeq === undefined) {
      self.selectedToolSeq = [];
    }
    if (self.toolsInsightsPanelCtrl.toolsDetailJson !== undefined) {
      self.selectedToolsDetailJson = self.toolsInsightsPanelCtrl.toolsDetailJson;
      self.selectedToolSeq = self.toolsInsightsPanelCtrl.selectedSeq;
      self.isCustomValue = self.toolsInsightsPanelCtrl.lastCustomValue;
      self.isDefaultValue = self.toolsInsightsPanelCtrl.lastDefaultValue;
    }
    self.checkEmptyToolList();
    self.fieldList = self.toolsInsightsPanelCtrl.toolDetails;
    self.toolsInsightsPanel = self.toolsInsightsPanelCtrl.panel.toolsInsightsPanelCtrl;
    self.addColumnSegment = this.uiSegmentSrv.newPlusButton();
    self.render();
  }

  //Use render method for refreshing the view.
  render() {
    this.toolsInsightsPanelCtrl.render();
  }

  getToolListOptions(toolDataArray) {
    this.toolDataJson = toolDataArray;
    for (var i in toolDataArray) {
      var toolListRow = toolDataArray[i]['toolName'];
      if (this.toolListData.indexOf(toolListRow[i]) === -1) {
        this.toolListData.push(toolListRow);
      }
    }
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
      this.selectedToolsDetailJson[toolName] = [];
    }
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.render();
  }

  removeTool(tool) {
    this.selectedToolSeq = _.without(this.selectedToolSeq, tool);
    delete this.selectedToolsDetailJson[tool];
    if (this.selectedToolSeq.length === 0) {
      this.toolsInsightsPanel["lastSelectedSeq"] = [];
      this.toolsInsightsPanel["selectedSeq"] = [];
    }
    this.render();
  }

  getFieldOptions(selectedTool) {
    this.fieldList = [];
    for (var i in this.toolDataJson) {
      var toolName = this.toolDataJson[i]['toolName'];
      if (toolName === selectedTool) {
        this.fieldList = this.toolDataJson[i]['keys'];
        break;
      }
    }
    let fieldList = this.fieldList;
    let segments = _.map(fieldList, (c) => this.uiSegmentSrv.newSegment({ value: c }));
    return this.$q.when(segments);
  }

  addFields(selectedToolNm) {
    let field = new Fields('', '');
    let fieldVal = this.addColumnSegment.value;
    var data = this.selectedToolsDetailJson[selectedToolNm];
    var index;
    if (data.length === 0) {
      index = -1;
    }
    if (data.length !== 0) {
      for (var i in data) {
        var inside = data[i];
        for (var j in inside) {
          index = inside.dbName.indexOf(fieldVal);
          if (inside.dbName === fieldVal) {
            index = 0;
            break;
          }
        }
        if (index === 0) {
          break;
        }
      }
    }
    if (index === -1) {
      this.selectedFieldList.push(fieldVal);
      this.selectedToolsDetailJson[selectedToolNm].push(field);
    }
    if (fieldVal) {
      this.showFieldDetails = true;
      field.dbName = fieldVal;
      this.selectedFieldList.push(fieldVal);
    }
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.render();
  }

  removeField(key, field, indexValue) {
    this.selectedFieldList = _.without(this.selectedFieldList, field);
    this.selectedToolsDetailJson[key].splice(indexValue, 1);
    this.render();
  }

  onSubmitAction(): void {
    this.toolsInsightsPanel["selectedToolsSeq"] = this.selectedToolSeq;
    this.toolsInsightsPanel["selectedSeq"] = [];
    this.render();
    var self = this;
    setTimeout(function () {
      self.toolsInsightsPanel["selectedSeq"] = self.selectedToolSeq;
      self.render();
    }, 200);
    this.toolsInsightsPanel["lastSelectedSeq"] = this.selectedToolSeq;
    this.toolsInsightsPanel["toolsDetailJson"] = this.selectedToolsDetailJson;
    this.toolsInsightsPanel["lastSelectedDetailJson"] = this.selectedToolsDetailJson;
    this.render();
  }

  checkFieldMapping() {
    var result = this.selectedToolsDetailJson;
    var length = 0;
    for (var i in result) {
      length++;
    }
    var toolListDataLen = this.toolListData.length;
    if (length !== 0 && toolListDataLen !== 0) {
      return true;
    } else {
      return false;
    }
  }

  checkValueMapping() {
    var data = this.selectedToolsDetailJson;
    var toolListDataLen = this.toolListData.length;
    var count = 0;
    for (var i in data) {
      if (data[i].length > 0) {
        count = 1;
      }
    }
    if (count === 1 && toolListDataLen !== 0) {
      return true;
    } else {
      return false;
    }
  }

  checkEmptyToolList() {
    var DsResponse = this.toolsInsightsPanelCtrl.dataSourceResponse;
    //console.log(DsResponse);
    var DsResponseLen = this.toolsInsightsPanelCtrl.dataSourceResponse.length;
    //console.log(DsResponseLen);
    if (DsResponse !== undefined) {
      if (DsResponseLen <= 1) {
        return true;
      }
    } else {
      return false;
    }
  }

  defaultValueAction() {
    var self = this;
    self.toolsInsightsPanel["selectedSeq"] = [];
    setTimeout(function () {
      self.toolsInsightsPanel["selectedSeq"] = self.defaultToolSequence;
      self.render();
    }, 200);
    self.selectedToolSeq = self.defaultToolSequence;
    self.selectedToolsDetailJson = self.defaultJson;
    //console.log(this.selectedToolsDetailJson);
    self.toolsInsightsPanel["toolsDetailJson"] = self.defaultJson;
    self.render();
    self.isDefaultValue = 0;
    self.isCustomValue = 1;
    self.toolsInsightsPanel["lastCustomValue"] = self.isCustomValue;
    self.toolsInsightsPanel["lastDefaultValue"] = self.isDefaultValue;
  }

  customValueAction() {
    var self = this;
    self.toolsInsightsPanel["selectedSeq"] = [];
    setTimeout(function () {
      self.toolsInsightsPanel["selectedSeq"] = self.lastSelectedToolSequence;
      self.render();
    }, 200);
    self.selectedToolSeq = self.lastSelectedToolSequence;
    self.selectedToolsDetailJson = self.lastSelectedToolsJson;
    //console.log(this.selectedToolsDetailJson);
    self.toolsInsightsPanel["toolsDetailJson"] = self.lastSelectedToolsJson;
    self.render();
    self.isCustomValue = 0;
    self.isDefaultValue = 1;
    self.toolsInsightsPanel["lastCustomValue"] = self.isCustomValue;
    self.toolsInsightsPanel["lastDefaultValue"] = self.isDefaultValue;
  }

  checkDefaultButtonActionValue() {
    if (this.isDefaultValue === 1) {
      return true;
    } else {
      return false;
    }
  }

  checkCustomButtonActionValue() {
    if (this.isCustomValue === 1) {
      return true;
    } else {
      return false;
    }
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
