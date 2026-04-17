import {
  AfterViewInit,
  Component,
  EventEmitter,
  OnInit,
  Output,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs/internal/Subject';
import { CoreModule } from '../../../../../core/core.module';
import { SharedModule } from '../../../../../shared/shared.module';
import { BaseFormCompoent } from '../../../../../shared/component/base/base-form.component';
import { OptionService } from '../../../../../shared/services/option.service';
import { SystemMessageService } from '../../../../../core/services/system-message.service';
import { DataType } from '../../../../../core/enums/data-type.enum';
import { Option } from '../../../../../shared/models/option.model';
import { TrainTicketService } from '../../../services/train-ticket.service';
import { StorageService } from '../../../../../core/services/storage.service';
import { Router } from '@angular/router';
import { LoadingMaskService } from '../../../../../core/services/loading-mask.service';

@Component({
  selector: 'app-train-info-form',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [TrainTicketService, SystemMessageService, LoadingMaskService],
  templateUrl: './train-info-form.component.html',
  styleUrl: './train-info-form.component.scss',
})
export class TrainInfoFormComponent extends BaseFormCompoent implements OnInit {
  @Output() activeStepEmit = new EventEmitter<number>(); // 控制階段

  trainNoList: Option[] = []; // TrainNo 的下拉式選單
  stops: Option[] = []; // 車站資料的下拉式選單
  kinds: Option[] = []; // 車種資料的下拉式選單
  ticketTypes: Option[] = []; // 車票種類的下拉式選單
  readonly _destroying$ = new Subject<void>(); // 用來取消訂閱

  constructor(
    private optionService: OptionService,
    private storageService: StorageService,
    private router: Router,
    private loadingMaskService: LoadingMaskService,
    private messageService: SystemMessageService
  ) {
    super();
  }

  ngOnInit(): void {
    this.storageService.setSessionStorageItem('step', '' + 0);

    // 初始化表單
    this.formGroup = new FormGroup({
      trainNo: new FormControl(''), // 車次
      trainKind: new FormControl(''), // 車種
      ticketType: new FormControl('', [Validators.required]), // 票別
      fromStop: new FormControl('', [Validators.required]), // 起站
      toStop: new FormControl('', [Validators.required]), // 起站
      takeDate: new FormControl('', [Validators.required]), // 搭乘日期
      takeTime: new FormControl('', [Validators.required]), // 搭乘時間
    });

    this.optionService.getSettingTypes(DataType.STOP_KIND).subscribe({
      next: (res) => {
        this.stops = res;
      },
      error: (err) => {
        this.messageService.error(err);
      },
    });

    //	 取得火車種類的下拉式選單資料
    this.optionService.getTrainKinds().subscribe({
      next: (res) => {
        this.kinds = res;
      },
      error: (error) => {
        this.messageService.error(
          '取得火車種類的下拉式選單資料時，發生錯誤',
          error.message
        );
      },
    });
    //	 取得車票種類的下拉式選單資料
    this.optionService.getTicketTypes().subscribe({
      next: (res) => {
        this.ticketTypes = res;
      },
      error: (error) => {
        this.messageService.error(
          '取得火車種類的下拉式選單資料時，發生錯誤',
          error.message
        );
      },
    });

    //	 取得火車種類的下拉式選單資料
    this.optionService.getTrainNoList().subscribe({
      next: (res) => {
        this.trainNoList = res;
      },
      error: (error) => {
        this.messageService.error(
          '取得火車車次的下拉式選單資料時，發生錯誤',
          error.message
        );
      },
    });
  }

  /**
   * 查詢車次資料
   * @returns
   */
  query() {
    this.submitted = true;

    if (!this.submitted || this.formGroup.invalid) {
      return;
    }

    this.submitted = false;
    const formData = this.formGroup.value;

    // 傳遞到父組件告訴他要變更階段
    this.updateStopIndex();

    // 設置火車車次查詢資料
    this.router.navigate(['/ticket/train-selecting'], {
      queryParams: {
        trainNo: formData.trainNo,
        trainKind: formData.trainKind,
        fromStop: formData.fromStop,
        toStop: formData.toStop,
        ticketType: formData.ticketType,
        takeDate: formData.takeDate,
        takeTime: formData.takeTime,
      },
    });
  }

  clear() {
    this.formGroup.reset();
  }

  /**
   * 傳遞到父組件告訴他要變更階段2
   */
  updateStopIndex() {
    this.storageService.setSessionStorageItem('step', '' + 1);
  }
}
