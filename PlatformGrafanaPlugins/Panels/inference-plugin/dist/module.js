define(["@grafana/data","@grafana/runtime","emotion","react"], (__WEBPACK_EXTERNAL_MODULE__grafana_data__, __WEBPACK_EXTERNAL_MODULE__grafana_runtime__, __WEBPACK_EXTERNAL_MODULE_emotion__, __WEBPACK_EXTERNAL_MODULE_react__) => { return /******/ (() => { // webpackBootstrap
/******/ 	"use strict";
/******/ 	var __webpack_modules__ = ({

/***/ "./InferencePanel.tsx":
/*!****************************!*\
  !*** ./InferencePanel.tsx ***!
  \****************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   InferencePanel: () => (/* binding */ InferencePanel)
/* harmony export */ });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var emotion__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! emotion */ "emotion");
/* harmony import */ var emotion__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(emotion__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _inferenceUtil__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./inferenceUtil */ "./inferenceUtil.tsx");
/* harmony import */ var _inferenceLayout_css__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./inferenceLayout.css */ "./inferenceLayout.css");
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @grafana/runtime */ "@grafana/runtime");
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(_grafana_runtime__WEBPACK_IMPORTED_MODULE_4__);
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
var _img, _img2, _templateObject;
function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }
function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }
function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) arr2[i] = arr[i]; return arr2; }
function _taggedTemplateLiteral(strings, raw) { if (!raw) { raw = strings.slice(0); } return Object.freeze(Object.defineProperties(strings, { raw: { value: Object.freeze(raw) } })); }
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }
function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); Object.defineProperty(Constructor, "prototype", { writable: false }); return Constructor; }
function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); Object.defineProperty(subClass, "prototype", { writable: false }); if (superClass) _setPrototypeOf(subClass, superClass); }
function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf ? Object.setPrototypeOf.bind() : function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }
function _createSuper(Derived) { var hasNativeReflectConstruct = _isNativeReflectConstruct(); return function _createSuperInternal() { var Super = _getPrototypeOf(Derived), result; if (hasNativeReflectConstruct) { var NewTarget = _getPrototypeOf(this).constructor; result = Reflect.construct(Super, arguments, NewTarget); } else { result = Super.apply(this, arguments); } return _possibleConstructorReturn(this, result); }; }
function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } else if (call !== void 0) { throw new TypeError("Derived constructors may only return object or undefined"); } return _assertThisInitialized(self); }
function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }
function _isNativeReflectConstruct() { if (typeof Reflect === "undefined" || !Reflect.construct) return false; if (Reflect.construct.sham) return false; if (typeof Proxy === "function") return true; try { Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function () {})); return true; } catch (e) { return false; } }
function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf.bind() : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }


//import $ from 'jquery';



