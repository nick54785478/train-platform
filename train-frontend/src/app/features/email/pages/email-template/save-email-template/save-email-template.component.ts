import { Component, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../shared/shared.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Option } from '../../../../../shared/models/option.model';
import { CoreModule } from '../../../../../core/core.module';
import { SystemMessageService } from '../../../../../core/services/system-message.service';
import { BaseFormCompoent } from '../../../../../shared/component/base/base-form.component';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SettingService } from '../../../../setting/service/setting.service';
import { OptionService } from '../../../../../shared/services/option.service';
import { Location } from '@angular/common';
import { environment } from '../../../../../../environments/environment';
import { CreateSetting } from '../../../../setting/models/create-setting-request.model';
import { finalize } from 'rxjs/internal/operators/finalize';
import { UpdateSetting } from '../../../../setting/models/update-setting-request.model';
import { SettingQueriedResource } from '../../../../setting/models/setting-queried-resource.model';
import { DataType } from '../../../../../core/enums/data-type.enum';
import { EmailTemplateGottenData } from '../../../models/email-template-queried-resource.model';
import * as beautify from 'js-beautify';
import { SaveEmailTemplate } from '../../../models/save-email-template-resource.model';
import { EmailTemplateService } from '../../../services/email-template.service';

@Component({
  selector: 'app-save-email-template',
  standalone: true,
  imports: [SharedModule, CoreModule],
  providers: [SystemMessageService],
  templateUrl: './save-email-template.component.html',
  styleUrl: './save-email-template.component.scss',
})
export class SaveEmailTemplateComponent
  extends BaseFormCompoent
  implements OnInit, OnDestroy
{
  dataTypes: Option[] = [];
  activeFlags: Option[] = [];

  constructor(
    private location: Location,
    private dialogConfig: DynamicDialogConfig,
    public ref: DynamicDialogRef,
    private emailTemplateService: EmailTemplateService,
    private optionService: OptionService,
    private systemMessageService: SystemMessageService,
  ) {
    super();
  }

  ngOnInit(): void {
    // 監聽上一頁切換，關閉 Dialog
    this.location.onUrlChange(() => {
      this.onCloseForm();
    });

    this.formAction = this.dialogConfig.data['action'];

    this.formGroup = new FormGroup({
      subject: new FormControl('', [Validators.required]),
      content: new FormControl('', [Validators.required]),
      templateName: new FormControl('', [Validators.required]),
      templateKey: new FormControl('', [Validators.required]),
      activeFlag: new FormControl('', [Validators.required]),
    });

    console.log(this.dialogConfig.data);

    // 若為 'edit' => 編輯，
    if (this.formAction === 'edit') {
      this.patchFormGroupValue(this.dialogConfig.data['data']);
    }

    console.log(this.ref);
    // 取得 DataTypes 下拉資料
    this.optionService.getDataTypes().subscribe((res) => {
      this.dataTypes = res;
    });

    // 取得 activeFlag 下拉資料
    this.optionService.getSettingTypes(DataType.YES_NO).subscribe((res) => {
      this.activeFlags = res;
    });
  }

  ngOnDestroy() {}

  onSubmit() {
    // 將表單資料設置進 CreateSetting
    const request: SaveEmailTemplate = { ...this.formGroup.value };
    this.emailTemplateService
      .saveEmailTemplate(request)
      .pipe(
        finalize(() => {
          // 無論成功或失敗都會執行
          this.clear();
          // this.loading = false;
          setTimeout(() => {
            location.reload();
          }, 500);
        }),
      )
      .subscribe({
        next: (res) => {
          this.systemMessageService.success('新增資料成功');
          this.onCloseForm();
        },
        error: (error) => {
          this.systemMessageService.error(error.message);
          // this.onCloseForm();
        },
      });
  }

  /**
   * 關閉 Dialog
   */
  onCloseForm() {
    console.log('關閉 Dialog');
    this.ref.close();
    this.clear();
  }

  /**
   * 清除表單資料
   */
  clear() {
    this.formGroup.reset();
  }

  /**
   * 要編輯時，設值進Form表單
   * @param data
   */
  override patchFormGroupValue(data: EmailTemplateGottenData): void {
    this.formGroup.patchValue({
      subject: data.subject,
      content: data.content,
      templateName: data.templateName,
      templateKey: data.templateKey,
      activeFlag: data.activeFlag,
    });

    // 載入後自動排版一次，讓畫面看起來專業點
    this.formatContent();
  }

  /**
   * 執行 HTML 自動排版
   */
  formatContent(): void {
    const contentControl = this.formGroup.get('content');
    const rawValue = contentControl?.value;

    if (rawValue) {
      // 設定排版參數
      const options = {
        indent_size: 2, // 縮排 2 格
        indent_char: ' ',
        max_preserve_newlines: 1, // 最多保留連續 1 個換行
        preserve_newlines: true, // 是否保留換行
        indent_inner_html: true, // <html> 內部的標籤是否要縮排
        wrap_line_length: 0, // 不要強行換行（設為 0 代表不限制）
        extra_liners: [], // 不要在特定標籤前加空行
      };

      // 改用這行：調用其中的 html 方法
      const formatted = beautify.html(rawValue, options);
      // 更新表單值
      contentControl.setValue(formatted);
    }
  }
}
