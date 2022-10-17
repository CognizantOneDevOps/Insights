define(["@grafana/data","@grafana/runtime","emotion","react"], function(__WEBPACK_EXTERNAL_MODULE__grafana_data__, __WEBPACK_EXTERNAL_MODULE__grafana_runtime__, __WEBPACK_EXTERNAL_MODULE_emotion__, __WEBPACK_EXTERNAL_MODULE_react__) { return /******/ (function(modules) { // webpackBootstrap
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

/***/ "../node_modules/css-loader/dist/cjs.js?!../node_modules/postcss-loader/src/index.js?!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css":
/*!*******************************************************************************************************************************************************************!*\
  !*** ../node_modules/css-loader/dist/cjs.js??ref--8-1!../node_modules/postcss-loader/src??ref--8-2!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css ***!
  \*******************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// Imports
var ___CSS_LOADER_API_IMPORT___ = __webpack_require__(/*! ../node_modules/css-loader/dist/runtime/api.js */ "../node_modules/css-loader/dist/runtime/api.js");
exports = ___CSS_LOADER_API_IMPORT___(true);
// Module
exports.push([module.i, "* {\n  box-sizing: border-box;\n}\n\n/* Create two equal columns that floats next to each other */\n.column {\n  float: left;\n  width: 50%;\n  padding: 10px;\n}\n\n/* Clear floats after the columns */\n.row:after {\n  content: \"\";\n  display: table;\n  clear: both;\n}\n\n/* Responsive layout - makes the two columns stack on top of each other instead of next to each other */\n@media screen and (max-width: 600px) {\n  .column {\n    width: 100%;\n  }\n}", "",{"version":3,"sources":["inferenceLayout.css"],"names":[],"mappings":"AAAA;EACE,sBAAsB;AACxB;;AAEA,4DAA4D;AAC5D;EACE,WAAW;EACX,UAAU;EACV,aAAa;AACf;;AAEA,mCAAmC;AACnC;EACE,WAAW;EACX,cAAc;EACd,WAAW;AACb;;AAEA,uGAAuG;AACvG;EACE;IACE,WAAW;EACb;AACF","file":"inferenceLayout.css","sourcesContent":["* {\n  box-sizing: border-box;\n}\n\n/* Create two equal columns that floats next to each other */\n.column {\n  float: left;\n  width: 50%;\n  padding: 10px;\n}\n\n/* Clear floats after the columns */\n.row:after {\n  content: \"\";\n  display: table;\n  clear: both;\n}\n\n/* Responsive layout - makes the two columns stack on top of each other instead of next to each other */\n@media screen and (max-width: 600px) {\n  .column {\n    width: 100%;\n  }\n}"]}]);
// Exports
module.exports = exports;


/***/ }),

/***/ "../node_modules/css-loader/dist/runtime/api.js":
/*!******************************************************!*\
  !*** ../node_modules/css-loader/dist/runtime/api.js ***!
  \******************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/*
  MIT License http://www.opensource.org/licenses/mit-license.php
  Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
// eslint-disable-next-line func-names
module.exports = function (useSourceMap) {
  var list = []; // return the list of modules as css string

  list.toString = function toString() {
    return this.map(function (item) {
      var content = cssWithMappingToString(item, useSourceMap);

      if (item[2]) {
        return "@media ".concat(item[2], " {").concat(content, "}");
      }

      return content;
    }).join('');
  }; // import a list of modules into the list
  // eslint-disable-next-line func-names


  list.i = function (modules, mediaQuery, dedupe) {
    if (typeof modules === 'string') {
      // eslint-disable-next-line no-param-reassign
      modules = [[null, modules, '']];
    }

    var alreadyImportedModules = {};

    if (dedupe) {
      for (var i = 0; i < this.length; i++) {
        // eslint-disable-next-line prefer-destructuring
        var id = this[i][0];

        if (id != null) {
          alreadyImportedModules[id] = true;
        }
      }
    }

    for (var _i = 0; _i < modules.length; _i++) {
      var item = [].concat(modules[_i]);

      if (dedupe && alreadyImportedModules[item[0]]) {
        // eslint-disable-next-line no-continue
        continue;
      }

      if (mediaQuery) {
        if (!item[2]) {
          item[2] = mediaQuery;
        } else {
          item[2] = "".concat(mediaQuery, " and ").concat(item[2]);
        }
      }

      list.push(item);
    }
  };

  return list;
};

function cssWithMappingToString(item, useSourceMap) {
  var content = item[1] || ''; // eslint-disable-next-line prefer-destructuring

  var cssMapping = item[3];

  if (!cssMapping) {
    return content;
  }

  if (useSourceMap && typeof btoa === 'function') {
    var sourceMapping = toComment(cssMapping);
    var sourceURLs = cssMapping.sources.map(function (source) {
      return "/*# sourceURL=".concat(cssMapping.sourceRoot || '').concat(source, " */");
    });
    return [content].concat(sourceURLs).concat([sourceMapping]).join('\n');
  }

  return [content].join('\n');
} // Adapted from convert-source-map (MIT)


