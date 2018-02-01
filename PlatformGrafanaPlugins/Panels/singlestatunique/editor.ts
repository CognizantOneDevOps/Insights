///<reference path="../../../headers/common.d.ts" />


import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import angular from 'angular';
import kbn from 'app/core/utils/kbn';

export class PipelinePanelEditorCtrl {
  uniqueSingleStatPanel: any;
  uniqueSingleStatPanelCtrl: any;
  uniqueSingleStatPanelMetaData: any;
  dataSourceResponse: any;
  fontsize: any;
  fieldsStatArray: any;
  selectedfield: any;
  neo4jDataStatus: any = false;
  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    //$scope.editor = this;
    var self = this;
    self.uniqueSingleStatPanelCtrl = $scope.ctrl;
    self.uniqueSingleStatPanel = self.uniqueSingleStatPanelCtrl.panel;
    self.uniqueSingleStatPanelMetaData = self.uniqueSingleStatPanel.uniqueSingleStatPanelMetaData;
    self.dataSourceResponse = self.uniqueSingleStatPanelCtrl.dataSourceResponse;
    self.neo4jDataStatus = self.uniqueSingleStatPanelCtrl.neo4jDataStatus;
    if (self.neo4jDataStatus === true) {
      self.fieldsStatArray = self.uniqueSingleStatPanelCtrl.fieldsStatArray;
      if (self.uniqueSingleStatPanelMetaData['selectedfield'] !== undefined) {
        self.selectedfield = self.uniqueSingleStatPanelMetaData['selectedfield'];
      }
    }
    self.getFontSizeValue();
    self.render();
  }

  getFontSizeValue() {
    var self = this;
    var fontlength = self.uniqueSingleStatPanelMetaData.inputProperties['font-size'].length;
    self.fontsize = self.uniqueSingleStatPanelMetaData.inputProperties['font-size'].substring(0, fontlength - 2);
    self.render();
  }
  setFontSizeValue() {
    var self = this;
    if (self.fontsize < 151) {
      self.uniqueSingleStatPanelMetaData.inputProperties['font-size'] = self.fontsize + 'px';
    }
  }
  setSelectedField() {
    var self = this;
    self.uniqueSingleStatPanelMetaData['selectedfield'] = self.selectedfield;
    self.render();
  }
  //Use render method for refreshing the view.
  render() {
    var self = this;
    if (self.neo4jDataStatus === true) {
      self.uniqueSingleStatPanelCtrl.setSelectedFieldUi();
    }
    self.uniqueSingleStatPanelCtrl.render();
  }
}

/** @ngInject */
export function pipelinePanelEditor($q, uiSegmentSrv) {
  'use strict';
  return {
    restrict: 'E',
    scope: true,
    templateUrl: 'public/plugins/singlestatunique/editor.html',
    controller: PipelinePanelEditorCtrl,
    controllerAs: 'pipelinePanelEditorCtrl'
  };
}
