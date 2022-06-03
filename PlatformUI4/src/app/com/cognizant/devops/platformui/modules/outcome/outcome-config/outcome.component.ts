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
import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import {
  Validators,
  FormGroup,
  FormBuilder,
  FormControl,
  FormArray,
} from "@angular/forms";
import { MessageDialogService } from "../../application-dialog/message-dialog-service";
import { MatDialog } from "@angular/material/dialog";
import { OutcomeService } from "../outcome.service";
import { OutComeDialogComponent } from "../outcome-dialog/outcome-dialog.component";
import {
  NEW_RELIC_APPLICATION_ID,
  SPLUNK_SUMMARY_INDEX,
  NEW_RELIC_METRIC_NAME,
  DynatraceROI_METRIC_KEY,
  APPDYNAMICS_APPLICATION_NAME,
  APPDYNAMICS_METRIC_PATH,
} from "../outcome-constants";

@Component({
  selector: "app-outcome",
  templateUrl: "./outcome.component.html",
  styleUrls: ["./outcome.component.scss", "./../../home.module.scss"],
})
export class OutcomeComponent implements OnInit {
  isProgress: boolean = false;
  outcomeForm: FormGroup;
  category: any;
  toolArray: [];
  outcomeArray = [{ type: "Tech" }, { type: "Business" }];
  toolNameSelected: any;
  toolJson: any;
  toolConfigMap = new Map();
  toolMap = new Map();
  selectedTool: any;
  enableFields: boolean;

  constructor(
    private router: Router,
    private messageDialog: MessageDialogService,
    private formBuilder: FormBuilder,
    private outcomeService: OutcomeService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.outcomeForm = this.formBuilder.group({
      outcomeName: ["", [Validators.required]],
      outcomeType: [[], [Validators.required]],
      toolName: ["", [Validators.required]],
      splunkIndex: [""],
      DynatraceMetricKey: [""],
      ESLogKey: [""],
      isActive: [true, [Validators.required]],
      metricUrl: ["", [Validators.required]],
      parameters: this.formBuilder.array([]),
    });

    this.fetchToolList();
  }

  async fetchToolList() {
    let response = await this.outcomeService.fetchOutcomeToolConfig();
    if (response.status === "success") {
      if (response.data) {
        response.data.forEach((element) => {
          this.toolConfigMap.set(
            element.toolName,
            JSON.parse(element.toolConfigJson)
          );
          this.toolMap.set(element.id, element.toolName);
        });
      }
      console.log(this.toolConfigMap);
      this.toolArray = response.data;
      console.log(this.toolArray);
    } else {
      this.messageDialog.openSnackBar(
        "Issue in Outcome Tool Configuration",
        "error"
      );
    }
  }

  getToolJson(toolId) {
    this.selectedTool = this.toolMap.get(parseInt(toolId));
    console.log(this.selectedTool);
    if (this.toolConfigMap.get(this.selectedTool).list) {
      this.enableFields = true;
    } else {
      this.enableFields = false;
    }
    console.log(this.enableFields);
  }

  openOutcomeDialog() {
    this.dialog.open(OutComeDialogComponent, {
      panelClass: "custom-dialog-container",
      height: "500px",
      width: "550px",
      disableClose: true,
    });
  }

  onSubmit() {
    console.log(this.outcomeForm.value);

    if (this.outcomeForm.valid) {
      this.isProgress = true;
      let requestObj = {};
      requestObj = this.prepareRequestObj(this.outcomeForm.value);
      var self = this;
      var dialogmessage = " Do you want to save the changes? ";
      var title = "Save Outcome Configuration ";
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "30%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.outcomeService
            .saveOutcomeConfig(requestObj)
            .then(function (response) {
              console.log(response);
              if (response.status === "success") {
                self.messageDialog.openSnackBar(
                  "Saved Successfully",
                  "success"
                );
                self.router.navigateByUrl("InSights/Home/fetchOutcome", {
                  skipLocationChange: true,
                });
              } else {
                self.messageDialog.openSnackBar(response.message, "error");
              }
            });
        }
      });
      this.isProgress = false;
    }
  }

  prepareRequestObj(value: any): {} {
    console.log(value);
    let toolJson = {};
    let result = {};
    const tool = this.toolMap.get(parseInt(value.toolName));
    switch (tool) {
      case "SPLUNK": {
        let toolSpecificProps = {};
        toolJson = { ...value };
        toolSpecificProps[SPLUNK_SUMMARY_INDEX] = value.splunkIndex;
        toolJson["toolConfigJson"] = toolSpecificProps;
        result = toolJson;
        break;
      }
      case "ELASTICSEARCH": {
        let toolSpecificProps = {};
        toolJson = { ...value };
        toolSpecificProps["ESLogKey"] = value.ESLogKey;
        toolJson["toolConfigJson"] = toolSpecificProps;
        result = toolJson;
        break;
      }
      case "DYNATRACEROI": {
        let toolSpecificProps = {};
        toolJson = { ...value };
        toolSpecificProps[DynatraceROI_METRIC_KEY] = value.DynatraceMetricKey;
        toolJson["toolConfigJson"] = toolSpecificProps;
        result = toolJson;
        break;
      }
      default:
        result = value;
        break;
    }
    delete result["ESLogKey"];
    delete result[SPLUNK_SUMMARY_INDEX];
    delete result[DynatraceROI_METRIC_KEY];
    return result;
  }

  reset() {
    this.outcomeForm.reset();
  }

  redirect() {
    this.router.navigate(["InSights/Home/fetchOutcome"], {
      skipLocationChange: true,
    });
  }

  addParameter() {
    const control = <FormArray>this.outcomeForm.controls["parameters"];
    control.push(
      this.formBuilder.group({
        key: ["", [Validators.required]],
        value: ["", [Validators.required]],
      })
    );
  }

  removeParameter(i: number) {
    const control = <FormArray>this.outcomeForm.controls["parameters"];
    control.removeAt(i);
  }
}
