/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
import { DomSanitizer, BrowserModule, SafeUrl } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatIconRegistry } from '@angular/material/icon';
import { APP_INITIALIZER } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from '@insights/app/material.module';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSortModule } from '@angular/material/sort';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { HomeRouting } from '@insights/app/modules/home.routing';
import { SharedServices } from '@insights/app/shared.services';
import { HomeComponent } from '@insights/app/modules/home/home.component';
import { PlaylistComponent } from '@insights/app/modules/playlist/playlist.component';
import { MenuListItemComponent } from '@insights/app/modules/menu-list-item/menu-list-item.component';
import { GrafanaDashboardComponent } from '@insights/app/modules/grafana-dashboard/grafana-dashboard.component';
import { PageNotFoundComponent } from '@insights/app/modules/page-not-found/page-not-found.component';
import { HealthCheckComponent } from '@insights/app/modules/healthcheck/healthcheck.component';
import { ShowDetailsDialog } from '@insights/app/modules/healthcheck/healthcheck-show-details-dialog';
import { AboutDialog } from '@insights/app/modules/about/about-show-popup';
import { AgentManagementComponent } from '@insights/app/modules/admin/agent-management/agent-management.component';
import { DatadictionaryComponent } from '@insights/app/modules/datadictionary/datadictionary.component';

import { DataArchivingComponent } from '@insights/app/modules/settings/dataarchiving/dataarchiving.component';
import { GrafanaAuthenticationService } from '@insights/common/grafana-authentication-service';
import { GrafanaDashboardService } from '@insights/app/modules/grafana-dashboard/grafana-dashboard-service';
import { AgentService } from '@insights/app/modules/admin/agent-management/agent-management-service';
import { HealthCheckService } from '@insights/app/modules/healthcheck/healthcheck.service';
import { DataDictionaryService } from '@insights/app/modules/datadictionary/datadictionary.service';

import { BusinessMappingService } from '@insights/app/modules/admin/businessmapping/businessmapping.service';
import { BusinessMappingComponent } from '@insights/app/modules/admin/businessmapping/businessmapping.component';

import { DataArchivingService } from '@insights/app/modules/settings/dataarchiving/dataarchiving-service';
import { AgentConfigurationComponent } from '@insights/app/modules/admin/agent-management/agent-configuration/agent-configuration.component';


import { UserOnboardingComponent } from '@insights/app/modules/user-onboarding/user-onboarding.component';
import { LandingPageComponent } from '@insights/app/modules/landing-page/landing-page.component';
import { ConfirmationMessageDialog } from '@insights/app/modules/application-dialog/confirmation-message-dialog';
import { ApplicationMessageDialog } from '@insights/app/modules/application-dialog/application-message-dialog';
import { AddGroupMessageDialog } from '@insights/app/modules/user-onboarding/add-group-message-dialog';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';

import { DatePipe, TitleCasePipe } from '@angular/common';
import { UserOnboardingService } from '@insights/app/modules/user-onboarding/user-onboarding-service';
import { StorageServiceModule } from 'ngx-webstorage-service';
import { LogoSettingComponent } from '@insights/app/modules/settings/logo-setting/logo-setting.component';
import { LogoSettingService } from '@insights/app/modules/settings/logo-setting/logo-setting.service';