var fusionID;
var gID;
var chartData;
var chartCaption;
var chartType;
var palettecolors;
var InferencePanel = /*#__PURE__*/function (_React$Component) {
  _inherits(InferencePanel, _React$Component);
  var _super = _createSuper(InferencePanel);
  function InferencePanel(props) {
    _classCallCheck(this, InferencePanel);
    return _super.call(this, props);
  }
  _createClass(InferencePanel, [{
    key: "render",
    value: function render() {
      var _this = this;
      var options = this.props.options;
      fusionID = 'insights-inference-fusion' + this.props.id;
      gID = 'insights-inference-google' + this.props.id;
      var data = (0,_inferenceUtil__WEBPACK_IMPORTED_MODULE_2__.processData)(this.props);
      //console.log('processed data--', data);
      var google = window.google;
      //console.log('google--',google);
      google.charts.load('46', {
        'packages': ['corechart', 'charteditor', 'gantt']
      });
      google.charts.setOnLoadCallback(this.googleChart);
      var inferenceLists;
      if (data.length === 0) {
        inferenceLists = "No Records found!";
      } else {
        inferenceLists = data[0].data.map(function (link) {
          return link.color === 'green' ? /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
            style: {
              display: 'flex',
              alignItems: 'center'
            },
            onMouseEnter: function onMouseEnter() {
              _this.getInference(link, options);
            }
          }, _img || (_img = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("img", {
            src: "public/img/satisfied.png"
          })), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("span", {
            style: {
              padding: '5px'
            }
          }, link.inference)) : /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
            style: {
              display: 'flex',
              alignItems: 'center'
            },
            onMouseEnter: function onMouseEnter() {
              _this.getInference(link, options);
            }
          }, _img2 || (_img2 = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("img", {
            src: "public/img/dissatisfied.png"
          })), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("span", {
            style: {
              padding: '5px'
            }
          }, link.inference));
        });
      }
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        style: {
          height: '100%',
          width: '100%'
        },
        className: (0,emotion__WEBPACK_IMPORTED_MODULE_1__.cx)("position:relative", (0,emotion__WEBPACK_IMPORTED_MODULE_1__.css)(_templateObject || (_templateObject = _taggedTemplateLiteral(["width: ", "px;height: ", "px;"])), this.props.width, this.props.height))
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "row"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "column",
        id: "inference-lists"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("ul", null, inferenceLists)), options.enableFusion && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "column",
        id: fusionID
      }, this.fusionChart()), !options.enableFusion && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "column",
        id: gID
      })));
    }

    /**Fetch Details on hover of each kpi */
  }, {
    key: "getInference",
    value: function getInference(link, options) {
      //console.log('hovered--',link);
      chartData = link.resultSet;
      chartCaption = link.inference;
      chartType = this.props.options.fusionChartType;
      palettecolors = link.color === 'green' ? '#008000' : '#FF0000';
      //console.log('pale--',palettecolors);
      if (options.enableFusion) {
        this.fusionChart();
      } else {
        this.googleChart(link);
      }
    }

    /*Render Fusioncharts*/
  }, {
    key: "fusionChart",
    value: function fusionChart() {
      var FusionCharts = window.FusionCharts;
      var chartConfig = this.fetchChartConfig();
      FusionCharts.ready(function () {
        var fusioncharts = new FusionCharts(chartConfig);
        fusioncharts.resizeTo('100%', '100%');
        fusioncharts.render();
      });
      return '';
    }

    /*Fetch Fusincharts config and data*/
  }, {
    key: "fetchChartConfig",
    value: function fetchChartConfig() {
      var theme = _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__.config.theme;
      var chartConfig = {
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
            palettecolors: palettecolors
          },
          "data": chartData
        }
      };
      return chartConfig;
    }

    /*Fetch KPI for google and render google chart*/
  }, {
    key: "googleChart",
    value: function googleChart(link) {
      var google = window.google;
      var data = new google.visualization.DataTable();
      if (this === undefined) {
        return;
      }
      data = google.visualization.arrayToDataTable(this.filterData(link));
      /*data.addColumn('string', 'Topping');
      data.addColumn('number', 'Slices');
      data.addRows([
          ['Mushrooms', 3],
          ['Onions', 1],
          ['Olives', 1],
          ['Zucchini', 1],
          ['Pepperoni', 2]
      ]);*/
      var options = {
        'title': chartCaption
      };
      if (window['grafanaBootData'].user.lightTheme) {
        options['backgroundColor'] = _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__.config.theme.colors.bg1;
        options['legendTextStyle'] = {
          color: 'black'
        };
        options['titleTextStyle'] = {
          color: 'black'
        };
        options['hAxis'] = {
          textStyle: {
            color: 'black'
          }
        };
        options['vAxis'] = {
          textStyle: {
            color: 'black'
          }
        };
      } else {
        options['backgroundColor'] = _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__.config.theme.colors.bg1;
        options['legendTextStyle'] = {
          color: 'white'
        };
        options['titleTextStyle'] = {
          color: 'white'
        };
        options['hAxis'] = {
          textStyle: {
            color: 'white'
          }
        };
        options['vAxis'] = {
          textStyle: {
            color: 'white'
          }
        };
      }
      var chart;
      if (this.props.options.googleChartType === 'bar') {
        chart = new google.visualization.BarChart(document.getElementById('insights-inference-google' + this.props.id));
      } else {
        chart = new google.visualization.LineChart(document.getElementById('insights-inference-google' + this.props.id));
      }
      chart.draw(data, options);
    }

    /*Filter kpi for google charts*/
  }, {
    key: "filterData",
    value: function filterData(link) {
      var drawArr = [];
      var col = ['Value', '', {
        role: 'style'
      }];
      drawArr.push(col);
      var color = link.color;
      var _iterator = _createForOfIteratorHelper(link.resultSet),
        _step;
      try {
        for (_iterator.s(); !(_step = _iterator.n()).done;) {
          var data = _step.value;
          var arr = [];
          arr.push(data["label"]);
          arr.push(data["value"]);
          arr.push(color);
          drawArr.push(arr);
        }
      } catch (err) {
        _iterator.e(err);
      } finally {
        _iterator.f();
      }
      return drawArr;
    }
  }]);
  return InferencePanel;
}((react__WEBPACK_IMPORTED_MODULE_0___default().Component));
;

/***/ }),

/***/ "./inferenceUtil.tsx":
/*!***************************!*\
  !*** ./inferenceUtil.tsx ***!
  \***************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   processData: () => (/* binding */ processData)
