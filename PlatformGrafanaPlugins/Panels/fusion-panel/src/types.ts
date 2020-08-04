import { SelectableValue } from '@grafana/data';

type SeriesSize = 'sm' | 'md' | 'lg';

export interface SimpleOptions {
  text: string;
  charttype: string;
  showSeriesCount: boolean;
  seriesCountSize: SeriesSize;
}

export let chartTypes: Array<SelectableValue> = [
  { value: 'column2d', label: 'Column 2D' },
  { value: 'scrollcolumn2d', label: 'Scroll Column 2D' },
  { value: 'column3d', label: 'Column 3D' },
  { value: 'bar2d', label: 'Bar 2D' },
  { value: 'scrollbar2d', label: 'Scroll Bar 2D' },
  { value: 'bar3d', label: 'Bar 3D' },
  { value: 'line', label: 'Line 2D' },
  { value: 'scrollline2d', label: 'Scroll Line 2D' },
  { value: 'area2d', label: 'Area 2D' },
  { value: 'scrollarea2d', label: 'Scroll Area 2D' },
  { value: 'pie2d', label: 'Pie 2D' },
  { value: 'pie3d', label: 'Pie 3D' },
  { value: 'doughnut2d', label: ' Doughnut 2D' },
  { value: 'doughnut3d', label: 'Doughnut 3D' },
  { value: 'pareto2d', label: 'Pareto 2D' },
  { value: 'pareto3d', label: 'Pareto 3D' },
  { value: 'waterfall2d', label: 'Waterfall' },
];
export let scrollCharts: Array<string> = [
  'scrollcolumn2d',
  'scrollbar2d',
  'scrollline2d',
  'scrollarea2d'
]

export let pieAndDoughnutCharts: Array<string> = [
  'pie2d',
  'pie3d',
  'doughnut2d',
  'doughnut3d'
]


export let fontFamily: Array<SelectableValue> = [
  { value: 'Roboto,Helvetica Neue,Arial,sans-serif', label: 'Default' },
  { value: 'Arial', label: 'Arial' },
  { value: 'Roboto', label: 'Roboto' },
  { value: 'Helvetica Neue', label: 'Helvetica Neue' },
  { value: 'sans-serif', label: 'sans-serif' },
  { value: 'Montserrat', label: 'Montserrat' },
  { value: 'Open Sans', label: 'Open Sans' },
  { value: 'Lato', label: 'Lato' },
  { value: 'Tahoma', label: 'Tahoma' },
]

export let FontSizes: Array<SelectableValue> = [

  { value: '5', label: '5' },
  { value: '7', label: '7' },
  { value: '8', label: '8' },
  { value: '9', label: '9' },
  { value: '10', label: '10' },
  { value: '11', label: '11' },
  { value: '12', label: '12' },
  { value: '13', label: '13' },
  { value: '15', label: '15' },
  { value: '17', label: '17' },
  { value: '19', label: '19' },
  { value: '21', label: '21' },
  { value: '23', label: '23' },
  { value: '25', label: '25' },
  { value: '27', label: '27' },
  { value: '28', label: '28' },
  { value: '29', label: '29' },
  { value: '30', label: '30' },
];

export let captionAlignment: Array<SelectableValue> = [
  { value: 'center', label: 'Center' },
  { value: 'left', label: 'Left' },
  { value: 'right', label: 'Right' },

];

export let axisPositions: Array<SelectableValue> = [
  { value: 'right', label: 'Right' },
  { value: 'left', label: 'Left' },
  { value: 'top', label: 'Top' },
  { value: 'bottom', label: 'Bottom' },
];

export let themes: Array<SelectableValue> = [
  { value: 'fusion', label: 'Fusion' },
  { value: 'gammel', label: 'Gammel' },
  { value: 'candy', label: 'candy' },
  { value: 'zune', label: 'Zune' },
  { value: 'ocean', label: 'Ocean' },
  { value: 'carbon', label: 'Carbon' }
];

export let rotateValues: Array<SelectableValue> = [
  { value: '0', label: 'Horizantal' },
  { value: '1', label: 'Vertical' }
];

