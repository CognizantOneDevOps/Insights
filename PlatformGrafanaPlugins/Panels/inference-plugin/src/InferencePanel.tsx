import React from 'react';
import { PanelProps } from '@grafana/data';
import { css, cx } from 'emotion';
//import $ from 'jquery';
import { processData } from './inferenceUtil';
import './inferenceLayout.css';
import { config } from '@grafana/runtime';
import { SimpleOptions } from 'types';

interface Props extends PanelProps<SimpleOptions> { }

let fusionID: string | undefined;
let gID: any;
let chartData;
let chartCaption;
let chartType;
let palettecolors;

export class InferencePanel extends React.Component<Props>{

    constructor(props) {
        super(props);
    }

    render() {
        const { options } = this.props;
        fusionID = 'insights-inference-fusion' + this.props.id;
        gID =  'insights-inference-google' + this.props.id;
        let data = processData(this.props);
        //console.log('processed data--', data);
        let google = (window as any).google
        //console.log('google--',google);
        google.charts.load('46', {'packages':['corechart','charteditor','gantt']});
        google.charts.setOnLoadCallback(this.googleChart);
        let inferenceLists;
        if(data.length == 0){
            inferenceLists = "No Records found!";
        }else{
           inferenceLists = data[0].data.map(link => {
            return link.color === 'green' ? <div onMouseEnter={() => { this.getInference(link,options) }}><img src="public/img/satisfied.png" />{link.inference}</div> :
                <div onMouseEnter={() => { this.getInference(link,options) }}><img src="public/img/dissatisfied.png" />{link.inference}</div>
         });
        }
        return (
            <div style={{ height: '100%',width:'100%' }} className={cx(`position:relative`, css`width: ${this.props.width}px;height: ${this.props.height}px;`)}>
                <div className="row">
                    <div className="column" id="inference-lists">
                        <ul>
                            {inferenceLists}
                        </ul>
                    </div>
                    {options.enableFusion && (<div className="column" id={fusionID} >{this.fusionChart()}</div>)}
                    {!options.enableFusion && (<div className="column" id={gID}></div>)}
                </div>
            </div>
        );
    }

    /**Fetch Details on hover of each kpi */
    getInference(link,options) {
        //console.log('hovered--',link);
        chartData = link.resultSet;
        chartCaption = link.inference;
        chartType = this.props.options.fusionChartType;
        palettecolors=link.color == 'green'?'#008000':'#FF0000';
        //console.log('pale--',palettecolors);
        if(options.enableFusion){
            this.fusionChart();
        }else{
            this.googleChart(link);
        }
    }

    /*Render Fusioncharts*/
    fusionChart(): React.ReactNode {
        let FusionCharts = (window as any).FusionCharts;
        const chartConfig = this.fetchChartConfig();
        FusionCharts.ready(function () {
            var fusioncharts = new FusionCharts(chartConfig);
            fusioncharts.resizeTo('100%', '100%');
            fusioncharts.render();
        });
        return '';
    }

    /*Fetch Fusincharts config and data*/
    private fetchChartConfig() {
        const { theme } = config;
        const chartConfig = {
            type: chartType ? chartType : 'pareto2d',
            renderAt: 'insights-inference-fusion' + this.props.id,
            dataFormat: 'json',
            containerBackgroundOpacity: '0',
            dataSource: {
                "chart": {
                    caption: chartCaption ? chartCaption : '',
                    theme: "fusion",
                    bgColor: theme.colors.bg1,
                    canvasbgColor: theme.colors.bg1,
                    valueFontColor: theme.colors.text,
                    labelFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
                    legendItemFontColor: theme.isDark ? theme.palette.white : theme.palette.dark1,
                    xAxisFontColor: theme.colors.text,
                    yAxisFontColor: theme.colors.text,
                    captionFontColor: theme.colors.text,
                    palettecolors:palettecolors
                },
                "data": chartData
            }
        };
        return chartConfig;
    }

    /*Fetch KPI for google and render google chart*/
    googleChart(link){
        let google = (window as any).google
        var data = new google.visualization.DataTable();
        if(this === undefined){
            return;
        }
        var data = google.visualization.arrayToDataTable(this.filterData(link));
        /*data.addColumn('string', 'Topping');
        data.addColumn('number', 'Slices');
        data.addRows([
            ['Mushrooms', 3],
            ['Onions', 1],
            ['Olives', 1],
            ['Zucchini', 1],
            ['Pepperoni', 2]
        ]);*/
        let options = { 'title': chartCaption};
        if (window['grafanaBootData'].user.lightTheme) {
            options['backgroundColor'] = config.theme.colors.bg1;
            options['legendTextStyle'] = { color: 'black' };
            options['titleTextStyle'] = { color: 'black' };
            options['hAxis'] = { textStyle: { color: 'black' } };
            options['vAxis'] = { textStyle: { color: 'black' } };
        }
        else {
            options['backgroundColor'] = config.theme.colors.bg1;
            options['legendTextStyle'] = { color: 'white' };
            options['titleTextStyle'] = { color: 'white' };
            options['hAxis'] = { textStyle: { color: 'white' } };
            options['vAxis'] = { textStyle: { color: 'white' } };
        }
        let chart;
        if(this.props.options.googleChartType == 'bar'){
             chart = new google.visualization.BarChart(document.getElementById('insights-inference-google' + this.props.id));
        }else{
             chart = new google.visualization.LineChart(document.getElementById( 'insights-inference-google' + this.props.id));
        }
        chart.draw(data, options);
    }

    /*Filter kpi for google charts*/
    filterData(link) {
        let drawArr = [] as any;
		let col = ['Value', '', { role: 'style' }];
		drawArr.push(col);
        let color = link.color;
		for (let data of link.resultSet) {
			let arr = [] as any;
			arr.push(data["label"]);
			arr.push(data["value"]);
			arr.push(color);
			drawArr.push(arr);
		}
		return drawArr;
	}
};


