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
import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { MatTableDataSource } from '@angular/material/table';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { ModelManagementService } from '@insights/app/modules/model-management/model-management.service';
import {Router,NavigationExtras } from '@angular/router';
import { DataSharedService } from '@insights/common/data-shared-service';
import { WorkflowHistoryDetailsDialog } from '@insights/app/modules/reportmanagement/workflow-history-details/workflow-history-details-dialog';

@Component({
  selector: 'app-model-management',
  templateUrl: './model-management.component.html',
  styleUrls: ['./model-management.component.css','../home.module.css']
})
export class ModelManagementComponent implements OnInit {

  toDelete: string = null;
  UsecaseListDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  displayedColumns = [];
  timeZone: string = '';
  usecaseList: any;
  dateObj: Date;
  updatedData = [];
  MAX_ROWS = 10;
  enableRetry: boolean = false;
  enableLeaderboard: boolean = false;
  enableDelete: boolean = false;
  selectedUsecase: any;
  table;
  tbody;
  columnDefs = [
    {headerName: 'Make', field: 'make' },
    {headerName: 'Model', field: 'model' },
    {headerName: 'Price', field: 'price'}
];

rowData = [
    { make: 'Toyota', model: 'Celica', price: 35000 },
    { make: 'Ford', model: 'Mondeo', price: 32000 },
    { make: 'Porsche', model: 'Boxter', price: 72000 }
];

