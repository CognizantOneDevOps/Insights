# Fusion Single Series Panel

This sections help you to configure sigle series charts

First, install dependencies:
```
yarn install
```

To work with this plugin run:
```
yarn dev
```

or
```
yarn watch
```

This will run linting tools and apply prettier fix.


To build the plugin run:
```
yarn build
```

Response format required from data source.(sample response)

```
columns:Array[2]
0:"Team"
1:"CommittedStoryPoints"
data:Array[4]
0:Object
row:Array[BTG,943]
meta:Array[2]
1:Object
row:Array[Consumer,923]
meta:Array[2]  ...etc
```

Data format required to render single series as below except scroll charts
```
"data": [
{
"label": "Jan",
"value": "420000"
},
{
"label": "Feb",
"value": "810000"
},
...etc
```

Data format required to render single series as below for scroll charts
```
"categories": [
{
"category": [
label:value ,
..etc
]
 “dataset”:[
{
"seriesname": "Current Year",
"data": [
{"value": "25400"},
{"value": "25400"}
Etc..]
}
{
"seriesname": "Current Year",
"data": [
{"value": "25400"},
{"value": "25400"}
Etc..]
}
]
```

Refer below link for the detailed options available  :
```
https://www.fusioncharts.com/dev/chart-guide/list-of-charts#single-series-charts
```

```
------------------------------------------------------------------------------------------
```

# Fusion Panel Multiseries Charts

This is just a stub to show how you can create a basic visualization plugin.

First, install dependencies:
```
yarn install
```

To work with this plugin run:
```
yarn dev
```

or
```
yarn watch
```

This will run linting tools and apply prettier fix.


To build the plugin run:
```
yarn build
```

Data format required for the all multi-series except zoomline  
```
"categories": [
{
"category": [
label :value ,
..etc
]
“dataset”:[
{
"seriesname": "Current Year",
"data": [
{"value": "25400"},
{"value": "25400"}
Etc..]
}
{
"seriesname": "Current Year",
"data": [
{"value": "25400"},
{"value": "25400"}
Etc..]
}
]


```

Data format required for zoomline format.
```
"categories": [
        {
            "category": "Jan 01|Jan 02|Jan 03|Jan 04|Jan 05|Jan 06|Jan 07|Jan 08|Jan 09|
        }
    ],
    "dataset": [
        {
            "seriesname": "harrysfoodmart.com",
      "data": 978|976|955|981|992|964|973|949|985|962|977|955|988|959|985|965|
 },
        
        {
            "seriesname": "harrysfoodmart.com",
      "data": 978|976|955|981|992|964|973|949|985|962|977|955|988|959|985|965|},]

```

Refer below link for the detailed options available  :
```
https://www.fusioncharts.com/dev/chart-guide/list-of-charts#multi-series-charts
```

```
------------------------------------------------------------------------------------------
```
# Fusion Panel Combination Charts

This is just a stub to show how you can create a basic visualization plugin.

First, install dependencies:
```
yarn install
```

To work with this plugin run:
```
yarn dev
```

or
```
yarn watch
```

This will run linting tools and apply prettier fix.


To build the plugin run:
```
yarn build
```

Response format required from data source.(sample response) (This must render 3 columns)

```
columns:Array[4]
0:"Team"
1:"CommittedStoryPoints"
2:"CompletedStoryPoints"
3:"Inprogress"
data:Array[4]
0:Object
row:Array[4]
0:"BTG"
1:943
2:242
3:212
meta:Array[4]
1:Object
row:Array[4]
0:"Consumer"
1:923
2:431
3:299
meta:Array[4]
2:Object
row:Array[4]
0:"FEIT"
1:905
2:528
3:108
meta:Array[4]
3:Object
row:Array[4]
0:"NewsDigital"
1:765
2:598
3:114
```

1.1 Data format required for charts (2D single Y-Axis Combination Charts, 3D single Y-Axis Combination Charts, 2D Dual Y-Axis Combination Chart, 3D Dual Y-Axis Combination Chart)
```
  "categories": 
        {
            "category": [
                {
                    "label": "Jan"
                },
                {
                    "label": "Feb"
                },
               
            ]
        }
    ],
    "dataset": [
        {
            "seriesName": "Actual Revenue",
            "showValues": "1",
            "data": [
                {
                    "value": "16000"
                },
                {
                    "value": "20000"
                },
               
            ]
        },
        {
            "seriesName": "Projected Revenue",
            "renderAs": "line",
            "data": [
                {
                    "value": "15000"
                },
                {
                    "value": "16000"
                },
                
            ]
        },
        {
            "seriesName": "Profit",
            "renderAs": "area",
            "data": [
                {
                    "value": "4000"
                },
                {
                    "value": "5000"
                },
              
            ]
        }
    ]

```

