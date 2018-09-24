//<reference path="../../../headers/common.d.ts" />

import angular from 'angular';
import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import * as FileExport from 'app/core/utils/file_export';
import {MetricsPanelCtrl} from 'app/plugins/sdk';
import {pipelinePanelEditor,PipelinePanelEditorCtrl} from './editor';
import {TableRenderer} from './renderer';
import {Transformers} from './transformers';

class PipelinePanelCtrl extends MetricsPanelCtrl {
  static templateUrl = 'module.html';
//  toolsMetaData: ToolModel[];
  dataSourceResponse: any;
  pipelinePanelMetaData: any = {};
  panelDefaults: any = {
    pipelinePanelMetaData : this.pipelinePanelMetaData
  };
      labelIcons = {
        'clear': 'public/plugins/pipeline/img/ic_clear_24px.svg',
        'left': 'public/plugins/pipeline/img/ic_keyboard_arrow_left_24px.svg',
        'right': 'public/plugins/pipeline/img/ic_keyboard_arrow_right_24px.svg'
    };
  pipelineCloneData: any;
  screenWidth: number;
  toolsCompWidth: number;
  showScroll: boolean = false;
  start: number;
  end: number;
  count: number = 1;
  functionBlockFixCount: number = 3;
  totalHeight: number;
  showNoData: number = 0;

  //rowModel, ToolsModel
  /** @ngInject */
  constructor($scope, $injector, private annotationsSrv, private $sanitize, private $window) {
    super($scope, $injector);
    _.defaults(this.panel, this.panelDefaults);
    this.pipelinePanelMetaData = this.panel.pipelinePanelMetaData;
    this.events.on('data-received', this.onDataReceived.bind(this));
    this.events.on('data-error', this.onDataError.bind(this));
    this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
    this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
    this.screenWidth = $window.innerWidth;
  }

  onInitEditMode() {
    this.addEditorTab('Options', pipelinePanelEditor, 2);
  }

  onInitPanelActions(actions) {
    actions.push({text: 'Export CSV', click: 'ctrl.exportCsv()'});
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
    this.pipelineCloneData = angular.copy(this.pipelinePanelMetaData);
    this.pipelineCloneData = new Transformers().insertValueProperty(this.dataSourceResponse,this.pipelineCloneData);
    this.widthComputation();
    return super.render(this.dataSourceResponse);
  }

  widthComputation(): void{
       this.showScroll = false;
       var functionalBlockLength = 0;
       if (this.pipelineCloneData["toolsMetaData"] !== undefined){
          functionalBlockLength = this.pipelineCloneData["toolsMetaData"].length;
       }
       this.totalHeight = 70*functionalBlockLength;
       this.start = this.count;
       this.end = functionalBlockLength;
       var pipelineTotalWdth = this.screenWidth-245;
       var whiteframeCompWdth = 330; //fixed width with margin
       this.toolsCompWidth = whiteframeCompWdth * functionalBlockLength;
       var totalWdt = this.toolsCompWidth+ 245;
       if ( totalWdt >= this.screenWidth){
           this.showScroll = true;
       }
       if (this.showScroll){
         this.start = this.count;
         this.end = functionalBlockLength-(functionalBlockLength-this.functionBlockFixCount);
       }
  }

  nextFuncBlock(start,end): void {
    var functionalBlockLength = this.pipelineCloneData["toolsMetaData"].length;
    if (this.showScroll && end !== functionalBlockLength){
      this.start = start + 1;
      this.end = end+1;
    }
  }

  prevFuncBlock(start,end): void {
    var functionalBlockLength = this.pipelineCloneData["toolsMetaData"].length;
    if (this.showScroll && start !== 1 && end !== this.functionBlockFixCount){
      this.start = start-1;
      this.end = end-1;
    }
  }

  //Essential
  link(scope, elem, attrs, ctrl) {
    var data;
    var panel = ctrl.panel;
    var pageCount = 0;
    var formaters = [];
    function renderPanel() {
    }

    ctrl.events.on('render', function(renderData) {
      data = renderData || data;
      if (data) {
        renderPanel();
      }
      ctrl.renderingCompleted();
    });
  }
}

export {
  PipelinePanelCtrl,
  PipelinePanelCtrl as PanelCtrl
};
