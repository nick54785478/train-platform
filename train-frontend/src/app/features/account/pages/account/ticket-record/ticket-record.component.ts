import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { BaseFormCompoent } from '../../../../../shared/component/base/base-form.component';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../../shared/shared.module';
import { TicketDetailResource } from '../../../models/ticket-detail-data-resource.model copy';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Location } from '@angular/common';

@Component({
  selector: 'app-ticket-record',
  standalone: true,
  imports: [CommonModule, SharedModule],
  templateUrl: './ticket-record.component.html',
  styleUrl: './ticket-record.component.scss',
})
export class TicketRecordComponent extends BaseFormCompoent implements OnInit {
  ticketData!: TicketDetailResource;
  constructor(
    public ref: DynamicDialogRef,
    private location: Location,
    private dialogConfig: DynamicDialogConfig
  ) {
    super();
  }
  ngOnInit(): void {
    // 監聽上一頁切換，關閉 Dialog
    this.location.onUrlChange(() => {
      this.onCloseForm();
    });

    console.log(this.dialogConfig.data);
    this.ticketData = this.dialogConfig.data.data;
    // 初始化表單
    this.formGroup = new FormGroup({
      number: new FormControl(''), // 車次
      kind: new FormControl(''), // 車種
      fromStop: new FormControl(''), // 起站
      toStop: new FormControl(''), // 迄站
      fromStopTime: new FormControl(''), // 發車時間
      takeDate: new FormControl(''), // 搭乘日期
      seatNo: new FormControl(''), // 座位號碼
      carNo: new FormControl(''), // 車廂編號
    });

    this.formGroup.patchValue({
      number: this.ticketData.number,
      kind: this.ticketData.kind,
      fromStop: this.ticketData.from,
      toStop: this.ticketData.to,
      fromStopTime: this.ticketData.startTime,
      // toStopTime: this.ticketData.toStopTime,
      takeDate: this.ticketData.takeDate,
      seatNo: this.ticketData.seatNo,
      carNo: this.ticketData.carNo,
    });
  }

  /**
   * 關閉 Dialog
   */
  onCloseForm() {
    console.log('關閉 Dialog');
    this.ref.close();
  }
}