/* harmony export */ });
function processData(props) {
  var jsonArrtoStr = [];
  if (props.data.state === 'Done') {
    if (props.data.series.length > 0) {
      if (props.data.series[0].source) {
        // For Grafana version < 10.0.0
        jsonArrtoStr = props.data.series[0].source;
      } else {
        // For Grafana version >= 10.0.0
        props.data.series.forEach(function (element) {
          var newObj = {
            "heading": element.fields.find(function (e) {
              return e.name === "heading";
            }).values,
            "inferenceDetails": element.fields.find(function (e) {
              return e.name === "inferenceDetails";
            }).values[0],
            "ranking": element.fields.find(function (e) {
              return e.name === "ranking";
            }).values
          };
          jsonArrtoStr.push(newObj);
        });
      }
    }
  }
  var googleChartData = {};
  var uiResponseArr = [];
  if (jsonArrtoStr.length > 0) {
    for (var i = 0; i < jsonArrtoStr.length; i++) {
      var arr = [];
      var vectorMap = {};
      vectorMap["vectorName"] = jsonArrtoStr[i]["heading"];
      var jsonObjtoStr = jsonArrtoStr[i];
      var _loop = function _loop() {
        var resultArray = [];
        var data = jsonObjtoStr["inferenceDetails"][vector];
        var vectorProperty = {};
        googleChartData[data["kpiId"]] = data["resultSet"];
        vectorProperty["kpi"] = data["kpi"];
        vectorProperty["sentiment"] = data["sentiment"];
        vectorProperty["kpiId"] = data["kpiId"];
        vectorProperty["schedule"] = data["schedule"];
        vectorProperty["trendline"] = data["trendline"];
        vectorProperty["inference"] = data["inference"];
        vectorMap["lastRun"] = data["lastRun"];
        vectorMap["schedule"] = data["schedule"];
        if (data["resultSet"].length !== undefined) {
          data.resultSet.forEach(function (x) {
            return resultArray.push({
              'label': x.resultDate,
              'value': x.value
            });
          });
        }
        vectorProperty["resultSet"] = resultArray;
        if (data["sentiment"] === "POSITIVE" && data["trendline"] === "High to Low") {
          vectorProperty["color"] = "green";
          vectorProperty["type"] = "increased";
          googleChartData[data["kpiId"]].push("green");
        } else if (data["sentiment"] === "POSITIVE" && data["trendline"] === "Low to High") {
          vectorProperty["color"] = "green";
          vectorProperty["type"] = "increased";
          googleChartData[data["kpiId"]].push("green");
        } else if (data["sentiment"] === "NEGATIVE" && data["trendline"] === "Low to High") {
          vectorProperty["color"] = "red";
          vectorProperty["type"] = "increased";
          googleChartData[data["kpiId"]].push("red");
        } else if (data["sentiment"] === "NEGATIVE" && data["trendline"] === "High to Low") {
          vectorProperty["color"] = "red";
          vectorProperty["type"] = "decreased";
          googleChartData[data["kpiId"]].push("red");
        } else if (data["sentiment"] === "NEUTRAL") {
          vectorProperty["color"] = "green";
          vectorProperty["type"] = "same";
          googleChartData[data["kpiId"]].push("green");
        }
        arr.push(vectorProperty);
      };
      for (var vector in jsonObjtoStr["inferenceDetails"]) {
        _loop();
      }
      vectorMap["data"] = arr;
      uiResponseArr.push(vectorMap);
    }
  }
  return uiResponseArr;
}

//Sample data to test inference without datasource
/*jsonArrtoStr = [{
  "heading": "CODEQUALITY",
  "inferenceDetails": [
    {
      "kpi": "Average Complexity",
      "sentiment": "POSITIVE",
      "trendline": "High to Low",
      "action": "AVERAGE",
      "inference": "Average Code Complexity has decreased to 110 from 136",
      "kpiId": 131,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:10 AM",
      "resultSet": [
        {
          "value": 128,
          "resultDate": "Jul 3, 2020 2:41:16 AM"
        },
        {
          "value": 136,
          "resultDate": "Jul 6, 2020 2:41:16 AM"
        },
        {
          "value": 110,
          "resultDate": "Jul 7, 2020 2:41:10 AM"
        },
        "green"
      ]
    },
    {
      "kpi": "Average Duplicated Blocks",
      "sentiment": "NEUTRAL",
      "trendline": "Low to High",
      "action": "AVERAGE",
      "inference": "Average duplicated blocks has remain to 1",
      "kpiId": 132,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:12 AM",
      "resultSet": [
        {
          "value": 1,
          "resultDate": "Jul 3, 2020 2:41:19 AM"
        },
        {
          "value": 1,
          "resultDate": "Jul 6, 2020 2:41:17 AM"
        },
        {
          "value": 1,
          "resultDate": "Jul 7, 2020 2:41:12 AM"
        },
        "green"
      ]
    },
    {
      "kpi": "Number of Quality Passed Blocks",
      "sentiment": "NEGATIVE",
      "trendline": "High to Low",
      "action": "COUNT",
      "inference": "Number of Quality Passed Blocks has decreased to 6 from 7",
      "kpiId": 133,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:13 AM",
      "resultSet": [
        {
          "value": 7,
          "resultDate": "Jul 3, 2020 2:41:20 AM"
        },
        {
          "value": 7,
          "resultDate": "Jul 6, 2020 2:41:19 AM"
        },
        {
          "value": 6,
          "resultDate": "Jul 7, 2020 2:41:13 AM"
        },
        "red"
      ]
    },
    {
      "kpi": "Number of Quality Failed Blocks",
      "sentiment": "NEGATIVE",
      "trendline": "Low to High",
      "action": "COUNT",
      "inference": "Number of Quality Failed Blocks has increased to 11 from 7",
      "kpiId": 134,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:11 AM",
      "resultSet": [
        {
          "value": 8,
          "resultDate": "Jul 3, 2020 2:41:17 AM"
        },
        {
          "value": 7,
          "resultDate": "Jul 6, 2020 2:41:16 AM"
        },
        {
          "value": 11,
          "resultDate": "Jul 7, 2020 2:41:11 AM"
        },
        "red"
      ]
    },
    {
      "kpi": "Average Code Coverage",
      "sentiment": "NEUTRAL",
      "trendline": "Low to High",
      "action": "AVERAGE",
      "inference": "Average Code Coverage has remain same to 100",
      "kpiId": 135,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:12 AM",
      "resultSet": [
        {
          "value": 100,
          "resultDate": "Jul 3, 2020 2:41:19 AM"
        },
        {
          "value": 100,
          "resultDate": "Jul 6, 2020 2:41:18 AM"
        },
        {
          "value": 100,
          "resultDate": "Jul 7, 2020 2:41:12 AM"
        },
        "green"
      ]
    },
    {
      "kpi": "Number of Successful Sonar Executions",
      "sentiment": "NEGATIVE",
      "trendline": "High to Low",
      "action": "COUNT",
      "inference": "Number of Successful Sonar Executions has decreased to 6 from 7",
      "kpiId": 136,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:13 AM",
      "resultSet": [
        {
          "value": 7,
          "resultDate": "Jul 3, 2020 2:41:20 AM"
        },
        {
          "value": 7,
          "resultDate": "Jul 6, 2020 2:41:19 AM"
        },
        {
          "value": 6,
          "resultDate": "Jul 7, 2020 2:41:13 AM"
        },
        "red"
      ]
    },
    {
      "kpi": "Number of Failed Sonar Executions",
      "sentiment": "NEGATIVE",
      "trendline": "Low to High",
      "action": "COUNT",
      "inference": "Number of Failed Sonar Executions has increased to 5 from 1",
      "kpiId": 137,
      "schedule": "DAILY",
      "lastRun": "Jul 7, 2020 2:41:11 AM",
      "resultSet": [
        {
          "value": 2,
          "resultDate": "Jul 3, 2020 2:41:17 AM"
        },
        {
          "value": 1,
          "resultDate": "Jul 6, 2020 2:41:16 AM"
        },
        {
          "value": 5,
          "resultDate": "Jul 7, 2020 2:41:11 AM"
        },
        "red"
      ]
    }
  ],
  "ranking": 1
}];*/

