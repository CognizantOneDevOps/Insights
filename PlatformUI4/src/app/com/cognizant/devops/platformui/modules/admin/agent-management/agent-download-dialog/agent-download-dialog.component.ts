/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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

import { Component, Inject, OnInit } from "@angular/core";
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { AgentService } from "../agent-management-service";

@Component({
  selector: "app-agent-download-dialog",
  templateUrl: "./agent-download-dialog.component.html",
  styleUrls: [
    "./agent-download-dialog.component.scss",
    "./../agent-management.component.scss",
    "./../../../home.module.scss",
  ],
})
export class AgentDownloadDialogComponent implements OnInit {
  agentTags: any = [];
  selectedVersion: string = "";

  constructor(
    public dialogRef: MatDialogRef<AgentDownloadDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public agentService: AgentService,
    public messageDialog: MessageDialogService
  ) {}

  ngOnInit(): void {
    this.getAgentTags();
  }

  public async getAgentTags() {
    let response = await this.agentService.getAgentTags();
    if (response.status === "success" && response.data !== null) {
      this.agentTags = Object.values(response.data).reverse();
    } else {
      this.closeDialog();
      this.messageDialog.openSnackBar(response.message, "error");
    }
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  public async downloadAgent() {
    if (this.selectedVersion.length > 0) {
      let response = await this.agentService.downloadAgentPackage(
        this.selectedVersion
      );
      this.selectedVersion = "";

      this.closeDialog();

      if (response.status == "success") {
        this.messageDialog.openSnackBar(response.data, "success");
      } else {
        this.messageDialog.openSnackBar(response.message, "error");
      }
    } else {
      this.messageDialog.openSnackBar("Please select a version first", "error");
    }
  }
}
