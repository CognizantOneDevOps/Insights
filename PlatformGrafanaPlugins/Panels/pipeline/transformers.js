System.register([], function (exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var Transformers;
    return {
        setters: [],
        execute: function () {
            ///<reference path="../../../headers/common.d.ts" />
            Transformers = /** @class */ (function () {
                function Transformers() {
                    this.FieldsMetaDataArray = [];
                }
                //get fields for db object from real data
                Transformers.prototype.getFields = function (data) {
                    for (var i in data.results) {
                        this.FieldsMetaDataArray.push.apply(this.FieldsMetaDataArray, (data.results[i].columns));
                    }
                    return this.FieldsMetaDataArray;
                };
                //insert for property value in  clone object from real data
                Transformers.prototype.insertValueProperty = function (data, cloneObject) {
                    if (data !== undefined && cloneObject.pipelineDataModel !== undefined) {
                        for (var x = 0; x < cloneObject.pipelineDataModel.length; x++) {
                            var pipeline = cloneObject.pipelineDataModel[x];
                            for (var i = 0; i < pipeline.toolsList.length; i++) {
                                var toolList = pipeline.toolsList[i];
                                for (var j = 0; j < toolList.fieldList.length; j++) {
                                    var fields = toolList.fieldList[j];
                                    for (var _i = 0, _a = data.results[x].columns; _i < _a.length; _i++) {
                                        var fieldName = _a[_i];
                                        if (fields.dbName === fieldName) {
                                            fields['value'] = data.results[x].data[0].row[data.results[x].columns.indexOf(fieldName)];
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return cloneObject;
                };
                return Transformers;
            }());
            exports_1("Transformers", Transformers);
        }
    };
});
//# sourceMappingURL=transformers.js.map