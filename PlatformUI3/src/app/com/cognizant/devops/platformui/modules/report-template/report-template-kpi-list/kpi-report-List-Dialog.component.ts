import { Component, OnInit, ElementRef, ViewChild, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { ReportTemplateService } from '../report-template-service';

@Component({
  selector: 'app-kpi-report-List-Dialog',
  templateUrl: './kpi-report-List-Dialog.component.html',
  styleUrls: ['./kpi-report-List-Dialog.component.css']
})
export class KpiReportListDialog implements OnInit {
  kpiList;
  displayedColumns: string[];
  kpiDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  selectedKpi: any;
  templateName: string;

  constructor(public templateService: ReportTemplateService, @Inject(MAT_DIALOG_DATA) public data: any,
  public dialogRef: MatDialogRef<KpiReportListDialog>) {
    this.displayedColumns = ['kpiId', 'vType', 'vQuery'];
    this.templateName = this.data.templateName;
    this.kpiDatasource.data = JSON.parse(data.kpiDetails);
    this.kpiDatasource.paginator = this.paginator;
    console.log(this.kpiDatasource.data);
  }

  ngOnInit() {
  }

  ngAfterViewInit() {
    this.kpiDatasource.paginator = this.paginator;

  }

  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }

}
