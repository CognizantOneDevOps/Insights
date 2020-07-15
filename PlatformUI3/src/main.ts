/*
*******************************************************************************
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
 *******************************************************************************/

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { InsightsAppModule } from './app/insights.module';
import { environment } from './environments/environment';
import { InsightsInitService } from '@insights/common/insights-initservice';

if (environment.production) {
  enableProdMode();
  if (window) {
    if (InsightsInitService.isDebugModeEnable) {
      console.log(" debug mode enable ")
    } else {
      console.log(" debug mode disable " + InsightsInitService.isDebugModeEnable)
      window.console.log = function () { };
    }
    window.onbeforeunload = function () { return "Back button is not available!"; window.history.forward(1); };
  }
}

if (environment) {
  window.onbeforeunload = function () { return "Back button is not available!"; window.history.forward(1); };
}

platformBrowserDynamic().bootstrapModule(InsightsAppModule)
  .catch(err => console.log(err));

