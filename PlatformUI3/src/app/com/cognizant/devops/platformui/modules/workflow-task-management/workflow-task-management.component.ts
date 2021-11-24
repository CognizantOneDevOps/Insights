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
import { Component, OnInit, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { DataSharedService } from '@insights/common/data-shared-service';
import { MatPaginator } from '@angular/material/paginator';
import { MatRadioChange } from '@angular/material/radio';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { NavigationExtras, Router } from '@angular/router';
import { WorkflowTaskManagementService } from './workflow-task-management.service';

@Component({
  selector: 'app-workflow-task-management',
  templateUrl: './workflow-task-management.component.html',
  styleUrls: ['./workflow-task-management.component.css', './../home.module.css']
})
export class WorkflowTaskManagementComponent implements OnInit {
  selectedWorkflow: any;
  showPagination: boolean = true;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  displayedColumns: string[];
  workflowDataSourceFromService: any;
  workflowDataSource = new MatTableDataSource<any>();
  disableButton: boolean = true;
  taskId: number;
  taskParameter = {};
  selectedIndex = -1;

  constructor(
    private workflowService: WorkflowTaskManagementService,
    public dataShare: DataSharedService,
    private messageDialog: MessageDialogService,
    public router: Router,
    public datePipe: DatePipe
  ) {
    this.displayedColumns = [
      'select',
      'TaskName',
      'TaskClassDetail',
      'QueueName',
      'WorkflowType'
    ];
    this.workflowDataSource.paginator = this.paginator;
    this.loadWorkflowTask();
  }

  ngOnInit(): void {

  }
  async loadWorkflowTask() {
    let workflowDataSourceFromService = await this.workflowService.getWorkFlowTask();
    this.workflowDataSource.data = workflowDataSourceFromService.data;
    this.workflowDataSource.paginator = this.paginator;
  }
  addTask() {
    this.taskParameter = JSON.stringify({ 'type': 'Add' });
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "taskParameter": this.taskParameter
      }
    };
    this.router.navigate(['InSights/Home/workflow-configuration'], navigationExtras);
  }
  async editTask() {
    this.taskParameter = JSON.stringify({ 'type': 'update', 'detailedArr': this.selectedWorkflow });
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "taskParameter": this.taskParameter
      }
    }
    this.router.navigate(['InSights/Home/workflow-configuration'], navigationExtras);
  }
  radioChange(event: MatRadioChange, index) {
    this.selectedIndex = index;
    this.disableButton = false;
  }
 
  deleteTask() {
    var self = this;
    var title = 'Delete Report';
    var dialogmessage =
      'Do you want to delete <b>' +
      self.selectedWorkflow.description +
      '</b> ? <br> <b> Please note: </b> The action of deleting ' +
      '<b>' +
      self.selectedWorkflow.description +
      '</b> cannot be undone. Do you want to continue ? ';
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedWorkflow.description,
      'ALERT',
      '45%'
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        self.workflowService.deleteWorkflow(self.selectedWorkflow.taskId)
          .then(function (data) {
            if (data.status == 'success') {
              self.messageDialog.showApplicationsMessage(
                '<b>' +
                self.selectedWorkflow.description +
                '</b> deleted successfully.',
                'SUCCESS'
              );
              self.refresh();
            } else if (data.message == 'The workflow task in use') {
              self.messageDialog.showApplicationsMessage(
                'The workflow task cannot be deleted since it is in use.',
                'ERROR'
              );
            } else {
              self.messageDialog.showApplicationsMessage(
                'Failed to Delete the task. Please check logs.',
                'ERROR'
              );
            }
          })
          .catch(function (data) {
            self.messageDialog.showApplicationsMessage(
              'Failed to Delete the task. Please check logs.','ERROR');
           });
      }
    });
  }

  refresh() {
    this.selectedWorkflow = '';
    if (this.selectedIndex >= 0) {
      this.disableButton = true;
    }
    this.loadWorkflowTask();
  }
}
