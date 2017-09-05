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

///<reference path="../../../headers/common.d.ts" />

import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import angular from 'angular';
import kbn from 'app/core/utils/kbn';

export class InsightsVizOptionsRenderer {
    loadVizOptions() {
        return this.vizOptions;
    }

    private vizOptions: any = {
        "discreteBarChart": {
            defaultOptions: {
                chart: {
                    type: 'discreteBarChart',
                    height: 500,
                    margin: {
                        top: 20,
                        right: 20,
                        bottom: 60,
                        left: 55
                    },
                    x: function (d) { return d.label; },
                    y: function (d) { return d.value; },
                    showValues: true,
                    valueFormat: function (d) {
                        return d3.format(',.2f')(d);
                    },
                    transitionDuration: 500,
                    xAxis: {
                        axisLabel: 'X Axis'
                    },
                    yAxis: {
                        axisLabel: 'Y Axis',
                        axisLabelDistance: 30
                    }
                }
            },
            dataFormat: [
                {
                    "fieldName": "label",
                    "info": "The label name which should apear on bar"
                },
                {
                    "fieldName": "value",
                    "info": "The value of the bar"
                }
            ],
            dataFormatMapper: function (records, fieldMapping) {
                let values = [];
                let data = [
                    {
                        "key": "Cumulative Dashboard",
                        "values": values
                    }
                ];
                var labelField = fieldMapping['label'];
                var valueField = fieldMapping['value'];
                if (labelField && valueField) {
                    for (let record of records) {
                        let dataPoint = {};
                        dataPoint["label"] = record[labelField];
                        dataPoint["value"] = Number(record[valueField]);
                        values.push(dataPoint);
                    }
                }
                return data;
            }
        },
        "historicalBarChart": {
            defaultOptions: {
                chart: {
                    type: 'historicalBarChart',
                    height: 450,
                    margin: {
                        top: 20,
                        right: 20,
                        bottom: 65,
                        left: 50
                    },
                    x: function (d) { return d[0]; },
                    y: function (d) { return d[1]; },
                    showValues: true,
                    valueFormat: function (d) {
                        return d3.format(',.1f')(d);
                    },
                    duration: 10,
                    xAxis: {
                        axisLabel: 'X Axis',
                        tickFormat: function (d) {
                            return d3.time.format('%x')(new Date(d));
                        },
                        rotateLabels: 30,
                        showMaxMin: false
                    },
                    yAxis: {
                        axisLabel: 'Y Axis',
                        axisLabelDistance: -10,
                        tickFormat: function (d) {
                            return d3.format(',.1f')(d);
                        }
                    },
                    tooltip: {
                        keyFormatter: function (d) {
                            return d3.time.format('%x')(new Date(d));
                        }
                    },
                    zoom: {
                        enabled: true,
                        scaleExtent: [1, 10],
                        useFixedDomain: false,
                        useNiceScale: false,
                        horizontalOff: false,
                        verticalOff: true,
                        unzoomEventType: 'dblclick.zoom'
                    }
                }
            },
            dataFormat: [
                {
                    "fieldName": "timestamp",
                    "info": "The label name which should apear on bar"
                },
                {
                    "fieldName": "value",
                    "info": "The value of the bar"
                }
            ],
            dataFormatMapper: function (records, fieldMapping) {
                let values = [];
                let data = [{
                    "key": "Cumulative Dashboard",
                    "bar": true,
                    "values": values
                }];
                var timestampField = fieldMapping['timestamp'];
                var valueField = fieldMapping['value'];
                if (timestampField && valueField) {
                    for (let record of records) {
                        let dataPoint = [];
                        dataPoint.push(Number(record[timestampField]));
                        dataPoint.push(record[valueField]);
                        values.push(dataPoint);
                    }
                }
                return data;
            }
        },
        "stackedAreaChart": {
            defaultOptions: {
                chart: {
                    type: 'stackedAreaChart',
                    height: 450,
                    margin: {
                        top: 20,
                        right: 20,
                        bottom: 30,
                        left: 40
                    },
                    x: function (d) { return d[0]; },
                    y: function (d) { return d[1]; },
                    useVoronoi: false,
                    clipEdge: true,
                    duration: 100,
                    useInteractiveGuideline: true,
                    xAxis: {
                        showMaxMin: false,
                        tickFormat: function (d) {
                            return d3.time.format('%x')(new Date(d));
                        }
                    },
                    yAxis: {
                        tickFormat: function (d) {
                            return d3.format(',.2f')(d);
                        }
                    },
                    zoom: {
                        enabled: true,
                        scaleExtent: [1, 10],
                        useFixedDomain: false,
                        useNiceScale: false,
                        horizontalOff: false,
                        verticalOff: true,
                        unzoomEventType: 'dblclick.zoom'
                    }
                }
            },
            dataFormat: [
                {
                    "fieldName": "key",
                    "info": "The label name which should apear on bar"
                },
                {
                    "fieldName": "timestamp",
                    "info": "The label name which should apear on bar"
                },
                {
                    "fieldName": "value",
                    "info": "The value of the bar"
                }
            ],
            dataFormatMapper: function (records, fieldMapping) {
                let keyField = fieldMapping['key'];
                let timestampField = fieldMapping['timestamp'];
                let valueField = fieldMapping['value'];
                let keyDataMap = {};
                if (keyField && timestampField && valueField) {
                    let uniqueKeyValues = _.countBy(records, keyField);
                    for (let keyValue in uniqueKeyValues) {
                        keyDataMap[keyValue] = [];
                    }
                    for (let record of records) {
                        let key = record[keyField];
                        let values = keyDataMap[key];
                        let dataPoint = [];
                        dataPoint.push(Number(record[timestampField]));
                        dataPoint.push(Number(record[valueField]));
                        values.push(dataPoint);
                        let zeroValueDataPoint = [];
                        zeroValueDataPoint.push(Number(record[timestampField]));
                        zeroValueDataPoint.push(0);
                        for (let keyValue in uniqueKeyValues) {
                            if (keyValue !== key) {
                                keyDataMap[keyValue].push(zeroValueDataPoint);
                            }
                        }
                    }
                }
                let data = [];
                for (var i in keyDataMap) {
                    data.push({
                        "key": i,
                        "values": keyDataMap[i]
                    });
                }
                return data;
            }
        },
        "pieChart": {
            defaultOptions: {
                chart: {
                    type: 'pieChart',
                    height: 500,
                    x: function (d) { return d.key; },
                    y: function (d) { return d.y; },
                    showLabels: true,
                    duration: 500,
                    labelThreshold: 0.01,
                    labelSunbeamLayout: true,
                    legend: {
                        margin: {
                            top: 5,
                            right: 35,
                            bottom: 5,
                            left: 0
                        }
                    }
                }
            },
            dataFormat: [
                {
                    "fieldName": "pieLabel",
                    "info": "The label name which should apear on bar"
                },
                {
                    "fieldName": "value",
                    "info": "The value of the bar"
                }
            ],
            dataFormatMapper: function (records, fieldMapping) {
                let data = [];
                var keyField = fieldMapping['pieLabel'];
                var valueField = fieldMapping['value'];
                if (keyField && valueField) {
                    for (let record of records) {
                        let dataPoint = {};
                        dataPoint["key"] = record[keyField];
                        dataPoint["y"] = record[valueField];
                        data.push(dataPoint);
                    }
                }
                return data;
            }
        },
        "lineChart": {
            defaultOptions: {
                chart: {
                    type: 'cumulativeLineChart',
                    height: 450,
                    margin: {
                        top: 20,
                        right: 20,
                        bottom: 50,
                        left: 65
                    },
                    x: function (d) { return d[0]; },
                    y: function (d) { return d[1] / 100; },
                    average: function (d) { return d.mean / 100; },

                    color: d3.scale.category10().range(),
                    duration: 300,
                    useInteractiveGuideline: true,
                    clipVoronoi: false,

                    xAxis: {
                        axisLabel: 'X Axis',
                        tickFormat: function (d) {
                            var data = d3.time.format('%m/%d/%y')(new Date(d));
                            return data;
                        },
                        showMaxMin: false,
                        staggerLabels: true
                    },

                    yAxis: {
                        axisLabel: 'Y Axis',
                        tickFormat: function (d) {
                            return d3.format(',.1%')(d);
                        },
                        axisLabelDistance: 0
                    }
                }
            },
            dataFormat: [
                {
                    "fieldName": "LineLabel",
                    "info": "The label name which should apear on bar"
                },
                {
                    "fieldName": "Timestamp",
                    "info": "The value of the bar"
                }
                ,
                {
                    "fieldName": "value",
                    "info": "The value of the bar"
                }
            ],
            dataFormatMapper: function (records, fieldMapping) {
                let keyField = fieldMapping['LineLabel'];
                let timestampField = fieldMapping['Timestamp'];
                let valueField = fieldMapping['value'];
                let keyDataMap = {};
                if (keyField && timestampField && valueField) {
                    let uniqueKeyValues = _.countBy(records, keyField);
                    for (let keyValue in uniqueKeyValues) {
                        keyDataMap[keyValue] = [];
                    }
                    for (let record of records) {
                        let key = record[keyField];
                        let values = keyDataMap[key];
                        let dataPoint = [];
                        dataPoint.push(Number(record[timestampField]));
                        dataPoint.push(Number(record[valueField]));
                        values.push(dataPoint);
                        let zeroValueDataPoint = [];
                        zeroValueDataPoint.push(Number(record[timestampField]));
                        zeroValueDataPoint.push(0);
                        for (let keyValue in uniqueKeyValues) {
                            if (keyValue !== key) {
                                keyDataMap[keyValue].push(zeroValueDataPoint);
                            }
                        }
                    }
                }
                let data = [];
                for (var i in keyDataMap) {
                    data.push({
                        "key": i,
                        "values": keyDataMap[i]
                    });
                }
                return data;
            }
        }
    };
}
