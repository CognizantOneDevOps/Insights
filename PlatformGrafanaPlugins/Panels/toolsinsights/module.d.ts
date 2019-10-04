/// <reference path="../../../../../public/app/headers/common.d.ts" />
import { MetricsPanelCtrl } from 'app/plugins/sdk';
declare class PipelinePanelCtrl extends MetricsPanelCtrl {
    private annotationsSrv;
    private $sanitize;
    private $window;
    private $rootScope;
    static templateUrl: string;
    dataSourceResponse: any;
    toolsInsightsPanelData: any;
    panelDefaults: any;
    nodeNameMapping: {
        'JIRA': string;
        'GIT': string;
        'BITBUCKET': string;
        'JENKINS': string;
        'RUNDECK': string;
        'SONAR': string;
        'ALM': string;
        'SCM': string;
        'CI': string;
        'DEPLOYMENT': string;
        'DEFECTS': string;
        'TESTING': string;
        'LOADRUNNER': string;
    };
    labelIcons: {
        'GIT': string;
        'JENKINS': string;
        'SONAR': string;
        'RUNDECK': string;
        'JIRA': string;
        'BITBUCKET': string;
        'TESTING': string;
        'LOADRUNNER': string;
    };
    toolsList: any[];
    selectedTool: string;
    selectedField: string;
    toolDetails: any;
    showToolDetails: boolean;
    selectedToolData: any;
    selectedToolVal: string;
    inputVal: any;
    datasourceDtl: any;
    relationData: any[];
    isToolChange: boolean;
    msg: string;
    totalNodes: number;
    toolsRelationDataArray: any[];
    tableHeader: any[];
    selectedToolName: string;
    traceTimelagRelArray: any[];
    showAdvanceView: boolean;
    timelagToolsRelArray: any[];
    selectedToolSeq: any[];
    pipelineToolsArray: any[];
    resultContainer: {};
    count: number;
    start: number;
    end: number;
    displayTableFixCount: number;
    fieldListArray: any[];
    selectOptionsMsg: string;
    toolListData: any[];
    toolDataMap: {};
    showThrobber: boolean;
    advColumnMsg: string;
    fieldOptions: any[];
    /** @ngInject */
    constructor($scope: any, $injector: any, annotationsSrv: any, $sanitize: any, $window: any, $rootScope: any);
    loadGoogleCharts(): void;
    loadGoogleChart(input: any): void;
    onInitEditMode(): void;
    onInitPanelActions(actions: any): void;
    issueQueries(datasource: any): any;
    onDataError(err: any): void;
    onDataReceived(dataList: any): void;
    selectedSeq: any[];
    toolDetailMappingJson: any[];
    render(): void;
    checkToolSelection(): boolean;
    inputQuery: {
        "targets": {
            "rawQuery": boolean;
            "refId": string;
            "target": string;
            "$$hashKey": string;
        }[];
    };
    toolListQuery: {
        "targets": {
            "rawQuery": boolean;
            "refId": string;
            "target": string;
            "$$hashKey": string;
        }[];
    };
    fieldListQuery: {
        "targets": {
            "rawQuery": boolean;
            "refId": string;
            "target": string;
            "$$hashKey": string;
        }[];
    };
    buildNewTraceabilityQuery: {
        "targets": {
            "rawQuery": boolean;
            "refId": string;
            "target": string;
            "$$hashKey": string;
        }[];
    };
    buildToolsRelationQuery: {
        "targets": {
            "rawQuery": boolean;
            "refId": string;
            "target": string;
            "$$hashKey": string;
        }[];
    };
    buildToolsDetailQuery: {
        "targets": {
            "rawQuery": boolean;
            "refId": string;
            "target": string;
            "$$hashKey": string;
        }[];
    };
    toolsRelationQueryOutput(): void;
    sortResult(records: any, userSelectedToolName: any): void;
    buildCypher(sortedRecords: any, selectedToolName: any, selectedField: any, selectedValue: any): void;
    timeLagQueryOutput(cypher: any): void;
    formatDate(dateVal: any): any;
    timeLagDifference(dateVal: any): string;
    toolsFieldDetails(): void;
    toolSelection(tool: any): void;
    onToolSelectAction(): void;
    onFieldSelectAction(): void;
    onInputValChangeAction(): void;
    getLength(obj: any): number;
    buildNextHopQuery(label: string, queryField: string, fieldValues: any[], excludeLabels: string[]): string;
    buildTraceabilityQuery(label: string, queryField: string, fieldValues: any[], uuidCollected: string[]): string;
    cypherRequest: number;
    cypherResponse: number;
    noCypherResponse: boolean;
    processHop(label: string, queryField: string, fieldValues: any[], excludeLabels: string[], resultContainer: any, hopLevel: number): void;
    parseHopResult(queryData: any): any[];
    onSubmitAction(selectedField: any, inputVal: any): void;
    parseQueryResult(queryData: any): void;
    paginationArray: any[];
    currentPage: number;
    paginatedToolData: any[];
    showPagination: boolean;
    pagecount: number;
    showScroll: boolean;
    showToolsDetail(toolName: any, data: any): void;
    switchTableRows(currentpage: any): void;
    toggleTable(): void;
    showAdvanceTableView(): void;
    scrollAction(): void;
    nextFuncBlock(start: any, end: any): void;
    prevFuncBlock(start: any, end: any): void;
    link(scope: any, elem: any, attrs: any, ctrl: any): void;
}
export { PipelinePanelCtrl, PipelinePanelCtrl as PanelCtrl };
