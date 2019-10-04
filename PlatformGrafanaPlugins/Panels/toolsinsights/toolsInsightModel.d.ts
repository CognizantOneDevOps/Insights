/// <reference path="../../../../../public/app/headers/common.d.ts" />
export declare class ToolsInsightModel {
    toolsDetails: Tools[];
    constructor(toolsDetails: Tools[]);
}
export declare class Tools {
    toolName: string;
    fields: Fields[];
    constructor(toolName: string, fields: Fields[]);
}
export declare class Fields {
    fieldName: string;
    headerName: string;
    constructor(fieldName: string, headerName: string);
}
