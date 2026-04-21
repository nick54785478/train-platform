import { Component, OnDestroy, OnInit } from '@angular/core';
import { BaseFormTableCompoent } from '../../../../shared/component/base/base-form-table.component';
import { CoreModule } from '../../../../core/core.module';
import { SharedModule } from '../../../../shared/shared.module';
import {
  DialogService,
  DynamicDialogConfig,
  DynamicDialogRef,
} from 'primeng/dynamicdialog';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { DialogConfirmService } from '../../../../core/services/dialog-confirm.service';
import { OptionService } from '../../../../shared/services/option.service';
import { Option } from '../../../../shared/models/option.model';
import { SettingService } from '../../../setting/service/setting.service';
import { DataType } from '../../../../core/enums/data-type.enum';
import { FormControl, FormGroup } from '@angular/forms';
import { MenuItem } from 'primeng/api';
import { Subject } from 'rxjs/internal/Subject';
import { FormAction } from '../../../../core/enums/form-action.enum';
import { UpdateCustomizedValueResource } from '../../../../shared/models/update-customized-value-resource.model';
import { CustomisationService } from '../../../../shared/services/customisation.service';
import { EmailTemplateTableColumnCustomisation } from '../../enums/EmailTemplateColumnCustomisation..enum';
import { firstValueFrom } from 'rxjs/internal/firstValueFrom';
import { StorageService } from '../../../../core/services/storage.service';
import { map } from 'rxjs/internal/operators/map';
import { SystemStorageKey } from '../../../../core/enums/system-storage.enum';
import { of } from 'rxjs/internal/observable/of';

@Component({
  selector: 'app-email-template',
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
  templateUrl: './email-template.component.html',
  styleUrl: './email-template.component.scss',
})
export class EmailTemplateComponent
  extends BaseFormTableCompoent
  implements OnInit, OnDestroy
{
  templateNames: Option[] = [];
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
  override cols: any[] = []; // 表格資訊
  override selectedData: [] = []; // 選擇資料清單
  override dialogOpened: boolean = false;
  filteredCols: any[] = [];

  constructor(
    private dialogConfirmService: DialogConfirmService,
    private dynamicDialogRef: DynamicDialogRef,
    public dialogService: DialogService,
    private storageService: StorageService,
    private optionService: OptionService,
    private settingService: SettingService,
    private customisationService: CustomisationService,
    public override messageService: SystemMessageService,
  ) {
    super();
  }

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      templateName: new FormControl(''),
      activeFlag: new FormControl(''),
    });

    // 取得範本下拉式選單
    this.optionService
      .getSettingTypes(DataType.EMAIL_TEMPLATE)
      .subscribe((e) => {
        this.templateNames = e;
      });
    // 取得 activeFlag 下拉資料
    this.optionService.getSettingTypes(DataType.YES_NO).subscribe((res) => {
      this.activeFlags = res;
    });

    this.cols = [
      { field: 'templateKey', header: '範本唯一值', key: 'TEMPLATE_KRY' },
      { field: 'templateName', header: '範本名稱', key: 'TEMPLATE_NAME' },
      { field: 'subject', header: '標題', key: 'SUBJECT' },
      { field: 'content', header: '內容', key: 'CONTENT' },
      { field: 'activeFlag', header: '是否生效', key: 'ACTIVE_FLAG' },
    ];

    this.loadTableViewOptions();
  }

  ngOnDestroy(): void {}

  query() {}

  onAdd() {}

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
          'EMAIL_TEMPLATE_TABLE_COLUMN',
        )
        .pipe(
          map((res) => {
            console.log(res);
            return res.value;
          }),
        ),
    );

    // 取得 Table Column 可視設定資料
    this.fields = await firstValueFrom(
      this.optionService.getSettingsByDataTypeAndType(
        'EMAIL_TEMPLATE_TABLE_COLUMN',
      ),
    );

    console.log(this.selectedFields);
    // 可視清單，控制該 table column 是否可視
    this.viewCols = this.selectedFields.map((e) => e.value);

    // 只保留在 viewCols 中的欄位
    this.filteredCols = this.cols.filter((col) =>
      this.viewCols.includes(col.key),
    );
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
   * 提交個人化設定(該使用者可看到的 Table Columns)
   */
  submitCustomisation() {
    let selectValues = this.selectedFields.map((e) => e.value);
    console.log(selectValues);
    let request: UpdateCustomizedValueResource = {
      dataType: DataType.CUSTOMISATION,
      type: 'EMAIL_TEMPLATE_TABLE_COLUMN',
      valueList: selectValues,
    };
    this.customisationService
      .updateCustomizedValue(this.username, request)
      .subscribe({
        next: (res) => {
          if (res.code === '200' || res.code === '201') {
            this.messageService.success(res.message);
            this.loadTableViewOptions();
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
   * reset 個人化設定(該使用者可看到的 Table Columns)
   */
  resetFields() {
    this.selectedFields = this.fields.filter((e) =>
      this.viewCols.includes(e.value),
    );
  }

  // /**
  //  * 取得 Enum 對應的 Column 中文名稱
  //  * @param label
  //  * */
  // getColNameByField(label: string): string {
  //   return (
  //     EmailTemplateTableColumnCustomisation[
  //       label as keyof typeof EmailTemplateTableColumnCustomisation
  //     ] || label
  //   );
  // }

  /**
   * 取得 Table 所有 columns，後續用於個人化配置
   */
  getFields() {
    this.optionService
      .getSettingsByDataTypeAndType('EMAIL_TEMPLATE_TABLE_COLUMN')
      .subscribe((res) => {
        this.fields = res;
      });
  }
}
