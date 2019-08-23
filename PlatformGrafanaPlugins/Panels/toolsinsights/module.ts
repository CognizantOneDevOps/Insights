///<reference path="../../../headers/common.d.ts" />

import angular from 'angular';
import _ from 'lodash';
import $ from 'jquery';
import moment from 'moment';
import * as FileExport from 'app/core/utils/file_export';
import { MetricsPanelCtrl } from 'app/plugins/sdk';
//import {transformDataToTable} from './transformers';
import { toolsInsightEditor } from './editor';
//import {TableRenderer} from './renderer';
declare var google: any;

class PipelinePanelCtrl extends MetricsPanelCtrl {
    static templateUrl = 'module.html';

    dataSourceResponse: any;
    toolsInsightsPanelData: any = {};
    panelDefaults: any = {
        toolsInsightsPanelCtrl: {},
    };

    nodeNameMapping = {
        'JIRA': 'UserStory',
        'GIT': 'Commits',
        'BITBUCKET': 'Commits',
        'JENKINS': 'CI Jobs',
        'RUNDECK': 'Deployment',
        'SONAR': 'Executions',
        'ALM': 'UserStory',
        'SCM': 'Commits',
        'CI': 'CI Jobs',
        'DEPLOYMENT': 'Deployment',
        'DEFECTS': 'Defects',
        'TESTING': 'Test Executions',
        'LOADRUNNER': 'Runs'
    };

    labelIcons = {
        'GIT': 'public/app/plugins/panel/toolsinsights/img/GIT.svg',
        'JENKINS': 'public/app/plugins/panel/toolsinsights/img/Jenkins.svg',
        'SONAR': 'public/app/plugins/panel/toolsinsights/img/SONAR_new.svg',
        'RUNDECK': 'public/app/plugins/panel/toolsinsights/img/RUNDECK_new.svg',
        'JIRA': 'public/app/plugins/panel/toolsinsights/img/JIRA.svg',
        'BITBUCKET': 'public/app/plugins/panel/toolsinsights/img/BitBucket.svg',
        'TESTING': 'public/app/plugins/panel/toolsinsights/img/Testing_img.svg',
        'LOADRUNNER': 'public/app/plugins/panel/toolsinsights/img/LoadRunner.svg'
    };

    toolsList = [];
    selectedTool: string;
    selectedField: string;
    toolDetails: any;
    showToolDetails: boolean = false;
    selectedToolData: any;
    selectedToolVal: string;
    inputVal: any;
    datasourceDtl: any;
    relationData = [];
    isToolChange: boolean = false;
    msg: string;
    totalNodes: number;
    toolsRelationDataArray = [];
    tableHeader = [];
    selectedToolName: string;
    traceTimelagRelArray = [];
    showAdvanceView: boolean = false;
    timelagToolsRelArray = [];
    selectedToolSeq = [];
    pipelineToolsArray = [];
    resultContainer = {};
    count: number = 1;
    start: number;
    end: number;
    displayTableFixCount: number = 2;
    fieldListArray = [];
    selectOptionsMsg: string;
    toolListData = [];
    toolDataMap = {};
    showThrobber: boolean = false;
    advColumnMsg: string;
    fieldOptions = [];

    /** @ngInject */
    constructor($scope, $injector, private annotationsSrv, private $sanitize, private $window, private $rootScope) {
        super($scope, $injector);
        _.defaults(this.panel, this.panelDefaults);
        this.toolsInsightsPanelData = this.panelDefaults.selectedToolSeq;
        this.events.on('data-received', this.onDataReceived.bind(this));
        this.events.on('data-error', this.onDataError.bind(this));
        this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
        this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
        this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
        this.loadGoogleCharts();
        if (this.panel.toolsInsightsPanelCtrl.selectedToolsSeq === undefined) {
            this.selectOptionsMsg = "Please set required options by clicking options tab";
        }
        //console.log(this.toolsDetailJson);
    }

