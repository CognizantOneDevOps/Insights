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
  addColumnSegment: any;
  fieldList = [];
  selectedFieldList = [];
  toolDataJson = {};
  selectedToolsDetailJson = {};
  showFieldDetails: boolean = false;
  fieldVal: Fields[];
  toolMappingJson = [];
  defaultMappingJson = [];
  defaultButtonOption: number;
  advanceSettingOption = 0;

  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    //super($scope, $q, uiSegmentSrv);
    var self = this;
    self.toolsInsightsPanelCtrl = $scope.ctrl;
    if (self.toolsInsightsPanelCtrl.dataSourceResponse === undefined) {
      self.toolsInsightsPanelCtrl.dataSourceResponse = [];
    }
    if (self.toolsInsightsPanelCtrl.dataSourceResponse !== undefined && self.toolsInsightsPanelCtrl.dataSourceResponse.length !== 0) {
      var toolDataArray = self.toolsInsightsPanelCtrl.dataSourceResponse.results[0].data[0].row[0];
      self.getToolListOptions(toolDataArray);
    }

    if (self.toolsInsightsPanelCtrl.toolDetailMappingJson !== undefined) {
      self.toolMappingJson = self.toolsInsightsPanelCtrl.toolDetailMappingJson;
    }
    if (self.defaultButtonOption === undefined) {
      self.defaultButtonOption = 1;
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
        this.getDefaultTools(toolListRow);
        this.getDefaultFieldMapping(toolListRow);
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
    var idx;
    if (this.toolMappingJson.length === 0) {
      idx = -1;
    }
    for (var i in this.toolMappingJson) {
      if (this.toolMappingJson[i].toolName === toolName) {
        idx = 0;
      }
      else {
        idx = -1;
      }
    }
    if (idx === -1) {
      this.toolMappingJson.push(toolData);
      toolData.toolName = toolName;
    }
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.onSubmitAction();
    this.render();
  }

  removeTool(tool, index) {
    for (var i in this.toolMappingJson) {
      if (this.toolMappingJson[i].toolName === tool)
        /*delete this.toolMappingJson[i];*/
        this.toolMappingJson.splice(index, 1);
    }
    this.onSubmitAction();
    this.render();
  }

  getDefaultTools(tool) {
    let toolData = new Tools('', []);
    this.defaultMappingJson.push(toolData);
    toolData.toolName = tool;
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

  getDefaultFieldMapping(tool) {
    var fieldList = [];
    var keysMap = Object.keys(this.toolDataJson[0]);
    for (var i in this.toolDataJson) {
      var toolName = this.toolDataJson[i][keysMap[0]];
      if (toolName === tool) {
        fieldList = this.toolDataJson[i][keysMap[1]];
        for (var fields in fieldList) {
          var option = fieldList[fields];
          this.addDefaultFieldOption(tool, option);
        }
        break;
      }
    }
  }

  addDefaultFieldOption(tool, option) {
    let field = new Fields('', '');
    for (var i in this.defaultMappingJson) {
      if (this.defaultMappingJson[i].toolName === tool) {
        this.defaultMappingJson[i].fields.push(field);
        field.fieldName = option;
      }
    }
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
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.onSubmitAction();
    this.render();
  }

  removeField(key, field, indexValue) {
    this.selectedFieldList = _.without(this.selectedFieldList, field);
    for (var i in this.toolMappingJson) {
      if (this.toolMappingJson[i].toolName === key)
        this.toolMappingJson[i].fields.splice(indexValue, 1);
    }
    this.onSubmitAction();
    this.render();
  }

  onSubmitAction(): void {
    var self = this;
    self.toolsInsightsPanel["toolDetailMappingJson"] = self.toolMappingJson;
    this.render();
  }

  defaultValueAction() {
    var self = this;
    self.toolMappingJson = self.defaultMappingJson;
    this.defaultButtonOption = 0;
    self.onSubmitAction();
    self.render();
  }

  customValueAction() {
    var self = this;
    self.toolMappingJson = [];
    this.defaultButtonOption = 1;
    self.onSubmitAction();
    self.render();
  }

  checkButtonForMapping() {
    if (this.defaultButtonOption === 1) {
      return true;
    }
    else if (this.defaultButtonOption === 0) {
      return false;
    }
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
    var DsResponseLen = this.toolsInsightsPanelCtrl.dataSourceResponse.length;
    if (DsResponse !== undefined) {
      if (DsResponseLen <= 1) {
        return true;
      }
    } else {
      return false;
    }
  }

  advanceViewAccordian() {
    this.advanceSettingOption = 1;
  }

  advanceViewAccordianHide() {
    this.advanceSettingOption = 0;
  }

}

/** @ngInject */
export function toolsInsightEditor($q, uiSegmentSrv) {
  'use strict';
  return {
    restrict: 'E',
    scope: true,
    templateUrl: 'public/plugins/toolsinsights/editor.html',
    controller: ToolsInsightEditorCtrl,
    controllerAs: 'toolsInsightEditorCtrl'
  };
}
