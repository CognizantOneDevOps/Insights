import _ from 'lodash';

import {
  MetricFindValue,
  DataQueryRequest, 
  DataSourceApi, 
  DataSourceInstanceSettings, 
  DataQueryResponse, 
  AnnotationQueryRequest,
  AnnotationEvent
} from '@grafana/data';

import { getBackendSrv } from '@grafana/runtime';

import { MyQuery, MyDataSourceOptions } from './types';

import { from, merge, Observable } from 'rxjs';

import {convertResponseToDataFramesTable, applyTemplateVariables, addTimestampToQuery, convertResponseToDataFramesGraph, convertResponseToDataFramesRaw } from 'Utils';

export class DataSource extends DataSourceApi<MyQuery, MyDataSourceOptions> {

  type: any;
  url: any;
  name: string;
  serviceUrl: any;
  datasourceId: any;
  datasourceType: any;
  datasourceName: any;
  userId: any;
  userName: any;
  email: any;
  orgId: any;
  logging: any;
  tokenEnabled: any;
  
  constructor(instanceSettings: DataSourceInstanceSettings<MyDataSourceOptions>, private templateSrv) {
    super(instanceSettings);
    this.type = instanceSettings.type;
    this.url = instanceSettings.url;
    this.name = instanceSettings.name;
    this.serviceUrl = instanceSettings.jsonData.serviceUrl;
    this.datasourceId = instanceSettings.id;
    this.logging = instanceSettings.jsonData.logging;
    this.tokenEnabled = instanceSettings.jsonData.authToken;
    if(this.logging){
        this.getUserDetailsPerDashboardHit();
    }
  }

