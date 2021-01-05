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
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router, NavigationExtras, ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatRadioChange } from '@angular/material/radio';
import { MatTableDataSource } from '@angular/material/table';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MLWizardService } from '@insights/app/modules/model-management/mlwizard/mlwizard.service';
import { DataSharedService } from '@insights/common/data-shared-service';
import { PredictionShowDetailsDialog } from './prediction-show-detail/prediction-show-details-dialog';

@Component({
  selector: 'app-prediction',
  templateUrl: './prediction.component.html',
  styleUrls: ['./prediction.component.css','../mlwizard.component.css','../../../home.module.css']
})
export class PredictionComponent implements OnInit {

  targetML: string = null;
  showOutput: boolean = false;
  usecaseid: string = null;
  predictionType :string =null;
  targetColumn: string = null;
  resultData = [];
  fields = [];
  p: object;
  pageObj: object = null; 
  selectedModel: any;
  LeaderboardDataSrc = new MatTableDataSource<any>();
  displayedColumnsForRegression = [ "radio", "model_id", "mrd", "rmse", "mse", "mae", "rmsle" ];
  displayedColumnsForClassification=[ "radio", "model_id","auc","logloss","mpce", "rmse", "mse"];
  enableSaveMOJO: boolean = false;
  enablePredict: boolean = false;
  showThrobber: boolean = false;
  infoURL = "https://h2o-release.s3.amazonaws.com/h2o/rel-xu/5/docs-website/h2o-docs/performance-and-prediction.html";
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private router: Router, private route: ActivatedRoute, public messageDialog: MessageDialogService,
    private mlwizardService: MLWizardService, private dataShare: DataSharedService, private dialog: MatDialog) { 
   
    } 
  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      console.log(params);
      this.usecaseid = params.usecase;
      this.targetColumn = params.targetColumn;
      this.predictionType=params.predictionType;
    });
    this.getLeaderBoard();
  }


  getLeaderBoard() {
    var leaderboardData = [];
    this.mlwizardService.getLeaderboard(this.usecaseid).then( event => {
      if(event.status == "success"){
        console.log(event);
        this.LeaderboardDataSrc.data = event.data;       
        this.LeaderboardDataSrc.paginator = this.paginator;
      } else if(event.status == "failure") {
        const dialog = this.messageDialog.showApplicationsMessage("Unable to fetch leaderboard data. You may need to re-run the usecase <b>"+this.usecaseid+"</b>.","ERROR");
        dialog.afterClosed().subscribe(result => {
          this.navigateToPrevious();
        })
      } else {
        this.messageDialog.showApplicationsMessage("Something wrong with Service.Please try again.","ERROR");
      }
      
    })
    

  }

  selectML(selected) { 
    this.enablePredict = true;
    this.enableSaveMOJO = true;

  }



  onPrediction() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(PredictionShowDetailsDialog, {
        panelClass: 'prediction-details-dialog-container',
        disableClose: true,
        data: {
          usecase: this.usecaseid,
          model_id: this.selectedModel.model_id,
          targetColumn: this.targetColumn
        }
      });
    }
  }

  onSaveMOJO() {
    const dialogRef = this.messageDialog.showConfirmationMessage("Save ","Do you want to download MOJO <b>" +this.selectedModel.model_id+"</b> ? <br/>"
    +" <b>Please Note:</b> This will be final MOJO to be deployed and it cannot be altered later. ",this.selectedModel.model_id,"ALERT","40%");
    dialogRef.afterClosed().subscribe(result => {
      if(result == 'yes') {
        this.showThrobber = true;
        this.mlwizardService.saveMOJO(this.usecaseid, this.selectedModel.model_id).then( event => {
          if(event.status == "success"){
            this.showThrobber = false;
            console.log(event);
            const dialog = this.messageDialog.showApplicationsMessage(event.data.Message , "SUCCESS");
            console.log(dialog);
            dialog.afterClosed().subscribe(result => {
              console.log(result);
              this.navigateToPrevious();
            })
          }else if (event.status == "failure") {
            this.showThrobber = false;
            this.messageDialog.showApplicationsMessage("Couldn't download the MOJO. Please try again.", "ERROR");
          }
         });
      } 
    })
     
     
    
  }

  onInfoClick() {
    window.open(this.infoURL, "_blank");
  }

  isMLSelected() {
    if(this.targetML == null) {
     this.messageDialog.showApplicationsMessage("Please choose an algorithm name to continue", "ERROR");
     return false;
    }
    else
     return true;
  }

  getColor(headerstr: string ) {
    if(headerstr == this.pageObj['target'] || headerstr == 'predict')
      return "#FF8F1C";
  }

  navigateToPrevious() {
    let navigationExt: NavigationExtras = {
      skipLocationChange: true,
      queryParams: this.pageObj
    };
    this.router.navigate(['InSights/Home/modelmanagement'], navigationExt);
  }

}
