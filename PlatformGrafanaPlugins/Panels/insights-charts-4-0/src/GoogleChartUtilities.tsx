import { ChartData } from './models/ChartModel';

export function googlechartutilities(theme: any, transformDataInstruction: string, joinInstructions: string, google: any, isEditor: boolean) {
    const applyTheme = (chartOptions: any) => {
        let grafanaBootData = (window as any)['grafanaBootData'];
        let version = Number(grafanaBootData.settings.buildInfo.version.split(".")[0]);
        if (version >= 5) {
            let textColor = '';
            let fillColor = '';
            fillColor = theme.colors.background.primary;
            textColor = theme.colors.text.primary;
            chartOptions['backgroundColor'] = fillColor;
            let hAxis = chartOptions['hAxis'];
            if (hAxis === undefined) {
                hAxis = {};
                chartOptions['hAxis'] = hAxis;
            }
            let hTextStyle = hAxis['textStyle'];
            if (hTextStyle === undefined) {
                hTextStyle = {};
                hAxis['textStyle'] = hTextStyle;
            }
            hTextStyle['color'] = textColor;
            let legendTextStyle = chartOptions['legendTextStyle'];
            if (legendTextStyle === undefined) {
                legendTextStyle = {};
                chartOptions['legendTextStyle'] = legendTextStyle;
            }
            legendTextStyle['color'] = textColor;
            let vAxes = chartOptions['vAxes'];
            if (vAxes === undefined) {
                vAxes = [{}];
                chartOptions['vAxes'] = vAxes;
            }
            for (let v in vAxes) {
                let vAxis = vAxes[v];
                let vTextStyle = vAxis['textStyle'];
                if (vTextStyle === undefined) {
                    vTextStyle = {};
                    vAxis['textStyle'] = vTextStyle;
                }
                vTextStyle['color'] = textColor;
            }
        }
        return chartOptions;
    };

    const innerDimensions = (node: any) => {
        var computedStyle = getComputedStyle(node);

        let width = node.clientWidth; // width with padding
        let height = node.clientHeight; // height with padding

        height -= parseFloat(computedStyle.paddingTop) + parseFloat(computedStyle.paddingBottom);
        width -= parseFloat(computedStyle.paddingLeft) + parseFloat(computedStyle.paddingRight);
        return { height, width };
    };

    const transformData = (data: any) => {
        if (transformDataInstruction === undefined || transformDataInstruction === null || transformDataInstruction === "") {
            return data;
        } else {
            let transformDataFunc = new Function('data', transformDataInstruction);
            return transformDataFunc(data);
        }
    };

    const joinDataTables = (dataTables: any[]) => {
        if (dataTables && dataTables.length > 0) {
            if (joinInstructions === undefined || joinInstructions === null || joinInstructions === "") {
                return dataTables[0];
            } else {
                let joinFunc = new Function('dataTables', joinInstructions);
                return joinFunc(dataTables);
            }
        }
    };

    const buildDataTables = (dataArray: ChartData[]) => {
        let dataTables = [];
        if (dataArray) {
            for (let data of dataArray) {
                if (data.columns) {
                    let typeMapping = [];
                    for (let column of data.columns) {
                        typeMapping.push({ label: column.name, type: column.type, refId: column.refId });
                    }
                    dataTables.push(convertData(data.data, typeMapping));
                }
            }
        }
        return dataTables;
    };
    const convertData = (dataRows: any, typeMapping: any[]) => {
        let data = new google.visualization.DataTable();
        for (let column of typeMapping) {
            data.addColumn(column['type'], column['label']);
        }

        let rowArr: any = [];
        let dataIter: any = []
        if (!isEditor) {
            dataIter = dataRows.series
        } else {
            dataIter = dataRows
        }
        let dataRow: any;
        for (let refD of dataIter) {
            if (refD.refId === typeMapping[0].refId) {
                dataRow = refD
            }
        }
        for (let i = 0; i < dataRow.length; i++) {
            let row: any = [];
            for (let fields of dataRow.fields) {
                let columnObj = typeMapping.find(o => o.label === fields.name)
                row.push(convertToType(fields.values.buffer[i], columnObj?.type));
            }
            rowArr.push(row);
        }

        data.addRows(rowArr);
        return data;
    };

    const convertToType = (data: any, type: any) => {
        if (data === undefined || data === null) {
            return null;
        } else {
            if (type === 'string') {
                return data.toString();
            } else if (type === 'number') {
                return Number(data);
            } else if (type === 'boolean') {
                if (typeof data === 'boolean') {
                    return data;
                }
                return (data === "true");
            } else if (type === 'date') {
                if (typeof data === 'number') {
                    let dataStr = data.toString();
                    if (dataStr.length < 13) {
                        let appendZeros = '0000000000000';
                        data = dataStr + appendZeros.substring(0, (13 - dataStr.length));
                        return new Date(Number(data));
                    } else {
                        return new Date(Number(data));
                    }
                } else {
                    return new Date(data);
                }
            } else if (type === 'datetime') {
            } else if (type === 'timeofday') {
            } else {
                throw new TypeError('Unknown Type passed.');
            }
        }
    }

    return { buildDataTables, joinDataTables, transformData, innerDimensions, applyTheme };
}