function toComment(sourceMap) {
  // eslint-disable-next-line no-undef
  var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap))));
  var data = "sourceMappingURL=data:application/json;charset=utf-8;base64,".concat(base64);
  return "/*# ".concat(data, " */");
}

/***/ }),

/***/ "../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js":
/*!*****************************************************************************!*\
  !*** ../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js ***!
  \*****************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var isOldIE = function isOldIE() {
  var memo;
  return function memorize() {
    if (typeof memo === 'undefined') {
      // Test for IE <= 9 as proposed by Browserhacks
      // @see http://browserhacks.com/#hack-e71d8692f65334173fee715c222cb805
      // Tests for existence of standard globals is to allow style-loader
      // to operate correctly into non-standard environments
      // @see https://github.com/webpack-contrib/style-loader/issues/177
      memo = Boolean(window && document && document.all && !window.atob);
    }

    return memo;
  };
}();

var getTarget = function getTarget() {
  var memo = {};
  return function memorize(target) {
    if (typeof memo[target] === 'undefined') {
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
  };
}();

var stylesInDom = [];

function getIndexByIdentifier(identifier) {
  var result = -1;

  for (var i = 0; i < stylesInDom.length; i++) {
    if (stylesInDom[i].identifier === identifier) {
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
    var index = getIndexByIdentifier(identifier);
    var obj = {
      css: item[1],
      media: item[2],
      sourceMap: item[3]
    };

    if (index !== -1) {
      stylesInDom[index].references++;
      stylesInDom[index].updater(obj);
    } else {
      stylesInDom.push({
        identifier: identifier,
        updater: addStyle(obj, options),
        references: 1
      });
    }

    identifiers.push(identifier);
  }

  return identifiers;
}

function insertStyleElement(options) {
  var style = document.createElement('style');
  var attributes = options.attributes || {};

  if (typeof attributes.nonce === 'undefined') {
    var nonce =  true ? __webpack_require__.nc : undefined;

    if (nonce) {
      attributes.nonce = nonce;
    }
  }

  Object.keys(attributes).forEach(function (key) {
    style.setAttribute(key, attributes[key]);
  });

  if (typeof options.insert === 'function') {
    options.insert(style);
  } else {
    var target = getTarget(options.insert || 'head');

    if (!target) {
      throw new Error("Couldn't find a style target. This probably means that the value for the 'insert' parameter is invalid.");
    }

    target.appendChild(style);
  }

  return style;
}

function removeStyleElement(style) {
  // istanbul ignore if
  if (style.parentNode === null) {
    return false;
  }

  style.parentNode.removeChild(style);
}
/* istanbul ignore next  */


var replaceText = function replaceText() {
  var textStore = [];
  return function replace(index, replacement) {
    textStore[index] = replacement;
    return textStore.filter(Boolean).join('\n');
  };
}();

function applyToSingletonTag(style, index, remove, obj) {
  var css = remove ? '' : obj.media ? "@media ".concat(obj.media, " {").concat(obj.css, "}") : obj.css; // For old IE

  /* istanbul ignore if  */

  if (style.styleSheet) {
    style.styleSheet.cssText = replaceText(index, css);
  } else {
    var cssNode = document.createTextNode(css);
    var childNodes = style.childNodes;

    if (childNodes[index]) {
      style.removeChild(childNodes[index]);
    }

    if (childNodes.length) {
      style.insertBefore(cssNode, childNodes[index]);
    } else {
      style.appendChild(cssNode);
    }
  }
}

function applyToTag(style, options, obj) {
  var css = obj.css;
  var media = obj.media;
  var sourceMap = obj.sourceMap;

  if (media) {
    style.setAttribute('media', media);
  } else {
    style.removeAttribute('media');
  }

  if (sourceMap && btoa) {
    css += "\n/*# sourceMappingURL=data:application/json;base64,".concat(btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))), " */");
  } // For old IE

  /* istanbul ignore if  */


  if (style.styleSheet) {
    style.styleSheet.cssText = css;
  } else {
    while (style.firstChild) {
      style.removeChild(style.firstChild);
    }

    style.appendChild(document.createTextNode(css));
  }
}

