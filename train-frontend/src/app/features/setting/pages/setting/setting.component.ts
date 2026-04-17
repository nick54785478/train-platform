import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import {
  DialogService,
  DynamicDialogConfig,
  DynamicDialogRef,
} from 'primeng/dynamicdialog';
import { SharedModule } from '../../../../shared/shared.module';
import { CoreModule } from '../../../../core/core.module';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { OptionService } from '../../../../shared/services/option.service';
import { Option } from '../../../../shared/models/option.model';
import { SettingService } from '../../service/setting.service';
import { MenuItem } from 'primeng/api';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { Subject } from 'rxjs/internal/Subject';
import { DialogFormComponent } from '../../../../shared/component/dialog-form/dialog-form.component';
import { FormAction } from '../../../../core/enums/form-action.enum';
import { SettingFormComponent } from './setting-form/setting-form.component';
import { DialogConfirmService } from '../../../../core/services/dialog-confirm.service';
import { DataType } from '../../../../core/enums/data-type.enum';
import { finalize, first, firstValueFrom, lastValueFrom, map, of } from 'rxjs';
import { StorageService } from '../../../../core/services/storage.service';
import { SystemStorageKey } from '../../../../core/enums/system-storage.enum';
import { SettingQueriedResource } from '../../models/setting-queried-resource.model';
import { error } from 'console';
import { SettingTableColumnCustomisation } from '../../enums/setting-tablecolumn-customisation.enum';
import { UpdateCustomizedValueResource } from '../../../../shared/models/update-customized-value-resource.model';
import { CustomisationService } from '../../../../shared/services/customisation.service';

@Component({
  selector: 'app-setting',
  standalone: true,
  imports: [SharedModule, CoreModule],
  providers: [
    DialogService,
    DialogConfirmService,
    DynamicDialogConfig,
    SystemMessageService,
    OptionService,
    DynamicDialogRef,
  ],
  templateUrl: './setting.component.html',
  styleUrl: './setting.component.scss',
})
export class SettingComponent implements OnInit, OnDestroy {
  dataTypes: Option[] = [];
  activeFlags: Option[] = [];
  // Table Row Actions 選單。
  rowActionMenu: MenuItem[] = [];
  username!: string;
  fields: Option[] = []; // Table 可視選項
  selectedFields: Option[] = []; // 被選擇的 Table Column 名
  viewCols: string[] = []; // 可視清單

  /**
   * 用來取消訂閱
   */
  readonly _destroying$ = new Subject<void>();

  // 現在選取的那一筆
  rowCurrentData: any;
  tableData: SettingQueriedResource[] = []; // 查詢表格資料
  cols: any[] = []; // 表格資訊
  filteredCols: any[] = []; // 過濾後的表格資訊
  selectedData: [] = []; // 選擇資料清單
  dialogOpened: boolean = false;
  formAction!: FormAction; // Dialog 操作

  constructor(
    private dialogConfirmService: DialogConfirmService,
    private dynamicDialogRef: DynamicDialogRef,
    public dialogService: DialogService,
    private storageService: StorageService,
    private optionService: OptionService,
    private settingService: SettingService,
    private customisationService: CustomisationService,
    public messageService: SystemMessageService,
  ) {}

  ngOnDestroy(): void {
    // 保證組件銷毀時關閉 Dialog
    if (this.dynamicDialogRef) {
      this.dynamicDialogRef.close();
    }
    this._destroying$.closed;
    this._destroying$.unsubscribe();
  }

