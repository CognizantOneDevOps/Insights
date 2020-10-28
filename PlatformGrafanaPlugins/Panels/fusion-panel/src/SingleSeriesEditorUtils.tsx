import { ChangeEvent, MouseEvent } from 'react';

import { pieAndDoughnutCharts, scrollCharts } from './types';

export function onChanged(field: string, event: any, props: any) {
    let obj = { [field]: event.target.value }
    props.onOptionsChange({ ...props.options, ...obj });
};

export function onValueChanged(key: any, value: any, props: any): void {
    let obj = { [key]: value }
    props.onOptionsChange({ ...props.options, ...obj })
}

export function onSwitched(key: any, value: any, props: any): void {
    props.onOptionsChange({ ...props.options, [key]: !props.options[key] });
}

export function onLevel2ChartTypeChanged({ value }: any, props: any) {
    if (value === 'pareto2d' || value === 'pareto3d') {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isParetoChart': true, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (pieAndDoughnutCharts.includes(value)) {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': true, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (scrollCharts.includes(value)) {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': true, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (value === 'line') {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': false, 'isLineChart': true, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (value === 'waterfall2d') {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': true, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }

    else {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
}

export function onLevel3ChartTypeChanged({ value }: any, props: any) {
    if (value === 'pareto2d' || value === 'pareto3d') {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isParetoChart': true, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (pieAndDoughnutCharts.includes(value)) {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': true, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (scrollCharts.includes(value)) {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': true, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (value === 'line') {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': false, 'isLineChart': true, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (value === 'waterfall2d') {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': true, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }

    else {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
}

/**
 * Change Chart type based on chart attribute.
 */
export function onChartTypeChanged({ value }: any, props: any) {
    if (value === 'pareto2d' || value === 'pareto3d') {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isParetoChart': true, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (pieAndDoughnutCharts.includes(value)) {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isParetoChart': false, 'isPieOrDoughnutChart': true, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (scrollCharts.includes(value)) {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': true, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (value === 'line') {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': false, 'isLineChart': true, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
    else if (value === 'waterfall2d') {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': false, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': true, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }

    else {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isParetoChart': false, 'isPieOrDoughnutChart': false, 'isBarAndColumnChart': true, 'isScrollCombi2D': false, 'isLineChart': false, 'isWaterfallChart': false, 'isDualAxis': false, 'isArea': false, 'isGauge': false });
    }
}

export function onTextChange(event: ChangeEvent<HTMLTextAreaElement>, props: any) {
    if (event.target.value == "") {
        props.onOptionsChange({ ...props.options, 'atext': event.target.value, 'btnEnabled': true });
    }
    else {
        props.onOptionsChange({ ...props.options, 'atext': event.target.value, 'btnEnabled': false });
    }
}

export function onLevel2ChartPropertiesChange(event: ChangeEvent<HTMLTextAreaElement>, props: any) {
    if (event.target.value == "") {
        props.onOptionsChange({ ...props.options, 'level2ChartProperties': event.target.value });
    }
    else {
        props.onOptionsChange({ ...props.options, 'level2ChartProperties': event.target.value });
    }
}

export function onLevel3ChartPropertiesChange(event: ChangeEvent<HTMLTextAreaElement>, props: any) {
    if (event.target.value == "") {
        props.onOptionsChange({ ...props.options, 'level3ChartProperties': event.target.value });
    }
    else {
        props.onOptionsChange({ ...props.options, 'level3ChartProperties': event.target.value });
    }
}

export function onDrillChange(event: ChangeEvent<HTMLTextAreaElement>, props: any, index: any, name: any) {
    let drill = [];
    props.options.drilldown[index] = event.target.value;
    drill = props.options.drilldown;
    drill.forEach((x: any) => {
        props.options.drillObj[name] = props.options.drilldown[index];
    })
    props.onOptionsChange({ ...props.options, ...props.options.drilldown })
    props.onOptionsChange({ ...props.options, ...props.options.drillObj })
}

export function onLevel3DrillChange(event: ChangeEvent<HTMLTextAreaElement>, props: any, index: any, name: any) {
    let drill = [];
    props.options.level3drilldown[index] = event.target.value;
    drill = props.options.level3drilldown;
    drill.forEach((x: any) => {
        props.options.level3drillObj[name] = props.options.level3drilldown[index];
    })
    props.onOptionsChange({ ...props.options, ...props.options.level3drilldown })
    props.onOptionsChange({ ...props.options, ...props.options.level3drillObj })
}

export function onClick(event: MouseEvent<HTMLButtonElement>, props: any) {
    let advanceChartOptions = JSON.parse(props.options.atext)
    let chartOptions = props.options;
    let dynamicProps = props.options.dynamicProps;
    for (let key in advanceChartOptions) {

        /* this condition ensures the property is part of chart options and is empty */

        if (chartOptions[key] != undefined && chartOptions[key] === "") {
            chartOptions[key] = advanceChartOptions[key];
            props.onOptionsChange({ ...chartOptions })
        }
        else {
            /*Check advance property is not part of dyanamic props  and also chartoptions which indicates 
            its new property and neeeds to be added in dynamicProps */

            // It's new property                                                 //its old property but part of dyanamic
            if ((!dynamicProps.hasOwnProperty(key) && !chartOptions.hasOwnProperty(key)) || dynamicProps.hasOwnProperty(key)) {
                dynamicProps[key] = advanceChartOptions[key];
            }
            //Delete properties 
            for (let dkey in dynamicProps) {
                if (!advanceChartOptions.hasOwnProperty(dkey)) {
                    // Delete it from  dyanamic Props
                    delete dynamicProps[dkey];
                    // delete it from chart options too as through dyamanic props it gets added to chart options as well
                    delete chartOptions[dkey];
                }
            }
            props.onOptionsChange({ ...chartOptions, ...dynamicProps })
        }
    }
}