define(["@grafana/data","@grafana/runtime","@grafana/ui","react","rxjs"], (__WEBPACK_EXTERNAL_MODULE__grafana_data__, __WEBPACK_EXTERNAL_MODULE__grafana_runtime__, __WEBPACK_EXTERNAL_MODULE__grafana_ui__, __WEBPACK_EXTERNAL_MODULE_react__, __WEBPACK_EXTERNAL_MODULE_rxjs__) => { return /******/ (() => { // webpackBootstrap
/******/ 	var __webpack_modules__ = ({

/***/ "./AnnotationQueryEditor.ts":
/*!**********************************!*\
  !*** ./AnnotationQueryEditor.ts ***!
  \**********************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "AnnotationQueryEditor": () => (/* binding */ AnnotationQueryEditor)
/* harmony export */ });
function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }
function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); Object.defineProperty(Constructor, "prototype", { writable: false }); return Constructor; }
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
var AnnotationQueryEditor = /*#__PURE__*/_createClass(function AnnotationQueryEditor() {
  _classCallCheck(this, AnnotationQueryEditor);
  _defineProperty(this, "annotation", void 0);
  this.annotation.rawQuery = this.annotation.rawQuery || '';
});
_defineProperty(AnnotationQueryEditor, "templateUrl", 'partials/annotations.editor.html');

/***/ }),

/***/ "./CheatSheet.tsx":
/*!************************!*\
  !*** ./CheatSheet.tsx ***!
  \************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "default": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
var _h;

var examples = [{
  title: 'Fetch top 25 nodes of JIRA',
  expression: 'MATCH (n:JIRA) RETURN n LIMIT 25',
  label: 'TOP JIRA NODES'
}];
var CheatSheet = function CheatSheet(props) {
  return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", null, _h || (_h = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("h2", null, "Cheat Sheet")), examples.map(function (item, index) {
    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
      className: "cheat-sheet-item",
      key: index
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
      className: "cheat-sheet-item__title"
    }, item.title), item.expression ? /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
      className: "cheat-sheet-item__example",
      onClick: function onClick(e) {
        return props.onClickExample({
          refId: 'A',
          queryText: item.expression
        });
      }
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("code", null, item.expression)) : null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
      className: "cheat-sheet-item__label"
    }, item.label));
  }));
};
/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (CheatSheet);

/***/ }),

/***/ "./ConfigEditor.tsx":
/*!**************************!*\
  !*** ./ConfigEditor.tsx ***!
  \**************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ConfigEditor": () => (/* binding */ ConfigEditor)
/* harmony export */ });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @grafana/ui */ "@grafana/ui");
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__);
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
var _h, _h2;
function _extends() { _extends = Object.assign ? Object.assign.bind() : function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; }; return _extends.apply(this, arguments); }
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
function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }


var logapi = window.location.protocol + "//localhost:8080/PlatformService/externalApi/Neo4jPluginLog/logDashboardInfo";
if (document.domain !== 'localhost') {
  logapi = window.location.protocol + "//" + document.domain + "/PlatformService/externalApi/Neo4jPluginLog/logDashboardInfo";
}
var ConfigEditor = /*#__PURE__*/function (_PureComponent) {
  _inherits(ConfigEditor, _PureComponent);
  var _super = _createSuper(ConfigEditor);
  function ConfigEditor() {
    var _this;
    _classCallCheck(this, ConfigEditor);
    for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }
    _this = _super.call.apply(_super, [this].concat(args));
    _defineProperty(_assertThisInitialized(_this), "onPathChange", function (event) {
      var _this$props = _this.props,
        onOptionsChange = _this$props.onOptionsChange,
        options = _this$props.options;
      var jsonData = _extends({}, options.jsonData, {
        path: event.target.value
      });
      onOptionsChange(_extends({}, options, {
        jsonData: jsonData
      }));
    });
    _defineProperty(_assertThisInitialized(_this), "onAPIKeyChange", function (event) {
      var _this$props2 = _this.props,
        onOptionsChange = _this$props2.onOptionsChange,
        options = _this$props2.options;
      onOptionsChange(_extends({}, options, {
        secureJsonData: {
          apiKey: event.target.value
        }
      }));
    });
    _defineProperty(_assertThisInitialized(_this), "onResetAPIKey", function () {
      var _this$props3 = _this.props,
        onOptionsChange = _this$props3.onOptionsChange,
        options = _this$props3.options;
      onOptionsChange(_extends({}, options, {
        secureJsonFields: _extends({}, options.secureJsonFields, {
          apiKey: false
        }),
        secureJsonData: _extends({}, options.secureJsonData, {
          apiKey: ''
        })
      }));
    });
    _defineProperty(_assertThisInitialized(_this), "onServiceUrlChange", function (event) {
      var _this$props4 = _this.props,
        onOptionsChange = _this$props4.onOptionsChange,
        options = _this$props4.options;
      var jsonData = _extends({}, options.jsonData, {
        serviceUrl: event.target.value
      });
      onOptionsChange(_extends({}, options, {
        jsonData: jsonData
      }));
    });
    _defineProperty(_assertThisInitialized(_this), "onSwitched", function (event) {
      var _this$props5 = _this.props,
        onOptionsChange = _this$props5.onOptionsChange,
        options = _this$props5.options;
      var jsonData = _extends({}, options.jsonData, {
        logging: event.target.checked
      });
      onOptionsChange(_extends({}, options, {
        jsonData: jsonData
      }));
    });
    _defineProperty(_assertThisInitialized(_this), "onBasicPwdChange", function (event) {
      var _this$props6 = _this.props,
        onOptionsChange = _this$props6.onOptionsChange,
        options = _this$props6.options;
      var jsonData = _extends({}, options.jsonData, {
        basicPassword: event.target.value
      });
      onOptionsChange(_extends({}, options, {
        jsonData: jsonData
      }));
    });
    _defineProperty(_assertThisInitialized(_this), "onAuthEnable", function (event) {
      var _this$props7 = _this.props,
        onOptionsChange = _this$props7.onOptionsChange,
        options = _this$props7.options;
      var jsonData = _extends({}, options.jsonData, {
        authToken: event.target.checked
      });
      onOptionsChange(_extends({}, options, {
        jsonData: jsonData
      }));
    });
    return _this;
  }
  _createClass(ConfigEditor, [{
    key: "render",
    value: function render() {
      console.log(this.props.options.jsonData);
      var _this$props8 = this.props,
        options = _this$props8.options,
        onOptionsChange = _this$props8.onOptionsChange;
      var jsonData = options.jsonData,
        secureJsonFields = options.secureJsonFields;
      var secureJsonData = options.secureJsonData || {};
      options.jsonData.serviceUrl = options.jsonData.serviceUrl == undefined ? logapi : options.jsonData.serviceUrl;
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement((react__WEBPACK_IMPORTED_MODULE_0___default().Fragment), null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "gf-form-group"
      }, _h || (_h = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("h3", {
        className: "page-heading"
      }, "Enable Auth Token")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.LegacyForms.Switch, {
        label: "Enable Token Auth",
        labelClass: "width-8",
        checked: options.jsonData.authToken,
        onChange: this.onAuthEnable
      }), options.jsonData.authToken && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement((react__WEBPACK_IMPORTED_MODULE_0___default().Fragment), null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "gf-form"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.LegacyForms.FormField, {
        label: "Path",
        labelWidth: 6,
        inputWidth: 20,
        onChange: this.onPathChange,
        value: jsonData.path || '',
        placeholder: "External API url"
      })), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "gf-form-inline"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("div", {
        className: "gf-form"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.LegacyForms.SecretFormField, {
        isConfigured: secureJsonFields && secureJsonFields.apiKey,
        value: secureJsonData.apiKey || '',
        label: "API Key",
        placeholder: "secure json field (backend only)",
        labelWidth: 6,
        inputWidth: 20,
        onReset: this.onResetAPIKey,
        onChange: this.onAPIKeyChange
      }))))), !options.jsonData.authToken && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement((react__WEBPACK_IMPORTED_MODULE_0___default().Fragment), null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.DataSourceHttpSettings, {
        defaultUrl: "http://localhost:7474/db/data/transaction/commit?includeStats=true",
        dataSourceConfig: options,
        showAccessOptions: true,
        onChange: onOptionsChange
      })), _h2 || (_h2 = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement("h3", {
        className: "page-heading"
      }, "Enable Logging")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.LegacyForms.Switch, {
        label: "Enable Logging",
        labelClass: "width-8",
        checked: options.jsonData.logging,
        onChange: this.onSwitched
      }), options.jsonData.logging && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement((react__WEBPACK_IMPORTED_MODULE_0___default().Fragment), null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.Field, {
        label: "Service URL",
        description: "This is required only when insights usage is collected."
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.Input, {
        name: "serviceUrl",
        onChange: this.onServiceUrlChange,
        value: options.jsonData.serviceUrl,
        placeholder: "External API url"
      })), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_1__.LegacyForms.FormField, {
        label: "Password",
        labelWidth: 6,
        inputWidth: 20,
        onChange: this.onBasicPwdChange,
        value: jsonData.basicPassword || '',
        placeholder: "Base64 password without Basic"
      })));
    }
  }]);
  return ConfigEditor;
}(react__WEBPACK_IMPORTED_MODULE_0__.PureComponent);

/***/ }),

/***/ "./DataSource.ts":
/*!***********************!*\
  !*** ./DataSource.ts ***!
  \***********************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "DataSource": () => (/* binding */ DataSource)
