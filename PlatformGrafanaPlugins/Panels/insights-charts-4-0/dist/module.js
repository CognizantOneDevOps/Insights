define(["@grafana/data","@grafana/runtime","@grafana/ui","emotion","jquery","lodash","react"], function(__WEBPACK_EXTERNAL_MODULE__grafana_data__, __WEBPACK_EXTERNAL_MODULE__grafana_runtime__, __WEBPACK_EXTERNAL_MODULE__grafana_ui__, __WEBPACK_EXTERNAL_MODULE_emotion__, __WEBPACK_EXTERNAL_MODULE_jquery__, __WEBPACK_EXTERNAL_MODULE_lodash__, __WEBPACK_EXTERNAL_MODULE_react__) { return /******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "/";
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = "./module.ts");
/******/ })
/************************************************************************/
/******/ ({

/***/ "../node_modules/grafana-plugin-support/dist/components/FieldSelectEditor.js":
/*!***********************************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/components/FieldSelectEditor.js ***!
  \***********************************************************************************/
/*! exports provided: FieldSelectEditor */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "FieldSelectEditor", function() { return FieldSelectEditor; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @grafana/ui */ "@grafana/ui");
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__);


/**
 * FieldSelectEditor populates a Select with the names of the fields returned by
 * the query.
 *
 * Requires Grafana >=7.0.3. For more information, refer to the following
 * pull request:
 *
 * https://github.com/grafana/grafana/pull/24829
 */
var FieldSelectEditor = function (_a) {
    var _b;
    var item = _a.item, value = _a.value, onChange = _a.onChange, context = _a.context;
    if (context.data && context.data.length > 0) {
        var options = context.data
            .flatMap(function (frame) { return frame.fields; })
            .filter(function (field) {
            var _a, _b;
            return ((_a = item.settings) === null || _a === void 0 ? void 0 : _a.filterByType) ? (_b = item.settings) === null || _b === void 0 ? void 0 : _b.filterByType.some(function (_) { return field.type === _; }) : true;
        })
            .map(function (field) { return ({
            label: field.name,
            value: field.name,
        }); });
        if ((_b = item.settings) === null || _b === void 0 ? void 0 : _b.multi) {
            return (react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__["MultiSelect"], { isClearable: true, isLoading: false, value: value, onChange: function (e) { return onChange(e.map(function (_) { return _.value; })); }, options: options }));
        }
        else {
            return (react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__["Select"], { isClearable: true, isLoading: false, value: value, onChange: function (e) {
                    onChange(e === null || e === void 0 ? void 0 : e.value);
                }, options: options }));
        }
    }
    return react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__["Select"], { onChange: function () { }, disabled: true });
};
//# sourceMappingURL=FieldSelectEditor.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/components/PanelWizard.js":
/*!*****************************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/components/PanelWizard.js ***!
  \*****************************************************************************/
/*! exports provided: PanelWizard, validateFields */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "PanelWizard", function() { return PanelWizard; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "validateFields", function() { return validateFields; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "../node_modules/tslib/tslib.es6.js");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @grafana/ui */ "@grafana/ui");
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var emotion__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! emotion */ "emotion");
/* harmony import */ var emotion__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(emotion__WEBPACK_IMPORTED_MODULE_3__);




var PanelWizard = function (_a) {
    var schema = _a.schema, fields = _a.fields, url = _a.url;
    var theme = Object(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["useTheme"])();
    var report = validateFields(fields !== null && fields !== void 0 ? fields : [], schema);
    return (react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", { style: {
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: "100%",
        } },
        react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["InfoBox"], { title: "Configure your query", url: url, severity: "info", style: { maxWidth: "500px" } },
            react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("p", null, "Define a data source query that return at least the following field" + (report.length > 1 ? "s" : "") + ":"),
            react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null, report.map(function (_a, key) {
                var type = _a.type, description = _a.description, ok = _a.ok;
                return (react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", { key: key, className: Object(emotion__WEBPACK_IMPORTED_MODULE_3__["css"])(templateObject_1 || (templateObject_1 = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__makeTemplateObject"])(["\n                display: flex;\n                align-items: center;\n                padding-bottom: ", ";\n                & > * {\n                  margin-right: ", ";\n                }\n                & > *:last-child {\n                  margin-right: 0;\n                }\n              "], ["\n                display: flex;\n                align-items: center;\n                padding-bottom: ", ";\n                & > * {\n                  margin-right: ", ";\n                }\n                & > *:last-child {\n                  margin-right: 0;\n                }\n              "])), theme.spacing.sm, theme.spacing.sm) },
                    ok ? (react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["Icon"], { name: "check-circle", size: "lg", style: {
                            color: theme.palette.brandSuccess,
                        } })) : (react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["Icon"], { name: "circle", size: "lg", style: {
                            color: theme.colors.linkDisabled,
                        } })),
                    react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["Badge"], { className: Object(emotion__WEBPACK_IMPORTED_MODULE_3__["css"])(templateObject_2 || (templateObject_2 = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__makeTemplateObject"])(["\n                  margin-top: 0;\n                "], ["\n                  margin-top: 0;\n                "]))), text: type.slice(0, 1).toUpperCase() + type.slice(1), color: "blue" }),
                    description && react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("span", null, "" + description)));
            })))));
};
var validateFields = function (fields, schema) {
    var seen = [];
    return schema.map(function (_a) {
        var type = _a.type, description = _a.description;
        var field = fields
            .filter(function (field) { return !seen.includes(field); })
            .find(function (field) { return field.type === type; });
        if (field) {
            seen.push(field);
        }
        return { type: type, description: description, ok: !!field };
    });
};
var templateObject_1, templateObject_2;
//# sourceMappingURL=PanelWizard.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/components/index.js":
/*!***********************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/components/index.js ***!
  \***********************************************************************/
/*! exports provided: FieldSelectEditor, PanelWizard */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _FieldSelectEditor__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./FieldSelectEditor */ "../node_modules/grafana-plugin-support/dist/components/FieldSelectEditor.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "FieldSelectEditor", function() { return _FieldSelectEditor__WEBPACK_IMPORTED_MODULE_0__["FieldSelectEditor"]; });

/* harmony import */ var _PanelWizard__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./PanelWizard */ "../node_modules/grafana-plugin-support/dist/components/PanelWizard.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "PanelWizard", function() { return _PanelWizard__WEBPACK_IMPORTED_MODULE_1__["PanelWizard"]; });



//# sourceMappingURL=index.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/index.js":
/*!************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/index.js ***!
  \************************************************************/
/*! exports provided: FieldSelectEditor, PanelWizard, standardOptionsCompat, fieldConfigWithMinMaxCompat, hasCapability, toTimeField, getFormattedDisplayValue, measureText, getPanelPluginOrFallback */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _components__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./components */ "../node_modules/grafana-plugin-support/dist/components/index.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "FieldSelectEditor", function() { return _components__WEBPACK_IMPORTED_MODULE_0__["FieldSelectEditor"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "PanelWizard", function() { return _components__WEBPACK_IMPORTED_MODULE_0__["PanelWizard"]; });

/* harmony import */ var _utils__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./utils */ "../node_modules/grafana-plugin-support/dist/utils/index.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "standardOptionsCompat", function() { return _utils__WEBPACK_IMPORTED_MODULE_1__["standardOptionsCompat"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "fieldConfigWithMinMaxCompat", function() { return _utils__WEBPACK_IMPORTED_MODULE_1__["fieldConfigWithMinMaxCompat"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "hasCapability", function() { return _utils__WEBPACK_IMPORTED_MODULE_1__["hasCapability"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "toTimeField", function() { return _utils__WEBPACK_IMPORTED_MODULE_1__["toTimeField"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "getFormattedDisplayValue", function() { return _utils__WEBPACK_IMPORTED_MODULE_1__["getFormattedDisplayValue"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "measureText", function() { return _utils__WEBPACK_IMPORTED_MODULE_1__["measureText"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "getPanelPluginOrFallback", function() { return _utils__WEBPACK_IMPORTED_MODULE_1__["getPanelPluginOrFallback"]; });



//# sourceMappingURL=index.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/utils/capability.js":
/*!***********************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/utils/capability.js ***!
  \***********************************************************************/
/*! exports provided: hasCapability, standardOptionsCompat, fieldConfigWithMinMaxCompat */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "hasCapability", function() { return hasCapability; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "standardOptionsCompat", function() { return standardOptionsCompat; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "fieldConfigWithMinMaxCompat", function() { return fieldConfigWithMinMaxCompat; });
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @grafana/runtime */ "@grafana/runtime");
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_grafana_runtime__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var semver__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! semver */ "../node_modules/semver/semver.js");
/* harmony import */ var semver__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(semver__WEBPACK_IMPORTED_MODULE_2__);



/**
 * hasCapability returns true if the currently running version of Grafana
 * supports a given feature. Enables graceful degredation for earlier versions
 * that don't support a given capability.
 */
var hasCapability = function (capability) {
    var version = _grafana_runtime__WEBPACK_IMPORTED_MODULE_1__["config"].buildInfo.version;
    switch (capability) {
        case "color-scheme":
            return Object(semver__WEBPACK_IMPORTED_MODULE_2__["gte"])(version, "7.3.0");
        case "standard-options-object":
            return Object(semver__WEBPACK_IMPORTED_MODULE_2__["gte"])(version, "7.4.0");
        case "custom-editor-context":
            return Object(semver__WEBPACK_IMPORTED_MODULE_2__["gte"])(version, "7.0.3");
        case "field-config-with-min-max":
            return Object(semver__WEBPACK_IMPORTED_MODULE_2__["gte"])(version, "7.4.0");
        default:
            return false;
    }
};
/**
 * standardOptionsCompat translates the standard options API prior to 7.4 to the
 * new API.
 */
var standardOptionsCompat = function (options) {
    if (hasCapability("standard-options-object")) {
        return options.reduce(function (acc, curr) {
            acc[curr] = {};
            return acc;
        }, {});
    }
    return options;
};
/**
 * fieldConfigWithMinMaxCompat uses the getFieldConfigWithMinMax if
 * available, otherwise falls back.
 */
var fieldConfigWithMinMaxCompat = function (field) {
    if (hasCapability("field-config-with-min-max")) {
        return Object(_grafana_data__WEBPACK_IMPORTED_MODULE_0__["getFieldConfigWithMinMax"])(field, true);
    }
    return field.config;
};
//# sourceMappingURL=capability.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/utils/dependency.js":
/*!***********************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/utils/dependency.js ***!
  \***********************************************************************/
/*! exports provided: getPanelPluginOrFallback */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getPanelPluginOrFallback", function() { return getPanelPluginOrFallback; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "../node_modules/tslib/tslib.es6.js");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @grafana/runtime */ "@grafana/runtime");
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(_grafana_runtime__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var semver__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! semver */ "../node_modules/semver/semver.js");
/* harmony import */ var semver__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(semver__WEBPACK_IMPORTED_MODULE_4__);






/**
 * getPluginOrFallback checks if the currently running Grafana version satisfies
 * the plugin requirements. If not, an error is displayed instead.
 *
 * TODO: Is there anyway to detect the plugin id automatically?
 */
var getPanelPluginOrFallback = function (pluginId, plugin) { return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(void 0, void 0, void 0, function () {
    var res, meta;
    return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"])(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, Object(_grafana_runtime__WEBPACK_IMPORTED_MODULE_3__["getBackendSrv"])().datasourceRequest({
                    url: "/public/plugins/" + pluginId + "/plugin.json",
                })];
            case 1:
                res = _a.sent();
                meta = res.data;
                if (Object(semver__WEBPACK_IMPORTED_MODULE_4__["satisfies"])(_grafana_runtime__WEBPACK_IMPORTED_MODULE_3__["config"].buildInfo.version, meta.dependencies.grafanaDependency, {
                    includePrerelease: true,
                })) {
                    return [2 /*return*/, plugin];
                }
                return [2 /*return*/, new _grafana_data__WEBPACK_IMPORTED_MODULE_2__["PanelPlugin"](function (_a) {
                        var width = _a.width, height = _a.height;
                        var style = {
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            height: "100%",
                        };
                        return (react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", { style: {
                                width: width,
                                height: height,
                            } },
                            react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", { style: style },
                                react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null,
                                    react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("p", null,
                                        react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("strong", null, "Error loading: " + meta.id)), "This plugin requires a more recent version of Grafana (" + meta.dependencies.grafanaDependency + ")."))));
                    })];
        }
    });
}); };
//# sourceMappingURL=dependency.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/utils/fields.js":
/*!*******************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/utils/fields.js ***!
  \*******************************************************************/
/*! exports provided: toTimeField */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "toTimeField", function() { return toTimeField; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "../node_modules/tslib/tslib.es6.js");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_1__);


var toTimeField = function (field, timeZone, theme) {
    if ((field === null || field === void 0 ? void 0 : field.type) === _grafana_data__WEBPACK_IMPORTED_MODULE_1__["FieldType"].number) {
        var tmp = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, field), { type: _grafana_data__WEBPACK_IMPORTED_MODULE_1__["FieldType"].time });
        tmp.display = Object(_grafana_data__WEBPACK_IMPORTED_MODULE_1__["getDisplayProcessor"])({ field: tmp, timeZone: timeZone, theme: theme });
        return tmp;
    }
    else if ((field === null || field === void 0 ? void 0 : field.type) === _grafana_data__WEBPACK_IMPORTED_MODULE_1__["FieldType"].string) {
        var tmp = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, field), { type: _grafana_data__WEBPACK_IMPORTED_MODULE_1__["FieldType"].time, values: new _grafana_data__WEBPACK_IMPORTED_MODULE_1__["ArrayVector"](field.values.toArray().map(function (_) {
                return Object(_grafana_data__WEBPACK_IMPORTED_MODULE_1__["dateTimeParse"])(_, {
                    timeZone: timeZone,
                    format: "YYYY-MM-DDTHH:mm:ss.SSSSSSSZ",
                }).valueOf();
            })) });
        tmp.display = Object(_grafana_data__WEBPACK_IMPORTED_MODULE_1__["getDisplayProcessor"])({ field: tmp, timeZone: timeZone, theme: theme });
        return tmp;
    }
    return field;
};
//# sourceMappingURL=fields.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/utils/format.js":
/*!*******************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/utils/format.js ***!
  \*******************************************************************/
