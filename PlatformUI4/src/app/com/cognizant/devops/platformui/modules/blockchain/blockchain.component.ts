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
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { BlockChainService } from '@insights/app/modules/blockchain/blockchain.service';
import { DatePipe } from '@angular/common'
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MatInput } from '@angular/material/input';
import { MatRadioChange } from '@angular/material/radio';
import { AssetDetailsDialog } from '@insights/app/modules/blockchain/bc-asset-details-dialog';
import { FormControl, Validators, FormBuilder, FormGroup } from '@angular/forms';
import { NoopScrollStrategy } from '@angular/cdk/overlay';
import { DataSharedService } from '@insights/common/data-shared-service';


export interface AssetData {
  assetID: string;
  phase: string;
  toolstatus: string;
  toolName: string;
  timestamp: string;
}



@Component({
  selector: 'app-blockchain',
  templateUrl: './blockchain.component.html',
  styleUrls: ['./blockchain.component.scss', './../home.module.scss']
})
export class BlockChainComponent implements OnInit {
  today = new Date();
  maxDateValue: any;
  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  displayedColumns: string[] = ['select', 'assetID', 'toolName', 'phase', 'toolstatus', 'timestamp','details'];
  dataSource = new MatTableDataSource<AssetData>([]);
  MAX_ROWS_PER_TABLE = 5;
  startDate: string;
  endDate: string;
  showSearchResult = false;
  selectedOption: string = "searchByDates";
  startDateFormatted: string;
  endDateFormatted: string;
  assetID: string = "";
  startDateInput: Date;
  endDateInput: Date;
  searchCriteria: string = "";
  searchResultNotFoundMsg: string = "";
  noSearchResultFlag: boolean = false;
  @ViewChild('startDateMatInput', { read: MatInput, static: true }) startDateMatInput: MatInput;
  @ViewChild('endDateMatInput', { read: MatInput, static: true }) endDateMatInput: MatInput;
  @ViewChild('assetIdInput', { read: MatInput, static: true }) assetIdInput: MatInput;
  selectedBasePrimeID: string = "";
  selectedAssetID: string = "";
  displayProgressBar: boolean = false;
  toolname: string;
  tools = [];
  blockChainconfigForm: FormGroup;
  timeZone: string = "";
  timeZoneAbbr: string = "";
  selectedIndex : number=-1;
  currentPageIndex : number= 1;
  totalPages: number = -1;

 constructor(private blockChainService: BlockChainService, private datepipe: DatePipe,
    private messageDialog: MessageDialogService, private dialog: MatDialog,
    private formBuilder: FormBuilder,public dataShare: DataSharedService) {

    this.blockChainService.getProcessFlow()
      .then((data) => {
        data.Steps.forEach(element => {
          this.tools.push(element.Tool.toUpperCase());
        });
      });

  }

  ngOnInit() {
    this.timeZone = this.dataShare.getTimeZone();
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
 }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  //Method gets invoked when search button is clicked
  searchAllAssets() {
    this.searchCriteria = "";
    this.selectedAssetID = "";
    this.selectedBasePrimeID = "";
    console.log(this.selectedOption)
    if (this.selectedOption == "searchByDates") {
      console.log(this.toolname);
      if (this.startDateInput === undefined || this.endDateInput === undefined || this.toolname == '') {
        this.messageDialog.openSnackBar("Please select start/end date & tool for Date search.", "error");
        return;
      }
      let dateCompareResult: number = this.compareDate(this.startDateInput, this.endDateInput);
      if (dateCompareResult == 1) {
        this.messageDialog.openSnackBar("Start date cannot be greater than end date.", "error");
        return;
      }
      this.displayProgressBar = true;
      this.noSearchResultFlag = false;
      this.showSearchResult = false;
      this.blockChainService.getAllAssets(this.startDate, this.endDate, this.toolname)
        .then((data) => {
          let assetDetails = [];
          if (data.status != 'failure') {
            data.data.map((d) => {
              Object.keys(d).forEach(k => {
                const matchKey = k.match('AssetID');
                if (matchKey) {
                  d['assetID'] = d[k];
                }

              })
              assetDetails.push(d);
            });
          }
          this.displayProgressBar = false;
          if (data.status === "failure") {
            console.error(data);
            this.noSearchResultFlag = true;
            this.showSearchResult = false;
            this.searchCriteria = this.startDateFormatted + " to " + this.endDateFormatted;
            this.searchResultNotFoundMsg = data.message;
            this.messageDialog.openSnackBar(this.searchResultNotFoundMsg,'error');
          } else {
            console.log(" success server response >>");
            console.log(assetDetails);
            this.dataSource.data = assetDetails;
            this.showSearchResult = true;
            this.noSearchResultFlag = false;
            this.searchCriteria = this.startDateFormatted + " to " + this.endDateFormatted;
            this.dataSource.sort = this.sort;
            this.dataSource.paginator = this.paginator;
            this.currentPageIndex = this.paginator.pageIndex +1;
            this.totalPages = Math.ceil(this.dataSource.data.length / this.MAX_ROWS_PER_TABLE);
          }
        });
    } else if (this.selectedOption == "searchByAssetId") {
      if (this.assetID === undefined || this.assetID === "") {
        this.messageDialog.openSnackBar("Please provide Input Asset ID.", "error");
        return;
      } else {
        this.displayProgressBar = true;
        this.noSearchResultFlag = false;
        this.showSearchResult = false;
        this.blockChainService.getAssetInfo(encodeURIComponent(this.assetID))
          .then((data) => {
            let assetDetails = [];
            if (data.status != 'failure') {
              data.data.map((d) => {
                Object.keys(d).forEach(k => {
                  const matchKey = k.match('AssetID');
                  if (matchKey) {
                    d['assetID'] = d[k];
                  }

                })
                assetDetails.push(d);
              });
            }
            this.displayProgressBar = false;
            if (data.status === "failure") {
              this.noSearchResultFlag = true;
              this.showSearchResult = false;
              this.searchCriteria = this.assetID;
              this.searchResultNotFoundMsg = data.message;
              this.messageDialog.openSnackBar(this.searchResultNotFoundMsg,'error');
            } else {
              console.log("server response >>");
              console.log(assetDetails);
              this.dataSource.data = assetDetails;
              //this.displayedColumns = ['select', 'assetID', 'toolName', 'phase', 'toolStatus'];
              this.showSearchResult = true;
              this.noSearchResultFlag = false;
              this.searchCriteria = this.assetID;
              this.dataSource.sort = this.sort;
              this.dataSource.paginator = this.paginator;
              this.currentPageIndex = this.paginator.pageIndex +1;
              this.totalPages = Math.ceil(this.dataSource.data.length / this.MAX_ROWS_PER_TABLE);
            }
          });
      }
    }
  }