/* harmony export */ });
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @grafana/runtime */ "@grafana/runtime");
/* harmony import */ var _grafana_runtime__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_grafana_runtime__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var rxjs__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! rxjs */ "rxjs");
/* harmony import */ var rxjs__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(rxjs__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var Utils__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! Utils */ "./Utils.ts");
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
function _regeneratorRuntime() { "use strict"; /*! regenerator-runtime -- Copyright (c) 2014-present, Facebook, Inc. -- license (MIT): https://github.com/facebook/regenerator/blob/main/LICENSE */ _regeneratorRuntime = function _regeneratorRuntime() { return exports; }; var exports = {}, Op = Object.prototype, hasOwn = Op.hasOwnProperty, defineProperty = Object.defineProperty || function (obj, key, desc) { obj[key] = desc.value; }, $Symbol = "function" == typeof Symbol ? Symbol : {}, iteratorSymbol = $Symbol.iterator || "@@iterator", asyncIteratorSymbol = $Symbol.asyncIterator || "@@asyncIterator", toStringTagSymbol = $Symbol.toStringTag || "@@toStringTag"; function define(obj, key, value) { return Object.defineProperty(obj, key, { value: value, enumerable: !0, configurable: !0, writable: !0 }), obj[key]; } try { define({}, ""); } catch (err) { define = function define(obj, key, value) { return obj[key] = value; }; } function wrap(innerFn, outerFn, self, tryLocsList) { var protoGenerator = outerFn && outerFn.prototype instanceof Generator ? outerFn : Generator, generator = Object.create(protoGenerator.prototype), context = new Context(tryLocsList || []); return defineProperty(generator, "_invoke", { value: makeInvokeMethod(innerFn, self, context) }), generator; } function tryCatch(fn, obj, arg) { try { return { type: "normal", arg: fn.call(obj, arg) }; } catch (err) { return { type: "throw", arg: err }; } } exports.wrap = wrap; var ContinueSentinel = {}; function Generator() {} function GeneratorFunction() {} function GeneratorFunctionPrototype() {} var IteratorPrototype = {}; define(IteratorPrototype, iteratorSymbol, function () { return this; }); var getProto = Object.getPrototypeOf, NativeIteratorPrototype = getProto && getProto(getProto(values([]))); NativeIteratorPrototype && NativeIteratorPrototype !== Op && hasOwn.call(NativeIteratorPrototype, iteratorSymbol) && (IteratorPrototype = NativeIteratorPrototype); var Gp = GeneratorFunctionPrototype.prototype = Generator.prototype = Object.create(IteratorPrototype); function defineIteratorMethods(prototype) { ["next", "throw", "return"].forEach(function (method) { define(prototype, method, function (arg) { return this._invoke(method, arg); }); }); } function AsyncIterator(generator, PromiseImpl) { function invoke(method, arg, resolve, reject) { var record = tryCatch(generator[method], generator, arg); if ("throw" !== record.type) { var result = record.arg, value = result.value; return value && "object" == _typeof(value) && hasOwn.call(value, "__await") ? PromiseImpl.resolve(value.__await).then(function (value) { invoke("next", value, resolve, reject); }, function (err) { invoke("throw", err, resolve, reject); }) : PromiseImpl.resolve(value).then(function (unwrapped) { result.value = unwrapped, resolve(result); }, function (error) { return invoke("throw", error, resolve, reject); }); } reject(record.arg); } var previousPromise; defineProperty(this, "_invoke", { value: function value(method, arg) { function callInvokeWithMethodAndArg() { return new PromiseImpl(function (resolve, reject) { invoke(method, arg, resolve, reject); }); } return previousPromise = previousPromise ? previousPromise.then(callInvokeWithMethodAndArg, callInvokeWithMethodAndArg) : callInvokeWithMethodAndArg(); } }); } function makeInvokeMethod(innerFn, self, context) { var state = "suspendedStart"; return function (method, arg) { if ("executing" === state) throw new Error("Generator is already running"); if ("completed" === state) { if ("throw" === method) throw arg; return doneResult(); } for (context.method = method, context.arg = arg;;) { var delegate = context.delegate; if (delegate) { var delegateResult = maybeInvokeDelegate(delegate, context); if (delegateResult) { if (delegateResult === ContinueSentinel) continue; return delegateResult; } } if ("next" === context.method) context.sent = context._sent = context.arg;else if ("throw" === context.method) { if ("suspendedStart" === state) throw state = "completed", context.arg; context.dispatchException(context.arg); } else "return" === context.method && context.abrupt("return", context.arg); state = "executing"; var record = tryCatch(innerFn, self, context); if ("normal" === record.type) { if (state = context.done ? "completed" : "suspendedYield", record.arg === ContinueSentinel) continue; return { value: record.arg, done: context.done }; } "throw" === record.type && (state = "completed", context.method = "throw", context.arg = record.arg); } }; } function maybeInvokeDelegate(delegate, context) { var method = delegate.iterator[context.method]; if (undefined === method) { if (context.delegate = null, "throw" === context.method) { if (delegate.iterator["return"] && (context.method = "return", context.arg = undefined, maybeInvokeDelegate(delegate, context), "throw" === context.method)) return ContinueSentinel; context.method = "throw", context.arg = new TypeError("The iterator does not provide a 'throw' method"); } return ContinueSentinel; } var record = tryCatch(method, delegate.iterator, context.arg); if ("throw" === record.type) return context.method = "throw", context.arg = record.arg, context.delegate = null, ContinueSentinel; var info = record.arg; return info ? info.done ? (context[delegate.resultName] = info.value, context.next = delegate.nextLoc, "return" !== context.method && (context.method = "next", context.arg = undefined), context.delegate = null, ContinueSentinel) : info : (context.method = "throw", context.arg = new TypeError("iterator result is not an object"), context.delegate = null, ContinueSentinel); } function pushTryEntry(locs) { var entry = { tryLoc: locs[0] }; 1 in locs && (entry.catchLoc = locs[1]), 2 in locs && (entry.finallyLoc = locs[2], entry.afterLoc = locs[3]), this.tryEntries.push(entry); } function resetTryEntry(entry) { var record = entry.completion || {}; record.type = "normal", delete record.arg, entry.completion = record; } function Context(tryLocsList) { this.tryEntries = [{ tryLoc: "root" }], tryLocsList.forEach(pushTryEntry, this), this.reset(!0); } function values(iterable) { if (iterable) { var iteratorMethod = iterable[iteratorSymbol]; if (iteratorMethod) return iteratorMethod.call(iterable); if ("function" == typeof iterable.next) return iterable; if (!isNaN(iterable.length)) { var i = -1, next = function next() { for (; ++i < iterable.length;) { if (hasOwn.call(iterable, i)) return next.value = iterable[i], next.done = !1, next; } return next.value = undefined, next.done = !0, next; }; return next.next = next; } } return { next: doneResult }; } function doneResult() { return { value: undefined, done: !0 }; } return GeneratorFunction.prototype = GeneratorFunctionPrototype, defineProperty(Gp, "constructor", { value: GeneratorFunctionPrototype, configurable: !0 }), defineProperty(GeneratorFunctionPrototype, "constructor", { value: GeneratorFunction, configurable: !0 }), GeneratorFunction.displayName = define(GeneratorFunctionPrototype, toStringTagSymbol, "GeneratorFunction"), exports.isGeneratorFunction = function (genFun) { var ctor = "function" == typeof genFun && genFun.constructor; return !!ctor && (ctor === GeneratorFunction || "GeneratorFunction" === (ctor.displayName || ctor.name)); }, exports.mark = function (genFun) { return Object.setPrototypeOf ? Object.setPrototypeOf(genFun, GeneratorFunctionPrototype) : (genFun.__proto__ = GeneratorFunctionPrototype, define(genFun, toStringTagSymbol, "GeneratorFunction")), genFun.prototype = Object.create(Gp), genFun; }, exports.awrap = function (arg) { return { __await: arg }; }, defineIteratorMethods(AsyncIterator.prototype), define(AsyncIterator.prototype, asyncIteratorSymbol, function () { return this; }), exports.AsyncIterator = AsyncIterator, exports.async = function (innerFn, outerFn, self, tryLocsList, PromiseImpl) { void 0 === PromiseImpl && (PromiseImpl = Promise); var iter = new AsyncIterator(wrap(innerFn, outerFn, self, tryLocsList), PromiseImpl); return exports.isGeneratorFunction(outerFn) ? iter : iter.next().then(function (result) { return result.done ? result.value : iter.next(); }); }, defineIteratorMethods(Gp), define(Gp, toStringTagSymbol, "Generator"), define(Gp, iteratorSymbol, function () { return this; }), define(Gp, "toString", function () { return "[object Generator]"; }), exports.keys = function (val) { var object = Object(val), keys = []; for (var key in object) { keys.push(key); } return keys.reverse(), function next() { for (; keys.length;) { var key = keys.pop(); if (key in object) return next.value = key, next.done = !1, next; } return next.done = !0, next; }; }, exports.values = values, Context.prototype = { constructor: Context, reset: function reset(skipTempReset) { if (this.prev = 0, this.next = 0, this.sent = this._sent = undefined, this.done = !1, this.delegate = null, this.method = "next", this.arg = undefined, this.tryEntries.forEach(resetTryEntry), !skipTempReset) for (var name in this) { "t" === name.charAt(0) && hasOwn.call(this, name) && !isNaN(+name.slice(1)) && (this[name] = undefined); } }, stop: function stop() { this.done = !0; var rootRecord = this.tryEntries[0].completion; if ("throw" === rootRecord.type) throw rootRecord.arg; return this.rval; }, dispatchException: function dispatchException(exception) { if (this.done) throw exception; var context = this; function handle(loc, caught) { return record.type = "throw", record.arg = exception, context.next = loc, caught && (context.method = "next", context.arg = undefined), !!caught; } for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i], record = entry.completion; if ("root" === entry.tryLoc) return handle("end"); if (entry.tryLoc <= this.prev) { var hasCatch = hasOwn.call(entry, "catchLoc"), hasFinally = hasOwn.call(entry, "finallyLoc"); if (hasCatch && hasFinally) { if (this.prev < entry.catchLoc) return handle(entry.catchLoc, !0); if (this.prev < entry.finallyLoc) return handle(entry.finallyLoc); } else if (hasCatch) { if (this.prev < entry.catchLoc) return handle(entry.catchLoc, !0); } else { if (!hasFinally) throw new Error("try statement without catch or finally"); if (this.prev < entry.finallyLoc) return handle(entry.finallyLoc); } } } }, abrupt: function abrupt(type, arg) { for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i]; if (entry.tryLoc <= this.prev && hasOwn.call(entry, "finallyLoc") && this.prev < entry.finallyLoc) { var finallyEntry = entry; break; } } finallyEntry && ("break" === type || "continue" === type) && finallyEntry.tryLoc <= arg && arg <= finallyEntry.finallyLoc && (finallyEntry = null); var record = finallyEntry ? finallyEntry.completion : {}; return record.type = type, record.arg = arg, finallyEntry ? (this.method = "next", this.next = finallyEntry.finallyLoc, ContinueSentinel) : this.complete(record); }, complete: function complete(record, afterLoc) { if ("throw" === record.type) throw record.arg; return "break" === record.type || "continue" === record.type ? this.next = record.arg : "return" === record.type ? (this.rval = this.arg = record.arg, this.method = "return", this.next = "end") : "normal" === record.type && afterLoc && (this.next = afterLoc), ContinueSentinel; }, finish: function finish(finallyLoc) { for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i]; if (entry.finallyLoc === finallyLoc) return this.complete(entry.completion, entry.afterLoc), resetTryEntry(entry), ContinueSentinel; } }, "catch": function _catch(tryLoc) { for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i]; if (entry.tryLoc === tryLoc) { var record = entry.completion; if ("throw" === record.type) { var thrown = record.arg; resetTryEntry(entry); } return thrown; } } throw new Error("illegal catch attempt"); }, delegateYield: function delegateYield(iterable, resultName, nextLoc) { return this.delegate = { iterator: values(iterable), resultName: resultName, nextLoc: nextLoc }, "next" === this.method && (this.arg = undefined), ContinueSentinel; } }, exports; }
function asyncGeneratorStep(gen, resolve, reject, _next, _throw, key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { Promise.resolve(value).then(_next, _throw); } }
function _asyncToGenerator(fn) { return function () { var self = this, args = arguments; return new Promise(function (resolve, reject) { var gen = fn.apply(self, args); function _next(value) { asyncGeneratorStep(gen, resolve, reject, _next, _throw, "next", value); } function _throw(err) { asyncGeneratorStep(gen, resolve, reject, _next, _throw, "throw", err); } _next(undefined); }); }; }
function _extends() { _extends = Object.assign ? Object.assign.bind() : function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; }; return _extends.apply(this, arguments); }
function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }
function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }
function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }
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
function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }




