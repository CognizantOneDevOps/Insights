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
import { ShowTraceabiltyDetailsDialog } from '../traceability/traceabilty-show-details-dialog';



@Component({
    selector: 'show-correlationjson',
    templateUrl: './show-correlationjson.html',
    styleUrls: ['./relationship-builder.component.css']
})

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
    corelation: any;
    sample: any
    title: any
    destination: string;
    source: string;
    relationName: string;
    detailProp: string;
    flag: boolean;
    relationshipProp: string
    destprop: string
    sourceprop: string
    showModel = null;
    destText: boolean;
    sourceText: boolean;
    destformattedString: string;
    sourceformattedString: string


    constructor(private dialog: MatDialog, private relationshipBuilderService: RelationshipBuilderService, public dialogRef: MatDialogRef<ShowJsonDialog>,
        @Inject(MAT_DIALOG_DATA) public data: any) {
        // this.sample = JSON.stringify((data.message), null, '\t')//data.data;// JSON.parse(data.data);  JSON.stringify(data.data, null, 4)
        this.title = data.title
        this.corelation = data.message;
        this.destination = this.corelation.destination;
        this.source = this.corelation.source;
        this.relationName = this.corelation.relationName;
        this.flag = this.corelation.flag;
        this.relationshipProp = this.corelation.relationshipProp
        this.sourceprop = this.corelation.sourceprop
        this.destprop = this.corelation.destprop;
        this.destText = false;
        this.sourceText = false;
        if (this.destprop != null)  {
            this.destformattedString = this.destprop.split(",").join("\n");
        }
        if (this.sourceprop != null) {            
            this.sourceformattedString = this.sourceprop.split(",").join("\n")
        }       

    }


    ngOnInit() {

    }

    closeShowDetailsDialog(): void {
        this.dialogRef.close();
    }


}