  formGroup!: FormGroup;

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      dataType: new FormControl(''),
      type: new FormControl(''),
      name: new FormControl(''),
      activeFlag: new FormControl(''),
    });

    // 取得 DataTypes 下拉資料
    this.optionService.getDataTypes().subscribe((res) => {
      this.dataTypes = res;
    });
    // 取得 activeFlag 下拉資料
    this.optionService.getSettingTypes(DataType.YES_NO).subscribe((res) => {
      this.activeFlags = res;
    });

    this.cols = [
      { field: 'dataType', header: '配置種類' },
      { field: 'type', header: '類別' },
      { field: 'name', header: '名稱' },
      { field: 'value', header: '值' },
      { field: 'description', header: '說明' },
      { field: 'priorityNo', header: '排序' },
    ];
    this.loadTableViewOptions();

    this.query();
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
          'SETTING_TABLE_COLUMN',
        )
        .pipe(
          map((res) => {
            return res.value;
          }),
        ),
    );

    // 取得 Table Column 可視設定資料
    this.fields = await firstValueFrom(
      this.optionService.getSettingsByDataTypeAndType('SETTING_TABLE_COLUMN'),
    );

    // 可視清單，控制該 table column 是否可視
    this.viewCols = this.selectedFields.map((e) => e.value);

    // 只保留在 viewCols 中的欄位
    this.filteredCols = this.cols.filter((col) =>
      this.viewCols.includes(col.field),
    );
  }

  /**
   * 開啟 Dialog 表單
   * @returns DynamicDialogRef
   */
  openFormDialog(formAction?: FormAction, data?: any): DynamicDialogRef {
    this.dialogOpened = true;

    const ref = this.dialogService.open(DialogFormComponent, {
      header: formAction === FormAction.ADD ? '新增一筆資料' : '更新一筆資料',
      width: '70%',
      contentStyle: { overflow: 'auto' },
      baseZIndex: 10000,
      maximizable: true,
      data: {
        action: formAction,
        data: data,
      },
      templates: {
        content: SettingFormComponent,
      },
    });
    // Dialog 關閉後要做的事情
    ref?.onClose
      .pipe(takeUntil(this._destroying$))
      .subscribe((returnData: any) => {
        console.log('關閉 Dialog');
        this.dialogOpened = false;
        this.query();
      });
    return ref;
  }

  // async loadOptions() {
  //   this.fields = await firstValueFrom(
  //     this.optionService.getSettingsByDataTypeAndType('SETTING_TABLE_COLUMN')
  //   );

  //   this.selectedFields = this.fields.filter((e) =>
  //     this.viewCols.includes(e.value)
  //   );

  //   this.cdr.detectChanges(); // 強制刷新 UI
  // }

  /**
   * 取得 Table 所有 columns，後續用於個人化配置
   */
  getFields() {
    this.optionService
      .getSettingsByDataTypeAndType('SETTING_TABLE_COLUMN')
      .subscribe((res) => {
        this.fields = res;
      });
  }

  /**
   * 新增一筆設定資料表單
   */
  onAdd() {
    this.openFormDialog(FormAction.ADD);
  }

  /**
   * 編輯一筆設定資料
   */
  onEdit() {
    this.openFormDialog(FormAction.EDIT, this.rowCurrentData);
  }

  /**
   * 關閉 Dialog 表單
   */
  closeFormDialog() {
    this.dialogOpened = false;
    this.dynamicDialogRef.close();
    console.log('關閉 Dialog 表單');
  }

  /**
   * 清除表單資料
   */
  clear() {
    this.formGroup.reset();
    this.tableData = [];
  }

  /**
   * 透過特定條件查詢設定資料
   */
  query() {
    let formData = this.formGroup.value;
    console.log(formData);
    this.settingService
      .query(
        formData.dataType,
        formData.type,
        formData.name,
        formData.activeFlag,
      )
      .subscribe({
        next: (res) => {
          this.messageService.success('查詢成功');
          this.tableData = res;
        },
        error: (error) => {
          this.messageService.error(error.message);
        },
      });
  }

  /**
   * Table Action 按鈕按下去的時候要把該筆資料記錄下來。
   * @param rowData 點選的資料
   */
  clickRowActionMenu(rowData: any): void {
    // console.log('clickRowActionMenu rowData = ' + JSON.stringify(rowData));
    this.rowCurrentData = rowData;
    console.log(this.rowCurrentData);

    // 開啟 Dialog
    this.openFormDialog(FormAction.EDIT, this.rowCurrentData);
  }

  /**
   * 刪除特定資料
   * @param id
   */
  delete(id: number) {
    console.log(id);
    this.dialogConfirmService.confirmDelete(() => {
      // 確認後的動作
      this.settingService.delete(id).subscribe({
        next: (res) => {
          this.messageService.success(res.message);
          // 再查一次
          this.query();
        },
        error: (error) => {
          this.messageService.error(error.message);
        },
      });
    });
  }

  /**
   * reset 個人化設定(該使用者可看到的 Table Columns)
   */
  resetFields() {
    this.selectedFields = this.fields.filter((e) =>
      this.viewCols.includes(e.value),
    );
  }

  /**
   * 提交個人化設定(該使用者可看到的 Table Columns)
   */
  submitCustomisation() {
    let selectValues = this.selectedFields.map((e) => e.label);
    let request: UpdateCustomizedValueResource = {
      dataType: DataType.CUSTOMISATION,
      type: 'SETTING_TABLE_COLUMN',
      valueList: selectValues,
    };
    this.customisationService
      .updateCustomizedValue(this.username, request)
      .subscribe({
        next: (res) => {
          if (res.code === '200' || res.code === '201') {
            this.messageService.success(res.message);
            location.reload();
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
      SettingTableColumnCustomisation[
        label as keyof typeof SettingTableColumnCustomisation
      ] || label
    );
  }
}
