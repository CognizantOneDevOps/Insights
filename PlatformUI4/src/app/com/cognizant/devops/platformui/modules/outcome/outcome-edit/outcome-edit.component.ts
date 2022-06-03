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
import { ActivatedRoute, Router } from "@angular/router";
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
import { OutcomeProvider } from "../outcome.provider";
import {
  NEW_RELIC_APPLICATION_ID,
  SPLUNK_SUMMARY_INDEX,
  NEW_RELIC_METRIC_NAME,
  DynatraceROI_METRIC_KEY,
} from "../outcome-constants";

@Component({
  selector: "app-outcome-edit",
  templateUrl: "./outcome-edit.component.html",
  styleUrls: [
    "../outcome-config/outcome.component.scss",
    "./../../home.module.scss",
  ],
})
export class OutcomeEditComponent implements OnInit {
  isProgress: boolean = false;
  outcomeForm: FormGroup;
  outcomeArray = [{ type: "Tech" }, { type: "Business" }];
  toolArray: [];
  toolConfigMap = new Map();
  toolMap = new Map();
  selectedTool: any;
  enableFields: boolean = true;
  toolConfigList: any;
  toolNameSelected: any;

  constructor(
    private router: Router,
    private messageDialog: MessageDialogService,
    private formBuilder: FormBuilder,
    private outcomeService: OutcomeService,
    private dialog: MatDialog,
    private _route: ActivatedRoute,
    private outcomeProvider: OutcomeProvider
  ) {}

  ngOnInit() {
    this.outcomeForm = this.formBuilder.group({
      id: [""],
      outcomeName: ["", [Validators.required]],
      outcomeType: ["", [Validators.required]],
      toolName: ["", [Validators.required]],
      splunkIndex: [""],
      DynatraceMetricKey: [""],
      ESLogKey: [""],
      isActive: [false, [Validators.required]],
      toolId: [""],
      metricUrl: ["", [Validators.required]],
      parameters: this.formBuilder.array([]),
    });

    this.fetchToolList();
    var self = this;
    this._route.queryParams.subscribe((params) => {
      console.log(params);
      console.log(typeof params);
      var parameterList = JSON.parse(params.parameters);
      console.log(parameterList);
      Object.keys(params).map((key) => {
        console.log("key " + key + "  value " + params[key]);
        if (
          self.outcomeForm.controls[key] != undefined &&
          key != "parameters"
        ) {
          self.outcomeForm.controls[key].setValue(params[key]);
          console.log("controls"+self.outcomeForm.controls["outcomeType"]);
        }
      });

      console.log(this.outcomeForm.value);
      this.toolNameSelected = params.toolId;
      this.toolConfigList = JSON.parse(this.outcomeProvider.storage);
      console.log(this.toolConfigList);

      const control = <FormArray>this.outcomeForm.controls["parameters"];
      parameterList.forEach((x) => {
        control.push(
          this.formBuilder.group({
            key: x.key,
            value: x.value,
          })
        );
      });
    });

    this.outcomeForm.controls["outcomeName"].disable();
    this.outcomeForm.controls["toolName"].disable();
  }

  async fetchToolList() {
    let response = await this.outcomeService.fetchOutcomeToolConfig();
    if (response.status === "success") {
      this.toolArray = response.data;
      console.log(this.toolArray);
    } else {
      this.messageDialog.openSnackBar(
        "Issue in Outcome Tool Configuration",
        "error"
      );
    }
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
    this.outcomeForm.controls["outcomeName"].enable();
    this.outcomeForm.controls["toolName"].enable();
    if (this.outcomeForm.valid) {
      this.isProgress = true;
      let requestObj = {};
      requestObj = this.prepareRequestObj(this.outcomeForm.value);
      var self = this;
      var dialogmessage = " Do you want to save the changes? ";
      var title = "Update Outcome Configuration ";
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "40%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.outcomeService
            .updateOutcomeConfig(requestObj)
            .then(function (response) {
              console.log(response);
              if (response.status === "success") {
                self.messageDialog.openSnackBar(
                  "Updated Successfully",
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
    let toolJson = {};
    let result = {};
    const tool = value.toolName;
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
