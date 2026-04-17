import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SharedModule } from '../../../../../shared/shared.module';
import { CoreModule } from '../../../../../core/core.module';
import { BaseFormCompoent } from '../../../../../shared/component/base/base-form.component';
import { FormControl, FormGroup } from '@angular/forms';
import { StorageService } from '../../../../../core/services/storage.service';

@Component({
  selector: 'app-booked-successfully',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  templateUrl: './booked-successfully.component.html',
  styleUrl: './booked-successfully.component.scss',
})
export class BookedSuccessfullyComponent
  extends BaseFormCompoent
  implements OnInit
{
  payMethods: any[] | undefined;

  ticketBooking!: any;

  constructor(private router: Router, private storageService: StorageService) {
    super();
  }
  ngOnInit(): void {
    // 初始化表單
    this.formGroup = new FormGroup({
      trainNo: new FormControl(''), // 車次
      trainKind: new FormControl(''), // 車種
      fromStop: new FormControl(''), // 起站
      toStop: new FormControl(''), // 起站
      fromStopTime: new FormControl(''), // 起站
      toStopTime: new FormControl(''), // 起站
      takeDate: new FormControl(''), // 搭乘日期
      price: new FormControl(''), // 價格
      seatNo: new FormControl(''), // 座位號碼
      carNo: new FormControl(''), // 車廂編號
      payMethod: new FormControl(''),
      status: new FormControl(''),
    });

    this.formGroup.patchValue({
      ...history.state,
      status: '已付款',
    });
  }
  /**
   * 重導向回首頁
   */
  redirectHome() {
    this.router.navigate(['/home']);
    this.storageService.setSessionStorageItem('step', '' + 0);
  }
}
