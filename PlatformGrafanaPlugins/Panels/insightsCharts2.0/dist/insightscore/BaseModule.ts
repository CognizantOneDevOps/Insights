///<reference path="../../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import angular from 'angular';
import { MetricsPanelCtrl } from 'app/plugins/sdk';
import { BaseParser } from './BaseParser';

abstract class BasePanelCtrl extends MetricsPanelCtrl {
    static templateUrl = 'module.html';
    dataSourceResponse: any;
    insightsPanelData: any;
    panelEditor: any;
    panel: any;
    responseParser: BaseParser;
    /** @ngInject */
    constructor($scope, $injector) {
        super($scope, $injector);
        this.events.on('data-received', this.onDataReceived.bind(this));
        this.events.on('data-error', this.onDataError.bind(this));
        this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
        this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
        this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
        this.initializeBaseParser();
        this.initializeInsightsPanelMetadata();
    }

    initializeBaseParser() {
        let datasourceType = this.getDatasourceType();
        if (datasourceType === null || datasourceType === undefined) {
            let self = this;
            setTimeout(function () {
                self.initializeBaseParser();
            }, 50);
        } else {
            this.responseParser = new BaseParser(datasourceType);
        }
    }

    initializeInsightsPanelMetadata() {
        if (this.panel.insightsPanelData) {
            this.insightsPanelData = this.panel.insightsPanelData;
        } else {
            this.insightsPanelData = {};
            this.panel.insightsPanelData = this.insightsPanelData;
        }
    }

    registerEditor(editor) {
        this.panelEditor = editor;
    }

    getInsightsPanelData() {
        return this.insightsPanelData;
    }

    getPanel() {
        return this.panel;
    }

    getDatasourceType() {
        if (this.datasource) {
            return this.datasource.meta.name;
        }
        return null;
    }

    getResponseParser() {
        return this.responseParser;
    }

    onInitEditMode() {
        this.addEditorTab('Options', this.panelEditor.loadEditorCtrl.bind(this.buildEditorCtrlData()), 2);
    }

    buildEditorCtrlData() {
        return {
            templateUrl: 'public/plugins/' + this['pluginId'] + '/editor.html',
            controller: this.panelEditor,
            controllerAs: 'editorCtrl'
        };
    }

    onInitPanelActions(actions) {
        //actions.push({ text: 'Export CSV', click: 'ctrl.exportCsv()' });
    }

    issueQueries(datasource) {
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
        this.handlePreRender(this.dataSourceResponse);
        return super.render(this.dataSourceResponse);
    }

    //Override for rendering.
    abstract handlePreRender(dataSourceResponse);

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
    BasePanelCtrl
};
