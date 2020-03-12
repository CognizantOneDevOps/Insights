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

export class DerivedOperations {
    wid: string;
    operationName: string;
    operationFields: {};
    webhookName: string;

    public setData(wid, operationName, operationFields, webhookName): void {
        this.wid = wid;
        this.operationName = operationName;
        this.operationFields = operationFields;
        this.webhookName = webhookName;
    }

    public markAsDelete() {
        this.operationFields = "";
    }

    public setWebhookName(webhookName) {
        this.webhookName = webhookName;
    }
}