var DataSource = /*#__PURE__*/function (_DataSourceApi) {
  _inherits(DataSource, _DataSourceApi);
  var _super = _createSuper(DataSource);
  function DataSource(instanceSettings, templateSrv, dashboardSrv, timeSrv) {
    var _this;
    _classCallCheck(this, DataSource);
    _this = _super.call(this, instanceSettings);
    _defineProperty(_assertThisInitialized(_this), "type", void 0);
    _defineProperty(_assertThisInitialized(_this), "url", void 0);
    _defineProperty(_assertThisInitialized(_this), "name", void 0);
    _defineProperty(_assertThisInitialized(_this), "serviceUrl", void 0);
    _defineProperty(_assertThisInitialized(_this), "datasourceId", void 0);
    _defineProperty(_assertThisInitialized(_this), "datasourceType", void 0);
    _defineProperty(_assertThisInitialized(_this), "datasourceName", void 0);
    _defineProperty(_assertThisInitialized(_this), "logging", void 0);
    _defineProperty(_assertThisInitialized(_this), "tokenEnabled", void 0);
    _this.templateSrv = templateSrv;
    _this.dashboardSrv = dashboardSrv;
    _this.timeSrv = timeSrv;
    _this.type = instanceSettings.type;
    _this.url = instanceSettings.url;
    _this.name = instanceSettings.name;
    _this.serviceUrl = instanceSettings.jsonData.serviceUrl;
    _this.datasourceId = instanceSettings.id;
    _this.logging = instanceSettings.jsonData.logging;
    _this.tokenEnabled = instanceSettings.jsonData.authToken;
    return _this;
  }

  /**provides ability to test connection from connection settings page . */
  _createClass(DataSource, [{
    key: "testDatasource",
    value: function testDatasource() {
      var queryJson = {
        "statements": [{
          "statement": "match (n) return n limit 1",
          "includeStats": true,
          "resultDataContents": ["row", "graph"]
        }],
        "metadata": [{
          "testDB": true
        }]
      };
      var testQuery = JSON.stringify(queryJson);
      var routePath = this.url;
      if (this.tokenEnabled) {
        routePath = this.url + "/platformservice";
      }
      return (0,_grafana_runtime__WEBPACK_IMPORTED_MODULE_1__.getBackendSrv)().datasourceRequest({
        url: routePath,
        method: 'POST',
        data: testQuery
      }).then(function (res) {
        var data = res.data;
        if (res.status === 200) {
          if (data && data.results.length > 0) {
            return {
              status: 'success',
              message: 'Data source is working'
            };
          } else {
            return {
              status: "failure",
              message: "No data returned",
              title: "failure"
            };
          }
        } else {
          return {
            status: 'error',
            message: res.error
          };
        }
      })["catch"](function (err) {
        console.log(err);
        if (err.data && err.data.errors[0].message) {
          return {
            status: 'error',
            message: err.data.errors[0].message
          };
        } else {
          return {
            status: 'error',
            message: err.status
          };
        }
      });
    }
  }, {
    key: "query",
    value: function query(options) {
      var _this2 = this;
      var queries = [];
      var streams = [];
      var range = options.range;
      var from1 = range.from.valueOf() / 1000;
      var to = range.to.valueOf() / 1000;
      var cypherQuery = {};
      var statements = [];
      var metadata = [];
      cypherQuery['statements'] = statements;
      cypherQuery['metadata'] = metadata;
      var logInfo = {};
      logInfo['eventName'] = 'data-request';
      logInfo['panelId'] = options.panelId;
      logInfo['dashboardId'] = options.dashboardId;
      logInfo['datasourceId'] = this.datasourceId;
      logInfo['datasourceType'] = this.type;
      logInfo['datasourceName'] = this.name;
      logInfo['timestamp'] = new Date().toISOString();
      // Start streams and prepare queries
      var _iterator = _createForOfIteratorHelper(options.targets),
        _step;
      try {
        for (_iterator.s(); !(_step = _iterator.n()).done;) {
          var target = _step.value;
          if (target.hide) {
            continue;
          }
          //let query = target.queryText;
          var query = this.templateSrv.replace(target.queryText, options.scopedVars, Utils__WEBPACK_IMPORTED_MODULE_3__.applyTemplateVariables);
          query = (0,Utils__WEBPACK_IMPORTED_MODULE_3__.addTimestampToQuery)(query, options);
          var resultDataContents = [];
          var statement = {
            "statement": query,
            "resultDataContents": resultDataContents
          };
          var cacheoptions = {
            "startTime": from1,
            "endTime": to,
            "testDB": false,
            "resultCache": target.cache,
            "cachingType": target.cacheType,
            "cachingValue": target.cacheValue
          };
          statements.push(statement);
          metadata.push(cacheoptions);
          queries.push(_extends({}, target, {
            intervalMs: options.intervalMs,
            maxDataPoints: options.maxDataPoints,
            datasourceId: this.id
          }));
        }
      } catch (err) {
        _iterator.e(err);
      } finally {
        _iterator.f();
      }
      console.log(this.url);
      logInfo['query'] = cypherQuery;
      var startTime = Date.now();
      if (queries.length) {
        var routePath = this.url;
        if (this.tokenEnabled) {
          routePath = this.url + "/platformservice";
        }
        var req = (0,_grafana_runtime__WEBPACK_IMPORTED_MODULE_1__.getBackendSrv)().datasourceRequest({
          method: 'POST',
          url: routePath,
          data: cypherQuery
        }).then(function (res) {
          var endTime = Date.now();
          logInfo['time_ms'] = endTime - startTime;
          logInfo['panelQuery'] = true;
          logInfo['requestId'] = options.requestId;
          if (_this2.logging) {
            try {
              var user = _grafana_runtime__WEBPACK_IMPORTED_MODULE_1__.config.bootData.user;
              var dashboard = _this2.dashboardSrv.dashboard;
              logInfo['userId'] = user.id;
              logInfo['userName'] = user.name;
              logInfo['email'] = user.email;
              logInfo['orgId'] = user.orgId;
              logInfo['orgName'] = user.orgName;
              logInfo['uid'] = dashboard.uid;
              logInfo['dashboardName'] = dashboard.title;
              logInfo['panelCount'] = dashboard.panels.length;
              dashboard.panels.forEach(function (panel) {
                if (panel.id === options.panelId) {
                  logInfo['panelName'] = panel.title;
                  logInfo['chartType'] = panel.type;
                }
              });
              _this2.logDashboardInfo(logInfo);
            } catch (e) {
              console.log(e);
            }
          }
          if (queries[0].table) {
            return (0,Utils__WEBPACK_IMPORTED_MODULE_3__.convertResponseToDataFramesTable)(queries, res);
          } else if (queries[0].graph) {
            return (0,Utils__WEBPACK_IMPORTED_MODULE_3__.convertResponseToDataFramesGraph)(queries, res);
          } else if (queries[0].raw) {
            return (0,Utils__WEBPACK_IMPORTED_MODULE_3__.convertResponseToDataFramesRaw)(queries, res);
          } else if (queries[0].mode == 'Metrics' || queries[0].showingTable) {
            //Explore Functionality
            return (0,Utils__WEBPACK_IMPORTED_MODULE_3__.convertResponseToDataFramesTable)(queries, res);
          } else if (queries[0].mode == 'Metrics' || queries[0].showingGraph) {
            //Explore Functionality
            return (0,Utils__WEBPACK_IMPORTED_MODULE_3__.convertResponseToDataFramesGraph)(queries, res);
          } else {
            throw new Error("Select some response type");
          }
        });
        streams.push((0,rxjs__WEBPACK_IMPORTED_MODULE_2__.from)(req));
      }
      return rxjs__WEBPACK_IMPORTED_MODULE_2__.merge.apply(void 0, streams);
    }
  }, {
    key: "logDashboardInfo",
    value: function logDashboardInfo(logInfo) {
      (0,_grafana_runtime__WEBPACK_IMPORTED_MODULE_1__.getBackendSrv)().datasourceRequest({
        method: 'POST',
        url: this.url + '/neo4jLog',
        data: logInfo
      }).then(function (res) {
        console.log(res);
      });
    }
  }, {
    key: "metricFindQuery",
    value: function metricFindQuery(query, options) {
      var _this3 = this;
      var cypherQuery = {};
      var statements = [];
      cypherQuery['statements'] = statements;
      if (this.timeSrv && this.timeSrv.timeRange()) {
        var _options = {
          range: this.timeSrv.timeRange()
        };
        query = (0,Utils__WEBPACK_IMPORTED_MODULE_3__.addTimestampToQuery)(query, _options);
      }
      //query = addTimestampToQuery(query, null);
      query = this.templateSrv.replace(query, {}, Utils__WEBPACK_IMPORTED_MODULE_3__.applyTemplateVariables);
      var resultDataContents = ["row"];
      var statement = {
        "statement": query,
        "includeStats": false,
        "resultDataContents": resultDataContents
      };
      statements.push(statement);
      return new Promise(function (resolve, reject) {
        return (0,_grafana_runtime__WEBPACK_IMPORTED_MODULE_1__.getBackendSrv)().datasourceRequest({
          url: _this3.url,
          method: 'POST',
          data: cypherQuery
        }).then(function (res) {
          if (res.status === 200 && res.data.errors.length === 0) {
            var result = res.data.results[0];
            if (result) {
              var data = result.data;
              if (data) {
                var metrics = [];
                var _iterator2 = _createForOfIteratorHelper(data),
                  _step2;
                try {
                  for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
                    var row = _step2.value;
                    var record = row.row;
                    if (record && record.length > 0) {
                      metrics.push({
                        text: record[0],
                        value: record[0]
                      });
                    }
                  }
                } catch (err) {
                  _iterator2.e(err);
                } finally {
                  _iterator2.f();
                }
                return resolve(metrics);
              }
            }
          } else {
            return reject(res.data.errors[0].message);
          }
        })["catch"](function (err) {
          console.log(err);
          if (err.data && err.data.message) {
            return {
              status: 'error',
              message: err.data.message
            };
          } else {
            return {
              status: 'error',
              message: err.status
            };
          }
        });
      });
    }

    /**
     * 
     *  all Below methods need improvisation.
     */
  }, {
    key: "annotationQuery",
    value: function () {
      var _annotationQuery = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee(options) {
        var events, date, event;
        return _regeneratorRuntime().wrap(function _callee$(_context) {
          while (1) {
            switch (_context.prev = _context.next) {
              case 0:
                events = []; //const expression = options.annotation.queryText;
                date = new Date();
                event = {
                  time: date.valueOf(),
                  text: 'foo',
                  tags: ['bar', 'foo']
                };
                events.push(event);
                return _context.abrupt("return", events);
              case 5:
              case "end":
                return _context.stop();
            }
          }
        }, _callee);
      }));
      function annotationQuery(_x) {
        return _annotationQuery.apply(this, arguments);
      }
      return annotationQuery;
    }()
    /* annotationQuery(options) {
         //console.log('annotations---');
         var query = this.templateSrv.replace(options.annotation.query, {}, 'glob');
         var annotationQuery = {
           range: options.range,
           annotation: {
             name: options.annotation.name,
             datasource: options.annotation.datasource,
             enable: options.annotation.enable,
             iconColor: options.annotation.iconColor,
             query: query
           },
           rangeRaw: options.rangeRaw
         };
     
         return getBackendSrv().datasourceRequest({
           url: this.url + '/annotations',
           method: 'GET',
           data: annotationQuery
         }).then(result => {
           console.log(result);
           return result.data;
         });
    }*/
  }]);
  return DataSource;
}(_grafana_data__WEBPACK_IMPORTED_MODULE_0__.DataSourceApi);

/***/ }),

/***/ "./QueryEditor.tsx":
/*!*************************!*\
  !*** ./QueryEditor.tsx ***!
  \*************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "QueryEditor": () => (/* binding */ QueryEditor)
/* harmony export */ });
/* harmony import */ var lodash_defaults__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! lodash/defaults */ "../node_modules/lodash/defaults.js");
/* harmony import */ var lodash_defaults__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(lodash_defaults__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _types__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./types */ "./types.ts");
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @grafana/ui */ "@grafana/ui");
/* harmony import */ var _grafana_ui__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__);
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
var _div, _div2, _label, _label2, _label3, _div3;
function _extends() { _extends = Object.assign ? Object.assign.bind() : function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; }; return _extends.apply(this, arguments); }
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
function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }




