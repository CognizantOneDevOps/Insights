///<reference path="../../../../headers/common.d.ts" />
import { BaseParser } from './BaseParser';

export class BaseEditorCtrl {
  panel: any;
  panelCtrl: any;
  insightsPanelData: any;
  dataSourceResponse: any;
  responseParser: BaseParser;
  /** @ngInject */
  constructor($scope, private $q, private uiSegmentSrv) {
    this.panelCtrl = $scope.ctrl;
    this.panel = this.panelCtrl.panel;
    this.insightsPanelData = this.panel.insightsPanelData;
    this.dataSourceResponse = this.panelCtrl.dataSourceResponse;
    this.responseParser = this.panelCtrl.responseParser;
  }

  /** @ngInject */
  static loadEditorCtrl($q, uiSegmentSrv) {
    'use strict';
    return {
      restrict: 'E',
      scope: true,
      templateUrl: this['templateUrl'],
      controller: this['controller'],
      controllerAs: this['controllerAs']
    };
  }

  protected getPanel() {
    return this.panel;
  }

  protected getDataSourceRespone() {
    return this.dataSourceResponse;
  }

  protected getInsightsPanelData() {
    return this.insightsPanelData;
  }

  protected getResponseParser() {
    return this.responseParser;
  }

  protected getDatasourceType(){
    return this.panelCtrl.getDatasourceType();
  }

  render() {
    this.panelCtrl.render();
  }
}
