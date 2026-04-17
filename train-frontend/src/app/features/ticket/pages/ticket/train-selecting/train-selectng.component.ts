import { Component, OnDestroy, OnInit } from '@angular/core';
import { DialogService } from 'primeng/dynamicdialog';
import { CommonModule } from '@angular/common';
import { CoreModule } from '../../../../../core/core.module';
import { SharedModule } from '../../../../../shared/shared.module';
import { TrainTicketService } from '../../../services/train-ticket.service';
import { SystemMessageService } from '../../../../../core/services/system-message.service';
import { ActivatedRoute, Router } from '@angular/router';
import { NavigateService } from '../../../../../core/services/navigate.service';
import { StorageService } from '../../../../../core/services/storage.service';
import { firstValueFrom, Subject, takeUntil } from 'rxjs';
import { StepQueryKey } from '../../../../../core/enums/step-query-key.enum copy';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BaseFormTableCompoent } from '../../../../../shared/component/base/base-form-table.component';
import { BookTicketResource } from '../../../models/book-ticket-resource.model';
import { TrainInfoSelectedResource } from '../../../models/train-info-selected-resource.model';

@Component({
  selector: 'app-ticket-booking',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [DialogService, TrainTicketService, NavigateService],
  templateUrl: './train-selecting.component.html',
  styleUrl: './train-selecting.component.scss',
})
export class TrainSelectingComponent
  extends BaseFormTableCompoent
  implements OnInit, OnDestroy
{
  // 訂閱
  private readonly _destroying$ = new Subject<void>();

  detailVisible: boolean = false;

  rowSelected: boolean = false;

  ticketInfo!: TrainInfoSelectedResource; // 確認訂票資訊

  constructor(
    private route: ActivatedRoute,
    private trainTicketService: TrainTicketService,
    private storageService: StorageService,
    private router: Router
  ) {
    super();
  }

  async ngOnInit(): Promise<void> {
    // 初始化表單
    this.formGroup = new FormGroup({
      trainNo: new FormControl(''), // 車次
      trainKind: new FormControl(''), // 車種
      fromStop: new FormControl(''), // 起站
      toStop: new FormControl(''), // 起站
      takeDate: new FormControl(''), // 搭乘日期
      fromStopTime: new FormControl(''), // 發車時間
      price: new FormControl(''), // 票價
    });

    this.cols = [
      {
        field: 'trainNo',
        header: '車次',
        type: '',
      },
      {
        field: 'kind',
        header: '車種',
        type: '',
      },
      {
        field: 'fromStop',
        header: '起站',
        type: '',
      },
      {
        field: 'fromStopTime',
        header: '發車時間',
        type: '',
      },
      {
        field: 'toStop',
        header: '迄站',
        type: '',
      },
      {
        field: 'toStopTime',
        header: '抵達時間',
        type: '',
      },
      {
        field: 'price',
        header: '票價',
        type: '',
      },
    ];

    // 從 URL 的 queryParam 取得查詢條件
    this.route.queryParams.subscribe(async (params) => {
      let trainNo = params['trainNo'] || '';
      let trainKind = params['trainKind'] || ''; // 將字串轉為數字
      let fromStop = params['fromStop'] || '';
      let toStop = params['toStop'] || '';
      let takeDate = params['takeDate'] || '';
      let takeTime = params['takeTime'] || '';
      let ticketType = params['ticketType'] || '';

      // 將 queryParam 轉換為字串存入 Session Storage
      const searchParams = new URLSearchParams(params).toString();
      this.storageService.setSessionStorageItem(
        StepQueryKey.STEP2,
        searchParams
      );

      this.tableData = await firstValueFrom(
        this.trainTicketService
          .query(
            trainNo,
            trainKind,
            fromStop,
            toStop,
            takeDate,
            takeTime,
            ticketType
          )
          .pipe(takeUntil(this._destroying$))
      );
    });
  }

  ngOnDestroy(): void {
    this._destroying$.unsubscribe();
    this._destroying$.closed;
  }

  /**
   * 提交訂單
   */
  submit() {
    this.ticketInfo = {
      trainUuid: this.selectedData.uuid,
      ticketUuid: this.selectedData.ticketUuid,
      trainNo: this.selectedData.trainNo,
      price: this.selectedData.price,
      takeDate: this.selectedData.takeDate,
      fromStop: this.selectedData.fromStop,
      toStop: this.selectedData.toStop,
      fromStopTime: this.selectedData.fromStopTime,
      toStopTime: this.selectedData.toStopTime,
      trainKind: this.selectedData.kind,
      seatNo: '',
    };
    // 使用 State 在轉傳時攜帶資料轉傳
    this.router.navigateByUrl('/ticket/ticket-detail', {
      state: this.ticketInfo,
    });
    this.storageService.setSessionStorageItem('step', '' + 2);
  }

  // 紀錄該筆資料
  override clickRowActionMenu(rowData: any) {
    // if (this.selectedData && this.rowSelected) {
    //   return;
    // }

    if (this.selectedData && rowData.uuid === this.selectedData.uuid) {
      // 若 SelectedData 有值 且 uuid 相等 => 代表點選同一筆，即取消該選取
      this.detailVisible = false;
      this.selectedData = null;
    } else {
      this.detailVisible = true;
      this.selectedData = rowData;
      console.log(rowData);
      this.rowSelected = true;
      this.patchFormGroupValue(rowData);
    }
  }

  // Patch FormGroup 的值
  override patchFormGroupValue(data?: any): void {
    this.formGroup.patchValue({
      trainNo: data.trainNo, // 角色名稱
      trainKind: data.kind, // 種類
      fromStop: data.fromStop, // 起站
      toStop: data.toStop, // 迄站
      takeDate: data.takeDate, // 搭乘日期
      fromStopTime: data.fromStopTime, // 搭乘時間
      price: data.price, // 票價
    });
  }

  /**
   * 檢查 rowData 的 uuid (唯一值) 是否與 當前 SelectedData 相同
   * @param rowData
   * @returns
   */
  isChecked(rowData: any): boolean {
    // 有選擇的資料 且有進行過 select 動作
    // if (!this.selectedData && this.rowSelected === false) {
    //   return false;
    // }
    console.log(this.selectedData);

    return this.selectedData && this.selectedData.uuid === rowData.uuid;
  }

  /**
   * 取消選取
   */
  cancel() {
    // this.selectedData = null;
    // this.rowSelected = false;
    this.detailVisible = false;
  }

  /**
   * 取得此車次選擇的表單資料
   * @returns
   */
  getSelectedTrainInfo(): BookTicketResource {
    return this.ticketInfo;
  }
}
