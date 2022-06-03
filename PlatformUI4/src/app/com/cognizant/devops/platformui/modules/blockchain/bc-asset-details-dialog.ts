/*********************************************************************************
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
import { Component, OnInit, Inject, ViewChild, ChangeDetectorRef } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { BlockChainService } from '@insights/app/modules/blockchain/blockchain.service';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { saveAs as importedSaveAs } from "file-saver";
import { DataSharedService } from '@insights/common/data-shared-service';

export interface AssetHistoryData {
    assetID: string;
    phase: string;
    toolstatus: string;
    toolName: string;
    author: string;
    timestamp: string;
}

@Component({
    selector: 'bc-asset-details-dialog',
    templateUrl: './bc-asset-details-dialog.html',
    styleUrls: ['./bc-asset-details-dialog.scss', './../home.module.scss'],
    animations: [
        trigger('indicatorRotate', [
            state('collapsed', style({ transform: 'rotate(0deg)' })),
            state('expanded', style({ transform: 'rotate(180deg)' })),
            transition('expanded <=> collapsed',
                animate('225ms cubic-bezier(0.4,0.0,0.2,1)')
            ),
        ]),
        trigger('detailExpand', [
            state('collapsed, void', style({ height: '0px', minHeight: '0', display: 'none' })),
            state('expanded', style({ height: '*' })),
            transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
            transition('expanded <=> void', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)'))
        ]),
    ]
})
export class AssetDetailsDialog implements OnInit {
    displayedColumns: string[] = ['select', 'assetID', 'phase', 'toolstatus', 'toolName', 'author', 'timestamp'];;
    assetHistoryDataSource = new MatTableDataSource<AssetHistoryData>([]);
    MAX_ROWS_PER_TABLE = 5;
    assetID: string = "";
    @ViewChild(MatSort, { static: true }) sort: MatSort;
    @ViewChild(MatPaginator) paginator: MatPaginator;
    expandedElement: AssetHistoryData | null;
    headerArrayDisplay = [];
    masterHeader = new Map<String, String>();
    finalHeaderToShow = new Map<String, String>();
    headerSet = new Set();
    pdfData;
    displayProgressBar = false;
    pipeline = false;
    isrowExpand = false;
    list = [];
    showModel = null;
    pipeheight = 0;
    searchInput = '';
    innerObjAsset = {};
    expandObjects = [];
    order = [];
    viewToggle = "tableView";
    timeZone: string = "";
    timeZoneAbbr: string = "";
    currentPageIndex: number = 1;
    totalPages: number = -1;

    constructor(public dialogRef: MatDialogRef<AssetDetailsDialog>, @Inject(MAT_DIALOG_DATA) public parentData: any,
        private blockChainService: BlockChainService, private changeDet: ChangeDetectorRef,
        public dataShare: DataSharedService) {

    }

    ngOnInit() {
        this.timeZone = this.dataShare.getTimeZone();
        this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
        this.assetID = this.parentData.assetID;
        this.order = this.parentData.tools;
        this.assetHistoryDataSource.sort = this.sort;
        this.assetHistoryDataSource.paginator = this.paginator;
        this.fillMasterHeaderData();
        this.getAssetHistoryDetails();
    }

    ngAfterViewInit() {
        this.assetHistoryDataSource.sort = this.sort;
        this.assetHistoryDataSource.paginator = this.paginator;
    }

    fillMasterHeaderData() {
        this.masterHeader.set("select", "");
        this.masterHeader.set("assetID", "Asset ID");
        this.masterHeader.set("phase", "Phase");
        this.masterHeader.set("toolstatus", "Status");
        this.masterHeader.set("toolName", "Tool");
        this.masterHeader.set("author", "Owner");
        this.masterHeader.set("timestamp", "Time Stamp");
    }

    getAssetHistoryDetails() {

        this.displayProgressBar = true;
        this.blockChainService.getAssetHistory(encodeURIComponent(this.parentData.assetID))
            .then((data) => {
                this.displayProgressBar = false;
                let result = data.data;
                let historyData = [];
                result.map((d) => {
                    Object.keys(d).forEach(k => {
                        const matchKey = k.match('AssetID');
                        if (matchKey) {
                            d['assetID'] = d[k];
                        }

                    })
                    historyData.push(d);
                });
                this.assetHistoryDataSource.data = historyData;
                this.pdfData = data;
                for (var index in historyData) {
                    var eachObject = historyData[index];
                    let obj = [];
                    for (var key in eachObject) {
                        let innerObj = {
                            key: key,
                            value: ''
                        };
                        if (!this.masterHeader.has(key)) {
                            if (eachObject[key] != 'undefined' && eachObject[key] != null && eachObject[key] != '') {
                                innerObj['value'] = eachObject[key];
                                obj.push(innerObj);
                            }
                        }
                    }
                    eachObject['innerObjAsset'] = obj;
                    this.headerArrayDisplay.push(eachObject);

                }
                this.assetHistoryDataSource.sort = this.sort;
                this.assetHistoryDataSource.paginator = this.paginator;
                this.totalPages = Math.ceil(this.assetHistoryDataSource.data.length / this.MAX_ROWS_PER_TABLE);

            });
    }

    clickRow(index, TxID) {
        this.expandObjects = [];
        this.headerArrayDisplay.forEach(x => {
            if (x.TxID == TxID) {
                this.expandObjects.push(x.innerObjAsset);
            }
        });
        this.changeDet.detectChanges();
    }

    closeAssetDetailsDialog() {
        this.dialogRef.close();
    }

    exportToPdf() {
        this.blockChainService.exportToPdf(this.pdfData)
            .subscribe((data) => {
                var pdfFileName = 'Traceability_report.pdf';
                importedSaveAs(data, pdfFileName);
            },
                error => {
                    console.error(error);
                });
    }

    applyAssetDetailsFilter() {
        this.assetHistoryDataSource.filter = this.searchInput.trim().toLowerCase();
        this.totalPages = Math.ceil(this.assetHistoryDataSource.paginator.length / this.MAX_ROWS_PER_TABLE);
    }


    workflow() {
        this.pipeline = !this.pipeline;
        this.drawPipe();
    }

    tableView() {
        this.pipeline = !this.pipeline;
    }

    drawPipe() {
        this.list = [];
        let custMap = {};
        this.pdfData.data.map(x => {
            x["moddate"] = new Date(x.timestamp);
            if (custMap[x.toolName]) {
                let list = [...custMap[x.toolName]];
                list.push(x);
                custMap[x.toolName] = this.sortArray(list);

            } else {
                let lst = []
                lst.push(x)
                custMap[x.toolName] = lst
            }
        });
        let orderlst = [];
        let clst = custMap;
        Object.keys(clst).forEach((s) => {
            let obj = {
                point: s,
                child: []
            }
            orderlst.push(obj);
        })
        orderlst.forEach((a) => {
            clst[a.point].forEach((s) => {
                if (a.child.length === 0) {
                    a.child.push({ point: s });
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


        // let processorder = ["JIRA", "GIT", "JENKINS", "NEXUS"];
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

    onExpanIconClick(expandedElement, row) {
        this.isrowExpand = !this.isrowExpand;
        return expandedElement === row ? null : row;
    }

    goToNextPage() {
        this.paginator.nextPage();
        this.currentPageIndex = this.paginator.pageIndex + 1;
    }
    goToPrevPage() {
        this.paginator.previousPage();
        this.currentPageIndex = this.paginator.pageIndex + 1;
    }
}