var cacheTypes = [{
  value: 'Fixed Time',
  label: 'Fixed Time'
}, {
  value: 'Variance Time',
  label: 'Variance Time'
}];
var fixedTime = [{
  value: '1',
  label: '1'
}, {
  value: '2',
  label: '2'
}, {
  value: '3',
  label: '3'
}];
var varianeTime = [{
  value: '5',
  label: '5'
}, {
  value: '10',
  label: '10'
}, {
  value: '15',
  label: '15'
}];
var QueryEditor = /*#__PURE__*/function (_PureComponent) {
  _inherits(QueryEditor, _PureComponent);
  var _super = _createSuper(QueryEditor);
  function QueryEditor() {
    var _this;
    _classCallCheck(this, QueryEditor);
    for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }
    _this = _super.call.apply(_super, [this].concat(args));
    _defineProperty(_assertThisInitialized(_this), "state", {
      graph: false,
      timeseries: false,
      table: false,
      stats: false,
      cache: false,
      raw: false,
      fixTime: false,
      varTime: false,
      cacheType: '',
      cacheValue: ''
    });
    _defineProperty(_assertThisInitialized(_this), "onQueryTextChange", function (event) {
      var _this$props = _this.props,
        onChange = _this$props.onChange,
        query = _this$props.query,
        onRunQuery = _this$props.onRunQuery;
      onChange(_extends({}, query, {
        queryText: event.target.value
      }));
      onRunQuery();
    });
    _defineProperty(_assertThisInitialized(_this), "onConstantChange", function (event) {
      var _this$props2 = _this.props,
        onChange = _this$props2.onChange,
        query = _this$props2.query,
        onRunQuery = _this$props2.onRunQuery;
      onChange(_extends({}, query, {
        constant: parseFloat(event.target.value)
      }));
      onRunQuery(); // executes the query
    });
    _defineProperty(_assertThisInitialized(_this), "onCacheChanged", function (_ref) {
      var value = _ref.value;
      if (value === 'Fixed Time' && _this.props.query["cache"]) {
        _this.props.query["fixTime"] = true;
        _this.props.query["varTime"] = false;
        _this.props.query["cacheType"] = 'Fixed Time';
        _this.setState({
          fixTime: true,
          varTime: false,
          cacheType: 'Fixed Time'
        });
      } else if (value === 'Variance Time' && _this.props.query["cache"]) {
        _this.props.query["varTime"] = true;
        _this.props.query["fixTime"] = false;
        _this.props.query["cacheType"] = 'Variance Time';
        _this.setState({
          varTime: true,
          fixTime: false,
          cacheType: 'Variance Time'
        });
      }
    });
    return _this;
  }
  _createClass(QueryEditor, [{
    key: "componentDidMount",
    value: function componentDidMount() {
      this.setState(this.props.query);
    }
  }, {
    key: "onChanged",
    value: function onChanged(type, e) {
      var _this2 = this;
      //const { query } = this.props;
      //throw new Error("Method not implemented.");
      var obj = {};
      if (type === 'cache') {
        obj['cache'] = !this.props.query[type];
        this.props.query['cache'] = !this.props.query[type];
      } else {
        Object.keys(this.props.query).forEach(function (c) {
          if (c == type) {
            obj[type] = !_this2.props.query[type];
            _this2.props.query[type] = !_this2.props.query[type];
          } else if (c == 'constant' || c == 'queryText' || c == 'cypherQuery' || c == 'refId') {
            // do nothing
          } else {
            if (c !== 'cache') {
              obj[c] = false;
              _this2.props.query[c] = false;
            }
          }
        });
      }
      this.setState(obj);
    }
  }, {
    key: "onVarianceChanged",
    value: function onVarianceChanged(e) {
      this.props.query["cacheValue"] = e.value;
      this.setState({
        cacheValue: e.value
      });
    }
  }, {
    key: "onFixChanged",
    value: function onFixChanged(e) {
      // const params = this.props.query;
      this.props.query["cacheValue"] = e.value;
      this.setState({
        cacheValue: e.value
      });
    }
  }, {
    key: "render",
    value: function render() {
      var _this3 = this;
      var query = lodash_defaults__WEBPACK_IMPORTED_MODULE_0___default()(this.props.query, _types__WEBPACK_IMPORTED_MODULE_2__.defaultQuery);
      var queryText = query.queryText;
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement((react__WEBPACK_IMPORTED_MODULE_1___default().Fragment), null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form-inline"
      }, _div || (_div = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form gf-form--v-stretch"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("label", {
        className: "gf-form-label width-5"
      }, "Cypher"))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form gf-form--grow"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("textarea", {
        rows: 5,
        className: "gf-form-input width-100",
        onChange: this.onQueryTextChange,
        placeholder: "Example: match (n) return n limit 1",
        value: queryText,
        required: true
      }))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form-inline"
      }, _div2 || (_div2 = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form gf-form--v-stretch"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("label", {
        className: "gf-form-label width-5"
      }, "Type"))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__.LegacyForms.Switch, {
        label: "Graph",
        labelClass: "width-5",
        checked: this.state.graph,
        onChange: function onChange(e) {
          return _this3.onChanged('graph', e);
        }
      }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__.LegacyForms.Switch, {
        label: "Table",
        labelClass: "width-5",
        checked: this.state.table,
        onChange: function onChange(e) {
          return _this3.onChanged('table', e);
        }
      }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__.LegacyForms.Switch, {
        label: "Cache",
        labelClass: "width-5",
        checked: this.state.cache,
        onChange: function onChange(e) {
          return _this3.onChanged('cache', e);
        }
      })), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form-inline"
      }, this.props.query.cache && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form gf-form--v-stretch"
      }, _label || (_label = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("label", {
        className: "gf-form-label width-5"
      }, "Cache Type")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__.Select, {
        width: 20,
        options: cacheTypes,
        onChange: function onChange(e) {
          return _this3.onCacheChanged(e);
        },
        value: cacheTypes.find(function (item) {
          return item.value === _this3.state.cacheType;
        })
      })), this.state.fixTime && this.props.query.cache && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form gf-form--v-stretch"
      }, _label2 || (_label2 = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("label", {
        className: "gf-form-label width-10"
      }, "Duration (Hours)")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__.Select, {
        width: 10,
        options: fixedTime,
        onChange: function onChange(e) {
          return _this3.onFixChanged(e);
        },
        value: fixedTime.find(function (item) {
          return item.value === _this3.state.cacheValue;
        })
      })), this.state.varTime && this.props.query.cache && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", {
        className: "gf-form gf-form--v-stretch"
      }, _label3 || (_label3 = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("label", {
        className: "gf-form-label width-10"
      }, "Variance (%)")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement(_grafana_ui__WEBPACK_IMPORTED_MODULE_3__.Select, {
        width: 10,
        options: varianeTime,
        onChange: function onChange(e) {
          return _this3.onVarianceChanged(e);
        },
        value: varianeTime.find(function (item) {
          return item.value === _this3.state.cacheValue;
        })
      }))), this.state.fixTime && this.props.query.cache && (_div3 || (_div3 = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("div", null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_1___default().createElement("label", {
        className: "gf-form-label width-50"
      }, "Please note that when Fixed Time is selected, the time selector would be disabled for selected fixed time interval.")))));
    }
  }]);
  return QueryEditor;
}(react__WEBPACK_IMPORTED_MODULE_1__.PureComponent);

/***/ }),

/***/ "./QueryEditorHelp.tsx":
/*!*****************************!*\
  !*** ./QueryEditorHelp.tsx ***!
  \*****************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "default": () => (/* binding */ QueryEditorHelp)
/* harmony export */ });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _CheatSheet__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./CheatSheet */ "./CheatSheet.tsx");
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
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


var QueryEditorHelp = /*#__PURE__*/function (_PureComponent) {
  _inherits(QueryEditorHelp, _PureComponent);
  var _super = _createSuper(QueryEditorHelp);
  function QueryEditorHelp() {
    _classCallCheck(this, QueryEditorHelp);
    return _super.apply(this, arguments);
  }
  _createClass(QueryEditorHelp, [{
    key: "render",
    value: function render() {
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default().createElement(_CheatSheet__WEBPACK_IMPORTED_MODULE_1__["default"], {
        onClickExample: this.props.onClickExample
      });
    }
  }]);
  return QueryEditorHelp;
}(react__WEBPACK_IMPORTED_MODULE_0__.PureComponent);

;

/***/ }),

/***/ "./Utils.ts":
/*!******************!*\
  !*** ./Utils.ts ***!
  \******************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "addTimestampToQuery": () => (/* binding */ addTimestampToQuery),
/* harmony export */   "applyTemplateVariables": () => (/* binding */ applyTemplateVariables),
/* harmony export */   "checkCypherQueryModificationKeyword": () => (/* binding */ checkCypherQueryModificationKeyword),
/* harmony export */   "convertResponseToDataFramesGraph": () => (/* binding */ convertResponseToDataFramesGraph),
/* harmony export */   "convertResponseToDataFramesRaw": () => (/* binding */ convertResponseToDataFramesRaw),
/* harmony export */   "convertResponseToDataFramesTable": () => (/* binding */ convertResponseToDataFramesTable),
/* harmony export */   "convertResponseToDataFramesTimeSeries": () => (/* binding */ convertResponseToDataFramesTimeSeries),
/* harmony export */   "convertResponseToDataFramesTimeSeries1": () => (/* binding */ convertResponseToDataFramesTimeSeries1)
/* harmony export */ });
var convertResponseToDataFramesTable = function convertResponseToDataFramesTable(queries, res) {
  var data = [];
  var error = undefined;
  var dataArray1 = [];
  var columnArray1 = [];
  var rows1;
  if (res.data) {
    for (var index = 0; index < res.data.results.length; index++) {
      dataArray1 = res.data.results[index].data;
      columnArray1 = res.data.results[index].columns.map(function (x) {
        return {
          text: x
        };
      });
      rows1 = dataArray1.map(function (c) {
        return c.row;
      });
      data.push({
        columns: columnArray1,
        rows: rows1,
        refId: queries[index].refId,
        name: undefined,
        target: queries[index]
      });
    }
  }

  //console.log('datahere ----',data);

  /*let dataArray = res.data.results[0].data;
  let columnArray = res.data.results[0].columns.map(x => { return { text: x } });
  const rows = dataArray.map(c => c.row);
  data.push({ columns: columnArray, rows: rows, refId: 'A', name: undefined,target: queries });*/
  return {
    data: data,
    error: error
  };
};
var convertResponseToDataFramesGraph = function convertResponseToDataFramesGraph(queries, res) {
  var data = [];
  var error = undefined;
  var datapoints = [];
  var multiSeriesResponse = false;
  var response = [];
  var timestamp = new Date().getTime() * 1000;
  if (res.options && res.options.range && res.options.range.to) {
    timestamp = res.options.range.to.valueOf();
  }
  var targetResponse = {
    target: queries[0].refId,
    datapoints: datapoints
  };
  var targetDatapointsMap = {};
  var dataArray = res.data.results[0].data;
  var columns = res.data.results[0].columns;
  for (var r in dataArray) {
    var row = dataArray[r].row;
    if (columns.length === 1) {
      datapoints.push([row[0], timestamp]);
    } else if (columns.length === 3) {
      var targetName = row[2];
      var targetDataPoints = targetDatapointsMap[targetName];
      if (targetDataPoints === undefined) {
        targetDataPoints = [];
        targetDatapointsMap[targetName] = targetDataPoints;
        data.push({
          target: targetName,
          datapoints: targetDataPoints
        });
        multiSeriesResponse = true;
      }
      targetDataPoints.push([row[1], row[0] * 1000]);
    } else {
      //Assuming the first column will be the time and second column will be the data.
      datapoints.push([row[1], row[0] * 1000]);
    }
  }
  if (!multiSeriesResponse) {
    response.push(targetResponse);
    data.push(response[0]);
  }
  return {
    data: data,
    error: error
  };
};
var convertResponseToDataFramesRaw = function convertResponseToDataFramesRaw(queries, res) {
  /* let datapoints = {} as any;
   datapoints["datapoints"] = res 
   datapoints["type"] = "docs";*/
  var data = [];
  var error = undefined;
  res.data["targets"] = queries;
  data.push(res);
  var obj = data[0].data;
  return {
    obj: obj,
    error: error
  };
};
var convertResponseToDataFramesTimeSeries = function convertResponseToDataFramesTimeSeries(queries, res) {
  var timestamp = new Date().getTime() * 1000;
  var data = [];
  var error = undefined;
  var datapoints = [];
  var results = res.data.results;
  for (var i in results) {
    var targetDatapointsMap = {};
    var response = [];
    var result = results[i];
    var rows = result.data;
    for (var r in rows) {
      var row = rows[r].row;
      if (result.columns.length === 1) {
        datapoints.push([row[0], timestamp]);
        data.push({
          target: results[i].refId,
          datapoints: datapoints,
          refId: results[i].refId,
          tags: undefined
        });
      } else if (result.columns.length === 3) {
        var targetName = row[2];
        var targetDataPoints = targetDatapointsMap[targetName];
        if (targetDataPoints === undefined) {
          targetDataPoints = [];
          targetDatapointsMap[targetName] = targetDataPoints;
          response.push({
            target: targetName,
            datapoints: targetDataPoints
          });
        }
        targetDataPoints.push([row[1], row[0] * 1000]);
        data.push({
          target: results[i].refId,
          datapoints: targetDataPoints,
          refId: results[i].refId,
          tags: undefined
        });
      } else {
        //Assuming the first column will be the time and second column will be the data.
        datapoints.push([row[1], row[0] * 1000]);
        data.push({
          target: results[i].refId,
          datapoints: datapoints,
          refId: results[i].refId,
          tags: undefined
        });
      }
    }
  }
  return {
    data: data,
    error: error
  };
};
var convertResponseToDataFramesTimeSeries1 = function convertResponseToDataFramesTimeSeries1(queries, res) {
  var data = [];
  var error = undefined;
  var responseArray = res.data.results[0].data;
  var datapoints = responseArray.map(function (c) {
    return c.row;
  });
  var columnArray = res.data.results[0].columns;
  var targetName = columnArray.map(function (c) {
    return c;
  });
  data.push({
    target: targetName,
    datapoints: datapoints,
    refId: 'A',
    tags: undefined
  });
  return {
    data: data,
    error: error
  };
};
function checkCypherQueryModificationKeyword(cypherQuery) {
  var keywords;
  keywords = ["create", "delete", "set", "update", "merge", "detach"];
  var flag = 0;
  var queryCorrect = true;
  if (cypherQuery.statements[0].statement == undefined) {
    return queryCorrect;
  }
  var j;
  for (j in keywords) {
    var query = cypherQuery.statements[0].statement.toString().toLowerCase();
    if (query.indexOf(" " + keywords[j] + " ") >= 0) {
      console.log(keywords[j] + " is present as an individual word.");
      flag = 1;
      break;
    }
    if (query.indexOf("\n" + keywords[j]) >= 0 && query.indexOf("\n" + keywords[j] + " ") >= 0) {
      console.log(keywords[j] + " is present after new line.");
      flag = 1;
      break;
    }
    if (query.indexOf(keywords[j] + ")") >= 0 || query.indexOf(keywords[j] + "(") >= 0 || query.indexOf(")" + keywords[j]) >= 0 || query.indexOf("(" + keywords[j]) >= 0 || query.indexOf(" " + keywords[j] + "\n") >= 0 || query.indexOf("\n" + keywords[j] + "\n") >= 0) {
      console.log(keywords[j] + " is present.");
      flag = 1;
      break;
    }
  }
  if (flag == 0) {
    return queryCorrect;
  } else {
    queryCorrect = false;
    return queryCorrect;
  }
}
function addTimestampToQuery(query, options) {
  if (query && options) {
    var range = options.range;
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
function applyTemplateVariables(value, variable, formatValue) {
  if (typeof value === 'string') {
    var values = [];
    values.push(value);
    value = values;
  }
  return JSON.stringify(value);
}

/***/ }),

