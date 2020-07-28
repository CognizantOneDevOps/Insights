System.register(['../insightscore/ChartModel'], function(exports_1) {
    var ChartModel_1;
    var BaseParser, ParsedResponse;
    return {
        setters:[
            function (ChartModel_1_1) {
                ChartModel_1 = ChartModel_1_1;
            }],
        execute: function() {
            BaseParser = (function () {
                function BaseParser(datasourceType) {
                    this.datasourceType = datasourceType;
                }
                BaseParser.prototype.parseResponse = function (response) {
                    if ('Elasticsearch' === this.datasourceType) {
                        return this.parseElasticSearchResponseArray(response);
                    }
                    else {
                        return this.parseNeo4jResponseArray(response);
                    }
                };
                BaseParser.prototype.parseNeo4jResponse = function (response) {
                    var parsedResponse = {};
                    if (response && response.results) {
                        var dataArray = [];
                        parsedResponse['data'] = dataArray;
                        for (var _i = 0, _a = response.results; _i < _a.length; _i++) {
                            var result = _a[_i];
                            var columns = result.columns;
                            parsedResponse['columns'] = columns;
                            for (var _b = 0, _c = result.data; _b < _c.length; _b++) {
                                var dataRow = _c[_b];
                                var data = {};
                                dataArray.push(data);
                                for (var columnIndex in columns) {
                                    data[columns[columnIndex]] = dataRow.row[columnIndex];
                                }
                            }
                        }
                    }
                    return parsedResponse;
                };
                BaseParser.prototype.parseNeo4jResponseArray = function (response) {
                    var parsedResponseArray = [];
                    var results = [];
                    var targets = [];
                    var obj = { errors: [], results: results, targets: targets };
                    if (response) {
                        var queryLength = response.length;
                        for (var index = 0; index < queryLength; index++) {
                            var dataArray = [];
                            if (response[index].rows) {
                                for (var _i = 0, _a = response[index].rows; _i < _a.length; _i++) {
                                    var row = _a[_i];
                                    dataArray.push({ row: row });
                                }
                            }
                            else {
                                for (var _b = 0, _c = response[index].datapoints; _b < _c.length; _b++) {
                                    var row = _c[_b];
                                    dataArray.push({ row: row.reverse() });
                                }
                            }
                            if (response[index].columns) {
                                var resultsObj = { columns: response[index].columns.map(function (c) { return c.text; }), data: dataArray };
                                results.push(resultsObj);
                                targets.push({ refId: response[index].refId });
                            }
                            else {
                                var resultsObj = { columns: ["Time", response[index].target], data: dataArray };
                                results.push(resultsObj);
                                targets.push({ refId: response[index].refId });
                            }
                        }
                        for (var index in obj.targets) {
                            var dataArray = [];
                            var data = dataArray;
                            var target = obj.targets[index].refId;
                            var result = obj.results[index];
                            var columns = result.columns;
                            for (var _d = 0, _e = result.data; _d < _e.length; _d++) {
                                var dataRow = _e[_d];
                                var data_1 = {};
                                dataArray.push(data_1);
                                for (var columnIndex in columns) {
                                    data_1[columns[columnIndex]] = dataRow.row[columnIndex];
                                }
                            }
                            var colummnModels = [];
                            for (var _f = 0, _g = result.columns; _f < _g.length; _f++) {
                                var column = _g[_f];
                                colummnModels.push(new ChartModel_1.ColumnModel(column));
                            }
                            parsedResponseArray.push(new ParsedResponse(target, data, colummnModels));
                        }
                    }
                    return parsedResponseArray;
                };
                BaseParser.prototype.parseElasticSearchResponseArray = function (response) {
                    var parsedResponseArray = [];
                    if (response) {
                        var dataArray = [];
                        var columnNames = [];
                        for (var _i = 0; _i < response.length; _i++) {
                            var data = response[_i];
                            var datapoints = data.datapoints;
                            var keyColumnName = data.target;
                            if (columnNames.indexOf(keyColumnName) === -1) {
                                columnNames.push(keyColumnName);
                            }
                            //Need to identify various formats we can get in the response.
                            for (var _a = 0; _a < datapoints.length; _a++) {
                                var datapoint = datapoints[_a];
                                var count = datapoint[0];
                                if (count !== 0) {
                                    var parsedData = {};
                                    parsedData['time'] = datapoint[1];
                                    parsedData[keyColumnName] = datapoint[0];
                                    dataArray.push(parsedData);
                                }
                            }
                        }
                        var colummnModels = [];
                        colummnModels.push(new ChartModel_1.ColumnModel('time', 'date'));
                        for (var _b = 0; _b < columnNames.length; _b++) {
                            var column = columnNames[_b];
                            //Need to identify various formats we can get in the response.
                            colummnModels.push(new ChartModel_1.ColumnModel(column, 'number'));
                        }
                        parsedResponseArray.push(new ParsedResponse('Elasticsearch', dataArray, colummnModels));
                    }
                    return parsedResponseArray;
                };
                return BaseParser;
            })();
            exports_1("BaseParser", BaseParser);
            ParsedResponse = (function () {
                function ParsedResponse(target, data, columns) {
                    this.target = target;
                    this.data = data;
                    this.columns = columns;
                }
                return ParsedResponse;
            })();
            exports_1("ParsedResponse", ParsedResponse);
        }
    }
});
//# sourceMappingURL=BaseParser.js.map