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

import { AfterViewInit, Component, OnInit, Renderer2 } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { DataSharedService } from '@insights/common/data-shared-service';
import { ShowTraceabiltyDetailsDialog } from './traceabilty-show-details-dialog';
import { TraceabiltyService } from './traceablity-builder.service';
import { ImageHandlerService } from '@insights/common/imageHandler.service';
import { saveAs as importedSaveAs } from "file-saver";
import html2canvas from 'html2canvas';

@Component({
    selector: 'app-traceabilitydashboard',
    templateUrl: './traceability-builder.component.html',
    styleUrls: ['./traceability-builder.component.scss', './../home.module.scss']
})

export class TraceabilityDashboardCompenent implements OnInit, AfterViewInit {
    isDatainProgress = false;
    selectedTool: string;
    selectedField: string;
    tools = [];
    fieldList = [];
    isToolSelected: boolean;
    isFieldSelected: boolean;
    list = [];
    epiccachestring: string;
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
    toolTimelagArray: any;
    isEnable = false;
    timelagData: {}
    isEpic: boolean = false;
    issueTypeSelected: string;
    git = [];
    epicArr = [];
    sprintArr = [];
    releaseArr = [];
    jiraArr = [];
    jenkins = [];
    sonar = [];
    runDeck = [];
    showGit: boolean = false;
    epic: boolean;
    showAll: boolean;
    issueTypes: string[] = ['Other', 'Epic', 'Sprint', 'Release', 'Issue'];
    issueTypesDefault: string[] = ['Other', 'Epic', 'Sprint', 'Release', 'Issue'];
    issueTypesJIRA:any = {
            "key" : ["Epic", "Issue"],
            "sprintId" : ["Sprint"],
            "versionId" : ["Release"]
        
    }
    globalData: any;
    filteredData = [];
    toolNameArr = [];
    list1 = [];
    isSelected: string;
    selected = [false, false, false, false];
    selectedJira: number;
    selectedGit: number;
    selectedJenkin: number;
    selectedSonar: number;
    selectedRunDeck: number;
    traceabilityObj = {};
    map = new Map<String, Object[]>();
    globalMap = new Map<String, Object[]>();
    displayProperty = [];
    mapValues: any;
    formatedData: string;
    modalleft: any;
    modaltop: any;
    cacheMap = new Map<any, any>();
    sublData: any;
    showDiv: boolean;
    inputVal: any;
    displayButton: String = 'Show Summary'
    displaySummary: boolean;
    showSummary: boolean;
    columnsToDisplay: string[] = ['Tools', 'Handover Time'];
    timelagDataSource = new MatTableDataSource([]);
    toolData: any[];
    gChart: boolean;
    total: number;
    mapToolColor = new Map<String, Object[]>();
    listOfColor: any[] = ['#FAE0E1', '#D4F9DF', '#D4F0F0', '#FFFFE3', '#ACB1D6',  '#CBEBCA', '#F2FAE0', '#F9E0FA', '#EAE0FA']
    selectedTabIndex: number = 0
    isDownload = false;
    isCheckedSelectedSprint = true;
    showSprintCheckbox = false;