/*! exports provided: measureText, getFormattedDisplayValue */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "measureText", function() { return measureText; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getFormattedDisplayValue", function() { return getFormattedDisplayValue; });
var measureText = function (text, size) {
    var canvas = document.createElement("canvas");
    var ctx = canvas.getContext("2d");
    if (ctx) {
        ctx.font = size + " sans-serif";
        return ctx.measureText(text);
    }
    return undefined;
};
var getFormattedDisplayValue = function (displayValue) {
    var _a, _b;
    return displayValue
        ? "" + ((_a = displayValue.prefix) !== null && _a !== void 0 ? _a : "") + displayValue.text + ((_b = displayValue.suffix) !== null && _b !== void 0 ? _b : "")
        : "";
};
//# sourceMappingURL=format.js.map

/***/ }),

/***/ "../node_modules/grafana-plugin-support/dist/utils/index.js":
/*!******************************************************************!*\
  !*** ../node_modules/grafana-plugin-support/dist/utils/index.js ***!
  \******************************************************************/
/*! exports provided: standardOptionsCompat, fieldConfigWithMinMaxCompat, hasCapability, toTimeField, getFormattedDisplayValue, measureText, getPanelPluginOrFallback */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _capability__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./capability */ "../node_modules/grafana-plugin-support/dist/utils/capability.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "standardOptionsCompat", function() { return _capability__WEBPACK_IMPORTED_MODULE_0__["standardOptionsCompat"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "fieldConfigWithMinMaxCompat", function() { return _capability__WEBPACK_IMPORTED_MODULE_0__["fieldConfigWithMinMaxCompat"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "hasCapability", function() { return _capability__WEBPACK_IMPORTED_MODULE_0__["hasCapability"]; });

/* harmony import */ var _fields__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./fields */ "../node_modules/grafana-plugin-support/dist/utils/fields.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "toTimeField", function() { return _fields__WEBPACK_IMPORTED_MODULE_1__["toTimeField"]; });

/* harmony import */ var _format__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./format */ "../node_modules/grafana-plugin-support/dist/utils/format.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "getFormattedDisplayValue", function() { return _format__WEBPACK_IMPORTED_MODULE_2__["getFormattedDisplayValue"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "measureText", function() { return _format__WEBPACK_IMPORTED_MODULE_2__["measureText"]; });

/* harmony import */ var _dependency__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./dependency */ "../node_modules/grafana-plugin-support/dist/utils/dependency.js");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "getPanelPluginOrFallback", function() { return _dependency__WEBPACK_IMPORTED_MODULE_3__["getPanelPluginOrFallback"]; });





//# sourceMappingURL=index.js.map

/***/ }),

/***/ "../node_modules/process/browser.js":
/*!******************************************!*\
  !*** ../node_modules/process/browser.js ***!
  \******************************************/
/*! no static exports found */
/***/ (function(module, exports) {

// shim for using process in browser
var process = module.exports = {};

// cached from whatever global is present so that test runners that stub it
// don't break things.  But we need to wrap it in a try catch in case it is
// wrapped in strict mode code which doesn't define any globals.  It's inside a
// function because try/catches deoptimize in certain engines.

var cachedSetTimeout;
var cachedClearTimeout;

function defaultSetTimout() {
    throw new Error('setTimeout has not been defined');
}
function defaultClearTimeout () {
    throw new Error('clearTimeout has not been defined');
}
(function () {
    try {
        if (typeof setTimeout === 'function') {
            cachedSetTimeout = setTimeout;
        } else {
            cachedSetTimeout = defaultSetTimout;
        }
    } catch (e) {
        cachedSetTimeout = defaultSetTimout;
    }
    try {
        if (typeof clearTimeout === 'function') {
            cachedClearTimeout = clearTimeout;
        } else {
            cachedClearTimeout = defaultClearTimeout;
        }
    } catch (e) {
        cachedClearTimeout = defaultClearTimeout;
    }
} ())
function runTimeout(fun) {
    if (cachedSetTimeout === setTimeout) {
        //normal enviroments in sane situations
        return setTimeout(fun, 0);
    }
    // if setTimeout wasn't available but was latter defined
    if ((cachedSetTimeout === defaultSetTimout || !cachedSetTimeout) && setTimeout) {
        cachedSetTimeout = setTimeout;
        return setTimeout(fun, 0);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedSetTimeout(fun, 0);
    } catch(e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't trust the global object when called normally
            return cachedSetTimeout.call(null, fun, 0);
        } catch(e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error
            return cachedSetTimeout.call(this, fun, 0);
        }
    }


}
function runClearTimeout(marker) {
    if (cachedClearTimeout === clearTimeout) {
        //normal enviroments in sane situations
        return clearTimeout(marker);
    }
    // if clearTimeout wasn't available but was latter defined
    if ((cachedClearTimeout === defaultClearTimeout || !cachedClearTimeout) && clearTimeout) {
        cachedClearTimeout = clearTimeout;
        return clearTimeout(marker);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedClearTimeout(marker);
    } catch (e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't  trust the global object when called normally
            return cachedClearTimeout.call(null, marker);
        } catch (e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error.
            // Some versions of I.E. have different rules for clearTimeout vs setTimeout
            return cachedClearTimeout.call(this, marker);
        }
    }



}
var queue = [];
var draining = false;
var currentQueue;
var queueIndex = -1;

function cleanUpNextTick() {
    if (!draining || !currentQueue) {
        return;
    }
    draining = false;
    if (currentQueue.length) {
        queue = currentQueue.concat(queue);
    } else {
        queueIndex = -1;
    }
    if (queue.length) {
        drainQueue();
    }
}

function drainQueue() {
    if (draining) {
        return;
    }
    var timeout = runTimeout(cleanUpNextTick);
    draining = true;

    var len = queue.length;
    while(len) {
        currentQueue = queue;
        queue = [];
        while (++queueIndex < len) {
            if (currentQueue) {
                currentQueue[queueIndex].run();
            }
        }
        queueIndex = -1;
        len = queue.length;
    }
    currentQueue = null;
    draining = false;
    runClearTimeout(timeout);
}

process.nextTick = function (fun) {
    var args = new Array(arguments.length - 1);
    if (arguments.length > 1) {
        for (var i = 1; i < arguments.length; i++) {
            args[i - 1] = arguments[i];
        }
    }
    queue.push(new Item(fun, args));
    if (queue.length === 1 && !draining) {
        runTimeout(drainQueue);
    }
};

// v8 likes predictible objects
function Item(fun, array) {
    this.fun = fun;
    this.array = array;
}
Item.prototype.run = function () {
    this.fun.apply(null, this.array);
};
process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];
process.version = ''; // empty string to avoid regexp issues
process.versions = {};

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;
process.prependListener = noop;
process.prependOnceListener = noop;

process.listeners = function (name) { return [] }

process.binding = function (name) {
    throw new Error('process.binding is not supported');
};

process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};
process.umask = function() { return 0; };


/***/ }),