  /**provides ability to test connection from connection settings page . */
  testDatasource() {
    const queryJson = {
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
    let testQuery = JSON.stringify(queryJson);
    let routePath = '';
    if(this.tokenEnabled){
      routePath = this.url + `/platformservice`;
    }else{
      routePath = this.url
    }
    return getBackendSrv().datasourceRequest({ url: routePath, method: 'POST', data: testQuery }).then((res: any) => {
      let data = res.data;
      if (res.status === 200) {
        if (data && data.results.length > 0) {
          return { status: 'success', message: 'Data source is working' }
        } else {
          return { status: "failure", message: "No data returned", title: "failure" };
        }
      } else {
        return { status: 'error', message: res.error };
      }
    }).catch((err: any) => {
      console.log(err);
      if (err.data && err.data.errors[0].message) {
        return { status: 'error', message: err.data.errors[0].message };
      } else {
        return { status: 'error', message: err.status };
      }
    });
  }

  query(options: DataQueryRequest<MyQuery>) {
    const queries: any[] = [];
    const streams: Array<Observable<DataQueryResponse>> = [];
    const { range } = options;
    const from1 = range!.from.valueOf() / 1000;
    const to = range!.to.valueOf() / 1000;
    let cypherQuery = {};
    let statements = [] as any;
    let metadata = [] as any;
    let logInfo = {};
    cypherQuery['statements'] = statements;
    cypherQuery['metadata'] = metadata;
    logInfo['eventName'] = 'data-request';
    logInfo['panelId'] = options.panelId;
    logInfo['dashboardId'] = options.dashboardId;
    logInfo['datasourceId'] = this.datasourceId;
    logInfo['datasourceType'] = this.type;
    logInfo['datasourceName'] = this.name;
    logInfo['timestamp'] = new Date().toISOString();
    
    // Start streams and prepare queries
    for (const target of options.targets) {
      if (target.hide) {
        continue;
      }
      //let query = target.queryText;
      let query = this.templateSrv.replace(target.queryText, options.scopedVars, applyTemplateVariables);
      query = addTimestampToQuery(query, options);
      let resultDataContents = [];
      const statement = { "statement": query, "resultDataContents": resultDataContents };
      const cacheoptions = {
        "startTime": from1, "endTime": to, "testDB": false, "resultCache": target.cache, "cachingType": target.cacheType,
        "cachingValue": target.cacheValue
      };
      statements.push(statement);
      metadata.push(cacheoptions);
      queries.push({
        ...target,
        intervalMs: options.intervalMs,
        maxDataPoints: options.maxDataPoints,
        datasourceId: this.id,
      });
    }
    logInfo['query'] = cypherQuery;
    let startTime = Date.now();
    if (queries.length) {
      let routePath = '';
      if (this.tokenEnabled) {
        routePath = this.url + `/platformservice`;
      } else {
        routePath = this.url
      }
      const req: Promise<any> = getBackendSrv()
        .datasourceRequest({
          method: 'POST',
          url: routePath,
          data: cypherQuery
        })
        .then((res: any) => {
          let endTime = Date.now();
          logInfo['time_ms'] = endTime-startTime;
          logInfo['panelQuery'] = true;
          logInfo['requestId'] = options.requestId;
          if (this.logging) {
            try {
              logInfo['userId'] = this.userId;
              logInfo['userName'] = this.userName;
              logInfo['email'] = this.email;
              logInfo['orgId'] = this.orgId;
              this.logDashboardInfo(logInfo);
            } catch (e) {
              console.log(e);
            }
          }
          if (queries[0].table) {
            return convertResponseToDataFramesTable(queries, res);
          } else if (queries[0].graph) {
            return convertResponseToDataFramesGraph(queries, res);
          } else if (queries[0].raw) {
            return convertResponseToDataFramesRaw(queries, res);
          } else if (queries[0].mode =='Metrics' || queries[0].showingTable) { //Explore Functionality
            return convertResponseToDataFramesTable(queries, res);
          } else if (queries[0].mode =='Metrics' || queries[0].showingGraph) { //Explore Functionality
            return convertResponseToDataFramesGraph(queries, res);
          } else {
              throw new Error("Select some response type");
          }
        });
      streams.push(from(req));
    }
    //console.log(logInfo);
    return merge(...streams);
  }

  private logDashboardInfo(logInfo: {}) {
    getBackendSrv().datasourceRequest({
      method: 'POST',
      url: this.url+'/neo4jLog',
      data: logInfo
    })
      .then((res: any) => {
        console.log(res);
      });
  }

  private async getUserDetailsPerDashboardHit() {
  await getBackendSrv().datasourceRequest({
      method: 'GET',
      url: '/api/user',
    }).then((res: any) => {
        this.userId = res.data.id;
        this.userName = res.data.name;
        this.email = res.data.email;
        this.orgId = res.data.orgId;
      });

  }

  metricFindQuery(query: string, options: any) {
    var cypherQuery = {};
    var statements = [] as any;
    cypherQuery['statements'] = statements;
    query = addTimestampToQuery(query, null);
    query = this.templateSrv.replace(query, {}, applyTemplateVariables);
    var resultDataContents = ["row"];
    var statement = {
      "statement": query,
      "includeStats": false,
      "resultDataContents": resultDataContents
    };
    statements.push(statement);
    return new Promise<MetricFindValue[]>((resolve, reject) => {
      return getBackendSrv().datasourceRequest({ url: this.url, method: 'POST', data: cypherQuery }).then((res: any) => {
        if (res.status === 200 && res.data.errors.length === 0) {
          let result = res.data.results[0];
          if (result) {
            let data = result.data;
            if (data) {
              let metrics = [] as any;
              for (let row of data) {
                let record = row.row;
                if (record && record.length > 0) {
                  metrics.push({ text: record[0], value: record[0] });
                }
              }
              return resolve(metrics);
            }
          }
        } else {
          return reject(res.data.errors[0].message);
        }
      }).catch((err: any) => {
        console.log(err);
        if (err.data && err.data.message) {
          return { status: 'error', message: err.data.message };
        } else {
          return { status: 'error', message: err.status };
        }
      });
    });
  }

  


  

  /**
   * 
   *  all Below methods need improvisation.
   */
  async annotationQuery(options: AnnotationQueryRequest<MyQuery>): Promise<AnnotationEvent[]> {
    const events: AnnotationEvent[] = [];
    //const expression = options.annotation.queryText;
    const date = new Date();

    const event: AnnotationEvent = {
      time: date.valueOf(),
      text: 'foo',
      tags: ['bar'],
    };

    events.push(event);

    return events;
  }

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


}