/***/ }),

/***/ "../node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!../node_modules/postcss-loader/dist/cjs.js??ruleSet[1].rules[3].use[2]!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css":
/*!***************************************************************************************************************************************************************************************************************!*\
  !*** ../node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!../node_modules/postcss-loader/dist/cjs.js??ruleSet[1].rules[3].use[2]!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css ***!
  \***************************************************************************************************************************************************************************************************************/
/***/ ((module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "default": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _node_modules_css_loader_dist_runtime_sourceMaps_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../node_modules/css-loader/dist/runtime/sourceMaps.js */ "../node_modules/css-loader/dist/runtime/sourceMaps.js");
/* harmony import */ var _node_modules_css_loader_dist_runtime_sourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_sourceMaps_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../node_modules/css-loader/dist/runtime/api.js */ "../node_modules/css-loader/dist/runtime/api.js");
/* harmony import */ var _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1__);
// Imports


var ___CSS_LOADER_EXPORT___ = _node_modules_css_loader_dist_runtime_api_js__WEBPACK_IMPORTED_MODULE_1___default()((_node_modules_css_loader_dist_runtime_sourceMaps_js__WEBPACK_IMPORTED_MODULE_0___default()));
// Module
___CSS_LOADER_EXPORT___.push([module.id, "* {\n  box-sizing: border-box;\n}\n\n/* Create two equal columns that floats next to each other */\n.column {\n  float: left;\n  width: 50%;\n  padding: 10px;\n}\n\n/* Clear floats after the columns */\n.row:after {\n  content: \"\";\n  display: table;\n  clear: both;\n}\n\n/* Responsive layout - makes the two columns stack on top of each other instead of next to each other */\n@media screen and (max-width: 600px) {\n  .column {\n    width: 100%;\n  }\n}", "",{"version":3,"sources":["webpack://./inferenceLayout.css"],"names":[],"mappings":"AAAA;EACI,sBAAA;AACJ;;AAEE,4DAAA;AACA;EACE,WAAA;EACA,UAAA;EACA,aAAA;AACJ;;AAEE,mCAAA;AACA;EACE,WAAA;EACA,cAAA;EACA,WAAA;AACJ;;AAEE,uGAAA;AACA;EACE;IACE,WAAA;EACJ;AACF","sourcesContent":["* {\r\n    box-sizing: border-box;\r\n  }\r\n  \r\n  /* Create two equal columns that floats next to each other */\r\n  .column {\r\n    float: left;\r\n    width: 50%;\r\n    padding: 10px;\r\n  }\r\n  \r\n  /* Clear floats after the columns */\r\n  .row:after {\r\n    content: \"\";\r\n    display: table;\r\n    clear: both;\r\n  }\r\n  \r\n  /* Responsive layout - makes the two columns stack on top of each other instead of next to each other */\r\n  @media screen and (max-width: 600px) {\r\n    .column {\r\n      width: 100%;\r\n    }\r\n  }\r\n\r\n  "],"sourceRoot":""}]);
// Exports
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (___CSS_LOADER_EXPORT___);


/***/ }),

/***/ "../node_modules/css-loader/dist/runtime/api.js":
/*!******************************************************!*\
  !*** ../node_modules/css-loader/dist/runtime/api.js ***!
  \******************************************************/
