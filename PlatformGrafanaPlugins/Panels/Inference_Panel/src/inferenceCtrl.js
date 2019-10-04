import { MetricsPanelCtrl } from "app/plugins/sdk";
import _ from "lodash";
import TimeSeries from "app/core/time_series2";
import coreModule from "app/core/core_module";
import kbn from "app/core/utils/kbn";
import moment from "moment";
var google;
const panelDefaults = {
	primary: 1,
	open: false,
	showHoverChart: false,
	scrollable: true,
	happy_img: 'public/plugins/Inference_panel/img/happy.svg',
	sad_img: 'public/plugins/Inference_panel/img/sad.svg'
};
export class InferenceCtrl extends MetricsPanelCtrl {
	/** @ngInject */

	constructor($scope, $injector) {
		super($scope, $injector);
		_.defaultsDeep(this.panel, panelDefaults);

		this.events.on('render', this.onRender.bind(this));
		this.events.on('refresh', this.postRefresh.bind(this));
		this.events.on('data-error', this.onDataError.bind(this));
		this.events.on('data-received', this.onDataReceived.bind(this));
		this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
		this.googleChartData = {};
		this.panel.showHoverChart = false;

	}

	postRefresh() {

	}
	onInitEditMode() {
		this.addEditorTab('Options', './editor.html', 2);
	}
	onRender() {
		/* google = window['google'];
		google.charts.load('current', { packages: ['corechart'] }); */
		if ($('#googleChartLoaderScript').length === 0) {
            google = window['google'];
			google.charts.load('46', { packages: ['corechart', 'charteditor','gantt'] });
        }else{
			google = window['google'];
		} 
	}
	onDataReceived(dataList) {
		this.uiResponseArr = [];
		this.jsonArrtoStr = dataList;
		this.singleVectorKpiDetails = [];
		this.panel.showNoDataMessage = '';
		this.textcolor = "green";
		if (window['grafanaBootData'].user.lightTheme) {
			this.textcolor = 'black';
		} else {
			this.textcolor = 'white';
		}
		if (this.jsonArrtoStr.length > 0) {
			for (let i = 0; i < this.jsonArrtoStr.length; i++) {
				this.arr = [];
				this.vectorMap = {};
				this.vectorMap["vectorName"] = this.jsonArrtoStr[i]["heading"];
				this.jsonObjtoStr = this.jsonArrtoStr[i];
				for (var vector in this.jsonObjtoStr["inferenceDetails"]) {
					this.data = this.jsonObjtoStr["inferenceDetails"][vector];
					this.vectorProperty = {};
					this.googleChartData[this.data["kpiId"]] = this.data["resultSet"];
					this.vectorProperty["kpi"] = this.data["kpi"];
					this.vectorProperty["sentiment"] = this.data["sentiment"];
					this.vectorProperty["kpiId"] = this.data["kpiId"];
					this.vectorProperty["schedule"] = this.data["schedule"];
					this.vectorProperty["trendline"] = this.data["trendline"];
					this.vectorProperty["inference"] = this.data["inference"];
					this.vectorMap["lastRun"] = this.data["lastRun"];
					this.vectorMap["schedule"] = this.data["schedule"];
					if (this.data["sentiment"] == "POSITIVE" && this.data["trendline"] == "High to Low") {
						this.vectorProperty["color"] = "green";
						this.vectorProperty["type"] = "increased";
						this.googleChartData[this.data["kpiId"]].push("green");
					}
					else if (this.data["sentiment"] == "POSITIVE" && this.data["trendline"] == "Low to High") {
						this.vectorProperty["color"] = "green";
						this.vectorProperty["type"] = "increased";

						this.googleChartData[this.data["kpiId"]].push("green");
					}
					else if (this.data["sentiment"] == "NEGATIVE" && this.data["trendline"] == "Low to High") {
						this.vectorProperty["color"] = "red";
						this.vectorProperty["type"] = "increased";

						this.googleChartData[this.data["kpiId"]].push("red");
					}
					else if (this.data["sentiment"] == "NEGATIVE" && this.data["trendline"] == "High to Low") {
						this.vectorProperty["color"] = "red";
						this.vectorProperty["type"] = "decreased";

						this.googleChartData[this.data["kpiId"]].push("red");
					}
					else if (this.data["sentiment"] == "NEUTRAL") {
						this.vectorProperty["color"] = "green";
						this.vectorProperty["type"] = "same";

						this.googleChartData[this.data["kpiId"]].push("green");
					}
					this.arr.push(this.vectorProperty);
				}
				this.vectorMap["data"] = this.arr;
				this.uiResponseArr.push(this.vectorMap);
			}
		} else if (this.jsonArrtoStr.length == 0) {
			this.panel.showNoDataMessage = "No Data Found";
		}
		this.onRender();
	}
	onDataError() {

	}
	openCollapse() {
		this.show = true;
		var coll = document.getElementsByClassName("collapsible");
		var i;

		for (i = 0; i < coll.length; i++) {
			coll[i].addEventListener("click", function () {
				this.classList.toggle("active");
				var content = this.nextElementSibling;
				if (content.style.display === "block") {
					content.style.display = "none";
				} else {
					content.style.display = "block";
				}
			});
		}
	}
	filterData(x) {

		this.drawArr = [];
		this.col = ['Value', '', { role: 'style' }];
		this.drawArr.push(this.col);

		this.c = this.googleChartData[x][this.googleChartData[x].length - 1]
		for (var data in this.googleChartData[x]) {
			this.arr = [];
			this.arr.push(this.googleChartData[x][data]["resultDate"]);
			this.arr.push(this.googleChartData[x][data]["value"]);
			this.arr.push(this.c);
			this.drawArr.push(this.arr);
		}

		return this.drawArr;
	}
	hoverOn(x, title, vectorName) {




		this.panel.primary += 1;
		this.panel.showHoverChart = true;
		var options = { 'title': title };
		this.arr = [];

		// this.arr.push(this.filterData(x));
		var data = new google.visualization.DataTable();
		var data = google.visualization.arrayToDataTable(this.filterData(x));
		if ($('#googleChartLoaderScript').length === 0) {
				google.charts.load('46', { packages: ['corechart', 'charteditor','gantt'] });
		} 
		google.charts.setOnLoadCallback(this.drawCharts.bind(this));
		// Instantiate and draw the chart.
		var chartType = this.panel.targets[0].chartType;
		if (chartType == "LineChart") {
			var chart = new google.visualization.LineChart(document.getElementById(this.panel.id));
		} else if (chartType == "BarChart") {
			var chart = new google.visualization.BarChart(document.getElementById(this.panel.id));
		}
		if (window['grafanaBootData'].user.lightTheme) {
			options['backgroundColor'] = '#ffffff';
			options['legendTextStyle'] = { color: 'black' };
			options['titleTextStyle'] = { color: 'black' };
			options['hAxis'] = { textStyle: { color: 'black' } };
			options['vAxis'] = { textStyle: { color: 'black' } };
			this.panel.textColor = 'black';

		}
		else {
			options['backgroundColor'] = '#212124';
			options['legendTextStyle'] = { color: 'white' };
			options['titleTextStyle'] = { color: 'white' };
			options['hAxis'] = { textStyle: { color: 'white' } };
			options['vAxis'] = { textStyle: { color: 'white' } };
			this.panel.textColor = 'white';
		}
		chart.draw(data, options);
	}
	hoverOut(x) {
		document.getElementById(x).style.display = 'none'

		this.panel.showHoverChart = false;

	}
	drawCharts() {

	}
}
InferenceCtrl.templateUrl = 'module.html';