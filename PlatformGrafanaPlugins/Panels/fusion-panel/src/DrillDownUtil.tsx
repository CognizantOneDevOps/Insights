import { getBackendSrv, getTemplateSrv } from '@grafana/runtime';

import {
    DataQueryError,
    TableData,
    DataFrame, FieldType, FieldConfig, ArrayVector, guessFieldTypeForField
} from '@grafana/data';


import { isArray } from 'lodash';

let multiseries: Array<string> = ['mscolumn2d', 'mscolumn3d', 'msbar2d', 'msbar3d', 'msline', 'msarea', 'marimekko', 'overlappedcolumn2d',
    'overlappedbar2d', 'zoomline', 'zoomlinedy', 'stackedcolumn2d', 'stackedcolumn3d', 'stackedbar2d', 'scrollStackedBar2D',
    'stackedbar3d', 'stackedarea2d', 'radar'];

let scrollcharts2d: Array<string> = ['scrollcolumn2d', 'scrollbar2d', 'scrollline2d', 'scrollarea2d'];

let MSStackedColumn2DDual: string = 'msstackedcolumn2dlinedy';

let msScrollList: Array<string> = ['scrollcombi2d', 'scrollcombidy2d'];

let MSStackedColumnSingleDualAxis: Array<string> = ['stackedcolumn2dlinedy', 'stackedcolumn3dlinedy',
    'mscolumnline3d', 'stackedcolumn2dline',
    'stackedcolumn3dline', 'mscolumn3dlinedy'];

let StackedAreaLine2D: string = 'stackedarea2dlinedy';

let ColumnLineAreaSDAxis: Array<string> = ['mscombi2d', 'mscombi3d', 'mscombidy2d', 'mscombidy3d'];

//type Data = TimeSeries | TableData;
export function fetchDrillDownData(props: any, categoryLabel: any, drilldown: any, chartType: any, level: any) {


    let queryText;
    //console.log(queryText);
    if(level == 'level2'){
        queryText = props.options.drillObj[categoryLabel];
    }else if(level == 'level3'){
        queryText = props.options.level3drillObj[categoryLabel];
    }
    let dquery = getTemplateSrv().replace(queryText, {}, applyTemplateVariables);
    dquery = addTimestampToQuery(dquery, props.timeRange);
    //console.log('dquery--', dquery);
    const queryJson = {
        "statements": [
            {
                "statement": dquery,
                "includeStats": true,
                "resultDataContents": ["row", "graph"]
            }
        ],
        "metadata": [{
            "testDB": true
        }]
    };
    let testQuery = JSON.stringify(queryJson);
    return getBackendSrv().datasourceRequest({ url: 'api/datasources/proxy/' + props.options.datasourceId, method: 'POST', data: testQuery }).then((res: any) => {
        let data = res.data;
        if (drilldown) {
            props.options.drillDownResponse = data;
            return data;
        }
        if (res.status === 200) {
            return convertResponseToDataFramesTable(res, props, chartType);
        }
        else {
            return { status: 'error', message: res.error };
        }
    }).catch((err: any) => {
        console.log(err);
        if (err.data && err.data.message) {
            return { status: 'error', message: err.data.message };
        }
        else {
            return { status: 'error', message: err.status };
        }
    });

}


export const convertResponseToDataFramesTable = (res: any, props: any, chartType: any): any => {
    const data: any = [];
    let error: DataQueryError | undefined = undefined;
    let dataArray1 = [] as any;
    let columnArray1 = [] as any;
    let rows1;
    if (res.data) {
        for (let index = 0; index < res.data.results.length; index++) {
            dataArray1 = res.data.results[index].data;
            columnArray1 = res.data.results[index].columns.map((x: any) => { return { text: x } });
            rows1 = dataArray1.map((c: any) => c.row);
            data.push({ columns: columnArray1, rows: rows1, refId: undefined, name: undefined, target: undefined });
        }
    }
    //console.log('Data-->', data);
    let df = convertTableToDataFrame(data[0]);
    //console.log('Table--', df)
    //console.log(df.fields[0].values.toArray())
    if (props.options.level0Query) {
        props.options.level0 = df.fields[0].values.toArray();
        props.options.level0Query = false;
    } else {
        props.options.level1 = df.fields[0].values.toArray()
        props.options.level1Query = false;
    }
    let fields;
    if (chartType != '') {
        if (ColumnLineAreaSDAxis.includes(chartType)) {
            fields = formatChartType_Multi_Series_Column_Line_Area_Single_Dual_Y_Axis(df, chartType);
        }
        else if (chartType === StackedAreaLine2D) {
            fields = format_StackedArea_Line_Dual_Y_Axis(df);
        }
        else if (MSStackedColumnSingleDualAxis.includes(chartType)) {
            fields = formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis(df, chartType);
        }
        else if (msScrollList.includes(chartType)) {
            fields = formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis(df, chartType);
        }
        else if (chartType === MSStackedColumn2DDual) {
            fields = formatChartType_MultiSeries_Stacked_Column2D_Line_Dual_Y_Axis(df);
        }
        else if (scrollcharts2d.includes(chartType)) {
            fields = formatScrollCharts(df);
        }
        else if (multiseries.includes(chartType)) {
            fields = formatMultiSeriesChartType(df);
        } else {
            fields = formatNonScrollCharts(df);
        }
    }
    return { fields, error };
};