    constructor(private dialog: MatDialog, public messageDialog: MessageDialogService,
        private traceablityService: TraceabiltyService, private imageHandeler: ImageHandlerService, private renderer: Renderer2, public dataShare: DataSharedService) {
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
    ngAfterViewInit() {
        //this.tabClick();

    }

    downloadPdf() {
        if (this.isDownload) {
            this.createHTMLImage().then((imageString) => {
                this.getTraceabilityPDF(this.selectedTool, this.selectedField, this.fieldValue, this.issueTypeSelected, imageString);
            });
        } 
    }

    createHTMLImage(): Promise<String> {
        return new Promise((resolve, reject) => {
            let elementDoc = document.getElementById("traceabilityPDF");
            window.scrollTo(0, 0);
            elementDoc.style.overflow = 'visible';
            elementDoc.style.height = "auto";
            html2canvas(elementDoc, {
                allowTaint: true,
                useCORS: true,
                width: elementDoc.scrollWidth,
                height: elementDoc.scrollHeight,
            }).then((canvas) => {
                resolve(canvas.toDataURL('image/png', 1.0));
            });
            elementDoc.style.overflowY = 'auto';
            elementDoc.style.height = "inherit";
        });
    }

    getTraceabilityPDF(toolName: string, toolField: string, toolValue: string, type: string, imageSummary: any) {
        this.traceablityService.downloadTraceabiltyPDF(toolName, toolField, toolValue, type, imageSummary).then(function (data) {
            importedSaveAs(data, "Trace.pdf");
        });
    }

    getDetails() {
        this.isCheckedSelectedSprint = true;
        this.list = [];
        this.toolSummaryArray = [];
        this.map = new Map<String, Object[]>();
        this.cacheMap = new Map<String, Object[]>();
        this.isEnable = false;
        this.showDiv = false
        this.getToolDisplayProperties();
        if (this.issueTypeSelected == 'Epic') {
            this.getEpicIssuesDetails(this.selectedTool, this.selectedField, this.fieldValue, "Epic");
        }
        else if (this.issueTypeSelected == 'Sprint') {
            this.getSprintIssuesDetails(this.selectedTool, this.selectedField, this.fieldValue, "Sprint");
            this.showSprintCheckbox = true;
        }
        else if (this.issueTypeSelected == 'Release') {
            this.getReleaseIssuesDetails(this.selectedTool, this.selectedField, this.fieldValue, "Release");
            this.showSprintCheckbox = false;
        }
        else if (this.issueTypeSelected == 'Issue') {
            this.getAssetHistoryDetails(this.selectedTool, this.selectedField, this.fieldValue, "Issue");
        }
        else {
            this.getAssetHistoryDetails(this.selectedTool, this.selectedField, this.fieldValue, "Other");
        }


        
    }
    filterValues(data, key) {
        let searchKey = data;
        if (this.map.has(key)) {
            let val = JSON.parse(JSON.stringify(this.map.get(key)));
            if (val.includes(data)) {
                this.map.delete(key);
                this.map.set(key, val);
            }
        }

    }
    showButton() {
        if (this.displayButton === 'Show Summary') {
            this.displaySummary = true;
            this.displayButton = 'Show Graph';
        } else {
            this.displaySummary = false;
            this.displayButton = 'Show Summary'
        }
    }
    getToolDisplayProperties() {
        let response = this.traceablityService.getToolDisplayProperties();
        response.then((data) => {
            let res = data.data;
            this.displayProperty = res;
                this.displayProperty['Epic'] = ["epicName", "summary", "issueKey","toolstatus"];
                this.displayProperty['Sprint'] = ["sprintId", "name", "startDate", "endDate", "completeDate", "state"];
                this.displayProperty['Release'] = ["versionId","versionName", "released", "releaseDate"];
        });
    }
    getEpicIssuesDetails(toolName: string, toolField: string, toolValue: string, type: string) {
        this.isDatainProgress = true;
        let response = this.traceablityService.getEpicIssues(toolName, toolField, toolValue, "Epic");
        this.cachestring = toolName + "." + toolField + "." + toolValue;
        response.then((data) => {
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
                    this.prepareSummaryDetail(data.data.summary);

                    if (data.data.combinedSummary.length > 0) {
                        this.list1 = data.data.combinedSummary;
                    }
                    this.traceabilityData = data;
                    this.globalData = this.traceabilityData.data.pipeline;
                    this.constructObject(this.traceabilityData);
                    this.epic = true;
                    this.showAll = false;
                    this.epicArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'Epic');
                    this.jiraArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'JIRA');
                    this.workflow();
                    this.isDatainProgress = false;
                    this.isDownload = true;
                }
                else {
                    this.messageDialog.openSnackBar("No data found for the given selection.",'error');
                    //this.messageDialog.showApplicationsMessage("No data found for the given selection.", "ERROR");
                    this.isDatainProgress = false;
                    this.isDownload = false;
                }

            }
            else {
                this.messageDialog.showApplicationsMessage(data.message, "ERROR");
                this.isDatainProgress = false;
            }
        });
    }
    constructObject(data) {        
        data.data.pipeline.forEach(element => {
            this.showDiv = true;
            let key = element.order + "_" + element.toolName;
            if (this.map.has(key)) {
                this.map.get(key).push(element);
            }
            else {
                this.map.set(key, [element]);
            }
        });
        let ii = 0;
        for (let [key, value] of this.map.entries()) {
            if(ii > 3){
                ii = 0;
            }
            this.mapToolColor.set(key.split('_')[1],this.listOfColor[ii])
            ii = ii + 1
        }
        
        this.globalMap = this.map;        
    }
    prepareSummaryDetail(data){

        if (data.length > 0) {
            this.toolSummaryArray = [];
            var totalMessage = data[0]['Total']
            this.toolSummaryArray.push(totalMessage);
        }
    }
    onCardClickJira(index, event, key) {
        this.isCheckedSelectedSprint = false;
        this.showSprintCheckbox = false;
        this.showModel = null;
        this.selectedJira = index
        let clickedElement = key;
        clickedElement['searchKey'] = this.selectedField;
        if(clickedElement['toolName'] === 'Sprint') return;
        if(clickedElement['toolName'] === 'Release') return;
        this.isDatainProgress=true;
        this.cachestring = clickedElement['toolName'] + "." + clickedElement['searchKey'] + "." + clickedElement['issueKey'];
        
        if (this.map.size == 1) {
            this.messageDialog.openSnackBar("No further pipeline found for given selection.",'error');
            //this.messageDialog.showApplicationsMessage("No further pipeline found for given selection.", "ERROR");
            this.isDatainProgress = false;
        } else if (this.cacheMap.has(clickedElement.uuid)) {
            this.map = new Map<String, Object[]>();
            let data = this.cacheMap.get(clickedElement.uuid);
            data.forEach(element => {
                let key = element.order + "_" + element.toolName;
                if (this.map.has(key)) {
                    this.map.get(key).push(element);
                }
                else {
                    this.map.set(key, [element]);
                }
            });
            this.globalMap = this.map;
            this.isDatainProgress = false;
        } else {
            this.traceablityService.getIssuesPipeline(clickedElement)
                .then((data) => {
                   // setTimeout(()=>{this.isDatainProgress=true},8000); 
                   this.map = new Map<String, Object[]>();
                    let res = data.data.pipeline;
                    this.cacheMap.set(clickedElement.uuid, res);
                    this.globalData = res;
                    /* res.forEach(element => {
                        let key = element.order + "_" + element.toolName;
                        if (this.map.has(key)) {
                            this.map.get(key).push(element);
                        }
                        else {
                            this.map.set(key, [element]);
                        }
                    }); */
                    this.constructObject(data);
                    this.globalMap = this.map;
                    this.isDatainProgress=false;   
                  this.prepareSummaryDetail(data.data.summary);

                    if (data.data.combinedSummary.length > 0) {
                        this.list1 = data.data.combinedSummary;
                    }

                });
        }
    }
    onCardClick(inbex: number, event, key) {
        let cache;
        this.showModel = null;
        this.map = new Map<String, Object[]>();;
        let clickedElement = key;
        clickedElement['searchKey'] = this.selectedField;
        this.cachestring = clickedElement['toolName'] + "." + clickedElement['searchKey'] + "." + clickedElement['issueKey'];

        this.traceablityService.getIssuesPipeline(clickedElement)
            .then((data) => {
                let res = data.data.pipeline;
                this.globalData = res;
                res.forEach(element => {
                    let key = element.order + "_" + element.toolName;
                    if (this.map.has(key)) {
                        this.map.get(key).push(element);
                    }
                    else {
                        this.map.set(key, [element]);
                    }
                });
            });
    }

    applyFilter(filterValue: string) {

        this.git = this.git.filter(x => x.commitId === filterValue);
    }

    getAssetHistoryDetails(toolName: string, toolField: string, toolValue: string, type: string) {
        this.isDatainProgress = true;
        this.cachestring = toolName + "." + toolField + "." + toolValue;
        var self = this;
        this.traceablityService.getAssetHistory(toolName, toolField, toolValue, type)
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
                        this.prepareSummaryDetail(data.data.summary);
                        if (data.data.combinedSummary.length > 0) {
                            this.list1 = data.data.combinedSummary;
                        }
                        this.traceabilityData = data;
                        this.constructObject(this.traceabilityData);
                        this.epic = false;
                        this.jiraArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'JIRA');
                        this.git = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'GIT');
                        this.jenkins = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'JENKINS');
                        this.sonar = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'SONAR');
                        this.runDeck = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'RUNDECK');
                        this.showAll = true;
                        this.workflow();
                        this.isDatainProgress = false;
                        this.isDownload = true;
                    }
                    else {
                        this.messageDialog.openSnackBar("No data found for the given selection.",'error');
                        //this.messageDialog.showApplicationsMessage("No data found for the given selection.", "ERROR");
                        this.isDatainProgress = false;
                        this.isDownload = false;

                    }

                }
                else {
                    this.messageDialog.showApplicationsMessage(data.message, "ERROR");
                    this.isDatainProgress = false;
                    this.isDownload = false;
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
            //element["moddate"] = new Date(element.timestamp*1000);
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
    eventGet(e, index, data) {
        this.formatedData = '';
        this.showModel = index;
        Object.entries(data).forEach(([key, value]) =>
            this.formatedData += `<b>${key}</b><span> :${value}</span><br/>`
        );
    }
    onmouseleave(index) {

    }

    onclick(index) {
        
        let topElementIndex = index.split('-')[0];
        let childElementIndex = index.split('-')[1];
        let topElement = this.list[topElementIndex];
        let clickedElement = topElement.child[childElementIndex].point;
        clickedElement['searchKey'] = this.selectedField;
        this.cachestring = clickedElement['toolName'] + "." + clickedElement['searchKey'] + "." + clickedElement['issueKey'];
        this.traceablityService.getIssuesPipeline(clickedElement)
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
                        this.prepareSummaryDetail(data.data.summary);
                        if (data.data.combinedSummary.length > 0) {
                            this.list1 = data.data.combinedSummary;
                        }
                        this.traceabilityData = data;
                        this.workflow();
                        this.isDatainProgress = false;
                    }
                    else {
                        this.messageDialog.openSnackBar("No data found for the given selection.",'error');
                        this.isDatainProgress = false;
                    }

                }
                else {
                    this.messageDialog.showApplicationsMessage(data.message, "ERROR");
                    this.isDatainProgress = false;
                }
            });
}

    eventLeave() {
        this.showModel = null;
    }

    issueTypeOnChange(value): void {
        this.issueTypeSelected = value;
    }

    toolOnChange(): void {
        this.fieldValue = null;
        this.fieldPlaceVal = null;
        this.selectedField = null;
        this.fieldList = [];
        this.showSprintCheckbox = false;
        this.traceablityService.getToolKeyset(this.selectedTool)
            .then((response) => {
                if (response.status == 'success') {
                    for (var x of response.data) {
                        this.fieldList.push(x)
                    }
                } else {
                    this.messageDialog.showApplicationsMessage(
                        response.message,
                        "ERROR"
                    );
                }
            });
        var self = this;
        self.isToolSelected = true;
        self.isFieldSelected = false;

    }
    FieldOnChange(key, type): void {
        this.fieldValue = null;
        this.showSprintCheckbox = false;
        var self = this;
        self.isFieldSelected = true;
        self.fieldPlaceVal = key + "_val";
        
        if(this.selectedTool === "JIRA"){
            this.issueTypes = this.issueTypesJIRA[this.selectedField]
        } else {
            this.issueTypes = ["Other"]
        }
    }

    showDetailsDialog(toolName: string) {
        let dialogData;
        for (let [key, value] of this.map.entries()) {
            if (key.split('_')[1] === toolName) {
                dialogData = value;
            }
        }
        let showDetailsDialog = this.dialog.open(ShowTraceabiltyDetailsDialog, {
            panelClass: 'custom-dialog-container',
            width: '85%',
            height: '80%',
            disableClose: true,
            data: { toolName: toolName, cachestring: dialogData, showToolDetail: true, showSearch: true }
        });
    }

    showOnHoverDialog(index, data) {
        let dialogData = data;
        let showDetailsDialog = this.dialog.open(ShowTraceabiltyDetailsDialog, {
            panelClass: 'custom-dialog-container',
            width: '35%',
            height: '70%',
            disableClose: true,
            data: { cardData: dialogData, index: index, showCardDetail: true, showSearch: false }
        });
    }

    showTimeDetailsDialog() {
        let showDetailsDialog = this.dialog.open(ShowTraceabiltyDetailsDialog, {
            panelClass: 'custom-dialog-container',
            width: '40%',
            disableClose: true,
            data: { toolName: "toolName", dataArr: this.toolTimelagArray, showToolDetail: false }
        });
    }
    clear() {
        this.selectedTool = '';
        this.selectedField = '';
        this.issueTypeSelected = '';
        this.fieldValue = '';
        this.showDiv = false
        this.cacheMap.clear();
        this.map.clear();
        this.list1 = [];
        this.isDownload = false;
        this.showSprintCheckbox = false;
    }
    reset() {
        this.map = new Map<String, Object[]>();;
        this.getDetails();
    }

    getToolColor(toolName){        
       return this.mapToolColor.get(toolName);
    }

    tabChanged(tabChangeEvent: MatTabChangeEvent): void {
        this.selectedTabIndex = tabChangeEvent.index;
        if(this.selectedTabIndex == 1) {
            this.isDownload = false;
            
        } else {
            if(this.list1.length > 0) {
                this.isDownload = true;
            }
        }
      }

      getSprintIssuesDetails(toolName: string, toolField: string, toolValue: string, type: string) {
        this.isDatainProgress = true;
        let response = this.traceablityService.getSprintIssues(toolName, toolField, toolValue, type);
        this.cachestring = toolName + "." + toolField + "." + toolValue;
        response.then((data) => {
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
                    this.prepareSummaryDetail(data.data.summary);

                    if (data.data.combinedSummary.length > 0) {
                        this.list1 = data.data.combinedSummary;
                    }
                    this.traceabilityData = data;
                    this.globalData = this.traceabilityData.data.pipeline;
                    this.constructObject(this.traceabilityData);
                    this.epic = false;
                    this.showAll = true;
                    this.epicArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'Epic');
                    this.sprintArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'Sprint');
                    this.jiraArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'JIRA');
                    this.workflow();
                    this.isDatainProgress = false;
                    this.isDownload = true;
                    this.handleCheckboxEvent()
                }
                else {
                    this.messageDialog.openSnackBar("No data found for the given selection.",'error');
                    //this.messageDialog.showApplicationsMessage("No data found for the given selection.", "ERROR");
                    this.isDatainProgress = false;
                    this.isDownload = false;
                }

            }
            else {
                this.messageDialog.showApplicationsMessage(data.message, "ERROR");
                this.isDatainProgress = false;
            }
        });
    }

    getReleaseIssuesDetails(toolName: string, toolField: string, toolValue: string, type: string) {
        this.isDatainProgress = true;
        let response = this.traceablityService.getReleaseIssues(toolName, toolField, toolValue, type);
        this.cachestring = toolName + "." + toolField + "." + toolValue;
        response.then((data) => {
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
                    this.prepareSummaryDetail(data.data.summary);

                    if (data.data.combinedSummary.length > 0) {
                        this.list1 = data.data.combinedSummary;
                    }
                    this.traceabilityData = data;
                    this.globalData = this.traceabilityData.data.pipeline;
                    this.constructObject(this.traceabilityData);
                    this.epic = false;
                    this.showAll = true;
                    this.epicArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'Epic');
                    this.sprintArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'Sprint');
                    this.releaseArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'Release');
                    this.jiraArr = this.traceabilityData.data.pipeline.filter(x => x.toolName === 'JIRA');
                    this.workflow();
                    this.isDatainProgress = false;
                    this.isDownload = true;
                }
                else {
                    this.messageDialog.openSnackBar("No data found for the given selection.",'error');
                    //this.messageDialog.showApplicationsMessage("No data found for the given selection.", "ERROR");
                    this.isDatainProgress = false;
                    this.isDownload = false;
                }

            }
            else {
                this.messageDialog.showApplicationsMessage(data.message, "ERROR");
                this.isDatainProgress = false;
            }
        });
    }

    handleCheckboxEvent(){            
        let sprints = this.map.get("2_Sprint");
        if(this.isCheckedSelectedSprint){
            sprints = sprints.filter(sprint => sprint["sprintId"] === this.fieldValue);
        } else {
            sprints = this.traceabilityData.data.pipeline.filter((entry) => entry.order === "2");
        }
        this.map.set("2_Sprint", sprints);
    }
}