/***/ "../node_modules/semver/semver.js":
/*!****************************************!*\
  !*** ../node_modules/semver/semver.js ***!
  \****************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

/* WEBPACK VAR INJECTION */(function(process) {exports = module.exports = SemVer

var debug
/* istanbul ignore next */
if (typeof process === 'object' &&
    process.env &&
    process.env.NODE_DEBUG &&
    /\bsemver\b/i.test(process.env.NODE_DEBUG)) {
  debug = function () {
    var args = Array.prototype.slice.call(arguments, 0)
    args.unshift('SEMVER')
    console.log.apply(console, args)
  }
} else {
  debug = function () {}
}

// Note: this is the semver.org version of the spec that it implements
// Not necessarily the package version of this code.
exports.SEMVER_SPEC_VERSION = '2.0.0'

var MAX_LENGTH = 256
var MAX_SAFE_INTEGER = Number.MAX_SAFE_INTEGER ||
  /* istanbul ignore next */ 9007199254740991

// Max safe segment length for coercion.
var MAX_SAFE_COMPONENT_LENGTH = 16

// The actual regexps go on exports.re
var re = exports.re = []
var src = exports.src = []
var t = exports.tokens = {}
var R = 0

function tok (n) {
  t[n] = R++
}

// The following Regular Expressions can be used for tokenizing,
// validating, and parsing SemVer version strings.

// ## Numeric Identifier
// A single `0`, or a non-zero digit followed by zero or more digits.

tok('NUMERICIDENTIFIER')
src[t.NUMERICIDENTIFIER] = '0|[1-9]\\d*'
tok('NUMERICIDENTIFIERLOOSE')
src[t.NUMERICIDENTIFIERLOOSE] = '[0-9]+'

// ## Non-numeric Identifier
// Zero or more digits, followed by a letter or hyphen, and then zero or
// more letters, digits, or hyphens.

tok('NONNUMERICIDENTIFIER')
src[t.NONNUMERICIDENTIFIER] = '\\d*[a-zA-Z-][a-zA-Z0-9-]*'

// ## Main Version
// Three dot-separated numeric identifiers.

tok('MAINVERSION')
src[t.MAINVERSION] = '(' + src[t.NUMERICIDENTIFIER] + ')\\.' +
                   '(' + src[t.NUMERICIDENTIFIER] + ')\\.' +
                   '(' + src[t.NUMERICIDENTIFIER] + ')'

tok('MAINVERSIONLOOSE')
src[t.MAINVERSIONLOOSE] = '(' + src[t.NUMERICIDENTIFIERLOOSE] + ')\\.' +
                        '(' + src[t.NUMERICIDENTIFIERLOOSE] + ')\\.' +
                        '(' + src[t.NUMERICIDENTIFIERLOOSE] + ')'

// ## Pre-release Version Identifier
// A numeric identifier, or a non-numeric identifier.

tok('PRERELEASEIDENTIFIER')
src[t.PRERELEASEIDENTIFIER] = '(?:' + src[t.NUMERICIDENTIFIER] +
                            '|' + src[t.NONNUMERICIDENTIFIER] + ')'

tok('PRERELEASEIDENTIFIERLOOSE')
src[t.PRERELEASEIDENTIFIERLOOSE] = '(?:' + src[t.NUMERICIDENTIFIERLOOSE] +
                                 '|' + src[t.NONNUMERICIDENTIFIER] + ')'

// ## Pre-release Version
// Hyphen, followed by one or more dot-separated pre-release version
// identifiers.

tok('PRERELEASE')
src[t.PRERELEASE] = '(?:-(' + src[t.PRERELEASEIDENTIFIER] +
                  '(?:\\.' + src[t.PRERELEASEIDENTIFIER] + ')*))'

tok('PRERELEASELOOSE')
src[t.PRERELEASELOOSE] = '(?:-?(' + src[t.PRERELEASEIDENTIFIERLOOSE] +
                       '(?:\\.' + src[t.PRERELEASEIDENTIFIERLOOSE] + ')*))'

// ## Build Metadata Identifier
// Any combination of digits, letters, or hyphens.

tok('BUILDIDENTIFIER')
src[t.BUILDIDENTIFIER] = '[0-9A-Za-z-]+'

// ## Build Metadata
// Plus sign, followed by one or more period-separated build metadata
// identifiers.

tok('BUILD')
src[t.BUILD] = '(?:\\+(' + src[t.BUILDIDENTIFIER] +
             '(?:\\.' + src[t.BUILDIDENTIFIER] + ')*))'

// ## Full Version String
// A main version, followed optionally by a pre-release version and
// build metadata.

// Note that the only major, minor, patch, and pre-release sections of
// the version string are capturing groups.  The build metadata is not a
// capturing group, because it should not ever be used in version
// comparison.

tok('FULL')
tok('FULLPLAIN')
src[t.FULLPLAIN] = 'v?' + src[t.MAINVERSION] +
                  src[t.PRERELEASE] + '?' +
                  src[t.BUILD] + '?'

src[t.FULL] = '^' + src[t.FULLPLAIN] + '$'

// like full, but allows v1.2.3 and =1.2.3, which people do sometimes.
// also, 1.0.0alpha1 (prerelease without the hyphen) which is pretty
// common in the npm registry.
tok('LOOSEPLAIN')
src[t.LOOSEPLAIN] = '[v=\\s]*' + src[t.MAINVERSIONLOOSE] +
                  src[t.PRERELEASELOOSE] + '?' +
                  src[t.BUILD] + '?'

tok('LOOSE')
src[t.LOOSE] = '^' + src[t.LOOSEPLAIN] + '$'

tok('GTLT')
src[t.GTLT] = '((?:<|>)?=?)'

// Something like "2.*" or "1.2.x".
// Note that "x.x" is a valid xRange identifer, meaning "any version"
// Only the first item is strictly required.
tok('XRANGEIDENTIFIERLOOSE')
src[t.XRANGEIDENTIFIERLOOSE] = src[t.NUMERICIDENTIFIERLOOSE] + '|x|X|\\*'
tok('XRANGEIDENTIFIER')
src[t.XRANGEIDENTIFIER] = src[t.NUMERICIDENTIFIER] + '|x|X|\\*'

tok('XRANGEPLAIN')
src[t.XRANGEPLAIN] = '[v=\\s]*(' + src[t.XRANGEIDENTIFIER] + ')' +
                   '(?:\\.(' + src[t.XRANGEIDENTIFIER] + ')' +
                   '(?:\\.(' + src[t.XRANGEIDENTIFIER] + ')' +
                   '(?:' + src[t.PRERELEASE] + ')?' +
                   src[t.BUILD] + '?' +
                   ')?)?'

tok('XRANGEPLAINLOOSE')
src[t.XRANGEPLAINLOOSE] = '[v=\\s]*(' + src[t.XRANGEIDENTIFIERLOOSE] + ')' +
                        '(?:\\.(' + src[t.XRANGEIDENTIFIERLOOSE] + ')' +
                        '(?:\\.(' + src[t.XRANGEIDENTIFIERLOOSE] + ')' +
                        '(?:' + src[t.PRERELEASELOOSE] + ')?' +
                        src[t.BUILD] + '?' +
                        ')?)?'

tok('XRANGE')
src[t.XRANGE] = '^' + src[t.GTLT] + '\\s*' + src[t.XRANGEPLAIN] + '$'
tok('XRANGELOOSE')
src[t.XRANGELOOSE] = '^' + src[t.GTLT] + '\\s*' + src[t.XRANGEPLAINLOOSE] + '$'

// Coercion.
// Extract anything that could conceivably be a part of a valid semver
tok('COERCE')
src[t.COERCE] = '(^|[^\\d])' +
              '(\\d{1,' + MAX_SAFE_COMPONENT_LENGTH + '})' +
              '(?:\\.(\\d{1,' + MAX_SAFE_COMPONENT_LENGTH + '}))?' +
              '(?:\\.(\\d{1,' + MAX_SAFE_COMPONENT_LENGTH + '}))?' +
              '(?:$|[^\\d])'
tok('COERCERTL')
re[t.COERCERTL] = new RegExp(src[t.COERCE], 'g')

// Tilde ranges.
// Meaning is "reasonably at or greater than"
tok('LONETILDE')
src[t.LONETILDE] = '(?:~>?)'

tok('TILDETRIM')
src[t.TILDETRIM] = '(\\s*)' + src[t.LONETILDE] + '\\s+'
re[t.TILDETRIM] = new RegExp(src[t.TILDETRIM], 'g')
var tildeTrimReplace = '$1~'

tok('TILDE')
src[t.TILDE] = '^' + src[t.LONETILDE] + src[t.XRANGEPLAIN] + '$'
tok('TILDELOOSE')
src[t.TILDELOOSE] = '^' + src[t.LONETILDE] + src[t.XRANGEPLAINLOOSE] + '$'

// Caret ranges.
// Meaning is "at least and backwards compatible with"
tok('LONECARET')
src[t.LONECARET] = '(?:\\^)'

tok('CARETTRIM')
src[t.CARETTRIM] = '(\\s*)' + src[t.LONECARET] + '\\s+'
re[t.CARETTRIM] = new RegExp(src[t.CARETTRIM], 'g')
var caretTrimReplace = '$1^'

tok('CARET')
src[t.CARET] = '^' + src[t.LONECARET] + src[t.XRANGEPLAIN] + '$'
tok('CARETLOOSE')
src[t.CARETLOOSE] = '^' + src[t.LONECARET] + src[t.XRANGEPLAINLOOSE] + '$'

// A simple gt/lt/eq thing, or just "" to indicate "any version"
tok('COMPARATORLOOSE')
src[t.COMPARATORLOOSE] = '^' + src[t.GTLT] + '\\s*(' + src[t.LOOSEPLAIN] + ')$|^$'
tok('COMPARATOR')
src[t.COMPARATOR] = '^' + src[t.GTLT] + '\\s*(' + src[t.FULLPLAIN] + ')$|^$'

// An expression to strip any whitespace between the gtlt and the thing
// it modifies, so that `> 1.2.3` ==> `>1.2.3`
tok('COMPARATORTRIM')
src[t.COMPARATORTRIM] = '(\\s*)' + src[t.GTLT] +
                      '\\s*(' + src[t.LOOSEPLAIN] + '|' + src[t.XRANGEPLAIN] + ')'

// this one has to use the /g flag
re[t.COMPARATORTRIM] = new RegExp(src[t.COMPARATORTRIM], 'g')
var comparatorTrimReplace = '$1$2$3'

// Something like `1.2.3 - 1.2.4`
// Note that these all use the loose form, because they'll be
// checked against either the strict or loose comparator form
// later.
tok('HYPHENRANGE')
src[t.HYPHENRANGE] = '^\\s*(' + src[t.XRANGEPLAIN] + ')' +
                   '\\s+-\\s+' +
                   '(' + src[t.XRANGEPLAIN] + ')' +
                   '\\s*$'

tok('HYPHENRANGELOOSE')
src[t.HYPHENRANGELOOSE] = '^\\s*(' + src[t.XRANGEPLAINLOOSE] + ')' +
                        '\\s+-\\s+' +
                        '(' + src[t.XRANGEPLAINLOOSE] + ')' +
                        '\\s*$'

// Star ranges basically just allow anything at all.
tok('STAR')
src[t.STAR] = '(<|>)?=?\\s*\\*'

// Compile to actual regexp objects.
// All are flag-free, unless they were created above with a flag.
for (var i = 0; i < R; i++) {
  debug(i, src[i])
  if (!re[i]) {
    re[i] = new RegExp(src[i])
  }
}

exports.parse = parse
function parse (version, options) {
  if (!options || typeof options !== 'object') {
    options = {
      loose: !!options,
      includePrerelease: false
    }
  }

  if (version instanceof SemVer) {
    return version
  }

  if (typeof version !== 'string') {
    return null
  }

  if (version.length > MAX_LENGTH) {
    return null
  }

  var r = options.loose ? re[t.LOOSE] : re[t.FULL]
  if (!r.test(version)) {
    return null
  }

  try {
    return new SemVer(version, options)
  } catch (er) {
    return null
  }
}

exports.valid = valid
function valid (version, options) {
  var v = parse(version, options)
  return v ? v.version : null
}

exports.clean = clean
function clean (version, options) {
  var s = parse(version.trim().replace(/^[=v]+/, ''), options)
  return s ? s.version : null
}

exports.SemVer = SemVer

function SemVer (version, options) {
  if (!options || typeof options !== 'object') {
    options = {
      loose: !!options,
      includePrerelease: false
    }
  }
  if (version instanceof SemVer) {
    if (version.loose === options.loose) {
      return version
    } else {
      version = version.version
    }
  } else if (typeof version !== 'string') {
    throw new TypeError('Invalid Version: ' + version)
  }

  if (version.length > MAX_LENGTH) {
    throw new TypeError('version is longer than ' + MAX_LENGTH + ' characters')
  }

  if (!(this instanceof SemVer)) {
    return new SemVer(version, options)
  }

  debug('SemVer', version, options)
  this.options = options
  this.loose = !!options.loose

  var m = version.trim().match(options.loose ? re[t.LOOSE] : re[t.FULL])

  if (!m) {
    throw new TypeError('Invalid Version: ' + version)
  }

  this.raw = version

  // these are actually numbers
  this.major = +m[1]
  this.minor = +m[2]
  this.patch = +m[3]

  if (this.major > MAX_SAFE_INTEGER || this.major < 0) {
    throw new TypeError('Invalid major version')
  }

  if (this.minor > MAX_SAFE_INTEGER || this.minor < 0) {
    throw new TypeError('Invalid minor version')
  }

  if (this.patch > MAX_SAFE_INTEGER || this.patch < 0) {
    throw new TypeError('Invalid patch version')
  }

  // numberify any prerelease numeric ids
  if (!m[4]) {
    this.prerelease = []
  } else {
    this.prerelease = m[4].split('.').map(function (id) {
      if (/^[0-9]+$/.test(id)) {
        var num = +id
        if (num >= 0 && num < MAX_SAFE_INTEGER) {
          return num
        }
      }
      return id
    })
  }

  this.build = m[5] ? m[5].split('.') : []
  this.format()
}

SemVer.prototype.format = function () {
  this.version = this.major + '.' + this.minor + '.' + this.patch
  if (this.prerelease.length) {
    this.version += '-' + this.prerelease.join('.')
  }
  return this.version
}

SemVer.prototype.toString = function () {
  return this.version
}

SemVer.prototype.compare = function (other) {
  debug('SemVer.compare', this.version, this.options, other)
  if (!(other instanceof SemVer)) {
    other = new SemVer(other, this.options)
  }

  return this.compareMain(other) || this.comparePre(other)
}

SemVer.prototype.compareMain = function (other) {
  if (!(other instanceof SemVer)) {
    other = new SemVer(other, this.options)
  }

  return compareIdentifiers(this.major, other.major) ||
         compareIdentifiers(this.minor, other.minor) ||
         compareIdentifiers(this.patch, other.patch)
}

SemVer.prototype.comparePre = function (other) {
  if (!(other instanceof SemVer)) {
    other = new SemVer(other, this.options)
  }

  // NOT having a prerelease is > having one
  if (this.prerelease.length && !other.prerelease.length) {
    return -1
  } else if (!this.prerelease.length && other.prerelease.length) {
    return 1
  } else if (!this.prerelease.length && !other.prerelease.length) {
    return 0
  }

  var i = 0
  do {
    var a = this.prerelease[i]
    var b = other.prerelease[i]
    debug('prerelease compare', i, a, b)
    if (a === undefined && b === undefined) {
      return 0
    } else if (b === undefined) {
      return 1
    } else if (a === undefined) {
      return -1
    } else if (a === b) {
      continue
    } else {
      return compareIdentifiers(a, b)
    }
  } while (++i)
}

SemVer.prototype.compareBuild = function (other) {
  if (!(other instanceof SemVer)) {
    other = new SemVer(other, this.options)
  }

  var i = 0
  do {
    var a = this.build[i]
    var b = other.build[i]
    debug('prerelease compare', i, a, b)
    if (a === undefined && b === undefined) {
      return 0
    } else if (b === undefined) {
      return 1
    } else if (a === undefined) {
      return -1
    } else if (a === b) {
      continue
    } else {
      return compareIdentifiers(a, b)
    }
  } while (++i)
}

// preminor will bump the version up to the next minor release, and immediately
// down to pre-release. premajor and prepatch work the same way.
SemVer.prototype.inc = function (release, identifier) {
  switch (release) {
    case 'premajor':
      this.prerelease.length = 0
      this.patch = 0
      this.minor = 0
      this.major++
      this.inc('pre', identifier)
      break
    case 'preminor':
      this.prerelease.length = 0
      this.patch = 0
      this.minor++
      this.inc('pre', identifier)
      break
    case 'prepatch':
      // If this is already a prerelease, it will bump to the next version
      // drop any prereleases that might already exist, since they are not
      // relevant at this point.
      this.prerelease.length = 0
      this.inc('patch', identifier)
      this.inc('pre', identifier)
      break
    // If the input is a non-prerelease version, this acts the same as
    // prepatch.
    case 'prerelease':
      if (this.prerelease.length === 0) {
        this.inc('patch', identifier)
      }
      this.inc('pre', identifier)
      break

    case 'major':
      // If this is a pre-major version, bump up to the same major version.
      // Otherwise increment major.
      // 1.0.0-5 bumps to 1.0.0
      // 1.1.0 bumps to 2.0.0
      if (this.minor !== 0 ||
          this.patch !== 0 ||
          this.prerelease.length === 0) {
        this.major++
      }
      this.minor = 0
      this.patch = 0
      this.prerelease = []
      break
    case 'minor':
      // If this is a pre-minor version, bump up to the same minor version.
      // Otherwise increment minor.
      // 1.2.0-5 bumps to 1.2.0
      // 1.2.1 bumps to 1.3.0
      if (this.patch !== 0 || this.prerelease.length === 0) {
        this.minor++
      }
      this.patch = 0
      this.prerelease = []
      break
    case 'patch':
      // If this is not a pre-release version, it will increment the patch.
      // If it is a pre-release it will bump up to the same patch version.
      // 1.2.0-5 patches to 1.2.0
      // 1.2.0 patches to 1.2.1
      if (this.prerelease.length === 0) {
        this.patch++
      }
      this.prerelease = []
      break
    // This probably shouldn't be used publicly.
    // 1.0.0 "pre" would become 1.0.0-0 which is the wrong direction.
    case 'pre':
      if (this.prerelease.length === 0) {
        this.prerelease = [0]
      } else {
        var i = this.prerelease.length
        while (--i >= 0) {
          if (typeof this.prerelease[i] === 'number') {
            this.prerelease[i]++
            i = -2
          }
        }
        if (i === -1) {
          // didn't increment anything
          this.prerelease.push(0)
        }
      }
      if (identifier) {
        // 1.2.0-beta.1 bumps to 1.2.0-beta.2,
        // 1.2.0-beta.fooblz or 1.2.0-beta bumps to 1.2.0-beta.0
        if (this.prerelease[0] === identifier) {
          if (isNaN(this.prerelease[1])) {
            this.prerelease = [identifier, 0]
          }
        } else {
          this.prerelease = [identifier, 0]
        }
      }
      break

    default:
      throw new Error('invalid increment argument: ' + release)
  }
  this.format()
  this.raw = this.version
  return this
}

exports.inc = inc
function inc (version, release, loose, identifier) {
  if (typeof (loose) === 'string') {
    identifier = loose
    loose = undefined
  }

  try {
    return new SemVer(version, loose).inc(release, identifier).version
  } catch (er) {
    return null
  }
}

exports.diff = diff
function diff (version1, version2) {
  if (eq(version1, version2)) {
    return null
  } else {
    var v1 = parse(version1)
    var v2 = parse(version2)
    var prefix = ''
    if (v1.prerelease.length || v2.prerelease.length) {
      prefix = 'pre'
      var defaultResult = 'prerelease'
    }
    for (var key in v1) {
      if (key === 'major' || key === 'minor' || key === 'patch') {
        if (v1[key] !== v2[key]) {
          return prefix + key
        }
      }
    }
    return defaultResult // may be undefined
  }
}

exports.compareIdentifiers = compareIdentifiers

var numeric = /^[0-9]+$/
function compareIdentifiers (a, b) {
  var anum = numeric.test(a)
  var bnum = numeric.test(b)

  if (anum && bnum) {
    a = +a
    b = +b
  }

  return a === b ? 0
    : (anum && !bnum) ? -1
    : (bnum && !anum) ? 1
    : a < b ? -1
    : 1
}

exports.rcompareIdentifiers = rcompareIdentifiers
function rcompareIdentifiers (a, b) {
  return compareIdentifiers(b, a)
}

exports.major = major
function major (a, loose) {
  return new SemVer(a, loose).major
}

exports.minor = minor
function minor (a, loose) {
  return new SemVer(a, loose).minor
}

exports.patch = patch
function patch (a, loose) {
  return new SemVer(a, loose).patch
}

exports.compare = compare
function compare (a, b, loose) {
  return new SemVer(a, loose).compare(new SemVer(b, loose))
}

exports.compareLoose = compareLoose
function compareLoose (a, b) {
  return compare(a, b, true)
}

exports.compareBuild = compareBuild
function compareBuild (a, b, loose) {
  var versionA = new SemVer(a, loose)
  var versionB = new SemVer(b, loose)
  return versionA.compare(versionB) || versionA.compareBuild(versionB)
}

exports.rcompare = rcompare
function rcompare (a, b, loose) {
  return compare(b, a, loose)
}

exports.sort = sort
function sort (list, loose) {
  return list.sort(function (a, b) {
    return exports.compareBuild(a, b, loose)
  })
}

exports.rsort = rsort
function rsort (list, loose) {
  return list.sort(function (a, b) {
    return exports.compareBuild(b, a, loose)
  })
}

exports.gt = gt
function gt (a, b, loose) {
  return compare(a, b, loose) > 0
}

exports.lt = lt
function lt (a, b, loose) {
  return compare(a, b, loose) < 0
}

exports.eq = eq
function eq (a, b, loose) {
  return compare(a, b, loose) === 0
}

exports.neq = neq
function neq (a, b, loose) {
  return compare(a, b, loose) !== 0
}

exports.gte = gte
function gte (a, b, loose) {
  return compare(a, b, loose) >= 0
}

exports.lte = lte
function lte (a, b, loose) {
  return compare(a, b, loose) <= 0
}

exports.cmp = cmp
function cmp (a, op, b, loose) {
  switch (op) {
    case '===':
      if (typeof a === 'object')
        a = a.version
      if (typeof b === 'object')
        b = b.version
      return a === b

    case '!==':
      if (typeof a === 'object')
        a = a.version
      if (typeof b === 'object')
        b = b.version
      return a !== b

    case '':
    case '=':
    case '==':
      return eq(a, b, loose)

    case '!=':
      return neq(a, b, loose)

    case '>':
      return gt(a, b, loose)

    case '>=':
      return gte(a, b, loose)

    case '<':
      return lt(a, b, loose)

    case '<=':
      return lte(a, b, loose)

    default:
      throw new TypeError('Invalid operator: ' + op)
  }
}

exports.Comparator = Comparator
function Comparator (comp, options) {
  if (!options || typeof options !== 'object') {
    options = {
      loose: !!options,
      includePrerelease: false
    }
  }

  if (comp instanceof Comparator) {
    if (comp.loose === !!options.loose) {
      return comp
    } else {
      comp = comp.value
    }
  }

  if (!(this instanceof Comparator)) {
    return new Comparator(comp, options)
  }

  debug('comparator', comp, options)
  this.options = options
  this.loose = !!options.loose
  this.parse(comp)

  if (this.semver === ANY) {
    this.value = ''
  } else {
    this.value = this.operator + this.semver.version
  }

  debug('comp', this)
}

var ANY = {}
Comparator.prototype.parse = function (comp) {
  var r = this.options.loose ? re[t.COMPARATORLOOSE] : re[t.COMPARATOR]
  var m = comp.match(r)

  if (!m) {
    throw new TypeError('Invalid comparator: ' + comp)
  }

  this.operator = m[1] !== undefined ? m[1] : ''
  if (this.operator === '=') {
    this.operator = ''
  }

  // if it literally is just '>' or '' then allow anything.
  if (!m[2]) {
    this.semver = ANY
  } else {
    this.semver = new SemVer(m[2], this.options.loose)
  }
}

Comparator.prototype.toString = function () {
  return this.value
}

Comparator.prototype.test = function (version) {
  debug('Comparator.test', version, this.options.loose)

  if (this.semver === ANY || version === ANY) {
    return true
  }

  if (typeof version === 'string') {
    try {
      version = new SemVer(version, this.options)
    } catch (er) {
      return false
    }
  }

  return cmp(version, this.operator, this.semver, this.options)
}

Comparator.prototype.intersects = function (comp, options) {
  if (!(comp instanceof Comparator)) {
    throw new TypeError('a Comparator is required')
  }

  if (!options || typeof options !== 'object') {
    options = {
      loose: !!options,
      includePrerelease: false
    }
  }

  var rangeTmp

  if (this.operator === '') {
    if (this.value === '') {
      return true
    }
    rangeTmp = new Range(comp.value, options)
    return satisfies(this.value, rangeTmp, options)
  } else if (comp.operator === '') {
    if (comp.value === '') {
      return true
    }
    rangeTmp = new Range(this.value, options)
    return satisfies(comp.semver, rangeTmp, options)
  }

  var sameDirectionIncreasing =
    (this.operator === '>=' || this.operator === '>') &&
    (comp.operator === '>=' || comp.operator === '>')
  var sameDirectionDecreasing =
    (this.operator === '<=' || this.operator === '<') &&
    (comp.operator === '<=' || comp.operator === '<')
  var sameSemVer = this.semver.version === comp.semver.version
  var differentDirectionsInclusive =
    (this.operator === '>=' || this.operator === '<=') &&
    (comp.operator === '>=' || comp.operator === '<=')
  var oppositeDirectionsLessThan =
    cmp(this.semver, '<', comp.semver, options) &&
    ((this.operator === '>=' || this.operator === '>') &&
    (comp.operator === '<=' || comp.operator === '<'))
  var oppositeDirectionsGreaterThan =
    cmp(this.semver, '>', comp.semver, options) &&
    ((this.operator === '<=' || this.operator === '<') &&
    (comp.operator === '>=' || comp.operator === '>'))

  return sameDirectionIncreasing || sameDirectionDecreasing ||
    (sameSemVer && differentDirectionsInclusive) ||
    oppositeDirectionsLessThan || oppositeDirectionsGreaterThan
}

exports.Range = Range
function Range (range, options) {
  if (!options || typeof options !== 'object') {
    options = {
      loose: !!options,
      includePrerelease: false
    }
  }

  if (range instanceof Range) {
    if (range.loose === !!options.loose &&
        range.includePrerelease === !!options.includePrerelease) {
      return range
    } else {
      return new Range(range.raw, options)
    }
  }

  if (range instanceof Comparator) {
    return new Range(range.value, options)
  }

  if (!(this instanceof Range)) {
    return new Range(range, options)
  }

  this.options = options
  this.loose = !!options.loose
  this.includePrerelease = !!options.includePrerelease

  // First, split based on boolean or ||
  this.raw = range
  this.set = range.split(/\s*\|\|\s*/).map(function (range) {
    return this.parseRange(range.trim())
  }, this).filter(function (c) {
    // throw out any that are not relevant for whatever reason
    return c.length
  })

  if (!this.set.length) {
    throw new TypeError('Invalid SemVer Range: ' + range)
  }

  this.format()
}

Range.prototype.format = function () {
  this.range = this.set.map(function (comps) {
    return comps.join(' ').trim()
  }).join('||').trim()
  return this.range
}

Range.prototype.toString = function () {
  return this.range
}

Range.prototype.parseRange = function (range) {
  var loose = this.options.loose
  range = range.trim()
  // `1.2.3 - 1.2.4` => `>=1.2.3 <=1.2.4`
  var hr = loose ? re[t.HYPHENRANGELOOSE] : re[t.HYPHENRANGE]
  range = range.replace(hr, hyphenReplace)
  debug('hyphen replace', range)
  // `> 1.2.3 < 1.2.5` => `>1.2.3 <1.2.5`
  range = range.replace(re[t.COMPARATORTRIM], comparatorTrimReplace)
  debug('comparator trim', range, re[t.COMPARATORTRIM])

  // `~ 1.2.3` => `~1.2.3`
  range = range.replace(re[t.TILDETRIM], tildeTrimReplace)

  // `^ 1.2.3` => `^1.2.3`
  range = range.replace(re[t.CARETTRIM], caretTrimReplace)

  // normalize spaces
  range = range.split(/\s+/).join(' ')

  // At this point, the range is completely trimmed and
  // ready to be split into comparators.

  var compRe = loose ? re[t.COMPARATORLOOSE] : re[t.COMPARATOR]
  var set = range.split(' ').map(function (comp) {
    return parseComparator(comp, this.options)
  }, this).join(' ').split(/\s+/)
  if (this.options.loose) {
    // in loose mode, throw out any that are not valid comparators
    set = set.filter(function (comp) {
      return !!comp.match(compRe)
    })
  }
  set = set.map(function (comp) {
    return new Comparator(comp, this.options)
  }, this)

  return set
}

Range.prototype.intersects = function (range, options) {
  if (!(range instanceof Range)) {
    throw new TypeError('a Range is required')
  }

  return this.set.some(function (thisComparators) {
    return (
      isSatisfiable(thisComparators, options) &&
      range.set.some(function (rangeComparators) {
        return (
          isSatisfiable(rangeComparators, options) &&
          thisComparators.every(function (thisComparator) {
            return rangeComparators.every(function (rangeComparator) {
              return thisComparator.intersects(rangeComparator, options)
            })
          })
        )
      })
    )
  })
}

// take a set of comparators and determine whether there
// exists a version which can satisfy it
function isSatisfiable (comparators, options) {
  var result = true
  var remainingComparators = comparators.slice()
  var testComparator = remainingComparators.pop()

  while (result && remainingComparators.length) {
    result = remainingComparators.every(function (otherComparator) {
      return testComparator.intersects(otherComparator, options)
    })

    testComparator = remainingComparators.pop()
  }

  return result
}

// Mostly just for testing and legacy API reasons
exports.toComparators = toComparators
function toComparators (range, options) {
  return new Range(range, options).set.map(function (comp) {
    return comp.map(function (c) {
      return c.value
    }).join(' ').trim().split(' ')
  })
}

// comprised of xranges, tildes, stars, and gtlt's at this point.
// already replaced the hyphen ranges
// turn into a set of JUST comparators.
function parseComparator (comp, options) {
  debug('comp', comp, options)
  comp = replaceCarets(comp, options)
  debug('caret', comp)
  comp = replaceTildes(comp, options)
  debug('tildes', comp)
  comp = replaceXRanges(comp, options)
  debug('xrange', comp)
  comp = replaceStars(comp, options)
  debug('stars', comp)
  return comp
}

function isX (id) {
  return !id || id.toLowerCase() === 'x' || id === '*'
}

// ~, ~> --> * (any, kinda silly)
// ~2, ~2.x, ~2.x.x, ~>2, ~>2.x ~>2.x.x --> >=2.0.0 <3.0.0
// ~2.0, ~2.0.x, ~>2.0, ~>2.0.x --> >=2.0.0 <2.1.0
// ~1.2, ~1.2.x, ~>1.2, ~>1.2.x --> >=1.2.0 <1.3.0
// ~1.2.3, ~>1.2.3 --> >=1.2.3 <1.3.0
// ~1.2.0, ~>1.2.0 --> >=1.2.0 <1.3.0
function replaceTildes (comp, options) {
  return comp.trim().split(/\s+/).map(function (comp) {
    return replaceTilde(comp, options)
  }).join(' ')
}

function replaceTilde (comp, options) {
  var r = options.loose ? re[t.TILDELOOSE] : re[t.TILDE]
  return comp.replace(r, function (_, M, m, p, pr) {
    debug('tilde', comp, _, M, m, p, pr)
    var ret

    if (isX(M)) {
      ret = ''
    } else if (isX(m)) {
      ret = '>=' + M + '.0.0 <' + (+M + 1) + '.0.0'
    } else if (isX(p)) {
      // ~1.2 == >=1.2.0 <1.3.0
      ret = '>=' + M + '.' + m + '.0 <' + M + '.' + (+m + 1) + '.0'
    } else if (pr) {
      debug('replaceTilde pr', pr)
      ret = '>=' + M + '.' + m + '.' + p + '-' + pr +
            ' <' + M + '.' + (+m + 1) + '.0'
    } else {
      // ~1.2.3 == >=1.2.3 <1.3.0
      ret = '>=' + M + '.' + m + '.' + p +
            ' <' + M + '.' + (+m + 1) + '.0'
    }

    debug('tilde return', ret)
    return ret
  })
}

// ^ --> * (any, kinda silly)
// ^2, ^2.x, ^2.x.x --> >=2.0.0 <3.0.0
// ^2.0, ^2.0.x --> >=2.0.0 <3.0.0
// ^1.2, ^1.2.x --> >=1.2.0 <2.0.0
// ^1.2.3 --> >=1.2.3 <2.0.0
// ^1.2.0 --> >=1.2.0 <2.0.0
function replaceCarets (comp, options) {
  return comp.trim().split(/\s+/).map(function (comp) {
    return replaceCaret(comp, options)
  }).join(' ')
}

function replaceCaret (comp, options) {
  debug('caret', comp, options)
  var r = options.loose ? re[t.CARETLOOSE] : re[t.CARET]
  return comp.replace(r, function (_, M, m, p, pr) {
    debug('caret', comp, _, M, m, p, pr)
    var ret

    if (isX(M)) {
      ret = ''
    } else if (isX(m)) {
      ret = '>=' + M + '.0.0 <' + (+M + 1) + '.0.0'
    } else if (isX(p)) {
      if (M === '0') {
        ret = '>=' + M + '.' + m + '.0 <' + M + '.' + (+m + 1) + '.0'
      } else {
        ret = '>=' + M + '.' + m + '.0 <' + (+M + 1) + '.0.0'
      }
    } else if (pr) {
      debug('replaceCaret pr', pr)
      if (M === '0') {
        if (m === '0') {
          ret = '>=' + M + '.' + m + '.' + p + '-' + pr +
                ' <' + M + '.' + m + '.' + (+p + 1)
        } else {
          ret = '>=' + M + '.' + m + '.' + p + '-' + pr +
                ' <' + M + '.' + (+m + 1) + '.0'
        }
      } else {
        ret = '>=' + M + '.' + m + '.' + p + '-' + pr +
              ' <' + (+M + 1) + '.0.0'
      }
    } else {
      debug('no pr')
      if (M === '0') {
        if (m === '0') {
          ret = '>=' + M + '.' + m + '.' + p +
                ' <' + M + '.' + m + '.' + (+p + 1)
        } else {
          ret = '>=' + M + '.' + m + '.' + p +
                ' <' + M + '.' + (+m + 1) + '.0'
        }
      } else {
        ret = '>=' + M + '.' + m + '.' + p +
              ' <' + (+M + 1) + '.0.0'
      }
    }

    debug('caret return', ret)
    return ret
  })
}

function replaceXRanges (comp, options) {
  debug('replaceXRanges', comp, options)
  return comp.split(/\s+/).map(function (comp) {
    return replaceXRange(comp, options)
  }).join(' ')
}

function replaceXRange (comp, options) {
  comp = comp.trim()
  var r = options.loose ? re[t.XRANGELOOSE] : re[t.XRANGE]
  return comp.replace(r, function (ret, gtlt, M, m, p, pr) {
    debug('xRange', comp, ret, gtlt, M, m, p, pr)
    var xM = isX(M)
    var xm = xM || isX(m)
    var xp = xm || isX(p)
    var anyX = xp

    if (gtlt === '=' && anyX) {
      gtlt = ''
    }

    // if we're including prereleases in the match, then we need
    // to fix this to -0, the lowest possible prerelease value
    pr = options.includePrerelease ? '-0' : ''

    if (xM) {
      if (gtlt === '>' || gtlt === '<') {
        // nothing is allowed
        ret = '<0.0.0-0'
      } else {
        // nothing is forbidden
        ret = '*'
      }
    } else if (gtlt && anyX) {
      // we know patch is an x, because we have any x at all.
      // replace X with 0
      if (xm) {
        m = 0
      }
      p = 0

      if (gtlt === '>') {
        // >1 => >=2.0.0
        // >1.2 => >=1.3.0
        // >1.2.3 => >= 1.2.4
        gtlt = '>='
        if (xm) {
          M = +M + 1
          m = 0
          p = 0
        } else {
          m = +m + 1
          p = 0
        }
      } else if (gtlt === '<=') {
        // <=0.7.x is actually <0.8.0, since any 0.7.x should
        // pass.  Similarly, <=7.x is actually <8.0.0, etc.
        gtlt = '<'
        if (xm) {
          M = +M + 1
        } else {
          m = +m + 1
        }
      }

      ret = gtlt + M + '.' + m + '.' + p + pr
    } else if (xm) {
      ret = '>=' + M + '.0.0' + pr + ' <' + (+M + 1) + '.0.0' + pr
    } else if (xp) {
      ret = '>=' + M + '.' + m + '.0' + pr +
        ' <' + M + '.' + (+m + 1) + '.0' + pr
    }

    debug('xRange return', ret)

    return ret
  })
}

// Because * is AND-ed with everything else in the comparator,
// and '' means "any version", just remove the *s entirely.
function replaceStars (comp, options) {
  debug('replaceStars', comp, options)
  // Looseness is ignored here.  star is always as loose as it gets!
  return comp.trim().replace(re[t.STAR], '')
}

// This function is passed to string.replace(re[t.HYPHENRANGE])
// M, m, patch, prerelease, build
// 1.2 - 3.4.5 => >=1.2.0 <=3.4.5
// 1.2.3 - 3.4 => >=1.2.0 <3.5.0 Any 3.4.x will do
// 1.2 - 3.4 => >=1.2.0 <3.5.0
function hyphenReplace ($0,
  from, fM, fm, fp, fpr, fb,
  to, tM, tm, tp, tpr, tb) {
  if (isX(fM)) {
    from = ''
  } else if (isX(fm)) {
    from = '>=' + fM + '.0.0'
  } else if (isX(fp)) {
    from = '>=' + fM + '.' + fm + '.0'
  } else {
    from = '>=' + from
  }

  if (isX(tM)) {
    to = ''
  } else if (isX(tm)) {
    to = '<' + (+tM + 1) + '.0.0'
  } else if (isX(tp)) {
    to = '<' + tM + '.' + (+tm + 1) + '.0'
  } else if (tpr) {
    to = '<=' + tM + '.' + tm + '.' + tp + '-' + tpr
  } else {
    to = '<=' + to
  }

  return (from + ' ' + to).trim()
}

// if ANY of the sets match ALL of its comparators, then pass
Range.prototype.test = function (version) {
  if (!version) {
    return false
  }

  if (typeof version === 'string') {
    try {
      version = new SemVer(version, this.options)
    } catch (er) {
      return false
    }
  }

  for (var i = 0; i < this.set.length; i++) {
    if (testSet(this.set[i], version, this.options)) {
      return true
    }
  }
  return false
}

function testSet (set, version, options) {
  for (var i = 0; i < set.length; i++) {
    if (!set[i].test(version)) {
      return false
    }
  }

  if (version.prerelease.length && !options.includePrerelease) {
    // Find the set of versions that are allowed to have prereleases
    // For example, ^1.2.3-pr.1 desugars to >=1.2.3-pr.1 <2.0.0
    // That should allow `1.2.3-pr.2` to pass.
    // However, `1.2.4-alpha.notready` should NOT be allowed,
    // even though it's within the range set by the comparators.
    for (i = 0; i < set.length; i++) {
      debug(set[i].semver)
      if (set[i].semver === ANY) {
        continue
      }

      if (set[i].semver.prerelease.length > 0) {
        var allowed = set[i].semver
        if (allowed.major === version.major &&
            allowed.minor === version.minor &&
            allowed.patch === version.patch) {
          return true
        }
      }
    }

    // Version has a -pre, but it's not one of the ones we like.
    return false
  }

  return true
}

exports.satisfies = satisfies
function satisfies (version, range, options) {
  try {
    range = new Range(range, options)
  } catch (er) {
    return false
  }
  return range.test(version)
}

exports.maxSatisfying = maxSatisfying
function maxSatisfying (versions, range, options) {
  var max = null
  var maxSV = null
  try {
    var rangeObj = new Range(range, options)
  } catch (er) {
    return null
  }
  versions.forEach(function (v) {
    if (rangeObj.test(v)) {
      // satisfies(v, range, options)
      if (!max || maxSV.compare(v) === -1) {
        // compare(max, v, true)
        max = v
        maxSV = new SemVer(max, options)
      }
    }
  })
  return max
}

exports.minSatisfying = minSatisfying
function minSatisfying (versions, range, options) {
  var min = null
  var minSV = null
  try {
    var rangeObj = new Range(range, options)
  } catch (er) {
    return null
  }
  versions.forEach(function (v) {
    if (rangeObj.test(v)) {
      // satisfies(v, range, options)
      if (!min || minSV.compare(v) === 1) {
        // compare(min, v, true)
        min = v
        minSV = new SemVer(min, options)
      }
    }
  })
  return min
}

exports.minVersion = minVersion
function minVersion (range, loose) {
  range = new Range(range, loose)

  var minver = new SemVer('0.0.0')
  if (range.test(minver)) {
    return minver
  }

  minver = new SemVer('0.0.0-0')
  if (range.test(minver)) {
    return minver
  }

  minver = null
  for (var i = 0; i < range.set.length; ++i) {
    var comparators = range.set[i]

    comparators.forEach(function (comparator) {
      // Clone to avoid manipulating the comparator's semver object.
      var compver = new SemVer(comparator.semver.version)
      switch (comparator.operator) {
        case '>':
          if (compver.prerelease.length === 0) {
            compver.patch++
          } else {
            compver.prerelease.push(0)
          }
          compver.raw = compver.format()
          /* fallthrough */
        case '':
        case '>=':
          if (!minver || gt(minver, compver)) {
            minver = compver
          }
          break
        case '<':
        case '<=':
          /* Ignore maximum versions */
          break
        /* istanbul ignore next */
        default:
          throw new Error('Unexpected operation: ' + comparator.operator)
      }
    })
  }

  if (minver && range.test(minver)) {
    return minver
  }

  return null
}

exports.validRange = validRange
function validRange (range, options) {
  try {
    // Return '*' instead of '' so that truthiness works.
    // This will throw if it's invalid anyway
    return new Range(range, options).range || '*'
  } catch (er) {
    return null
  }
}

// Determine if version is less than all the versions possible in the range
exports.ltr = ltr
function ltr (version, range, options) {
  return outside(version, range, '<', options)
}

// Determine if version is greater than all the versions possible in the range.
exports.gtr = gtr
function gtr (version, range, options) {
  return outside(version, range, '>', options)
}

exports.outside = outside
function outside (version, range, hilo, options) {
  version = new SemVer(version, options)
  range = new Range(range, options)

  var gtfn, ltefn, ltfn, comp, ecomp
  switch (hilo) {
    case '>':
      gtfn = gt
      ltefn = lte
      ltfn = lt
      comp = '>'
      ecomp = '>='
      break
    case '<':
      gtfn = lt
      ltefn = gte
      ltfn = gt
      comp = '<'
      ecomp = '<='
      break
    default:
      throw new TypeError('Must provide a hilo val of "<" or ">"')
  }

  // If it satisifes the range it is not outside
  if (satisfies(version, range, options)) {
    return false
  }

  // From now on, variable terms are as if we're in "gtr" mode.
  // but note that everything is flipped for the "ltr" function.

  for (var i = 0; i < range.set.length; ++i) {
    var comparators = range.set[i]

    var high = null
    var low = null

    comparators.forEach(function (comparator) {
      if (comparator.semver === ANY) {
        comparator = new Comparator('>=0.0.0')
      }
      high = high || comparator
      low = low || comparator
      if (gtfn(comparator.semver, high.semver, options)) {
        high = comparator
      } else if (ltfn(comparator.semver, low.semver, options)) {
        low = comparator
      }
    })

    // If the edge version comparator has a operator then our version
    // isn't outside it
    if (high.operator === comp || high.operator === ecomp) {
      return false
    }

    // If the lowest version comparator has an operator and our version
    // is less than it then it isn't higher than the range
    if ((!low.operator || low.operator === comp) &&
        ltefn(version, low.semver)) {
      return false
    } else if (low.operator === ecomp && ltfn(version, low.semver)) {
      return false
    }
  }
  return true
}

exports.prerelease = prerelease
function prerelease (version, options) {
  var parsed = parse(version, options)
  return (parsed && parsed.prerelease.length) ? parsed.prerelease : null
}

exports.intersects = intersects
function intersects (r1, r2, options) {
  r1 = new Range(r1, options)
  r2 = new Range(r2, options)
  return r1.intersects(r2)
}

exports.coerce = coerce
function coerce (version, options) {
  if (version instanceof SemVer) {
    return version
  }

  if (typeof version === 'number') {
    version = String(version)
  }

  if (typeof version !== 'string') {
    return null
  }

  options = options || {}

  var match = null
  if (!options.rtl) {
    match = version.match(re[t.COERCE])
  } else {
    // Find the right-most coercible string that does not share
    // a terminus with a more left-ward coercible string.
    // Eg, '1.2.3.4' wants to coerce '2.3.4', not '3.4' or '4'
    //
    // Walk through the string checking with a /g regexp
    // Manually set the index so as to pick up overlapping matches.
    // Stop when we get a match that ends at the string end, since no
    // coercible string can be more right-ward without the same terminus.
    var next
    while ((next = re[t.COERCERTL].exec(version)) &&
      (!match || match.index + match[0].length !== version.length)
    ) {
      if (!match ||
          next.index + next[0].length !== match.index + match[0].length) {
        match = next
      }
      re[t.COERCERTL].lastIndex = next.index + next[1].length + next[2].length
    }
    // leave it in a clean state
    re[t.COERCERTL].lastIndex = -1
  }

  if (match === null) {
    return null
  }

  return parse(match[2] +
    '.' + (match[3] || '0') +
    '.' + (match[4] || '0'), options)
}

/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./../process/browser.js */ "../node_modules/process/browser.js")))

