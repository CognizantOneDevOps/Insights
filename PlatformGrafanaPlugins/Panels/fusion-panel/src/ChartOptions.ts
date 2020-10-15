let colorObj = {
  "colorRange": {
    "color": [{
      "minValue": "0",
      "maxValue": "60",
      "code": "#e44a00"
    },
    {
      "minValue": "60",
      "maxValue": "150",
      "code": "#f8bd19"
    },
    {
      "minValue": "150",
      "maxValue": "300",
      "code": "#6baa01"
    }
    ]
  }
}
export interface Settings {
  //Single Series
  isParetoChart: any;
  isPieOrDoughnutChart: any;
  isBarAndColumnChart: any;
  isScrollCombi2D: any;
  isLineChart: any;
  isWaterfallChart: any;

  //Multiseries
  isScrollCharts: any;
  isZoomlineChart: any;

  //Combination
  isArea: any;
  isDualAxis: any;

  //WWidgets
  isGauge: any;
}

export interface ChartOptions extends Settings {
  enableLevel3: boolean
  level2ChartType: any;
  level3ChartType: any;
  level0: any;
  level0Query: any;
  level1: any;
  level1Query: any;
  parentData: any;
  secondLevelData: any;
  drillDownResponse: any;
  linkeddata: any;
  drilldown: any;
  level3drilldown:any;
  drillObj: any;
  level3drillObj:any;
  datasource:any;
  datasourceId:any;
  enableDrillDown:any;
  level2ChartProperties:any;
  level3ChartProperties:any;

  btnEnabled: any;
  atext: any;
  dynamicProps: any;

  staticProps: any;
  SbtnEnabled: any;

  charttype: any;
  connectorThickness: any;
  connectorAlpha: any;
  connectorColor: any;
  sumlabel: any;
  showSumAtEnd: any;
  negativeColor: any;
  positiveColor: any;
  valuePosition: any;
  labelPosition: any;
  minAngleForValue: any;
  centerLabel: any;
  defaultCenterLabel: any;
  enableMultiSlicing: any;
  showPercentValues: any;
  lineDashGap: any;
  lineDashed: any;
  lineDashLen: any;
  lineThickness: any;
  lineAlpha: any;
  lineColor: any;
  showLineValues: any;
  placeValuesInside: any;
  scrollShowButtons: any;
  flatScrollBars: any;
  scrollColor: any;
  scrollheight: any;
  numVisiblePlot: any;

  caption: any;
  captionFont: any;
  subCaption: any;
  captionAlignment: any;
  subcaptionFontBold: any;
  subcaptionFontColor: any;
  subcaptionFontSize: any;
  subcaptionFont: any;
  captionFontColor: any;
  captionFontBold: any;
  captionFontSize: any;

  xAxisName: any;
  xAxisNameBgColor: any;
  xAxisNameBorderColor: any;
  xAxisNameBorderThickness: any;
  xAxisNameBorderRadius: any;
  xAxisNameBorderPadding: any;
  xAxisPosition: any;
  xAxisNameFontBold: any;
  xAxisNameFontSize: any;
  xAxisNameFont: any;
  xAxisNameFontColor: any;
  xAxisValueFontColor: any;
  xAxisMinValue: any;
  xAxisMaxValue: any;

  yAxisName: any;
  yAxisNameFont: any;
  yAxisNameFontSize: any;
  yAxisNameFontBold: any;
  yAxisNameBgColor: any;
  yAxisNameBorderColor: any;
  yAxisNameBorderThickness: any;
  yAxisNameFontColor: any;
  yAxisValueFontColor: any;
  yAxisNameBorderRadius: any;
  yAxisNameBorderPadding: any;
  yAxisPosition: any;
  yAxisMinValue: any;
  yAxisMaxValue: any;

  theme: any;
  toolbarButtonColor: any;
  palettecolors: any;

  valueFontSize: any;
  rotateValues: any;
  valueFontBold: any;
  numberPrefix: any;
  showValues: any;
  toolTipPadding: any;
  toolTipBorderRadius: any;
  toolTipBgAlpha: any;
  toolTipBorderThickness: any;
  toolTipBgColor: any;
  toolTipColor: any;
  showToolTip: any;
  exportEnabled: any;

  labelFontBold: any;
  labelFontSize: any;
  labelFont: any;
  labelDisplay: any;

  crossLineAlpha: any;
  crosslinecolor: any;
  drawCrossLine: any;
  borderAlpha: any;
  borderThickness: any;
  borderColor: any;
  showBorder: any;

  legendItemFontSize: any;
  legendItemFont: any;
  legendCaptionFontSize: any;
  legendItemFontBold: any;
  legendCaption: any;
  legendCaptionBold: any;
  showLegend: any;

  anchorBorderColor: any;
  anchorBgColor: any;
  anchorBorderThickness: any;
  anchorRadius: any;

  zoomPaneBgColor: any;
  numVisibleLabels: any;
  pixelsPerPoint: any;
  pixelsPerLabel: any;
  zoomPaneBgAlpha: any;

  plotFillColor: any;
  snumbersuffix: any;
  plottooltext: any;
  formatnumberscale: any;
  syaxisname: any;

  minorTMThickness: any;
  minorTMHeight: any;
  minorTMAlpha: any;
  minorTMColor: any;
  majorTMThickness: any;
  majorTMHeight: any;
  majorTMAlpha: any;
  majorTMColor: any;
  minorTMNumber: any;
  majorTMNumber: any;
  gaugeFillRatio: any;
  gaugeFillMix: any;