    loadGoogleCharts() {
        let self = this;
        /*if ($('#googleChartLoaderScript').length === 0) {
            google = {};
            $('<script>', {
                src: 'https://www.gstatic.com/charts/loader.js',
                id: 'googleChartLoaderScript',
                type: 'text/javascript'
            }).appendTo('body');
        }

        if (google === undefined || google.charts === undefined) {
            setTimeout(function () {
                self.loadGoogleCharts();
            }, 100);
        } else {
            google.charts.load('current', { 'packages': ['gantt'] });
        }*/

        $.getScript('https://www.gstatic.com/charts/loader.js', function () {
            google.charts.load('current', { 'packages': ['gantt'] });
        });
    }

    loadGoogleChart(input) {
        let self = this;
        if (google === undefined) {
            setTimeout(function () {
                self.loadGoogleChart(input);
            }, 100);
        } else {
            var data = new google.visualization.DataTable();
            data.addColumn('string', 'Tool ID');
            data.addColumn('string', 'Tool Name');
            data.addColumn('string', 'Category Name');
            data.addColumn('date', 'Start Date');
            data.addColumn('date', 'End Date');
            data.addColumn('number', 'Duration');
            data.addColumn('number', 'Percent Complete');
            data.addColumn('string', 'Dependencies');
            let rows = [];
            let toolsData = input[0]['row'][0];
            for (let tool of self.pipelineToolsArray) {
                let toolData = toolsData[tool];
                if (toolData && toolData.min && toolData.max) {
                    let min = toolData.min;
                    let max = toolData.max;
                    if (max - min < 84000) {
                        max = min + 84000;
                    }
                    let row = [];
                    row.push(tool);
                    row.push(tool);
                    row.push(tool);
                    row.push(new Date(min * 1000));
                    row.push(new Date(max * 1000));
                    row.push(null);
                    row.push(100);
                    row.push(null);
                    rows.push(row);
                }
            }
            data.addRows(rows);
            var options = {
                height: 400,
                width: 1213,
                gantt: {
                    trackHeight: 40
                }
            };
            var chart = new google.visualization.Gantt(document.getElementById('chart_div'));
            chart.draw(data, options);
        }
    }


    onInitEditMode() {
        this.addEditorTab('Options', toolsInsightEditor, 2);
    }

    onInitPanelActions(actions) {
        actions.push({ text: 'Export CSV', click: 'ctrl.exportCsv()' });
    }

    issueQueries(datasource) {
        this.datasourceDtl = datasource;
        return super.issueQueries(datasource);
    }

    onDataError(err) {
        this.dataSourceResponse = [];
        this.render();
    }

    onDataReceived(dataList) {
        this.dataSourceResponse = dataList;
        this.render();
    }

    selectedSeq = [];
    toolDetailMappingJson = [];

    render() {
        this.pipelineToolsArray = [];
        this.toolDetailMappingJson = this.panel.toolsInsightsPanelCtrl.toolDetailMappingJson;
        //console.log(this.toolDetailMappingJson);
        for (var key in this.toolDetailMappingJson) {
            var keyName = this.toolDetailMappingJson[key].toolName;
            var idx = this.pipelineToolsArray.indexOf(keyName);
            if (idx === -1) {
                this.pipelineToolsArray.push(keyName);
            }
        }
        this.selectedSeq = [];
        for (var i in this.toolDetailMappingJson) {
            this.selectedSeq[i] = this.toolDetailMappingJson[i].toolName;
        }
        console.log(this.selectedSeq);
        //console.log(this.pipelineToolsArray);
        this.checkToolSelection();
        return super.render(this.dataSourceResponse);
    }

    checkToolSelection() {
        if (this.toolDetailMappingJson === undefined) {
            this.toolDetailMappingJson = [];
        }
        if (this.toolDetailMappingJson.length === 0) {
            this.selectedSeq = [];
        }
        if(this.panel.toolsInsightsPanelCtrl.message){
            this.msg = this.panel.toolsInsightsPanelCtrl.message;
        }
        if (this.selectedSeq.length !== 0) {
            this.selectOptionsMsg = "";
            return true;
        }
        if (this.selectedSeq.length === 0) {
            this.selectOptionsMsg = "Please set required options by clicking options tab";
            return false;
        }
    }

