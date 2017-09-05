///<reference path="../../../headers/common.d.ts" />
System.register([], function(exports_1) {
    var PipelinePanelEditorCtrl;
    /** @ngInject */
    function pipelinePanelEditor($q, uiSegmentSrv) {
        'use strict';
        return {
            restrict: 'E',
            scope: true,
            templateUrl: 'public/app/plugins/panel/singlestatneo/editor.html',
            controller: PipelinePanelEditorCtrl,
            controllerAs: 'pipelinePanelEditorCtrl'
        };
    }
    exports_1("pipelinePanelEditor", pipelinePanelEditor);
    return {
        setters:[],
        execute: function() {
            PipelinePanelEditorCtrl = (function () {
                /** @ngInject */
                function PipelinePanelEditorCtrl($scope, $q, uiSegmentSrv) {
                    this.$q = $q;
                    this.uiSegmentSrv = uiSegmentSrv;
                    this.neo4jDataStatus = false;
                    //$scope.editor = this;
                    var self = this;
                    self.uniqueSingleStatPanelCtrl = $scope.ctrl;
                    self.uniqueSingleStatPanel = self.uniqueSingleStatPanelCtrl.panel;
                    self.uniqueSingleStatPanelMetaData = self.uniqueSingleStatPanel.uniqueSingleStatPanelMetaData;
                    self.dataSourceResponse = self.uniqueSingleStatPanelCtrl.dataSourceResponse;
                    self.neo4jDataStatus = self.uniqueSingleStatPanelCtrl.neo4jDataStatus;
                    if (self.neo4jDataStatus === true) {
                        self.fieldsStatArray = self.uniqueSingleStatPanelCtrl.fieldsStatArray;
                        if (self.uniqueSingleStatPanelMetaData['selectedfield'] !== undefined) {
                            self.selectedfield = self.uniqueSingleStatPanelMetaData['selectedfield'];
                        }
                    }
                    self.getFontSizeValue();
                    self.render();
                }
                PipelinePanelEditorCtrl.prototype.getFontSizeValue = function () {
                    var self = this;
                    var fontlength = self.uniqueSingleStatPanelMetaData.inputProperties['font-size'].length;
                    self.fontsize = self.uniqueSingleStatPanelMetaData.inputProperties['font-size'].substring(0, fontlength - 2);
                    self.render();
                };
                PipelinePanelEditorCtrl.prototype.setFontSizeValue = function () {
                    var self = this;
                    if (self.fontsize < 151) {
                        self.uniqueSingleStatPanelMetaData.inputProperties['font-size'] = self.fontsize + 'px';
                    }
                };
                PipelinePanelEditorCtrl.prototype.setSelectedField = function () {
                    var self = this;
                    self.uniqueSingleStatPanelMetaData['selectedfield'] = self.selectedfield;
                    self.render();
                };
                //Use render method for refreshing the view.
                PipelinePanelEditorCtrl.prototype.render = function () {
                    var self = this;
                    if (self.neo4jDataStatus === true) {
                        self.uniqueSingleStatPanelCtrl.setSelectedFieldUi();
                    }
                    self.uniqueSingleStatPanelCtrl.render();
                };
                return PipelinePanelEditorCtrl;
            })();
            exports_1("PipelinePanelEditorCtrl", PipelinePanelEditorCtrl);
        }
    }
});
//# sourceMappingURL=editor.js.map