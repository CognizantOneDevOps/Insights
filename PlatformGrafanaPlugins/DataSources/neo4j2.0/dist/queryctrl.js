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
System.register(['app/plugins/sdk'], function(exports_1) {
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var sdk_1;
    var Neo4jQueryCtrl;
    return {
        setters:[
            function (sdk_1_1) {
                sdk_1 = sdk_1_1;
            }],
        execute: function() {
            Neo4jQueryCtrl = (function (_super) {
                __extends(Neo4jQueryCtrl, _super);
                /** @ngInject **/
                function Neo4jQueryCtrl($scope, $injector /* , private templateSrv, private $q, private uiSegmentSrv */) {
                    _super.call(this, $scope, $injector);
                    this.target.rawQuery = true;
                }
                Neo4jQueryCtrl.prototype.onChangeInternal = function () {
                    this['panelCtrl'].refresh();
                };
                Neo4jQueryCtrl.templateUrl = 'partials/query.editor.html';
                return Neo4jQueryCtrl;
            })(sdk_1.QueryCtrl);
            exports_1("Neo4jQueryCtrl", Neo4jQueryCtrl);
        }
    }
});
//# sourceMappingURL=queryctrl.js.map