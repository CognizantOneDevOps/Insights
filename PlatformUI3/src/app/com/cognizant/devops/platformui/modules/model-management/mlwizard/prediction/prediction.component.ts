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
import { MatRadioChange, MatTableDataSource, MatPaginator, MatDialog  } from '@angular/material';
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

  
  // leaders = ["DRF_1_AutoML_20200701_163135", "GLM_1_AutoML_20200701_163135"];
  // mse = [ 56.750536916922705, 61.727453260662394].map(String);
  // rmse = [ 7.533295223003192, 7.85668207710242].map(String);
  // mean_residual_deviance = [ 56.750536916922705, 61.727453260662394 ].map(String);
  // rmsle = [ 0.6783952366191675, "NaN" ].map(String);
  // mae =[ 3.5014141209008263, 3.793888105971443 ].map(String);
  // resultData = [{"Date":"2019-06-12 05:30:00","AuthorName":"Karthikeyan Mohan","Experience":12.0,"RepoName":1.0,"Commits":4.0,"predict":"3.293"},
  // {"Date":"2020-02-17 05:30:00","AuthorName":"madhubalakrishnan","Experience":7.0,"RepoName":4.0,"Commits":1.0,"predict":"1.957"}];
  // fields = ["Date","AuthorName","Experience","RepoName","Commits","predict"];
  // leaders = [];
  // mse = [];
  // rmse = [];
  // mean_residual_deviance = [];
  // rmsle = [];
  // mae =[];
  targetML: string = null;
  showOutput: boolean = false;
  usecaseid: string = null;
  targetColumn: string = null;
  resultData = [];
  fields = [];
  p: object;
  pageObj: object = null;
  //leaderboard: [];
  selectedModel: any;
  LeaderboardDataSrc = new MatTableDataSource<any>();
  displayedColumns = [ "radio", "model_id", "mean_residual_deviance", "rmse", "mse", "mae", "rmsle" ];
  enableSaveMOJO: boolean = false;
  enablePredict: boolean = false;
  showThrobber: boolean = false;
  infoURL = "https://h2o-release.s3.amazonaws.com/h2o/rel-xu/5/docs-website/h2o-docs/performance-and-prediction.html";
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private router: Router, private route: ActivatedRoute, public messageDialog: MessageDialogService,
    private mlwizardService: MLWizardService, private dataShare: DataSharedService, private dialog: MatDialog) { 
     // this.getLeaderBoard();
    }

  // ngOnInit() {
  //   this.route.queryParams.subscribe(params => {
  //     console.log(params);
  //     if(params.leaderboard){
  //     let leaderbrd = JSON.parse(params.leaderboard);
  //     this.pageObj = { leaderboard: leaderbrd,
  //       headers: params.headers,
  //       usecaseid: params.usecase,
  //       sratio: params.sratio,
  //       target: params.target,
  //       noOfModels: params.noOfModels,
  //       hideLeaderboardbtn: params.hideLeaderboardbtn, tableObject: params.tableObject }
  //     this.usecaseid = params.usecase;
  //     //get the leader algorithm names from leaderboard object & assign to leaders array
  //     this.leaders = leaderbrd.model_id;
  //     this.mse = leaderbrd.mse.map(String);
  //     this.rmse = leaderbrd.rmse.map(String);
  //     this.mae = leaderbrd.mae.map(String);
  //     this.mean_residual_deviance = leaderbrd.mean_residual_deviance.map(String);
  //     this.rmsle = leaderbrd.rmsle.map(String);
  //   }
  //     else{
  //       this.route.queryParams.subscribe(params => {
  //         this.usecaseid = params.usecase;
  //       });
  //     }
  //     //this.LeaderboardDataSrc.data = leaderbrd;
  //   })
  // }
  
  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      console.log(params);
      this.usecaseid = params.usecase;
      this.targetColumn = params.targetColumn;
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
    //this.targetML = ml.value;
    this.enablePredict = true;
    this.enableSaveMOJO = true;

  }

  // onPrediction() {
  //   console.log(this.selectedModel.model_id);
      
  //    this.mlwizardService.getPredictions(this.usecaseid, this.selectedModel.model_id).then( event => {
  //     //this.mlwizardService.getPredictions("test", "GLM_1_AutoML_20200707_151411").then( event => {
  //       if(event.status == "success"){
  //         console.log(event);
  //         this.resultData = event.data.Data;
  //         this.fields = event.data.Fields;
  //         this.fields.sort();
  //         this.showOutput = true;
          
  //       }else if (event.status == "failure") {
  //         //handle failure
  //       }
  //     });
    
  // }

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
