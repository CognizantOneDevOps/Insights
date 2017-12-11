///<reference path="../../../headers/common.d.ts" />
System.register([], function (exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var PipelineModel, ToolModel, FieldLevelModel;
    return {
        setters: [],
        execute: function () {///<reference path="../../../headers/common.d.ts" />
            /*export class PipelinePageModel {
            constructor(
              public toolCategory: ToolsCategoryModel[],
              public pipelineDataModel: PipelineModel[]
              ) { }
            }*/
            /*export class ToolsCategoryModel {
            constructor(
              public toolCategoryName: string,
              public toolCategoryIcon: string
              ) { }
            }*/
            PipelineModel = /** @class */ (function () {
                function PipelineModel(pipelineRefId, pipelineName, pipelineColor, toolsList) {
                    this.pipelineRefId = pipelineRefId;
                    this.pipelineName = pipelineName;
                    this.pipelineColor = pipelineColor;
                    this.toolsList = toolsList;
                }
                return PipelineModel;
            }());
            exports_1("PipelineModel", PipelineModel);
            ToolModel = /** @class */ (function () {
                function ToolModel(position, toolCategoryName, toolCategoryIcon, fieldList) {
                    this.position = position;
                    this.toolCategoryName = toolCategoryName;
                    this.toolCategoryIcon = toolCategoryIcon;
                    this.fieldList = fieldList;
                }
                return ToolModel;
            }());
            exports_1("ToolModel", ToolModel);
            FieldLevelModel = /** @class */ (function () {
                function FieldLevelModel(dbName, displayName
                    //public value: number
                ) {
                    this.dbName = dbName;
                    this.displayName = displayName;
                }
                return FieldLevelModel;
            }());
            exports_1("FieldLevelModel", FieldLevelModel);
        }
    };
});
//# sourceMappingURL=pipelineModel.js.map