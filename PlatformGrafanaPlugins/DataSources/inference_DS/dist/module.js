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
System.register(['./datasource', './queryctrl'], function(exports_1) {
    var datasource_1, queryctrl_1;
    var Neo4jConfigCtrl, Neo4jQueryOptionsCtrl, Neo4jAnnotationsQueryCtrl;
    return {
        setters:[
            function (datasource_1_1) {
                datasource_1 = datasource_1_1;
            },
            function (queryctrl_1_1) {
                queryctrl_1 = queryctrl_1_1;
            }],
        execute: function() {
            Neo4jConfigCtrl = (function () {
                function Neo4jConfigCtrl() {
                }
                Neo4jConfigCtrl.templateUrl = 'partials/config.html';
                return Neo4jConfigCtrl;
            })();
            Neo4jQueryOptionsCtrl = (function () {
                function Neo4jQueryOptionsCtrl() {
                }
                Neo4jQueryOptionsCtrl.templateUrl = 'partials/query.options.html';
                return Neo4jQueryOptionsCtrl;
            })();
            Neo4jAnnotationsQueryCtrl = (function () {
                function Neo4jAnnotationsQueryCtrl() {
                }
                Neo4jAnnotationsQueryCtrl.templateUrl = 'partials/annotations.editor.html';
                return Neo4jAnnotationsQueryCtrl;
            })();
            exports_1("Datasource", datasource_1.default);
            exports_1("QueryCtrl", queryctrl_1.InferenceDSCtrl);
            exports_1("ConfigCtrl", Neo4jConfigCtrl);
            exports_1("QueryOptionsCtrl", Neo4jQueryOptionsCtrl);
            exports_1("AnnotationsQueryCtrl", Neo4jAnnotationsQueryCtrl);
        }
    }
});
//# sourceMappingURL=module.js.map