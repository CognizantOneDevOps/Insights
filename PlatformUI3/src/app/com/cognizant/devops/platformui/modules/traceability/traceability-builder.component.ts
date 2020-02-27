import { Component, OnInit } from '@angular/core';
import { TraceabiltyService } from './traceablity-builder.service';
import { MatTableDataSource, MatSort, MatPaginator, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ShowTraceabiltyDetailsDialog } from './traceabilty-show-details-dialog';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { DataSharedService } from '@insights/common/data-shared-service';

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
    resultSummary: any;
    toolSummaryArray = [];
    timeZone: string = "";
    toolTimelagArray = [];
    isEnable = false;
    timelagData: {}
    constructor(private dialog: MatDialog, public messageDialog: MessageDialogService,
        private traceablityService: TraceabiltyService, public dataShare: DataSharedService) {
    }

    ngOnInit() {
        this.timeZone = this.dataShare.getTimeZone()
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
        this.list = [];
        this.toolSummaryArray = [];
        this.isEnable = false;
        this.getAssetHistoryDetails(this.selectedTool, this.selectedField, this.fieldValue);

    }

    getAssetHistoryDetails(toolName: string, toolField: string, toolValue: string) {
        this.isDatainProgress = true;
        this.cachestring = toolName + "." + toolField + "." + toolValue;
        var self = this;
        this.traceablityService.getAssetHistory(toolName, toolField, toolValue)
            .then((data) => {
                if (data.status == "success") {
                    if (data.data.pipeline.length != 0) {
                        for (var element of data.data.pipeline) {
                            this.order[(element.order) - 1] = element.toolName;
                        }
                        let result = data.data.pipeline;
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
                        if (data.data.summary.length > 0) {

                            this.toolSummaryArray = [];
                            var tempArr = []
                            for (let orderredTool in this.order) {
                                var toolName = this.order[orderredTool]
                                var toolData = data.data.summary[0][toolName]
                                if (toolData != undefined) {
                                    tempArr.push(toolData)
                                }
                            }
                            var summaryLen = tempArr.length;
                            for (let index = 0; index < summaryLen; index++) {
                                var object = tempArr[index]
                                var obejctString = JSON.stringify(object);
                                var objectLen = obejctString.split(",").length
                                for (var i = 0; i < objectLen; i++) {
                                    this.toolSummaryArray.push(tempArr[index][i])
                                }
                            }
                        }
                        if (data.data.timelag.length > 0) {
                            var tempArr = []
                            for (var element of data.data.timelag) {
                                for (var key in element) {
                                    this.timelagData = { 'Tools': key, 'HandoverTime': element[key] }
                                    tempArr.push(this.timelagData)
                                }
                            }

                            this.toolTimelagArray = tempArr

                            if (this.toolTimelagArray.length > 0) {
                                this.isEnable = true;
                            }
                            console.log(this.toolTimelagArray)
                        }
                        this.traceabilityData = data;
                        this.workflow();
                        this.isDatainProgress = false;
                    }
                    else {
                        this.messageDialog.showApplicationsMessage("No data found for the given selection.", "ERROR");
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
        this.traceabilityData.data.pipeline.map(element => {
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
            width: '65%',
            height: '70%',
            disableClose: true,
            data: { toolName: toolName, cachestring: this.cachestring, showToolDetail: true }
        });
    }

    showTimeDetailsDialog() {
        let showDetailsDialog = this.dialog.open(ShowTraceabiltyDetailsDialog, {
            panelClass: 'traceablity-timelag-dialog-container',
            width: '40%',
            disableClose: true,
            data: { toolName: "toolName", dataArr: this.toolTimelagArray, showToolDetail: false }
        });
    }

}