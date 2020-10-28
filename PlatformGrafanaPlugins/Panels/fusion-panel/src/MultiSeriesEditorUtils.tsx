/**
 * Change Chart type based on chart attribute.
 */
export function onGraphModeChanged({ value }: any, props: any) {

    if (value === 'zoomline' || value === 'zoomlinedy') {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isZoomlineChart': true, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else if (value === 'msline') {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isZoomlineChart': false, 'isLineChart': true, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else if (value === 'scrollStackedBar2D') {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': true, 'isDualAxis': false , 'isArea': false, 'isGauge':false});
    }
    else if (value === 'radar') {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else {
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': true, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
}

export function onLevel2GraphModeChanged({ value }: any, props: any) {

    if (value === 'zoomline' || value === 'zoomlinedy') {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isZoomlineChart': true, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else if (value === 'msline') {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isZoomlineChart': false, 'isLineChart': true, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else if (value === 'scrollStackedBar2D') {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': true, 'isDualAxis': false , 'isArea': false, 'isGauge':false});
    }
    else if (value === 'radar') {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else {
        props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': true, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
}

export function onLevel3GraphModeChanged({ value }: any, props: any) {

    if (value === 'zoomline' || value === 'zoomlinedy') {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isZoomlineChart': true, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else if (value === 'msline') {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isZoomlineChart': false, 'isLineChart': true, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else if (value === 'scrollStackedBar2D') {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': true, 'isDualAxis': false , 'isArea': false, 'isGauge':false});
    }
    else if (value === 'radar') {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
    else {
        props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': true, 'isScrollCharts': false, 'isDualAxis': false, 'isArea': false, 'isGauge':false });
    }
}