///<reference path="../../../headers/common.d.ts" />
System.register([], function(exports_1) {
    var PipelineModel, FieldModel;
    return {
        setters:[],
        execute: function() {
            PipelineModel = (function () {
                function PipelineModel(pipelineRefId, fieldsList) {
                    this.pipelineRefId = pipelineRefId;
                    this.fieldsList = fieldsList;
                }
                return PipelineModel;
            })();
            exports_1("PipelineModel", PipelineModel);
            FieldModel = (function () {
                function FieldModel(fieldName, fieldMapName, fieldColor, fieldPosition) {
                    this.fieldName = fieldName;
                    this.fieldMapName = fieldMapName;
                    this.fieldColor = fieldColor;
                    this.fieldPosition = fieldPosition;
                }
                return FieldModel;
            })();
            exports_1("FieldModel", FieldModel);
        }
    }
});
//# sourceMappingURL=pipelineModel.js.map