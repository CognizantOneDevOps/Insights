import { ChartOptions } from 'ChartOptions';

import { fetchDrillDownData } from './DrillDownUtil';

import { formatMultiSeriesChartType, formatZoomlineTypeChart } from './MultiSeriesParser';
import { formatAnguarGuage } from './WidgetChartsParser';
import { formatChartType_Multi_Series_Column_Line_Area_Single_Dual_Y_Axis, formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis, format_StackedArea_Line_Dual_Y_Axis, formatChartType_MultiSeries_Stacked_Column2D_Line_Dual_Y_Axis } from 'CombinationSeriesParser';
import { getBackendSrv } from '@grafana/runtime';

let charts: Array<string> = ['scrollcolumn2d', 'scrollbar2d', 'scrollline2d', 'scrollarea2d'];
let multiseries: Array<string> = ['mscolumn2d', 'mscolumn3d', 'msbar2d', 'msbar3d', 'msline', 'msarea', 'marimekko', 'overlappedcolumn2d',
  'overlappedbar2d', 'zoomline', 'zoomlinedy', 'stackedcolumn2d', 'stackedcolumn3d', 'stackedbar2d', 'scrollStackedBar2D',
  'stackedbar3d', 'stackedarea2d', 'radar'];

let ColumnLineAreaSDAxis: Array<string> = ['mscombi2d', 'mscombi3d', 'mscombidy2d', 'mscombidy3d'];
let MSStackedColumnSingleDualAxis: Array<string> = ['stackedcolumn2dlinedy', 'stackedcolumn3dlinedy',
  'mscolumnline3d', 'stackedcolumn2dline',
  'stackedcolumn3dline', 'mscolumn3dlinedy'];
let StackedAreaLine2D: string = 'stackedarea2dlinedy';
let MSStackedColumn2DDual: string = 'msstackedcolumn2dlinedy';
let msScrollList: Array<string> = ['scrollcombi2d', 'scrollcombidy2d'];

let annotations = {
  "origw": "400",
  "origh": "300",
  "autoscale": "1",
  "groups": [{
    "items": [{
      "id": "dyn-labelBG",
      "type": "rectangle",
      "radius": "3",
      "x": "$chartEndX-150",
      "y": "$chartStartY-200",
      "tox": "$chartEndX-50",
      "toy": "$chartStartY + 30",
      "color": "#5d62b5",
      "alpha": "70"
    }, {
      "id": "dyn-label",
      "type": "text",
      "fillcolor": "#ffffff",
      "fontsize": "15",
      "x": "$chartEndX - 100",
      "y": "$chartStartY + 15",
      "text": "Back"
    }]
  }]
}



export function fetchChartData(data: any, options: ChartOptions, props: any, config: any) {

  const chartType = options.charttype;
  if (chartType === 'zoomline' || chartType === 'zoomlinedy') {
  data = formatZoomlineTypeChart(props);
  }else 
  if (ColumnLineAreaSDAxis.includes(chartType)) {
    data = formatChartType_Multi_Series_Column_Line_Area_Single_Dual_Y_Axis(props);
  }
  else if (MSStackedColumnSingleDualAxis.includes(chartType)) {
    data = formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis(props);
  }
  else if (chartType === StackedAreaLine2D) {
    data = format_StackedArea_Line_Dual_Y_Axis(props);
  }
  else if (chartType === MSStackedColumn2DDual) {
    data = formatChartType_MultiSeries_Stacked_Column2D_Line_Dual_Y_Axis(props);
  }
  else if (msScrollList.includes(chartType)) {
    data = formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis(props);
  }
  else if (charts.includes(chartType)) {
    data = formatScrollCharts(props);
  }
  else if (multiseries.includes(chartType)) {
    data = formatMultiSeriesChartType(props, options, config);
  } else if (chartType === 'angulargauge') {
    data = formatAnguarGuage(props);
  } else if (chartType === 'dragnode') {
    data = props.options.addRemove;
  }
  else {
    data = formatNonScrollCharts(props, options, config);
  }
  return data;
}

export function dynamicProps(options: ChartOptions, theme: any, data: any, rootID: any, props: any, FusionCharts: any) {
  let chart = {
    bgColor: theme.colors.bg1,
    canvasbgColor: theme.colors.bg1,
    valueFontColor: theme.colors.text,
    labelFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
    legendItemFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
    xAxisFontColor: theme.palette.dark1,
    valueBgColor: (theme.isDark && options.charttype === 'marimekko') ? theme.palette.dark1 : '',
    radarfillcolor: (theme.isDark && options.charttype === 'radar') ? theme.palette.dark1 : '',
    "compactDataMode": "1",
    
    ...options,
    
  };

  /**Rendered as seperate method to be reused when back button is clicked during drilldown usage. */
  parentChartRenderer(FusionCharts, options, rootID, chart, data, props);
}

