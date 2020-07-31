///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import angular from 'angular';
import _ from 'lodash';
import { BaseEditorCtrl } from './insightscore/BaseEditor';
import { BaseCharts } from './insightscore/BaseCharts';
import { BaseParser, ParsedResponse } from './insightscore/BaseParser';
import { ChartModel, ChartData, ColumnModel, ContainerModel } from './insightscore/ChartModel';
import { InsightsChartEditorModel, InsightsChartTargetModel } from './models';

export class InsightsChartsEditorCtrl extends BaseEditorCtrl {
  supportedDataTypes = BaseCharts.supportedDataTypes;
  chartEditorWarpper: any;
  insightsPanelData = super.getInsightsPanelData();
  chartVisible: boolean = false;
  chartModel: ChartModel;
  insightsChartEditorModel: InsightsChartEditorModel = this.insightsPanelData['insightsChartEditorModel'];
  //chipsModel: any = {};
  columnCache: any = {};
  parsedResponseArray: ParsedResponse[];
  /** @ngInject */
  constructor($scope, $q, uiSegmentSrv) {
    super($scope, $q, uiSegmentSrv);
    this.process();
    this.render();
  }

  process() {
    //this.responseParser = new BaseParser('neo4j');
    if (this.getResponseParser()) {
      this.loadDataAndColumns();
      this.loadChartEditor();
    } else {
      let self = this;
      setTimeout(function () {
        self.process();
      }, 50);
    }
  }


  loadDataAndColumns() {
    //this.parsedResponseArray = new BaseParser().parseNeo4jResponseArray(super.getDataSourceRespone());
    this.parsedResponseArray = this.getResponseParser().parseResponse(super.getDataSourceRespone());
    if (this.parsedResponseArray.length > 0) {
      let insightsChartTargetModelArray: InsightsChartTargetModel[] = [];
      for (let responseData of this.parsedResponseArray) {
        let preConfiguredColumns: ColumnModel[] = [];
        if (this.insightsChartEditorModel) {
          for (let target of this.insightsChartEditorModel.targets) {
            if (target.id === responseData.target) {
              preConfiguredColumns = target.columnModel;
              break;
            }
          }
        }
        let columnModelArray: ColumnModel[] = [];
        for (let column of responseData.columns) {
          for (let preConfiguredColumn of preConfiguredColumns) {
            if (column.name === preConfiguredColumn.name) {
              column.type = preConfiguredColumn.type;
              break;
            }
          }
          columnModelArray.push(column);
        }
        insightsChartTargetModelArray.push(new InsightsChartTargetModel(responseData.target, columnModelArray));
      }
      if (this.insightsChartEditorModel === undefined) {
        this.insightsChartEditorModel = new InsightsChartEditorModel(insightsChartTargetModelArray, undefined, undefined, undefined);
      } else {
        this.insightsChartEditorModel.targets = insightsChartTargetModelArray;
      }
    }
  }

  loadChartEditor() {
    if(this.parsedResponseArray.length == 0){
      this.parsedResponseArray = this.getResponseParser().parseResponse(super.getDataSourceRespone());
    }
    if(this.parsedResponseArray){
    if (this.parsedResponseArray.length > 0) {
      let chartDataArray: ChartData[] = [];
      for (let parsedResponse of this.parsedResponseArray) {
        let columnModel: ColumnModel[] = [];
        for (let target of this.insightsChartEditorModel.targets) {
          if (target.id === parsedResponse.target) {
            let availabelColumns = target.columnModel;
            let columnTypesConfigured = true;
            for (let column of availabelColumns) {
              if (column.type === undefined) {
                columnTypesConfigured = false;
                break;
              }
            }
            if (columnTypesConfigured) {
              columnModel = target.columnModel;
            }
            break;
          }
        }
        if (columnModel.length > 0) {
          chartDataArray.push(new ChartData(parsedResponse.target, parsedResponse.data, columnModel));
        }
      }
      if (chartDataArray.length > 0) {
        let insightsPanelData = this.getInsightsPanelData();
        let containerModel = new ContainerModel('chartEditorContainer', 600);
        let chartType = insightsPanelData['chartType'];
        let chartOptions = insightsPanelData['chartOptions'];
        if (this.insightsChartEditorModel.chartOptions) {
          let additionalOptions = this.parseAdditionalChartOptions();
          chartOptions = _.defaults(additionalOptions, chartOptions);
          if (insightsPanelData['chartOptions'] === undefined) {
            insightsPanelData['chartOptions'] = chartOptions;
          }
        }
        let chartModel = new ChartModel(chartType, chartOptions, chartDataArray, containerModel,
          this.insightsChartEditorModel.transformInstrctions, this.insightsChartEditorModel.joinInstructions);
        let chart = new BaseCharts(chartModel);
        chart.renderChart(true);
        this.chartVisible = true;
        this.chartEditorWarpper = chart.getChartEditorWarpper();
      }
    }
  }
  }

  parseAdditionalChartOptions() {
    try {
      return JSON.parse(this.insightsChartEditorModel.chartOptions);
    } catch (error) {
      return {};
    }
  }

  saveChartOptions() {
    if (this.chartEditorWarpper['chartEditor']) {
      let chartEditor = this.chartEditorWarpper['chartEditor'].getChartWrapper();
      this.insightsPanelData['chartType'] = chartEditor.getChartType();
      let chartOptions = chartEditor.getOptions();
      if (this.insightsChartEditorModel.chartOptions) {
        let additionalOptions = this.parseAdditionalChartOptions();
        chartOptions = _.defaults(additionalOptions, chartOptions);
      }
      this.insightsPanelData['chartOptions'] = chartOptions;
      if ('Elasticsearch' === this.getDatasourceType()) {
        //this.insightsChartEditorModel.targets = [new InsightsChartTargetModel('Elasticsearch', [])];
      }
      this.insightsPanelData['insightsChartEditorModel'] = this.insightsChartEditorModel;
      this.render();
    }
  }
}