/***/ "./types.ts":
/*!******************!*\
  !*** ./types.ts ***!
  \******************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "defaultQuery": () => (/* binding */ defaultQuery),
/* harmony export */   "defaults": () => (/* binding */ defaults)
/* harmony export */ });
var defaultQuery = {
  queryText: undefined,
  constant: 6.5,
  cypherQuery: undefined,
  graph: false,
  timeseries: false,
  table: false,
  stats: false,
  cache: false,
  raw: false,
  varTime: false,
  fixTime: false,
  cacheType: undefined,
  cacheValue: undefined
};

/**
 * These are options configured for each DataSource instance
 */

/** changes*/
var defaults = {
  logging: true,
  path: '',
  serviceUrl: '',
  authToken: true,
  basicPassword: ''
};

/***/ }),

/***/ "../node_modules/lodash/_Symbol.js":
/*!*****************************************!*\
  !*** ../node_modules/lodash/_Symbol.js ***!
  \*****************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var root = __webpack_require__(/*! ./_root */ "../node_modules/lodash/_root.js");

/** Built-in value references. */
var Symbol = root.Symbol;

module.exports = Symbol;


/***/ }),

/***/ "../node_modules/lodash/_apply.js":
/*!****************************************!*\
  !*** ../node_modules/lodash/_apply.js ***!
  \****************************************/
/***/ ((module) => {

/**
 * A faster alternative to `Function#apply`, this function invokes `func`
 * with the `this` binding of `thisArg` and the arguments of `args`.
 *
 * @private
 * @param {Function} func The function to invoke.
 * @param {*} thisArg The `this` binding of `func`.
 * @param {Array} args The arguments to invoke `func` with.
 * @returns {*} Returns the result of `func`.
 */
function apply(func, thisArg, args) {
  switch (args.length) {
    case 0: return func.call(thisArg);
    case 1: return func.call(thisArg, args[0]);
    case 2: return func.call(thisArg, args[0], args[1]);
    case 3: return func.call(thisArg, args[0], args[1], args[2]);
  }
  return func.apply(thisArg, args);
}

module.exports = apply;


/***/ }),

/***/ "../node_modules/lodash/_arrayLikeKeys.js":
/*!************************************************!*\
  !*** ../node_modules/lodash/_arrayLikeKeys.js ***!
  \************************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseTimes = __webpack_require__(/*! ./_baseTimes */ "../node_modules/lodash/_baseTimes.js"),
    isArguments = __webpack_require__(/*! ./isArguments */ "../node_modules/lodash/isArguments.js"),
    isArray = __webpack_require__(/*! ./isArray */ "../node_modules/lodash/isArray.js"),
    isBuffer = __webpack_require__(/*! ./isBuffer */ "../node_modules/lodash/isBuffer.js"),
    isIndex = __webpack_require__(/*! ./_isIndex */ "../node_modules/lodash/_isIndex.js"),
    isTypedArray = __webpack_require__(/*! ./isTypedArray */ "../node_modules/lodash/isTypedArray.js");

/** Used for built-in method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Creates an array of the enumerable property names of the array-like `value`.
 *
 * @private
 * @param {*} value The value to query.
 * @param {boolean} inherited Specify returning inherited property names.
 * @returns {Array} Returns the array of property names.
 */
function arrayLikeKeys(value, inherited) {
  var isArr = isArray(value),
      isArg = !isArr && isArguments(value),
      isBuff = !isArr && !isArg && isBuffer(value),
      isType = !isArr && !isArg && !isBuff && isTypedArray(value),
      skipIndexes = isArr || isArg || isBuff || isType,
      result = skipIndexes ? baseTimes(value.length, String) : [],
      length = result.length;

  for (var key in value) {
    if ((inherited || hasOwnProperty.call(value, key)) &&
        !(skipIndexes && (
           // Safari 9 has enumerable `arguments.length` in strict mode.
           key == 'length' ||
           // Node.js 0.10 has enumerable non-index properties on buffers.
           (isBuff && (key == 'offset' || key == 'parent')) ||
           // PhantomJS 2 has enumerable non-index properties on typed arrays.
           (isType && (key == 'buffer' || key == 'byteLength' || key == 'byteOffset')) ||
           // Skip index properties.
           isIndex(key, length)
        ))) {
      result.push(key);
    }
  }
  return result;
}

module.exports = arrayLikeKeys;


/***/ }),

/***/ "../node_modules/lodash/_baseGetTag.js":
/*!*********************************************!*\
  !*** ../node_modules/lodash/_baseGetTag.js ***!
  \*********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var Symbol = __webpack_require__(/*! ./_Symbol */ "../node_modules/lodash/_Symbol.js"),
    getRawTag = __webpack_require__(/*! ./_getRawTag */ "../node_modules/lodash/_getRawTag.js"),
    objectToString = __webpack_require__(/*! ./_objectToString */ "../node_modules/lodash/_objectToString.js");

/** `Object#toString` result references. */
var nullTag = '[object Null]',
    undefinedTag = '[object Undefined]';

/** Built-in value references. */
var symToStringTag = Symbol ? Symbol.toStringTag : undefined;

/**
 * The base implementation of `getTag` without fallbacks for buggy environments.
 *
 * @private
 * @param {*} value The value to query.
 * @returns {string} Returns the `toStringTag`.
 */
function baseGetTag(value) {
  if (value == null) {
    return value === undefined ? undefinedTag : nullTag;
  }
  return (symToStringTag && symToStringTag in Object(value))
    ? getRawTag(value)
    : objectToString(value);
}

module.exports = baseGetTag;


/***/ }),

/***/ "../node_modules/lodash/_baseIsArguments.js":
/*!**************************************************!*\
  !*** ../node_modules/lodash/_baseIsArguments.js ***!
  \**************************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseGetTag = __webpack_require__(/*! ./_baseGetTag */ "../node_modules/lodash/_baseGetTag.js"),
    isObjectLike = __webpack_require__(/*! ./isObjectLike */ "../node_modules/lodash/isObjectLike.js");

/** `Object#toString` result references. */
var argsTag = '[object Arguments]';

/**
 * The base implementation of `_.isArguments`.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is an `arguments` object,
 */
function baseIsArguments(value) {
  return isObjectLike(value) && baseGetTag(value) == argsTag;
}

module.exports = baseIsArguments;


/***/ }),

/***/ "../node_modules/lodash/_baseIsNative.js":
/*!***********************************************!*\
  !*** ../node_modules/lodash/_baseIsNative.js ***!
  \***********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var isFunction = __webpack_require__(/*! ./isFunction */ "../node_modules/lodash/isFunction.js"),
    isMasked = __webpack_require__(/*! ./_isMasked */ "../node_modules/lodash/_isMasked.js"),
    isObject = __webpack_require__(/*! ./isObject */ "../node_modules/lodash/isObject.js"),
    toSource = __webpack_require__(/*! ./_toSource */ "../node_modules/lodash/_toSource.js");

/**
 * Used to match `RegExp`
 * [syntax characters](http://ecma-international.org/ecma-262/7.0/#sec-patterns).
 */
var reRegExpChar = /[\\^$.*+?()[\]{}|]/g;

/** Used to detect host constructors (Safari). */
var reIsHostCtor = /^\[object .+?Constructor\]$/;

/** Used for built-in method references. */
var funcProto = Function.prototype,
    objectProto = Object.prototype;

/** Used to resolve the decompiled source of functions. */
var funcToString = funcProto.toString;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/** Used to detect if a method is native. */
var reIsNative = RegExp('^' +
  funcToString.call(hasOwnProperty).replace(reRegExpChar, '\\$&')
  .replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, '$1.*?') + '$'
);

/**
 * The base implementation of `_.isNative` without bad shim checks.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a native function,
 *  else `false`.
 */
function baseIsNative(value) {
  if (!isObject(value) || isMasked(value)) {
    return false;
  }
  var pattern = isFunction(value) ? reIsNative : reIsHostCtor;
  return pattern.test(toSource(value));
}

module.exports = baseIsNative;


/***/ }),

/***/ "../node_modules/lodash/_baseIsTypedArray.js":
/*!***************************************************!*\
  !*** ../node_modules/lodash/_baseIsTypedArray.js ***!
  \***************************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseGetTag = __webpack_require__(/*! ./_baseGetTag */ "../node_modules/lodash/_baseGetTag.js"),
    isLength = __webpack_require__(/*! ./isLength */ "../node_modules/lodash/isLength.js"),
    isObjectLike = __webpack_require__(/*! ./isObjectLike */ "../node_modules/lodash/isObjectLike.js");

/** `Object#toString` result references. */
var argsTag = '[object Arguments]',
    arrayTag = '[object Array]',
    boolTag = '[object Boolean]',
    dateTag = '[object Date]',
    errorTag = '[object Error]',
    funcTag = '[object Function]',
    mapTag = '[object Map]',
    numberTag = '[object Number]',
    objectTag = '[object Object]',
    regexpTag = '[object RegExp]',
    setTag = '[object Set]',
    stringTag = '[object String]',
    weakMapTag = '[object WeakMap]';

var arrayBufferTag = '[object ArrayBuffer]',
    dataViewTag = '[object DataView]',
    float32Tag = '[object Float32Array]',
    float64Tag = '[object Float64Array]',
    int8Tag = '[object Int8Array]',
    int16Tag = '[object Int16Array]',
    int32Tag = '[object Int32Array]',
    uint8Tag = '[object Uint8Array]',
    uint8ClampedTag = '[object Uint8ClampedArray]',
    uint16Tag = '[object Uint16Array]',
    uint32Tag = '[object Uint32Array]';

/** Used to identify `toStringTag` values of typed arrays. */
var typedArrayTags = {};
typedArrayTags[float32Tag] = typedArrayTags[float64Tag] =
typedArrayTags[int8Tag] = typedArrayTags[int16Tag] =
typedArrayTags[int32Tag] = typedArrayTags[uint8Tag] =
typedArrayTags[uint8ClampedTag] = typedArrayTags[uint16Tag] =
typedArrayTags[uint32Tag] = true;
typedArrayTags[argsTag] = typedArrayTags[arrayTag] =
typedArrayTags[arrayBufferTag] = typedArrayTags[boolTag] =
typedArrayTags[dataViewTag] = typedArrayTags[dateTag] =
typedArrayTags[errorTag] = typedArrayTags[funcTag] =
typedArrayTags[mapTag] = typedArrayTags[numberTag] =
typedArrayTags[objectTag] = typedArrayTags[regexpTag] =
typedArrayTags[setTag] = typedArrayTags[stringTag] =
typedArrayTags[weakMapTag] = false;

/**
 * The base implementation of `_.isTypedArray` without Node.js optimizations.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a typed array, else `false`.
 */
function baseIsTypedArray(value) {
  return isObjectLike(value) &&
    isLength(value.length) && !!typedArrayTags[baseGetTag(value)];
}

module.exports = baseIsTypedArray;


/***/ }),

/***/ "../node_modules/lodash/_baseKeysIn.js":
/*!*********************************************!*\
  !*** ../node_modules/lodash/_baseKeysIn.js ***!
  \*********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var isObject = __webpack_require__(/*! ./isObject */ "../node_modules/lodash/isObject.js"),
    isPrototype = __webpack_require__(/*! ./_isPrototype */ "../node_modules/lodash/_isPrototype.js"),
    nativeKeysIn = __webpack_require__(/*! ./_nativeKeysIn */ "../node_modules/lodash/_nativeKeysIn.js");

/** Used for built-in method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * The base implementation of `_.keysIn` which doesn't treat sparse arrays as dense.
 *
 * @private
 * @param {Object} object The object to query.
 * @returns {Array} Returns the array of property names.
 */
