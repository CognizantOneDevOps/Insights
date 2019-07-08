/*********************************************************************************
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
 *******************************************************************************/

import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatPaginator, MatTableDataSource } from '@angular/material';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { RelationshipBuilderService } from './relationship-builder.service';



@Component({
    selector: 'show-correlationjson',
    templateUrl: './show-correlationjson.html',
    styleUrls: ['./relationship-builder.component.css']
})


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
export class ShowJsonDialog implements OnInit {
    showContent: boolean;
    showThrobber: boolean = false;
    checkResponseData: boolean;
    pathName: string;
    detailType: string;
    columnLength: number;
    resultsLength: number = 6;
    agentDetailedNode = [];
    agentDetailedDatasource = new MatTableDataSource([]);
    headerArrayDisplay = [];
    masterHeader = new Map<String, String>();
    finalHeaderToShow = new Map<String, String>();
    @ViewChild(MatPaginator) paginator: MatPaginator;
    headerSet = new Set();
    corelationResponse: any;
    sample: any
    title1: any
    constructor(private relationshipBuilderService: RelationshipBuilderService, public dialogRef: MatDialogRef<ShowJsonDialog>,
        @Inject(MAT_DIALOG_DATA) public data: any) {
        this.sample = JSON.stringify((data.message), null, '\t')//data.data;// JSON.parse(data.data);  JSON.stringify(data.data, null, 4)
        this.title1 = data.title
    }

    ngOnInit() {

    }

    closeShowDetailsDialog(): void {
        this.dialogRef.close();
    }


}

