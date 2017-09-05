/// <reference path="../../../../../public/app/headers/common.d.ts" />
export declare class PipelineModel {
    pipelineRefId: string;
    fieldsList: FieldModel[];
    constructor(pipelineRefId: string, fieldsList: FieldModel[]);
}
export declare class FieldModel {
    fieldName: string;
    fieldMapName: string;
    fieldColor: string;
    fieldPosition: number;
    constructor(fieldName: string, fieldMapName: string, fieldColor: string, fieldPosition: number);
}