  constructor(public messageDialog: MessageDialogService, public modelManagementService: ModelManagementService,private dataShare: DataSharedService,
    private changeDetectorRefs: ChangeDetectorRef,public router:Router, private dialog: MatDialog) { 
    this.displayedColumns = [ "radio", "UsecaseName", "PredictionColumn", "ModelName", "SplitRatio", "Created","Status","active", "details" ];
    this.getModelDetails();
    this.table = document.getElementsByTagName("table")[0];
    this.tbody = this.table.getElementsByTagName("tbody")[0];
// this.tbody.onclick = function (e) {
//     e = e || window.event;
//     let data = [];
//     let target = e.srcElement || e.target;
//     while (target && target.nodeName !== "TR") {
//         target = target.parentNode;
//     }
//     if (target) {
//         let cells = target.getElementsByTagName("td");
//         for (let i = 0; i < cells.length; i++) {
//             data.push(cells[i].innerHTML);
//         }
//     }
//     alert(data);
// };

  }

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), '');
    //doStuff
  }
  redirectToPrection(event){
    let data =event;
    let navigationExt: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
       // leaderboard: this.leaderboard,
        usecase: event.usecase,
        //headers: this.headers,
        //sratio: this.splitRatio,
       // target: this.target,
       // noOfModels: this.noOfModels, hideLeaderboardbtn: this.hideLeaderboardbtn, tableObject: JSON.stringify(this.tableForNav)
      }
    };
    this.router.navigate(['InSights/Home/prediction'], navigationExt);

  }
  ngAfterViewInit() {
    this.UsecaseListDatasource.paginator = this.paginator;
  }
  add(){
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
      }
      };
     this.router.navigate(['InSights/Home/mlwizard'], navigationExtras);
   //this.router.navigate(['InSights/Home/mlwizard']);
  }
  async getModelDetails() {
    this.updatedData = [];
    this.usecaseList = await this.modelManagementService.loadUsecaseDetails();
    console.log(this.usecaseList);
    
    if (this.usecaseList != null && this.usecaseList.status == 'success') {
      this.UsecaseListDatasource.data = this.usecaseList.data.usecases.sort((a, b) => a.usecaseName > b.usecaseName);
      var dataArray = this.UsecaseListDatasource.data;
      if(dataArray != undefined) {
        dataArray.forEach(key => {
          if(typeof key.createdAt !== undefined) {
            if (key.createdAt === 0) {
              key.createdAt = '-';
            } else {
              this.dateObj = new Date(key.createdAt);
              key.createdAt = this.dataShare.convertDateToSpecificDateFormat(this.dateObj, "yyyy-MM-dd");
            }
          }
          if(typeof key.updatedAt !== undefined) {
            if (key.updatedAt === 0) {
              key.updatedAt = '-';
            } else {
              this.dateObj = new Date(key['updatedAt']);
              key['updatedAt'] = this.dataShare.convertDateToSpecificDateFormat(this.dateObj, "yyyy-MM-dd");
            }
          }
          if(key.modelName == '') {
            key.modelName = '-';
          }
          this.updatedData.push(key);
        });
        this.UsecaseListDatasource.data = this.updatedData;
        this.UsecaseListDatasource.paginator = this.paginator;
        this.updatedData = [];
      }
      
    } 
    else if(this.usecaseList.StatusCode == '204') {
      this.UsecaseListDatasource.data = this.updatedData;
      this.UsecaseListDatasource.paginator = this.paginator;
      this.messageDialog.showApplicationsMessage("No Usecase found, Please click on <b>+</b> icon to add Usecase.", "ERROR");
    }  else {
      this.messageDialog.showApplicationsMessage("Something wrong with Service.Please try again.","ERROR");
  }
  }

  setUsecase(ev: string) {
    this.toDelete = ev;
    console.log(this.toDelete);
  }

  onDelete() {
    var dialog;
    const dialogRef = this.messageDialog.showConfirmationMessage("Delete","This action will delete the usecase <b>"+this.selectedUsecase.usecaseName+"</b> along with all related files (MOJOs, csv etc.) and cannot be reverted. Would you like to continue?",this.toDelete,"ALERT","40%");
    dialogRef.afterClosed().subscribe(result => {
      if(result == 'yes') {
        console.log("Delete the usecase: ", this.selectedUsecase.usecaseName);
        this.modelManagementService.deleteUsecase(this.selectedUsecase.usecaseName).subscribe(event => {
          console.log(event);
          if(event.status == 'success' && event.data.statusCode == 1 ){
            dialog = this.messageDialog.showApplicationsMessage("The usecase <b>"+this.selectedUsecase.usecaseName+"</b> and uploaded csv file both has been deleted succesfully.","SUCCESS");
            
          } else if(event.status == 'success' && event.data.statusCode == 0) {
            dialog = this.messageDialog.showApplicationsMessage("The usecase <b>"+this.selectedUsecase.usecaseName+"</b> has been deleted succesfully but failed to delete uploaded csv file","SUCCESS");
          } else if(event.status == 'failure' && event.StatusCode == 409 ) {
            this.messageDialog.showApplicationsMessage(event.message,"ERROR");
          }
          else {
           this.messageDialog.showApplicationsMessage(event.message+". Please try again","ERROR");
          }
          dialog.afterClosed().subscribe(result => {
            this.refresh();
          })
        })
      }
    })
  }

  refresh() {
    this.getModelDetails();
    this.enableDelete = false;
    this.selectedUsecase = '';
  }

  onSelect(selected) {
    this.enableDelete = true;
    if(selected.status == 'LEADERBOARD_READY') {
      this.enableLeaderboard = true;
    } else {
      this.enableLeaderboard = false;
    }
  }

  navigateToLeaderboard() {
    let navigationExt: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        usecase: this.selectedUsecase.usecaseName,
        targetColumn: this.selectedUsecase.predictionColumn
      }
    };
    this.router.navigate(['InSights/Home/prediction'], navigationExt);
  }

  showWorkflowHistoryDetailsDialog(usecaseName: String, workflowId: String) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(WorkflowHistoryDetailsDialog, {
        panelClass: 'workflow-history-details-dialog-container',
        disableClose: true,
        data: {
          reportName: usecaseName,
          workflowId: workflowId,
          timeZone: this.timeZone
        }
      });
    }
  }

  updateUsecaseState(event: MatSlideToggleChange, name, element) {
    var self = this;
    var dialog;
    var statusUpdateJson = {};
    var title = 'Update Active/Inactive State';
    var state = element.isActive ? 'Active' : 'Inactive';
    var dialogmessage =
      'Are you sure you want to make <b>' + element.usecaseName + '</b> state to <b>' + state + '</b> ?';
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title, dialogmessage, element.usecaseName, 'ALERT', '40%');
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        statusUpdateJson["usecaseName"] = name;
        statusUpdateJson["isActive"] = event.checked;
        console.log(statusUpdateJson);
        self.modelManagementService.usecaseStateUpdate(JSON.stringify(statusUpdateJson))
          .then(function (response) {
            if (response.status == 'success') {
              dialog = self.messageDialog.showApplicationsMessage('Status updated successfully.', 'SUCCESS');
            } else if (response.status == 'failure' && 
              response.message == "Usecase does not exists in database.") {
              dialog = self.messageDialog.showApplicationsMessage("Usecase <b>" + name + "</b> does not exists in database.", 'ERROR');
            } else if (response.status == 'failure' && 
              response.message == "Usecase cannot be deactivated as it is attached to kpi.") {
              dialog = self.messageDialog.showApplicationsMessage(response.message , 'ERROR');
            } else {
              dialog = self.messageDialog.showApplicationsMessage(
                'Failed to update state. Please check logs for more details.', 'ERROR');
            }
            dialog.afterClosed().subscribe(result => {
            self.refresh();
          })
          })
      } else {
        element.isActive = !event.checked;
      }
    });
  }
  

}

