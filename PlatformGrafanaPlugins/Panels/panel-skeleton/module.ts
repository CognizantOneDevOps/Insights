///<reference path="../../../headers/common.d.ts" />

import angular from 'angular';
import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import * as FileExport from 'app/core/utils/file_export';
import {MetricsPanelCtrl} from 'app/plugins/sdk';
import {transformDataToTable} from './transformers';
import {pipelinePanelEditor} from './editor';
import {TableRenderer} from './renderer';

class PipelinePanelCtrl extends MetricsPanelCtrl {
  static templateUrl = 'module.html';

  dataSourceResponse: any;
  panelDefaults = {
    pipelinePanelMetaData: {}
  }; //rowModel, ToolsModel
  /** @ngInject */
  constructor($scope, $injector, private annotationsSrv, private $sanitize) {
    super($scope, $injector);
    _.defaults(this.panel, this.panelDefaults);
    this.events.on('data-received', this.onDataReceived.bind(this));
    this.events.on('data-error', this.onDataError.bind(this));
    this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
    this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
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
    //this.table = transformDataToTable(this.dataSourceResponse, this.panel);
    return super.render(this.dataSourceResponse);
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
