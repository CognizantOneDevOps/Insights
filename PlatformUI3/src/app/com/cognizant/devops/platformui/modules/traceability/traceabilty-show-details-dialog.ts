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
 import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
 import { MatPaginator } from '@angular/material/paginator';
 import { MatTableDataSource } from '@angular/material/table';
 import { TraceabiltyService } from './traceablity-builder.service';
 import { DatePipe } from '@angular/common';
 import { DataSharedService } from '@insights/common/data-shared-service';
 
 export interface TimelagElement {
     toolname: string;
     average: string;
 }
 @Component({
     selector: 'traceabilty-show-details-dialog',
     templateUrl: './traceablilty-show-details-dialog.html',
     styleUrls: ['./traceabilty-show-details-dialog.css']
 })
 
 
 export class ShowTraceabiltyDetailsDialog implements OnInit {
     showContent: boolean;
     MAX_ROWS_PER_TABLE = 10;
     showThrobber: boolean = false;
     checkResponseData: boolean;
     pathName: string;
     detailType: string;
     columnLength: number;
     resultsLength: number = 6;
     agentDetailedNode = [];
     agentDetailedDatasource = new MatTableDataSource([]);
     @ViewChild(MatPaginator) paginator: MatPaginator;
     agentFailureDetailsDatasource = new MatTableDataSource([]);
     agentFailureRecords = [];
     headerArrayDisplay = [];
     key1 = [];
     key = []
     dispplaytoolname: string;
     value = []
     masterHeader = new Map<String, String>();
     finalHeaderToShow = new Map<String, String>();
     displayedColumns: string[] = ['inSightsTimeX', 'message'];
     headerSet = new Set();
     showAgentFailureTab: boolean = false;
     timeZone: string = "";
     showToolDetailProp: boolean = true;
     showCardDetail:boolean=false;
     showSearchBox: boolean = false;
     timelagArray = [];
     columnsToDisplay: string[] = ['Tools', 'Handover Time'];
     timelagDataSource = new MatTableDataSource([]);
     formatedData: string;
     expandedElement: any;
     cardData: any;
     cardKeys: any;
     expandObjects: any[];
 
 
     constructor(public dialogRef: MatDialogRef<ShowTraceabiltyDetailsDialog>,
         @Inject(MAT_DIALOG_DATA) public data: any,
         private traceabiltyService: TraceabiltyService, public datePipe: DatePipe,
         public dataShare: DataSharedService) {
     }
 
     ngOnInit() {
         this.showToolDetailProp = this.data.showToolDetail;
         this.showCardDetail=this.data.showCardDetail;
         this.showSearchBox = this.data.showSearch;
         if(this.showCardDetail){
             this.getCardDetails();
         }
         if (this.showToolDetailProp) {
             this.gettooldetails();
             this.agentDetailedDatasource.paginator = this.paginator
         }
         else if (!this.showToolDetailProp) {
             this.getTimelagdetails();
         }
         this.timeZone = this.dataShare.getTimeZone()
     }
 
     getTimelagdetails() {
         this.timelagArray["average"] = this.data.dataArr
         this.timelagDataSource = this.data.dataArr
     }
     getCardDetails(){
             this.formatedData='';
             this.cardData=this.data.cardData;
             const notallowed = ['uuid', 'count','order','moddate'];           
            this.cardKeys = Object.keys(this.cardData).filter(key => !notallowed.includes(key));           
           
     }
     gettooldetails() {
         for (var x of this.data.cachestring) {
                    
             var obj = x;
 
             for (let key in x) {
 
                 if (key == 'uuid' || key == 'count' || key == 'toolName') {
                     if (key == 'toolName')
                         this.dispplaytoolname = obj[key];
                     continue;
                 }
                 if (typeof obj["inSightsTimeX"] !== "undefined") {
 
                     obj["inSightsTimeX"] = this.datePipe.transform(obj["inSightsTimeX"], 'yyyy-MM-dd HH:mm:ss');
                 }
                 this.finalHeaderToShow.set(key, obj[key]);
             }
             this.key.push(x)
         }
         for (var x of this.data.cachestring) {
             for (let key in x) {
                 if (key == 'uuid' || key == 'count' || key == 'toolName' || key == 'order') {
                     continue
                 }
                 this.key1.push(key);
             }
             break;
         }
                 this.headerArrayDisplay = this.key1;
                 this.agentDetailedDatasource.data = this.key;
                 this.agentDetailedDatasource.paginator = this.paginator;
     }
 
     ngAfterViewInit() {
         this.agentDetailedDatasource.paginator = this.paginator;
 
     }
 
     applyFilter(filterValue: string) {
         this.agentDetailedDatasource.filter = filterValue.trim();
       }
 
     showSelectedField(): void {
         //Define sequence of headerSet according to mater array and remove unwanted header 
         this.masterHeader.forEach((value: string, key: string) => {
             if (this.headerSet.has(key)) {
                 this.finalHeaderToShow.set(key, value);
             }
 
         });
         this.columnLength = this.finalHeaderToShow.size;
         // create headerArrayDisplay from map keys 
         if (this.finalHeaderToShow.size > 0) {
             this.headerArrayDisplay = Array.from(this.finalHeaderToShow.keys());
         }
     }
 
     closeShowDetailsDialog(): void {
         this.dialogRef.close();
     }
 }
 