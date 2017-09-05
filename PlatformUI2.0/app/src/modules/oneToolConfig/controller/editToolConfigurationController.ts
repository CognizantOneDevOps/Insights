/*******************************************************************************
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

/// <reference path="../../../_all.ts" />

module ISightApp {
    export class EditToolConfigurationController {
        static $inject = ['$mdDialog', '$route'];
        constructor( private $mdDialog, private $route) {
           this.ToolConfigurationDataModel = this['locals'].toolConfigurationData.ToolConfigurationDataModel;
           this.toolData = JSON.stringify(this.ToolConfigurationDataModel,null,"    ");
        }
         ToolConfigurationDataModel : any;
         toolData : any;

        hide(): void {
            this.$mdDialog.hide();
        }
        cancel(): void {
            this.$mdDialog.cancel();
        }

        save(): void {
             this['locals'].toolConfigurationData.ToolConfigurationDataModel = JSON.parse(this.toolData);
            this.cancel();
        }
    }
}