var singleton = null;
var singletonCounter = 0;

function addStyle(obj, options) {
  var style;
  var update;
  var remove;

  if (options.singleton) {
    var styleIndex = singletonCounter++;
    style = singleton || (singleton = insertStyleElement(options));
    update = applyToSingletonTag.bind(null, style, styleIndex, false);
    remove = applyToSingletonTag.bind(null, style, styleIndex, true);
  } else {
    style = insertStyleElement(options);
    update = applyToTag.bind(null, style, options);

    remove = function remove() {
      removeStyleElement(style);
    };
  }

  update(obj);
  return function updateStyle(newObj) {
    if (newObj) {
      if (newObj.css === obj.css && newObj.media === obj.media && newObj.sourceMap === obj.sourceMap) {
        return;
      }

      update(obj = newObj);
    } else {
      remove();
    }
  };
}

module.exports = function (list, options) {
  options = options || {}; // Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
  // tags it will allow on a page

  if (!options.singleton && typeof options.singleton !== 'boolean') {
    options.singleton = isOldIE();
  }

  list = list || [];
  var lastIdentifiers = modulesToDom(list, options);
  return function update(newList) {
    newList = newList || [];

    if (Object.prototype.toString.call(newList) !== '[object Array]') {
      return;
    }

    for (var i = 0; i < lastIdentifiers.length; i++) {
      var identifier = lastIdentifiers[i];
      var index = getIndexByIdentifier(identifier);
      stylesInDom[index].references--;
    }

    var newLastIdentifiers = modulesToDom(newList, options);

    for (var _i = 0; _i < lastIdentifiers.length; _i++) {
      var _identifier = lastIdentifiers[_i];

      var _index = getIndexByIdentifier(_identifier);

      if (stylesInDom[_index].references === 0) {
        stylesInDom[_index].updater();

        stylesInDom.splice(_index, 1);
      }
    }

    lastIdentifiers = newLastIdentifiers;
  };
};

/***/ }),

/***/ "./InferencePanel.tsx":
/*!****************************!*\
  !*** ./InferencePanel.tsx ***!
  \****************************/
/*! exports provided: InferencePanel */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "InferencePanel", function() { return InferencePanel; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var emotion__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! emotion */ "emotion");
/* harmony import */ var emotion__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(emotion__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _inferenceUtil__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./inferenceUtil */ "./inferenceUtil.tsx");
/* harmony import */ var _inferenceLayout_css__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./inferenceLayout.css */ "./inferenceLayout.css");
/* harmony import */ var _inferenceLayout_css__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(_inferenceLayout_css__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @grafana/runtime */ "@grafana/runtime");
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(_grafana_runtime__WEBPACK_IMPORTED_MODULE_4__);
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }

var _templateObject;

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

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
      var data = Object(_inferenceUtil__WEBPACK_IMPORTED_MODULE_2__["processData"])(this.props); //console.log('processed data--', data);

      var google = window.google; //console.log('google--',google);

      google.charts.load('46', {
        'packages': ['corechart', 'charteditor', 'gantt']
      });
      google.charts.setOnLoadCallback(this.googleChart);
      var inferenceLists;

      if (data.length == 0) {
        inferenceLists = "No Records found!";
      } else {
        inferenceLists = data[0].data.map(function (link) {
          return link.color === 'green' ? react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
            style: {
              display: 'flex',
              alignItems: 'center'
            },
            onMouseEnter: function onMouseEnter() {
              _this.getInference(link, options);
            }
          }, react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("img", {
            src: "public/img/satisfied.png"
          }), react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("span", {
            style: {
              padding: '5px'
            }
          }, link.inference)) : react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
            style: {
              display: 'flex',
              alignItems: 'center'
            },
            onMouseEnter: function onMouseEnter() {
              _this.getInference(link, options);
            }
          }, react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("img", {
            src: "public/img/dissatisfied.png"
          }), react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("span", {
            style: {
              padding: '5px'
            }
          }, link.inference));
        });
      }

      return react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        style: {
          height: '100%',
          width: '100%'
        },
        className: Object(emotion__WEBPACK_IMPORTED_MODULE_1__["cx"])("position:relative", Object(emotion__WEBPACK_IMPORTED_MODULE_1__["css"])(_templateObject || (_templateObject = _taggedTemplateLiteral(["width: ", "px;height: ", "px;"])), this.props.width, this.props.height))
      }, react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "row"
      }, react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "column",
        id: "inference-lists"
      }, react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("ul", null, inferenceLists)), options.enableFusion && react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "column",
        id: fusionID
      }, this.fusionChart()), !options.enableFusion && react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
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
      palettecolors = link.color == 'green' ? '#008000' : '#FF0000'; //console.log('pale--',palettecolors);

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
      var theme = _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__["config"].theme;
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

      var options = {
        'title': chartCaption
      };

      if (window['grafanaBootData'].user.lightTheme) {
        options['backgroundColor'] = _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__["config"].theme.colors.bg1;
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
        options['backgroundColor'] = _grafana_runtime__WEBPACK_IMPORTED_MODULE_4__["config"].theme.colors.bg1;
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

      if (this.props.options.googleChartType == 'bar') {
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
}(react__WEBPACK_IMPORTED_MODULE_0___default.a.Component);
;

/***/ }),