export let labels: Array<SelectableValue> = [
  { value: 'Auto', label: 'Auto' },
  { value: 'Slant', label: 'Slant' },
  { value: 'Rotate', label: 'Rotate' },
  { value: 'Stagger', label: 'Stagger' }
];

export let valuePositions: Array<SelectableValue> = [
  { value: 'inside', label: 'Inside' },
  { value: 'outside', label: 'Outside' }
];

/**MultiSereis */

export let multiSeries: Array<SelectableValue> = [
  { value: 'mscolumn2d', label: 'Column 2D' },
  { value: 'mscolumn3d', label: 'Column 3D' },
  { value: 'msbar2d', label: 'Bar 2D' },
  { value: 'msbar3d', label: 'Bar 3D' },
  { value: 'msline', label: 'Line 2D' },
  { value: 'msarea', label: 'Area 2D' },
  { value: 'marimekko', label: 'Marimekko' },
  { value: 'overlappedcolumn2d', label: 'Overlapped Column 2D' },
  { value: 'overlappedbar2d', label: 'Overlapped Bar 2D' },
  { value: 'zoomline', label: 'Zoom Line' },
  { value: 'zoomlinedy', label: 'Zoom Line DY' },
  { value: 'stackedcolumn2d', label: 'Stacked Column 2D' },
  { value: 'stackedcolumn3d', label: 'Stacked Column 3D' },
  { value: 'stackedbar2d', label: 'Stacked Bar 2D' },
  { value: 'scrollStackedBar2D', label: 'Scroll Stacked Bar 2D' },
  { value: 'stackedbar3d', label: 'Stacked Bar 3D' },
  { value: 'stackedarea2d', label: 'Stacked Area 2D' },
  { value: 'radar', label: 'Radar' },

]

export let multiSeriesChats = [
   'mscolumn2d',
   'mscolumn3d', 
   'msbar2d', 
   'msbar3d', 
   'msline',
   'msarea',
   'marimekko', 
   'overlappedcolumn2d',
   'overlappedbar2d', 
   'zoomline', 
   'zoomlinedy', 
   'stackedcolumn2d',
   'stackedcolumn3d', 
   'stackedbar2d', 
   'scrollStackedBar2D',
   'stackedbar3d',
   'stackedarea2d', 
   'radar'

];


//COmbination charts
export let combinationCharts: Array<SelectableValue> = [
  { value: 'mscombi2d', label: 'Multi-2D SingleY(Column+Line+Area)' },
  { value: 'mscombi3d', label: 'Multi-3D SingleY(Column+Line+Area)' },
  { value: 'mscombidy2d', label: 'Multi-2D DualY(Column+Line+Area)' },
  { value: 'mscombidy3d', label: 'Multi-3D DualY(Column+Line+Area)' },

  { value: 'mscolumnline3d', label: 'Multi Column 3D+Line-SingleY' },
  { value: 'mscolumn3dlinedy', label: 'Multi Column 3D+Line-DualY' },

  //Scroll Combination Chart

  { value: 'scrollcombi2d', label: 'Scroll Combination 2D' },
  { value: 'scrollcombidy2d', label: 'Scroll Combination 2D DualY' },


  { value: 'stackedarea2dlinedy', label: 'Stacked Area+Line DualY' },
  { value: 'msstackedcolumn2dlinedy', label: 'Multi Stacked Column2D+Line DualY' },

  { value: 'stackedcolumn2dline', label: 'Stacked Column2D+Line SingleY' },
  { value: 'stackedcolumn3dline', label: 'Stacked Column3D +Line SingleY' },
  { value: 'stackedcolumn2dlinedy', label: 'Stacked Column2D +Line Dual Y' },
  { value: 'stackedcolumn3dlinedy', label: 'Stacked Column3D +Line Dual Y' },
]

export let combinationWithLine = ['mscombi2d' ,'mscombi3d','mscombidy2d','mscombidy3d'
,'mscolumnline3d' ,'mscolumn3dlinedy' ,'stackedcolumn2dline'
,'stackedcolumn3dline' ,'stackedcolumn2dlinedy' ,'stackedcolumn3dlinedy'];

//Widget chart

export let widgetCharts: Array<SelectableValue> = [
  { value: 'angulargauge', label: 'Angular Gauge' },
  { value: 'dragnode', label: 'Add Remove Node' }
];