    inputQuery = {
        "targets": [
            {
                "rawQuery": true,
                "refId": "A",
                "target":
                "MATCH (a) -[*0..100]- (x)\nWHERE a.git_ScmRevisionNumber IN [\"9830b3760f8149637bfddcf743081178cddc5b89\"]\nRETURN a, x",
                "$$hashKey": "object:190"
            }
        ]
    };

    toolListQuery = {
        "targets": [
            {
                "rawQuery": true,
                "refId": "A",
                "target": "MATCH (n) where exists(n.toolName) return DISTINCT (n.toolName) as TOOLS",
                "$$hashKey": "object:190"
            }
        ]
    };

    fieldListQuery = {
        "targets": [
            {
                "rawQuery": true,
                "refId": "A",
                "target": "MATCH (n:JENKINS:DATA) return keys (n) limit 1",
                "$$hashKey": "object:190"
            }
        ]
    };

    buildNewTraceabilityQuery = {
        "targets": [
            {
                "rawQuery": true,
                "refId": "A",
                "target": "MATCH (a) -[rMap*0..10]- (x) WHERE a.jir_jirakey IN [\"IS-10\"] " +
                "unwind rMap as r with collect(distinct r) as relList unwind relList as rel " +
                "return startNode(rel) as start, endNode(rel) as end, type(rel) as relation",
                "$$hashKey": "object:190"
            }
        ]
    };

    buildToolsRelationQuery = {
        "targets": [
            {
                "rawQuery": true,
                "refId": "A",
                "target": "match (n) -[rMap]-> (m) where exists(n.toolName) AND exists(m.toolName)  WITH distinct " +
                "n.toolName as start, m.toolName as end,  type(rMap) as rel WITH {start:start, end:end, rel:rel} as row " +
                "return collect(row) as rows",
                "$$hashKey": "object:190"
            }
        ]
    };

    buildToolsDetailQuery = {
        "targets": [
            {
                "rawQuery": true,
                "refId": "A",
                "target": "match (n:DATA) where exists(n.toolName) WITH distinct n.toolName as toolName, " +
                "keys(n) as keys UNWIND keys as key WITH distinct toolName, collect(distinct key) as keys WITH " +
                "{toolName : toolName, keys : keys} as toolsProperties return collect(toolsProperties) as toolsPropertiesList ",
                "$$hashKey": "object:190"
            }
        ]
    };

    /*timelag traceability changes start*/

    toolsRelationQueryOutput(): void {
        var buildToolsRelaQueryRes = this.datasourceDtl.query(this.buildToolsRelationQuery);
        var self = this;
        self.showThrobber = false;
        buildToolsRelaQueryRes.then(function (data) {
            let traceTimelagToolsRelArray = data.data.results[0].data[0].row[0];
            self.sortResult(traceTimelagToolsRelArray, self.selectedTool);
        });
        //self.toolsFieldDetails();
    }

    sortResult(records, userSelectedToolName): void {
        var processingCount = 0;
        var tools = [userSelectedToolName];
        var sortedRecords = [];
        while (processingCount <= records.length) {
            var toolName = tools[0];
            for (var i in records) {
                var record = records[i];
                if (record !== undefined) {
                    var matchFound = false;
                    var nextToolName = '';
                    if (toolName === record.start) {
                        matchFound = true;
                        nextToolName = record.end;
                    } else if (toolName === record.end) {
                        matchFound = true;
                        nextToolName = record.start;
                    }
                    if (matchFound) {
                        sortedRecords.push(record);
                        records[i] = undefined;
                        if (tools.indexOf(nextToolName) === -1) {
                            tools.push(nextToolName);
                        }
                    }
                }
            }
            tools.splice(0, 1);
            processingCount++;
        }
        //return sortedRecords;
        this.buildCypher(sortedRecords, this.selectedTool, this.selectedField, this.inputVal);
    }

