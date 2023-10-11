/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
import { Routes, RouterModule } from '@angular/router';
import { ModuleWithProviders } from '@angular/core';
import { PlaylistComponent } from '@insights/app/modules/playlist/playlist.component';
import { HealthCheckComponent } from '@insights/app/modules/healthcheck/healthcheck.component';
import { GrafanaDashboardComponent } from '@insights/app/modules/grafana-dashboard/grafana-dashboard.component';
import { HomeComponent } from '@insights/app/modules/home/home.component';
import { PageNotFoundComponent } from '@insights/app/modules/page-not-found/page-not-found.component';
import { AgentManagementComponent } from '@insights/app/modules/admin/agent-management/agent-management.component';
import { DatadictionaryComponent } from '@insights/app/modules/datadictionary/datadictionary.component';
import { BusinessMappingComponent } from '@insights/app/modules/admin/businessmapping/businessmapping.component';
import { DataArchivingComponent } from '@insights/app/modules/settings/dataarchiving/dataarchiving.component';
import { AgentConfigurationComponent } from '@insights/app/modules/admin/agent-management/agent-configuration/agent-configuration.component';
import { UserOnboardingComponent } from '@insights/app/modules/user-onboarding/user-onboarding.component';
import { LandingPageComponent } from '@insights/app/modules/landing-page/landing-page.component';
import { LogoSettingComponent } from '@insights/app/modules/settings/logo-setting/logo-setting.component';
import { RelationshipBuilderComponent } from '@insights/app/modules/relationship-builder/relationship-builder.component';
import { BulkUploadComponent } from '@insights/app/modules/bulkupload/bulkupload.component';
import { WebHookComponent } from '@insights/app/modules/webhook/webhook.component';
import { AuthGuardService as AuthGuard } from '@insights/common/auth-guard.service';
import { TraceabilityDashboardCompenent } from '@insights/app/modules/traceability/traceability-builder.component';
import { ReportConfigComponent } from '@insights/app/modules/reportmanagement/report-configuration/report-configuration.component';
import { ReportManagementComponent } from './reportmanagement/reportmanagement.component';
import { MLWizardComponent } from '@insights/app/modules/model-management/mlwizard/mlwizard.component';
import { PredictionComponent } from '@insights/app/modules/model-management/mlwizard/prediction/prediction.component';
import { ModelManagementComponent } from '@insights/app/modules/model-management/model-management.component';
import { KpiCreationComponent } from '@insights/app/modules/kpi-creation/kpi-creation.component';
import { KpiAdditionComponent } from '@insights/app/modules/kpi-addition/kpi-addition.component';
import { ContentConfigComponent } from '@insights/app/modules/content-config-list/content-config-list.component';
import { ContentConfigAddition } from './content-config-add/content-config-add.component';
import { ReportTemplateComponent } from './report-template/report-template-list.component';
import { ReportTemplateConfig } from './report-template/template-configuration/template-configuration.component';
import { ServerConfigurationComponent } from '@insights/app/modules/server-configuration/server-configuration.component';
import { FileSystemComponent } from './filesystem/file-system.component';
import { FileSystemConfigComponent } from './filesystem/file-system-configuration/filesystem-config.component';
import { DashboardPdfDownloadComponent } from './dashboard-pdf-download/dashboard-pdf-download.component';
import { DashboardListComponent } from './dashboard-pdf-download/dashboard-list/dash-list.component';
import { EditDashboardComponent } from './dashboard-pdf-download/edit-dashboard/edit-dashboardcomponent';
import { WorkflowTaskManagementComponent } from './workflow-task-management/workflow-task-management.component';
import { AddWorkflowTaskComponent } from './workflow-task-management/add-workflow-task/add-workflow-task.component';
import { OutcomeComponent } from './outcome/outcome-config/outcome.component';
import { OutcomeListComponent } from './outcome/outcome-list/outcome-list.component';
import { OutcomeEditComponent } from './outcome/outcome-edit/outcome-edit.component';
import { MileStoneComponent } from './mile-stone/mile-stone-config/mile-stone.component';
import { MileStoneListComponent } from './mile-stone/mile-stone-list/mile-stone-list.component';
import { MileStoneEditComponent } from './mile-stone/mile-stone-edit/mile-stone-edit.component';
import { ScheduleTaskManagmentComponent } from '@insights/app/modules/schedule-task-managment/schedule-task-managment.component';
import { MultipleEmailConfigurationComponent } from './multiple-email-configuration/multiple-email-configuration.component';
import { AddComponent } from './multiple-email-configuration/dialogs/add/add.component';
import { OfflineDataListComponent } from '@insights/app/modules/offline-data-processing/offline-data-list/offline-data-list.component';
import { OfflineConfigurationComponent } from '@insights/app/modules/offline-data-processing/offline-configuration/offline-configuration.component';
import { OfflineDetailsComponent } from '@insights/app/modules/offline-data-processing/offline-details/offline-details.component';
import { OfflineAlertListComponent } from '@insights/app/modules/offline-alerting/offline-alert-list/offline-alert-list.component';
import { OfflineAlertConfigurationComponent } from '@insights/app/modules/offline-alerting/offline-alert-configuration/offline-alert-configuration.component';


