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
import { FormControl, FormGroup, Validators } from '@angular/forms';
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
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { SaveEmailTemplateComponent } from './save-email-template/save-email-template.component';
import { DialogFormComponent } from '../../../../shared/component/dialog-form/dialog-form.component';
import { EmailTemplateService } from '../../services/email-template.service';
import { finalize } from 'rxjs/internal/operators/finalize';
import { EmailTemplateGottenData } from '../../models/email-template-queried-resource.model';

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
    EmailTemplateService,
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
    private optionService: OptionService,
    private emailTemplateService: EmailTemplateService,
    public override messageService: SystemMessageService,
  ) {
    super();
  }

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      templateKey: new FormControl('', Validators.required),
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
  }

  ngOnDestroy(): void {}

  /**
   * 查詢信件範本總覽
   */
  query() {
    this.submitted = true;

    if (this.formGroup.invalid || !this.submitted) {
      return;
    }

    let formData = this.formGroup.value;
    console.log(formData);
    this.emailTemplateService
      .getEmailTemplateByKey(formData.templateKey)
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
        }),
      )
      .subscribe((res) => {
        console.log(res);
        this.openFormDialog(FormAction.EDIT, res);
      });
  }

  /**
   * 檢查是否編輯過
   * @param data
   * @returns
   */
  checkIsEdited(data: EmailTemplateGottenData): boolean {
    if (data.subject || data.content) {
      return false;
    }
    return true;
  }

  /**
   * 新增一筆 Email 範本資料表單
   */
  onAdd() {
    this.openFormDialog(FormAction.ADD);
  }

  /**
   * 編輯一筆 Email 範本資料
   */
  onEdit() {
    this.openFormDialog(FormAction.EDIT, this.rowCurrentData);
  }

  /**
   * 開啟 Dialog 表單
   * @returns DynamicDialogRef
   */
  openFormDialog(formAction?: FormAction, data?: any): DynamicDialogRef {
    this.dialogOpened = true;

    const ref = this.dialogService.open(DialogFormComponent, {
      header: formAction === FormAction.ADD ? '新增一筆資料' : '更新一筆資料',
      width: '80%', // 寬度建議不要 100%，留點邊框比較像視窗
      height: '90vh', // 直接設為視窗高度的 85%，這樣上下就會非常寬敞
      contentStyle: {
        overflow: 'auto',
        padding: '0', // 移除預設 padding，由元件內部控制
      },
      baseZIndex: 10000,
      maximizable: true,
      data: {
        action: formAction,
        data: data,
      },
      templates: {
        content: SaveEmailTemplateComponent,
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

  /**
   * Table Action 按鈕按下去的時候要把該筆資料記錄下來。
   * @param rowData 點選的資料
   */
  override clickRowActionMenu(rowData: any): void {
    // console.log('clickRowActionMenu rowData = ' + JSON.stringify(rowData));
    this.rowCurrentData = rowData;
    console.log(this.rowCurrentData);

    // 開啟 Dialog
    this.openFormDialog(FormAction.EDIT, this.rowCurrentData);
  }
}