/***/ "./inferenceLayout.css":
/*!*****************************!*\
  !*** ./inferenceLayout.css ***!
  \*****************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var api = __webpack_require__(/*! ../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js */ "../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js");
            var content = __webpack_require__(/*! !../node_modules/css-loader/dist/cjs.js??ref--8-1!../node_modules/postcss-loader/src??ref--8-2!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css */ "../node_modules/css-loader/dist/cjs.js?!../node_modules/postcss-loader/src/index.js?!../node_modules/sass-loader/dist/cjs.js!./inferenceLayout.css");

            content = content.__esModule ? content.default : content;

            if (typeof content === 'string') {
              content = [[module.i, content, '']];
            }

var options = {};

options.insert = "head";
options.singleton = false;

var update = api(content, options);

var exported = content.locals ? content.locals : {};



module.exports = exported;

/***/ }),

/***/ "./inferenceUtil.tsx":
/*!***************************!*\
  !*** ./inferenceUtil.tsx ***!
  \***************************/
/*! exports provided: processData */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "processData", function() { return processData; });
function processData(props) {
  var jsonArrtoStr = [];

  if (props.data.state == 'Done') {
    if (props.data.series.length > 0) {
      jsonArrtoStr = props.data.series[0].source;
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

        if (data["resultSet"].length != undefined) {
          data.resultSet.forEach(function (x) {
            return resultArray.push({
              'label': x.resultDate,
              'value': x.value
            });
          });
        }

        vectorProperty["resultSet"] = resultArray;

        if (data["sentiment"] == "POSITIVE" && data["trendline"] == "High to Low") {
          vectorProperty["color"] = "green";
          vectorProperty["type"] = "increased";
          googleChartData[data["kpiId"]].push("green");
        } else if (data["sentiment"] == "POSITIVE" && data["trendline"] == "Low to High") {
          vectorProperty["color"] = "green";
          vectorProperty["type"] = "increased";
          googleChartData[data["kpiId"]].push("green");
        } else if (data["sentiment"] == "NEGATIVE" && data["trendline"] == "Low to High") {
          vectorProperty["color"] = "red";
          vectorProperty["type"] = "increased";
          googleChartData[data["kpiId"]].push("red");
        } else if (data["sentiment"] == "NEGATIVE" && data["trendline"] == "High to Low") {
          vectorProperty["color"] = "red";
          vectorProperty["type"] = "decreased";
          googleChartData[data["kpiId"]].push("red");
        } else if (data["sentiment"] == "NEUTRAL") {
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
} //Sample data to test inference without datasource

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

/***/ "./module.ts":
/*!*******************!*\
  !*** ./module.ts ***!
  \*******************/
/*! exports provided: plugin */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "plugin", function() { return plugin; });
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _InferencePanel__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./InferencePanel */ "./InferencePanel.tsx");


var plugin = new _grafana_data__WEBPACK_IMPORTED_MODULE_0__["PanelPlugin"](_InferencePanel__WEBPACK_IMPORTED_MODULE_1__["InferencePanel"]).setPanelOptions(function (builder) {
  return builder
  /*.addTextInput({
    path: 'text',
    name: 'Simple text option',
    description: 'Description of panel option',
    defaultValue: 'Default value of text input option',
  })*/
  .addBooleanSwitch({
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

/***/ "emotion":
/*!**************************!*\
  !*** external "emotion" ***!
  \**************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE_emotion__;

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
