///<reference path="../../../headers/common.d.ts" />
System.register([], function(exports_1) {
    function neo4jDataParser(response) {
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
    }
    return {
        setters:[],
        execute: function() {
            exports_1("neo4jDataParser", neo4jDataParser);
        }
    }
});
//# sourceMappingURL=transformers.js.map