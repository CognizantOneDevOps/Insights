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
import { AdminComponent } from '@insights/app/modules/admin/admin.component';
import { HealthCheckComponent } from '@insights/app/modules/healthcheck/healthcheck.component';
import { GrafanaDashboardComponent } from '@insights/app/modules/grafana-dashboard/grafana-dashboard.component';
import { HomeComponent } from '@insights/app/modules/home/home.component';
import { PageNotFoundComponent } from '@insights/app/modules/page-not-found/page-not-found.component';
import { AgentManagementComponent } from '@insights/app/modules/admin/agent-management/agent-management.component';
import { DatadictionaryComponent } from '@insights/app/modules/datadictionary/datadictionary.component';
import { BusinessMappingComponent } from '@insights/app/modules/admin/businessmapping/businessmapping.component';
import { DataArchivingComponent } from '@insights/app/modules/settings/dataarchiving/dataarchiving.component';
import { AgentConfigurationComponent } from '@insights/app/modules/admin/agent-management/agent-configuration/agent-configuration.component';
import { BlockChainComponent } from '@insights/app/modules/blockchain/blockchain.component';
import { UserOnboardingComponent } from '@insights/app/modules/user-onboarding/user-onboarding.component';
import { LandingPageComponent } from '@insights/app/modules/landing-page/landing-page.component';
import { LogoSettingComponent } from '@insights/app/modules/settings/logo-setting/logo-setting.component';

const homeRoutes: Routes = [
  {
    path: 'InSights/Home', component: HomeComponent,
    children: [
      { path: 'playlist', component: PlaylistComponent },
      { path: 'admin', component: AdminComponent },
      { path: 'grafanadashboard/:id', component: GrafanaDashboardComponent },
      { path: 'blockchain', component: BlockChainComponent },
      { path: 'healthcheck', component: HealthCheckComponent },
      { path: 'loggedout', redirectTo: 'login' },
      { path: 'agentmanagement', component: AgentManagementComponent },
      { path: 'datadictionary', component: DatadictionaryComponent },
      { path: 'businessmapping', component: BusinessMappingComponent },
      { path: 'dataarchiving', component: DataArchivingComponent },
      { path: 'agentconfiguration', component: AgentConfigurationComponent },
      { path: 'accessGroupManagement', component: UserOnboardingComponent },
      { path: 'landingPage', component: LandingPageComponent },
      { path: 'logoSetting', component: LogoSettingComponent }
    ]
  }
];

export const HomeRouting: ModuleWithProviders = RouterModule.forChild(homeRoutes);