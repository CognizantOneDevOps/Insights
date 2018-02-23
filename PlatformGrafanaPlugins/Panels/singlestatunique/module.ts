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
  validQuery: boolean = false;
  finalStat: any = 0;
  uniqueSingleStatPanelMetaData = {
    inputProperties: {
      'color': 'black',
      'font-size': '30px'
    }
  };
  panelDefaults = {
    uniqueSingleStatPanelMetaData: this.uniqueSingleStatPanelMetaData
  }; //rowModel, ToolsModel
  neo4jHandledData: any;
  fieldsStatArray: any;
  neo4jDataStatus: boolean = false;
  /** @ngInject */
  constructor($scope, $injector, private annotationsSrv, private $sanitize) {
    super($scope, $injector);
    _.defaults(this.panel, this.panelDefaults);
    this.uniqueSingleStatPanelMetaData = this.panel.uniqueSingleStatPanelMetaData;
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
    var self = this;
    self.finalStat = 0;
     self.neo4jDataStatus = false;
    self.dataSourceResponse = dataList;

    if (self.datasource !== undefined &&
      self.datasource.constructor.name !== undefined &&
      self.datasource.constructor.name === 'ElasticDatasource') {
      var seriesLength = this.dataSourceResponse;
      self.neo4jDataStatus = false;
      if (typeof seriesLength === 'object' && seriesLength.constructor.name === 'Array') {
        self.validQuery = true;
        self.finalStat = seriesLength.length;
      } else {
        self.validQuery = false;
        self.finalStat = 'Invalid query';
      }
    } else if (self.datasource !== undefined &&
      self.datasource.constructor.name !== undefined &&
      self.datasource.constructor.name === 'Neo4jDatasource') {
      self.validQuery = true;
      self.neo4jHandledData = neo4jDataParser(self.dataSourceResponse);
      if (self.neo4jHandledData['data'] !== undefined) {
        self.fieldsStatArray = Object.keys(self.neo4jHandledData['data'][0]);
        self.neo4jDataStatus = true;
        self.finalStat = 'Please Select Field';
      }
    } else {
      self.validQuery = false;
      self.finalStat = 'no datasource';
    }
    this.render();

  }
  setSelectedFieldUi() {
    var self = this;
    if (self.neo4jHandledData['data'] !== undefined) {
      var neoDataObject = self.neo4jHandledData['data'][0];
      for (var key in neoDataObject) {
        if (neoDataObject.hasOwnProperty(key)) {
          if (key === self.uniqueSingleStatPanelMetaData['selectedfield']) {
            self.finalStat = neoDataObject[key];
          }
        }
      }
    }
  }

  render() {
    var self = this;
    if (self.neo4jDataStatus === true) {
      self.setSelectedFieldUi();
    }
    return super.render(self.dataSourceResponse);
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