    buildCypher(sortedRecords, selectedToolName, selectedField, selectedValue) {
        var cypher = 'MATCH (' + selectedToolName + ':' + selectedToolName + ' {' + selectedField + ':"' + selectedValue + '"}) ';
        var tools = [selectedToolName];
        var optionalMatchTemplate = 'OPTIONAL MATCH (START_NODE:START_NODE) --> (END_NODE:END_NODE)';
        for (var i in sortedRecords) {
            var record = sortedRecords[i];
            if (record) {
                cypher += 'OPTIONAL MATCH (' + record.start + ':' + record.start + ') --> (' + record.end + ':' + record.end + ') ';
                if (tools.indexOf(record.start) === -1) {
                    tools.push(record.start);
                }
                if (tools.indexOf(record.end) === -1) {
                    tools.push(record.end);
                }
            }
        }
        let returnClauseTemplate = 'TOOL_NAME : {min: min(TOOL_NAME.inSightsTime), max: max(TOOL_NAME.inSightsTime), ';
        returnClauseTemplate += 'avg: avg(TOOL_NAME.inSightsTime), count: count(distinct TOOL_NAME) }';
        var withClause = 'WITH ';
        var returnCluase = 'return  { ';
        for (var i in tools) {
            var tool = tools[i];
            withClause += tool + ',';
            returnCluase += returnClauseTemplate.replace(/TOOL_NAME/g, tool) + ',';
        }
        withClause = withClause.substring(0, withClause.lastIndexOf(','));
        returnCluase = returnCluase.substring(0, returnCluase.lastIndexOf(','));
        returnCluase += '} as toolsData';
        cypher += withClause + ' ';
        cypher += returnCluase + ' ';
        this.timeLagQueryOutput(cypher);
    }

    timeLagQueryOutput(cypher): void {
        let traceTimeLagQuery = {
            "targets": [
                {
                    "rawQuery": true,
                    "refId": "A",
                    "target": cypher,
                    "$$hashKey": "object:190"
                }
            ]
        };
        var buildTimeLagQueryRes = this.datasourceDtl.query(traceTimeLagQuery);
        var self = this;
        buildTimeLagQueryRes.then(function (data) {
            self.timelagToolsRelArray = data.data.results[0].data;
            let toolsSeqArry = [];
            self.traceTimelagRelArray = [];
            for (var key in self.toolsRelationDataArray) {
                toolsSeqArry.push(self.toolsRelationDataArray[key].name);
            }
            self.loadGoogleChart(self.timelagToolsRelArray);
            for (var rows of self.timelagToolsRelArray) {
                let timeLagData = rows.row[0];
                let pathData = [];
                for (var key in toolsSeqArry) {
                    var toolNm = toolsSeqArry[key];
                    var nextToolNm = toolsSeqArry[+key + 1];
                    let timeDiff;
                    let timeLagDt;
                    if (timeLagData[toolNm].count !== 0) {
                        if (nextToolNm !== undefined) {
                            /*if ((timeLagData[toolNm].max - timeLagData[nextToolNm].min) > 0) {
                              timeDiff = timeLagData[toolNm].max - timeLagData[nextToolNm].min;
                            }else if ((timeLagData[toolNm].min - timeLagData[nextToolNm].max) > 0) {
                              timeDiff = timeLagData[toolNm].min - timeLagData[nextToolNm].max;
                            } else {
                              timeDiff = timeLagData[nextToolNm].max - timeLagData[toolNm].max;
                            }*/
                            timeDiff = timeLagData[nextToolNm].max - timeLagData[toolNm].max;
                            timeLagDt = self.timeLagDifference(timeDiff);
                        }
                        let timeLagToolData = {
                            min: self.formatDate(new Date(timeLagData[toolNm].min * 1000)),
                            max: self.formatDate(new Date(timeLagData[toolNm].max * 1000)),
                            toolName: toolNm,
                            timeLag: timeLagDt,
                            adjcTool: nextToolNm
                        };
                        pathData.push(timeLagToolData);
                    } else {
                        pathData.push({});
                    }
                }
                self.traceTimelagRelArray.push(pathData);
            }
        });
    }

