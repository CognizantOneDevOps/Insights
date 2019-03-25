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
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material';
import { MatDatepickerModule } from '@angular/material/datepicker';


import { HomeRouting } from '@insights/app/modules/home.routing';
import { SharedServices } from '@insights/app/shared.services';

import { HomeComponent } from '@insights/app/modules/home/home.component';
import { PlaylistComponent } from '@insights/app/modules/playlist/playlist.component';
import { AdminComponent } from '@insights/app/modules/admin/admin.component';
import { MenuListItemComponent } from '@insights/app/modules/menu-list-item/menu-list-item.component';
import { GrafanaDashboardComponent } from '@insights/app/modules/grafana-dashboard/grafana-dashboard.component';
import { PageNotFoundComponent } from '@insights/app/modules/page-not-found/page-not-found.component';
import { HealthCheckComponent } from '@insights/app/modules/healthcheck/healthcheck.component';
import { ShowDetailsDialog } from '@insights/app/modules/healthcheck/healthcheck-show-details-dialog';
import { AboutDialog } from '@insights/app/modules/about/about-show-popup';
import { BlockChainComponent } from '@insights/app/modules/blockchain/blockchain.component';
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
import { BlockChainService } from '@insights/app/modules/blockchain/blockchain.service';
import { DatePipe } from '@angular/common';
import { UserOnboardingService } from '@insights/app/modules/user-onboarding/user-onboarding-service';
import { StorageServiceModule } from 'ngx-webstorage-service';
import { LogoSettingComponent } from '@insights/app/modules/settings/logo-setting/logo-setting.component';
import { AssetDetailsDialog } from '@insights/app/modules/blockchain/bc-asset-details-dialog';
import { LogoSettingService } from '@insights/app/modules/settings/logo-setting/logo-setting.service';
import { AssetPipe } from './blockchain/bc-asset-pipe.pipe';



@NgModule({
  declarations: [
    HomeComponent,
    PlaylistComponent,
    AdminComponent,
    MenuListItemComponent,
    GrafanaDashboardComponent,
    PageNotFoundComponent,
    HealthCheckComponent,
    ShowDetailsDialog,
    AboutDialog,
    AgentManagementComponent,
    DatadictionaryComponent,
    BusinessMappingComponent,
    DataArchivingComponent,
    AgentConfigurationComponent,
    BlockChainComponent,
    UserOnboardingComponent,
    LandingPageComponent,
    ConfirmationMessageDialog,
    ApplicationMessageDialog,
    AddGroupMessageDialog,
    LogoSettingComponent,
    AssetDetailsDialog,
    AssetPipe
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
    StorageServiceModule
  ],
  entryComponents: [
    ShowDetailsDialog,
    AboutDialog,
    ConfirmationMessageDialog,
    ApplicationMessageDialog,
    AddGroupMessageDialog,
    AssetDetailsDialog
  ],

  providers: [
    GrafanaAuthenticationService,
    GrafanaDashboardService,
    AgentService,
    HealthCheckService,
    DataDictionaryService,
    BusinessMappingService,
    DataArchivingService,
    UserOnboardingService,
    MessageDialogService,
    LogoSettingService,
    BlockChainService,
    DatePipe
  ]
})



export class HomeModules { }
