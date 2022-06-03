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
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { NavigationExtras, Router } from "@angular/router";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { GrafanaAuthenticationService } from "@insights/common/grafana-authentication-service";
import { InsightsInitService } from "@insights/common/insights-initservice";

@Component({
  selector: "dashboard-preview-configuration-dialog",
  templateUrl: "dashboard-preview-configuration-dialog.html",
  styleUrls: ["dashboard-preview-configuration-dialog.scss"],
})
export class DashboardPreviewConfigDialog implements OnInit {
  dashboardUrl: SafeResourceUrl;
  isAssessmentReport: boolean = false;

  constructor(
    public dialogRef: MatDialogRef<DashboardPreviewConfigDialog>,
    public router: Router,
    private sanitizer: DomSanitizer,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private grafanaService: GrafanaAuthenticationService
  ) {}

  ngOnInit() {
    this.dashboardUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      this.data.route
    );
    if (this.data.isAssessmentReport != null) {
      this.isAssessmentReport = this.data.isAssessmentReport;
    }
  }

  closeDialog() {
    this.dialogRef.close();
    this.grafanaService.onOkSubject.next("OK");
  }

  confirm() {
    this.dialogRef.close("confirm");
  }
}
