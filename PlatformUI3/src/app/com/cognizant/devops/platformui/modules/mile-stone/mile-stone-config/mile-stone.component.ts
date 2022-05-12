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

 
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, ParamMap, NavigationExtras } from '@angular/router';
import { FormControl, Validators, FormGroup, FormBuilder, AbstractControl } from '@angular/forms';
import { MessageDialogService } from '../../application-dialog/message-dialog-service';
import { DataSharedService } from '@insights/common/data-shared-service';
import { MileStoneService } from '@insights/app/modules/mile-stone/mile-stone.service';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { OutcomeService } from '../../outcome/outcome.service';
import { MatDialog } from '@angular/material/dialog';
import { OutComeDialogComponent } from '../../outcome/outcome-dialog/outcome-dialog.component';

@Component({
    selector: 'app-mile-stone',
    templateUrl: './mile-stone.component.html',
    styleUrls: ['./mile-stone.component.css', './../../home.module.css']
})
export class MileStoneComponent implements OnInit{

    isProgress: boolean = false;
    mileStoneForm: FormGroup;
    today = new Date();
    start: any;
    end: any;
    outcomeArray = [];

    constructor(private router: Router,
        private messageDialog: MessageDialogService, 
        private formBuilder: FormBuilder,
        private mileStoneService: MileStoneService,
        private dataShare: DataSharedService,
        private outcomeService: OutcomeService, private dialog: MatDialog) {
    }

    ngOnInit(){
        this.mileStoneForm = this.formBuilder.group({
            mileStoneName: ['', [Validators.required]],
            milestoneReleaseID: ['', [Validators.required]],
            startDate: ['', [Validators.required]],
            endDate: ['', [Validators.required]],
            outcomeList: [[],[Validators.required]]
        });


        //this.fetchOutcomeList();
    }

    async fetchOutcomeList(){
        let response = await this.mileStoneService.fetchOutcomeConfig();
        if (response.status === "success") {
            response.data.forEach(element => {
                if(element.isActive) {
                    this.outcomeArray.push(element);
                }
            });
        }else{
            this.messageDialog.showApplicationsMessage("Issue in Outcome Configuration", "ERROR");
        }
    }

    getStartDate(event: MatDatepickerInputEvent<Date>) {
        this.start = this.dataShare.convertDateToSpecificDateFormat(new Date(event.value), "yyyy-MM-dd'T'HH:mm:ss'Z'");
      }
    
      getEndDate(event: MatDatepickerInputEvent<Date>) {
        this.end = this.dataShare.convertDateToSpecificDateFormat(new Date(event.value), "yyyy-MM-dd'T'HH:mm:ss'Z'");
      }

    onSubmit() {
        console.log(this.mileStoneForm.value);

        if (this.mileStoneForm.valid) {
            this.isProgress = true;
            //this.mileStoneForm.controls['startDate'].setValue(this.start);
            //this.mileStoneForm.controls['endDate'].setValue(this.end);
            let requestObj = {};
            requestObj = this.mileStoneForm.value;
            requestObj['startDate'] = this.start;
            requestObj['endDate'] = this.end;
            var self = this;
            var dialogmessage = " Do you want to save the changes? "
            var title = "Save MileStone Configuration ";
            const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
            dialogRef.afterClosed().subscribe(result => {
                if (result == 'yes') {
                    this.mileStoneService.saveMileStoneConfig(requestObj)
                        .then(function (response) {
                            console.log(response);
                            if (response.status === "success") {
                                self.messageDialog.showApplicationsMessage("Saved Successfully", "SUCCESS");
                                self.router.navigateByUrl('InSights/Home/fetchMileStone', { skipLocationChange: true });
                            }else{
                                self.messageDialog.showApplicationsMessage(response.message, "ERROR");
                            }
                        })
                }
            })
            this.isProgress = false;
        }
        else{
            this.messageDialog.showApplicationsMessage("Please fill mandatory fields", "ERROR");
        }
    }

    openOutcomeDialog() {
        var dialogRef = this.dialog.open(OutComeDialogComponent, {
          panelClass: 'showjson-dialog-container',
          height: "500px",
          width: "550px",
          disableClose: true,
    
        });

        dialogRef.afterClosed().subscribe(result => {
            this.outcomeArray = []
            this.outcomeService.setOutcomeSubject.subscribe(res => {
                if(res && res.length > 0) {
                    for (var ele of res) {
                        this.outcomeArray.push(ele.outcomeName)
                    }
                } 
                console.log(this.outcomeArray)
                this.mileStoneForm.controls['outcomeList'].setValue(this.outcomeArray);
            })
        });

    }

    reset(){
        this.mileStoneForm.reset();
    }

    redirect(){
        this.router.navigate(['InSights/Home/fetchMileStone'], { skipLocationChange: true });
    }
}