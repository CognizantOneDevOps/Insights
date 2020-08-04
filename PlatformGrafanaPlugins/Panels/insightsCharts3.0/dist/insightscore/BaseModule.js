System.register(['app/plugins/sdk', './BaseParser'], function(exports_1) {
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var sdk_1, BaseParser_1;
    var BasePanelCtrl;
    return {
        setters:[
            function (sdk_1_1) {
                sdk_1 = sdk_1_1;
            },
            function (BaseParser_1_1) {
                BaseParser_1 = BaseParser_1_1;
            }],
        execute: function() {
            BasePanelCtrl = (function (_super) {
                __extends(BasePanelCtrl, _super);
                /** @ngInject */
                function BasePanelCtrl($scope, $injector) {
                    _super.call(this, $scope, $injector);
                    this.events.on('data-received', this.onDataReceived.bind(this));
                    this.events.on('data-error', this.onDataError.bind(this));
                    this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
                    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
                    this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
                    this.initializeBaseParser();
                    this.initializeInsightsPanelMetadata();
                }
                BasePanelCtrl.prototype.initializeBaseParser = function () {
                    var datasourceType = this.getDatasourceType();
                    /*if (datasourceType === null || datasourceType === undefined) {
                        let self = this;
                        setTimeout(function () {
                            self.initializeBaseParser();
                        }, 50);
                    } else {*/
                    this.responseParser = new BaseParser_1.BaseParser(datasourceType);
                    //}
                };
                BasePanelCtrl.prototype.initializeInsightsPanelMetadata = function () {
                    if (this.panel.insightsPanelData) {
                        this.insightsPanelData = this.panel.insightsPanelData;
                    }
                    else {
                        this.insightsPanelData = {};
                        this.panel.insightsPanelData = this.insightsPanelData;
                    }
                };
                BasePanelCtrl.prototype.registerEditor = function (editor) {
                    this.panelEditor = editor;
                };
                BasePanelCtrl.prototype.getInsightsPanelData = function () {
                    return this.insightsPanelData;
                };
                BasePanelCtrl.prototype.getPanel = function () {
                    return this.panel;
                };
                BasePanelCtrl.prototype.getDatasourceType = function () {
                    if (this.datasource) {
                        return this.datasource.meta.name;
                    }
                    return null;
                };
                BasePanelCtrl.prototype.getResponseParser = function () {
                    return this.responseParser;
                };
                BasePanelCtrl.prototype.onInitEditMode = function () {
                    this.addEditorTab('Options', this.panelEditor.loadEditorCtrl.bind(this.buildEditorCtrlData()), 2);
                };
                BasePanelCtrl.prototype.buildEditorCtrlData = function () {
                    return {
                        templateUrl: 'public/plugins/' + this['pluginId'] + '/editor.html',
                        controller: this.panelEditor,
                        controllerAs: 'editorCtrl'
                    };
                };
                BasePanelCtrl.prototype.onInitPanelActions = function (actions) {
                    //actions.push({ text: 'Export CSV', click: 'ctrl.exportCsv()' });
                };
                BasePanelCtrl.prototype.issueQueries = function (datasource) {
                    return _super.prototype.issueQueries.call(this, datasource);
                };
                BasePanelCtrl.prototype.onDataError = function (err) {
                    this.dataSourceResponse = [];
                    this.render();
                };
                BasePanelCtrl.prototype.onDataReceived = function (dataList) {
                    this.dataSourceResponse = dataList;
                    this.render();
                };
                BasePanelCtrl.prototype.render = function () {
                    this.handlePreRender(this.dataSourceResponse);
                    return _super.prototype.render.call(this, this.dataSourceResponse);
                };
                //Essential
                BasePanelCtrl.prototype.link = function (scope, elem, attrs, ctrl) {
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
                };
                BasePanelCtrl.templateUrl = 'module.html';
                return BasePanelCtrl;
            })(sdk_1.MetricsPanelCtrl);
            exports_1("BasePanelCtrl", BasePanelCtrl);
        }
    }
});
//# sourceMappingURL=BaseModule.js.map