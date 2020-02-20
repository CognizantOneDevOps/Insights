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

import { Routes, RouterModule } from '@angular/router';
import { ModuleWithProviders } from '@angular/core';
import { LoginComponent } from '@insights/app/login/login.component';
import { SSOLoginComponent } from '@insights/app/com/cognizant/devops/platformui/ssologin/ssologin.component';
import { PageNotFoundComponent } from '@insights/app/modules/page-not-found/page-not-found.component';
import { LogoutHandlerComponent } from '@insights/app/com/cognizant/devops/platformui/logout-handler/logout-handler.component.ts';
import { AuthGuardService as AuthGuard } from '@insights/common/auth-guard.service';

const appRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'ssologin', component: SSOLoginComponent },
  { path: 'logout/:id', component: LogoutHandlerComponent },
  { path: '**', component: PageNotFoundComponent },
  { path: 'Insights/Home', loadChildren: '@insights/app/modules/home.modules#HomeModules', canActivate: [AuthGuard]  }
];

export const InsightsModuleRouting: ModuleWithProviders = RouterModule.forRoot(appRoutes, { useHash: true });//{ useHash: true  , onSameUrlNavigation: 'reload' , enableTracing: true }