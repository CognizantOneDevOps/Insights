///<reference path="../../../headers/common.d.ts" />

export class ToolsInsightModel {
    constructor(
        public toolsDetails: Tools[]
    ) { }
}

export class Tools {
    constructor(
        public toolName: string,
        public fields: Fields[]
    ) { }
}

export class Fields {
    constructor(
        public fieldName: string,
        public headerName: string
    ) { }
}
