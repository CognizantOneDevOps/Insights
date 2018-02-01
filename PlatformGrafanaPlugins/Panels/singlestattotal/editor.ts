///<reference path="../../../headers/common.d.ts" />


import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import angular from 'angular';
import kbn from 'app/core/utils/kbn';

export class PipelinePanelEditorCtrl {
  totalSingleStatPanel: any;
  totalSingleStatPanelCtrl: any;
  totalSingleStatPanelMetaData: any;
  dataSourceResponse: any;
  fontsize: any;
  fieldsStatArray: any;
  selectedfield: any;
  neo4jDataStatus: any = false;
  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    //$scope.editor = this;
    var self = this;
    self.totalSingleStatPanelCtrl = $scope.ctrl;
    self.totalSingleStatPanel = self.totalSingleStatPanelCtrl.panel;
    self.totalSingleStatPanelMetaData = self.totalSingleStatPanel.totalSingleStatPanelMetaData;
    self.dataSourceResponse = self.totalSingleStatPanelCtrl.dataSourceResponse;
    self.neo4jDataStatus = self.totalSingleStatPanelCtrl.neo4jDataStatus;
    if (self.neo4jDataStatus === true) {
      self.fieldsStatArray = self.totalSingleStatPanelCtrl.fieldsStatArray;
      if (self.totalSingleStatPanelMetaData['selectedfield'] !== undefined) {
        self.selectedfield = self.totalSingleStatPanelMetaData['selectedfield'];
      }
    }
    self.getFontSizeValue();
    self.render();
  }

  getFontSizeValue() {
    var self = this;
    var fontlength = self.totalSingleStatPanelMetaData.inputProperties['font-size'].length;
    self.fontsize = self.totalSingleStatPanelMetaData.inputProperties['font-size'].substring(0, fontlength - 2);
    self.render();
  }
  setFontSizeValue() {
    var self = this;
    if (self.fontsize < 151) {
      self.totalSingleStatPanelMetaData.inputProperties['font-size'] = self.fontsize + 'px';
    }
  }
  setSelectedField() {
    var self = this;
    self.totalSingleStatPanelMetaData['selectedfield'] = self.selectedfield;
    self.render();
  }
  //Use render method for refreshing the view.
  render() {
    this.totalSingleStatPanelCtrl.render();
  }
}

/** @ngInject */
export function pipelinePanelEditor($q, uiSegmentSrv) {
  'use strict';
  return {
    restrict: 'E',
    scope: true,
    templateUrl: 'public/plugins/singlestattotal/editor.html',
    controller: PipelinePanelEditorCtrl,
    controllerAs: 'pipelinePanelEditorCtrl'
  };
}
