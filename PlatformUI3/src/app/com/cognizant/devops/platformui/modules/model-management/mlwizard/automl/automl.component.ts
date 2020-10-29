/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
import { Component, OnInit } from '@angular/core';
import { Router, NavigationExtras, ActivatedRoute } from '@angular/router';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MLWizardService } from '@insights/app/modules/model-management/mlwizard/mlwizard.service';

@Component({
  selector: 'app-automl',
  templateUrl: './automl.component.html',
  styleUrls: ['../mlwizard.component.css','../../../home.module.css']
})
export class AutomlComponent implements OnInit {
  headers = [];
  userData: Object;
  usecaseid:string;
  splitRatio: number = null;
  target: string = null;
  hideProgressBar: boolean;
  progress: number = 0;
  hideLeaderboardbtn: boolean = true;
  leaderboard: string = null;
  poll_id: any = null;
  noOfModels: number = null;
  tableForNav: object = null;
  hideNav: boolean = true;

  constructor(private router: Router, private route: ActivatedRoute, public messageDialog: MessageDialogService,
    private mlwizardService: MLWizardService) { }

  ngOnInit() {
    // this.mlwizardService.sendHeaders.subscribe(data => {
    //   //if (data) {this
    //   this.headers=[];
    //     data.forEach(col => {
    //       //if (this.headers.indexOf(col.FieldName) === -1) {
    //         this.headers.push(col.FieldName);
    //      // }
    //     });
    //   //}
    // })
    // this.route.queryParams.subscribe(params => {
    //   console.log(params);
    //   this.headers = params.headers;
    //   this.usecaseid = params.usecaseid;
    //   //this.userData = params.userinput;
    //   this.tableForNav = JSON.parse(params.tableObject);
    //   this.splitRatio = params.sratio;
    //   this.target = params.target
    //   this.hideLeaderboardbtn = params.hideLeaderboardbtn == "true";
    //   console.log(this.hideLeaderboardbtn);
      
    //   this.noOfModels = params.noOfModels;
    // })
    this.hideProgressBar = true;
    
  }

  getHeaders() {
    this.mlwizardService.sendHeaders.subscribe(data => {
      data.forEach(col => {
        if (this.headers.indexOf(col.FieldName) === -1) {
        this.headers.push(col.FieldName);
        }
      });
    });
  }

  setValue(inputVal: string, inputType: string) {
    console.log(inputVal);
    if(inputType=='ratio'){
      this.splitRatio = Number(inputVal);
      if(isNaN(this.splitRatio))
        this.messageDialog.showApplicationsMessage("Please provide a valid integer between 10 to 90 as split ratio", "ERROR");
    }
    else if(inputType=='target')
      this.target = inputVal;
    else
      this.noOfModels = Number(inputVal);
  }

  sendToAutoML() {
    console.log("Inside sendToAutoML()");
    this.hideLeaderboardbtn = true;
    if(this.splitRatio==null)
      this.messageDialog.showApplicationsMessage("Please provide a valid integer between 10 to 90 as split ratio", "ERROR");
    else if(this.target==null)
      this.messageDialog.showApplicationsMessage("Please select target column for the ML", "ERROR");
    else if(this.noOfModels==null)
      this.messageDialog.showApplicationsMessage("Please select Max number of models for the AutoML", "ERROR");
    else{
      this.hideProgressBar = false;
      this.mlwizardService.splitNtrain(this.usecaseid, this.splitRatio, this.target, this.noOfModels).subscribe( event => {
        console.log(event);
        if (event.status == "success") {
          this.poll_id = setInterval( ()=> {this.pollStat( event.data.PollingUrl, this.usecaseid)}, 7000);
          
          
        } else if (event.status == "failure") {
          this.messageDialog.showApplicationsMessage("AutoML failed, Please try again :  " + event.message, "ERROR");
          this.hideProgressBar = true;
        }
      })
    }
  }

  pollStat(url: string, usecase: string) {
    this.mlwizardService.pollAutoMLStat(usecase, url).then( ev => {
      if(ev.status == "success"){
        console.log(ev);
        if(ev.data.Status == "DONE"){
          clearInterval(this.poll_id);
          this.messageDialog.showApplicationsMessage("<b>AutoML Completed.", "SUCCESS");
          this.hideLeaderboardbtn = false;
          this.hideProgressBar = true;
        }else
          this.progress = ev.data.Progress;
      }else if (ev.status == "failure") {
        //handle failure
      }
    })
  }

  getLeaderBoard() {
    this.mlwizardService.getLeaderboard(this.usecaseid).then( event => {
      if(event.status == "success"){
        console.log(event);
        this.leaderboard = JSON.stringify(event.data.Leaderboard);
        
        this.nextPage();
      }else if (event.status == "failure") {
        //handle failure
      }
    })
  }

  nextPage() {
    let navigationExt: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        leaderboard: this.leaderboard,
        usecase: this.usecaseid,
        headers: this.headers,
        sratio: this.splitRatio,
        target: this.target,
        noOfModels: this.noOfModels, hideLeaderboardbtn: this.hideLeaderboardbtn, tableObject: JSON.stringify(this.tableForNav)
      }
    };
    this.router.navigate(['InSights/Home/prediction'], navigationExt);
  }
  navigateToPrevious() {
    let secondPageData = { }
    let navigationExt: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        //file: this.userData,
        usecaseid: this.usecaseid,
        headers: this.headers,
        isSmallTableVisible: true,
        tableObject: JSON.stringify(this.tableForNav),
        sratio: this.splitRatio,
        target: this.target,
        noOfModels: this.noOfModels, hideLeaderboardbtn: this.hideLeaderboardbtn
      }
    };
    this.router.navigate(['InSights/Home/mlwizard'], navigationExt);
  }

  

}
