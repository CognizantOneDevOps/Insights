/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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

export class ToolLabelMapping {
    id: number;
    key: string;
    value: any;
    label: string;
    editProperties?: boolean = true;


    public setData(id, key, value, label, editProperties): void {
        this.id = id;
        this.key = key;
        this.value = value;
        this.label = label;
        this.editProperties = editProperties;
    }
    constructor(id, key, value, label, editProperties) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.label = label;
        this.editProperties = editProperties;
    }
    public setValue(value) {
        this.value = value;
    }
}