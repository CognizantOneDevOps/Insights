/*********************************************************************************
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
 *******************************************************************************/

import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Component, OnInit, Inject } from "@angular/core";
import { KafkaNeo4jService } from "./../kafka-neo4j.service";
import { ShowDetailsDialog } from "@insights/app/modules/healthcheck/healthcheck-show-details-dialog";
import { MatTableDataSource } from "@angular/material/table";

@Component({
  selector: "app-kafka-neo4j-details",
  templateUrl: "./kafka-neo4j-details.component.html",
  styleUrls: [
    "./kafka-neo4j-details.component.scss",
    "./../../home.module.scss",
  ],
})
export class KafkaNeo4jDetailsComponent implements OnInit {
  resyncDatasource = new MatTableDataSource<any>();
  deleteDatasource = new MatTableDataSource<any>();
  displayedColumns: string[] = ["executionTime", "status", "message"];
  showThrobber: boolean = false;
  resyncResponse: boolean = false;
  deleteResponse: boolean = false;
  executionTime: string;
  status: string;
  message: string;
  pathName: string = "${INSIGHTS_HOME}/logs/ReplicaDaemon/resync_script_log.txt";
  showConfirmMessage: string;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<ShowDetailsDialog>,
    public kafkaNeo4jService: KafkaNeo4jService
  ) {}

  ngOnInit() {
    this.getLogDeatils();
  }

  async getLogDeatils() {
    let offlineList = [];
    offlineList = await this.kafkaNeo4jService
      .getLogDetails()
      .then((result) => {
        if (result.status === "success") {
          return result.data;
        }
      });

    if (offlineList != null) {
      Object.entries(offlineList).forEach(([key, value]) => {
        if (key === "resync_script.log") {
          this.resyncResponse = true;
          this.resyncDatasource.data = value;
        } else if (key === "delete_script.log") {
          this.deleteResponse = true;
          this.deleteDatasource.data = value;
        }
      });
    }
  }

  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }

  tabChanged(event: any) {
    if (event.tab.textLabel === "Resync Log Details") {
      this.pathName = "${INSIGHTS_HOME}/logs/ReplicaDaemon/resync_script.log";
    } else if (event.tab.textLabel === "Delete Log Details") {
      this.pathName = "${INSIGHTS_HOME}/logs/ReplicaDaemon/delete_script.log";
    }
  }
}
