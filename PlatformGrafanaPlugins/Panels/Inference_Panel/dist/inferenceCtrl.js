"use strict";

System.register(["app/plugins/sdk", "lodash", "app/core/time_series2", "app/core/core_module", "app/core/utils/kbn", "moment"], function (_export, _context) {
	"use strict";

	var MetricsPanelCtrl, _, TimeSeries, coreModule, kbn, moment, _createClass, google, panelDefaults, InferenceCtrl;

	function _classCallCheck(instance, Constructor) {
		if (!(instance instanceof Constructor)) {
			throw new TypeError("Cannot call a class as a function");
		}
	}

	function _possibleConstructorReturn(self, call) {
		if (!self) {
			throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
		}

		return call && (typeof call === "object" || typeof call === "function") ? call : self;
	}

	function _inherits(subClass, superClass) {
		if (typeof superClass !== "function" && superClass !== null) {
			throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
		}

		subClass.prototype = Object.create(superClass && superClass.prototype, {
			constructor: {
				value: subClass,
				enumerable: false,
				writable: true,
				configurable: true
			}
		});
		if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
	}

	return {
		setters: [function (_appPluginsSdk) {
			MetricsPanelCtrl = _appPluginsSdk.MetricsPanelCtrl;
		}, function (_lodash) {
			_ = _lodash.default;
		}, function (_appCoreTime_series) {
			TimeSeries = _appCoreTime_series.default;
		}, function (_appCoreCore_module) {
			coreModule = _appCoreCore_module.default;
		}, function (_appCoreUtilsKbn) {
			kbn = _appCoreUtilsKbn.default;
		}, function (_moment) {
			moment = _moment.default;
		}],
		execute: function () {
			_createClass = function () {
				function defineProperties(target, props) {
					for (var i = 0; i < props.length; i++) {
						var descriptor = props[i];
						descriptor.enumerable = descriptor.enumerable || false;
						descriptor.configurable = true;
						if ("value" in descriptor) descriptor.writable = true;
						Object.defineProperty(target, descriptor.key, descriptor);
					}
				}

				return function (Constructor, protoProps, staticProps) {
					if (protoProps) defineProperties(Constructor.prototype, protoProps);
					if (staticProps) defineProperties(Constructor, staticProps);
					return Constructor;
				};
			}();

			panelDefaults = {
				primary: 1,
				open: false,
				showHoverChart: false,
				scrollable: true,
				happy_img: 'public/plugins/Inference_panel/img/happy.svg',
				sad_img: 'public/plugins/Inference_panel/img/sad.svg'
			};

			_export("InferenceCtrl", InferenceCtrl = function (_MetricsPanelCtrl) {
				_inherits(InferenceCtrl, _MetricsPanelCtrl);

				/** @ngInject */

				function InferenceCtrl($scope, $injector) {
					_classCallCheck(this, InferenceCtrl);

					var _this = _possibleConstructorReturn(this, (InferenceCtrl.__proto__ || Object.getPrototypeOf(InferenceCtrl)).call(this, $scope, $injector));

					_.defaultsDeep(_this.panel, panelDefaults);

					_this.events.on('render', _this.onRender.bind(_this));
					_this.events.on('refresh', _this.postRefresh.bind(_this));
					_this.events.on('data-error', _this.onDataError.bind(_this));
					_this.events.on('data-received', _this.onDataReceived.bind(_this));
					_this.events.on('init-edit-mode', _this.onInitEditMode.bind(_this));
					_this.googleChartData = {};
					_this.panel.showHoverChart = false;

					return _this;
				}

				_createClass(InferenceCtrl, [{
					key: "postRefresh",
					value: function postRefresh() {}
				}, {
					key: "onInitEditMode",
					value: function onInitEditMode() {
						this.addEditorTab('Options', './editor.html', 2);
					}
				}, {
					key: "onRender",
					value: function onRender() {
						/* google = window['google'];
      google.charts.load('current', { packages: ['corechart'] }); */
						if ($('#googleChartLoaderScript').length === 0) {
							google = window['google'];
							google.charts.load('46', { packages: ['corechart', 'charteditor', 'gantt'] });
						} else {
							google = window['google'];
						}
					}
				}, {
					key: "onDataReceived",
					value: function onDataReceived(dataList) {
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
							for (var i = 0; i < this.jsonArrtoStr.length; i++) {
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
									} else if (this.data["sentiment"] == "POSITIVE" && this.data["trendline"] == "Low to High") {
										this.vectorProperty["color"] = "green";
										this.vectorProperty["type"] = "increased";

										this.googleChartData[this.data["kpiId"]].push("green");
									} else if (this.data["sentiment"] == "NEGATIVE" && this.data["trendline"] == "Low to High") {
										this.vectorProperty["color"] = "red";
										this.vectorProperty["type"] = "increased";

										this.googleChartData[this.data["kpiId"]].push("red");
									} else if (this.data["sentiment"] == "NEGATIVE" && this.data["trendline"] == "High to Low") {
										this.vectorProperty["color"] = "red";
										this.vectorProperty["type"] = "decreased";

										this.googleChartData[this.data["kpiId"]].push("red");
									} else if (this.data["sentiment"] == "NEUTRAL") {
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
				}, {
					key: "onDataError",
					value: function onDataError() {}
				}, {
					key: "openCollapse",
					value: function openCollapse() {
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
				}, {
					key: "filterData",
					value: function filterData(x) {

						this.drawArr = [];
						this.col = ['Value', '', { role: 'style' }];
						this.drawArr.push(this.col);

						this.c = this.googleChartData[x][this.googleChartData[x].length - 1];
						for (var data in this.googleChartData[x]) {
							this.arr = [];
							this.arr.push(this.googleChartData[x][data]["resultDate"]);
							this.arr.push(this.googleChartData[x][data]["value"]);
							this.arr.push(this.c);
							this.drawArr.push(this.arr);
						}

						return this.drawArr;
					}
				}, {
					key: "hoverOn",
					value: function hoverOn(x, title, vectorName) {

						this.panel.primary += 1;
						this.panel.showHoverChart = true;
						var options = { 'title': title };
						this.arr = [];

						// this.arr.push(this.filterData(x));
						var data = new google.visualization.DataTable();
						var data = google.visualization.arrayToDataTable(this.filterData(x));
						if ($('#googleChartLoaderScript').length === 0) {
							google.charts.load('46', { packages: ['corechart', 'charteditor', 'gantt'] });
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
						} else {
							options['backgroundColor'] = '#212124';
							options['legendTextStyle'] = { color: 'white' };
							options['titleTextStyle'] = { color: 'white' };
							options['hAxis'] = { textStyle: { color: 'white' } };
							options['vAxis'] = { textStyle: { color: 'white' } };
							this.panel.textColor = 'white';
						}
						chart.draw(data, options);
					}
				}, {
					key: "hoverOut",
					value: function hoverOut(x) {
						document.getElementById(x).style.display = 'none';

						this.panel.showHoverChart = false;
					}
				}, {
					key: "drawCharts",
					value: function drawCharts() {}
				}]);

				return InferenceCtrl;
			}(MetricsPanelCtrl));

			_export("InferenceCtrl", InferenceCtrl);

			InferenceCtrl.templateUrl = 'module.html';
		}
	};
});
//# sourceMappingURL=inferenceCtrl.js.map