const homeRoutes: Routes = [
  {
    path: 'InSights/Home', component: HomeComponent,
    children: [
      { path: 'playlist', component: PlaylistComponent },
      { path: 'grafanadashboard', component: GrafanaDashboardComponent },   
      { path: 'healthcheck', component: HealthCheckComponent },
      { path: 'loggedout', redirectTo: 'login' },
      { path: 'agentmanagement', component: AgentManagementComponent },
      { path: 'datadictionary', component: DatadictionaryComponent },
      { path: 'businessmapping', component: BusinessMappingComponent },
      { path: 'dataarchiving', component: DataArchivingComponent },
      { path: 'agentconfiguration', component: AgentConfigurationComponent },
      { path: 'accessGroupManagement', component: UserOnboardingComponent },
      { path: 'landingPage/:id', component: LandingPageComponent },
      { path: 'logoSetting', component: LogoSettingComponent },
      { path: 'relationship-builder', component: RelationshipBuilderComponent },
      { path: 'bulkupload', component: BulkUploadComponent },
      { path: 'webhook', component: WebHookComponent },
      { path: 'reportmanagement', component: ReportManagementComponent },
      { path: 'report-configuration', component: ReportConfigComponent },
      { path: 'traceability', component: TraceabilityDashboardCompenent },
      { path: 'mlwizard', component: MLWizardComponent },
      { path: 'prediction', component: PredictionComponent },
      { path: 'modelmanagement', component: ModelManagementComponent },
      { path: 'kpicreation', component: KpiCreationComponent },
      { path: 'kpiaddition', component: KpiAdditionComponent },
      { path: 'contentConfig', component: ContentConfigComponent },
      { path: 'contentConfigAdd', component: ContentConfigAddition },
      { path: 'reportTemplate', component: ReportTemplateComponent },
      { path: 'template-configuration', component: ReportTemplateConfig },
      { path: 'server-configuration', component: ServerConfigurationComponent },
      { path: 'filesystem', component: FileSystemComponent },
      { path: 'file-system-configuration', component: FileSystemConfigComponent },
      { path:'dash-pdf-download',component:DashboardListComponent},
      { path:'dash-pdf-config',component:DashboardPdfDownloadComponent},
      { path:'edit-dashboard',component:EditDashboardComponent},
      {path:'workflow-task-management',component:WorkflowTaskManagementComponent},
      {path:'workflow-configuration' , component:AddWorkflowTaskComponent},
      { path:'edit-dashboard',component:EditDashboardComponent},
      { path:'outcome', component:OutcomeComponent},
      { path:'fetchOutcome', component:OutcomeListComponent },
      { path:'editOutcome', component: OutcomeEditComponent},
      { path:'milestone', component:MileStoneComponent},
      { path:'fetchMileStone', component:MileStoneListComponent },
      { path:'editMileStone', component:MileStoneEditComponent},
      {path:'workflow-configuration' , component:AddWorkflowTaskComponent},
      { path:'edit-dashboard',component:EditDashboardComponent},
      { path:'taskManagement',component:ScheduleTaskManagmentComponent},
      { path : 'email-configuration',component : MultipleEmailConfigurationComponent },
      { path: 'add-component', component: AddComponent },
      { path: 'offlineDataList', component:OfflineDataListComponent },
      { path: 'offlineConfiguration', component:OfflineConfigurationComponent },
      { path: 'offlineAlertingList', component:OfflineAlertListComponent },
      { path: 'offlineAlertingConfig', component:OfflineAlertConfigurationComponent}
    ],
    canActivate: [AuthGuard]
  }
];

export const HomeRouting: ModuleWithProviders<RouterModule> = RouterModule.forChild(homeRoutes);