function parentChartRenderer(FusionCharts: any, options: ChartOptions, rootID: any, chart: any, data: any, props: any) {
  //FusionCharts["debugger"].enable(true);
  FusionCharts.ready(function () {
    let fusioncharts = new FusionCharts({
      type: options.charttype,
      renderAt: rootID,
      dataFormat: 'json',
      containerBackgroundOpacity: '0',
      dataEmptyMessage: 'Please wait, chart is loading the data....',
      dataLoadStartMessage: "Please wait, chart is loading the data....",
      baseChartMessageFont: "Arial",
      baseChartMessageFontSize: "18",
      baseChartMessageColor: "#FC0000",
      dataSource: parentDataSource(chart, data, options),
      events: {
        dataPlotClick: function (ev: any, dataprops: any) {
          if (options.enableDrillDown) {
            fusioncharts.setChartData();
            options.level0Query = true;
            options.parentData = data;
            fetchLevel2Charrt(props, dataprops, options, FusionCharts, rootID, chart, options.level2ChartType);
          } else {
            console.log('Please configure drilldown levels!')
          }
        }
      }
    });
    fusioncharts.resizeTo('100%', '100%');
    fusioncharts.render();
  });
}

function fetchLevel2Charrt(props: any, dataprops: any, options: ChartOptions, FusionCharts: any, rootID: any, chart: any, chartType: any) {
  getBackendSrv().datasourceRequest({ url: 'api/datasources/name/' + props.options.datasource, method: 'GET' }).then((res: any) => {
    let data = res.data;
    if (res.status === 200) {
      props.options.datasourceId = data.id;
      fetchDrillDownData(props, dataprops.categoryLabel, false, chartType,'level2').then(x => {
        childDrillDown(x, props, options, FusionCharts, rootID, chart, chartType);
      });
    }
    else {
      return { status: 'error', message: res.error };
    }
    return res;
  }).catch((err: any) => {
    console.log(err);
  });
}

function childDrillDown(x: any, props: any, options: any, FusionCharts: any, rootID: any, chart: any, chartType) {
  //console.log('x==', x);
  let categories = x.fields.categories;
  let dataset = x.fields.dataset;
  let data = x.fields.data;
  let lineset = x.fields.lineset;
  let fusioncharts = new FusionCharts({
    type: options.level2ChartType,
    renderAt: rootID,
    dataFormat: 'json',
    containerBackgroundOpacity: '0',
    dataEmptyMessage: 'Please wait, chart is loading the data....',
    dataLoadStartMessage: "Please wait, chart is loading the data....",
    baseChartMessageFont: "Arial",
    baseChartMessageFontSize: "18",
    baseChartMessageColor: "#FC0000",
    dataSource: childDataSource(chart, data, dataset, categories, lineset, chartType),
    events: {
      "annotationClick": function (e) {
        let chartOptions = props.options;
        data = chartOptions.parentData;
        parentChartRenderer(FusionCharts, chartOptions, rootID, chart, data, props);
      },
      dataPlotClick: function (ev: any, dataprops: any) {
        if (options.enableLevel3) {
          fusioncharts.setChartData();
          options.secondLevelData = x;
          fetchLevel3Chart(props, dataprops, options, FusionCharts, rootID, chart, options.level3ChartType);
        }else{
          console.log('Please configure level3 !');
        }

      }
    }
  });
  fusioncharts.resizeTo('100%', '100%');
  fusioncharts.render();
}

function fetchLevel3Chart(props: any, dataprops: any, options: any, FusionCharts: any, rootID: any, chart: any, chartType: any) {
  getBackendSrv().datasourceRequest({ url: 'api/datasources/name/' + props.options.datasource, method: 'GET' }).then((res: any) => {
    let data = res.data;
    if (res.status === 200) {
      props.options.datasourceId = data.id;
      fetchDrillDownData(props, dataprops.categoryLabel, false, chartType,'level3').then(x => {
        secondChildDrillDown(x, props, options, FusionCharts, rootID, chart, chartType);
      });
    }
    else {
      return { status: 'error', message: res.error };
    }
    return res;
  }).catch((err: any) => {
    console.log(err);
  });
}

function secondChildDrillDown(x: any, props: any, options: any, FusionCharts: any, rootID: any, chart: any, chartType: any) {
  //console.log('x==', x);
  let categories = x.fields.categories;
  let dataset = x.fields.dataset;
  let data = x.fields.data;
  let lineset = x.fields.lineset;
  let fusioncharts = new FusionCharts({
    type: options.level3ChartType,
    renderAt: rootID,
    dataFormat: 'json',
    containerBackgroundOpacity: '0',
    dataEmptyMessage: 'Please wait, chart is loading the data....',
    dataLoadStartMessage: "Please wait, chart is loading the data....",
    dataLoadStartMessageFontSize: "20",
    dataSource: secondChildDataSource(chart, data, dataset, categories, lineset, chartType),
    events: {
      "annotationClick": function (e) {
        let chartOptions = props.options;
        data = chartOptions.secondLevelData;
        childDrillDown(data, props, options, FusionCharts, rootID, chart,  options.level2ChartType);
      },
      dataPlotClick: function (ev: any, dataprops: any) {
        console.log('clicked');
      }
    }
  });
  fusioncharts.resizeTo('100%', '100%');
  fusioncharts.render();
}