import { ShowJsonDialog } from '@insights/app/modules/relationship-builder/show-correlationjson';
import { RelationshipBuilderComponent } from '@insights/app/modules/relationship-builder/relationship-builder.component';
import { RelationshipBuilderService } from '@insights/app/modules/relationship-builder/relationship-builder.service';
import { BulkUploadComponent } from '@insights/app/modules/bulkupload/bulkupload.component';
import { BulkUploadService } from '@insights/app/modules/bulkupload/bulkupload.service';
import { WebHookComponent } from '@insights/app/modules/webhook/webhook.component';
import { WebHookService } from '@insights/app/modules/webhook/webhook.service';
import { TraceabilityDashboardCompenent } from '@insights/app/modules/traceability/traceability-builder.component';
import { TraceabiltyService } from '@insights/app/modules/traceability/traceablity-builder.service';
import { ShowTraceabiltyDetailsDialog } from '@insights/app/modules/traceability/traceabilty-show-details-dialog';
import { AddPropertyDialog } from '@insights/app/modules/relationship-builder/add-propertydialog';
import { LandingPageService } from '@insights/app/modules/landing-page/landing-page.service';
import { ViewKPIDialog } from '@insights/app/modules/reportmanagement/report-configuration/view-kpi-dialog';
import { AddTasksDialog } from '@insights/app/modules/reportmanagement/report-configuration/add-task';
import { DragulaModule, DragulaService } from 'ng2-dragula';
import { ReportManagementService } from '@insights/app/modules/reportmanagement/reportmanagement.service';
import { ReportManagementComponent } from '@insights/app/modules/reportmanagement/reportmanagement.component';
import { WorkflowHistoryDetailsDialog } from '@insights/app/modules/reportmanagement/workflow-history-details/workflow-history-details-dialog';
import { ReportConfigComponent } from '@insights/app/modules/reportmanagement/report-configuration/report-configuration.component';
import { DataArchiveDetailsDialog } from '@insights/app/modules/settings/dataarchiving/data-archive-details/data-archive-details-dialog';
import { DataArchiveConfigureURLDialog } from '@insights/app/modules/settings/dataarchiving/data-archive-configureurl/data-archive-configureurl-dialog';
import { MatSliderModule } from '@angular/material/slider';
import { NgxPaginationModule } from 'ngx-pagination';
import { MLWizardComponent } from '@insights/app/modules/model-management/mlwizard/mlwizard.component';
import { MLWizardService } from '@insights/app/modules/model-management/mlwizard/mlwizard.service';
import { PredictionComponent } from '@insights/app/modules/model-management/mlwizard/prediction/prediction.component';
import { ModelManagementComponent } from '@insights/app/modules/model-management/model-management.component';
import { ModelManagementService } from '@insights/app/modules/model-management/model-management.service';
import { PredictionShowDetailsDialog } from '@insights/app/modules/model-management/mlwizard/prediction/prediction-show-detail/prediction-show-details-dialog';
import { EmailConfigurationDialog } from '@insights/app/modules/reportmanagement/report-configuration/email-configuration-dialog';
import { KpiCreationComponent } from '@insights/app/modules/kpi-creation/kpi-creation.component';
import { KpiAdditionComponent } from '@insights/app/modules/kpi-addition/kpi-addition.component';
import { FileUploadDialog } from './fileUploadDialog/fileUploadDialog.component';
import { KpiService } from './kpi-addition/kpi-service';
import { ContentConfigComponent } from '@insights/app/modules/content-config-list/content-config-list.component';
import { ContentConfigAddition } from '@insights/app/modules/content-config-add/content-config-add.component';
import { KpiListDialog } from './kpiList-Dialog/kpiList-Dialog.component';
import { ContentService } from './content-config-list/content-service';
import { ReportTemplateComponent } from './report-template/report-template-list.component';
import { ReportTemplateService } from './report-template/report-template-service';
import { KpiReportListDialog } from './report-template/report-template-kpi-list/kpi-report-List-Dialog.component';
import { ReportTemplateConfig } from './report-template/template-configuration/template-configuration.component';
import { ServerConfigurationComponent } from '@insights/app/modules/server-configuration/server-configuration.component';
import { ServerConfigurationService } from '@insights/app/modules/server-configuration/server-configuration-service';
import { FileSystemComponent } from './filesystem/file-system.component';
import { FileSystemService } from './filesystem/file-system.service';
import { FileSystemConfigComponent } from './filesystem/file-system-configuration/filesystem-config.component';
import { DashboardPdfDownloadComponent } from './dashboard-pdf-download/dashboard-pdf-download.component';
import { DashboardPreviewConfigDialog } from './dashboard-pdf-download/dashboard-preview-configuration-dialog';
import { DashboardListComponent } from './dashboard-pdf-download/dashboard-list/dash-list.component';
import { DashboardDetailsDialog } from './dashboard-pdf-download/dashboard-details-dialog/dashboard-details-dialog';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { EditDashboardComponent } from './dashboard-pdf-download/edit-dashboard/edit-dashboardcomponent';
import { WorkflowTaskManagementComponent } from './workflow-task-management/workflow-task-management.component';
import { WorkflowTaskManagementService } from './workflow-task-management/workflow-task-management.service';
import { AddWorkflowTaskComponent } from './workflow-task-management/add-workflow-task/add-workflow-task.component';
import { OutcomeComponent } from './outcome/outcome-config/outcome.component';
import { OutcomeService } from './outcome/outcome.service';
import { OutcomeProvider } from './outcome/outcome.provider';
import { OutcomeListComponent } from './outcome/outcome-list/outcome-list.component';
import { OutcomeEditComponent } from './outcome/outcome-edit/outcome-edit.component';
import { OutComeDialogComponent } from './outcome/outcome-dialog/outcome-dialog.component';
import { OfflineDataListComponent } from '@insights/app/modules/offline-data-processing/offline-data-list/offline-data-list.component';
import { OfflineDetailsComponent } from '@insights/app/modules/offline-data-processing/offline-details/offline-details.component';
import { OfflineConfigurationComponent } from '@insights/app/modules/offline-data-processing/offline-configuration/offline-configuration.component';
import { OfflineService } from '@insights/app/modules/offline-data-processing/offline-service';
import { MileStoneComponent } from './mile-stone/mile-stone-config/mile-stone.component';
import { MileStoneService } from './mile-stone/mile-stone.service';
import { MileStoneListComponent } from './mile-stone/mile-stone-list/mile-stone-list.component'
import { MileStoneEditComponent } from './mile-stone/mile-stone-edit/mile-stone-edit.component';
import { MileStoneDialog } from './mile-stone/mile-stone-dialog/milestone-dialog';
import { ScheduleTaskManagmentComponent } from '@insights/app/modules/schedule-task-managment/schedule-task-managment.component';
import { TaskManagementService } from '@insights/app/modules/schedule-task-managment/task-management-service';
import { TaskHistoryDetailsDialog } from '@insights/app/modules/schedule-task-managment/task-history-details/task-history-details-dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import { SnackbarComponent } from './application-dialog/snackbar-message';
import { AgentDownloadDialogComponent } from './admin/agent-management/agent-download-dialog/agent-download-dialog.component';
import { MultipleEmailConfigurationComponent } from '@insights/app/modules/multiple-email-configuration/multiple-email-configuration.component';
import { AddComponent } from '@insights/app/modules/multiple-email-configuration/dialogs/add/add.component';
import { OfflineAlertListComponent } from '@insights/app/modules/offline-alerting/offline-alert-list/offline-alert-list.component';
import { OfflineAlertConfigurationComponent } from '@insights/app/modules/offline-alerting/offline-alert-configuration/offline-alert-configuration.component';
import { OfflineAlertingService } from '@insights/app/modules/offline-alerting/offline-alerting-service';
import { OfflineAlertHistoryDetailsDialogComponent } from '@insights/app/modules/offline-alerting/offline-alert-history-details-dialog/offline-alert-history-details-dialog.component';