/***/ }),

/***/ "../node_modules/tslib/tslib.es6.js":
/*!******************************************!*\
  !*** ../node_modules/tslib/tslib.es6.js ***!
  \******************************************/
/*! exports provided: __extends, __assign, __rest, __decorate, __param, __metadata, __awaiter, __generator, __createBinding, __exportStar, __values, __read, __spread, __spreadArrays, __spreadArray, __await, __asyncGenerator, __asyncDelegator, __asyncValues, __makeTemplateObject, __importStar, __importDefault, __classPrivateFieldGet, __classPrivateFieldSet */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__extends", function() { return __extends; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__assign", function() { return __assign; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__rest", function() { return __rest; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__decorate", function() { return __decorate; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__param", function() { return __param; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__metadata", function() { return __metadata; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__awaiter", function() { return __awaiter; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__generator", function() { return __generator; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__createBinding", function() { return __createBinding; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__exportStar", function() { return __exportStar; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__values", function() { return __values; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__read", function() { return __read; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__spread", function() { return __spread; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__spreadArrays", function() { return __spreadArrays; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__spreadArray", function() { return __spreadArray; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__await", function() { return __await; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__asyncGenerator", function() { return __asyncGenerator; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__asyncDelegator", function() { return __asyncDelegator; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__asyncValues", function() { return __asyncValues; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__makeTemplateObject", function() { return __makeTemplateObject; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__importStar", function() { return __importStar; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__importDefault", function() { return __importDefault; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__classPrivateFieldGet", function() { return __classPrivateFieldGet; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "__classPrivateFieldSet", function() { return __classPrivateFieldSet; });
/*! *****************************************************************************
Copyright (c) Microsoft Corporation.

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.
***************************************************************************** */
/* global Reflect, Promise */

var extendStatics = function(d, b) {
    extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
    return extendStatics(d, b);
};

function __extends(d, b) {
    if (typeof b !== "function" && b !== null)
        throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
    extendStatics(d, b);
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
}

var __assign = function() {
    __assign = Object.assign || function __assign(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
        }
        return t;
    }
    return __assign.apply(this, arguments);
}

function __rest(s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
}

function __decorate(decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
}

function __param(paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
}

function __metadata(metadataKey, metadataValue) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(metadataKey, metadataValue);
}

function __awaiter(thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
}

function __generator(thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
}

var __createBinding = Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
});

function __exportStar(m, o) {
    for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(o, p)) __createBinding(o, m, p);
}

function __values(o) {
    var s = typeof Symbol === "function" && Symbol.iterator, m = s && o[s], i = 0;
    if (m) return m.call(o);
    if (o && typeof o.length === "number") return {
        next: function () {
            if (o && i >= o.length) o = void 0;
            return { value: o && o[i++], done: !o };
        }
    };
    throw new TypeError(s ? "Object is not iterable." : "Symbol.iterator is not defined.");
}

function __read(o, n) {
    var m = typeof Symbol === "function" && o[Symbol.iterator];
    if (!m) return o;
    var i = m.call(o), r, ar = [], e;
    try {
        while ((n === void 0 || n-- > 0) && !(r = i.next()).done) ar.push(r.value);
    }
    catch (error) { e = { error: error }; }
    finally {
        try {
            if (r && !r.done && (m = i["return"])) m.call(i);
        }
        finally { if (e) throw e.error; }
    }
    return ar;
}

/** @deprecated */
function __spread() {
    for (var ar = [], i = 0; i < arguments.length; i++)
        ar = ar.concat(__read(arguments[i]));
    return ar;
}

/** @deprecated */
function __spreadArrays() {
    for (var s = 0, i = 0, il = arguments.length; i < il; i++) s += arguments[i].length;
    for (var r = Array(s), k = 0, i = 0; i < il; i++)
        for (var a = arguments[i], j = 0, jl = a.length; j < jl; j++, k++)
            r[k] = a[j];
    return r;
}

function __spreadArray(to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
}

function __await(v) {
    return this instanceof __await ? (this.v = v, this) : new __await(v);
}

function __asyncGenerator(thisArg, _arguments, generator) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var g = generator.apply(thisArg, _arguments || []), i, q = [];
    return i = {}, verb("next"), verb("throw"), verb("return"), i[Symbol.asyncIterator] = function () { return this; }, i;
    function verb(n) { if (g[n]) i[n] = function (v) { return new Promise(function (a, b) { q.push([n, v, a, b]) > 1 || resume(n, v); }); }; }
    function resume(n, v) { try { step(g[n](v)); } catch (e) { settle(q[0][3], e); } }
    function step(r) { r.value instanceof __await ? Promise.resolve(r.value.v).then(fulfill, reject) : settle(q[0][2], r); }
    function fulfill(value) { resume("next", value); }
    function reject(value) { resume("throw", value); }
    function settle(f, v) { if (f(v), q.shift(), q.length) resume(q[0][0], q[0][1]); }
}

function __asyncDelegator(o) {
    var i, p;
    return i = {}, verb("next"), verb("throw", function (e) { throw e; }), verb("return"), i[Symbol.iterator] = function () { return this; }, i;
    function verb(n, f) { i[n] = o[n] ? function (v) { return (p = !p) ? { value: __await(o[n](v)), done: n === "return" } : f ? f(v) : v; } : f; }
}

function __asyncValues(o) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var m = o[Symbol.asyncIterator], i;
    return m ? m.call(o) : (o = typeof __values === "function" ? __values(o) : o[Symbol.iterator](), i = {}, verb("next"), verb("throw"), verb("return"), i[Symbol.asyncIterator] = function () { return this; }, i);
    function verb(n) { i[n] = o[n] && function (v) { return new Promise(function (resolve, reject) { v = o[n](v), settle(resolve, reject, v.done, v.value); }); }; }
    function settle(resolve, reject, d, v) { Promise.resolve(v).then(function(v) { resolve({ value: v, done: d }); }, reject); }
}

function __makeTemplateObject(cooked, raw) {
    if (Object.defineProperty) { Object.defineProperty(cooked, "raw", { value: raw }); } else { cooked.raw = raw; }
    return cooked;
};

var __setModuleDefault = Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
};

function __importStar(mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
}

function __importDefault(mod) {
    return (mod && mod.__esModule) ? mod : { default: mod };
}

function __classPrivateFieldGet(receiver, state, kind, f) {
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a getter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot read private member from an object whose class did not declare it");
    return kind === "m" ? f : kind === "a" ? f.call(receiver) : f ? f.value : state.get(receiver);
}

function __classPrivateFieldSet(receiver, state, value, kind, f) {
    if (kind === "m") throw new TypeError("Private method is not writable");
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a setter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot write private member to an object whose class did not declare it");
    return (kind === "a" ? f.call(receiver, value) : f ? f.value = value : state.set(receiver, value)), value;
}


/***/ }),

/***/ "./CustomFieldSelectEditor.tsx":
/*!*************************************!*\
  !*** ./CustomFieldSelectEditor.tsx ***!
  \*************************************/
/*! exports provided: CustomFieldSelectEditor */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CustomFieldSelectEditor", function() { return CustomFieldSelectEditor; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "../node_modules/tslib/tslib.es6.js");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @grafana/ui */ "@grafana/ui");
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var jquery__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! jquery */ "jquery");
/* harmony import */ var jquery__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(jquery__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! lodash */ "lodash");
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(lodash__WEBPACK_IMPORTED_MODULE_4__);
/* harmony import */ var models_ChartModel__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! models/ChartModel */ "./models/ChartModel.ts");
/* harmony import */ var models_InsightsChartEditorModel__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! models/InsightsChartEditorModel */ "./models/InsightsChartEditorModel.ts");
/* harmony import */ var GoogleChartUtilities__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! GoogleChartUtilities */ "./GoogleChartUtilities.tsx");








