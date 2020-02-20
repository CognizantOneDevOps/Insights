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
    flag: boolean;
    relationshipProp: string
    destprop: string
    sourceprop: string
    selfRelation: boolean

    public setData(destination, source, relationName, sourceprop, destprop, relationshipProp, flag, selfRelation): void {
        this.destination = destination;
        this.source = source;
        this.relationName = relationName;
        this.flag = flag;
        this.relationshipProp = relationshipProp
        this.sourceprop = sourceprop
        this.destprop = destprop;
        this.selfRelation = selfRelation;
    }
    constructor(destination, source, relationName, sourceprop, destprop, relationshipProp, flag, selfRelation) {
        this.destination = destination;
        this.source = source;
        this.relationName = relationName;
        this.sourceprop = sourceprop;
        this.destprop = destprop;
        this.flag = flag;
        this.relationshipProp = relationshipProp;
        this.selfRelation = selfRelation;

    }
}