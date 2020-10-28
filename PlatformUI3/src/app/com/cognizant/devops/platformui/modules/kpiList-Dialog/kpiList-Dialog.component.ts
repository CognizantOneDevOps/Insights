import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { MatTableDataSource, MatPaginator, MatDialogRef } from '@angular/material';
import { KpiService } from '../kpi-addition/kpi-service';

@Component({
  selector: 'app-kpiList-Dialog',
  templateUrl: './kpiList-Dialog.component.html',
  styleUrls: ['./kpiList-Dialog.component.css', './../home.module.css']
})
export class KpiListDialog implements OnInit {
  kpiList;
  displayedColumns: string[];
  kpiDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator) paginator: MatPaginator;
  selectedKpi: any;

  constructor(public kpiService: KpiService, public dialogRef: MatDialogRef<KpiListDialog>) {
    this.displayedColumns = ['radio', 'kpiId', 'kpiName', 'category'];

  }
  ngOnInit() {
    this.getAllActiveKpi();
  }
  ngAfterViewInit() {
    this.kpiDatasource.paginator = this.paginator;

  }
  applyFilter(filterValue: string) {
    this.kpiDatasource.filter = filterValue.trim();
  }
  setKpiValue() {
    this.kpiService.setKpiSubject.next(this.selectedKpi);
  }
  onOkClick() {
    let data = this.selectedKpi;
    this.setKpiValue();
    this.dialogRef.close();
  }

  public async getAllActiveKpi() {
    var self = this;
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
  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }

}