@NgModule({
    declarations: [
        HomeComponent,
        PlaylistComponent,
        MenuListItemComponent,
        GrafanaDashboardComponent,
        PageNotFoundComponent,
        HealthCheckComponent,
        ShowDetailsDialog,
        ShowTraceabiltyDetailsDialog,
        AboutDialog,
        AgentManagementComponent,
        DatadictionaryComponent,
        BusinessMappingComponent,
        DataArchivingComponent,
        DataArchiveDetailsDialog,
        DataArchiveConfigureURLDialog,
        AgentConfigurationComponent,
        MultipleEmailConfigurationComponent, 
        AddComponent,
        UserOnboardingComponent,
        LandingPageComponent,
        ConfirmationMessageDialog,
        ApplicationMessageDialog,
        AddGroupMessageDialog,
        LogoSettingComponent,
        ShowJsonDialog,
        RelationshipBuilderComponent,
        BulkUploadComponent,
        WebHookComponent,
        ReportManagementComponent,
        ReportConfigComponent,
        TraceabilityDashboardCompenent,
        AddPropertyDialog,
        AddTasksDialog,
        ViewKPIDialog,
        WorkflowHistoryDetailsDialog,
        MLWizardComponent,
        PredictionComponent,
        ModelManagementComponent,
        WorkflowHistoryDetailsDialog,
        EmailConfigurationDialog,
        DashboardPreviewConfigDialog,
        PredictionShowDetailsDialog,
        KpiCreationComponent,
        KpiAdditionComponent,
        FileUploadDialog,
        KpiListDialog,
        ContentConfigComponent,
        ContentConfigAddition,
        ReportTemplateComponent,
        KpiReportListDialog,
        ReportTemplateConfig,
        ServerConfigurationComponent,
        FileSystemComponent,
        FileSystemConfigComponent,
        DashboardPdfDownloadComponent,
        EditDashboardComponent,
        DashboardListComponent,
        DashboardDetailsDialog,
        WorkflowTaskManagementComponent,
        AddWorkflowTaskComponent,
        DashboardDetailsDialog,
        OutcomeComponent,
        OutcomeListComponent,
        OutcomeEditComponent,
        OutComeDialogComponent,
        OfflineDataListComponent,
        OfflineConfigurationComponent,
        OfflineDetailsComponent,
        MileStoneComponent,
        MileStoneListComponent,
        MileStoneEditComponent,
        MileStoneDialog,
        AddWorkflowTaskComponent,
        DashboardDetailsDialog,
        ScheduleTaskManagmentComponent,
        TaskHistoryDetailsDialog,
        SnackbarComponent,
        AgentDownloadDialogComponent,
        OfflineAlertListComponent,
        OfflineAlertConfigurationComponent,
        OfflineAlertHistoryDetailsDialogComponent 

    ],
    imports: [
        HomeRouting,
        BrowserModule,
        CommonModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        FormsModule,
        MaterialModule,
        SharedServices,
        MatDatepickerModule,
        StorageServiceModule,
        DragulaModule,
        MatSliderModule,
        NgxPaginationModule,
        NgxJsonViewerModule,
        MatSnackBarModule
    ],
    exports: [MatSortModule],
    providers: [
        GrafanaAuthenticationService,
        GrafanaDashboardService,
        AgentService,
        HealthCheckService,
        DataDictionaryService,
        BusinessMappingService,
        WorkflowTaskManagementService,
        DataArchivingService,
        UserOnboardingService,
        MessageDialogService,
        LogoSettingService,      
        DatePipe,
        DragulaService,
        TitleCasePipe,
        RelationshipBuilderService,
        BulkUploadService,
        WebHookService,
        ReportManagementService,
        LandingPageService,
        TraceabiltyService,
        MLWizardService,
        ModelManagementService,
        KpiService,
        ContentService,
        ReportTemplateService,
        ServerConfigurationService,
        FileSystemService,
        OutcomeService,
        MileStoneService,
        OutcomeProvider,
        FileSystemService,
        TaskManagementService,
        OfflineService,
        OfflineAlertingService
    ]
})
export class HomeModules { }
