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

import {
  AfterContentChecked,
  AfterContentInit,
  AfterViewChecked,
  AfterViewInit,
  Component,
  OnInit,
} from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialog } from "@angular/material/dialog";
import { ActivatedRoute, Router } from "@angular/router";
import { DataSharedService } from "@insights/common/data-shared-service";
import { MessageDialogService } from "../../application-dialog/message-dialog-service";
import { OutComeDialogComponent } from "../../outcome/outcome-dialog/outcome-dialog.component";
import { OutcomeService } from "../../outcome/outcome.service";
import { MileStoneService } from "../mile-stone.service";

@Component({
  selector: "app-mile-stone-edit",
  templateUrl: "./mile-stone-edit.component.html",
  styleUrls: [
    "./../../outcome/outcome-config/outcome.component.scss",
    "./../../home.module.scss",
  ],
})
export class MileStoneEditComponent implements OnInit {
  mileStoneForm: FormGroup;
  isProgress: boolean = false;
  startDate: any;
  endDate: Date;
  start: any;
  end: any;
  outcomeArray = [];
  existingOutcomeList: any;

  constructor(
    private _router: Router,
    private _route: ActivatedRoute,
    public messageDialog: MessageDialogService,
    private mileStoneService: MileStoneService,
    private formBuilder: FormBuilder,
    private dataShare: DataSharedService,
    private outcomeService: OutcomeService,
    private dialog: MatDialog
  ) {}

  async fetchOutcomeList() {
    let response = await this.mileStoneService.fetchOutcomeConfig();
    if (response.status === "success") {
      this.outcomeArray = response.data;
    } else {
      this.messageDialog.openSnackBar(
        "Issue in Outcome Configuration",
        "error"
      );
    }
  }

  ngOnInit(): void {
    this.mileStoneForm = this.formBuilder.group({
      id: [""],
      mileStoneName: ["", [Validators.required]],
      milestoneReleaseID: ["", [Validators.required]],
      startDate: ["", [Validators.required]],
      endDate: ["", [Validators.required]],
      outcomeList: [[], [Validators.required]],
    });
    this._route.queryParams.subscribe((params) => {
      console.log(params);
      this.existingOutcomeList = params.existingOutcomeList;
      this.mileStoneForm.patchValue(params);
      this.mileStoneForm.controls["startDate"].setValue(
        new Date(
          this.dataShare.convertDateToSpecificDateFormat(
            new Date(params["startDate"]),
            "MM/dd/yyyy"
          )
        )
      );
      this.mileStoneForm.controls["endDate"].setValue(
        new Date(
          this.dataShare.convertDateToSpecificDateFormat(
            new Date(params["endDate"]),
            "MM/dd/yyyy"
          )
        )
      );
      this.startDate = new Date(
        this.dataShare.convertDateToSpecificDateFormat(
          new Date(params["startDate"]),
          "MM/dd/yyyy"
        )
      );
      this.endDate = new Date(
        this.dataShare.convertDateToSpecificDateFormat(
          new Date(params["endDate"]),
          "MM/dd/yyyy"
        )
      );
    });

    this.mileStoneForm.controls["mileStoneName"].disable();
  }

  onSubmit() {
    console.warn(this.mileStoneForm.value);
    this.mileStoneForm.controls["mileStoneName"].enable();
    if (this.mileStoneForm.valid) {
      this.isProgress = true;
      let requestObj = {};
      requestObj = this.mileStoneForm.value;
      requestObj["startDate"] = this.dataShare.convertDateToSpecificDateFormat(
        this.startDate,
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
      );
      requestObj["endDate"] = this.dataShare.convertDateToSpecificDateFormat(
        this.endDate,
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
      );
      requestObj["existingOutcomeList"] = this.existingOutcomeList;
      var self = this;
      var dialogmessage = " Do you want to update the changes? ";
      var title = "Update MileStone Configuration ";
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "40%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.mileStoneService
            .updateMileStoneConfig(requestObj)
            .then(function (response) {
              console.log(response);
              if (response.status === "success") {
                self.messageDialog.openSnackBar(
                  "Updated Successfully",
                  "success"
                );
                self._router.navigateByUrl("InSights/Home/fetchMileStone", {
                  skipLocationChange: true,
                });
              } else {
                self.messageDialog.openSnackBar(
                  response.message,
                  "error"
                );
              }
            });
        }
      });
      this.isProgress = false;
    } else {
      this.messageDialog.openSnackBar(
        "Please fill mandatory fields",
        "error"
      );
    }
  }

  openOutcomeDialog() {
    var dialogRef = this.dialog.open(OutComeDialogComponent, {
      panelClass: "custom-dialog-container",
      height: "500px",
      width: "1170px",
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      this.outcomeArray = [];
      this.outcomeService.setOutcomeSubject.subscribe((res) => {
        if (res && res.length > 0) {
          for (var ele of res) {
            this.outcomeArray.push(ele.outcomeName);
          }
        }
        console.log(this.outcomeArray);
        this.mileStoneForm.controls["outcomeList"].setValue(this.outcomeArray);
      });
    });
  }

  redirect() {
    this._router.navigate(["InSights/Home/fetchMileStone"], {
      skipLocationChange: true,
    });
  }
}