/***/ ((module) => {



/*
  MIT License http://www.opensource.org/licenses/mit-license.php
  Author Tobias Koppers @sokra
*/
module.exports = function (cssWithMappingToString) {
  var list = [];

  // return the list of modules as css string
  list.toString = function toString() {
    return this.map(function (item) {
      var content = "";
      var needLayer = typeof item[5] !== "undefined";
      if (item[4]) {
        content += "@supports (".concat(item[4], ") {");
      }
      if (item[2]) {
        content += "@media ".concat(item[2], " {");
      }
      if (needLayer) {
        content += "@layer".concat(item[5].length > 0 ? " ".concat(item[5]) : "", " {");
      }
      content += cssWithMappingToString(item);
      if (needLayer) {
        content += "}";
      }
      if (item[2]) {
        content += "}";
      }
      if (item[4]) {
        content += "}";
      }
      return content;
    }).join("");
  };

  // import a list of modules into the list
  list.i = function i(modules, media, dedupe, supports, layer) {
    if (typeof modules === "string") {
      modules = [[null, modules, undefined]];
    }
    var alreadyImportedModules = {};
    if (dedupe) {
      for (var k = 0; k < this.length; k++) {
        var id = this[k][0];
        if (id != null) {
          alreadyImportedModules[id] = true;
        }
      }
    }
    for (var _k = 0; _k < modules.length; _k++) {
      var item = [].concat(modules[_k]);
      if (dedupe && alreadyImportedModules[item[0]]) {
        continue;
      }
      if (typeof layer !== "undefined") {
        if (typeof item[5] === "undefined") {
          item[5] = layer;
        } else {
          item[1] = "@layer".concat(item[5].length > 0 ? " ".concat(item[5]) : "", " {").concat(item[1], "}");
          item[5] = layer;
        }
      }
      if (media) {
        if (!item[2]) {
          item[2] = media;
        } else {
          item[1] = "@media ".concat(item[2], " {").concat(item[1], "}");
          item[2] = media;
        }
      }
      if (supports) {
        if (!item[4]) {
          item[4] = "".concat(supports);
        } else {
          item[1] = "@supports (".concat(item[4], ") {").concat(item[1], "}");
          item[4] = supports;
        }
      }
      list.push(item);
    }
  };
  return list;
};

/***/ }),

/***/ "../node_modules/css-loader/dist/runtime/sourceMaps.js":
/*!*************************************************************!*\
  !*** ../node_modules/css-loader/dist/runtime/sourceMaps.js ***!
  \*************************************************************/
/***/ ((module) => {



module.exports = function (item) {
  var content = item[1];
  var cssMapping = item[3];
  if (!cssMapping) {
    return content;
  }
  if (typeof btoa === "function") {
    var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(cssMapping))));
    var data = "sourceMappingURL=data:application/json;charset=utf-8;base64,".concat(base64);
    var sourceMapping = "/*# ".concat(data, " */");
    var sourceURLs = cssMapping.sources.map(function (source) {
      return "/*# sourceURL=".concat(cssMapping.sourceRoot || "").concat(source, " */");
    });
    return [content].concat(sourceURLs).concat([sourceMapping]).join("\n");
  }
  return [content].join("\n");
};

/***/ }),

/***/ "./inferenceLayout.css":
/*!*****************************!*\
  !*** ./inferenceLayout.css ***!
  \*****************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "default": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var _node_modules_style_loader_dist_runtime_injectStylesIntoStyleTag_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! !../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js */ "../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js");
/* harmony import */ var _node_modules_style_loader_dist_runtime_injectStylesIntoStyleTag_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_style_loader_dist_runtime_injectStylesIntoStyleTag_js__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _node_modules_style_loader_dist_runtime_styleDomAPI_js__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! !../node_modules/style-loader/dist/runtime/styleDomAPI.js */ "../node_modules/style-loader/dist/runtime/styleDomAPI.js");
/* harmony import */ var _node_modules_style_loader_dist_runtime_styleDomAPI_js__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_node_modules_style_loader_dist_runtime_styleDomAPI_js__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _node_modules_style_loader_dist_runtime_insertBySelector_js__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! !../node_modules/style-loader/dist/runtime/insertBySelector.js */ "../node_modules/style-loader/dist/runtime/insertBySelector.js");
/* harmony import */ var _node_modules_style_loader_dist_runtime_insertBySelector_js__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(_node_modules_style_loader_dist_runtime_insertBySelector_js__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _node_modules_style_loader_dist_runtime_setAttributesWithoutAttributes_js__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! !../node_modules/style-loader/dist/runtime/setAttributesWithoutAttributes.js */ "../node_modules/style-loader/dist/runtime/setAttributesWithoutAttributes.js");
/* harmony import */ var _node_modules_style_loader_dist_runtime_setAttributesWithoutAttributes_js__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(_node_modules_style_loader_dist_runtime_setAttributesWithoutAttributes_js__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var _node_modules_style_loader_dist_runtime_insertStyleElement_js__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! !../node_modules/style-loader/dist/runtime/insertStyleElement.js */ "../node_modules/style-loader/dist/runtime/insertStyleElement.js");
/* harmony import */ var _node_modules_style_loader_dist_runtime_insertStyleElement_js__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(_node_modules_style_loader_dist_runtime_insertStyleElement_js__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var _node_modules_style_loader_dist_runtime_styleTagTransform_js__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! !../node_modules/style-loader/dist/runtime/styleTagTransform.js */ "../node_modules/style-loader/dist/runtime/styleTagTransform.js");
/* harmony import */ var _node_modules_style_loader_dist_runtime_styleTagTransform_js__WEBPACK_IMPORTED_MODULE_5___default = /*#__PURE__*/__webpack_require__.n(_node_modules_style_loader_dist_runtime_styleTagTransform_js__WEBPACK_IMPORTED_MODULE_5__);
/* harmony import */ var _node_modules_css_loader_dist_cjs_js_ruleSet_1_rules_3_use_1_node_modules_postcss_loader_dist_cjs_js_ruleSet_1_rules_3_use_2_node_modules_sass_loader_dist_cjs_js_inferenceLayout_css__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! !!../node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!../node_modules/postcss-loader/dist/cjs.js??ruleSet[1].rules[3].use[2]!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css */ "../node_modules/css-loader/dist/cjs.js??ruleSet[1].rules[3].use[1]!../node_modules/postcss-loader/dist/cjs.js??ruleSet[1].rules[3].use[2]!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css");

      
      
      
      
      
      
      
      
      

