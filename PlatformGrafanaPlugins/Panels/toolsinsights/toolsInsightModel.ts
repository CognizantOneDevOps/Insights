///<reference path="../../../headers/common.d.ts" />

export class ToolsInsightModel {
    constructor(
        public toolsDetails: Tools[]
    ) { }
}

export class Tools {
    constructor(
        public toolName: string,
        public selectedFieldMapping: Fields[]
    ) { }
}

export class Fields {
    constructor(
        public dbName: string,
        public displayName: string
    ) { }
}
