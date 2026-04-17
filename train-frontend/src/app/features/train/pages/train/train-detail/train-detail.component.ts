import { Component, OnInit } from '@angular/core';
import { BaseFormCompoent } from '../../../../../shared/component/base/base-form.component';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../../shared/shared.module';
import { CoreModule } from '../../../../../core/core.module';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-train-detail',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  templateUrl: './train-detail.component.html',
  styleUrl: './train-detail.component.scss',
})
export class TrainDetailComponent extends BaseFormCompoent implements OnInit {
  cols: any[] = []; // Cols 資料
  tableData: any; // Table 資料
  constructor(
    private dialogConfig: DynamicDialogConfig,
    public ref: DynamicDialogRef
  ) {
    super();
  }
  ngOnInit(): void {
    const rowData = this.dialogConfig.data.rowData;
    console.log(this.dialogConfig.data);

    // 初始化表單
    this.formGroup = new FormGroup({
      trainNo: new FormControl(rowData.trainNo), // 車次
      kind: new FormControl(rowData.kind), // 車種
      fromStop: new FormControl(rowData.fromStop), // 起站
      fromStopTime: new FormControl(rowData.fromStopTime), // 搭乘日期
      toStop: new FormControl(rowData.toStop), // 起站
      toStopTime: new FormControl(rowData.toStopTime), // 搭乘時間
    });

    this.cols = this.dialogConfig.data.lineCols;
    this.tableData = this.dialogConfig.data.rowData.stops;
  }
}
