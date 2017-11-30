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
import { ToolsInsightModel, Tools, Fields } from './toolsInsightModel';

export class ToolsInsightEditorCtrl {
  toolsInsightsPanel: any;
  toolsInsightsPanelCtrl: any;
  dataSourceResponse: any;
  toolListData = [];
  selectedToolSeq = [];
  addColumnSegment: any;
  fieldList = [];
  selectedFieldList = [];
  toolDataJson = {};
  selectedToolsDetailJson = {};
  showFieldDetails: boolean = false;
  fieldVal: Fields[];
  toolMappingJson = [];
  defaultMappingJson = [];

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
    if (self.toolsInsightsPanelCtrl.lastSelectedDetailJson !== undefined) {
    }
    if (self.selectedToolSeq === undefined) {
      self.selectedToolSeq = [];
    }
    if (self.toolsInsightsPanelCtrl.toolsDetailJson !== undefined) {
      self.selectedToolsDetailJson = self.toolsInsightsPanelCtrl.toolsDetailJson;
      self.toolMappingJson = self.toolsInsightsPanelCtrl.toolDetailMappingJson;
      self.selectedToolSeq = self.toolsInsightsPanelCtrl.selectedSeq;
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
    let toolData = new Tools('', []);
    var keysMap = Object.keys(toolDataArray[0]);
    for (var i in toolDataArray) {
      var toolListRow = toolDataArray[i][keysMap[0]];
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
    let toolData = new Tools('', []);
    var idx = this.selectedToolSeq.indexOf(toolName);
    if (idx === -1) {
      this.selectedToolSeq.push(toolName);
      this.selectedToolsDetailJson[toolName] = [];
      this.toolMappingJson.push(toolData);
      toolData.toolName = toolName;
    }
    //console.log(this.toolMappingJson);
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.onSubmitAction();
    this.render();
  }

  removeTool(tool, index) {
    this.selectedToolSeq = _.without(this.selectedToolSeq, tool);
    delete this.selectedToolsDetailJson[tool];
    for (var i in this.toolMappingJson) {
      if (this.toolMappingJson[i].toolName === tool)
        /*delete this.toolMappingJson[i];*/
        this.toolMappingJson.splice(index, 1);
    }
    //console.log(this.toolMappingJson);
    if (this.selectedToolSeq.length === 0) {
      this.toolsInsightsPanel["selectedSeq"] = [];
    }
    this.onSubmitAction();
    this.render();
  }

  getFieldOptions(selectedTool) {
    this.fieldList = [];
    var keysMap = Object.keys(this.toolDataJson[0]);
    for (var i in this.toolDataJson) {
      var toolName = this.toolDataJson[i][keysMap[0]];
      if (toolName === selectedTool) {
        this.fieldList = this.toolDataJson[i][keysMap[1]];
        break;
      }
    }
    let fieldList = this.fieldList;
    let segments = _.map(fieldList, (c) => this.uiSegmentSrv.newSegment({ value: c }));
    return this.$q.when(segments);
  }

  addFields(selectedToolNm) {
    let field = new Fields('', '');
    let toolField = new Tools('', []);
    let fieldVal = this.addColumnSegment.value;
    var index;
    var toolMappingJsonData;

    for (var i in this.toolMappingJson) {
      if (selectedToolNm === this.toolMappingJson[i].toolName) {
        toolMappingJsonData = this.toolMappingJson[i].fields;
      }
    }
    if (toolMappingJsonData !== undefined && toolMappingJsonData.length === 0) {
      index = -1;
    }
    if (toolMappingJsonData !== undefined && toolMappingJsonData.length !== 0) {
      for (var i in toolMappingJsonData) {
        var inside = toolMappingJsonData[i];
        for (var j in inside) {
          index = inside.fieldName.indexOf(fieldVal);
          if (inside.fieldName === fieldVal) {
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
      for (var i in this.toolMappingJson) {
        if (this.toolMappingJson[i].toolName === selectedToolNm) {
          this.toolMappingJson[i].fields.push(field);
        }
      }
    }
    if (fieldVal) {
      this.showFieldDetails = true;
      field.fieldName = fieldVal;
      this.selectedFieldList.push(fieldVal);
    }
    //console.log(this.toolMappingJson);
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.onSubmitAction();
    this.render();
  }

  removeField(key, field, indexValue) {
    this.selectedFieldList = _.without(this.selectedFieldList, field);
    this.selectedToolsDetailJson[key].splice(indexValue, 1);
    for (var i in this.toolMappingJson) {
      if (this.toolMappingJson[i].toolName === key)
        this.toolMappingJson[i].fields.splice(indexValue, 1);
    }
    //console.log(this.toolMappingJson);
    this.onSubmitAction();
    this.render();
  }

  onSubmitAction(): void {
    this.toolsInsightsPanel["selectedToolsSeq"] = this.selectedToolSeq;
    this.toolsInsightsPanel["selectedSeq"] = this.selectedToolSeq;
    var self = this;
    self.toolsInsightsPanel["toolDetailMappingJson"] = self.toolMappingJson;
    /* self.toolsInsightsPanel["toolDetailMappingJson"] = [];
     setTimeout(function () {
       self.toolsInsightsPanel["toolDetailMappingJson"] = self.toolMappingJson;
       self.render();
     }, 200);*/
    //this.toolsInsightsPanel["toolDetailMappingJson"] = this.toolMappingJson;
    this.toolsInsightsPanel["toolsDetailJson"] = this.selectedToolsDetailJson;
    this.render();
  }

  checkFieldMapping() {
    var length = this.toolMappingJson.length;
    var toolListDataLen = this.toolListData.length;
    if (length !== 0 && toolListDataLen !== 0) {
      return true;
    } else {
      return false;
    }
  }

  checkValueMapping() {
    var data = this.toolMappingJson;
    var toolListDataLen = this.toolListData.length;
    var count = 0;
    for (var i in data) {
      if (data[i].fields.length > 0) {
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
