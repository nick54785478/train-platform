import { Component, DoCheck, OnInit } from '@angular/core';
import { CoreModule } from '../../../../core/core.module';
import { SharedModule } from '../../../../shared/shared.module';
import { BaseFormCompoent } from '../../../../shared/component/base/base-form.component';
import { BaseFormTableCompoent } from '../../../../shared/component/base/base-form-table.component';
import { FormControl, FormGroup } from '@angular/forms';
import { MoneyAccountQueriedResource } from '../../models/money-account-queried-resource.model';
import { Observable } from 'rxjs/internal/Observable';
import { AccountService } from '../../services/account.service';
import { StorageService } from '../../../../core/services/storage.service';
import {
  finalize,
  firstValueFrom,
  lastValueFrom,
  map,
  of,
  Subject,
  takeUntil,
} from 'rxjs';
import { LoadingMaskService } from '../../../../core/services/loading-mask.service';
import { SystemStorageKey } from '../../../../core/enums/system-storage.enum';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { MenuItem } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { DialogFormComponent } from '../../../../shared/component/dialog-form/dialog-form.component';
import { TicketRecordComponent } from './ticket-record/ticket-record.component';
import { DepositFormComponent } from './deposit-form/deposit-form.component';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [SharedModule, CoreModule],
  providers: [
    SystemMessageService,
    LoadingMaskService,
    AccountService,
    DialogService,
  ],
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss',
})
export class AccountComponent extends BaseFormTableCompoent implements OnInit {
  account!: any;

  mode: string = '';

  tableVisible: boolean = false;
  //Table Row Actions 選單。
  rowActionMenu: MenuItem[] = [];
  /**
   * 用來取消訂閱
   */
  readonly _destroying$ = new Subject<void>();

  constructor(
    private accountService: AccountService,
    private storageService: StorageService,
    public dialogService: DialogService
  ) {
    super();
  }
  async ngOnInit(): Promise<void> {
    // 初始化表單
    this.formGroup = new FormGroup({
      username: new FormControl(''),
      name: new FormControl(''),
      email: new FormControl(''),
      balance: new FormControl(''),
    });

    const username = await firstValueFrom(
      of(
        this.storageService.getLocalStorageItem(SystemStorageKey.USERNAME) ||
          this.storageService.getSessionStorageItem(SystemStorageKey.USERNAME)
      )
    );

    if (username) {
      this.account = await lastValueFrom(
        this.queryAccount(username).pipe(
          map((res) => {
            if (res.code === 'VALIDATION_EXCEPTION' && res?.message) {
              this.messageService.error(res?.message);
            }
            return res;
          })
        )
      );
      this.tableData = await lastValueFrom(
        this.accountService.getBooking(username).pipe(
          map((res) => {
            return res.bookedDatas;
          })
        )
      );
    }

    this.cols = [
      {
        field: 'number',
        header: '車次',
      },
      // {
      //   field: 'kind',
      //   header: '火車種類',
      // },
      // {
      //   field: 'carNo',
      //   header: '車廂',
      // },
      // {
      //   field: 'seatNo',
      //   header: '座位',
      // },
      {
        field: 'from',
        header: '起站',
      },
      // {
      //   field: 'startTime',
      //   header: '發車時間',
      // },
      {
        field: 'to',
        header: '迄站',
      },
      {
        field: 'takeDate',
        header: '乘車日期',
      },
    ];

    this.formGroup.patchValue({
      username: this.account.username,
      name: this.account.name,
      email: this.account.email,
      balance: this.account.balance,
    });
  }

  /**
   * 取得該使用者儲值帳戶資料
   * @param username
   * @returns
   */
  queryAccount(username: string): Observable<MoneyAccountQueriedResource> {
    return this.accountService.getAccountData(username).pipe(
      map((res) => {
        return res;
      })
    );
  }

  /**
   * 查詢該使用者的訂票資訊
   *
   * @param username
   */
  queryBooking(username: string) {
    return this.accountService.getBooking(username).pipe(
      map((res) => {
        return res;
      })
    );
  }

  /**
   * 顯示訂票資料
   */
  onToggleTable() {
    this.tableVisible = !this.tableVisible;
  }

  /**
   * Table Action 按鈕按下去的時候要把該筆資料記錄下來。
   * @param rowData 點選的資料
   */
  override clickRowActionMenu(rowData: any) {
    this.selectedData = rowData;
    // 開啟 Dialog
    this.openFormDialog(this.selectedData);
  }

  /**
   * 開啟儲值頁面
   * @returns
   */
  openDepositDialog(uuid: string) {
    this.dialogOpened = true;
    const ref = this.dialogService.open(DialogFormComponent, {
      header: '帳號儲值',
      width: '70%',
      contentStyle: { overflow: 'auto' },
      baseZIndex: 10000,
      maximizable: true,
      data: uuid,
      templates: {
        content: DepositFormComponent,
      },
    });
    // Dialog 關閉後要做的事情
    ref?.onClose
      .pipe(takeUntil(this._destroying$))
      .subscribe((returnData: any) => {
        setTimeout(() => {
          location.reload();
        }, 1000);
        console.log('關閉 Dialog');
        this.dialogOpened = false;
      });
    return ref;
  }

  /**
   * 開啟 Dialog
   * @param data
   */
  openFormDialog(data: any) {
    this.dialogOpened = true;
    const ref = this.dialogService.open(DialogFormComponent, {
      header: '訂票詳細資料',
      width: '70%',
      contentStyle: { overflow: 'auto' },
      baseZIndex: 10000,
      maximizable: true,
      data: {
        data: data,
      },
      templates: {
        content: TicketRecordComponent,
      },
    });
    // Dialog 關閉後要做的事情
    ref?.onClose
      .pipe(takeUntil(this._destroying$))
      .subscribe((returnData: any) => {
        console.log('關閉 Dialog');
        this.dialogOpened = false;
      });
    return ref;
  }
}
