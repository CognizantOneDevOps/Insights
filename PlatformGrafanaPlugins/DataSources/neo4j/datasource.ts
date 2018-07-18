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

///<reference path="../../../headers/common.d.ts" />

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

  executeCypherQuery(cypherQuery, targets) {
    var deferred = this.$q.defer();
    //var self = this;
    this.backendSrv.datasourceRequest({
      url: this.url,
      method: 'POST',
      data: JSON.stringify(cypherQuery)
    }).then(function (response) {
      var data = response.data;
      if (response.status === 200) {
        if (data && data.results && data.results.length > 0) {
          data['targets'] = targets;
          deferred.resolve({ data: data });
        } else {
          deferred.resolve({ status: "success", message: "No data returned", title: "success" });
        }
      } else {
        deferred.resolve({ status: "failure", message: "Unable to connect to Datasource", title: "Failure" });
      }
    });
    return deferred.promise;
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
    return this.executeCypherQuery(cypherQuery, targets);
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
    this.executeCypherQuery(cypherQuery, null).then(function (response) {
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
