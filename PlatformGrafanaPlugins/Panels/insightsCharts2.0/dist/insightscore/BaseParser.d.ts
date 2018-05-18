/// <reference path="../../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import { ColumnModel } from '../insightscore/ChartModel';
export declare class BaseParser {
    private datasourceType;
    constructor(datasourceType: any);
    parseResponse(response: any): ParsedResponse[];
    private parseNeo4jResponse(response);
    private parseNeo4jResponseArray(response);
    private parseElasticSearchResponseArray(response);
}
export declare class ParsedResponse {
    target: string;
    data: any;
    columns: ColumnModel[];
    constructor(target: string, data: any, columns: ColumnModel[]);
}