function convertTableToDataFrame(table: TableData): DataFrame {
    const fields = table.columns.map(c => {
        // TODO: should be Column but type does not exists there so not sure whats up here.
        const { text, type, ...disp } = c as any;
        return {
            name: text, // rename 'text' to the 'name' field
            config: (disp || {}) as FieldConfig,
            values: new ArrayVector(),
            type: type && Object.values(FieldType).includes(type as FieldType) ? (type as FieldType) : FieldType.other,
        };
    });

    if (!isArray(table.rows)) {
        throw new Error(`Expected table rows to be array, got ${typeof table.rows}.`);
    }

    for (const row of table.rows) {
        for (let i = 0; i < fields.length; i++) {
            fields[i].values.buffer.push(row[i]);
        }
    }

    for (const f of fields) {
        if (f.type === FieldType.other) {
            const t = guessFieldTypeForField(f);
            if (t) {
                f.type = t;
            }
        }
    }

    return {
        fields,
        refId: table.refId,
        meta: table.meta,
        name: table.name,
        length: table.rows.length,
    };
}

export function formatNonScrollCharts(res: any) {
    let data: any = new Array();
    let array = res.fields;
    let labels = array[0].values.buffer;
    let index = 0;
    let values = array[1].values.buffer;
    labels.forEach((obj: any) => {
        data.push({ "label": obj, "value": values[index] })
        index++;
    });
    return { data: data }
}

export function formatMultiSeriesChartType(res: any): { categories: any, dataset: any } {
    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    let labelFlag: boolean = false;
    let array = res.fields;
    array.forEach((obj: any) => {
        let array = obj.values.buffer;
        array.forEach((val: any) => {
            /*First element of the array are lables operating using labelFlag*/
            if (!labelFlag) {
                category.push({ "label": val })
            }
            else {
                data.push({ "value": val })
            }
        })
        if (labelFlag) {
            dataset.push({ "seriesname": obj.name, "data": data })
        }
        labelFlag = true;
        data = new Array();
    });
    categories.push({ "category": category });
    return { categories: categories, dataset: dataset };

}

/**
 * This method helps in parsing Neo4j response to fusion single series scroll charts
 * @param res
 */
export function formatScrollCharts(res: any) {
    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let data: any = undefined;
    let labelFlag: boolean = false;
    let array = res.fields;
    array.forEach((obj: any) => {
        let array = obj.values.buffer;
        array.forEach((val: any) => {
            /*First element of the array are lables operating using labelFlag*/
            if (!labelFlag) {
                category.push({ "label": val, })
            }
            else {
                data.push({ "value": val })
            }
        })
        if (labelFlag) {
            dataset.push({ "seriesname": obj.name, "data": data })
        }
        labelFlag = true;
        data = new Array();
    });
    categories.push({ "category": category });
    return { data: data, categories: categories, dataset: dataset };
}

export function formatChartType_MultiSeries_Stacked_Column2D_Line_Dual_Y_Axis(res: any): { categories: any, dataset: any, lineset: any } {
    let categories: any = new Array();
    let dataset: any = new Array();
    let lineset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    let labelFlag: boolean = false;
    let array = res.fields;
    let counter = 0;
    let size = array.length;
    array.forEach((obj: any) => {
        let array = obj.values.buffer;
        array.forEach((val: any) => {
            /*First element of the array are lables operating using labelFlag*/
            if (!labelFlag) {
                category.push({ "label": val, })
            }
            else {
                data.push({ "value": val })
            }
        })
        if (labelFlag) {
            // when Counter value 0 label are fetched , when Counter value 1 column values are fetched         
            if (counter === size) {
                lineset.push({ "seriesname": obj.name, "data": data })
            }
            else {
                dataset.push({ "seriesname": obj.name, "data": data })
            }
        }
        labelFlag = true;
        counter++;
        data = new Array();
    });
    categories.push({ "category": category });
    return { categories: categories, dataset: dataset, lineset: lineset };
}

