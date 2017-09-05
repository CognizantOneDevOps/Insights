/********************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/


import { InsightsChartsEditorCtrl } from './editor';
import { BasePanelCtrl } from '../insightsCore/BaseModule';
import { BaseCharts } from '../insightsCore/BaseCharts';
import { BaseParser, ParsedResponse } from '../insightsCore/BaseParser';
import { ChartModel, ChartData, ColumnModel, ContainerModel } from '../insightsCore/ChartModel';
import { InsightsChartEditorModel, InsightsChartTargetModel } from './models';

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
      let chartOptions = insightsPanelData['chartOptions'];
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
