import { Component, OnInit } from '@angular/core';

import { TraceabiltyService } from './traceablity-builder.service';
import { strictEqual } from 'assert';
import { MatTableDataSource, MatSort, MatPaginator, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ShowTraceabiltyDetailsDialog } from './traceabilty-show-details-dialog';
import { async } from 'q';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { element } from '@angular/core/src/render3/instructions';

@Component({
    selector: 'app-traceabilitydashboard',
    templateUrl: './traceability-builder.component.html',
    styleUrls: ['./traceability-builder.component.css', './../home.module.css']
})

export class TraceabilityDashboardCompenent implements OnInit {
    isDatainProgress = false;
    selectedTool: string;
    selectedField: string;
    tools = [];
    fieldList = [];
    isToolSelected: boolean;
    isFieldSelected: boolean;
    list = [];
    cachestring: string;
    pipeheight = 0;
    traceabilityData: any;
    order = [];
    countrack = [];
    showModel = null;
    fieldValue: string;
    str: string;
    fieldPlaceVal: string;

    constructor(private dialog: MatDialog, public messageDialog: MessageDialogService, private traceablityService: TraceabiltyService) {
    }

    ngOnInit() {
        this.isToolSelected = true;
        this.isFieldSelected = true;
        this.traceablityService.getAvailableTools()
            .then((response) => {
                for (var x of response.data) {
                    this.tools.push(x)
                }
            });
    }

    getDetails() {
        this.getAssetHistoryDetails(this.selectedTool, this.selectedField, this.fieldValue);

    }

    getAssetHistoryDetails(toolName: string, toolField: string, toolValue: string) {
        this.isDatainProgress = true;
        this.cachestring = toolName + "." + toolField + "." + toolValue;
        this.traceablityService.getAssetHistory(toolName, toolField, toolValue)
            .then((data) => {
                if (data.status == "success") {
                    if (data.data.length != 0) {
                        for (var element of data.data) {
                            this.order[(element.order) - 1] = element.toolName;
                        }

                        let result = data.data;
                        let historyData = [];
                        result.map((resultmap) => {
                            Object.keys(resultmap).forEach(element => {
                                const matchKey = element.match('AssetID');
                                if (matchKey) {
                                    resultmap['assetID'] = resultmap[element];
                                }
                            })
                            historyData.push(resultmap);
                        });
                        this.traceabilityData = data;
                        this.workflow();
                        this.isDatainProgress = false;
                    }
                    else {
                        this.messageDialog.showApplicationsMessage("Data not present for the entered input.", "ERROR");
                        this.isDatainProgress = false;
                    }
                }

                else {
                    this.messageDialog.showApplicationsMessage(data.message, "ERROR");
                    this.isDatainProgress = false;
                }
            });
    }

    workflow() {
        this.drawPipe();

    }

    drawPipe() {
        this.list = [];
        let custMap = {};
        this.traceabilityData.data.map(element => {
            element["moddate"] = new Date(element.timestamp);
            if (custMap[element.toolName]) {
                let list = [...custMap[element.toolName]];
                list.push(element);
                custMap[element.toolName] = this.sortArray(list);

            } else {
                let lst = []
                var labelName = element.toolName + " " + element.count;
                lst.push(element)
                custMap[element.toolName] = lst

            }
        });

        let orderlst = [];
        let clst = custMap;
        Object.keys(clst).forEach((s) => {
            let obj = {
                point: s,
                count: 1,
                child: []
            }
            orderlst.push(obj);
        })
        orderlst.forEach((a) => {
            clst[a.point].forEach((s) => {
                if (a.child.length === 0) {
                    a.child.push({ point: s });
                    a.count = s.count;

                } else {
                    let fil = a.child.filter(c => c.point.assetID === s.assetID);
                    if (fil.length > 0) {
                        a.child.push({ point: s });
                    } else {
                        let checkFinal = false;
                        orderlst.forEach((b) => {
                            if (b.point === s.toolName) {
                                b.child.forEach(k => {
                                    if (!checkFinal && k.point.assetID === s.assetID) {
                                        b.child.push({ point: s });
                                        checkFinal = true;
                                    }
                                })
                            }
                        })
                        if (!checkFinal) {
                            let obj = {
                                point: a.point,
                                child: [{ point: s }]
                            }
                            orderlst.push(obj);
                        }
                    }
                }
            })
        })
        this.order.forEach(p => {
            orderlst.forEach(a => {
                if (p === a.point) {
                    this.list.push(a);
                }
            })
        })

        this.list.map((l) => {
            this.pipeheight = this.pipeheight < l.child.length ? l.child.length : this.pipeheight;
        })

    }

    sortArray(list) {
        return list.sort((x, y) => {
            return x.moddate - y.moddate;
        })
    }
    eventGet(index) {
        this.showModel = index;
    }

    eventLeave() {
        this.showModel = null;
    }

    toolOnChange(): void {
        this.fieldValue = null;
        this.fieldPlaceVal = null;
        this.selectedField = null;
        this.fieldList = [];
        this.traceablityService.getToolKeyset(this.selectedTool)
            .then((response) => {
                for (var x of response.data) {
                    this.fieldList.push(x)
                }
            });
        var self = this;
        self.isToolSelected = true;
        self.isFieldSelected = false;
    }

    FieldOnChange(key, type): void {
        this.fieldValue = null;
        var self = this;
        self.isFieldSelected = true;
        self.fieldPlaceVal = key + "_val";
    }

    showDetailsDialog(toolName: string) {
        let showDetailsDialog = this.dialog.open(ShowTraceabiltyDetailsDialog, {
            panelClass: 'traceablity-show-details-dialog-container',
            width: "900px",
            disableClose: true,
            data: { toolName: toolName, cachestring: this.cachestring }
        });
    }

}