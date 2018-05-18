import { InsightsChartsEditorCtrl } from './editor';
import { BasePanelCtrl } from './insightscore/BaseModule';
import { BaseCharts } from './insightscore/BaseCharts';
import { BaseParser, ParsedResponse } from './insightscore/BaseParser';
import { ChartModel, ChartData, ColumnModel, ContainerModel } from './insightscore/ChartModel';
import { InsightsChartEditorModel, InsightsChartTargetModel } from './models';
import angular from 'angular';

class InsightsChartsPanelCtrl extends BasePanelCtrl {
  /** @ngInject */
  constructor($scope, $injector, private annotationsSrv, private $sanitize, private $window) {
    super($scope, $injector);
    super.registerEditor(InsightsChartsEditorCtrl);
  }
  chartContainerId: string;
  containerHeight: number;
  handlePreRender(dataSourceResponse) {
    if (this.getResponseParser() === undefined) {
      return;
    }
    this.chartContainerId = this['pluginId'] + '_' + super.getPanel()['id'] + '_' + new Date().getTime();
    this.containerHeight = this['height'];
    let parsedResponseArray = this.getResponseParser().parseResponse(dataSourceResponse);
    let insightsPanelData = this.getInsightsPanelData();
    let insightsChartEditorModel: InsightsChartEditorModel = this.insightsPanelData['insightsChartEditorModel'];
    if (insightsChartEditorModel && parsedResponseArray.length > 0) {
      let containerModel = new ContainerModel(this.chartContainerId, this.containerHeight);
      let chartType = insightsPanelData['chartType'];
      let chartOptions = angular.copy(insightsPanelData['chartOptions'], {})
      let chartDataArray: ChartData[] = [];
      for (let parsedResponse of parsedResponseArray) {
        for (let target of insightsChartEditorModel.targets) {
          if (parsedResponse.target === target.id) {
            if ('Elasticsearch' === this.getDatasourceType()) {
              chartDataArray.push(new ChartData(parsedResponse.target, parsedResponse.data, parsedResponse.columns));
            } else {
              chartDataArray.push(new ChartData(parsedResponse.target, parsedResponse.data, target.columnModel));
            }
            break;
          }
        }
      }
      let chartModel = new ChartModel(chartType, chartOptions, chartDataArray, containerModel,
        insightsChartEditorModel.transformInstrctions, insightsChartEditorModel.joinInstructions);
      let chart = new BaseCharts(chartModel);
      chart.renderChart(false);
    }
  }
}

export {
  InsightsChartsPanelCtrl,
  InsightsChartsPanelCtrl as PanelCtrl
};
