/// <reference path="../../../../../public/app/headers/common.d.ts" />
export declare class PipelineModel {
    pipelineRefId: string;
    pipelineName: string;
    pipelineColor: string;
    toolsList: ToolModel[];
    constructor(pipelineRefId: string, pipelineName: string, pipelineColor: string, toolsList: ToolModel[]);
}
export declare class ToolModel {
    position: number;
    toolCategoryName: string;
    toolCategoryIcon: string;
    fieldList: FieldLevelModel[];
    constructor(position: number, toolCategoryName: string, toolCategoryIcon: string, fieldList: FieldLevelModel[]);
}
export declare class FieldLevelModel {
    dbName: string;
    displayName: string;
    constructor(dbName: string, displayName: string);
}
