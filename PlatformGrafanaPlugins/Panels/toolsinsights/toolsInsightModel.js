///<reference path="../../../headers/common.d.ts" />
System.register([], function (exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var ToolsInsightModel, Tools, Fields;
    return {
        setters: [],
        execute: function () {///<reference path="../../../headers/common.d.ts" />
            ToolsInsightModel = (function () {
                function ToolsInsightModel(toolsDetails) {
                    this.toolsDetails = toolsDetails;
                }
                return ToolsInsightModel;
            }());
            exports_1("ToolsInsightModel", ToolsInsightModel);
            Tools = (function () {
                function Tools(toolName, fields) {
                    this.toolName = toolName;
                    this.fields = fields;
                }
                return Tools;
            }());
            exports_1("Tools", Tools);
            Fields = (function () {
                function Fields(fieldName, headerName) {
                    this.fieldName = fieldName;
                    this.headerName = headerName;
                }
                return Fields;
            }());
            exports_1("Fields", Fields);
        }
    };
});
//# sourceMappingURL=toolsInsightModel.js.map