function childDataSource(chart: any, data: any, dataset: any, categories: any, lineset: any, chartType) {
  //console.log(chart.level2ChartProperties);
  //console.log(JSON.parse(chart.level2ChartProperties))
  //console.log(chart)
  let customProps = chart.level2ChartProperties === '' ? chart:JSON.parse(chart.level2ChartProperties);
  //console.log(customProps)
  if (chartType == 'bar2d' || chartType == 'bar3d') {
    return {
      "chart": customProps,
      "data": data,
      "annotations": annotations
    }
  } else if (chartType == 'msstackedcolumn2dlinedy') {
    return {
      "chart": customProps,
      "dataset": dataset,
      "categories": categories,
      "lineset": lineset,
      "annotations": annotations
    }
  }
  return {
    "chart": customProps,
    "data": data,
    "dataset": dataset,
    "categories": categories,
    "lineset": lineset,
    "annotations": annotations
  };
}

function secondChildDataSource(chart: any, data: any, dataset: any, categories: any, lineset: any, chartType) {
  console.log(chart.level3ChartProperties);
  //console.log(JSON.parse(chart.level3ChartProperties))
  //console.log(chart)
  let customProps = chart.level3ChartProperties === '' ? chart:JSON.parse(chart.level3ChartProperties);
  console.log(customProps)
  if (chartType == 'bar2d' || chartType == 'bar3d') {
    return {
      "chart": customProps,
      "data": data,
      "annotations": annotations
    }
  } else if (chartType == 'msstackedcolumn2dlinedy') {
    return {
      "chart": customProps,
      "dataset": dataset,
      "categories": categories,
      "lineset": lineset,
      "annotations": annotations
    }
  }
  return {
    "chart": customProps,
    "data": data,
    "dataset": dataset,
    "categories": categories,
    "lineset": lineset,
    "annotations": annotations
  };
}

function parentDataSource(chart: any, data: any, options: ChartOptions) {
  //console.log(options.charttype)
  if (options.charttype == 'bar2d' || options.charttype == 'bar3d') {
    return {
      "chart": chart,
      "data": data.data
    }
  } else if (options.charttype == 'msstackedcolumn2dlinedy') {
    return {
      "chart": chart,
      "dataset": data.dataset,
      "categories": data.categories,
      "lineset": data.lineset,
    }
  }else if(options.charttype == 'dragnode'){
      return data;
  }else if (options.charttype == 'angulargauge') {
    return {

      "chart": chart,
      "colorRange": chart.staticProps === '' ? '':JSON.parse(chart.staticProps),
      "dials": data.dials
    }
  }
  return {
    chart: chart,
    "data": data.data,
    "dataset": data.dataset,
    "categories": data.categories,
    "lineset": data.lineset,
  };
}

/**
 * This method helps in parsing Neo4j response to fusion single series Non scroll charts
 * @param props
 */
export function formatNonScrollCharts(props: any, options: any, config: any) {
  //console.log(props);
  let data: any = new Array();
  if (props.data.state === 'Done') {
    let array = props.data.series[0].fields;
    let labels = array[0].values.buffer;
    let index = 0;
    let values = array[1].values.buffer;
    labels.forEach((obj: any) => {
      data.push({ "label": obj, "value": values[index] })
      index++;
    });
  }
  return { data: data }
}

/**
 * This method helps in parsing Neo4j response to fusion single series scroll charts
 * @param props
 */
export function formatScrollCharts(props: any) {
  let categories: any = new Array();
  let dataset: any = new Array();
  let category: any = new Array();
  let data: any = undefined;
  if (props.data.state === 'Done') {
    let labelFlag: boolean = false;
    let array = props.data.series[0].fields;
    array.forEach((obj: any) => {
      let array = obj.values.buffer;
      array.forEach((val: any) => {
        /*First element of the array are lables operating using labelFlag*/
        if (!labelFlag) {
          category.push({ "label": val, })
        }
        else {
          data.push({ "value": val })
        }
      })
      if (labelFlag) {
        dataset.push({ "seriesname": obj.name, "data": data })
      }
      labelFlag = true;
      data = new Array();
    });
    categories.push({ "category": category });
  }
  return { data: data, categories: categories, dataset: dataset };
}

