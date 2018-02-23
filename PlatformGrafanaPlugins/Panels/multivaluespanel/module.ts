///<reference path="../../../headers/common.d.ts" />

import angular from 'angular';
import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import * as FileExport from 'app/core/utils/file_export';
import {MetricsPanelCtrl} from 'app/plugins/sdk';
import {neo4jDataParser} from './transformers';
import {pipelinePanelEditor} from './editor';
import {TableRenderer} from './renderer';

class PipelinePanelCtrl extends MetricsPanelCtrl {
  static templateUrl = 'module.html';

  dataSourceResponse: any;
  pipelinePanelMetaData: any = {};
  ds: any;
  cloneUiArray: any = [];
  panelDefaults = {
    pipelinePanelMetaData: this.pipelinePanelMetaData
  }; //rowModel, ToolsModel
  /** @ngInject */
  constructor($scope, $injector, private annotationsSrv, private $sanitize) {
    super($scope, $injector);
    _.defaults(this.panel, this.panelDefaults);
    this.pipelinePanelMetaData = this.panel.pipelinePanelMetaData;
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
    actions.push({ text: 'Export CSV', click: 'ctrl.exportCsv()' });
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
  setCloneUiData() {
    var self = this;
    let data = self.dataSourceResponse;
    console.log(data);
    if (data !== undefined && data.length !== 0 && data.results !== undefined) {
      self.cloneUiArray = [];
      if (data.targets.length === data.results.length) {

        for (var i = 0; i < self.pipelinePanelMetaData.pipelinesList.length; i++) {
          for (var j = 0; j < data.targets.length; j++) {
            if (self.pipelinePanelMetaData.pipelinesList[i].pipelineRefId === data.targets[j].refId) {
              for (var x = 0; x < self.pipelinePanelMetaData.pipelinesList[i].fieldsList.length; x++) {
                for (var y = 0; y < data.results[j].columns.length; y++) {
                  if (self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldName === data.results[j].columns[y]) {
                    if (data.results[j].data[0] !== undefined) {
                      self.cloneUiArray.push(
                        {
                          fieldMapName: self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldMapName,
                          fieldValue: data.results[j].data[0].row[y],
                          fieldColor: 'background-color:' + self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldColor
                        }
                      );
                    } else {
                      self.cloneUiArray.push(
                        {
                          fieldMapName: self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldMapName,
                          fieldValue: 0,
                          fieldColor: 'background-color:' + self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldColor
                        }
                      );
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      self.cloneUiArray = [];
    }
  }


  render() {
    this.setCloneUiData();
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

    ctrl.events.on('render', function (renderData) {
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
