import { ChartData, ContainerModel, ColumnModel } from './models/ChartModel';
import React, { useEffect, useRef } from 'react'
import { useTheme2 } from "@grafana/ui";
import { googlechartutilities } from './GoogleChartUtilities';


interface Props {
  chartType: string,
  chartOptions: any,
  transformDataInstruction: string;
  joinInstructions: string;
  container: ContainerModel;
  rootId: string;
  data: any;
  isEdit: boolean;
  columnModel: ColumnModel[];
}

export const GoogleCharts = ({
  chartType,
  chartOptions,
  transformDataInstruction,
  joinInstructions,
  container,
  rootId,
  data,
  isEdit,
  columnModel
}: Props) => {
  const theme = useTheme2();
  let google = (window as any).google
  const { buildDataTables, joinDataTables, transformData, innerDimensions, applyTheme } = googlechartutilities(theme, transformDataInstruction, joinInstructions, google, false);
  const contRef = useRef<HTMLDivElement>(null);
  const conatinerViewId = "containerIdDiv_";
  const containerEditId = "containerIdDivEdit_";
  const resizeObserver = new ResizeObserver(entries => {
    drawChart()
    });
  useEffect(() => {
    google.charts.load('46', { 'packages': ['corechart', 'charteditor', 'gantt'] });
    google.charts.setOnLoadCallback(drawChart);
  }, [])  // eslint-disable-line react-hooks/exhaustive-deps
  const drawChart = () => {
    let chartDataArr: ChartData[] = [];
    for (let dt of data.series) {

      let refId: string = dt.refId || "";
      let columnModelArr: ColumnModel[] = [];
      for (let field of dt.fields) {
        let columnModelObj: ColumnModel;
        columnModelObj = columnModel.find(o => o.name === field.name && o.refId === refId)!;
        columnModelArr.push(columnModelObj);
      }
      let chartDataObjIndx = chartDataArr.findIndex(obj => obj.id === refId);
      if (chartDataObjIndx !== -1) {
        chartDataArr[chartDataObjIndx].data = data;
        chartDataArr[chartDataObjIndx].columns = columnModelArr
      } else {
        chartDataArr.push(new ChartData(refId, data, columnModelArr));
      }
    }
    let dataTables = buildDataTables(chartDataArr);
    let dataTb = joinDataTables(dataTables);
    dataTb = transformData(dataTb);
    let chartOptionsObj: any = {};
    if (chartOptions !== undefined && chartOptions !== "") {
      chartOptionsObj = JSON.parse(chartOptions);
    }
    let parentNd: any;
    let container: string;
    if (!isEdit) {
      container = conatinerViewId + rootId;
      parentNd = document.getElementById(container)?.parentNode?.parentNode;
    } else {
      container = containerEditId + rootId;
      parentNd = document.getElementById(container)?.parentNode?.parentNode;
    }
	resizeObserver.observe(parentNd?.parentNode)
    let chartDimensions = innerDimensions(parentNd)
    chartOptionsObj["height"] = chartDimensions.height;
    chartOptionsObj["width"] = chartDimensions.width;
    chartOptionsObj = applyTheme(chartOptionsObj);
    let wrapper = new google.visualization.ChartWrapper({
      'chartType': chartType,
      'dataTable': dataTb,
      'containerId': container,
      'options': chartOptionsObj
    });
    wrapper.draw();
  }

  let returnHtml: any;
  if (!isEdit) {
    returnHtml = <div id={conatinerViewId + rootId} ref={contRef} ></div>;
  } else {
    returnHtml = <div id={containerEditId + rootId} ref={contRef} ></div>;
  }

  return (
    <div>
      {returnHtml}
    </div>
  );
}