function baseKeysIn(object) {
  if (!isObject(object)) {
    return nativeKeysIn(object);
  }
  var isProto = isPrototype(object),
      result = [];

  for (var key in object) {
    if (!(key == 'constructor' && (isProto || !hasOwnProperty.call(object, key)))) {
      result.push(key);
    }
  }
  return result;
}

module.exports = baseKeysIn;


/***/ }),

/***/ "../node_modules/lodash/_baseRest.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/_baseRest.js ***!
  \*******************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var identity = __webpack_require__(/*! ./identity */ "../node_modules/lodash/identity.js"),
    overRest = __webpack_require__(/*! ./_overRest */ "../node_modules/lodash/_overRest.js"),
    setToString = __webpack_require__(/*! ./_setToString */ "../node_modules/lodash/_setToString.js");

/**
 * The base implementation of `_.rest` which doesn't validate or coerce arguments.
 *
 * @private
 * @param {Function} func The function to apply a rest parameter to.
 * @param {number} [start=func.length-1] The start position of the rest parameter.
 * @returns {Function} Returns the new function.
 */
function baseRest(func, start) {
  return setToString(overRest(func, start, identity), func + '');
}

module.exports = baseRest;


/***/ }),

/***/ "../node_modules/lodash/_baseSetToString.js":
/*!**************************************************!*\
  !*** ../node_modules/lodash/_baseSetToString.js ***!
  \**************************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var constant = __webpack_require__(/*! ./constant */ "../node_modules/lodash/constant.js"),
    defineProperty = __webpack_require__(/*! ./_defineProperty */ "../node_modules/lodash/_defineProperty.js"),
    identity = __webpack_require__(/*! ./identity */ "../node_modules/lodash/identity.js");

/**
 * The base implementation of `setToString` without support for hot loop shorting.
 *
 * @private
 * @param {Function} func The function to modify.
 * @param {Function} string The `toString` result.
 * @returns {Function} Returns `func`.
 */
var baseSetToString = !defineProperty ? identity : function(func, string) {
  return defineProperty(func, 'toString', {
    'configurable': true,
    'enumerable': false,
    'value': constant(string),
    'writable': true
  });
};

module.exports = baseSetToString;


/***/ }),

/***/ "../node_modules/lodash/_baseTimes.js":
/*!********************************************!*\
  !*** ../node_modules/lodash/_baseTimes.js ***!
  \********************************************/
/***/ ((module) => {

/**
 * The base implementation of `_.times` without support for iteratee shorthands
 * or max array length checks.
 *
 * @private
 * @param {number} n The number of times to invoke `iteratee`.
 * @param {Function} iteratee The function invoked per iteration.
 * @returns {Array} Returns the array of results.
 */
function baseTimes(n, iteratee) {
  var index = -1,
      result = Array(n);

  while (++index < n) {
    result[index] = iteratee(index);
  }
  return result;
}

module.exports = baseTimes;


/***/ }),

/***/ "../node_modules/lodash/_baseUnary.js":
/*!********************************************!*\
  !*** ../node_modules/lodash/_baseUnary.js ***!
  \********************************************/
/***/ ((module) => {

/**
 * The base implementation of `_.unary` without support for storing metadata.
 *
 * @private
 * @param {Function} func The function to cap arguments for.
 * @returns {Function} Returns the new capped function.
 */
function baseUnary(func) {
  return function(value) {
    return func(value);
  };
}

module.exports = baseUnary;


/***/ }),

/***/ "../node_modules/lodash/_coreJsData.js":
/*!*********************************************!*\
  !*** ../node_modules/lodash/_coreJsData.js ***!
  \*********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var root = __webpack_require__(/*! ./_root */ "../node_modules/lodash/_root.js");

/** Used to detect overreaching core-js shims. */
var coreJsData = root['__core-js_shared__'];

module.exports = coreJsData;


/***/ }),

/***/ "../node_modules/lodash/_defineProperty.js":
/*!*************************************************!*\
  !*** ../node_modules/lodash/_defineProperty.js ***!
  \*************************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var getNative = __webpack_require__(/*! ./_getNative */ "../node_modules/lodash/_getNative.js");

var defineProperty = (function() {
  try {
    var func = getNative(Object, 'defineProperty');
    func({}, '', {});
    return func;
  } catch (e) {}
}());

module.exports = defineProperty;


/***/ }),

/***/ "../node_modules/lodash/_freeGlobal.js":
/*!*********************************************!*\
  !*** ../node_modules/lodash/_freeGlobal.js ***!
  \*********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

/** Detect free variable `global` from Node.js. */
var freeGlobal = typeof __webpack_require__.g == 'object' && __webpack_require__.g && __webpack_require__.g.Object === Object && __webpack_require__.g;

module.exports = freeGlobal;


/***/ }),

/***/ "../node_modules/lodash/_getNative.js":
/*!********************************************!*\
  !*** ../node_modules/lodash/_getNative.js ***!
  \********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseIsNative = __webpack_require__(/*! ./_baseIsNative */ "../node_modules/lodash/_baseIsNative.js"),
    getValue = __webpack_require__(/*! ./_getValue */ "../node_modules/lodash/_getValue.js");

/**
 * Gets the native function at `key` of `object`.
 *
 * @private
 * @param {Object} object The object to query.
 * @param {string} key The key of the method to get.
 * @returns {*} Returns the function if it's native, else `undefined`.
 */
function getNative(object, key) {
  var value = getValue(object, key);
  return baseIsNative(value) ? value : undefined;
}

module.exports = getNative;


/***/ }),

/***/ "../node_modules/lodash/_getRawTag.js":
/*!********************************************!*\
  !*** ../node_modules/lodash/_getRawTag.js ***!
  \********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var Symbol = __webpack_require__(/*! ./_Symbol */ "../node_modules/lodash/_Symbol.js");

/** Used for built-in method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Used to resolve the
 * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
 * of values.
 */
var nativeObjectToString = objectProto.toString;

/** Built-in value references. */
var symToStringTag = Symbol ? Symbol.toStringTag : undefined;

/**
 * A specialized version of `baseGetTag` which ignores `Symbol.toStringTag` values.
 *
 * @private
 * @param {*} value The value to query.
 * @returns {string} Returns the raw `toStringTag`.
 */
function getRawTag(value) {
  var isOwn = hasOwnProperty.call(value, symToStringTag),
      tag = value[symToStringTag];

  try {
    value[symToStringTag] = undefined;
    var unmasked = true;
  } catch (e) {}

  var result = nativeObjectToString.call(value);
  if (unmasked) {
    if (isOwn) {
      value[symToStringTag] = tag;
    } else {
      delete value[symToStringTag];
    }
  }
  return result;
}

module.exports = getRawTag;


/***/ }),

/***/ "../node_modules/lodash/_getValue.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/_getValue.js ***!
  \*******************************************/
/***/ ((module) => {

/**
 * Gets the value at `key` of `object`.
 *
 * @private
 * @param {Object} [object] The object to query.
 * @param {string} key The key of the property to get.
 * @returns {*} Returns the property value.
 */
function getValue(object, key) {
  return object == null ? undefined : object[key];
}

module.exports = getValue;


/***/ }),

/***/ "../node_modules/lodash/_isIndex.js":
/*!******************************************!*\
  !*** ../node_modules/lodash/_isIndex.js ***!
  \******************************************/
/***/ ((module) => {

/** Used as references for various `Number` constants. */
var MAX_SAFE_INTEGER = 9007199254740991;

/** Used to detect unsigned integer values. */
var reIsUint = /^(?:0|[1-9]\d*)$/;

/**
 * Checks if `value` is a valid array-like index.
 *
 * @private
 * @param {*} value The value to check.
 * @param {number} [length=MAX_SAFE_INTEGER] The upper bounds of a valid index.
 * @returns {boolean} Returns `true` if `value` is a valid index, else `false`.
 */
function isIndex(value, length) {
  var type = typeof value;
  length = length == null ? MAX_SAFE_INTEGER : length;

  return !!length &&
    (type == 'number' ||
      (type != 'symbol' && reIsUint.test(value))) &&
        (value > -1 && value % 1 == 0 && value < length);
}

module.exports = isIndex;


/***/ }),

/***/ "../node_modules/lodash/_isIterateeCall.js":
/*!*************************************************!*\
  !*** ../node_modules/lodash/_isIterateeCall.js ***!
  \*************************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var eq = __webpack_require__(/*! ./eq */ "../node_modules/lodash/eq.js"),
    isArrayLike = __webpack_require__(/*! ./isArrayLike */ "../node_modules/lodash/isArrayLike.js"),
    isIndex = __webpack_require__(/*! ./_isIndex */ "../node_modules/lodash/_isIndex.js"),
    isObject = __webpack_require__(/*! ./isObject */ "../node_modules/lodash/isObject.js");

/**
 * Checks if the given arguments are from an iteratee call.
 *
 * @private
 * @param {*} value The potential iteratee value argument.
 * @param {*} index The potential iteratee index or key argument.
 * @param {*} object The potential iteratee object argument.
 * @returns {boolean} Returns `true` if the arguments are from an iteratee call,
 *  else `false`.
 */
function isIterateeCall(value, index, object) {
  if (!isObject(object)) {
    return false;
  }
  var type = typeof index;
  if (type == 'number'
        ? (isArrayLike(object) && isIndex(index, object.length))
        : (type == 'string' && index in object)
      ) {
    return eq(object[index], value);
  }
  return false;
}

module.exports = isIterateeCall;


/***/ }),

/***/ "../node_modules/lodash/_isMasked.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/_isMasked.js ***!
  \*******************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var coreJsData = __webpack_require__(/*! ./_coreJsData */ "../node_modules/lodash/_coreJsData.js");

/** Used to detect methods masquerading as native. */
var maskSrcKey = (function() {
  var uid = /[^.]+$/.exec(coreJsData && coreJsData.keys && coreJsData.keys.IE_PROTO || '');
  return uid ? ('Symbol(src)_1.' + uid) : '';
}());

/**
 * Checks if `func` has its source masked.
 *
 * @private
 * @param {Function} func The function to check.
 * @returns {boolean} Returns `true` if `func` is masked, else `false`.
 */
function isMasked(func) {
  return !!maskSrcKey && (maskSrcKey in func);
}

module.exports = isMasked;


/***/ }),

/***/ "../node_modules/lodash/_isPrototype.js":
/*!**********************************************!*\
  !*** ../node_modules/lodash/_isPrototype.js ***!
  \**********************************************/
/***/ ((module) => {

/** Used for built-in method references. */
var objectProto = Object.prototype;

/**
 * Checks if `value` is likely a prototype object.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a prototype, else `false`.
 */
function isPrototype(value) {
  var Ctor = value && value.constructor,
      proto = (typeof Ctor == 'function' && Ctor.prototype) || objectProto;

  return value === proto;
}

module.exports = isPrototype;


/***/ }),

/***/ "../node_modules/lodash/_nativeKeysIn.js":
/*!***********************************************!*\
  !*** ../node_modules/lodash/_nativeKeysIn.js ***!
  \***********************************************/
/***/ ((module) => {

/**
 * This function is like
 * [`Object.keys`](http://ecma-international.org/ecma-262/7.0/#sec-object.keys)
 * except that it includes inherited enumerable properties.
 *
 * @private
 * @param {Object} object The object to query.
 * @returns {Array} Returns the array of property names.
 */
function nativeKeysIn(object) {
  var result = [];
  if (object != null) {
    for (var key in Object(object)) {
      result.push(key);
    }
  }
  return result;
}

module.exports = nativeKeysIn;


/***/ }),

/***/ "../node_modules/lodash/_nodeUtil.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/_nodeUtil.js ***!
  \*******************************************/
/***/ ((module, exports, __webpack_require__) => {

/* module decorator */ module = __webpack_require__.nmd(module);
var freeGlobal = __webpack_require__(/*! ./_freeGlobal */ "../node_modules/lodash/_freeGlobal.js");

/** Detect free variable `exports`. */
var freeExports =  true && exports && !exports.nodeType && exports;

/** Detect free variable `module`. */
var freeModule = freeExports && "object" == 'object' && module && !module.nodeType && module;

/** Detect the popular CommonJS extension `module.exports`. */
var moduleExports = freeModule && freeModule.exports === freeExports;

/** Detect free variable `process` from Node.js. */
var freeProcess = moduleExports && freeGlobal.process;

/** Used to access faster Node.js helpers. */
var nodeUtil = (function() {
  try {
    // Use `util.types` for Node.js 10+.
    var types = freeModule && freeModule.require && freeModule.require('util').types;

    if (types) {
      return types;
    }

    // Legacy `process.binding('util')` for Node.js < 10.
    return freeProcess && freeProcess.binding && freeProcess.binding('util');
  } catch (e) {}
}());

module.exports = nodeUtil;


/***/ }),

