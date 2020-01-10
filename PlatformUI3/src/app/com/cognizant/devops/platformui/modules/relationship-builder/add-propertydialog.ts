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
import { Component, OnInit, Inject, ViewChild, Injector, EventEmitter, Output } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatPaginator, MatTableDataSource } from '@angular/material';
import { FormBuilder, FormGroup, FormArray } from '@angular/forms';


@Component({
    selector: 'add-propertydialog',
    templateUrl: './add-propertydialog.html',
    styleUrls: ['./relationship-builder.component.css'],

})


export class AddPropertyDialog implements OnInit {
    propertyForm: FormGroup;
    propertylist = [];

    constructor(private fb: FormBuilder, public dialogRef: MatDialogRef<AddPropertyDialog>,
        @Inject(MAT_DIALOG_DATA) public data: any) {

    }

    ngOnInit() {
        this.propertyForm = this.fb.group({
            title: [],
            property_points: this.fb.array([this.fb.group({ point: '' })])
        })
    }

    getFinalList() {

        for (var x of this.propertyForm.value.property_points) {
            var propertyname = (x.point)
            this.propertylist.push(propertyname)
        }
        this.dialogRef.close(this.propertylist)
    }

    get property_points() {
        return this.propertyForm.get('property_points') as FormArray;
    }

    addPropertyPoints() {
        this.property_points.push(this.fb.group({ point: '' }));
    }

    deletePropertyPoints(index) {
        console.log(index)
        this.property_points.removeAt(index);
    }

    closeShowDetailsDialog(): void {
        this.dialogRef.close();
    }


}