var CustomFieldSelectEditor = function CustomFieldSelectEditor(_a) {
  var e_1, _b;

  var item = _a.item,
      value = _a.value,
      onChange = _a.onChange,
      context = _a.context;
  var theme = Object(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["useTheme2"])();
  var chartEditor;
  var google = window.google;

  var _c = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__read"])(Object(react__WEBPACK_IMPORTED_MODULE_1__["useState"])(""), 2),
      dataTransform = _c[0],
      setDataTransform = _c[1];

  var _d = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__read"])(Object(react__WEBPACK_IMPORTED_MODULE_1__["useState"])(""), 2),
      dataJoin = _d[0],
      setDataJoin = _d[1];

  var _e = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__read"])(Object(react__WEBPACK_IMPORTED_MODULE_1__["useState"])(""), 2),
      chartOptions = _e[0],
      setChartOptions = _e[1];

  var _f = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__read"])(Object(react__WEBPACK_IMPORTED_MODULE_1__["useState"])(""), 2),
      chartType = _f[0],
      setChartType = _f[1];

  var _g = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__read"])(Object(react__WEBPACK_IMPORTED_MODULE_1__["useState"])([]), 2),
      columnModel = _g[0],
      setColumnModel = _g[1];

  var _h = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__read"])(Object(react__WEBPACK_IMPORTED_MODULE_1__["useState"])([]), 2),
      chartDataArray = _h[0],
      setChartDataArray = _h[1];

  Object(react__WEBPACK_IMPORTED_MODULE_1__["useEffect"])(function () {
    var e_2, _a;

    if (context.options.hasOwnProperty("chartFields")) {
      setChartValue();

      var _loop_2 = function _loop_2(dt) {
        var e_3, _d;

        var refId = dt.refId || "";
        var columnModelArr = new Array();

        var _loop_3 = function _loop_3(field) {
          var columnModelObj = void 0;
          columnModelObj = columnModel.find(function (o) {
            return o.name === field.name && o.refId === refId;
          });
          columnModelArr.push(columnModelObj);
        };

        try {
          for (var _e = (e_3 = void 0, Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(dt.fields)), _f = _e.next(); !_f.done; _f = _e.next()) {
            var field = _f.value;

            _loop_3(field);
          }
        } catch (e_3_1) {
          e_3 = {
            error: e_3_1
          };
        } finally {
          try {
            if (_f && !_f.done && (_d = _e["return"])) _d.call(_e);
          } finally {
            if (e_3) throw e_3.error;
          }
        }

        var chartDataObjIndx = chartDataArray.findIndex(function (obj) {
          return obj.id === refId;
        });

        if (chartDataObjIndx != -1) {
          chartDataArray[chartDataObjIndx].data = context.data;
          chartDataArray[chartDataObjIndx].columns = columnModelArr;
        } else {
          chartDataArray.push(new models_ChartModel__WEBPACK_IMPORTED_MODULE_5__["ChartData"](refId, context.data, columnModelArr));
        }
      };

      try {
        for (var _b = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(context.data), _c = _b.next(); !_c.done; _c = _b.next()) {
          var dt = _c.value;

          _loop_2(dt);
        }
      } catch (e_2_1) {
        e_2 = {
          error: e_2_1
        };
      } finally {
        try {
          if (_c && !_c.done && (_a = _b["return"])) _a.call(_b);
        } finally {
          if (e_2) throw e_2.error;
        }
      }

      setChartDataArray(chartDataArray);
    }

    function setChartValue() {
      if ('chartOptions' in context.options.chartFields) {
        setChartOptions(context.options.chartFields.chartOptions);
      }

      if ('chartType' in context.options.chartFields) {
        setChartType(context.options.chartFields.chartType);
      }

      if ('joinInstructions' in context.options.chartFields) {
        setDataJoin(context.options.chartFields.joinInstructions);
      }

      if ('transformDataInstruction' in context.options.chartFields) {
        setDataTransform(context.options.chartFields.transformDataInstruction);
      }

      if ('columnModel' in context.options.chartFields) {
        setColumnModel(context.options.chartFields.columnModel);
      } else {
        setColumnModel([new models_ChartModel__WEBPACK_IMPORTED_MODULE_5__["ColumnModel"]("", "")]);
      }
    }
  }, []);

  var onDataTransformChange = function onDataTransformChange(index) {
    return function (event) {
      setDataTransform(event.currentTarget.value);
      onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
        transformDataInstruction: event.currentTarget.value
      }));
    };
  };

  var onDataJoinChange = function onDataJoinChange(index) {
    return function (event) {
      setDataJoin(event.currentTarget.value);
      onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
        joinInstructions: event.currentTarget.value
      }));
    };
  };

  var onChartOptionsChange = function onChartOptionsChange(index) {
    return function (event) {
      setChartOptions(event.currentTarget.value);
      onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
        chartOptions: event.currentTarget.value
      }));
    };
  };

  var onColumnModelChange = function onColumnModelChange(columnName, refId) {
    return function (option) {
      var e_4, _a, e_5, _b;

      var i = columnModel.findIndex(function (element) {
        return element.name === columnName && element.refId === refId;
      });
      if (i > -1) columnModel[i].type = option.value;else {
        columnModel.push(new models_ChartModel__WEBPACK_IMPORTED_MODULE_5__["ColumnModel"](columnName, option.value, refId));
      }
      var filterColModel = [];

      try {
        for (var _c = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(context.data), _d = _c.next(); !_d.done; _d = _c.next()) {
          var dt = _d.value;

          try {
            for (var _e = (e_5 = void 0, Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(dt.fields)), _f = _e.next(); !_f.done; _f = _e.next()) {
              var field = _f.value;
              filterColModel.push({
                "refId": dt.refId,
                "label": field.name
              });
            }
          } catch (e_5_1) {
            e_5 = {
              error: e_5_1
            };
          } finally {
            try {
              if (_f && !_f.done && (_b = _e["return"])) _b.call(_e);
            } finally {
              if (e_5) throw e_5.error;
            }
          }
        }
      } catch (e_4_1) {
        e_4 = {
          error: e_4_1
        };
      } finally {
        try {
          if (_d && !_d.done && (_a = _c["return"])) _a.call(_c);
        } finally {
          if (e_4) throw e_4.error;
        }
      }

      var filteredList = columnModel.filter(function (el) {
        return filterColModel.some(function (f) {
          return f.refId === el.refId && f.label === el.name;
        });
      });
      setColumnModel(filteredList);
      onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
        columnModel: filteredList
      }));
    };
  };

  var _j = Object(GoogleChartUtilities__WEBPACK_IMPORTED_MODULE_7__["googlechartutilities"])(theme, dataTransform, dataJoin, google, true),
      buildDataTables = _j.buildDataTables,
      joinDataTables = _j.joinDataTables,
      transformData = _j.transformData,
      innerDimensions = _j.innerDimensions,
      applyTheme = _j.applyTheme;

  var renderChart = function renderChart(isEditChart) {
    if (context.data && context.data.length > 0) {
      var containerElem = document.getElementById("googleId");

      if (containerElem && google && google.charts) {
        if (isEditChart) {
          google.charts.load('46', {
            'packages': ['corechart', 'charteditor']
          });
          google.charts.setOnLoadCallback(executeEditChart());
        } else {
          if (chartOptions) {
            google.charts.load('46', {
              'packages': ['corechart']
            });
          }
        }
      } else {
        setTimeout(function () {
          renderChart(isEditChart);
        }, 50);
      }
    }
  };

  var saveChartOpts = function saveChartOpts() {
    var opts = document.getElementById("chartsOptId").value;
    setChartOptions(opts);
    onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
      chartOptions: opts
    }));
  };

  var executeEditChart = function executeEditChart() {
    var data = buildChartData();

    var _a = buildChartOptions(),
        defchartOptions = _a.defchartOptions,
        contId = _a.contId,
        contIdView = _a.contIdView;

    var wrapper = new google.visualization.ChartWrapper({
      'chartType': chartType,
      'dataTable': data,
      'options': defchartOptions
    });
    chartEditor = new google.visualization.ChartEditor();

    var redrawChart = function redrawChart() {
      var _a, _b, _c, _d;

      var chartWrapperTemp = chartEditor.getChartWrapper();
      var editorChartOptions = chartWrapperTemp.getOptions();
      var containerDimTemp = innerDimensions((_b = (_a = document.getElementById(contId)) === null || _a === void 0 ? void 0 : _a.parentNode) === null || _b === void 0 ? void 0 : _b.parentNode);
      var tempOptions = chartWrapperTemp.getOptions();
      tempOptions["height"] = containerDimTemp.height;
      tempOptions["width"] = containerDimTemp.width;
      tempOptions = lodash__WEBPACK_IMPORTED_MODULE_4___default.a.defaults(tempOptions, editorChartOptions);
      setChartOptions(JSON.stringify(tempOptions));
      onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
        chartOptions: JSON.stringify(tempOptions)
      }));
      setChartType(chartWrapperTemp.getChartType());
      onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
        chartType: chartWrapperTemp.getChartType()
      }));
      setTimeout(function () {
        chartWrapperTemp.draw(document.getElementById(contId), tempOptions);
      }, 2000);
      var viewElement = document.getElementById(contIdView);

      if (viewElement !== null) {
        var chartWrapperview = chartEditor.getChartWrapper();
        var containerDimView = innerDimensions((_d = (_c = document.getElementById(contId)) === null || _c === void 0 ? void 0 : _c.parentNode) === null || _d === void 0 ? void 0 : _d.parentNode);
        var tempViewOptions = tempOptions;
        tempViewOptions["height"] = containerDimView.height;
        tempViewOptions["width"] = containerDimView.width;
        setChartOptions(JSON.stringify(tempViewOptions));
        onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
          chartOptions: JSON.stringify(tempViewOptions)
        }));
        setChartType(chartWrapperview.getChartType());
        onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
          chartType: chartWrapperview.getChartType()
        }));
        chartWrapperview.draw(viewElement, tempViewOptions);
      }

      chartEditor.openDialog(chartEditor.getChartWrapper(), {});
      appendChartContainer();
    };

    chartEditor.openDialog(wrapper, {});
    appendChartContainer();
    google.visualization.events.addListener(chartEditor, 'ok', redrawChart);
    google.visualization.events.addListener(chartEditor, 'cancel', function () {
      jquery__WEBPACK_IMPORTED_MODULE_3___default()('#googleId').empty();
    });

    function buildChartOptions() {
      var _a, _b;

      if (chartType === null || chartType === undefined) {
        setChartType('PieChart');
      }

      var defchartOptions;

      if (chartOptions === undefined || chartOptions === "") {
        defchartOptions = {};
      } else {
        defchartOptions = JSON.parse(chartOptions);
      }

      var search = window.location.search;
      var searchParams = new URLSearchParams(search);
      var panelId = searchParams.get("editPanel");
      var contId = "containerIdDivEdit_insights_" + panelId;
      var contIdView = "containerIdDiv_insights_" + panelId;
      var containerDim = innerDimensions((_b = (_a = document.getElementById(contId)) === null || _a === void 0 ? void 0 : _a.parentNode) === null || _b === void 0 ? void 0 : _b.parentNode);
      defchartOptions["height"] = containerDim.height;
      defchartOptions["width"] = containerDim.width;
      defchartOptions = applyTheme(defchartOptions);
      setChartOptions(JSON.stringify(defchartOptions));
      return {
        defchartOptions: defchartOptions,
        contId: contId,
        contIdView: contIdView
      };
    }

    function buildChartData() {
      var e_6, _a;

      var targetModelArr = new Array();

      var _loop_4 = function _loop_4(dt) {
        var e_7, _d;

        var refId = dt.refId || "";
        var columnModelArr = new Array();

        var _loop_5 = function _loop_5(field) {
          var columnModelObj = void 0;
          columnModelObj = columnModel.find(function (o) {
            return o.name === field.name && o.refId === refId;
          });
          columnModelArr.push(columnModelObj);
        };

        try {
          for (var _e = (e_7 = void 0, Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(dt.fields)), _f = _e.next(); !_f.done; _f = _e.next()) {
            var field = _f.value;

            _loop_5(field);
          }
        } catch (e_7_1) {
          e_7 = {
            error: e_7_1
          };
        } finally {
          try {
            if (_f && !_f.done && (_d = _e["return"])) _d.call(_e);
          } finally {
            if (e_7) throw e_7.error;
          }
        }

        var chartDataObjIndx = chartDataArray.findIndex(function (obj) {
          return obj.id === refId;
        });

        if (chartDataObjIndx != -1) {
          chartDataArray[chartDataObjIndx].data = context.data;
          chartDataArray[chartDataObjIndx].columns = columnModelArr;
        } else {
          chartDataArray.push(new models_ChartModel__WEBPACK_IMPORTED_MODULE_5__["ChartData"](refId, context.data, columnModelArr));
        }

        var targetModel = new models_InsightsChartEditorModel__WEBPACK_IMPORTED_MODULE_6__["InsightsChartTargetModel"](refId, columnModelArr);
        targetModelArr.push(targetModel);
      };

      try {
        for (var _b = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(context.data), _c = _b.next(); !_c.done; _c = _b.next()) {
          var dt = _c.value;

          _loop_4(dt);
        }
      } catch (e_6_1) {
        e_6 = {
          error: e_6_1
        };
      } finally {
        try {
          if (_c && !_c.done && (_a = _b["return"])) _a.call(_b);
        } finally {
          if (e_6) throw e_6.error;
        }
      }

      setChartDataArray(chartDataArray);
      onChange(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])(Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, value), {
        dataArray: chartDataArray
      }));
      var datatab = buildDataTables(chartDataArray);
      var data = joinDataTables(datatab);
      data = transformData(data);
      return data;
    }
  };

  var appendChartContainer = function appendChartContainer() {
    var dialog = jquery__WEBPACK_IMPORTED_MODULE_3___default()('.google-visualization-charteditor-dialog');

    if (dialog.length === 0) {
      setTimeout(function () {
        appendChartContainer();
      }, 50);
    } else {
      jquery__WEBPACK_IMPORTED_MODULE_3___default()('#googleId').empty();
      dialog.children().each(function () {
        jquery__WEBPACK_IMPORTED_MODULE_3___default()('#googleId').append(this);
      });
      jquery__WEBPACK_IMPORTED_MODULE_3___default()(".modal-dialog-title").css({
        "background-color": theme.colors.background.primary,
        "color": theme.colors.text.primary
      });
      jquery__WEBPACK_IMPORTED_MODULE_3___default()(".modal-dialog-content").css({
        "background-color": theme.colors.background.primary,
        "color": theme.colors.text.primary
      });
      dialog.hide();
    }
  };

  if (context.data && context.data.length > 0) {
    var selectOptions_1 = [{
      label: 'string',
      value: 'string'
    }, {
      label: 'number',
      value: 'number'
    }, {
      label: 'boolean',
      value: 'boolean'
    }, {
      label: 'date',
      value: 'date'
    }, {
      label: 'datetime',
      value: 'datetime'
    }, {
      label: 'timeofday',
      value: 'timeofday'
    }];
    var css = "      \n      .google-visualization-charteditor-preview-td{\n        position: relative !important;\n        top: 0px;\n        left: 0px\n      }\n      #google-visualization-charteditor-panel-navigate-div{\n        width: 350px \n      }\n      .marlft{\n        margin-left:20px\n      }\n      ";

    if (context.data) {
      var itemrows = [];

      var _loop_1 = function _loop_1(dt) {
        var refId = react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null, dt.refId);
        var fieldItems = dt.fields.map(function (field) {
          var _a;

          return react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null, react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("span", null, field.name), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["Select"], {
            isLoading: false,
            value: (_a = columnModel.find(function (obj) {
              return obj.name == field.name && obj.refId === dt.refId;
            })) === null || _a === void 0 ? void 0 : _a.type,
            allowCustomValue: true,
            onChange: onColumnModelChange(field.name, dt.refId || ""),
            options: selectOptions_1
          }));
        });
        itemrows.push(refId);
        itemrows.push(fieldItems);
      };

      try {
        for (var _k = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(context.data), _l = _k.next(); !_l.done; _l = _k.next()) {
          var dt = _l.value;

          _loop_1(dt);
        }
      } catch (e_1_1) {
        e_1 = {
          error: e_1_1
        };
      } finally {
        try {
          if (_l && !_l.done && (_b = _k["return"])) _b.call(_k);
        } finally {
          if (e_1) throw e_1.error;
        }
      }

      return react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null, react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("style", null, css), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null, itemrows), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", {
        style: {
          paddingBottom: "20px;"
        }
      }, react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("label", null, "Data Transformation Instructions"), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["TextArea"], {
        placeholder: "Data Transformation Instructions",
        cols: 10,
        value: dataTransform,
        onChange: onDataTransformChange(1),
        name: "dataTransform"
      })), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", {
        style: {
          paddingBottom: "20px;"
        }
      }, react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("label", null, "Data Join Instructions"), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["TextArea"], {
        placeholder: "Data Join Instructions",
        cols: 10,
        value: dataJoin,
        onChange: onDataJoinChange(1)
      })), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", {
        style: {
          paddingBottom: "20px;"
        }
      }, react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("label", null, "Chart Option"), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["TextArea"], {
        placeholder: "Chart Options",
        cols: 10,
        value: chartOptions,
        onChange: onChartOptionsChange(1),
        id: "chartsOptId"
      })), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null, react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["Button"], {
        size: "md",
        onClick: function onClick() {
          renderChart(true);
        }
      }, "Load Chart"), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["Button"], {
        className: "marlft",
        size: "md",
        onClick: function onClick() {
          saveChartOpts();
        }
      }, "Save Chart")), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", null), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", {
        id: "chartEditorContainer"
      }), react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", {
        id: "googleId"
      }));
    }
  }

  return react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_2__["Select"], {
    onChange: function onChange() {},
    disabled: true
  });
};

