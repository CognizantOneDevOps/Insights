///<reference path="../../../headers/common.d.ts" />


import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import angular from 'angular';
import kbn from 'app/core/utils/kbn';
import {ToolModel,FieldLevelModel,PipelineModel} from './pipelineModel';
import {Transformers} from './transformers';
export class PipelinePanelEditorCtrl {
  pipelinePanel: any;
  pipelinePanelCtrl: any;
  pipelinePanelMetaData: any;
  dataSourceResponse: any;
  toolsMetaData: ToolModel[];
  addColumnSegment: any;
  clonedPipelinePanelMetaData: any;
  pipelineDataModel: PipelineModel[];
  showToolDetails: boolean = false;
  showFieldDetails: boolean = false;
  errMsg: string = '';


  /** @ngInject */

  constructor($scope, private $q, private uiSegmentSrv) {
    //$scope.editor = this;
    this.pipelinePanelCtrl = $scope.ctrl;
    this.pipelinePanel = this.pipelinePanelCtrl.panel;
    this.pipelinePanelMetaData = this.pipelinePanel.pipelinePanelMetaData;
    this.toolsMetaData = this.pipelinePanelMetaData.toolsMetaData;
    if (this.toolsMetaData === undefined) {
      this.toolsMetaData = [];
      this.pipelinePanelMetaData['toolsMetaData'] = this.toolsMetaData;
    }
    this.pipelineDataModel = this.pipelinePanelMetaData.pipelineDataModel;
    if (this.pipelineDataModel === undefined) {
      this.pipelineDataModel = [];
      this.pipelinePanelMetaData['pipelineDataModel'] = this.pipelineDataModel;
    }
    this.dataSourceResponse = this.pipelinePanelCtrl.dataSourceResponse;
    /*if (this.dataSourceResponse.results[0].columns === undefined) {
       this.errMsg = "Cypher Query does not match the required Format";
     }*/
    this.addColumnSegment = this.uiSegmentSrv.newPlusButton();
    this.getPipelineData();
  }

  getPipelineData() {
      let data = this.dataSourceResponse;

    if (data !== undefined){
      //check for  if no of real pipelines <no of saved pipelines
      for (var i = 0;i < this.pipelineDataModel.length;i++){
          let flag = 0;
          for (let target of data.targets){
              if (this.pipelineDataModel[i].pipelineRefId === target.refId){
                  flag = 1;
                  break;
              }
          }
          if (flag === 0){
          this.pipelineDataModel.splice(i,1);
          i = i-1;
            }
      }
      //check for  if no of saved pipelines < no of real pipelines
      for (let target of data.targets){
          let flag = 0;
          for (let pipeline of this.pipelineDataModel){
                if (target.refId === pipeline.pipelineRefId){
                   flag = 1;
                  break;
                }
          }
          if (flag === 0){
          this.pipelineDataModel.push(new PipelineModel(target.refId,'','',this.toolsMetaData));
          }
      }
    }
  }
  getPipelineName() {
     this.render();
  }

  getPipelineColor() {
    this.render();
  }
     //Use render method for refreshing the view.
   render() {
     this.getPipelineData();
     for (let x = 0 ; x < this.pipelineDataModel.length ; x++){
     this.pipelineDataModel[x].toolsList = angular.copy(this.toolsMetaData);}
     this.clonedPipelinePanelMetaData = angular.copy(this.pipelinePanelMetaData);
     if (this.getObjectLength(this.clonedPipelinePanelMetaData) !== 0){
      this.clonedPipelinePanelMetaData = new Transformers().insertValueProperty(this.dataSourceResponse,this.clonedPipelinePanelMetaData);
     }
     this.checkFieldMapping();
     this.pipelinePanelCtrl.render();
  }

  onAssignData(categoryname){
    this.render();
  }
  onAddCategory() {
    this.showToolDetails = true;
    var position = this.toolsMetaData.length + 1;
     this.toolsMetaData.push(new ToolModel(position,'','',[]));
     this.render();
  }

  getObjectLength(obj): number {
     return Object.keys(obj).length;
  }

   onRemoveCategory(category) {;
      let index = _.indexOf(this.toolsMetaData, category);
      this.toolsMetaData.splice(index, 1);
      var count = 1;
      for (var key of this.toolsMetaData){
        key.position = count++;
    }
      this.render();
  }
  getFieldOptions() {
    if (!this.dataSourceResponse) {
      return this.$q.when([]);
  }
    let fieldList = new Transformers().getFields(this.dataSourceResponse);
    let segments = _.map(fieldList, (c) => this.uiSegmentSrv.newSegment({value: c}));
    return this.$q.when(segments);


  }
  addField(indexValue) {
    let fieldLevelModel = new FieldLevelModel('','');
    let field = this.addColumnSegment.value;
    if (field) {
      this.showFieldDetails = true;
      fieldLevelModel.dbName = field;
      this.toolsMetaData[indexValue].fieldList.push(fieldLevelModel);
    }
    let plusButton = this.uiSegmentSrv.newPlusButton();
    this.addColumnSegment.html = plusButton.html;
    this.addColumnSegment.value = plusButton.value;
    this.render();
  }
  removeField(field,indexValue) {
    this.toolsMetaData[indexValue].fieldList =  _.without(this.toolsMetaData[indexValue].fieldList , field);
    this.render();
  }
  checkFieldMapping() {
   if (this.toolsMetaData[0] !== undefined){
      if (this.toolsMetaData[0].fieldList[0] !== undefined) {
        if (this.toolsMetaData[0].fieldList[0].dbName === '') {
          return false;
        }else{
          return true;
        }
      }
    }
  }
}

/** @ngInject */
export function pipelinePanelEditor($q, uiSegmentSrv) {
  'use strict';
  return {
    restrict: 'E',
    scope: true,
    templateUrl: 'public/plugins/pipeline/editor.html',
    controller: PipelinePanelEditorCtrl,
    controllerAs : 'pipelinePanelEditorCtrl'
  };
}
