
/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
import { Component, Inject } from '@angular/core';
import { DragulaService } from 'ng2-dragula';
import { ReportManagementService } from '@insights/app/modules/reportmanagement/reportmanagement.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { Subscription } from 'rxjs';

interface Iitem {
    description: string;
    taskId: Number;
    sequence: number;
}
interface Igroup {
    name: string;
    position: number;
    items: Iitem[];
}
@Component({
    selector: 'add-task',
    templateUrl: 'add-task.html',
    styleUrls: ['add-task.css', './../../home.module.css']
})

export class AddTasksDialog {

    // these are some basics to get you started -- modify as you see fit.

    listOftasks = [];
    subs = new Subscription();
    responseOfTasklist: any;
    selectedTaskList = [{ taskId: 0, description: "", workflow: "" }];
    dataSource: string;
    taskListTobeSaved = [];
    taskidInOrder = [];
    displayedTaskColumns: any = []
    previousSourceModel = []
    currentSourceModel = [];
    currentTargetModel = [];
    previousTargetModel = [];
    groups: Array<any>
    selectSourceModel: boolean
    targetTasks = [];

    constructor(public reportmanagemnetService: ReportManagementService, private dragulaService: DragulaService, public dialogRef: MatDialogRef<AddTasksDialog>, @Inject(MAT_DIALOG_DATA) public data: any) {
        // use these if you want
        this.displayedTaskColumns = ['toolproperties']
        this.taskidInOrder = [];
        this.targetTasks = data.message;
        this.getListOfTasks();
        this.dragulaService.destroy('TASKS');

        this.dragulaService.createGroup("TASKS", {
            // ...
        });

        const renumbering = (item: Iitem, newsequence: number, sourceModel: Iitem[], targetModel: Iitem[]) => {
            this.taskListTobeSaved = [];
            // Renumbering source
            sourceModel.filter(o => o.description !== item.description).forEach(_item => _item.sequence = _item.sequence < item.sequence ? _item.sequence : _item.sequence - 1);
            // Renumbering target
            item.sequence = newsequence;
            targetModel.filter(o => o.description !== item.description).forEach(_item => _item.sequence = _item.sequence < item.sequence ? _item.sequence : _item.sequence + 1);
            if (this.selectSourceModel == false) {
                this.taskListTobeSaved = targetModel
            }
            else {
                this.taskListTobeSaved = sourceModel
            }

        };

        this.subs.add(dragulaService.dropModel<Iitem>("TASKS")
            .subscribe(({ name, el, target, source, sourceModel, targetModel, item }) => {
                if (el.id == "fromAllTaskList") {
                    this.selectSourceModel = false;
                }
                else {
                    this.selectSourceModel = true;
                }
                let sequence: number = targetModel.indexOf(item);
                renumbering(item, sequence, sourceModel, targetModel);
            })
        );
    }

    closeShowDetailsDialog(): void {
        this.dialogRef.close();
    }

    async getListOfTasks() {
        this.responseOfTasklist = await this.reportmanagemnetService.getTasksList("Report");
        this.listOftasks = this.responseOfTasklist.data;
        if (this.targetTasks != null) {
            this.targetTasks.forEach((element) => {
                this.listOftasks = this.listOftasks.filter((element1) => {
                    if (element1.taskId !== element.taskId) {
                        return true
                    }
                    else {
                        return false;
                    }
                })
            })
            var i = 0;
            for (var element of this.targetTasks) {
                element['sequence'] = i;
                i++;
            }
        }
    }

    savetasks() {
        if(this.taskListTobeSaved.length > 0) {
            this.dialogRef.close(this.taskListTobeSaved);
        } else {
            this.dialogRef.close(this.targetTasks);
        }
        
    }
}