/***/ }),

/***/ "./GoogleChartUtilities.tsx":
/*!**********************************!*\
  !*** ./GoogleChartUtilities.tsx ***!
  \**********************************/
/*! exports provided: googlechartutilities */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "googlechartutilities", function() { return googlechartutilities; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "../node_modules/tslib/tslib.es6.js");

function googlechartutilities(theme, transformDataInstruction, joinInstructions, google, isEditor) {
  var applyTheme = function applyTheme(chartOptions) {
    var grafanaBootData = window['grafanaBootData'];
    var version = Number(grafanaBootData.settings.buildInfo.version.split(".")[0]);

    if (version >= 5) {
      var textColor = '';
      var fillColor = '';
      fillColor = theme.colors.background.primary;
      textColor = theme.colors.text.primary;
      chartOptions['backgroundColor'] = fillColor;
      var hAxis = chartOptions['hAxis'];

      if (hAxis === undefined) {
        hAxis = {};
        chartOptions['hAxis'] = hAxis;
      }

      var hTextStyle = hAxis['textStyle'];

      if (hTextStyle === undefined) {
        hTextStyle = {};
        hAxis['textStyle'] = hTextStyle;
      }

      hTextStyle['color'] = textColor;
      var legendTextStyle = chartOptions['legendTextStyle'];

      if (legendTextStyle === undefined) {
        legendTextStyle = {};
        chartOptions['legendTextStyle'] = legendTextStyle;
      }

      legendTextStyle['color'] = textColor;
      var vAxes = chartOptions['vAxes'];

      if (vAxes === undefined) {
        vAxes = [{}];
        chartOptions['vAxes'] = vAxes;
      }

      for (var v in vAxes) {
        var vAxis = vAxes[v];
        var vTextStyle = vAxis['textStyle'];

        if (vTextStyle === undefined) {
          vTextStyle = {};
          vAxis['textStyle'] = vTextStyle;
        }

        vTextStyle['color'] = textColor;
      }
    }

    return chartOptions;
  };

  var innerDimensions = function innerDimensions(node) {
    var computedStyle = getComputedStyle(node);
    var width = node.clientWidth; // width with padding

    var height = node.clientHeight; // height with padding

    height -= parseFloat(computedStyle.paddingTop) + parseFloat(computedStyle.paddingBottom);
    width -= parseFloat(computedStyle.paddingLeft) + parseFloat(computedStyle.paddingRight);
    return {
      height: height,
      width: width
    };
  };

  var transformData = function transformData(data) {
    if (transformDataInstruction === undefined || transformDataInstruction === null || transformDataInstruction === "") {
      return data;
    } else {
      var transformDataFunc = new Function('data', transformDataInstruction);
      return transformDataFunc(data);
    }
  };

  var joinDataTables = function joinDataTables(dataTables) {
    if (dataTables && dataTables.length > 0) {
      if (joinInstructions === undefined || joinInstructions === null || joinInstructions === "") {
        return dataTables[0];
      } else {
        var joinFunc = new Function('dataTables', joinInstructions);
        return joinFunc(dataTables);
      }
    }
  };

  var buildDataTables = function buildDataTables(dataArray) {
    var e_1, _a, e_2, _b;

    var dataTables = [];

    if (dataArray) {
      try {
        for (var dataArray_1 = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(dataArray), dataArray_1_1 = dataArray_1.next(); !dataArray_1_1.done; dataArray_1_1 = dataArray_1.next()) {
          var data = dataArray_1_1.value;

          if (data.columns) {
            var typeMapping = [];

            try {
              for (var _c = (e_2 = void 0, Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(data.columns)), _d = _c.next(); !_d.done; _d = _c.next()) {
                var column = _d.value;
                typeMapping.push({
                  label: column.name,
                  type: column.type,
                  refId: column.refId
                });
              }
            } catch (e_2_1) {
              e_2 = {
                error: e_2_1
              };
            } finally {
              try {
                if (_d && !_d.done && (_b = _c["return"])) _b.call(_c);
              } finally {
                if (e_2) throw e_2.error;
              }
            }

            dataTables.push(convertData(data.data, typeMapping));
          }
        }
      } catch (e_1_1) {
        e_1 = {
          error: e_1_1
        };
      } finally {
        try {
          if (dataArray_1_1 && !dataArray_1_1.done && (_a = dataArray_1["return"])) _a.call(dataArray_1);
        } finally {
          if (e_1) throw e_1.error;
        }
      }
    }

    return dataTables;
  };

  var convertData = function convertData(dataRows, typeMapping) {
    var e_3, _a, e_4, _b, e_5, _c;

    var data = new google.visualization.DataTable();

    try {
      for (var typeMapping_1 = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(typeMapping), typeMapping_1_1 = typeMapping_1.next(); !typeMapping_1_1.done; typeMapping_1_1 = typeMapping_1.next()) {
        var column = typeMapping_1_1.value;
        data.addColumn(column['type'], column['label']);
      }
    } catch (e_3_1) {
      e_3 = {
        error: e_3_1
      };
    } finally {
      try {
        if (typeMapping_1_1 && !typeMapping_1_1.done && (_a = typeMapping_1["return"])) _a.call(typeMapping_1);
      } finally {
        if (e_3) throw e_3.error;
      }
    }

    var rowArr = [];
    var dataIter = [];

    if (!isEditor) {
      dataIter = dataRows.series;
    } else {
      dataIter = dataRows;
    }

    var dataRow;

    try {
      for (var dataIter_1 = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(dataIter), dataIter_1_1 = dataIter_1.next(); !dataIter_1_1.done; dataIter_1_1 = dataIter_1.next()) {
        var refD = dataIter_1_1.value;

        if (refD.refId === typeMapping[0].refId) {
          dataRow = refD;
        }
      }
    } catch (e_4_1) {
      e_4 = {
        error: e_4_1
      };
    } finally {
      try {
        if (dataIter_1_1 && !dataIter_1_1.done && (_b = dataIter_1["return"])) _b.call(dataIter_1);
      } finally {
        if (e_4) throw e_4.error;
      }
    }

    for (var i = 0; i < dataRow.length; i++) {
      var row = [];

      var _loop_1 = function _loop_1(fields) {
        var columnObj = typeMapping.find(function (o) {
          return o.label === fields.name;
        });
        row.push(convertToType(fields.values.buffer[i], columnObj === null || columnObj === void 0 ? void 0 : columnObj.type));
      };

      try {
        for (var _d = (e_5 = void 0, Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(dataRow.fields)), _e = _d.next(); !_e.done; _e = _d.next()) {
          var fields = _e.value;

          _loop_1(fields);
        }
      } catch (e_5_1) {
        e_5 = {
          error: e_5_1
        };
      } finally {
        try {
          if (_e && !_e.done && (_c = _d["return"])) _c.call(_d);
        } finally {
          if (e_5) throw e_5.error;
        }
      }

      rowArr.push(row);
    }

    data.addRows(rowArr);
    return data;
  };

  var convertToType = function convertToType(data, type) {
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

        return data === "true";
      } else if (type === 'date') {
        if (typeof data === 'number') {
          var dataStr = data.toString();

          if (dataStr.length < 13) {
            var appendZeros = '0000000000000';
            data = dataStr + appendZeros.substring(0, 13 - dataStr.length);
            return new Date(Number(data));
          } else {
            return new Date(Number(data));
          }
        } else {
          return new Date(data);
        }
      } else if (type === 'datetime') {} else if (type === 'timeofday') {} else {
        throw new TypeError('Unknown Type passed.');
      }
    }
  };

  return {
    buildDataTables: buildDataTables,
    joinDataTables: joinDataTables,
    transformData: transformData,
    innerDimensions: innerDimensions,
    applyTheme: applyTheme
  };
}

