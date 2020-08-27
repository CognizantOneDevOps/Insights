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

import { NgModule } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTreeModule } from '@angular/material/tree';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ClipboardModule } from 'ngx-clipboard';

import { DragulaModule } from 'ng2-dragula';
import {
    MatButtonModule,
    MatMenuModule,
    MatToolbarModule,
    MatNativeDateModule,
    MatTableModule,
    MatIconModule,
    MatListModule,
    MatCardModule,
    MatInputModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatSidenavModule,
    MatSelectModule,
    MatGridListModule,
    MatRadioModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressBarModule,
    MatSlideToggleModule,
} from '@angular/material';
import { from } from 'rxjs';
@NgModule({
    imports: [],
    exports: [
        MatButtonModule,
        MatMenuModule,
        MatToolbarModule,
        MatNativeDateModule,
        MatTableModule,
        MatIconModule,
        MatListModule,
        MatCardModule,
        MatInputModule,
        MatDialogModule,
        MatProgressSpinnerModule,
        MatCheckboxModule,
        MatSidenavModule,
        MatGridListModule,
        MatSelectModule,
        MatRadioModule,
        MatTabsModule,
        MatTreeModule,
        MatPaginatorModule,
        MatSortModule,
        MatTooltipModule,
        MatProgressBarModule,
        ClipboardModule,
        MatSlideToggleModule,
        DragulaModule,


    ],
    declarations: []
})
export class MaterialModule { }