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

import { Component, HostBinding, Input, OnInit } from '@angular/core';
import { NavItem } from '@insights/app/modules/home/nav-item';
import { Router } from '@angular/router';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { HomeComponent } from '@insights/app/modules/home/home.component';

@Component({
  selector: 'app-menu-list-item',
  templateUrl: './menu-list-item.component.html',
  styleUrls: ['../home/home.component.css'],
  animations: [
    trigger('indicatorRotate', [
      state('collapsed', style({ transform: 'rotate(0deg)' })),
      state('expanded', style({ transform: 'rotate(180deg)' })),
      transition('expanded <=> collapsed',
        animate('225ms cubic-bezier(0.4,0.0,0.2,1)')
      ),
    ])
  ]
})
export class MenuListItemComponent implements OnInit {
  expanded: boolean;
  @HostBinding('attr.aria-expanded') ariaExpanded = this.expanded;
  @Input() item: NavItem;
  @Input() depth: number;
  @Input() isExpanded: boolean = false;
  @Input() selectedOrg: String;
  @Input() showAdminTab: boolean = true;
  @Input() leftNavWidthpx: number;


  constructor(public router: Router, private homeController: HomeComponent) {
    if (this.depth === undefined) {
      this.depth = 0;
    }
  }

  ngOnInit() {

  }
  
   getNavItemsChildernByFilter(navChildItems) {
    return navChildItems.filter(item => (
      item.showMenu == true
    )
    );
  }


  onItemSelected(item: NavItem) {
    if (item.children && item.children.length) {
      /*if (item.iconName == 'grafana') {
        console.log("If grafana item then open dashboard on click " + JSON.stringify(item));
        this.homeController.onItemSelected(item);
        this.expanded = !this.expanded;
      } else {*/
      this.expanded = !this.expanded;
      /* }*/
    } else if (!item.children || !item.children.length) {
      this.homeController.onItemSelected(item);
    }
  }
}
