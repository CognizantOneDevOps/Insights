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
declare var d3: any;
declare var dagreD3: any;

class PipelinePanelCtrl extends MetricsPanelCtrl {
  static templateUrl = 'module.html';

  dataSourceResponse: any;
  toolsInsightsPanelDate: any = {};
  panelDefaults: any = {
    toolsInsightsPanelCtrl: {},
  };

  fieldNameForMsg = {
    'bit_commitId': '',
    'jir_IssueType': '',
    'son_Resourcekey': '',
    'run_ExecutionId': '',
    'jen_JobName': '',
    'git_ScmRevisionNumber': '',
    'jir_jirakey': '',
    'run_JobName': ''
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
    'DEFECTS': 'Defects'
  };

  labelIcons = {
    'GIT': 'public/plugins/toolsinsights/img/GIT.svg',
    'JENKINS': 'public/plugins/toolsinsights/img/Jenkins.svg',
    'SONAR': 'public/plugins/toolsinsights/img/SONAR.svg',
    'RUNDECK': 'public/plugins/toolsinsights/img/Rundeck.svg',
    'JIRA': 'public/plugins/toolsinsights/img/JIRA.svg',
    'BITBUCKET': 'public/plugins/toolsinsights/img/BitBucket.svg'
  };

  uniqueFieldVal = ['jir_jirakey', 'bit_commitId', 'jen_BuildNumber'];

  tableMapping = {
    'JIRA': ['jir_projectname', 'jir_jirakey', 'jir_priority', 'jir_status', 'jir_priority', 'jir_updated'],
    'BITBUCKET': ['bit_reponame', 'bit_Jira_Key', 'bit_commiTime', 'bit_commitId', 'bit_authorName', 'bit_authorEmail'],
    'JENKINS': ['jen_Result', 'jen_RundeckJobId', 'jen_ProjectName', 'jen_SCMCommitId', 'jen_BuildNumber', 'jen_TimestampEnd'],
    'SONAR': ['resourcekey', 'complexity', 'coverage', 'duplicated_blocks', 'new_violations', 'metricdate'],
    'RUNDECK': ['run_ExecutionId', 'run_JobName', 'run_JobId', 'run_ProjectName', 'run_Status', 'run_DateEnded']
  };

  pipelineToolsArray = ['JIRA', 'BITBUCKET', 'JENKINS', 'SONAR', 'RUNDECK'];

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
  screenWidth: number;
  showScroll: boolean = false;
  count: number = 1;
  start: number;
  end: number;
  displayTableFixCount: number = 2;
  totalColumns: number;
  msg: string;
  totalNodes: number;
  toolsRelationDataArray = [];
  showDagreGraph: boolean = false;
  dagreMap = {};
  nodeData = {};