/***/ }),

/***/ "./GoogleCharts.tsx":
/*!**************************!*\
  !*** ./GoogleCharts.tsx ***!
  \**************************/
/*! exports provided: GoogleCharts */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "GoogleCharts", function() { return GoogleCharts; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "../node_modules/tslib/tslib.es6.js");
/* harmony import */ var _models_ChartModel__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./models/ChartModel */ "./models/ChartModel.ts");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @grafana/ui */ "@grafana/ui");
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var _GoogleChartUtilities__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./GoogleChartUtilities */ "./GoogleChartUtilities.tsx");





var GoogleCharts = function GoogleCharts(_a) {
  var chartType = _a.chartType,
      chartOptions = _a.chartOptions,
      transformDataInstruction = _a.transformDataInstruction,
      joinInstructions = _a.joinInstructions,
      container = _a.container,
      rootId = _a.rootId,
      data = _a.data,
      isEdit = _a.isEdit,
      columnModel = _a.columnModel;
  var theme = Object(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__["useTheme2"])();
  var google = window.google;

  var _b = Object(_GoogleChartUtilities__WEBPACK_IMPORTED_MODULE_4__["googlechartutilities"])(theme, transformDataInstruction, joinInstructions, google, false),
      buildDataTables = _b.buildDataTables,
      joinDataTables = _b.joinDataTables,
      transformData = _b.transformData,
      innerDimensions = _b.innerDimensions,
      applyTheme = _b.applyTheme;

  var contRef = Object(react__WEBPACK_IMPORTED_MODULE_2__["useRef"])(null);
  var conatinerViewId = "containerIdDiv_";
  var containerEditId = "containerIdDivEdit_";
  Object(react__WEBPACK_IMPORTED_MODULE_2__["useEffect"])(function () {
    google.charts.load('46', {
      'packages': ['corechart', 'charteditor', 'gantt']
    });
    google.charts.setOnLoadCallback(drawChart);
  }, []);

  var drawChart = function drawChart() {
    var e_1, _a;

    var _b, _c, _d, _e;

    var chartDataArr = [];

    var _loop_1 = function _loop_1(dt) {
      var e_2, _h;

      var refId = dt.refId || "";
      var columnModelArr = new Array();

      var _loop_2 = function _loop_2(field) {
        var columnModelObj = void 0;
        columnModelObj = columnModel.find(function (o) {
          return o.name === field.name && o.refId === refId;
        });
        columnModelArr.push(columnModelObj);
      };

      try {
        for (var _j = (e_2 = void 0, Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(dt.fields)), _k = _j.next(); !_k.done; _k = _j.next()) {
          var field = _k.value;

          _loop_2(field);
        }
      } catch (e_2_1) {
        e_2 = {
          error: e_2_1
        };
      } finally {
        try {
          if (_k && !_k.done && (_h = _j["return"])) _h.call(_j);
        } finally {
          if (e_2) throw e_2.error;
        }
      }

      var chartDataObjIndx = chartDataArr.findIndex(function (obj) {
        return obj.id === refId;
      });

      if (chartDataObjIndx != -1) {
        chartDataArr[chartDataObjIndx].data = data;
        chartDataArr[chartDataObjIndx].columns = columnModelArr;
      } else {
        chartDataArr.push(new _models_ChartModel__WEBPACK_IMPORTED_MODULE_1__["ChartData"](refId, data, columnModelArr));
      }
    };

    try {
      for (var _f = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__values"])(data.series), _g = _f.next(); !_g.done; _g = _f.next()) {
        var dt = _g.value;

        _loop_1(dt);
      }
    } catch (e_1_1) {
      e_1 = {
        error: e_1_1
      };
    } finally {
      try {
        if (_g && !_g.done && (_a = _f["return"])) _a.call(_f);
      } finally {
        if (e_1) throw e_1.error;
      }
    }

    var dataTables = buildDataTables(chartDataArr);
    var dataTb = joinDataTables(dataTables);
    dataTb = transformData(dataTb);
    console.log(chartOptions);
    var chartOptionsObj = {};

    if (chartOptions != undefined && chartOptions != "") {
      chartOptionsObj = JSON.parse(chartOptions);
    }

    var parentNd;
    var container = "";

    if (!isEdit) {
      container = conatinerViewId + rootId;
      parentNd = (_c = (_b = document.getElementById(container)) === null || _b === void 0 ? void 0 : _b.parentNode) === null || _c === void 0 ? void 0 : _c.parentNode;
    } else {
      container = containerEditId + rootId;
      parentNd = (_e = (_d = document.getElementById(container)) === null || _d === void 0 ? void 0 : _d.parentNode) === null || _e === void 0 ? void 0 : _e.parentNode;
    }

    var chartDimensions = innerDimensions(parentNd);
    chartOptionsObj["height"] = chartDimensions.height;
    chartOptionsObj["width"] = chartDimensions.width;
    chartOptionsObj = applyTheme(chartOptionsObj);
    var wrapper = new google.visualization.ChartWrapper({
      'chartType': chartType,
      'dataTable': dataTb,
      'containerId': container,
      'options': chartOptionsObj
    });
    wrapper.draw();
  };

  var returnHtml;

  if (!isEdit) {
    returnHtml = react__WEBPACK_IMPORTED_MODULE_2___default.a.createElement("div", {
      id: conatinerViewId + rootId,
      ref: contRef
    });
  } else {
    returnHtml = react__WEBPACK_IMPORTED_MODULE_2___default.a.createElement("div", {
      id: containerEditId + rootId,
      ref: contRef
    });
  }

  return react__WEBPACK_IMPORTED_MODULE_2___default.a.createElement("div", null, returnHtml);
};

