System.register([], function(exports_1) {
    var BaseEditorCtrl;
    return {
        setters:[],
        execute: function() {
            BaseEditorCtrl = (function () {
                /** @ngInject */
                function BaseEditorCtrl($scope, $q, uiSegmentSrv) {
                    this.$q = $q;
                    this.uiSegmentSrv = uiSegmentSrv;
                    this.panelCtrl = $scope.ctrl;
                    this.panel = this.panelCtrl.panel;
                    this.insightsPanelData = this.panel.insightsPanelData;
                    this.dataSourceResponse = this.panelCtrl.dataSourceResponse;
                    this.responseParser = this.panelCtrl.responseParser;
                }
                /** @ngInject */
                BaseEditorCtrl.loadEditorCtrl = function ($q, uiSegmentSrv) {
                    'use strict';
                    return {
                        restrict: 'E',
                        scope: true,
                        templateUrl: this['templateUrl'],
                        controller: this['controller'],
                        controllerAs: this['controllerAs']
                    };
                };
                BaseEditorCtrl.prototype.getPanel = function () {
                    return this.panel;
                };
                BaseEditorCtrl.prototype.getDataSourceRespone = function () {
                    return this.dataSourceResponse;
                };
                BaseEditorCtrl.prototype.getInsightsPanelData = function () {
                    return this.insightsPanelData;
                };
                BaseEditorCtrl.prototype.getResponseParser = function () {
                    return this.responseParser;
                };
                BaseEditorCtrl.prototype.getDatasourceType = function () {
                    return this.panelCtrl.getDatasourceType();
                };
                BaseEditorCtrl.prototype.render = function () {
                    this.panelCtrl.render();
                };
                return BaseEditorCtrl;
            })();
            exports_1("BaseEditorCtrl", BaseEditorCtrl);
        }
    }
});
//# sourceMappingURL=BaseEditor.js.map