/***/ "../node_modules/lodash/_objectToString.js":
/*!*************************************************!*\
  !*** ../node_modules/lodash/_objectToString.js ***!
  \*************************************************/
/***/ ((module) => {

/** Used for built-in method references. */
var objectProto = Object.prototype;

/**
 * Used to resolve the
 * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
 * of values.
 */
var nativeObjectToString = objectProto.toString;

/**
 * Converts `value` to a string using `Object.prototype.toString`.
 *
 * @private
 * @param {*} value The value to convert.
 * @returns {string} Returns the converted string.
 */
function objectToString(value) {
  return nativeObjectToString.call(value);
}

module.exports = objectToString;


/***/ }),

/***/ "../node_modules/lodash/_overRest.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/_overRest.js ***!
  \*******************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var apply = __webpack_require__(/*! ./_apply */ "../node_modules/lodash/_apply.js");

/* Built-in method references for those with the same name as other `lodash` methods. */
var nativeMax = Math.max;

/**
 * A specialized version of `baseRest` which transforms the rest array.
 *
 * @private
 * @param {Function} func The function to apply a rest parameter to.
 * @param {number} [start=func.length-1] The start position of the rest parameter.
 * @param {Function} transform The rest array transform.
 * @returns {Function} Returns the new function.
 */
function overRest(func, start, transform) {
  start = nativeMax(start === undefined ? (func.length - 1) : start, 0);
  return function() {
    var args = arguments,
        index = -1,
        length = nativeMax(args.length - start, 0),
        array = Array(length);

    while (++index < length) {
      array[index] = args[start + index];
    }
    index = -1;
    var otherArgs = Array(start + 1);
    while (++index < start) {
      otherArgs[index] = args[index];
    }
    otherArgs[start] = transform(array);
    return apply(func, this, otherArgs);
  };
}

module.exports = overRest;


/***/ }),

/***/ "../node_modules/lodash/_root.js":
/*!***************************************!*\
  !*** ../node_modules/lodash/_root.js ***!
  \***************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var freeGlobal = __webpack_require__(/*! ./_freeGlobal */ "../node_modules/lodash/_freeGlobal.js");

/** Detect free variable `self`. */
var freeSelf = typeof self == 'object' && self && self.Object === Object && self;

/** Used as a reference to the global object. */
var root = freeGlobal || freeSelf || Function('return this')();

module.exports = root;


/***/ }),

/***/ "../node_modules/lodash/_setToString.js":
/*!**********************************************!*\
  !*** ../node_modules/lodash/_setToString.js ***!
  \**********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseSetToString = __webpack_require__(/*! ./_baseSetToString */ "../node_modules/lodash/_baseSetToString.js"),
    shortOut = __webpack_require__(/*! ./_shortOut */ "../node_modules/lodash/_shortOut.js");

/**
 * Sets the `toString` method of `func` to return `string`.
 *
 * @private
 * @param {Function} func The function to modify.
 * @param {Function} string The `toString` result.
 * @returns {Function} Returns `func`.
 */
var setToString = shortOut(baseSetToString);

module.exports = setToString;


/***/ }),

/***/ "../node_modules/lodash/_shortOut.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/_shortOut.js ***!
  \*******************************************/
/***/ ((module) => {

/** Used to detect hot functions by number of calls within a span of milliseconds. */
var HOT_COUNT = 800,
    HOT_SPAN = 16;

/* Built-in method references for those with the same name as other `lodash` methods. */
var nativeNow = Date.now;

/**
 * Creates a function that'll short out and invoke `identity` instead
 * of `func` when it's called `HOT_COUNT` or more times in `HOT_SPAN`
 * milliseconds.
 *
 * @private
 * @param {Function} func The function to restrict.
 * @returns {Function} Returns the new shortable function.
 */
function shortOut(func) {
  var count = 0,
      lastCalled = 0;

  return function() {
    var stamp = nativeNow(),
        remaining = HOT_SPAN - (stamp - lastCalled);

    lastCalled = stamp;
    if (remaining > 0) {
      if (++count >= HOT_COUNT) {
        return arguments[0];
      }
    } else {
      count = 0;
    }
    return func.apply(undefined, arguments);
  };
}

module.exports = shortOut;


/***/ }),

/***/ "../node_modules/lodash/_toSource.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/_toSource.js ***!
  \*******************************************/
/***/ ((module) => {

/** Used for built-in method references. */
var funcProto = Function.prototype;

/** Used to resolve the decompiled source of functions. */
var funcToString = funcProto.toString;

/**
 * Converts `func` to its source code.
 *
 * @private
 * @param {Function} func The function to convert.
 * @returns {string} Returns the source code.
 */
function toSource(func) {
  if (func != null) {
    try {
      return funcToString.call(func);
    } catch (e) {}
    try {
      return (func + '');
    } catch (e) {}
  }
  return '';
}

module.exports = toSource;


/***/ }),

/***/ "../node_modules/lodash/constant.js":
/*!******************************************!*\
  !*** ../node_modules/lodash/constant.js ***!
  \******************************************/
/***/ ((module) => {

/**
 * Creates a function that returns `value`.
 *
 * @static
 * @memberOf _
 * @since 2.4.0
 * @category Util
 * @param {*} value The value to return from the new function.
 * @returns {Function} Returns the new constant function.
 * @example
 *
 * var objects = _.times(2, _.constant({ 'a': 1 }));
 *
 * console.log(objects);
 * // => [{ 'a': 1 }, { 'a': 1 }]
 *
 * console.log(objects[0] === objects[1]);
 * // => true
 */
function constant(value) {
  return function() {
    return value;
  };
}

module.exports = constant;


/***/ }),

/***/ "../node_modules/lodash/defaults.js":
/*!******************************************!*\
  !*** ../node_modules/lodash/defaults.js ***!
  \******************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseRest = __webpack_require__(/*! ./_baseRest */ "../node_modules/lodash/_baseRest.js"),
    eq = __webpack_require__(/*! ./eq */ "../node_modules/lodash/eq.js"),
    isIterateeCall = __webpack_require__(/*! ./_isIterateeCall */ "../node_modules/lodash/_isIterateeCall.js"),
    keysIn = __webpack_require__(/*! ./keysIn */ "../node_modules/lodash/keysIn.js");

/** Used for built-in method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Assigns own and inherited enumerable string keyed properties of source
 * objects to the destination object for all destination properties that
 * resolve to `undefined`. Source objects are applied from left to right.
 * Once a property is set, additional values of the same property are ignored.
 *
 * **Note:** This method mutates `object`.
 *
 * @static
 * @since 0.1.0
 * @memberOf _
 * @category Object
 * @param {Object} object The destination object.
 * @param {...Object} [sources] The source objects.
 * @returns {Object} Returns `object`.
 * @see _.defaultsDeep
 * @example
 *
 * _.defaults({ 'a': 1 }, { 'b': 2 }, { 'a': 3 });
 * // => { 'a': 1, 'b': 2 }
 */
var defaults = baseRest(function(object, sources) {
  object = Object(object);

  var index = -1;
  var length = sources.length;
  var guard = length > 2 ? sources[2] : undefined;

  if (guard && isIterateeCall(sources[0], sources[1], guard)) {
    length = 1;
  }

  while (++index < length) {
    var source = sources[index];
    var props = keysIn(source);
    var propsIndex = -1;
    var propsLength = props.length;

    while (++propsIndex < propsLength) {
      var key = props[propsIndex];
      var value = object[key];

      if (value === undefined ||
          (eq(value, objectProto[key]) && !hasOwnProperty.call(object, key))) {
        object[key] = source[key];
      }
    }
  }

  return object;
});

module.exports = defaults;


/***/ }),

/***/ "../node_modules/lodash/eq.js":
/*!************************************!*\
  !*** ../node_modules/lodash/eq.js ***!
  \************************************/
/***/ ((module) => {

/**
 * Performs a
 * [`SameValueZero`](http://ecma-international.org/ecma-262/7.0/#sec-samevaluezero)
 * comparison between two values to determine if they are equivalent.
 *
 * @static
 * @memberOf _
 * @since 4.0.0
 * @category Lang
 * @param {*} value The value to compare.
 * @param {*} other The other value to compare.
 * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
 * @example
 *
 * var object = { 'a': 1 };
 * var other = { 'a': 1 };
 *
 * _.eq(object, object);
 * // => true
 *
 * _.eq(object, other);
 * // => false
 *
 * _.eq('a', 'a');
 * // => true
 *
 * _.eq('a', Object('a'));
 * // => false
 *
 * _.eq(NaN, NaN);
 * // => true
 */
function eq(value, other) {
  return value === other || (value !== value && other !== other);
}

module.exports = eq;


/***/ }),

/***/ "../node_modules/lodash/identity.js":
/*!******************************************!*\
  !*** ../node_modules/lodash/identity.js ***!
  \******************************************/
/***/ ((module) => {

/**
 * This method returns the first argument it receives.
 *
 * @static
 * @since 0.1.0
 * @memberOf _
 * @category Util
 * @param {*} value Any value.
 * @returns {*} Returns `value`.
 * @example
 *
 * var object = { 'a': 1 };
 *
 * console.log(_.identity(object) === object);
 * // => true
 */
function identity(value) {
  return value;
}

module.exports = identity;


/***/ }),

/***/ "../node_modules/lodash/isArguments.js":
/*!*********************************************!*\
  !*** ../node_modules/lodash/isArguments.js ***!
  \*********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseIsArguments = __webpack_require__(/*! ./_baseIsArguments */ "../node_modules/lodash/_baseIsArguments.js"),
    isObjectLike = __webpack_require__(/*! ./isObjectLike */ "../node_modules/lodash/isObjectLike.js");

/** Used for built-in method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/** Built-in value references. */
var propertyIsEnumerable = objectProto.propertyIsEnumerable;

/**
 * Checks if `value` is likely an `arguments` object.
 *
 * @static
 * @memberOf _
 * @since 0.1.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is an `arguments` object,
 *  else `false`.
 * @example
 *
 * _.isArguments(function() { return arguments; }());
 * // => true
 *
 * _.isArguments([1, 2, 3]);
 * // => false
 */
var isArguments = baseIsArguments(function() { return arguments; }()) ? baseIsArguments : function(value) {
  return isObjectLike(value) && hasOwnProperty.call(value, 'callee') &&
    !propertyIsEnumerable.call(value, 'callee');
};

module.exports = isArguments;


/***/ }),

/***/ "../node_modules/lodash/isArray.js":
/*!*****************************************!*\
  !*** ../node_modules/lodash/isArray.js ***!
  \*****************************************/
/***/ ((module) => {

/**
 * Checks if `value` is classified as an `Array` object.
 *
 * @static
 * @memberOf _
 * @since 0.1.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is an array, else `false`.
 * @example
 *
 * _.isArray([1, 2, 3]);
 * // => true
 *
 * _.isArray(document.body.children);
 * // => false
 *
 * _.isArray('abc');
 * // => false
 *
 * _.isArray(_.noop);
 * // => false
 */
var isArray = Array.isArray;

module.exports = isArray;


/***/ }),

/***/ "../node_modules/lodash/isArrayLike.js":
/*!*********************************************!*\
  !*** ../node_modules/lodash/isArrayLike.js ***!
  \*********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var isFunction = __webpack_require__(/*! ./isFunction */ "../node_modules/lodash/isFunction.js"),
    isLength = __webpack_require__(/*! ./isLength */ "../node_modules/lodash/isLength.js");

/**
 * Checks if `value` is array-like. A value is considered array-like if it's
 * not a function and has a `value.length` that's an integer greater than or
 * equal to `0` and less than or equal to `Number.MAX_SAFE_INTEGER`.
 *
 * @static
 * @memberOf _
 * @since 4.0.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
 * @example
 *
 * _.isArrayLike([1, 2, 3]);
 * // => true
 *
 * _.isArrayLike(document.body.children);
 * // => true
 *
 * _.isArrayLike('abc');
 * // => true
 *
 * _.isArrayLike(_.noop);
 * // => false
 */
function isArrayLike(value) {
  return value != null && isLength(value.length) && !isFunction(value);
}

