/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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

 import { Component, OnInit, Inject, ViewChild } from '@angular/core';
 import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
 import { MatPaginator } from '@angular/material/paginator';
 import { MatTableDataSource } from '@angular/material/table';
 
 @Component({
     selector: 'milestone-dialog',
     templateUrl: './milestone-dialog.html',
     styleUrls: ['./milestone-dialog.css']
 
 })
 export class MileStoneDialog implements OnInit {
     listOfOutcomes = new MatTableDataSource<any>();
     displayedColumns: string[];
     showPagination: boolean = true;
     @ViewChild(MatPaginator) paginator: MatPaginator;
     milestoneName:any;

     constructor(public dialogRef: MatDialogRef<MileStoneDialog>,
         @Inject(MAT_DIALOG_DATA) public data: any) {
         console.log("data id", data)
         this.milestoneName = this.data.outcomeList.mileStoneName
         this.displayedColumns = ['outcomeName', 'status', 'lastUpdatedDate', 'statusMessage'  ];
         this.listOfOutcomes.paginator = this.paginator;
         this.listOfOutcomes.data = this.data.outcomeList.listOfOutcomes
     }
 
     ngOnInit() {
     }
 
     ngAfterViewInit() {
         this.listOfOutcomes.paginator = this.paginator;
     }
     
     closeShowDetailsDialog(): void {
         this.dialogRef.close();
     }
 
 }
 