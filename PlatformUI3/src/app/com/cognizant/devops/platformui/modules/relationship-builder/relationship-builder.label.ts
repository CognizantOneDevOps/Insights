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

export class RelationLabel {
    destination: string;
    source: string;
    relationName: string;
    detailProp: string;


    public setData(destination, source, relationName, detailProp): void {
        this.destination = destination;
        this.source = source;
        this.relationName = relationName;
        this.detailProp = detailProp;
    }
    constructor(destination, source, relationName, detailProp) {
        this.destination = destination;
        this.source = source;
        this.relationName = relationName;
        this.detailProp = detailProp;

    }
}