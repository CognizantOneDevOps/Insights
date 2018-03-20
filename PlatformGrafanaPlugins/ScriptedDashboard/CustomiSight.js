/********************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/


/* global _ */

/*
 * Complex scripted dashboard
 * This script generates a dashboard object that Grafana can load. It also takes a number of user
 * supplied URL parameters (in the ARGS variable)
 *
 * Return a dashboard object, or a function
 *
 * For async scripts, return a function, this function must take a single callback function as argument,
 * call this callback function with the dashboard object (look at scripted_async.js for an example)
 */

'use strict';

// accessible variables in this scope
var window, document, ARGS, $, jQuery, moment, kbn;

// Setup some variables
var dashboard;

// All url parameters are available via the ARGS object
var ARGS;

// Intialize a skeleton with nothing but a rows array and service object
dashboard = {
	rows: [],
};

var url = ARGS.url;
document.body.innerHTML = '';

$('<style>iframe {overflow:hidden;}</style>').appendTo('head');

var documentWidth = $(document).width();
var width = documentWidth - 10;
var documentHeight = window.innerHeight;

var addDashboardIframeToDom = function (url) {
	$('<iframe>', {
		src: url,
		id: 'iSightIframe',
		frameborder: 0,
		width: width,
		height: documentHeight,
		//style : 'overflow-y: hidden; width:'+width+'px; height:'+documentHeight+'px;'
		scrolling: 'no'
	}).appendTo('body');
};

var addRouteChangeDetector = function () {
	var grafanaApp = $('#iSightIframe').contents().find('.grafana-app');
	if (grafanaApp.length === 0 || document.getElementById("iSightIframe").contentWindow.angular === undefined) {
		setTimeout(function () {
			addRouteChangeDetector();
		}, 20);
	} else {
		setTimeout(function () {
			var $injector = document.getElementById("iSightIframe").contentWindow.angular.element(".grafana-app").injector();
			$injector.invoke(function ($rootScope) {
				$rootScope.$on('$viewContentLoaded', function (next, current) {
					calculateHeight();
				});
			});
		}, 20);
		addStyleTag();
	}
};

var calculateHeight = function (time) {
	var view = $('#iSightIframe').contents().find('.main-view');
	var dashboard = $('#iSightIframe').contents().find('.dashboard-container');
	var playlist = $('#iSightIframe').contents().find('.page-container');
	if (view.length !== 0 && (dashboard.length !== 0 || playlist.length !== 0)) {
		var height = $('#iSightIframe').contents().find('.main-view').height();
		if(height < 800){
			height = 800;
			$('#iSightIframe').width(documentWidth);
		}else{
			$('#iSightIframe').width(documentWidth - 10);
		}
		$('#iSightIframe').width(documentWidth);
		$('#iSightIframe').height(height);
		window.parent.postMessage(height, '*');
		if (time !== undefined && time !== 0) {
			setTimeout(function () {
				calculateHeight(time - 20);
			}, 20);
		}
	}else{
		setTimeout(function () {
			calculateHeight(3000);
		}, 20);
	}

	/*if (view.length === 0 || dashboard.length === 0) {
		setTimeout(function () {
			calculateHeight(3000);
		}, 20);
	} else {
		var height = $('#iSightIframe').contents().find('.main-view').height();
		if(height < 800){
			height = 800;
			$('#iSightIframe').width(documentWidth);
		}else{
			$('#iSightIframe').width(documentWidth - 10);
		}
		$('#iSightIframe').width(documentWidth);
		$('#iSightIframe').height(height);
		window.parent.postMessage(height, '*');
		if (time !== 0) {
			setTimeout(function () {
				calculateHeight(time - 20);
			}, 20);
		}
	}*/
}

var addStyleTag = function () {
	var head = $('#iSightIframe').contents().find('head');
	if (head.length === 0) {
		setTimeout(function () {
			addStyleTag();
		}, 20);
	} else {
		var style = "<style type=\"text/css\">" +
			".navbar-brand-btn {display : none !important;}\n" +
			".navbar-inner {display : none !important;}" +
			"h1 {display : none !important;}" +
			".search-item-dash-home {display : none !important;}" +
			".search-button-row-explore-link {display : none !important;}" +
			".footer {display : none !important;}" +
			"</style>";
		//dropdown-menu
		$(style).appendTo(head);
	}
};

var addDashboardClickDetector = function(){
	var iframeBody = $('#iSightIframe').contents().find('.grafana-app');
	if (iframeBody.length === 0) {
		setTimeout(function () {
			addDashboardClickDetector();
		}, 20);
	} else {
		$(iframeBody).on('mouseup', function () {
			calculateHeight(3000);
		})
	}
};

//Dast Fixes for URL parameter
var currentreferrer  = document.createElement ('a');
currentreferrer.href = window.location.origin;
var grafanaUrl = document.createElement('a');
grafanaUrl.href = url;
if (currentreferrer.hostname === grafanaUrl.hostname){
	addDashboardIframeToDom(url);
	calculateHeight();
	addStyleTag();
	addRouteChangeDetector();
	addDashboardClickDetector();
}

return dashboard;
