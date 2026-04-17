import { Component, OnDestroy, OnInit } from '@angular/core';
import { CoreModule } from '../../../../core/core.module';
import { Option } from '../../../../shared/models/option.model';
import { SharedModule } from '../../../../shared/shared.module';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { OptionService } from '../../../../shared/services/option.service';
import { DataType } from '../../../../core/enums/data-type.enum';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { TrainService } from '../../services/train.service';
import { finalize, firstValueFrom, map, of, Subject, takeUntil } from 'rxjs';
import { MenuItem } from 'primeng/api';
import { BaseHeaderLineTableCompoent } from '../../../../shared/component/base/base-header-line-table.component';
import { LoadingMaskService } from '../../../../core/services/loading-mask.service';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { DialogFormComponent } from '../../../../shared/component/dialog-form/dialog-form.component';
import { TrainDetailComponent } from './train-detail/train-detail.component';
import { TrainDialogType } from '../../../../core/enums/train-dialog-type.enum';
import { DialogConfig } from '../../../../shared/models/dialog-config.model';
import { TrainStopsComponent } from './train-stops/train-stops.component';
import { SystemStorageKey } from '../../../../core/enums/system-storage.enum';
import { StorageService } from '../../../../core/services/storage.service';
import { CustomisationService } from '../../../../shared/services/customisation.service';
import { UpdateCustomizedValueResource } from '../../../../shared/models/update-customized-value-resource.model';
import { TrainTableColumnCustomisation } from '../../enums/train-tablecolumn-customisation.enum';

