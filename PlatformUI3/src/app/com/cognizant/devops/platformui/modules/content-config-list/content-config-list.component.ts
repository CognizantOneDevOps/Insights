import { Component, OnInit, ViewChild } from '@angular/core';
import { Router, NavigationExtras } from '@angular/router';
import { MatDialog } from '@angular/material';
import { FileUploadDialog } from '../fileUploadDialog/fileUploadDialog.component';
import { MatTableDataSource, MatPaginator } from '@angular/material';
import { KpiService } from '../kpi-addition/kpi-service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { ContentService } from './content-service';

@Component({
  selector: 'app-content-config',
  templateUrl: './content-config-list.component.html',
  styleUrls: ['./content-config-list.component.css', './../home.module.css']
})
export class ContentConfigComponent implements OnInit {
  displayedColumns = [];
  @ViewChild(MatPaginator) paginator: MatPaginator;
  contentDatasource = new MatTableDataSource<any>();
  enableEdit: boolean = false;
  onRadioBtnSelect: boolean = false;
  contentList: any;
  data: any[];
  kpiId: number;
  kpiName: string;
  toolname: string;
  groupName: string;
  category: string;
  refreshRadio: boolean = false;
  selCont: any;
  showConfirmMessage: string;
  type: string;
  enableRefresh: boolean = false;
  MAX_ROWS_PER_TABLE = 10;
  action: any;
  constructor(public messageDialog: MessageDialogService,
    public router: Router, public dialog: MatDialog, public kpiService: KpiService, public contentService: ContentService) {
  }

  ngOnInit() {
    this.getAllActiveContent();
    this.displayedColumns = ['radio', 'ContentId', 'ContentName', 'KpiId', 'ExpectedTrend', 'ResultField'];
    this.kpiService.fileUploadSubject.subscribe(res => {
      if (res === 'REFRESH') {
        this.getAllActiveContent();
      }
    })
  }
  ngAfterViewInit() {
    this.contentDatasource.paginator = this.paginator;
  }

  addnewContent() {
    this.contentService.setType("ADD");
    this.router.navigateByUrl('InSights/Home/contentConfigAdd');
  }
  uploadFile() {
    this.contentService.setFileType('CONTENT');
    this.dialog.open(FileUploadDialog, {
      panelClass: 'DialogBox',
      width: '34%',
      height: '25%',
      disableClose: false,
    });
  }
  edit() {
    this.contentService.setType("EDIT");
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "contentId": this.selCont.contentId,
        "contentName": this.selCont.contentName,
        "kpiId": this.selCont.kpiId,
        "expectedTrend": this.selCont.expectedTrend,
        "directionThreshold": this.selCont.directionOfThreshold,
        "resultField": this.selCont.resultField,
        "action": this.selCont.action,
        "isActive": this.selCont.isActive,
        "message": JSON.stringify(this.selCont.message),
        "threshold": this.selCont.threshold,
        "thresholds": JSON.stringify(this.selCont.thresholds),
        "category": this.selCont.category
      }
    };
    this.router.navigate(['InSights/Home/contentConfigAdd'], navigationExtras);
  }
  applyFilter(filterValue: string) {
    this.contentDatasource.filter = filterValue.trim();
  }
  refresh() {
    this.getAllActiveContent();
    this.refreshRadio = false;
    this.onRadioBtnSelect = false;
  }
  enableButtons(element) {
    this.onRadioBtnSelect = true;
  }
  public async getAllActiveContent() {
    var self = this;
    self.refreshRadio = false;
    this.contentList = [];
    this.contentList = await this.contentService.loadContentList();
    if (this.contentList != null && this.contentList.status == "success") {
      this.contentDatasource.data = this.contentList.data.sort(
        (a, b) => a.kpiId > b.kpiId
      );

      this.contentDatasource.paginator = this.paginator;
    }
  }
  list() {
    this.getAllActiveContent();
  }

  uninstallContent() {
    var self = this;
    let data = self.selCont;
    var title = "Delete Content";
    var dialogmessage =
      "Do you want to delete content <b>" +
      self.selCont.contentId +
      "</b>? <br><br> <b> Please note: </b> The action of deleting content id " +
      "<b>" +
      self.selCont.contentId +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selCont.toolName,
      "ALERT",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var contentAPIRequestJson = {};
        contentAPIRequestJson['contentId'] = self.selCont.contentId;

        self.contentService
          .contentUninstall(JSON.stringify(contentAPIRequestJson))
          .then(function (data) {
            if (data.status === "success") {
              self.messageDialog.showApplicationsMessage("<b>" + data.data.message + "</b>", "SUCCESS");
              self.list();
            }
            if (data.status === "failure") {
              self.messageDialog.showApplicationsMessage("<b>" + data.message + "</b>", "Failure");
              self.list();
            }
          })
          .catch(function (data) {
            self.showConfirmMessage = "service_error";
          });
      }
    });
  }


}
