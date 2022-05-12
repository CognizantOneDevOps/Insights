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
import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MessageDialogService } from '../../application-dialog/message-dialog-service';
import { OutcomeService } from '../outcome.service';
import { SelectionModel } from '@angular/cdk/collections';

@Component({
  selector: 'app-outcomet-dialog',
  templateUrl: './outcome-dialog.component.html',
  styleUrls: ['./outcome-dialog.component.css', './../../home.module.css']
})
export class OutComeDialogComponent implements OnInit {
  outcomeList;
  displayedColumns: string[];
  outcomeDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  selectedOutcome: any;
  selection = new SelectionModel<[]>(true, []);

  constructor( private outcomeService: OutcomeService, public dialogRef: MatDialogRef<OutComeDialogComponent>,
              private messageDialog: MessageDialogService) {
    this.displayedColumns = ['radio', 'id', 'outcomeName', 'outcomeType'];

  }
  ngOnInit() {
    this.getAllActiveOutcome();
  }
  ngAfterViewInit() {
    this.outcomeDatasource.paginator = this.paginator;

  }

  selectHandler(element: any) {
    this.selection.toggle(element);
  }

  applyFilter(filterValue: string) {
    this.outcomeDatasource.filter = filterValue.trim();
  }
  setOutcomeValue() {
    this.outcomeService.setOutcomeSubject.next(this.selection.selected);
  }
  onOkClick() {
    //let data = this.selectedOutcome;
    this.setOutcomeValue();
    this.dialogRef.close();
  }

  public async getAllActiveOutcome() {
    var self = this;
    this.outcomeList = [];
    this.outcomeList = await this.outcomeService.loadOutcomeList();
    if(this.outcomeList.data.length > 0){
    this.outcomeList.data.forEach(outcome => {
      let outcomeIdArr = [];
      outcomeIdArr.push(outcome.id);
    });

    if (this.outcomeList != null && this.outcomeList.status == "success") {
      this.outcomeDatasource.data = this.outcomeList.data.sort(
        (a, b) => a.id > b.id
      );

      this.outcomeDatasource.paginator = this.paginator;
    }
  }else{
    this.messageDialog.showConfirmationMessage("Outcome is Empty", "Please configure Outcome!", "", "ALERT", "40%");
    this.dialogRef.close();
  }
  }
  closeShowDetailsDialog(): void {
    this.outcomeService.setOutcomeSubject.next([]);
    this.dialogRef.close();
  }

}
