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
import {QueryCtrl} from 'app/plugins/sdk';

export class Neo4jQueryCtrl extends QueryCtrl {
  static templateUrl = 'partials/query.editor.html';

  target: any;

  /** @ngInject **/
  constructor($scope, $injector/* , private templateSrv, private $q, private uiSegmentSrv */) {
    super($scope, $injector);
    this.target.rawQuery = true;	
  }

  public onChangeInternal(): void{
    this['panelCtrl'].refresh();	
  }
}