  //When radio button selection changes to select search criteria
  searchCriteriaChange($event: MatRadioChange) {
    console.log(" Event Value "+ $event.value )
    this.selectedOption = $event.value;
    if ($event.value == "searchByDates") {
      this.assetIdInput.value = '';
      this.assetID = "";
    } else if ($event.value == "searchByAssetId") {
      this.startDateMatInput.value = '';
      this.endDateMatInput.value = '';
      this.startDateInput = undefined;
      this.endDateInput = undefined;
      this.toolname ='';
    }
  }
  //Checks whether start date is greater than end date and if yes throws error message
  validateDateRange() {
    let dateCompareResult: number = this.compareDate(this.startDateInput, this.endDateInput);
    if (dateCompareResult == 1) {
      this.messageDialog.openSnackBar("Start date cannot be greater than end date.", "error");
      return;
    }
  }

  //Sets value in assetID property from user's input
  getAssetID(assetIdInput: string) {
    if (assetIdInput) {
      this.assetID = assetIdInput;
      this.selectedOption = 'searchByAssetId';
    } else {
      this.assetID = "";
    }
  }

  getStartDate(event: MatDatepickerInputEvent<Date>) {
    this.startDateInput = event.value;
    this.startDate = this.datepipe.transform(this.startDateInput, 'yyyy-MM-dd');
    this.startDateFormatted = this.datepipe.transform(this.startDateInput, 'MM/dd/yyyy');
    this.validateDateRange();
  }

  getEndDate(event: MatDatepickerInputEvent<Date>) {
    this.endDateInput = event.value;
    this.endDate = this.datepipe.transform(this.endDateInput, 'yyyy-MM-dd');
    this.endDateFormatted = this.datepipe.transform(this.endDateInput, 'MM/dd/yyyy');
    this.validateDateRange();
  }

  /*
  * Compares two Date objects and returns e number value that represents
  * the result:
  * 0 if the two dates are equal.
  * 1 if the first date is greater than second.
  * -1 if the first date is less than second.
  * @param date1 First date object to compare.
  * @param date2 Second date object to compare.
  */
  compareDate(date1: Date, date2: Date): number {
    // With Date object we can compare dates them using the >, <, <= or >=.
    // The ==, !=, ===, and !== operators require to use date.getTime(),
    // so we need to create a new instance of Date with 'new Date()'
    let d1 = new Date(date1); let d2 = new Date(date2);

    // Check if the dates are equal
    let same = d1.getTime() === d2.getTime();
    if (same) return 0;

    // Check if the first is greater than second
    if (d1 > d2) return 1;

    // Check if the first is less than second
    if (d1 < d2) return -1;
  }

  //Displays Asset Details Dialog box
  showAssetDetailsDialog() {
    if (this.selectedAssetID == "") {
      return;
    }
    let showDetailsDialog = this.dialog.open(AssetDetailsDialog, {
      panelClass: 'custom-dialog-container',
      height: '90%',
      width: '80%',
      disableClose: true,
      scrollStrategy: new NoopScrollStrategy(),
      data: { assetID: this.selectedAssetID, tools: this.tools }
    });
  }

  populateBasePrimeID($event: MatRadioChange, assetID: string, index) {
    this.selectedIndex = index + this.currentPageIndex ;
    this.selectedBasePrimeID = $event.value;
    this.selectedAssetID = assetID;
  }

  applyAssetFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim();
    this.totalPages = Math.ceil(this.dataSource.paginator.length / this.MAX_ROWS_PER_TABLE);
  }

  clear() {
    this.toolname="";
    this.endDateMatInput.value="";
    this.startDateMatInput.value="";
    this.assetIdInput.value="";
  }

  goToNextPage() {
    this.paginator.nextPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  goToPrevPage() {
    this.paginator.previousPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
}
