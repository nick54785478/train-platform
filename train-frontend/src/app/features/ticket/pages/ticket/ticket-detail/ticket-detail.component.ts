import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseFormTableCompoent } from '../../../../../shared/component/base/base-form-table.component';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../../shared/shared.module';
import { CoreModule } from '../../../../../core/core.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TrainSeatService } from '../../../services/train-seat.service';
import { lastValueFrom } from 'rxjs/internal/lastValueFrom';
import { map } from 'rxjs/internal/operators/map';
import { Router } from '@angular/router';
import { TrainTicketService } from '../../../services/train-ticket.service';
import { BookTicketResource } from '../../../models/book-ticket-resource.model';
import { finalize, firstValueFrom } from 'rxjs';
import { LoadingMaskService } from '../../../../../core/services/loading-mask.service';
import { error } from 'console';
import { SystemMessageService } from '../../../../../core/services/system-message.service';
import { TrainInfoSelectedResource } from '../../../models/train-info-selected-resource.model';
import { OptionService } from '../../../../../shared/services/option.service';
import { Option } from '../../../../../shared/models/option.model';
import { StorageService } from '../../../../../core/services/storage.service';
import { StepQueryKey } from '../../../../../core/enums/step-query-key.enum copy';
import { DataType } from '../../../../../core/enums/data-type.enum';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [Router],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.scss',
})
export class TicketDetailComponent
  extends BaseFormTableCompoent
  implements OnInit
{
  trainInfo!: TrainInfoSelectedResource;
  payMethods: Option[] = []; // 付款方式清單

  constructor(
    private router: Router,
    private trainSeatService: TrainSeatService,
    private trainTicketService: TrainTicketService,
    private optionService: OptionService,
    private storageService: StorageService,
  ) {
    super();
  }
  async ngOnInit(): Promise<void> {
    this.optionService.getSettingTypes(DataType.PAY_METHOD).subscribe((res) => {
      this.payMethods = res;
    });

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
      payMethod: new FormControl('', [Validators.required]),
    });

    console.log(history);
    if (history === undefined) {
      this.router.navigate(['/form-invalid']);
    }
    // 從 state 中取得資料
    this.trainInfo = history ? history.state : '';
    console.log(this.trainInfo);

    // 取得車票座位資訊
    const ticketInfo = await lastValueFrom(
      this.trainSeatService
        .getSeatInfo(this.trainInfo.trainUuid, this.trainInfo.takeDate)
        .pipe(
          map((res) => {
            return res;
          }),
        ),
    );

    if (this.trainInfo) {
      this.formGroup.patchValue({
        trainNo: this.trainInfo.trainNo,
        trainKind: this.trainInfo.trainKind,
        fromStop: this.trainInfo.fromStop,
        toStop: this.trainInfo.toStop,
        fromStopTime: this.trainInfo.fromStopTime,
        toStopTime: this.trainInfo.toStopTime,
        takeDate: this.trainInfo.takeDate,
        price: this.trainInfo.price,
        seatNo: ticketInfo.seatNo,
        carNo: ticketInfo.carNo,
      });
    }
  }

  /**
   * 提交訂單
   * @returns
   */
  submit() {
    this.submitted = true;
    if (!this.submitted || this.formGroup.invalid) {
      return;
    }
    this.loadingMaskService.show();
    let formData = this.formGroup.value;
    let request: BookTicketResource = { ...formData };
    request.ticketUuid = this.trainInfo.ticketUuid;
    request.trainUuid = this.trainInfo.trainUuid;
    request.payMethod = formData.payMethod;

    this.trainTicketService
      .bookTicket(request)
      .pipe(
        finalize(() => {
          this.submitted = false;
          this.loadingMaskService.hide();
        }),
      )
      .subscribe({
        next: (res) => {
          if (res.code === '200' || res.code === '201') {
            this.messageService.success('成功新增一筆資料');
            this.storageService.setSessionStorageItem('step', '' + 3);
            this.router.navigate(['/ticket/booked-successfully'], {
              state: request,
            });
          } else {
            this.messageService.error(res?.message);
          }
        },
        error: (err) => {
          this.messageService.error(err);
        },
      });
  }

  /**
   * 取消訂票，並重導向訂票首頁
   */
  cancel() {
    this.storageService.removeSessionStorageItem('step');
    this.router.navigate(['/ticket']);
  }
}