/***/ }),

/***/ "./SimplePanel.tsx":
/*!*************************!*\
  !*** ./SimplePanel.tsx ***!
  \*************************/
/*! exports provided: SimplePanel */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SimplePanel", function() { return SimplePanel; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "../node_modules/tslib/tslib.es6.js");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var grafana_plugin_support__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! grafana-plugin-support */ "../node_modules/grafana-plugin-support/dist/index.js");
/* harmony import */ var _GoogleCharts__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./GoogleCharts */ "./GoogleCharts.tsx");




var SimplePanel = function SimplePanel(_a) {
  var _b, _c, _d, _e, _f;

  var options = _a.options,
      data = _a.data,
      width = _a.width,
      height = _a.height,
      timeRange = _a.timeRange,
      onChangeTimeRange = _a.onChangeTimeRange,
      timeZone = _a.timeZone,
      onOptionsChange = _a.onOptionsChange,
      id = _a.id;
  var usage = {
    schema: [],
    url: 'https://github.com/TheCognizantFoundry/Insights'
  };
  var frame = data.series[0];

  if (!frame) {
    return react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement("div", {
      style: {
        width: width,
        height: height
      }
    }, react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(grafana_plugin_support__WEBPACK_IMPORTED_MODULE_2__["PanelWizard"], Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__assign"])({}, usage)));
  }

  var rootID = "insights_" + id;
  var queryString = window.location.search;
  var urlParams = new URLSearchParams(queryString);
  var editMode = urlParams.has('editPanel');
  console.log(options);
  return react__WEBPACK_IMPORTED_MODULE_1___default.a.createElement(_GoogleCharts__WEBPACK_IMPORTED_MODULE_3__["GoogleCharts"], {
    chartType: (_b = options.chartFields) === null || _b === void 0 ? void 0 : _b.chartType,
    chartOptions: (_c = options.chartFields) === null || _c === void 0 ? void 0 : _c.chartOptions,
    transformDataInstruction: (_d = options.chartFields) === null || _d === void 0 ? void 0 : _d.transformDataInstruction,
    joinInstructions: (_e = options.chartFields) === null || _e === void 0 ? void 0 : _e.joinInstructions,
    container: (_f = options.chartFields) === null || _f === void 0 ? void 0 : _f.container,
    rootId: rootID,
    data: data,
    isEdit: editMode,
    columnModel: options.chartFields.columnModel
  });
};

/***/ }),

/***/ "./models/ChartModel.ts":
/*!******************************!*\
  !*** ./models/ChartModel.ts ***!
  \******************************/
/*! exports provided: ChartModel, ColumnModel, ContainerModel, ChartData */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ChartModel", function() { return ChartModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ColumnModel", function() { return ColumnModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ContainerModel", function() { return ContainerModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ChartData", function() { return ChartData; });
var ChartModel =
/** @class */
function () {
  function ChartModel(chartType, chartOptions, dataArray, container, transformDataInstruction, joinInstructions, columnModel) {
    this.chartType = chartType;
    this.chartOptions = chartOptions;
    this.dataArray = dataArray;
    this.container = container;
    this.transformDataInstruction = transformDataInstruction;
    this.joinInstructions = joinInstructions;
    this.columnModel = columnModel;
  }

  return ChartModel;
}();



var ColumnModel =
/** @class */
function () {
  function ColumnModel(name, type, refId) {
    this.name = name;
    this.type = type;
    this.refId = refId;
  }

  return ColumnModel;
}();



var ContainerModel =
/** @class */
function () {
  function ContainerModel(id, height) {
    this.id = id;
    this.height = height;
  }

  return ContainerModel;
}();



var ChartData =
/** @class */
function () {
  function ChartData(id, data, columns) {
    this.id = id;
    this.data = data;
    this.columns = columns;
  }

  return ChartData;
}();



/***/ }),

/***/ "./models/InsightsChartEditorModel.ts":
/*!********************************************!*\
  !*** ./models/InsightsChartEditorModel.ts ***!
  \********************************************/
/*! exports provided: InsightsChartEditorModel, InsightsChartTargetModel */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "InsightsChartEditorModel", function() { return InsightsChartEditorModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "InsightsChartTargetModel", function() { return InsightsChartTargetModel; });
var InsightsChartEditorModel =
/** @class */
function () {
  function InsightsChartEditorModel(targets, transformInstrctions, joinInstructions, chartOptions) {
    this.targets = targets;
    this.transformInstrctions = transformInstrctions;
    this.joinInstructions = joinInstructions;
    this.chartOptions = chartOptions;
  }

  ;
  return InsightsChartEditorModel;
}();



var InsightsChartTargetModel =
/** @class */
function () {
  function InsightsChartTargetModel(id, columnModel) {
    this.id = id;
    this.columnModel = columnModel;
  }

  return InsightsChartTargetModel;
}();



/***/ }),

/***/ "./module.ts":
/*!*******************!*\
  !*** ./module.ts ***!
  \*******************/
/*! exports provided: plugin */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "plugin", function() { return plugin; });
/* harmony import */ var _SimplePanel__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./SimplePanel */ "./SimplePanel.tsx");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _CustomFieldSelectEditor__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./CustomFieldSelectEditor */ "./CustomFieldSelectEditor.tsx");



var plugin = new _grafana_data__WEBPACK_IMPORTED_MODULE_1__["PanelPlugin"](_SimplePanel__WEBPACK_IMPORTED_MODULE_0__["SimplePanel"]).setPanelOptions(function (builder) {
  return builder.addCustomEditor({
    id: 'chartFields',
    path: 'chartFields',
    name: 'ChartFields',
    description: 'Fields required for Insights Charts',
    editor: _CustomFieldSelectEditor__WEBPACK_IMPORTED_MODULE_2__["CustomFieldSelectEditor"]
  });
});

/***/ }),

/***/ "@grafana/data":
/*!********************************!*\
  !*** external "@grafana/data" ***!
  \********************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_data__;

/***/ }),

/***/ "@grafana/runtime":
/*!***********************************!*\
  !*** external "@grafana/runtime" ***!
  \***********************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_runtime__;

/***/ }),

/***/ "@grafana/ui":
/*!******************************!*\
  !*** external "@grafana/ui" ***!
  \******************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_ui__;

/***/ }),

/***/ "emotion":
/*!**************************!*\
  !*** external "emotion" ***!
  \**************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE_emotion__;

/***/ }),

/***/ "jquery":
/*!*************************!*\
  !*** external "jquery" ***!
  \*************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE_jquery__;

/***/ }),

/***/ "lodash":
/*!*************************!*\
  !*** external "lodash" ***!
  \*************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE_lodash__;

/***/ }),

/***/ "react":
/*!************************!*\
  !*** external "react" ***!
  \************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE_react__;

/***/ })

/******/ })});;
//# sourceMappingURL=module.js.map