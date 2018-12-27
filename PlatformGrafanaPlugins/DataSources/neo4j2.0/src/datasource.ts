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

///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />

//import angular from 'angular';
//import _ from 'lodash';

//import * as dateMath from 'app/core/utils/datemath';

export default class Neo4jDatasource {
  type: any;
  url: string;
  name: string;
  /** @ngInject */
  constructor(instanceSettings, private $q, private backendSrv, private templateSrv) {
    this.type = instanceSettings.type;
    this.url = instanceSettings.url;
    this.name = instanceSettings.name;
  }

  addTimestampToQuery(query, options) {
    if (query && options) {
      let range = options.range;
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

	/*
	This function gets executed when EnableTarget and TimeSeries checkbox is clicked.
	Purpose of this function is show data based on alias name in Status Panel with suuported Neo4j Datasource.
	*/
	processStatusQueryResponse(data, options, timestamp) {
			/*let query = data['targets'][0].target;
			let preseneceOfAs = query.split(" as ");
			var obj = Object.assign({}, data['targets'][0]);
			var data_targets = [];
			for(let i = 0; i < preseneceOfAs.length-1; i++) {
				var obj = Object.assign({}, data['targets'][0]);
				obj.refId = i;
				obj.$$hashKey = "object:"+i;
				data_targets.push(obj);
			}
			data['targets'] = data_targets;*/
			let targets = data['targets'];
			let results = data['results'];
			let defaultResponse = options ? false : true;
			let response = []
			let finalResponse = {}
			for (let i in targets) {
			  let target = targets[i];
			  let result = results[i];
			  if (target.timeSeries) {
				let datapoints = [];
				let targetResponse = {
				  target: target.refId,
				  datapoints: datapoints
				};
				let targetDatapointsMap = {}
				let rows = result.data;
				let columns = result.columns;
				let multiSeriesResponse = false;
				var i:number;
				for (let r in rows) {
						let row = rows[r].row;
						for (let values in rows[r].row) {
							let targetName = columns[values];
							let targetDataPoints = targetDatapointsMap[targetName];
							if (targetDataPoints === undefined) {
									targetDataPoints = [];
									targetDatapointsMap[targetName] = targetDataPoints;
									response.push({
											target: targetName,
											datapoints: targetDataPoints
									});
									multiSeriesResponse = true;
							}
							targetDataPoints.push([rows[r].row[values], timestamp * 1000]);
							finalResponse[values] = response;						
						}
				}
						return response;
			  }
			}
		return null;		
	}
  /*
    Need to provide following options:
    1. Consider time interval (5s, 30 mins, 30 hours, etc)
    2. Provide group by options (group by a result column)
    3. Table format
  */
  processResponse(data, options) {
    let timestamp = new Date().getTime() * 1000;
    if (options && options.range && options.range.to) {
      timestamp = options.range.to.valueOf();
    }
	var enableTarget = false;
	if(options)
	{
		enableTarget = (options.targets[0].enableTarget) ? options.targets[0].enableTarget : false;
	}
	if(enableTarget)
	{
		return this.processStatusQueryResponse(data, options, timestamp);
	}
    let targets = data['targets'];
    let results = data['results'];
    let defaultResponse = options ? false : true;
    let response = []
    for (let i in targets) {
      let target = targets[i];
      let result = results[i];
      if (target.timeSeries) {
        let datapoints = [];
        let targetResponse = {
          target: target.refId,
          datapoints: datapoints
        };
        let targetDatapointsMap = {}
        let rows = result.data;
        let multiSeriesResponse = false;
        for (let r in rows) {
          let row = rows[r].row;
          if (result.columns.length === 1) {
            datapoints.push([row[0], timestamp]);
          } else if (result.columns.length === 3) {
            let targetName = row[2];
            let targetDataPoints = targetDatapointsMap[targetName]
            if (targetDataPoints === undefined) {
              targetDataPoints = [];
              targetDatapointsMap[targetName] = targetDataPoints;
              response.push({
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
        }
      } else if (target.table) {
        let responseColumns = [];
        let responseRows = [];
        let tableResponse = { columns: responseColumns, rows: responseRows, type: "table" };
        let columns = result.columns;
        let data = result.data;
        for (let columnId in columns) {
          responseColumns.push({ text: columns[columnId] });
        }
        let rows = result.data;
        for (let r in rows) {
          responseRows.push(rows[r].row);
        }
        response.push(tableResponse);
      } else {
        defaultResponse = true;
        break;
      }
    }
    if (defaultResponse) {
      return data;
    }
    return response;
  }

  executeCypherQuery(cypherQuery, targets, options) {
    var deferred = this.$q.defer();
    var flag = this.checkCypherQueryModificationKeyword(cypherQuery);
	var flagToShowErrorMessage = false;
    if (flag == true) {
	  flagToShowErrorMessage = false;
      var self = this;
      this.backendSrv.datasourceRequest({
        url: this.url,
        method: 'POST',
        data: JSON.stringify(cypherQuery)
      }).then(function (response) {
        var data = response.data;
        if (response.status === 200) {
          if (data && data.results && data.results.length > 0) {
            data['targets'] = targets;
            deferred.resolve({ data: self.processResponse(data, options) });
          } else {
            deferred.resolve({ status: "success", message: "No data returned", title: "success" });
          }
        } else {
          deferred.resolve({ status: "failure", message: "Unable to connect to Datasource", title: "Failure" });
        }
      });

    }
    else {
	  flagToShowErrorMessage = true;
      deferred.resolve({ status: "failure", message: "Cannot run modification query in neo4j", title: "Failure" });
      console.log("It has create//delete/set/update keyword.");
    }
	if(options != null){
		options.targets[0].checkQuery = flagToShowErrorMessage;
	}
    return deferred.promise;
  }

  checkCypherQueryModificationKeyword(cypherQuery) {
    var keywords: string[];
    keywords = ["create", "delete", "set", "update", "merge", "detach"];
    var flag: number = 0;
    var queryCorrect = true;
	if(cypherQuery.statements[0].statement == undefined)
	{
		return queryCorrect;
	}
    var j;
    for (j in keywords) 
	{
		var query = (cypherQuery.statements[0].statement.toString()).toLowerCase();
		if(query.indexOf( " " + keywords[j] + " " ) >= 0){console.log(keywords[j]+" is present as an individual word.");flag = 1; break;}
		if(query.indexOf( "\n" + keywords[j] ) >= 0 && query.indexOf( "\n" + keywords[j] + " ") >= 0){console.log(keywords[j]+" is present after new line.");flag = 1; break;}
		if(query.indexOf( keywords[j] + ")" ) >= 0 
					|| query.indexOf( keywords[j] + "(" ) >= 0 
						|| query.indexOf( ")"+keywords[j]) >= 0 
							|| query.indexOf( "("+keywords[j]) >= 0 
								|| query.indexOf( " "+keywords[j] + "\n" ) >= 0
									|| query.indexOf( "\n" + keywords[j] + "\n" ) >= 0)
										{console.log(keywords[j]+" is present.");flag = 1; break;}
		//if(query.indexOf( keywords[j] + " (") >= 0){console.log(keywords[j]+" is present before ");flag = 1; break;}
    }
    if (flag == 0) 
	{ 
		return queryCorrect; 
	}
    else 
	{
		queryCorrect = false;
		return queryCorrect;
    }
  }

  //let templateName = variable.model.name;
  //Two places where the templates can be used:
  //1. Labels Names
  //2. Where clauseb
  /*const regex = /(match.*where\s?|match\s\(?)(.*)\$LABELS/g;
      let match = regex.exec(query);
      if (match && match.length > 2) {
        let querySegment = match[2];
        if(querySegment && querySegment.indexOf(' IN ') > -1){
          return JSON.stringify(value);
        }
      }*/

  applyTemplateVariables(value, variable, formatValue) {
    if (typeof value === 'string') {
      let values = [];
      values.push(value);
      value = values;
    }
    return JSON.stringify(value);
  }

  query(options) {
    //var adhocFilters = this.templateSrv.getAdhocFilters(this.name);
    var targets = options.targets;
    var target = targets[0];
    var resultCache = (target.rescache) ? target.rescache : false;
    if (resultCache) {
      let range = options.range;
      var fromTime = range.from.valueOf() / 1000;
      var toTime = range.to.valueOf() / 1000;
    }
    var cypherQuery = {};
    var statements = [];
    var metadata = [];

    cypherQuery['statements'] = statements;
    cypherQuery['metadata'] = metadata;

    for (var i in targets) {
      var target = targets[i];
      let query = this.templateSrv.replace(target.target, options.scopedVars, this.applyTemplateVariables);
      query = this.addTimestampToQuery(query, options);
      var resultDataContents = [];
      if (target.graph) {
        resultDataContents.push("graph");
      } else {
        resultDataContents.push("row");
      }
      var statement = {
        "statement": query,
        "includeStats": target.stats,
        "resultDataContents": resultDataContents
      };

      var cachingValue;
      if (target.selectionval === "Fixed Time") {
        cachingValue = target.cacheFixedTime;
      } else {
        cachingValue = target.cacheVariance;
      }

      var cacheoptions = {
        "startTime": fromTime,
        "endTime": toTime,
        "resultCache": resultCache,
        "testDB": false,
        "cachingType": target.selectionval,
        "cachingValue": cachingValue
      };

      statements.push(statement);
      metadata.push(cacheoptions);
    }
    return this.executeCypherQuery(cypherQuery, targets, options);
  }

  annotationQuery(options) {
  }

  metricFindQuery(query) {
    var cypherQuery = {};
    var statements = [];
    cypherQuery['statements'] = statements;
    query = this.addTimestampToQuery(query, null);
    query = this.templateSrv.replace(query, {}, this.applyTemplateVariables);
    var resultDataContents = ["row"];
    var statement = {
      "statement": query,
      "includeStats": false,
      "resultDataContents": resultDataContents
    };
    statements.push(statement);
    let deferred = this.$q.defer();
    let isResolved = false;
    this.executeCypherQuery(cypherQuery, null, null).then(function (response) {
      if (response && response.data && response.data.results) {
        let result = response.data.results[0];
        if (result) {
          let data = result.data;
          if (data) {
            let metrics = [];
            for (let row of data) {
              let record = row.row;
              if (record && record.length > 0) {
                metrics.push({ text: record[0], value: record[0] });
              }
            }
            deferred.resolve(metrics);
            isResolved = true;
          }
        }
      }
      if (!isResolved) {
        deferred.resolve([]);
      }
    });
    return deferred.promise;
  }

  testDatasource() {
    var queryJson = {
      "statements": [
        {
          "statement": "match (n) return n limit 1",
          "includeStats": true,
          "resultDataContents": ["row", "graph"]
        }
      ],
      "metadata": [{
        "testDB": true
      }]
    };
    var testQuery = JSON.stringify(queryJson);
    var deferred = this.$q.defer();
    //var self = this;
    try {
      this.backendSrv.datasourceRequest({
        url: this.url,
        method: 'POST',
        data: testQuery
      }).then(function (response) {
        var data = response.data;
        if (response.status === 200) {
          if (data && data.results.length > 0) {
            deferred.resolve({ status: "success", message: "Data source is working", title: "Success" });
          } else {
            deferred.resolve({ status: "failure", message: "No data returned", title: "failure" });
          }
        } else {
          deferred.resolve({ status: "failure", message: "Unable to connect to Datasource", title: "Failure" });
        }
      });
    } catch (error) {
      deferred.resolve({ status: "failure", message: "Unable to connect to Datasource", title: "Failure" });
    }
    return deferred.promise;
  }
}