    formatDate(dateVal) {
        var date = dateVal.toISOString();
        date = date.replace('T', ' ');
        date = date.substring(0, date.lastIndexOf('.'));
        return date;
    }

    timeLagDifference(dateVal) {
        var d = Math.floor(dateVal / 86400);
        var h = Math.floor((dateVal % 86400) / 3600);
        var m = Math.floor(((dateVal % 86400) % 3600) / 60);
        var s = Math.floor((dateVal % 86400) % 3600 % 60);
        return ((d > 0 ? d + " Days ," + (h < 10 ? "0" : "") : "") + (h > 0 ? h + " Hrs ," + (m < 10 ? "0" : "") : "")
            + (m > 0 ? m + " Mins ," + (s < 10 ? "0" : "") : "") + (s < 10 ? "0" : "") + s + " Sec");
    }

    /*timelag traceability changes end*/

    /*toolSequencing and fieldSelection externalization changes start*/

    toolsFieldDetails(): void {
        var result = this.datasourceDtl.query(this.buildToolsDetailQuery);
        var self = this;
        result.then(function (data) {
            var toolsFieldQueryData = data.data.results[0].data;
            for (var i in toolsFieldQueryData) {
                var toolsRow = toolsFieldQueryData[i]['row'];
                for (var i in toolsRow) {
                    if (self.fieldListArray.indexOf(toolsRow[i]) === -1) {
                        self.fieldListArray.push(toolsRow[i]);
                    }
                }
            }
        });
    }

    /*toolSequencing and fieldSelection externalization changes end*/

    toolSelection(tool): void {
        /*var result = this.datasourceDtl.query(this.toolListQuery);
        var self = this;
        result.then(function (data) {
          var toolListQueryData = data.data.results[0].data;
          for (var i in toolListQueryData) {
            var toolListRow = toolListQueryData[i]['row'];
            for (var i in toolListRow) {
              if (self.toolsList.indexOf(toolListRow[i]) === -1) {
                self.toolsList.push(toolListRow[i]);
              }
            }
          }
        });*/
        this.toolsList = this.toolListData;
        this.fieldOptions = [];
        for (var i in this.toolDetailMappingJson) {
            if (this.toolDetailMappingJson[i].toolName === tool) {
                for (var field in this.toolDetailMappingJson[i].fields) {
                    this.fieldOptions[field] = this.toolDetailMappingJson[i].fields[field].fieldName;
                }
                break;
            }
        }
    }

    onToolSelectAction(): void {
        this.isToolChange = true;
        this.selectedField = '';
        this.inputVal = '';
        this.relationData = [];
        this.showToolDetails = false;
        this.showAdvanceView = false;
        this.toolsRelationDataArray = [];
        this.msg = '';
        var fieldValQuery = "MATCH (n:" + this.selectedTool + " :DATA) return keys (n) limit 1";
        this.fieldListQuery.targets[0].target = fieldValQuery;
        var fieldValResult = this.datasourceDtl.query(this.fieldListQuery);
        var self = this;
        fieldValResult.then(function (data) {
            var fieldListQueryData = data.data.results[0].data;
            for (var i in fieldListQueryData) {
                var fieldListRow = fieldListQueryData[i]['row'];
                for (var i in fieldListRow) {
                    self.toolDetails = fieldListRow[i];
                }
            }
        });
    }

    onFieldSelectAction(): void {
        this.inputVal = '';
        this.msg = '';
        this.showAdvanceView = false;
        this.toolsRelationDataArray = [];
    }

