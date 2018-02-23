///<reference path="../../../headers/common.d.ts" />

import _ from 'lodash';
import moment from 'moment';
import flatten from '../../../core/utils/flatten';
import TimeSeries from '../../../core/time_series2';
import TableModel from '../../../core/table_model';

 function neo4jDataParser(response){
        let parsedResponse = {};
        if (response && response.results){
            let dataArray = [];
            parsedResponse['data'] = dataArray;
            for (let result of response.results) {
                var columns = result.columns;
                parsedResponse['columns'] = columns;
                for (let dataRow of result.data){
                    let data = {};
                    dataArray.push(data);
                    for (let columnIndex in columns){
                        data[columns[columnIndex]] = dataRow.row[columnIndex];
                    }
                }
            }
        }
        return parsedResponse;
    }
export {neo4jDataParser}