var options = {};

options.styleTagTransform = (_node_modules_style_loader_dist_runtime_styleTagTransform_js__WEBPACK_IMPORTED_MODULE_5___default());
options.setAttributes = (_node_modules_style_loader_dist_runtime_setAttributesWithoutAttributes_js__WEBPACK_IMPORTED_MODULE_3___default());

      options.insert = _node_modules_style_loader_dist_runtime_insertBySelector_js__WEBPACK_IMPORTED_MODULE_2___default().bind(null, "head");
    
options.domAPI = (_node_modules_style_loader_dist_runtime_styleDomAPI_js__WEBPACK_IMPORTED_MODULE_1___default());
options.insertStyleElement = (_node_modules_style_loader_dist_runtime_insertStyleElement_js__WEBPACK_IMPORTED_MODULE_4___default());

var update = _node_modules_style_loader_dist_runtime_injectStylesIntoStyleTag_js__WEBPACK_IMPORTED_MODULE_0___default()(_node_modules_css_loader_dist_cjs_js_ruleSet_1_rules_3_use_1_node_modules_postcss_loader_dist_cjs_js_ruleSet_1_rules_3_use_2_node_modules_sass_loader_dist_cjs_js_inferenceLayout_css__WEBPACK_IMPORTED_MODULE_6__["default"], options);




       /* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (_node_modules_css_loader_dist_cjs_js_ruleSet_1_rules_3_use_1_node_modules_postcss_loader_dist_cjs_js_ruleSet_1_rules_3_use_2_node_modules_sass_loader_dist_cjs_js_inferenceLayout_css__WEBPACK_IMPORTED_MODULE_6__["default"] && _node_modules_css_loader_dist_cjs_js_ruleSet_1_rules_3_use_1_node_modules_postcss_loader_dist_cjs_js_ruleSet_1_rules_3_use_2_node_modules_sass_loader_dist_cjs_js_inferenceLayout_css__WEBPACK_IMPORTED_MODULE_6__["default"].locals ? _node_modules_css_loader_dist_cjs_js_ruleSet_1_rules_3_use_1_node_modules_postcss_loader_dist_cjs_js_ruleSet_1_rules_3_use_2_node_modules_sass_loader_dist_cjs_js_inferenceLayout_css__WEBPACK_IMPORTED_MODULE_6__["default"].locals : undefined);


/***/ }),

/***/ "../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js":
/*!*****************************************************************************!*\
  !*** ../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js ***!
  \*****************************************************************************/
/***/ ((module) => {



var stylesInDOM = [];

function getIndexByIdentifier(identifier) {
  var result = -1;

  for (var i = 0; i < stylesInDOM.length; i++) {
    if (stylesInDOM[i].identifier === identifier) {
      result = i;
      break;
    }
  }

  return result;
}

function modulesToDom(list, options) {
  var idCountMap = {};
  var identifiers = [];

  for (var i = 0; i < list.length; i++) {
    var item = list[i];
    var id = options.base ? item[0] + options.base : item[0];
    var count = idCountMap[id] || 0;
    var identifier = "".concat(id, " ").concat(count);
    idCountMap[id] = count + 1;
    var indexByIdentifier = getIndexByIdentifier(identifier);
    var obj = {
      css: item[1],
      media: item[2],
      sourceMap: item[3],
      supports: item[4],
      layer: item[5]
    };

    if (indexByIdentifier !== -1) {
      stylesInDOM[indexByIdentifier].references++;
      stylesInDOM[indexByIdentifier].updater(obj);
    } else {
      var updater = addElementStyle(obj, options);
      options.byIndex = i;
      stylesInDOM.splice(i, 0, {
        identifier: identifier,
        updater: updater,
        references: 1
      });
    }

    identifiers.push(identifier);
  }

  return identifiers;
}

function addElementStyle(obj, options) {
  var api = options.domAPI(options);
  api.update(obj);

  var updater = function updater(newObj) {
    if (newObj) {
      if (newObj.css === obj.css && newObj.media === obj.media && newObj.sourceMap === obj.sourceMap && newObj.supports === obj.supports && newObj.layer === obj.layer) {
        return;
      }

      api.update(obj = newObj);
    } else {
      api.remove();
    }
  };

  return updater;
}

module.exports = function (list, options) {
  options = options || {};
  list = list || [];
  var lastIdentifiers = modulesToDom(list, options);
  return function update(newList) {
    newList = newList || [];

    for (var i = 0; i < lastIdentifiers.length; i++) {
      var identifier = lastIdentifiers[i];
      var index = getIndexByIdentifier(identifier);
      stylesInDOM[index].references--;
    }

    var newLastIdentifiers = modulesToDom(newList, options);

    for (var _i = 0; _i < lastIdentifiers.length; _i++) {
      var _identifier = lastIdentifiers[_i];

      var _index = getIndexByIdentifier(_identifier);

      if (stylesInDOM[_index].references === 0) {
        stylesInDOM[_index].updater();

        stylesInDOM.splice(_index, 1);
      }
    }

    lastIdentifiers = newLastIdentifiers;
  };
};

/***/ }),