    onInputValChangeAction(): void {
        this.relationData = [];
        this.showToolDetails = false;
        this.showAdvanceView = false;
        this.toolsRelationDataArray = [];
        this.msg = '';
    }

    getLength(obj): number {
        var length = obj.length;
        return length;
    }

    buildNextHopQuery(label: string, queryField: string, fieldValues: any[], excludeLabels: string[]): string {
        let query = 'MATCH (a:DATA:' + label + ') -[*0..1]- (b:DATA) WHERE ';
        if (queryField) {
            query += 'a.' + queryField + ' IN ' + JSON.stringify(fieldValues) + ' ';
        }
        if (excludeLabels && excludeLabels.length > 0) {
            query += 'AND NOT (';
            for (let excludeLabel of excludeLabels) {
                query += 'b:' + excludeLabel + ' OR ';
            }
            query = query.substring(0, query.lastIndexOf('OR'));
            query += ') ';
        }
        query += 'with a, b, collect(distinct a.uuid) as uuids ';
        query += 'WHERE NOT (b.uuid IN uuids) ';
        query += 'return  b.toolName as toolName, collect(distinct b.uuid) as uuids, collect(distinct a.uuid) as sourceUuids';
        return query;
    }

    buildTraceabilityQuery(label: string, queryField: string, fieldValues: any[], uuidCollected: string[]) {
        /*let query = 'MATCH (b:' + label + ') WHERE ';
        if (queryField) {
            query += 'b.' + queryField + ' IN ' + JSON.stringify(fieldValues) + ' ';
        }
        query += 'MATCH (a) WHERE a.uuid IN ' + JSON.stringify(uuidCollected) + ' ';
        query += 'WITH (collect (distinct a)) + [b] as nodes ';
        query += 'UNWIND nodes as node ';
        query += 'WITH distinct node as a ';
        query += 'WITH a.toolName as toolName, collect(distinct a) as nodes ';
        query += 'RETURN toolName, size(nodes) as count, nodes ';*/
        let query = 'MATCH (a:DATA) WHERE a.uuid IN ' + JSON.stringify(uuidCollected) + ' ';
        query += ' WITH distinct a.toolName as toolName, collect(distinct a) as nodes Return toolName, size(nodes) as count, nodes';
        console.log(query);
        return query;
    }

    cypherRequest: number = 0;
    cypherResponse: number = 0;
    noCypherResponse: boolean = false;

    processHop(label: string, queryField: string, fieldValues: any[], excludeLabels: string[],
        resultContainer, hopLevel: number) {
        if (hopLevel > 10) {
            return;
        }
        var self = this;
        let localExcludeLabels = excludeLabels.slice(0);
        let cypher = this.buildNextHopQuery(label, queryField, fieldValues, localExcludeLabels);
        this.inputQuery.targets[0].target = cypher;
        this.cypherRequest++;
        var queryResults = this.datasourceDtl.query(this.inputQuery);
        queryResults.then(function (data) {
            self.cypherResponse++;
            localExcludeLabels.push(label);
            let results = self.parseHopResult(data.data);
            for (let result of results) {
                let uuidCollected = resultContainer['' + hopLevel];
                if (uuidCollected === undefined) {
                    uuidCollected = [];
                }
                if (result.uuids && result.uuids.length > 0) {
                    if(hopLevel == 1){
                        resultContainer['0'] = uuidCollected.concat(result.sourceUuids);    
                    }
                    resultContainer['' + hopLevel] = uuidCollected.concat(result.uuids);
                    self.processHop(result.toolName, 'uuid', result.uuids, localExcludeLabels, resultContainer, (hopLevel + 1));
                } else {
                    self.noCypherResponse = true;
                }
            }
        });
    }

    parseHopResult(queryData) {
        let dataArray = [];
        for (var key in queryData.results) {
            var resultSet = queryData.results[key];
            var hopResultSet = resultSet['data'];
            for (let i in hopResultSet) {
                var rowObj = hopResultSet[i]['row'];
                let toolData = {
                    toolName: rowObj[0],
                    uuids: rowObj[1],
                    sourceUuids: rowObj[2]
                };
                dataArray.push(toolData);
            }
        }
        return dataArray;
    }

