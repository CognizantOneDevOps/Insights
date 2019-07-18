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
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatPaginator, MatTableDataSource, MatSort } from '@angular/material';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { BlockChainService } from '@insights/app/modules/blockchain/blockchain.service';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { AssetData } from '@insights/app/modules/blockchain/blockchain.component';
import {saveAs as importedSaveAs} from "file-saver"; 
import { InsightsInitService } from '../../common.services/insights-initservice';

export interface AssetHistoryData {
    assetID: string;
    phase: string;
    toolstatus: string;
    toolName: string;
    //basePrimeID: string;
    author: string;
    timestamp: string;
}

@Component({
    selector: 'bc-asset-details-dialog',
    templateUrl: './bc-asset-details-dialog.html',
    styleUrls: ['./bc-asset-details-dialog.css'],
    animations: [
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
    MAX_ROWS_PER_TABLE = 10;
    assetID: string = "";
    @ViewChild(MatSort) sort: MatSort;
    @ViewChild(MatPaginator) paginator: MatPaginator;
    expandedElement: AssetHistoryData | null;
    headerArrayDisplay = [];
    masterHeader = new Map<String, String>();
    finalHeaderToShow = new Map<String, String>();
    headerSet = new Set();
    pdfData;
    displayProgressBar = false;
    pipeline = false;
    list = [];
    showModel = null;
    pipeheight=0;
    searchInput = '';
    innerObjAsset = {};
    expandObjects = [];
    order = [];
    
    constructor(public dialogRef: MatDialogRef<AssetDetailsDialog>,
        @Inject(MAT_DIALOG_DATA) public parentData: any,
        private blockChainService: BlockChainService,
        private changeDet: ChangeDetectorRef) {
            
    }

    ngOnInit() {
        this.assetID = this.parentData.assetID;
        this.order = this.parentData.tools;
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
                console.log("asset history respose>>>");
                console.log(data);
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
                console.log(historyData);
                // historyData.sort((value1,value2)=> {
                //     // Ascending order
                //     return(new Date(value1.timestamp).getTime() - new Date(value2.timestamp).getTime());
                // });
                // Assign asset history details data sorted by timestamp in ascending order
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
                                innerObj['value'] = eachObject[key];// {key: 'assetId', value: 'TXN'}
                                obj.push(innerObj); // { assetId: {key: 'assetId', value: 'TXN'} }
                            }
                            // this.headerSet.add(key);
                        }
                    }
                    eachObject['innerObjAsset'] = obj; // [{asstId: 'sadf', innerObjAsset: [ {key: 'assetId', value: 'TXN'}]}]
                    this.headerArrayDisplay.push(eachObject);

                }
                console.log(this.headerArrayDisplay);//[{assetId: 'TRC-14', innerObjAsset:{as: 'asdf', name: 'sdf'}}]
                // this.headerArrayDisplay = Array.from(this.headerSet);                
                this.assetHistoryDataSource.sort = this.sort;
                this.assetHistoryDataSource.paginator = this.paginator;
            });
    }

    clickRow(index,TxID) {
        this.expandObjects = [];
        console.log(index , TxID);
        this.headerArrayDisplay.forEach(x=>{
            if(x.TxID == TxID){
                this.expandObjects.push(x.innerObjAsset);
            }
        });
        this.changeDet.detectChanges();
        console.log(this.expandObjects);
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
                console.log(error);
                });              
    }

    applyAssetDetailsFilter() {
        this.assetHistoryDataSource.filter = this.searchInput.trim().toLowerCase();
    }  


    workflow(){
        this.pipeline = !this.pipeline;
        this.drawPipe();
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
        console.log("custMap",custMap);

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

        console.log("orderlst",orderlst);
        console.log("processorder",this.order);
        
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
        console.log('pipeheight', this.pipeheight);
        console.log('lists', this.list);

    }
    
    sortArray(list) {
        return list.sort((x, y) => {
            return x.moddate - y.moddate;
        })
    } 

    eventGet(index) {
        console.log(index);
        this.showModel = index;
    }

    eventLeave() {
        this.showModel = null;
    }
}    