/***/ "../node_modules/style-loader/dist/runtime/insertBySelector.js":
/*!*********************************************************************!*\
  !*** ../node_modules/style-loader/dist/runtime/insertBySelector.js ***!
  \*********************************************************************/
/***/ ((module) => {



var memo = {};
/* istanbul ignore next  */

function getTarget(target) {
  if (typeof memo[target] === "undefined") {
    var styleTarget = document.querySelector(target); // Special case to return head of iframe instead of iframe itself

    if (window.HTMLIFrameElement && styleTarget instanceof window.HTMLIFrameElement) {
      try {
        // This will throw an exception if access to iframe is blocked
        // due to cross-origin restrictions
        styleTarget = styleTarget.contentDocument.head;
      } catch (e) {
        // istanbul ignore next
        styleTarget = null;
      }
    }

    memo[target] = styleTarget;
  }

  return memo[target];
}
/* istanbul ignore next  */


function insertBySelector(insert, style) {
  var target = getTarget(insert);

  if (!target) {
    throw new Error("Couldn't find a style target. This probably means that the value for the 'insert' parameter is invalid.");
  }

  target.appendChild(style);
}

module.exports = insertBySelector;

/***/ }),

/***/ "../node_modules/style-loader/dist/runtime/insertStyleElement.js":
/*!***********************************************************************!*\
  !*** ../node_modules/style-loader/dist/runtime/insertStyleElement.js ***!
  \***********************************************************************/
/***/ ((module) => {



/* istanbul ignore next  */
function insertStyleElement(options) {
  var element = document.createElement("style");
  options.setAttributes(element, options.attributes);
  options.insert(element, options.options);
  return element;
}

module.exports = insertStyleElement;

/***/ }),

/***/ "../node_modules/style-loader/dist/runtime/setAttributesWithoutAttributes.js":
/*!***********************************************************************************!*\
  !*** ../node_modules/style-loader/dist/runtime/setAttributesWithoutAttributes.js ***!
  \***********************************************************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {



/* istanbul ignore next  */
function setAttributesWithoutAttributes(styleElement) {
  var nonce =  true ? __webpack_require__.nc : 0;

  if (nonce) {
    styleElement.setAttribute("nonce", nonce);
  }
}

module.exports = setAttributesWithoutAttributes;

/***/ }),

/***/ "../node_modules/style-loader/dist/runtime/styleDomAPI.js":
/*!****************************************************************!*\
  !*** ../node_modules/style-loader/dist/runtime/styleDomAPI.js ***!
  \****************************************************************/
/***/ ((module) => {



/* istanbul ignore next  */
function apply(styleElement, options, obj) {
  var css = "";

  if (obj.supports) {
    css += "@supports (".concat(obj.supports, ") {");
  }

  if (obj.media) {
    css += "@media ".concat(obj.media, " {");
  }

  var needLayer = typeof obj.layer !== "undefined";

  if (needLayer) {
    css += "@layer".concat(obj.layer.length > 0 ? " ".concat(obj.layer) : "", " {");
  }

  css += obj.css;

  if (needLayer) {
    css += "}";
  }

  if (obj.media) {
    css += "}";
  }

  if (obj.supports) {
    css += "}";
  }

  var sourceMap = obj.sourceMap;

  if (sourceMap && typeof btoa !== "undefined") {
    css += "\n/*# sourceMappingURL=data:application/json;base64,".concat(btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))), " */");
  } // For old IE

  /* istanbul ignore if  */


  options.styleTagTransform(css, styleElement, options.options);
}

function removeStyleElement(styleElement) {
  // istanbul ignore if
  if (styleElement.parentNode === null) {
    return false;
  }

  styleElement.parentNode.removeChild(styleElement);
}
/* istanbul ignore next  */


function domAPI(options) {
  var styleElement = options.insertStyleElement(options);
  return {
    update: function update(obj) {
      apply(styleElement, options, obj);
    },
    remove: function remove() {
      removeStyleElement(styleElement);
    }
  };
}

module.exports = domAPI;

/***/ }),

/***/ "../node_modules/style-loader/dist/runtime/styleTagTransform.js":
/*!**********************************************************************!*\
  !*** ../node_modules/style-loader/dist/runtime/styleTagTransform.js ***!
  \**********************************************************************/
/***/ ((module) => {



/* istanbul ignore next  */
function styleTagTransform(css, styleElement) {
  if (styleElement.styleSheet) {
    styleElement.styleSheet.cssText = css;
  } else {
    while (styleElement.firstChild) {
      styleElement.removeChild(styleElement.firstChild);
    }

    styleElement.appendChild(document.createTextNode(css));
  }
}

module.exports = styleTagTransform;

/***/ }),

