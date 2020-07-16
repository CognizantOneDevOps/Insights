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

import { Component, ViewChild, HostBinding, Input, ElementRef, ViewEncapsulation, AfterViewInit, OnInit, HostListener } from '@angular/core';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { DomSanitizer, BrowserModule, SafeUrl, SafeResourceUrl } from '@angular/platform-browser';
import { InsightsInitService } from '@insights/common/insights-initservice';

@Component({
  selector: 'app-playlist',
  templateUrl: './playlist.component.html',
  styleUrls: ['./playlist.component.css', './../home.module.css']
})
export class PlaylistComponent implements OnInit {
  playListUrl: SafeResourceUrl;
  framesize: any;
  constructor(private restCallHandlerService: RestCallHandlerService, private sanitizer: DomSanitizer) {
    var self = this;
    self.setScrollBarPosition();
    var offset = 1; // 5 5px is app-grafana-dashboard height
    this.framesize = window.frames.innerHeight; //- offset

    var receiveMessage = function (evt) {
      var height = parseInt(evt.data);
      if (!isNaN(height)) {
        self.framesize = (evt.data + 20);
      }
    }
    window.addEventListener('message', receiveMessage, false);
    console.log(this.framesize);
    self.playListUrl = sanitizer.bypassSecurityTrustResourceUrl(InsightsInitService.grafanaHost + '/dashboard/script/iSight_ui3.js?url=' + InsightsInitService.grafanaHost + '/playlists');
  }

  setScrollBarPosition() {
    var self = this;
    this.framesize = window.frames.innerHeight;
    var receiveMessage = function (evt) {
      var height = parseInt(evt.data);
      if (!isNaN(height)) {
        self.framesize = (evt.data + 20);
      }
    }
    window.addEventListener('message', receiveMessage, false);
    setTimeout(function () {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 1000);
  }
  ngOnInit() {
    //console.log("playlist init");
    var self = this;
    //self.setScrollBarPosition();
  }

}