@Component({
  selector: 'app-train',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [DialogService, SystemMessageService],
  templateUrl: './train.component.html',
  styleUrl: './train.component.scss',
})
export class TrainComponent
  extends BaseHeaderLineTableCompoent
  implements OnInit, OnDestroy
{
  trainNoList: Option[] = []; // Active Flag 的下拉式選單
  stops: Option[] = []; // 車站資料的下拉式選單
  kinds: Option[] = []; // 車種資料的下拉式選單
  protected override lineTableVisibled: boolean = false;
  readonly _destroying$ = new Subject<void>(); // 用來取消訂閱

  filteredCols: any[] = []; // 過濾後的表格資訊
  username!: string;
  fields: Option[] = []; // Table 可視選項
  selectedFields: Option[] = []; // 被選擇的 Table Column 名
  viewCols: string[] = []; // 可視清單

  //Table Row Actions 選單。
  rowActionMenu: MenuItem[] = [];

  constructor(
    private storageService: StorageService,
    private optionService: OptionService,
    private trainService: TrainService,
    private dialogService: DialogService,
    private customisationService: CustomisationService,
  ) {
    super();
  }

  ngOnInit(): void {
    // 初始化表單
    this.formGroup = new FormGroup({
      trainNo: new FormControl(''), // 車次
      trainKind: new FormControl(''), // 車種
      fromStop: new FormControl(''), // 起站
      toStop: new FormControl(''), // 起站
      takeDate: new FormControl('', [Validators.required]), // 搭乘日期
      takeTime: new FormControl('', [Validators.required]), // 搭乘時間
    });

    this.headerCols = [
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
        header: '起始站',
        type: '',
      },
      {
        field: 'fromStopTime',
        header: '起始站發車時間',
        type: '',
      },
      {
        field: 'toStop',
        header: '終點站',
        type: '',
      },
      {
        field: 'toStopTime',
        header: '終點站到站時間',
        type: '',
      },
    ];

    this.lineCols = [
      {
        field: 'seq',
        header: '停靠站排序',
        type: '',
      },
      {
        field: 'stopName',
        header: '站名',
        type: '',
      },
      {
        field: 'stopTime',
        header: '抵達時間',
        type: '',
      },
    ];

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
          error.message,
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
          error.message,
        );
      },
    });

    this.loadTableViewOptions();
  }

  ngOnDestroy(): void {
    this._destroying$.unsubscribe();
    this._destroying$.closed;
  }

  /**
   * 查詢火車車次資料
   * @returns
   */
  query() {
    this.submitted = true;

    if (!this.submit || this.formGroup.invalid) {
      return;
    }

    this.loadingMaskService.show();
    const formData = this.formGroup.value;
    this.lineTableVisibled = false;
    this.trainService
      .query(
        formData.trainNo,
        formData.trainKind,
        formData.fromStop,
        formData.toStop,
        formData.takeDate,
        formData.takeTime,
      )
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
        }),
      )
      .subscribe({
        next: (res) => {
          this.headerTableData = res;
          this.messageService.success('查詢成功');
        },
        error: (error) => {
          this.messageService.error(error);
        },
      });
  }

  /**
   * Table Action 按鈕按下去的時候要把該筆資料記錄下來。
   * @param rowData 點選的資料
   */
  clickHeaderRowActionMenu(dialogType: string, rowData: any): void {
    // console.log('clickRowActionMenu rowData = ' + JSON.stringify(rowData));
    this.rowCurrentData = rowData;
    this.selectedHeaderData = rowData;
    console.log(this.rowCurrentData);

    // 開啟 Dialog
    this.openDialog(dialogType, this.rowCurrentData);
  }

  /**
   * Table Action 按鈕按下去的時候要把該筆資料記錄下來。
   * @param rowData 點選的資料
   */
  clickLineRowActionMenu(dialogType: string, rowData: any): void {
    // console.log('clickRowActionMenu rowData = ' + JSON.stringify(rowData));
    this.rowCurrentData = rowData;
    console.log(this.rowCurrentData);

    // 開啟 Dialog
    this.openDialog(dialogType, this.rowCurrentData);
  }

  /**
   * 顯示 Stop 詳細資料
   * @param rowData
   */
  showLineTable(event: any) {
    let trainData = event.data;
    this.lineTableVisibled = true;
    this.lineTableData = trainData.stops;
  }

  /**
   * 清除所有資料
   */
  protected override clear(): void {
    this.formGroup.reset();
    this.lineTableData = [];
    this.headerTableData = [];
    this.lineTableVisibled = false;
  }

  /**
   * 開啟 Dialog 表單
   * @returns DynamicDialogRef
   */
  openDialog(dialogType: string, rowData?: any): DynamicDialogRef {
    this.dialogOpened = true;

    console.log(rowData);

    let config: DialogConfig;
    // 根據 DialogType 設置值
    if (dialogType === TrainDialogType.TRAIN_DETAIL) {
      config = {
        component: TrainDetailComponent,
        dataAction: 'TRAIN_DETAIL',
        header: '車次詳細資料',
        data: {
          rowData: rowData,
          lineCols: this.lineCols,
        },
      };
    } else {
      config = {
        component: TrainStopsComponent,
        dataAction: 'STOP_DETAIL',
        header: '停靠站詳細資料',
        data: {
          uuid: this.selectedHeaderData.uuid,
          fromStop: rowData.stopName,
        },
      };
    }

    const ref = this.dialogService.open(DialogFormComponent, {
      header: config.header,
      width: '85%',
      contentStyle: { overflow: 'auto' },
      baseZIndex: 10000,
      maximizable: true,
      data: config.data,
      templates: {
        content: config.component,
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

  /**
   * 取得個人化設定(可以看到 Table 的欄位設定)
   */
  async loadTableViewOptions() {
    // 取得使用者名稱
    this.username = await firstValueFrom(
      of(
        this.storageService.getLocalStorageItem(SystemStorageKey.USERNAME) ||
          this.storageService.getSessionStorageItem(SystemStorageKey.USERNAME),
      ),
    );

    // 可以看到的欄位
    this.selectedFields = await firstValueFrom(
      this.customisationService
        .queryTableColumnCustomisation(
          this.username,
          DataType.CUSTOMISATION,
          'TRAIN_TABLE_COLUMN',
        )
        .pipe(
          map((res) => {
            return res.value;
          }),
        ),
    );

    // 取得 Table Column 可視設定資料
    this.fields = await firstValueFrom(
      this.optionService.getSettingsByDataTypeAndType('TRAIN_TABLE_COLUMN'),
    );

    // 可視清單，控制該 table column 是否可視
    this.viewCols = this.selectedFields.map((e) => e.value);

    // 只保留在 viewCols 中的欄位
    this.filteredCols = this.headerCols.filter((col) =>
      this.viewCols.includes(col.field),
    );
  }

  /**
   * 提交個人化設定(該使用者可看到的 Table Columns)
   */
  submitCustomisation() {
    let selectValues = this.selectedFields.map((e) => e.label);
    let request: UpdateCustomizedValueResource = {
      dataType: DataType.CUSTOMISATION,
      type: 'TRAIN_TABLE_COLUMN',
      valueList: selectValues,
    };
    this.customisationService
      .updateCustomizedValue(this.username, request)
      .pipe(
        finalize(() => {
          this.submitted = false;
        }),
      )
      .subscribe({
        next: (res) => {
          if (res.code === '200' || res.code === '201') {
            this.messageService.success(res.message);
            this.viewCols = this.selectedFields.map((e) => e.value);
            // 只保留在 viewCols 中的欄位
            this.filteredCols = this.headerCols.filter((col) =>
              this.viewCols.includes(col.field),
            );
            this.query();
          } else {
            this.messageService.error(res.message);
          }
        },
        error: (error) => {
          this.messageService.error(error);
        },
      });
  }

  /**
   * 取得 Enum 對應的 Column 中文名稱
   * @param label
   * */
  getColNameByField(label: string): string {
    return (
      TrainTableColumnCustomisation[
        label as keyof typeof TrainTableColumnCustomisation
      ] || label
    );
  }

  /**
   * reset 個人化設定(該使用者可看到的 Table Columns)
   */
  resetFields() {
    this.selectedFields = this.fields.filter((e) =>
      this.viewCols.includes(e.value),
    );
  }
}
