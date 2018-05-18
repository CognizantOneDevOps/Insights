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
import Neo4jDataSource from './datasource';
import { Neo4jQueryCtrl } from './queryctrl';
declare class Neo4jConfigCtrl {
    static templateUrl: string;
}
declare class Neo4jQueryOptionsCtrl {
    static templateUrl: string;
}
declare class Neo4jAnnotationsQueryCtrl {
    static templateUrl: string;
}
export { Neo4jDataSource as Datasource, Neo4jQueryCtrl as QueryCtrl, Neo4jConfigCtrl as ConfigCtrl, Neo4jQueryOptionsCtrl as QueryOptionsCtrl, Neo4jAnnotationsQueryCtrl as AnnotationsQueryCtrl };