  /** @ngInject */
  constructor($scope, $injector, private annotationsSrv, private $sanitize, private $window, private $rootScope) {
    super($scope, $injector);
    _.defaults(this.panel, this.panelDefaults);
    this.toolsInsightsPanelDate = this.panelDefaults.showDagreGraph;
    this.events.on('data-received', this.onDataReceived.bind(this));
    this.events.on('data-error', this.onDataError.bind(this));
    this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
    this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
    this.screenWidth = $window.innerWidth;
    this.loadD3Scripts();
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

  render() {
    this.toolsInsightsPanelDate = this.panel.toolsInsightsPanelCtrl.showDagreGraph;
    this.showDagreGraph = this.toolsInsightsPanelDate;
    return super.render(this.dataSourceResponse);
  }

  inputQuery = {
    "targets": [
      {
        "rawQuery": true,
        "refId": "A",
        "target": "MATCH (a) -[*0..100]- (x)\nWHERE a.git_ScmRevisionNumber IN [\"9830b3760f8149637bfddcf743081178cddc5b89\"]\nRETURN a, x",
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

  /**
    * Check if the D3 library is loaded or not. If not, then add new script tag.
    */
  loadD3Scripts() {
    if ($('#d3LoaderScript').length === 0) {
      d3 = {};
      $('<script>', {
        src: 'http://d3js.org/d3.v3.min.js',
        id: 'd3LoaderScript',
        type: 'text/javascript'
      }).appendTo('body');
    }

    if ($('#dagreD3LoaderScript').length === 0) {
      dagreD3 = {};
      $('<script>', {
        src: 'public/plugins/toolsinsights/dagre-d3.js',
        id: 'dagreD3LoaderScript',
        type: 'text/javascript'
      }).appendTo('body');
    }
  }

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

  onSubmitActionOutput(): void {
    var buildNewTraceabilityQryResult = this.datasourceDtl.query(this.buildNewTraceabilityQuery);
    var self = this;
    buildNewTraceabilityQryResult.then(function (data) {
      var tracebilityNewQueryData = data.data.results[0].data;
      var dashboardArray = [];
      for (var i in tracebilityNewQueryData) {
        var tracebilityNewQueryRow = tracebilityNewQueryData[i]['row'];
        let traceabilityRowData = {
          startNode: tracebilityNewQueryRow[0],
          endNode: tracebilityNewQueryRow[1],
          relNode: tracebilityNewQueryRow[2]
        };
        dashboardArray.push(traceabilityRowData);
      }
      self.buildDagreTracebilityGraph(dashboardArray);
    });
  }

  /*Dagre Graph code*/
  buildDagreTracebilityGraph(rows): void {
    if (d3 !== undefined) {
      var svg = d3.select("svg"),
        inner = svg.select("g"),
        zoom = d3.behavior.zoom().on("zoom", function () {
          inner.attr("transform", "translate(" + d3.event.translate + ")" +
            "scale(" + d3.event.scale + ")");
        });
      svg.call(zoom);
      var render = new dagreD3.render();
      var g = new dagreD3.graphlib.Graph();
      var svgDefs = svg.append('defs');
      /*code to add gradiant*/
      var mainGradient = svgDefs.append("linearGradient").attr('id', 'mainGradient');
      mainGradient.append('stop')
        .attr('class', 'stop-left')
        .attr('offset', '2%');

      mainGradient.append('stop')
        .attr('class', 'stop-right')
        .attr('offset', '95%');

      g.setGraph({
        nodesep: 20,
        ranksep: 150,
        rankdir: "LR",
        marginx: 10,
        marginy: 100
      });
      console.log(g);
      for (let row of rows) {
        this.drawDagreGraph(g, row.startNode, row.endNode);
      }
      inner.call(render, g);
      var self = this;
      svg.selectAll("g.node").on("click", function (id) {
        var _node = g.node(id);
        var nodeData = _node.data;
        self.nodeData = {};
        for (var key in self.tableMapping) {
          var nodeField = self.tableMapping[key];
          if (key === nodeData['toolName']) {
            for (var val of nodeField) {
              self.nodeData[val] = nodeData[val];
            }
            break;
          }
        }
        console.log(self.nodeData);
        console.log("Clicked " + id, self.nodeData);
        $('#tableText').show();
        self.$rootScope.$apply();
      });
    } else {
      let self = this;
      setTimeout(function () {
        self.buildDagreTracebilityGraph(rows);
      }, 50);
    }
  }

  drawDagreGraph(g, startNode, endNode): void {
    if (this.dagreMap[startNode.uuid] === undefined) {
      var startHtml = "<div style=padding-top:4px;>";
      startHtml += "<div>"
        + startNode.ToolName + "</div>";
      startHtml += "</div>";

      g.setNode(startNode.uuid, {
        labelType: "html",
        label: startHtml,
        rx: 5,
        ry: 5,
        data: startNode,
        class: 'filled',
      });
      this.dagreMap[startNode.uuid] = {};
    }

    if (this.dagreMap[endNode.uuid] === undefined) {
      var endHtml = "<div style=padding-top:4px;>";
      endHtml += "<div>"
        + endNode.ToolName + "</div>";
      endHtml += "</div>";

      g.setNode(endNode.uuid, {
        labelType: "html",
        label: endHtml,
        rx: 5,
        ry: 5,
        data: endNode,
        class: 'filled'
      });
      this.dagreMap[endNode.uuid] = {};
    }
    var difference = startNode.inSightsTime - endNode.inSightsTime;
    var h = Math.floor(difference / 3600);
    difference %= 3600;
    var m = Math.floor(difference / 60);
    var s = difference % 60;
    var output = h + 'hrs:' + m + 'mins:' + s + 'sec:';

    if (this.pipelineToolsArray.indexOf(startNode.toolName) > this.pipelineToolsArray.indexOf(endNode.toolName)) {
      g.setEdge(endNode.uuid, startNode.uuid, {
      });
    } else {
      g.setEdge(startNode.uuid, endNode.uuid, {
      });
    }
  }
  /*Dagre D3 code ends here*/

  toolSelection(): void {
    var result = this.datasourceDtl.query(this.toolListQuery);
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
    });
  }

  onToolSelectAction(): void {
    this.isToolChange = true;
    this.selectedField = '';
    this.inputVal = '';
    this.relationData = [];
    this.showToolDetails = false;
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
  }

  onInputValChangeAction(): void {
    this.relationData = [];
    this.showToolDetails = false;
    this.msg = '';
  }

  getLength(obj): number {
    var length = obj.length;
    return length;
  }

  buildNextHopQuery(label: string, queryField: string, fieldValues: any[], excludeLabels: string[]): string {
    let query = 'MATCH (a:' + label + ') -[*0..1]- (b) WHERE ';
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
    query += 'return  b.ToolName as toolName, collect(distinct b.uuid) as uuids';
    return query;
  }

  buildTraceabilityQuery(label: string, queryField: string, fieldValues: any[], uuidCollected: string[]) {
    let query = 'MATCH (b:' + label + ') WHERE ';
    if (queryField) {
      query += 'b.' + queryField + ' IN ' + JSON.stringify(fieldValues) + ' ';
    }
    query += 'MATCH (a) -[rMap*0..10]- (x) WHERE a.uuid IN ' + JSON.stringify(uuidCollected) + ' ';
    query += 'unwind rMap as r with collect(distinct r) as relList unwind relList as rel ';
    query += 'return startNode(rel) as start, endNode(rel) as end, type(rel) as relation ';
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
          uuids: rowObj[1]
        };
        dataArray.push(toolData);
      }
    }
    return dataArray;
  }

  resultContainer = {};
  onSubmitAction(selectedField, inputVal): void {
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
        console.log(cypher);
        self.inputQuery.targets[0].target = cypher;
        var queryResults = self.datasourceDtl.query(self.inputQuery);
        queryResults.then(function (data) {
          /*self.totalNodes = data.data.results[0].data.length;
          var queryData = data.data;
          self.parseQueryResult(queryData);*/
          console.log(data);
          var tracebilityNewQueryData = data.data.results[0].data;
          var dashboardArray = [];
          for (var i in tracebilityNewQueryData) {
            var tracebilityNewQueryRow = tracebilityNewQueryData[i]['row'];
            let traceabilityRowData = {
              startNode: tracebilityNewQueryRow[0],
              endNode: tracebilityNewQueryRow[1],
              relNode: tracebilityNewQueryRow[2]
            };
            dashboardArray.push(traceabilityRowData);
          }
          self.buildDagreTracebilityGraph(dashboardArray);
        });
      }
    };
    validateResults();
  }

  /*onSubmitAction(selectedField, inputVal): void {
    this.toolsRelationDataArray = [];
    this.relationData = [];
    var inputValArr = inputVal.split(",");
    this.showToolDetails = false;
    /*for multiple select
    var selectedFieldLength = this.selectedField.length;
    for (var i = 0; i < selectedFieldLength; i++) {
      if (selectedField[i] === "run_ExecutionId") {
        inputQry += "a." + selectedField[i] + " IN [" + inputValArr[i] + "]";
      } else {
        inputQry += "a." + selectedField[i] + " IN [\"" + inputValArr[i] + "\"]";
      }
      if (i !== selectedFieldLength - 1) {
        inputQry += " AND ";
      }
    }
    var inputQry;
    if (this.selectedField === "run_ExecutionId") {
      inputQry = "MATCH (a) -[*0..10]- (x) WHERE a." + this.selectedField + " IN [" + inputVal +
        "] with  x.toolName as toolName, collect(distinct x) as nodes return toolName, size(nodes) as count, nodes";
    } else {
      inputQry = "MATCH (a) -[*0..10]- (x) WHERE a." + this.selectedField + " IN [\"" + inputVal +
        "\"] with  x.toolName as toolName, collect(distinct x) as nodes return toolName, size(nodes) as count, nodes";
    }
    //inputQry += " \nRETURN a, x";
    this.inputQuery.targets[0].target = inputQry;
    this.datasourceDtl.query(this.inputQuery);
    var queryResults = this.datasourceDtl.query(this.inputQuery);
    var self = this;
    queryResults.then(function (data) {
      self.totalNodes = data.data.results[0].data.length;
      var queryData = data.data;
      self.parseQueryResult(queryData);
    });
}*/

  parseQueryResult(queryData): void {
    console.log(queryData);
    this.toolsRelationDataArray = [];
    for (var key in queryData.results) {
      var resultSet = queryData.results[key];
      var toolsResultSet = resultSet['data'];
      if (toolsResultSet.length === 0) {
        this.msg = "No results found";
      }

      for (let i in toolsResultSet) {
        var rowObj = toolsResultSet[i]['row'];
        let toolData = {
          name: rowObj[0],
          count: rowObj[1],
          data: rowObj[2]
        };
        this.toolsRelationDataArray.push(toolData);
      }

      /*for (var i in toolsResultSet) {
        var isToolNameSame = false;
        var rowObj = toolsResultSet[i]['row'];
        var rowData = rowObj[1];
        for (var fieldNm in this.fieldNameForMsg) {
          for (var key in rowData) {
            if (fieldNm === key) {
              this.fieldNameForMsg[fieldNm] = rowData[key];
            }
          }
        }
        var arrData = [];
        arrData.push(rowData);
        var toolInfo = {};
        if (this.relationData.length > 0) {
         
          if (!isToolNameSame) {
            toolInfo[rowData['ToolName']] = arrData;
            toolInfo['toolName'] = rowData['ToolName'];
            this.relationData.push(toolInfo);
          }
        } else {
          toolInfo[rowData['ToolName']] = arrData;
          toolInfo['toolName'] = rowData['ToolName'];
          this.relationData.push(toolInfo);
        }
      }*/

    }
    /*console.log(this.relationData);
    for (var key in this.relationData) {
      var toolData = this.relationData[key];
      for (var toolNm in toolData) {
        console.log(toolNm);
        console.log(toolData[toolNm]);
        if(toolNm === 'GIT'){
          
        }
      }
    }*/
  }

  showToolsDetail(toolName, data): void {
    this.showToolDetails = true;
    this.showScroll = false;
    this.selectedToolVal = toolName;
    this.selectedToolData = data;
    var count = 1;
    var toolsDataArray = [];
    var seleToolData = this.selectedToolData;
    for (var key in seleToolData) {
      var selectedToolArr = seleToolData[key];
      selectedToolArr["position"] = count++;
      toolsDataArray.push(selectedToolArr);
    }
    this.selectedToolData = toolsDataArray;
    this.scrollAction();
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