/***/ "@grafana/data":
/*!********************************!*\
  !*** external "@grafana/data" ***!
  \********************************/
/***/ ((module) => {

module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_data__;

/***/ }),

/***/ "@grafana/runtime":
/*!***********************************!*\
  !*** external "@grafana/runtime" ***!
  \***********************************/
/***/ ((module) => {

module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_runtime__;

/***/ }),

/***/ "emotion":
/*!**************************!*\
  !*** external "emotion" ***!
  \**************************/
/***/ ((module) => {

module.exports = __WEBPACK_EXTERNAL_MODULE_emotion__;

/***/ }),

/***/ "react":
/*!************************!*\
  !*** external "react" ***!
  \************************/
/***/ ((module) => {

module.exports = __WEBPACK_EXTERNAL_MODULE_react__;

/***/ })

/******/ 	});
/************************************************************************/
/******/ 	// The module cache
/******/ 	var __webpack_module_cache__ = {};
/******/ 	
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/ 		// Check if module is in cache
/******/ 		var cachedModule = __webpack_module_cache__[moduleId];
/******/ 		if (cachedModule !== undefined) {
/******/ 			return cachedModule.exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = __webpack_module_cache__[moduleId] = {
/******/ 			id: moduleId,
/******/ 			// no module.loaded needed
/******/ 			exports: {}
/******/ 		};
/******/ 	
/******/ 		// Execute the module function
/******/ 		__webpack_modules__[moduleId](module, module.exports, __webpack_require__);
/******/ 	
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/ 	
/************************************************************************/
/******/ 	/* webpack/runtime/compat get default export */
/******/ 	(() => {
/******/ 		// getDefaultExport function for compatibility with non-harmony modules
/******/ 		__webpack_require__.n = (module) => {
/******/ 			var getter = module && module.__esModule ?
/******/ 				() => (module['default']) :
/******/ 				() => (module);
/******/ 			__webpack_require__.d(getter, { a: getter });
/******/ 			return getter;
/******/ 		};
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/define property getters */
/******/ 	(() => {
/******/ 		// define getter functions for harmony exports
/******/ 		__webpack_require__.d = (exports, definition) => {
/******/ 			for(var key in definition) {
/******/ 				if(__webpack_require__.o(definition, key) && !__webpack_require__.o(exports, key)) {
/******/ 					Object.defineProperty(exports, key, { enumerable: true, get: definition[key] });
/******/ 				}
/******/ 			}
/******/ 		};
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/hasOwnProperty shorthand */
/******/ 	(() => {
/******/ 		__webpack_require__.o = (obj, prop) => (Object.prototype.hasOwnProperty.call(obj, prop))
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/make namespace object */
/******/ 	(() => {
/******/ 		// define __esModule on exports
/******/ 		__webpack_require__.r = (exports) => {
/******/ 			if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 				Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 			}
/******/ 			Object.defineProperty(exports, '__esModule', { value: true });
/******/ 		};
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/nonce */
/******/ 	(() => {
/******/ 		__webpack_require__.nc = undefined;
/******/ 	})();
/******/ 	
/************************************************************************/
var __webpack_exports__ = {};
// This entry need to be wrapped in an IIFE because it need to be isolated against other modules in the chunk.
(() => {
/*!*******************!*\
  !*** ./module.ts ***!
  \*******************/
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   plugin: () => (/* binding */ plugin)
/* harmony export */ });
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _InferencePanel__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./InferencePanel */ "./InferencePanel.tsx");


var plugin = new _grafana_data__WEBPACK_IMPORTED_MODULE_0__.PanelPlugin(_InferencePanel__WEBPACK_IMPORTED_MODULE_1__.InferencePanel).setPanelOptions(function (builder) {
  return builder
  /*.addTextInput({
    path: 'text',
    name: 'Simple text option',
    description: 'Description of panel option',
    defaultValue: 'Default value of text input option',
  })*/.addBooleanSwitch({
    path: 'enableFusion',
    name: 'Enable Fusion Chart',
    defaultValue: false
  }).addRadio({
    path: 'fusionChartType',
    defaultValue: 'column2d',
    name: 'Fusion Chart Type',
    settings: {
      options: [{
        value: 'column2d',
        label: 'Column'
      }, {
        value: 'bar2d',
        label: 'Bar'
      }, {
        value: 'line',
        label: 'Line'
      }, {
        value: 'area2d',
        label: 'Area'
      }, {
        value: 'pie2d',
        label: 'Pie'
      }, {
        value: 'doughnut2d',
        label: ' Doughnut'
      }, {
        value: 'pareto2d',
        label: 'Pareto'
      }]
    },
    showIf: function showIf(config) {
      return config.enableFusion;
    }
  }).addRadio({
    path: 'googleChartType',
    defaultValue: 'line',
    name: 'Google Chart Type',
    settings: {
      options: [{
        value: 'line',
        label: 'Line'
      }, {
        value: 'bar',
        label: 'Bar'
      }]
    },
    showIf: function showIf(config) {
      return !config.enableFusion;
    }
  });
});
})();

/******/ 	return __webpack_exports__;
/******/ })()
;
});;
//# sourceMappingURL=module.js.map