    onSubmitAction(selectedField, inputVal): void {
        this.showThrobber = true;
        if (this.pipelineToolsArray === undefined) {
            this.msg = "Please select valid tools sequence from options tab";
        }
        this.resultContainer = {};
        this.toolDataMap = {};
        this.processHop(this.selectedTool, selectedField, [inputVal], [], this.resultContainer, 1);
        let self = this;
        let validateResults = function () {
            if (!self.noCypherResponse && self.cypherRequest !== self.cypherResponse) {
                setTimeout(validateResults, 100);
            } else {
                let uuidCollected = [];
                for (let i in self.resultContainer) {
                    uuidCollected = uuidCollected.concat(self.resultContainer[i]);
                }
                let cypher = self.buildTraceabilityQuery(self.selectedTool, selectedField, [inputVal], uuidCollected);
                self.inputQuery.targets[0].target = cypher;
                var queryResults = self.datasourceDtl.query(self.inputQuery);
                queryResults.then(function (data) {
                    self.totalNodes = data.data.results[0].data.length;
                    var queryData = data.data;
                    self.parseQueryResult(queryData);
                    if(self.totalNodes > 0){
                        self.toolsRelationQueryOutput();
                    }else{
                        self.showAdvanceView = false;
                        self.showThrobber = false;
                        self.showToolDetails = false;
                        self.msg = "No results found";
                    }
                });
            }
        };
        validateResults();
    }

    parseQueryResult(queryData): void {
        this.toolsRelationDataArray = [];
        for (var key in queryData.results) {
            var resultSet = queryData.results[key];
            var toolsResultSet = resultSet['data'];
            if (toolsResultSet.length === 0) {
                this.msg = "No results found";
            }
            let toolDataMap = {};
            let toolName;
            for (let i in toolsResultSet) {
                var rowObj = toolsResultSet[i]['row'];
                let toolData = {
                    name: rowObj[0],
                    count: rowObj[1],
                    data: rowObj[2]
                };
                this.toolDataMap[toolData.name] = toolData;
            }
            /*if (this.selectedSeq.length < this.pipelineToolsArrayDefault.length) {
                for (var toolNm of this.pipelineToolsArrayDefault) {
                    if (this.toolDataMap[toolNm] !== undefined) {
                        this.toolsRelationDataArray.push(this.toolDataMap[toolNm]);
                    }
                }
            } else {*/
            for (var toolNam of this.selectedSeq) {
                if (this.toolDataMap[toolNam] !== undefined) {
                    this.toolsRelationDataArray.push(this.toolDataMap[toolNam]);
                }
            }
            //}
        }
        this.showAdvanceView = true;
        //console.log(this.toolDataMap);
    }

