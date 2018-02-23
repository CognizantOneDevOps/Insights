///<reference path="../../../../headers/common.d.ts" />
import { ColumnModel } from '../insightscore/ChartModel';

export class BaseParser {
    constructor(private datasourceType) {
        //console.log(datasourceType);
    }

    parseResponse(response) {
        //console.log(response);
        if ('Elasticsearch' === this.datasourceType) {
            return this.parseElasticSearchResponseArray(response);
        } else {
            return this.parseNeo4jResponseArray(response);
        }
    }

    private parseNeo4jResponse(response) {
        let parsedResponse = {};
        if (response && response.results) {
            let dataArray = [];
            parsedResponse['data'] = dataArray;
            for (let result of response.results) {
                var columns = result.columns;
                parsedResponse['columns'] = columns;
                for (let dataRow of result.data) {
                    let data = {};
                    dataArray.push(data);
                    for (let columnIndex in columns) {
                        data[columns[columnIndex]] = dataRow.row[columnIndex];
                    }
                }
            }
        }
        return parsedResponse;
    }

    private parseNeo4jResponseArray(response): ParsedResponse[] {
        let parsedResponseArray: ParsedResponse[] = [];
        if (response && response.targets) {
            for (let index in response.targets) {
                let dataArray = [];
                let data = dataArray;
                let target = response.targets[index].refId;
                let result = response.results[index];
                let columns = result.columns;
                for (let dataRow of result.data) {
                    let data = {};
                    dataArray.push(data);
                    for (let columnIndex in columns) {
                        data[columns[columnIndex]] = dataRow.row[columnIndex];
                    }
                }
                let colummnModels: ColumnModel[] = [];
                for (let column of result.columns) {
                    colummnModels.push(new ColumnModel(column));
                }
                parsedResponseArray.push(new ParsedResponse(target, data, colummnModels));
            }
        }
        return parsedResponseArray;
    }

    private parseElasticSearchResponseArray(response): ParsedResponse[] {
        let parsedResponseArray: ParsedResponse[] = [];
        if (response) {
            let dataArray = [];
            let columnNames = [];
            for (let data of response) {
                let datapoints = data.datapoints;
                let keyColumnName = data.target;
                if (columnNames.indexOf(keyColumnName) === -1) {
                    columnNames.push(keyColumnName);
                }
                //Need to identify various formats we can get in the response.
                for (let datapoint of datapoints) {
                    let count = datapoint[0];
                    if (count !== 0) {
                        let parsedData = {};
                        parsedData['time'] = datapoint[1];
                        parsedData[keyColumnName] = datapoint[0];
                        dataArray.push(parsedData);
                    }
                }
            }
            let colummnModels: ColumnModel[] = [];
            colummnModels.push(new ColumnModel('time', 'date'));
            for (let column of columnNames) {
                //Need to identify various formats we can get in the response.
                colummnModels.push(new ColumnModel(column, 'number'));
            }
            parsedResponseArray.push(new ParsedResponse('Elasticsearch', dataArray, colummnModels));
        }
        return parsedResponseArray;
    }
}

export class ParsedResponse {
    constructor(public target: string, public data: any, public columns: ColumnModel[]) { }
}