module.exports = isArrayLike;


/***/ }),

/***/ "../node_modules/lodash/isBuffer.js":
/*!******************************************!*\
  !*** ../node_modules/lodash/isBuffer.js ***!
  \******************************************/
/***/ ((module, exports, __webpack_require__) => {

/* module decorator */ module = __webpack_require__.nmd(module);
var root = __webpack_require__(/*! ./_root */ "../node_modules/lodash/_root.js"),
    stubFalse = __webpack_require__(/*! ./stubFalse */ "../node_modules/lodash/stubFalse.js");

/** Detect free variable `exports`. */
var freeExports =  true && exports && !exports.nodeType && exports;

/** Detect free variable `module`. */
var freeModule = freeExports && "object" == 'object' && module && !module.nodeType && module;

/** Detect the popular CommonJS extension `module.exports`. */
var moduleExports = freeModule && freeModule.exports === freeExports;

/** Built-in value references. */
var Buffer = moduleExports ? root.Buffer : undefined;

/* Built-in method references for those with the same name as other `lodash` methods. */
var nativeIsBuffer = Buffer ? Buffer.isBuffer : undefined;

/**
 * Checks if `value` is a buffer.
 *
 * @static
 * @memberOf _
 * @since 4.3.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a buffer, else `false`.
 * @example
 *
 * _.isBuffer(new Buffer(2));
 * // => true
 *
 * _.isBuffer(new Uint8Array(2));
 * // => false
 */
var isBuffer = nativeIsBuffer || stubFalse;

module.exports = isBuffer;


/***/ }),

/***/ "../node_modules/lodash/isFunction.js":
/*!********************************************!*\
  !*** ../node_modules/lodash/isFunction.js ***!
  \********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseGetTag = __webpack_require__(/*! ./_baseGetTag */ "../node_modules/lodash/_baseGetTag.js"),
    isObject = __webpack_require__(/*! ./isObject */ "../node_modules/lodash/isObject.js");

/** `Object#toString` result references. */
var asyncTag = '[object AsyncFunction]',
    funcTag = '[object Function]',
    genTag = '[object GeneratorFunction]',
    proxyTag = '[object Proxy]';

/**
 * Checks if `value` is classified as a `Function` object.
 *
 * @static
 * @memberOf _
 * @since 0.1.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a function, else `false`.
 * @example
 *
 * _.isFunction(_);
 * // => true
 *
 * _.isFunction(/abc/);
 * // => false
 */
function isFunction(value) {
  if (!isObject(value)) {
    return false;
  }
  // The use of `Object#toString` avoids issues with the `typeof` operator
  // in Safari 9 which returns 'object' for typed arrays and other constructors.
  var tag = baseGetTag(value);
  return tag == funcTag || tag == genTag || tag == asyncTag || tag == proxyTag;
}

module.exports = isFunction;


/***/ }),

/***/ "../node_modules/lodash/isLength.js":
/*!******************************************!*\
  !*** ../node_modules/lodash/isLength.js ***!
  \******************************************/
/***/ ((module) => {

/** Used as references for various `Number` constants. */
var MAX_SAFE_INTEGER = 9007199254740991;

/**
 * Checks if `value` is a valid array-like length.
 *
 * **Note:** This method is loosely based on
 * [`ToLength`](http://ecma-international.org/ecma-262/7.0/#sec-tolength).
 *
 * @static
 * @memberOf _
 * @since 4.0.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
 * @example
 *
 * _.isLength(3);
 * // => true
 *
 * _.isLength(Number.MIN_VALUE);
 * // => false
 *
 * _.isLength(Infinity);
 * // => false
 *
 * _.isLength('3');
 * // => false
 */
function isLength(value) {
  return typeof value == 'number' &&
    value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
}

module.exports = isLength;


/***/ }),

/***/ "../node_modules/lodash/isObject.js":
/*!******************************************!*\
  !*** ../node_modules/lodash/isObject.js ***!
  \******************************************/
/***/ ((module) => {

/**
 * Checks if `value` is the
 * [language type](http://www.ecma-international.org/ecma-262/7.0/#sec-ecmascript-language-types)
 * of `Object`. (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
 *
 * @static
 * @memberOf _
 * @since 0.1.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is an object, else `false`.
 * @example
 *
 * _.isObject({});
 * // => true
 *
 * _.isObject([1, 2, 3]);
 * // => true
 *
 * _.isObject(_.noop);
 * // => true
 *
 * _.isObject(null);
 * // => false
 */
function isObject(value) {
  var type = typeof value;
  return value != null && (type == 'object' || type == 'function');
}

module.exports = isObject;


/***/ }),

/***/ "../node_modules/lodash/isObjectLike.js":
/*!**********************************************!*\
  !*** ../node_modules/lodash/isObjectLike.js ***!
  \**********************************************/
/***/ ((module) => {

/**
 * Checks if `value` is object-like. A value is object-like if it's not `null`
 * and has a `typeof` result of "object".
 *
 * @static
 * @memberOf _
 * @since 4.0.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
 * @example
 *
 * _.isObjectLike({});
 * // => true
 *
 * _.isObjectLike([1, 2, 3]);
 * // => true
 *
 * _.isObjectLike(_.noop);
 * // => false
 *
 * _.isObjectLike(null);
 * // => false
 */
function isObjectLike(value) {
  return value != null && typeof value == 'object';
}

module.exports = isObjectLike;


/***/ }),

/***/ "../node_modules/lodash/isTypedArray.js":
/*!**********************************************!*\
  !*** ../node_modules/lodash/isTypedArray.js ***!
  \**********************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var baseIsTypedArray = __webpack_require__(/*! ./_baseIsTypedArray */ "../node_modules/lodash/_baseIsTypedArray.js"),
    baseUnary = __webpack_require__(/*! ./_baseUnary */ "../node_modules/lodash/_baseUnary.js"),
    nodeUtil = __webpack_require__(/*! ./_nodeUtil */ "../node_modules/lodash/_nodeUtil.js");

/* Node.js helper references. */
var nodeIsTypedArray = nodeUtil && nodeUtil.isTypedArray;

/**
 * Checks if `value` is classified as a typed array.
 *
 * @static
 * @memberOf _
 * @since 3.0.0
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a typed array, else `false`.
 * @example
 *
 * _.isTypedArray(new Uint8Array);
 * // => true
 *
 * _.isTypedArray([]);
 * // => false
 */
var isTypedArray = nodeIsTypedArray ? baseUnary(nodeIsTypedArray) : baseIsTypedArray;

module.exports = isTypedArray;


/***/ }),

/***/ "../node_modules/lodash/keysIn.js":
/*!****************************************!*\
  !*** ../node_modules/lodash/keysIn.js ***!
  \****************************************/
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

var arrayLikeKeys = __webpack_require__(/*! ./_arrayLikeKeys */ "../node_modules/lodash/_arrayLikeKeys.js"),
    baseKeysIn = __webpack_require__(/*! ./_baseKeysIn */ "../node_modules/lodash/_baseKeysIn.js"),
    isArrayLike = __webpack_require__(/*! ./isArrayLike */ "../node_modules/lodash/isArrayLike.js");

/**
 * Creates an array of the own and inherited enumerable property names of `object`.
 *
 * **Note:** Non-object values are coerced to objects.
 *
 * @static
 * @memberOf _
 * @since 3.0.0
 * @category Object
 * @param {Object} object The object to query.
 * @returns {Array} Returns the array of property names.
 * @example
 *
 * function Foo() {
 *   this.a = 1;
 *   this.b = 2;
 * }
 *
 * Foo.prototype.c = 3;
 *
 * _.keysIn(new Foo);
 * // => ['a', 'b', 'c'] (iteration order is not guaranteed)
 */
function keysIn(object) {
  return isArrayLike(object) ? arrayLikeKeys(object, true) : baseKeysIn(object);
}

module.exports = keysIn;


/***/ }),

/***/ "../node_modules/lodash/stubFalse.js":
/*!*******************************************!*\
  !*** ../node_modules/lodash/stubFalse.js ***!
  \*******************************************/
/***/ ((module) => {

/**
 * This method returns `false`.
 *
 * @static
 * @memberOf _
 * @since 4.13.0
 * @category Util
 * @returns {boolean} Returns `false`.
 * @example
 *
 * _.times(2, _.stubFalse);
 * // => [false, false]
 */
function stubFalse() {
  return false;
}

module.exports = stubFalse;


/***/ }),

/***/ "@grafana/data":
/*!********************************!*\
  !*** external "@grafana/data" ***!
  \********************************/
/***/ ((module) => {

"use strict";
module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_data__;

/***/ }),

/***/ "@grafana/runtime":
/*!***********************************!*\
  !*** external "@grafana/runtime" ***!
  \***********************************/
/***/ ((module) => {

"use strict";
module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_runtime__;

/***/ }),

/***/ "@grafana/ui":
/*!******************************!*\
  !*** external "@grafana/ui" ***!
  \******************************/
/***/ ((module) => {

"use strict";
module.exports = __WEBPACK_EXTERNAL_MODULE__grafana_ui__;

/***/ }),

/***/ "react":
/*!************************!*\
  !*** external "react" ***!
  \************************/
/***/ ((module) => {

"use strict";
module.exports = __WEBPACK_EXTERNAL_MODULE_react__;

/***/ }),

/***/ "rxjs":
/*!***********************!*\
  !*** external "rxjs" ***!
  \***********************/
/***/ ((module) => {

"use strict";
module.exports = __WEBPACK_EXTERNAL_MODULE_rxjs__;

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
/******/ 			loaded: false,
/******/ 			exports: {}
/******/ 		};
/******/ 	
/******/ 		// Execute the module function
/******/ 		__webpack_modules__[moduleId](module, module.exports, __webpack_require__);
/******/ 	
/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;
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
/******/ 	/* webpack/runtime/global */
/******/ 	(() => {
/******/ 		__webpack_require__.g = (function() {
/******/ 			if (typeof globalThis === 'object') return globalThis;
/******/ 			try {
/******/ 				return this || new Function('return this')();
/******/ 			} catch (e) {
/******/ 				if (typeof window === 'object') return window;
/******/ 			}
/******/ 		})();
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
/******/ 	/* webpack/runtime/node module decorator */
/******/ 	(() => {
/******/ 		__webpack_require__.nmd = (module) => {
/******/ 			module.paths = [];
/******/ 			if (!module.children) module.children = [];
/******/ 			return module;
/******/ 		};
/******/ 	})();
/******/ 	
/************************************************************************/
var __webpack_exports__ = {};
// This entry need to be wrapped in an IIFE because it need to be in strict mode.
(() => {
"use strict";
/*!*******************!*\
  !*** ./module.ts ***!
  \*******************/
__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "plugin": () => (/* binding */ plugin)
/* harmony export */ });
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @grafana/data */ "@grafana/data");
/* harmony import */ var _grafana_data__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_grafana_data__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _DataSource__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./DataSource */ "./DataSource.ts");
/* harmony import */ var _ConfigEditor__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./ConfigEditor */ "./ConfigEditor.tsx");
/* harmony import */ var _QueryEditor__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./QueryEditor */ "./QueryEditor.tsx");
/* harmony import */ var _AnnotationQueryEditor__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./AnnotationQueryEditor */ "./AnnotationQueryEditor.ts");
/* harmony import */ var _QueryEditorHelp__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./QueryEditorHelp */ "./QueryEditorHelp.tsx");




//import { AnnotationsQueryCtrl } from './AnnotationsQueryCtrl';

//import ExploreQueryEditor from './ExploreQueryEditor';
//import ExploreStartPage from './ExploreStartPage';

var plugin = new _grafana_data__WEBPACK_IMPORTED_MODULE_0__.DataSourcePlugin(_DataSource__WEBPACK_IMPORTED_MODULE_1__.DataSource).setConfigEditor(_ConfigEditor__WEBPACK_IMPORTED_MODULE_2__.ConfigEditor).setQueryEditor(_QueryEditor__WEBPACK_IMPORTED_MODULE_3__.QueryEditor).setAnnotationQueryCtrl(_AnnotationQueryEditor__WEBPACK_IMPORTED_MODULE_4__.AnnotationQueryEditor)
//.setExploreQueryField(ExploreQueryEditor)
.setQueryEditorHelp(_QueryEditorHelp__WEBPACK_IMPORTED_MODULE_5__["default"]);
})();

/******/ 	return __webpack_exports__;
/******/ })()
;
});;
//# sourceMappingURL=module.js.map