    paginationArray = [];
    currentPage: number = 1;
    paginatedToolData = [];
    showPagination: boolean = false;
    pagecount: number;
    showScroll: boolean = false;
    showToolsDetail(toolName, data): void {
        this.advColumnMsg = "";
        this.showToolDetails = true;
        this.showScroll = false;
        this.selectedToolVal = toolName;
        this.selectedToolData = data;
        var count = 1;
        var toolsDataArray = [];
        this.paginationArray = [];
        this.paginatedToolData = [];
        var seleToolData = this.selectedToolData;
        for (var key in seleToolData) {
            var selToolDataArray = {};
            this.tableHeader = [];
            var selectedToolArr = seleToolData[key];
            selectedToolArr["position"] = count++;

            for (var i in this.toolDetailMappingJson) {
                if (toolName === this.toolDetailMappingJson[i].toolName) {
                    var newSelectedToolArr = this.toolDetailMappingJson[i].fields;
                }
            }

            this.tableHeader.push("Sr.No");
            for (key in newSelectedToolArr) {

                var headVal = newSelectedToolArr[key]["headerName"];
                var fieldVal = newSelectedToolArr[key]["fieldName"];

                if (headVal === "") {
                    this.tableHeader.push(fieldVal);
                } else {
                    this.tableHeader.push(headVal);
                }
                selToolDataArray["position"] = selectedToolArr["position"];
                selToolDataArray[fieldVal] = selectedToolArr[fieldVal];

            }
            toolsDataArray.push(selToolDataArray);
            /*for (var key in this.tableMapping) {
                if (key === selectedToolArr['toolName']) {
                    var nodeField = this.tableMapping[key];
                    this.tableHeader = this.tableMappingHeader[key];
                    for (var val of nodeField) {
                        selToolDataArray[val] = selectedToolArr[val];
                    }
                    toolsDataArray.push(selToolDataArray);
                    break;
                }
            }*/
            this.selectedToolName = selectedToolArr['toolName'];
        }

        if (this.tableHeader.length === 1) {
            this.advColumnMsg = "Please set required columns from Field Mapping.";
        }

        this.selectedToolData = toolsDataArray;
        var selectedToolLength = this.selectedToolData.length;
        if (selectedToolLength > 10) {
            this.showPagination = true;
        } else {
            this.showPagination = false;
        }

        var perPagesize = 10;
        this.pagecount = Math.ceil(this.selectedToolData.length / 10);
        var pageindex = 0;
        this.switchTableRows(1);
        this.scrollAction();
        $("#advanceViewTableId").hide();
        $("#chart_div").hide();
        $("#showAdvanceText").show();
        $("#basicViewTableId").show();
    }

    switchTableRows(currentpage): void {
        this.currentPage = currentpage;

        if (this.pagecount === this.currentPage) {
            $("#activePage_" + this.currentPage).addClass("disableLi");
            this.paginationArray = [];
            var shiftRowVal = this.pagecount - 3;
            for (let i = shiftRowVal; i <= this.pagecount; i++) {
                this.paginationArray.push(i);
            }
        }
        if (this.currentPage === 1) {
            this.paginationArray = [];
            for (let i = 1; i < this.pagecount; i++) {
                if (i <= 4) {
                    this.paginationArray.push(i);
                }
            }
        }
        if (currentpage <= this.pagecount && currentpage >= 1) {
            var showFrom = 10 * (currentpage - 1);
            var showTo = showFrom + 10;
            this.paginatedToolData = this.selectedToolData.slice(showFrom, showTo);
        }
    }

    toggleTable(): void {
        $("#toolData").toggle();
    }

    showAdvanceTableView(): void {
        this.advColumnMsg = "";
        $("#advanceViewTableId").show();
        $("#chart_div").show();
        $("#basicViewTableId").hide();
        $("#showAdvanceText").hide();
    }


    scrollAction(): void {
        var totalNodes = this.selectedToolData.length;
        this.start = this.count;
        this.end = 2;
        if (totalNodes > this.displayTableFixCount) {
            this.showScroll = true;
        }
    }

    nextFuncBlock(start, end): void {
        var totalNodes = this.selectedToolData.length;
        if (this.showScroll && end !== totalNodes) {
            this.start = start + 2;
            this.end = end + 2;
        }
    }

    prevFuncBlock(start, end): void {
        var totalNodes = this.selectedToolData.length;
        if (this.showScroll && start !== 1 && end !== this.displayTableFixCount) {
            this.start = start - 2;
            this.end = end - 2;
        }
    }

    //Essential
    link(scope, elem, attrs, ctrl) {
        var data;
        var panel = ctrl.panel;
        var pageCount = 0;
        var formaters = [];
        function renderPanel() {
        }

        ctrl.events.on('render', function (renderData) {
            data = renderData || data;
            if (data) {
                renderPanel();
            }
            ctrl.renderingCompleted();
        });
    }
}

export {
    PipelinePanelCtrl,
    PipelinePanelCtrl as PanelCtrl
};