export function formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis(res: any, chartType: any): { categories: any, dataset: any, lineset: any } {

    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    let lineset: any = new Array();
    let labelFlag: boolean = false;
    let array = res.fields;
    let counter = 0;
    array.forEach((obj: any) => {
        let array = obj.values.buffer;
        array.forEach((val: any) => {
            /*First element of the array are lables operating using labelFlag*/
            if (!labelFlag) {
                category.push({ "label": val, })
            }
            else {
                data.push({ "value": val })
            }
        })
        if (labelFlag) {
            // when Counter value 0 label are fetched , when Counter value 1 column values are fetched         
            if (counter === 3) {
                // For Dual Y-Axis
                if (chartType === 'stackedcolumn2dlinedy' || chartType === 'stackedcolumn3dlinedy'
                    || chartType === 'mscolumn3dlinedy') {
                    dataset.push({ "seriesname": obj.name, "renderAs": "line", "parentyaxis": "S", "data": data })
                }
                else // For single Y-Axis
                {
                    dataset.push({ "seriesname": obj.name, "renderAs": "line", "data": data })
                }
            }
            else {
                dataset.push({ "seriesname": obj.name, "data": data })
            }
        }
        labelFlag = true;
        counter++;
        data = new Array();
    });
    categories.push({ "category": category });
    return { categories: categories, dataset: dataset, lineset: lineset };

}

export function format_StackedArea_Line_Dual_Y_Axis(res: any) {
    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    let lineset: any = new Array();
    let labelFlag: boolean = false;
    let array = res.fields;
    let counter = 0;
    let size = array.length;
    array.forEach((obj: any) => {
        let array = obj.values.buffer;
        array.forEach((val: any) => {
            /*First element of the array are lables operating using labelFlag*/
            if (!labelFlag) {
                category.push({ "label": val, })
            }
            else {
                data.push({ "value": val })
            }
        })
        if (labelFlag) {
            // when Counter value 0 label are fetched , when Counter value 1 column values are fetched         
            if (counter === size) {

                dataset.push({ "seriesname": obj.name, "renderAs": "line", "showanchors": "1", "parentYAxis": "S", "data": data })

            }
            else {
                dataset.push({ "seriesname": obj.name, "data": data })
            }
        }
        labelFlag = true;
        counter++;
        data = new Array();
    });
    categories.push({ "category": category });
    return { categories: categories, dataset: dataset, lineset: lineset };
}

export function formatChartType_Multi_Series_Column_Line_Area_Single_Dual_Y_Axis(res: any, chartType: any): { categories: any, dataset: any, lineset: any } {
    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let lineset: any = new Array();
    let data: any = new Array();
    let labelFlag: boolean = false;
    let array = res.fields;
    let counter = 0;
    array.forEach((obj: any) => {
        let array = obj.values.buffer;
        array.forEach((val: any) => {
            /*First element of the array are lables operating using labelFlag*/
            if (!labelFlag) {
                category.push({ "label": val, })
            }
            else {
                data.push({ "value": val })
            }
        })
        if (labelFlag) {
            // when Counter value 0 label are fetched , when Counter value 1 column values are fetched
            if (counter === 2) {
                if (chartType === 'mscombidy2d' || chartType === 'mscombidy3d') {
                    dataset.push({ "seriesname": obj.name, "renderAs": "line", "parentyaxis": "S", "data": data })
                }
                else // For single Y-Axis
                {
                    dataset.push({ "seriesname": obj.name, "renderAs": "line", "data": data })
                }
            }
            else if (counter === 3) {
                dataset.push({ "seriesname": obj.name, "renderAs": "area", "data": data })
            }
            else {
                dataset.push({ "seriesname": obj.name, "data": data })
            }
        }
        labelFlag = true;
        counter++;
        data = new Array();
    });
    categories.push({ "category": category });
    return { categories: categories, dataset: dataset, lineset: lineset };

}

export function applyTemplateVariables(value: any, variable: any, formatValue: any) {
    if (typeof value === 'string') {
        let values = [] as any;
        values.push(value);
        value = values;
    }
    return JSON.stringify(value);
}

export function addTimestampToQuery(query: any, options: any) {
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
