///<reference path="../../../headers/common.d.ts" />


import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import angular from 'angular';
import kbn from 'app/core/utils/kbn';
import {PipelineModel, FieldModel} from './pipelineModel';

export class PipelinePanelEditorCtrl {
  pipelinePanel: any;
  pipelinePanelCtrl: any;
  pipelinePanelMetaData: any;
  dataSourceResponse: any;
  fieldsList: FieldModel[] = [];
  pipelinesList: PipelineModel[];
  fieldsOptionArray: any = [];
  cloneUiArray: any = [];
  pipelineFieldsDropdownMenu = [];
  receivedDataStatus: boolean = false;

  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv, private $timeout) {
    this.pipelinePanelCtrl = $scope.ctrl;
    this.pipelinePanel = this.pipelinePanelCtrl.panel;
    this.dataSourceResponse = this.pipelinePanelCtrl.dataSourceResponse;
    if (this.dataSourceResponse === undefined || this.dataSourceResponse.length === 0) {
     this.receivedDataStatus = false;
    } else {
      this.receivedDataStatus = true;
    }
    this.pipelinePanelMetaData = this.pipelinePanel.pipelinePanelMetaData;
    this.pipelinesList = this.pipelinePanelMetaData.pipelinesList;
    if (this.pipelinesList === undefined) {
      this.pipelinesList = [];
      this.pipelinePanelMetaData['pipelinesList'] = this.pipelinesList;
    }
    this.cloneUiArray = this.pipelinePanelCtrl.cloneUiArray;
    this.getPipelineData();
    this.render();


  }
  getPipelineData() {
    var self = this;
    var data = self.dataSourceResponse;
    if (data !== undefined) {
      //check for  if no of real pipelines  == 0
      if (data.length === 0) {
        self.pipelinePanelMetaData['pipelinesList'] = self.pipelinesList = [];
      }
      //check for  if no of real pipelines < no of saved pipelines
      if (data.length !== 0) {
        for (var i = 0; i < self.pipelinesList.length; i++) {
          if (i >= 0) {
            let flag = 0;
            for (let target of data.targets) {
              if (self.pipelinesList[i].pipelineRefId === target.refId) {
                if (target.target === "" || target.target === null) {
                  self.pipelinesList.splice(i, 1);
                  i = i - 1;
                }
                flag = 1;
                break;
              }
            }
            if (flag === 0) {
              self.pipelinesList.splice(i, 1);
              i = i - 1;
            }
          }
        }

      }

      //check for  if no of real pipelines > no of saved pipelines
      if (data.length !== 0 && data.targets) {

        if (data.targets.length === data.results.length) {
          for (var i = 0; i < data.targets.length; i++) {
            var target = data.targets[i];
            let flag = 0;
            if (target.target === null || target.target === "") {

            } else {

              for (let pipeline of self.pipelinesList) {
                if (target.refId === pipeline.pipelineRefId) {
                  flag = 1;
                  self.pipelineFieldsDropdownMenu.push(data.results[i].columns);
                  break;
                }
              }
              if (flag === 0) {
                self.fieldsList = [];
                self.fieldsList.push(new FieldModel('', '', '', 1));
                self.pipelinesList.push(new PipelineModel(target.refId, self.fieldsList));
                self.pipelineFieldsDropdownMenu.push(data.results[i].columns);
              }

            }
          }
        }
      }
    }
  }
  //Use render method for refreshing the view.
  render() {
    var self = this;
    if (self.pipelinesList.length === 0) {
      self.receivedDataStatus = false;
    }
    self.pipelinePanelCtrl.render();
  }
  onAddField(pipelineId: string): void {
    var self = this;
    var fieldsPosition = [];
    for (var i = 0; i < self.pipelinesList.length; i++) {
      if (self.pipelinesList[i].pipelineRefId === pipelineId) {
        for (var j = 0; j < self.pipelinesList[i].fieldsList.length; j++) {
          fieldsPosition.push(self.pipelinesList[i].fieldsList[j].fieldPosition);
        }
        fieldsPosition.sort(self.fieldsSort);
        var currentFieldPosition = parseInt(fieldsPosition[fieldsPosition.length - 1]) + 1;
        self.pipelinesList[i].fieldsList.push(new FieldModel('', '', '', currentFieldPosition));
        self.render();
      }
    }
  }
  fieldsSort(a: number, b: number): number {
    var d = a - b;
    return d;
  }
  onRemoveField(pipelineId: string, fieldNo: number): void {
    var self = this;
    var fieldsPosition = [];
    for (var i = 0; i < self.pipelinesList.length; i++) {
      if (self.pipelinesList[i].pipelineRefId === pipelineId) {
        for (var j = 0; j < self.pipelinesList[i].fieldsList.length; j++) {
          if (self.pipelinesList[i].fieldsList[j].fieldPosition === fieldNo) {
            self.pipelinesList[i].fieldsList.splice(j, 1);
            if (self.pipelinesList[i].fieldsList.length === 0) {
              self.pipelinesList.splice(i, 1);
              i = i - 1;
            }
            break;
          }
        }
      }
    }
    this.render();
  }
}

/** @ngInject */
export function pipelinePanelEditor($q, uiSegmentSrv) {
  'use strict';
  return {
    restrict: 'E',
    scope: true,
    templateUrl: 'public/plugins/multivaluespanel/editor.html',
    controller: PipelinePanelEditorCtrl,
    controllerAs: 'pipelinePanelEditorCtrl'
  };
}

