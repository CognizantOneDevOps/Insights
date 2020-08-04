import { ChartOptions } from 'ChartOptions';

import { formatMultiSeriesChartType } from './MultiSeriesParser';
import { formatAnguarGuage } from './WidgetChartsParser';
import { formatChartType_Multi_Series_Column_Line_Area_Single_Dual_Y_Axis, formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis, format_StackedArea_Line_Dual_Y_Axis, formatChartType_MultiSeries_Stacked_Column2D_Line_Dual_Y_Axis } from 'CombinationSeriesParser';

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
let msScrollList:Array<string> =['scrollcombi2d','scrollcombidy2d'];

export function fetchChartData(data: any, options: ChartOptions,props:any) {
  const chartType = options.charttype;
  //if (this.props.options.charttype === 'zoomline' || this.props.options.charttype === 'zoomlinedy') {
    //data = formatZoomlineTypeChart(this.props);
    //}else 
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
    data = formatMultiSeriesChartType(props);
  }else if(chartType === 'angulargauge'){
    data = formatAnguarGuage(props);
  }else if(chartType === 'dragnode'){
    data = props.options.data;
  }
  else {
    data = formatNonScrollCharts(props);
  }
  return data;
}

export function dynamicProps(options: ChartOptions, theme: any, data: any, rootID:any) {
  if (options.charttype == 'bar2d' || options.charttype == 'bar3d') {
    return {
      type: options.charttype,
      renderAt: rootID,
      dataFormat: 'json',
      containerBackgroundOpacity: '0',
      dataEmptyMessage: 'Retreving data and changing chart propertes.. please wait..',
      dataLoadStartMessage: "Please wait, chart is loading the data....",
      dataLoadStartMessageFontSize: "14",
      dataSource: {
        // Chart Configuration
        chart: {
          bgColor: theme.colors.bg1,
          canvasbgColor:  theme.colors.bg1,
          valueFontColor : theme.colors.text,
          labelFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
          legendItemFontColor: theme.isDark ? theme.palette.white :theme.palette.dark1,
          xAxisFontColor:theme.palette.dark1,
          valueBgColor: (theme.isDark && options.charttype === 'marimekko') ? theme.palette.dark1 : '',
          radarfillcolor:(theme.isDark && options.charttype === 'radar') ? theme.palette.dark1 : '',
          ...options
        },
        // Chart Data
        "data": data.data
      }
    };
  } else if(options.charttype == 'msstackedcolumn2dlinedy'){
    return {
      type: options.charttype,
      renderAt: rootID,
      dataFormat: 'json',
      containerBackgroundOpacity: '0',
      dataEmptyMessage: 'Retreving data and changing chart propertes.. please wait..',
      dataLoadStartMessage: "Please wait, chart is loading the data....",
      dataLoadStartMessageFontSize: "14",
      dataSource: {
        // Chart Configuration
        chart: {
          bgColor: theme.colors.bg1,
          canvasbgColor:  theme.colors.bg1,
          valueFontColor : theme.colors.text,
          labelFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
          legendItemFontColor: theme.isDark ? theme.palette.white :theme.palette.dark1,
          xAxisFontColor:theme.palette.dark1,
          valueBgColor: (theme.isDark && options.charttype === 'marimekko') ? theme.palette.dark1 : '',
          radarfillcolor:(theme.isDark && options.charttype === 'radar') ? theme.palette.dark1 : '',
          ...options
        },
        // Chart Data
        "dataset": data.dataset,
        "categories": data.categories,
        "lineset": data.lineset
      }
    };
  }else if(options.charttype == 'angulargauge'){
    return {
      type: options.charttype,
      renderAt: rootID,
      dataFormat: 'json',
      containerBackgroundOpacity: '0',
      dataEmptyMessage: 'Retreving data and changing chart propertes.. please wait..',
      dataLoadStartMessage: "Please wait, chart is loading the data....",
      dataLoadStartMessageFontSize: "14",
      dataSource: {
        // Chart Configuration
        chart: {
          bgColor: theme.colors.bg1,
          canvasbgColor:  theme.colors.bg1,
          valueFontColor : theme.colors.text,
          labelFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
          legendItemFontColor: theme.isDark ? theme.palette.white :theme.palette.dark1,
          xAxisFontColor:theme.palette.dark1,
          valueBgColor: (theme.isDark && options.charttype === 'marimekko') ? theme.palette.dark1 : '',
          radarfillcolor:(theme.isDark && options.charttype === 'radar') ? theme.palette.dark1 : '',
          ...options
        },
        ...data,
        "dials": data.dials
      }
    };
  }else if(options.charttype === 'dragnode'){
    return {
      type: options.charttype,
      renderAt: rootID,
      dataFormat: 'json',
      containerBackgroundOpacity: '0',
      dataEmptyMessage: 'Retreving data and changing chart propertes.. please wait..',
      dataLoadStartMessage: "Please wait, chart is loading the data....",
      dataLoadStartMessageFontSize: "14",
      dataSource: {
        // Chart Configuration
        chart: {
          bgColor: theme.colors.bg1,
          canvasbgColor:  theme.colors.bg1,
          valueFontColor : theme.colors.text,
          labelFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
          legendItemFontColor: theme.isDark ? theme.palette.white :theme.palette.dark1,
          xAxisFontColor:theme.palette.dark1,
          valueBgColor: (theme.isDark && options.charttype === 'marimekko') ? theme.palette.dark1 : '',
          radarfillcolor:(theme.isDark && options.charttype === 'radar') ? theme.palette.dark1 : '',
          ...options
        },
        ...data
      }
    };
  }
  else {
    return {
      type: options.charttype,
      renderAt: rootID,
      dataFormat: 'json',
      containerBackgroundOpacity: '0',
      dataEmptyMessage: 'Retreving data and changing chart propertes.. please wait..',
      dataLoadStartMessage: "Please wait, chart is loading the data....",
      dataLoadStartMessageFontSize: "14",
      dataSource: {
        // Chart Configuration
        chart: {
          bgColor: theme.colors.bg1,
          canvasbgColor:  theme.colors.bg1,
          valueFontColor : theme.colors.text,
          labelFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
          legendItemFontColor: theme.isDark ? theme.palette.white :theme.palette.dark1,
          xAxisFontColor:theme.palette.dark1,
          valueBgColor: (theme.isDark && options.charttype === 'marimekko') ? theme.palette.dark1 : '',
          radarfillcolor:(theme.isDark && options.charttype === 'radar') ? theme.palette.dark1 : '',
          ...options
        },
        // Chart Data
        "data": data.data,
        "dataset": data.dataset,
        "categories": data.categories,
        "lineset": data.lineset,
      }
    };
  }
}


/**
 * This method helps in parsing Neo4j response to fusion single series Non scroll charts
 * @param props
 */
export function formatNonScrollCharts(props: any) {
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