  pivotRadius: any;
  pivotFillMix: any;
  pivotFillRatio: any;
  pivotFillColor: any;

  data: any;

  addRemove: any;


}

export const defaults: ChartOptions = {
  level2ChartType: 'column2d',
  level3ChartType: 'column2d',
  level0: [],
  level0Query: false,
  level1: [],
  level1Query: false,
  parentData: undefined,
  secondLevelData: undefined,
  drillDownResponse: '',
  linkeddata: [],
  drilldown: [],
  level3drilldown:[],
  drillObj: new Object(),
  level3drillObj: new Object(),
  datasource: undefined,
  datasourceId:undefined,
  enableDrillDown:false,
  enableLevel3: false,
  level2ChartProperties:'',
  level3ChartProperties:'',

  /**Advance view */
  atext: undefined,
  dynamicProps: new Object(),
  btnEnabled: 1,

  data: JSON.stringify(colorObj),
  staticProps: '',
  SbtnEnabled: 1,

  /**Single series props */
  charttype: 'column2d',
  isParetoChart: undefined,
  isPieOrDoughnutChart: undefined,
  isBarAndColumnChart: undefined,
  isScrollCombi2D: undefined,
  isLineChart: undefined,
  isWaterfallChart: undefined,
  isScrollCharts: undefined,
  isZoomlineChart: undefined,
  isArea: undefined,
  isDualAxis: undefined,
  isGauge: undefined,

  connectorThickness: '',
  connectorAlpha: '',
  connectorColor: '',
  sumlabel: '',
  showSumAtEnd: 1,
  negativeColor: '',
  positiveColor: '',

  // Doughnut Chart & Pie Chart
  valuePosition: '',
  labelPosition: '',
  minAngleForValue: '',
  centerLabel: '',
  defaultCenterLabel: '',
  enableMultiSlicing: 0,
  showPercentValues: 0,

  //Paretro Chart
  lineDashGap: '',
  lineDashed: '',
  lineDashLen: '',
  lineThickness: '',
  lineAlpha: '',
  lineColor: '',
  showLineValues: '',
  placeValuesInside: '',
  scrollShowButtons: '',
  flatScrollBars: 1,
  scrollColor: '',
  scrollheight: '',
  numVisiblePlot: '',


  //Caption
  caption: '', captionFont: "", captionFontSize: 14, captionFontColor: "", captionAlignment: 'center', captionFontBold: 0,
  //Subcaption
  subCaption: "", subcaptionFont: '', subcaptionFontSize: 12, subcaptionFontColor: '', subcaptionFontBold: 0,

  //X-Axis
  xAxisName: '', xAxisNameFont: '', xAxisNameFontSize: 14, xAxisNameFontBold: 0, xAxisNameBgColor: '',
  xAxisNameBorderColor: '', xAxisNameBorderThickness: '', xAxisNameFontColor: '', xAxisNameBorderRadius: '', xAxisNameBorderPadding: '',
  xAxisValueFontColor: "", xAxisMinValue: "", xAxisMaxValue: "", xAxisPosition: "",
  //Y-Axis
  yAxisName: '', yAxisNameFont: '', yAxisNameFontSize: 14, yAxisNameFontBold: 0, yAxisNameBgColor: '',
  yAxisNameBorderColor: '', yAxisNameBorderThickness: '', yAxisNameFontColor: '', yAxisNameBorderRadius: '', yAxisNameBorderPadding: '',
  yAxisMaxValue: "", yAxisPosition: "", yAxisMinValue: "", yAxisValueFontColor: '',

  //Colors&theme
  theme: 'fusion',
  toolbarButtonColor: '',
  palettecolors: '',

  //Display & cosmetics
  valueFontSize: '',
  rotateValues: 0,
  valueFontBold: 0,
  numberPrefix: '',
  showValues: 1,
  toolTipPadding: '',
  toolTipBorderRadius: '',
  toolTipBgAlpha: '',
  toolTipBorderThickness: '',
  toolTipBgColor: '',
  toolTipColor: '',
  showToolTip: 1,
  exportEnabled: 0,


  //Labels
  labelFontBold: '',
  labelFontSize: '',
  labelFont: '',
  labelDisplay: '',

  crossLineAlpha: '',
  crosslinecolor: '',
  drawCrossLine: '',
  borderAlpha: '',
  borderThickness: '',
  borderColor: '',
  showBorder: undefined,

  //Legends
  legendItemFontSize: '',
  legendItemFont: '',
  legendCaptionFontSize: '',
  legendItemFontBold: 0,
  legendCaption: '',
  legendCaptionBold: 0,
  showLegend: 1,

  anchorBorderColor: '',
  anchorBgColor: '',
  anchorBorderThickness: '',
  anchorRadius: '',

  zoomPaneBgColor: '',
  numVisibleLabels: '',
  pixelsPerPoint: '',
  pixelsPerLabel: '',
  zoomPaneBgAlpha: '',

  plotFillColor: '',
  snumbersuffix: '',
  plottooltext: '',
  formatnumberscale: '',
  syaxisname: '',

  minorTMThickness: '',
  minorTMHeight: '',
  minorTMAlpha: '',
  minorTMColor: '',
  majorTMThickness: '',
  majorTMHeight: '',
  majorTMAlpha: '',
  majorTMColor: '',
  minorTMNumber: '',
  majorTMNumber: '',

  gaugeFillRatio: '',
  gaugeFillMix: '',
  pivotRadius: '',
  pivotFillMix: '',
  pivotFillRatio: '',
  pivotFillColor: '',

  addRemove: '',
}