1.2 Data format required for charts (Column 3D+Line Single Y-Axis, Column 3D+Line Dual Y-Axis, Stacked Column 2D line Single Y-Axis. Stacked Column 3D line Single Y-Axis, Stacked Column 2D line Dual Y-Axis, Stacked Column 3D line Dual Y-Axis, Stacked Area 2D line Dual Y-Axis)
```
  "categories": [
        {
            "category": [
                {
                    "label": "Quarter 1"
                },
                {
                    "label": "Quarter 2"
                },
              ]
        }
    ],
    "dataset": [
        {
            "seriesname": "Fixed Cost",
            "data": [
                {
                    "value": "235000"
                },
                {
                    "value": "225100"
                },
             
            ]
        },
        {
            "seriesname": "Variable Cost",
            "data": [
                {
                    "value": "230000"
                },
                {
                    "value": "143000"
                },                
             
            ]
        },
        {
            "seriesname": "Budgeted cost",
            "renderas": "Line",
            "data": [
                {
                    "value": "455000"
                },
                {
                    "value": "334000"
                },
              
            ]
        }
    ]

```

1.3 Data Format required for the chart (Multi-Series Stacked Column 2D +Line Dual Y-Axis).
```
 
"categories": [
        {
            "category": [
                {
                    "label": "Q1"
                },
                {
                    "label": "Q2"
                },               
            ]
        }
    ],
  "dataset": [
        {
            "dataset": [
                {
                    "seriesname": "Processed Food",
                    "data": [
                        {
                            "value": "30"
                        },
                        {
                            "value": "26"
                        },                       
                    ]
                },
                {
                    "seriesname": "Un-Processed Food",
                    "data": [
                        {
                            "value": "21"
                        },
                        {
                            "value": "28"
                        },
                    
                    ]
                }
            ]
        },
        {
            "dataset": [
                {
                    "seriesname": "Electronics",
                    "data": [
                        {
                            "value": "27"
                        },
                        {
                            "value": "25"
                        },                    
                    ]
                },
                {
                    "seriesname": "Apparels",
                    "data": [
                        {
                            "value": "17"
                        },
                        {
                            "value": "15"
                        },                      
                    ]
                }
            ]
        }
    ],
    "lineset": [
        {
            "seriesname": "Profit %",
            "showValues": "0",
            "data": [
                {
                    "value": "14"
                },
                {
                    "value": "16"
                },
            ]
        }
    ]
}

```

Refer below link for the detailed options available  :
```
https://www.fusioncharts.com/dev/chart-guide/list-of-charts#combination-charts
```

```
------------------------------------------------------------------------------------------
```
# Fusion ADD/REMOVE Panel

This is just a stub to show how you can create a basic visualization plugin.

First, install dependencies:
```
yarn install
```

To work with this plugin run:
```
yarn dev
```

or
```
yarn watch
```

This will run linting tools and apply prettier fix.


To build the plugin run:
```
yarn build
```
Themewise Backgrond Color
```
for dark theme : "bgColor":'#212124'
for light theme : "bgColor"='#D8D9DA'

*Note: bgcolor property must be part of chart object
```
Boilerplate code to be used
```
{
   "chart":{
      "caption":"Amazon Prime Video Categorizat",
      "subcaption":"Use the menu on right to add/remove more categories",
      "theme":"fusion",
      "valuefontcolor":"#FFFFFF",
      "yaxismaxvalue":"1000",
      "yaxisminvalue":"0",
      "divlinealpha":"0",
      "bgColor":"#212124"
   },
   "dataset":[
      {
         "data":[
            {
               "id":"01",
               "label":"Home1",
               "x":"50",
               "y":"50",
               "shape":"rectangle",
               "width":"80",
               "height":"40"
            },
            {
               "id":"02",
               "label":"TV Shows",
               "x":"20",
               "y":"500",
               "shape":"rectangle",
               "width":"80",
               "height":"40"
            },
            {
               "id":"02.1",
               "label":"Thriller",
               "x":"2",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"02.2",
               "label":"Drama",
               "x":"12",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"02.3",
               "label":"Comedy",
               "x":"22",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"03",
               "label":"Movies",
               "x":"50",
               "y":"500",
               "shape":"rectangle",
               "width":"80",
               "height":"40"
            },
            {
               "id":"03.1",
               "label":"Drama",
               "x":"35",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"03.2",
               "label":"Action",
               "x":"45",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"03.3",
               "label":"Horror",
               "x":"55",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"03.5",
               "label":"Thriller",
               "x":"65",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"04",
               "label":"Kids",
               "x":"80",
               "y":"500",
               "shape":"rectangle",
               "width":"80",
               "height":"40"
            },
            {
               "id":"04.1",
               "label":"Fantasy",
               "x":"80",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            },
            {
               "id":"04.2",
               "label":"Anime Cartoons",
               "x":"90",
               "y":"100",
               "shape":"rectangle",
               "width":"60",
               "height":"40"
            }
         ]
      }
   ],
   "connectors":[
      {
         "stdthickness":"1.5",
         "connector":[
            {
               "from":"01",
               "to":"03",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"01",
               "to":"04",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"01",
               "to":"02",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"01.02",
               "to":"04",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"01.01",
               "to":"02",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"02",
               "to":"02.1",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"02",
               "to":"02.2",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"02",
               "to":"02.3",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"03",
               "to":"03.1",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"03",
               "to":"03.2",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"03",
               "to":"03.3",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"03",
               "to":"03.4",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"03",
               "to":"03.5",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"03",
               "to":"03.6",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"04",
               "to":"04.1",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            },
            {
               "from":"04",
               "to":"04.2",
               "color":"#FFC533",
               "arrowatstart":"0",
               "arrowatend":"1",
               "alpha":"100"
            }
         ]
      }
   ]
}
```


```
------------------------------------------------------------------------------------------
```