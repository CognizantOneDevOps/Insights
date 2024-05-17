/*******************************************************************************
 * Copyright 2024 Cognizant Technology Solutions
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
import { MessageDialogService } from "./../../application-dialog/message-dialog-service";
import { KafkaNeo4jService } from "./../kafka-neo4j.service";
import { NavigationExtras, Router, ActivatedRoute } from "@angular/router";
import { Component, OnInit } from "@angular/core";
import {
  FormGroup,
  FormBuilder,
  FormArray,
  Validators,
  FormControl,
} from "@angular/forms";

@Component({
  selector: "app-kafka-neo4j-configuration",
  templateUrl: "./kafka-neo4j-configuration.component.html",
  styleUrls: [
    "./kafka-neo4j-configuration.component.scss",
    "./../../home.module.scss",
  ],
})
export class KafkaNeo4jConfigurationComponent implements OnInit {
  onEdit: boolean = false;
  sourceForm: FormGroup;
  replicasForm: FormGroup;
  nodeLabelExample: string = `DATA{*} includes all data sublabels (i.e. GITHUB{*};JIRA{*};JENKINS{*} etc)`;
  relationshipLabelExample: string = `BRANCH_HAS_COMMITS{*};BRANCH_HAS_PULL_REQUESTS{*}`;
  isDataInProgress: boolean = false;
  neo4jScalingConfigs: any = {};
  streamsConfig: any = {};
  replicaConfig: any = [];
  receivedParam: any;
  replicasLength: number = 0;
  MAX_REPLICA_COUNT: number = 3;

  constructor(
    public router: Router,
    public route: ActivatedRoute,
    public kafkaNeo4jService: KafkaNeo4jService,
    public messageDialog: MessageDialogService,
    public formBuilder: FormBuilder
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      if (JSON.parse(params.isEdit)) {
        this.onEdit = JSON.parse(params.isEdit);
        this.streamsConfig = JSON.parse(params.streamsConfig);
        if (params.replicaConfig.length > 0) {
          this.replicaConfig = JSON.parse(params.replicaConfig);
        } else {
          this.replicaConfig = params.replicaConfig;
        }
      }
    });

    if (this.onEdit) {
      this.sourceForm = this.formBuilder.group({
        kafkaEndpoint: [
          { value: this.streamsConfig["kafkaEndpoint"], disabled: this.onEdit },
          [Validators.required],
        ],
        topicName: [
          { value: this.streamsConfig["topicName"], disabled: this.onEdit },
          [Validators.required, Validators.pattern("^[A-Za-zd-]+$")],
        ],
        nodeLabels: [
          this.streamsConfig["nodeLabels"],
          [Validators.required, this.noWhitespaceValidator],
        ],
        relationshipLabels: [
          this.streamsConfig["relationshipLabels"],
          [Validators.required, this.noWhitespaceValidator],
        ],
      });

      let replicas = [];
      this.replicaConfig.forEach((element) => {
        let replica = this.formBuilder.group({
          replicaName: [
            { value: element.replicaName, disabled: this.onEdit },
            [Validators.required],
          ],
          replicaIP: [
            { value: element.replicaIP, disabled: this.onEdit },
            [Validators.required],
          ],
          replicaEndpoint: [
            { value: element.replicaEndpoint, disabled: this.onEdit },
            [Validators.required],
          ],
        });
        replicas.push(replica);
        this.replicasLength++;
      });
      this.replicasForm = this.formBuilder.group({
        replicas: this.formBuilder.array(
          [...replicas],
          Validators.compose([
            Validators.minLength(1),
            Validators.maxLength(this.MAX_REPLICA_COUNT),
            Validators.required,
          ])
        ),
      });
    } else {
      this.sourceForm = this.formBuilder.group({
        kafkaEndpoint: ["", [Validators.required, this.noWhitespaceValidator]],
        topicName: [
          "",
          [Validators.required, Validators.pattern("^[A-Za-zd-]+$")],
        ],
        nodeLabels: [
          "DATA{*}",
          [Validators.required, this.noWhitespaceValidator],
        ],
        relationshipLabels: [
          "",
          [Validators.required, this.noWhitespaceValidator],
        ],
      });
      this.replicasForm = this.formBuilder.group({
        replicas: this.formBuilder.array(
          [],
          Validators.compose([
            Validators.minLength(1),
            Validators.maxLength(this.MAX_REPLICA_COUNT),
            Validators.required,
          ])
        ),
      });
    }
  }

  reset() {
    if (this.onEdit) {
      this.sourceForm.patchValue({
        nodeLabels: this.streamsConfig["nodeLabels"],
        relationshipLabels: this.streamsConfig["relationshipLabels"],
      });
    } else {
      this.sourceForm.patchValue({
        kafkaEndpoint: "",
        topicName: "",
        nodeLabels: "",
        relationshipLabels: "",
      });
    }
  }

  back() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
    };
    this.router.navigate(["InSights/Home/kafkaNeo4jList"], navigationExtras);
  }

  // Save Source and Replica Config
  save() {
    var self = this;
    let saveConfigStatus = "";
    let sourceFlag = this.validateSourceForm();
    let replicaFlag = this.validateReplicaForm();
    this.replicasForm.controls["replicas"].enable();
    if (sourceFlag && replicaFlag) {
      this.sourceForm.controls["kafkaEndpoint"].enable();
      this.sourceForm.controls["topicName"].enable();
      let dialogmessage =
        " Do you want to save the configuration? <b>Saving this will restart your Master Neo4j, Replicas and Engine.</b> Please be Cautious while Performing this action!";
      let title = "Save Source & Replica Configuration ";
      const confirmationDialog = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "30%"
      );

      confirmationDialog.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.kafkaNeo4jService
            .saveNeo4jScalingConfigs(
              this.sourceForm.value,
              this.replicasForm.value
            )
            .then(function (response) {
              if (response.status == "success") {
                saveConfigStatus = "Configurations Saved Successfully";
                self.messageDialog.openSnackBar(saveConfigStatus, "success");
              } else if (
                response.status == "failure" &&
                response.message == "Can't save master neo4j as replica"
              ) {
                self.messageDialog.openSnackBar(response.message, "error");
              } else if (
                response.status == "failure" &&
                response.message == "Failed to get node count from Neo4j"
              ) {
                self.messageDialog.openSnackBar(response.message, "error");
              } else if (response.status == "failure") {
                saveConfigStatus = "Failed to Configurations ";
                self.messageDialog.openSnackBar(saveConfigStatus, "error");
              }
            });
          this.back();
        } else {
          this.sourceForm.controls["kafkaEndpoint"].disable();
          this.sourceForm.controls["topicName"].disable();
          this.replicasForm.controls["replicas"].disable();
        }
      });
    } else {
      if (!sourceFlag) {
        self.messageDialog.openSnackBar(
          "Please provide correct values in Source configuration",
          "error"
        );
      } else if (!replicaFlag) {
        self.messageDialog.openSnackBar(
          "Please provide correct values in Replica configuration",
          "error"
        );
        this.back();
      }
      this.replicasForm.controls["replicas"]["controls"].forEach((replica) => {
        if (replica.status === "VALID") {
          replica.disable();
        }
      });
    }
  }

  // add replica to replica form
  addReplica() {
    const control = <FormArray>this.replicasForm.controls["replicas"];
    control.push(
      this.formBuilder.group({
        replicaName: ["", [Validators.required]],
        replicaIP: ["", [Validators.required, this.noWhitespaceValidator]],
        replicaEndpoint: [
          "",
          [Validators.required, this.noWhitespaceValidator],
        ],
      })
    );
    this.replicasLength++;
  }

  // remove replica from replica form
  removeReplica(i: number) {
    const control = <FormArray>this.replicasForm.controls["replicas"];
    control.removeAt(i);
    this.replicasLength--;
  }

  // UTILITIES
  validateSourceForm() {
    let isValid: boolean = false;
    if (this.sourceForm.valid) {
      isValid = true;
    } else {
      isValid = false;
    }
    return isValid;
  }

  validateReplicaForm() {
    let isValid: boolean = false;
    let replicas = this.replicasForm.value.replicas;
    if (this.replicasForm.valid && replicas.length > 0) {
      isValid = true;
    } else {
      isValid = false;
    }
    for (const [index, element] of replicas.entries()) {
      let pattern = "http://" + element.replicaIP + ":7474";
      if (element.replicaEndpoint.includes(pattern)) {
        isValid = true;
      } else {
        this.removeReplica(index);
        isValid = false;
        break;
      }
    }

    return isValid;
  }

  // custom whitspace validator
  noWhitespaceValidator(control: FormControl) {
    const isWhitespace = control.value.includes(" ");
    const isValid = !isWhitespace;
    return isValid ? null : { 'whitespace': { value: true } };
  }
}
