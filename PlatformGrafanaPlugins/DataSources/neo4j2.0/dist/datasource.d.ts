/// <reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
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
export default class Neo4jDatasource {
    private $q;
    private backendSrv;
    private templateSrv;
    type: any;
    url: string;
    name: string;
    /** @ngInject */
    constructor(instanceSettings: any, $q: any, backendSrv: any, templateSrv: any);
    addTimestampToQuery(query: any, options: any): any;
    processStatusQueryResponse(data: any, options: any, timestamp: any): any[];
    processResponse(data: any, options: any): any;
    executeCypherQuery(cypherQuery: any, targets: any, options: any): any;
    checkCypherQueryModificationKeyword(cypherQuery: any): boolean;
    applyTemplateVariables(value: any, variable: any, formatValue: any): string;
    query(options: any): any;
    annotationQuery(options: any): void;
    metricFindQuery(query: any): any;
    testDatasource(): any;
}
