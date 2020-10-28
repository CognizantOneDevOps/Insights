import { Component, OnInit, ViewChild } from '@angular/core';
import { Router, NavigationExtras } from '@angular/router';
import { MatDialog } from '@angular/material';
import { FileUploadDialog } from '../fileUploadDialog/fileUploadDialog.component';
import { MatTableDataSource, MatPaginator } from '@angular/material';
import { KpiService } from '../kpi-addition/kpi-service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { ContentService } from '../content-config-list/content-service';

@Component({
  selector: 'app-kpi-creation',
  templateUrl: './kpi-creation.component.html',
  styleUrls: ['./kpi-creation.component.css', './../home.module.css']
})
export class KpiCreationComponent implements OnInit {
  displayedColumns = [];
  @ViewChild(MatPaginator) paginator: MatPaginator;
  kpiDatasource = new MatTableDataSource<any>();
  enableEdit: boolean = false;
  onRadioBtnSelect: boolean = false;
  kpiList: any;
  data: any[];
  kpiId: number;
  kpiName: string;
  toolname: string;
  groupName: string;
  category: string;
  refreshRadio: boolean = false;
  selectedKpi: any;
  showConfirmMessage: string;
  type: string;
  enableRefresh: boolean = false;
  MAX_ROWS_PER_TABLE = 10;


  constructor(public messageDialog: MessageDialogService,
    public router: Router, public dialog: MatDialog,
    public kpiService: KpiService, public contentService: ContentService) {
  }

  ngOnInit() {
    this.getAllActiveKpi();
    this.displayedColumns = ['radio', 'KpiId', 'KpiName', 'ToolName', 'GroupName', 'Category'];
    this.kpiService.fileUploadSubject.subscribe(res => {
      if (res === 'REFRESH') {
        this.getAllActiveKpi();
      }
    })
  }
  ngAfterViewInit() {
    this.kpiDatasource.paginator = this.paginator;
  }

  addnewKpi() {
    this.kpiService.setType("ADD");
    this.router.navigateByUrl('InSights/Home/kpiaddition');
  }
  uploadFile() {
    this.contentService.setFileType('KPI');
    this.dialog.open(FileUploadDialog, {
      panelClass: 'DialogBox',
      width: '34%',
      height: '25%',
      disableClose: false,
    });
  }
  edit() {
    this.kpiService.setType("EDIT");
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "kpiId": this.selectedKpi.kpiId,
        "kpiName": this.selectedKpi.kpiName,
        "selectedTool": this.selectedKpi.toolname,
        "category": this.selectedKpi.category,
        "groupName": this.selectedKpi.groupName,
        "dataSource": this.selectedKpi.datasource,
        "dbQuery": this.selectedKpi.dBQuery,
        "isActive": this.selectedKpi.isActive,
      }
    };

    this.router.navigate(['InSights/Home/kpiaddition'], navigationExtras);
  }
  refresh() {
    this.getAllActiveKpi();
    this.refreshRadio = false;
  }
  enableButtons() {
    this.onRadioBtnSelect = true;
  }
  public async getAllActiveKpi() {
    var self = this;
    self.refreshRadio = false;
    this.kpiList = [];
    this.kpiList = await this.kpiService.loadKpiList();
    this.kpiList.data.forEach(kpi => {
      let kpiIdArr = [];
      kpiIdArr.push(kpi.kpiId);
    });

    if (this.kpiList != null && this.kpiList.status == "success") {
      this.kpiDatasource.data = this.kpiList.data.sort(
        (a, b) => a.kpiId > b.kpiId
      );

      this.kpiDatasource.paginator = this.paginator;
    }
  }
  applyFilter(filterValue: string) {
    this.kpiDatasource.filter = filterValue.trim();
  }

  list() {
    this.getAllActiveKpi();
  }
  uninstallKpi() {
    var self = this;
    let data = self.selectedKpi;

    var title = "Delete KPI";
    var dialogmessage =
      "Do you want to delete a Kpi <b>" +
      self.selectedKpi.kpiId +
      "</b>? <br><br> <b> Please note: </b> The action of deleting a Kpi " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedKpi.toolName,
      "ALERT",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var kpiAPIRequestJson = {};
        kpiAPIRequestJson['kpiId'] = self.selectedKpi.kpiId;

        self.kpiService
          .kpiUninstall(JSON.stringify(kpiAPIRequestJson))
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



