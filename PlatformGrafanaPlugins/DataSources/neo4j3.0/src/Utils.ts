import {
    DataQueryResponse,
    DataQueryError,
    TableData,
    TimeSeries,
} from '@grafana/data';


type Data = TimeSeries | TableData;
export const convertResponseToDataFramesTable = (queries, res: any): DataQueryResponse => {
    const data: Data[] = [];
    let error: DataQueryError | undefined = undefined;
    let dataArray1 = [] as any;
    let columnArray1 = [] as any;
    let rows1;
    if(res.data){
    for(let index=0;index<res.data.results.length;index++){
         dataArray1 = res.data.results[index].data;
         columnArray1 = res.data.results[index].columns.map(x => { return { text: x } });
         rows1 = dataArray1.map(c => c.row);
         data.push({ columns: columnArray1, rows: rows1, refId: queries[index].refId, name: undefined,target: queries[index] });
    }
}
    
    //console.log('datahere ----',data);

    /*let dataArray = res.data.results[0].data;
    let columnArray = res.data.results[0].columns.map(x => { return { text: x } });
    const rows = dataArray.map(c => c.row);
    data.push({ columns: columnArray, rows: rows, refId: 'A', name: undefined,target: queries });*/
    return { data, error };
};

export const convertResponseToDataFramesGraph = (queries, res: any): DataQueryResponse => {
    const data: Data[] = [];
    let error: DataQueryError | undefined = undefined;
    let datapoints = [] as any;
    let multiSeriesResponse = false;
    let response = [] as any;
    let timestamp = new Date().getTime() * 1000;
    if (res.options && res.options.range && res.options.range.to) {
        timestamp = res.options.range.to.valueOf();
    }
    let targetResponse = { target: queries[0].refId, datapoints: datapoints };
    let targetDatapointsMap = {}
    let dataArray = res.data.results[0].data;
    let columns = res.data.results[0].columns;
    for (let r in dataArray) {
        let row = dataArray[r].row;
        if (columns.length === 1) {
            datapoints.push([row[0], timestamp]);
        } else if (columns.length === 3) {
            let targetName = row[2];
            let targetDataPoints = targetDatapointsMap[targetName]
            if (targetDataPoints === undefined) {
                targetDataPoints = [];
                targetDatapointsMap[targetName] = targetDataPoints;
                data.push({
                    target: targetName,
                    datapoints: targetDataPoints
                });
                multiSeriesResponse = true;
            }
            targetDataPoints.push([row[1], row[0] * 1000]);
        } else {
            //Assuming the first column will be the time and second column will be the data.
            datapoints.push([row[1], row[0] * 1000]);
        }
    }

    if (!multiSeriesResponse) {
        response.push(targetResponse);
        data.push(response[0]);
    }

    return { data, error };
};



export const convertResponseToDataFramesRaw = (queries, res: any): any => {
   /* let datapoints = {} as any;
    datapoints["datapoints"] = res 
    datapoints["type"] = "docs";*/
    const data = [] as any;
    let error: DataQueryError | undefined = undefined;
    res.data["targets"] = queries;
    data.push(res);

    let obj = data[0].data;

    return { obj, error };
};

export const convertResponseToDataFramesTimeSeries = (queries, res: any): DataQueryResponse => {
    let timestamp = new Date().getTime() * 1000;
    const data: Data[] = [];
    let error: DataQueryError | undefined = undefined;
    let datapoints = [] as any;
    let results = res.data.results;
    for (let i in results) {
        let targetDatapointsMap = {};
        let response = [] as any;
        let result = results[i];
        let rows = result.data;
        for (let r in rows) {
            let row = rows[r].row;
            if (result.columns.length === 1) {
                datapoints.push([row[0], timestamp]);
                data.push({ target: results[i].refId, datapoints: datapoints, refId: results[i].refId, tags: undefined });
            } else if (result.columns.length === 3) {
                let targetName = row[2];
                let targetDataPoints = targetDatapointsMap[targetName];
                if (targetDataPoints === undefined) {
                    targetDataPoints = [];
                    targetDatapointsMap[targetName] = targetDataPoints;
                    response.push({
                        target: targetName,
                        datapoints: targetDataPoints
                    });
                }
                targetDataPoints.push([row[1], row[0] * 1000]);
                data.push({ target: results[i].refId, datapoints: targetDataPoints, refId: results[i].refId, tags: undefined });
            } else {
                //Assuming the first column will be the time and second column will be the data.
                datapoints.push([row[1], row[0] * 1000]);
                data.push({ target: results[i].refId, datapoints: datapoints, refId: results[i].refId, tags: undefined });
            }
        }
    }
    return { data, error };
};

export const convertResponseToDataFramesTimeSeries1 = (queries, res: any): DataQueryResponse => {
    const data: Data[] = [];
    let error: DataQueryError | undefined = undefined;
    let responseArray = res.data.results[0].data;
    const datapoints = responseArray.map(c => c.row);
    let columnArray = res.data.results[0].columns;
    const targetName = columnArray.map(c => c);
    data.push({ target: targetName, datapoints: datapoints, refId: 'A', tags: undefined });
    return { data, error };
};

export function checkCypherQueryModificationKeyword(cypherQuery) {
    let keywords: string[];
    keywords = ["create", "delete", "set", "update", "merge", "detach"];
    let flag: number = 0;
    let queryCorrect = true;
    if (cypherQuery.statements[0].statement == undefined) {
        return queryCorrect;
    }
    let j;
    for (j in keywords) {
        let query = (cypherQuery.statements[0].statement.toString()).toLowerCase();
        if (query.indexOf(" " + keywords[j] + " ") >= 0) { console.log(keywords[j] + " is present as an individual word."); flag = 1; break; }
        if (query.indexOf("\n" + keywords[j]) >= 0 && query.indexOf("\n" + keywords[j] + " ") >= 0) { console.log(keywords[j] + " is present after new line."); flag = 1; break; }
        if (query.indexOf(keywords[j] + ")") >= 0
            || query.indexOf(keywords[j] + "(") >= 0
            || query.indexOf(")" + keywords[j]) >= 0
            || query.indexOf("(" + keywords[j]) >= 0
            || query.indexOf(" " + keywords[j] + "\n") >= 0
            || query.indexOf("\n" + keywords[j] + "\n") >= 0) { console.log(keywords[j] + " is present."); flag = 1; break; }
    }
    if (flag == 0) {
        return queryCorrect;
    }
    else {
        queryCorrect = false;
        return queryCorrect;
    }
}

export function addTimestampToQuery(query, options) {
    if (query && options) {
        let range = options.range;
        if (range === undefined) {
            return query;
        }
        if (range.from) {
            var fromTime = range.from.valueOf() / 1000;
            if (query.indexOf('?START_TIME?') > -1) {
                query = query.replace(/\?START_TIME\?/g, fromTime.toString());
            }
        }
        if (range.to) {
            var toTime = range.to.valueOf() / 1000;
            if (query.indexOf('?END_TIME?') > -1) {
                query = query.replace(/\?END_TIME\?/g, toTime.toString());
            }
        }
    }
    return query;
}

export function applyTemplateVariables(value, variable, formatValue) {
    if (typeof value === 'string') {
        let values = [] as any;
        values.push(value);
        value = values;
    }
    return JSON.stringify(value);
}
