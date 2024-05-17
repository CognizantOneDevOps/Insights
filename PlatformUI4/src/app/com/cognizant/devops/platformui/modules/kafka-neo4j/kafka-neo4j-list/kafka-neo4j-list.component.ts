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
import { MatRadioChange } from "@angular/material/radio";
import { MatDialog } from "@angular/material/dialog";
import { DataSharedService } from "./../../../common.services/data-shared-service";
import { InsightsUtilService } from "./../../../common.services/insights-util.service";
import { Sort } from "@angular/material/sort";
import { MessageDialogService } from "./../../application-dialog/message-dialog-service";
import { MatTableDataSource } from "@angular/material/table";
import { Router, NavigationExtras } from "@angular/router";
import { KafkaNeo4jService } from "./../kafka-neo4j.service";
import { Component, OnInit } from "@angular/core";
import { KafkaNeo4jDetailsComponent } from "./../kafka-neo4j-details/kafka-neo4j-details.component";

@Component({
  selector: "app-kafka-neo4j-list",
  templateUrl: "./kafka-neo4j-list.component.html",
  styleUrls: ["./kafka-neo4j-list.component.scss", "./../../home.module.scss"],
})
export class KafkaNeo4jListComponent implements OnInit {
  replicaDataSource = new MatTableDataSource<any>();
  displayColumns = [];
  selectedReplica: any;
  replicaResponse: any;
  showConfirmMessage: string;
  onRadioBtnSelect: boolean = false;
  MAX_ROWS_PER_TABLE = 5;
  isDataInProgress: boolean = false;
  neo4jScalingConfigs: any = {};
  streamsConfig: any = {};
  replicaConfig: any = [];
  responseFlag: boolean = false;

  constructor(
    public router: Router,
    public kafkaNeo4jService: KafkaNeo4jService,
    private messageDialog: MessageDialogService,
    public insightsUtil: InsightsUtilService,
    public dataShare: DataSharedService,
    public dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.getAllReplicas();
    this.displayColumns = [
      "radio",
      "name",
      "endpoint",
      "nodeCount",
      "relationshipCount",
      "nodesBehind",
      "relationshipBehind",
      "status",
    ];
    this.getAllConfig();
  }

  // move to configure screen
  configure() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        streamsConfig:
          this.streamsConfig && this.streamsConfig["sourceServerIP"]
            ? JSON.stringify(this.streamsConfig)
            : null,
        replicaConfig:
          this.replicaConfig && this.replicaConfig.length > 0
            ? JSON.stringify(this.replicaConfig)
            : [],
        isEdit:
          this.streamsConfig && this.streamsConfig["sourceServerIP"]
            ? true
            : false,
      },
    };
    this.router.navigate(
      ["InSights/Home/KafkaNeo4jConfigurationComponent"],
      navigationExtras
    );
  }

  delete() {
    var self = this;
    const replicaName = this.selectedReplica.name;
    var title = "Delete Replica";
    if (!replicaName) return;
    var dialogmessage =
      "Do you want to delete <b>" +
      self.selectedReplica.name +
      "</b> ? <br> <b> Please note: </b> The action of deleting replica " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue ? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      replicaName,
      "DELETE",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        self.kafkaNeo4jService
          .deleteReplica(replicaName)
          .then(function (data) {
            if (data.status === "success") {
              self.messageDialog.openSnackBar(data.data, "success");
              self.refresh();
            }
            if (data.status === "failure") {
              self.messageDialog.openSnackBar(data.data, "error");
              self.refresh();
            }
          })
          .catch(function (data) {
            self.showConfirmMessage = "service_error";
          });
      }
    });
  }

  enableButtons(event: MatRadioChange, index) {
    this.onRadioBtnSelect = true;
  }

  async resync() {
    var self = this;
    var title = "Resync All Replicas";
    var dialogmessage =
      "Do you want to resync all replicas? <br> <b>Resyncing will restart your Master Neo4j, Replicas and Engine.</b> Please be Cautious while Performing this action!";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        self.kafkaNeo4jService
          .resyncAllReplicas()
          .then(function (data) {
            if (data.status === "success") {
              self.messageDialog.openSnackBar(data.data, "success");
              self.refresh();
            }
            if (data.status === "failure") {
              self.messageDialog.openSnackBar(data.data, "error");
              self.refresh();
            }
          })
          .catch(function (data) {
            self.showConfirmMessage = "service_error";
          });
      }
    });
  }

  refresh() {
    this.onRadioBtnSelect = false;
    this.getAllReplicas();
    this.getAllConfig();
  }

  // Get Source and Replica Config
  async getAllConfig() {
    this.isDataInProgress = true;

    this.neo4jScalingConfigs =
      await this.kafkaNeo4jService.getNeo4jScalingConfigs();
    if (
      this.neo4jScalingConfigs != null &&
      this.neo4jScalingConfigs.status == "success"
    ) {
      this.streamsConfig = this.neo4jScalingConfigs.data["sourceStreamsConfig"];
      this.replicaConfig = this.neo4jScalingConfigs.data["replicaConfig"];
      this.isDataInProgress = false;
    } else {
      this.isDataInProgress = false;
    }
  }

  // Get all replica details
  async getAllReplicas() {
    this.isDataInProgress = true;
    this.replicaResponse = await this.kafkaNeo4jService.getAllReplicas();
    if (
      this.replicaResponse.status === "success" &&
      this.replicaResponse.data
    ) {
      this.replicaDataSource.data = this.replicaResponse.data;
      this.isDataInProgress = false;
      this.responseFlag = true;
    } else {
      this.isDataInProgress = false;
    }
  }

  // Sort data
  sortData(sort: Sort) {
    const data = this.replicaResponse.data.slice();
    if (!sort.active || sort.direction === "") {
      this.replicaDataSource.data = data;
      return;
    }

    this.replicaDataSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === "asc";
      return this.insightsUtil.compare(a[sort.active], b[sort.active], isAsc);
    });
  }

  details() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(KafkaNeo4jDetailsComponent, {
        panelClass: "custom-dialog-container",
        height: "85%",
        width: "85%",
        disableClose: true,
      });
    }
  }
}
