import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import * as XLSX from 'xlsx';
import { SharedModule } from '../../../../shared/shared.module';
import { CoreModule } from '../../../../core/core.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BaseUploadCompoent } from '../../../../shared/component/base/base-upload.component';
import { ExcelData } from '../../../../shared/models/excel-data.model';
import { LoadingMaskService } from '../../../../core/services/loading-mask.service';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { JspreadsheetWrapper } from '../../../../shared/wrapper/jspreadsheet-wrapper';
import jspreadsheet from 'jspreadsheet-ce';
import {
  defaultIfEmpty,
  finalize,
  firstValueFrom,
  map,
  Subject,
  takeUntil,
} from 'rxjs';
import { OptionService } from '../../../../shared/services/option.service';
import { DataType } from '../../../../core/enums/data-type.enum';
import { BaseFormCompoent } from '../../../../shared/component/base/base-form.component';
import { TemplateUploadService } from '../../services/template-upload.service';
import { TemplateType } from '../../../../core/enums/template-type.enum';
import { TemplateQueriedResource } from '../../models/template-queried-resource.model';
import { error } from 'console';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, SharedModule],
  providers: [SystemMessageService, LoadingMaskService],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss',
})
export class UploadComponent
  extends BaseUploadCompoent
  implements OnInit, AfterViewInit, OnDestroy
{
  sheetNameOptions: any[] = [];

  showResult: boolean = false;

  currentData!: TemplateQueriedResource;

  templateForm!: FormGroup;

  // 訂閱
  private readonly _destroying$ = new Subject<void>();

  spreadsheet: any;
  templates: any[] = [];

  constructor(
    private optionService: OptionService,
    private templateUploadService: TemplateUploadService,
  ) {
    super();
  }

  async ngOnInit(): Promise<void> {
    // 上方查詢用表單
    this.formGroup = new FormGroup({
      templateType: new FormControl('', [Validators.required]),
    });

    // 下方 Form 表單
    this.templateForm = new FormGroup({
      name: new FormControl('', [Validators.required]),
      templateType: new FormControl('', [Validators.required]),
      filePath: new FormControl('', [Validators.required]),
      sheetName: new FormControl('', [Validators.required]),
      fileName: new FormControl('', [Validators.required]),
    });

    this.templates = await firstValueFrom(
      this.optionService.getSettingTypes(DataType.TEMPLATE).pipe(
        // 根據 Enum 轉換 Label
        map((res) => {
          res.forEach((option) => {
            option.label =
              TemplateType[option.value as keyof typeof TemplateType];
          });
          return res;
        }),
        defaultIfEmpty([]),
        takeUntil(this._destroying$),
      ),
    );
  }

  ngOnDestroy(): void {
    this._destroying$.unsubscribe;
  }

  /**
   * 進行查詢
   */
  query() {
    this.submitted = true;

    if (!this.submitted || this.formGroup.invalid) {
      return;
    }

    this.showResult = true;
    let formData = this.formGroup.value;

    this.loadingMaskService.show();
    this.templateUploadService
      .queryTemplate(formData.templateType)
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
        }),
      )
      .subscribe({
        next: (res) => {
          if (!res) {
            this.templateForm.reset();
            this.templateForm.patchValue({
              name: TemplateType[
                formData.templateType as keyof typeof TemplateType
              ],
              templateType: formData.templateType,
            });
          } else {
            this.templateForm.patchValue({
              name: res.name,
              templateType: res.type,
              filePath: res.filePath + '/' + res.fileName,
            });
            this.currentData = res;
            console.log(this.currentData);
          }
        },
        error: (error) => {
          this.messageService.error(error);
        },
      });
  }

  /**
   * 清除
   */
  clear() {
    this.showResult = false;
    this.formGroup.reset();
    this.templateForm.reset();
  }

  /**
   * Patch FormGroup 的值
   * @param data
   */
  override patchFormGroupValue(data?: any) {}

  /**
   * 取得 FormControl。
   * @param formControlName formControlNameformControl 的名稱
   * @returns FormControl
   */
  override formControl(formControlName: string): FormControl {
    return this.formGroup.get(formControlName) as FormControl;
  }

  /**
   * 判斷 formControl 欄位是否有錯誤。
   * @param formControlName formControl 的名稱
   * @returns boolean 欄位是否有錯誤
   */
  override formControlInvalid(formControlName: string): boolean {
    const formControl = this.formGroup.get(formControlName);
    if (formControl) {
      return formControl.invalid && (formControl.dirty || this.submitted);
    } else {
      return false;
    }
  }

  /**
   * 判斷 Template formControl 欄位是否有錯誤。
   * @param formControlName formControl 的名稱
   * @returns boolean 欄位是否有錯誤
   */
  templateFormControl(formControlName: string): FormControl {
    return this.templateForm.get(formControlName) as FormControl;
  }

  /**
   * 判斷 Template formControl 欄位是否有錯誤。
   * @param formControlName formControl 的名稱
   * @returns boolean 欄位是否有錯誤
   */
  templateFormControlInvalid(formControlName: string): boolean {
    const formControl = this.templateForm.get(formControlName);
    if (formControl) {
      return formControl.invalid && (formControl.dirty || this.submitted);
    } else {
      return false;
    }
  }

  /**
   * 處理上傳檔案的解析
   * @param event
   * @param fileNameFormControlName
   * @returns
   */
  override fileUploadHandler(
    event: any,
    fileNameFormControlName: string,
  ): void {
    // 清除檔案預覽
    this.destroyJExcel();

    // 先清除畫面上的訊息，避免錯誤重複或被前一個訊息誤導
    this.messageService.clear();

    this.selectedFile = event.files[0] as File;
    // 檢查檔案大小是否超過上限
    this.fileSizeInvalid = this.excelFileReaderService.fileSizeInvalid(
      this.selectedFile,
      this.maxFileSize,
    );
    if (this.fileSizeInvalid) {
      // 如果檔案太大，把檔案名稱寫回欄位，為了消掉必填的驗證
      this.formControl(fileNameFormControlName).patchValue(
        this.selectedFile.name,
      );
      // sheetNameOptions = [];
      this.formControl(fileNameFormControlName).markAsDirty();
      // 清除檔案選取元件內容，不然不能重選檔案
      this.fileUploadComponent.clear();
      return;
    }

    this.loadingMaskService.show();
    // 讀取檔案內容
    this.excelFileReaderService
      .parseFile(this.selectedFile)
      .then((result) => {
        this.afterFileParseSuccess(result);
        // 把解析後的 XLSX workBook 資料放到全域變數，預覽會用到
        this.workbook = result.workBook;
        this.loadingMaskService.hide();
      })
      .catch((error) => {
        // Handle the error
        this.afterFileParseFail();
        this.messageService.error(error);
      })
      .finally(() => {
        // 清除檔案選取元件內容，不然不能重選檔案
        this.fileUploadComponent.clear();
        this.loadingMaskService.hide();
      });
  }

  /**
   * 清除檔案預覽
   */
  override destroyJExcel(): void {
    this.showPreview = false;
    this.jexcel?.destroy();
  }

  /**
   * Parse Excel 成功後的處理
   * @param result: Excel Data
   */
  afterFileParseSuccess(result: ExcelData): void {
    // 設定 sheetname 下拉選單
    this.sheetNameOptions = result.sheetNameOptions;

    this.templateForm.patchValue({
      fileName: result.fileName,
      sheetName: result.sheetNameOptions[0].value,
    });
  }

  /**
   * Parse Excel 失敗後的處理，重置上方表單內容
   */
  afterFileParseFail(): void {
    this.sheetNameOptions = [];
    this.formControl('fileName').reset();
    this.formControl('sheetName').reset();
  }

  /**
   * 上傳資料
   */
  upload() {
    this.submitted = true;

    console.log(this.templateForm.invalid);
    if (!this.submitted || !this.workbook || this.templateForm.invalid) {
      return;
    }

    console.log('workbook = ' + JSON.stringify(this.workbook));
    // 轉成 Blob
    const excelBuffer: any = XLSX.write(this.workbook, {
      bookType: 'xlsx',
      type: 'array',
    });
    const blob: Blob = new Blob([excelBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    // 轉成 File
    const file = new File([blob], 'data.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    let templateData = this.templateForm.value;
    // 範例: /home/user/documents/report.xlsx
    const parts = templateData.filePath.split('/');
    const fullFileName = parts.pop() || ''; // report.xlsx
    const middlePath = parts.join('/'); // /home/user/documents
    const [fileName, fileType] = fullFileName.split('.'); // xlsx

    const resource = {
      name: templateData.name,
      type: templateData.templateType,
      fileType: fileType,
      filePath: middlePath,
      fileName: fullFileName,
    };
    console.log(resource);

    const formData = new FormData();
    // 轉換 JSON 為 Blob 並加入 FormData
    formData.append(
      'resource',
      new Blob([JSON.stringify(resource)], { type: 'application/json' }),
    );
    formData.append('file', file);

    this.loadingMaskService.show();

    this.templateUploadService
      .upload(formData)
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
          location.reload();
          this.query();
        }),
      )
      .subscribe({
        next: (res) => {
          if (res.code === '200' || res.code === '201') {
            this.messageService.success(res.message);
          } else {
            this.messageService.error(res.message);
          }
        },
        error: (error) => {
          this.messageService.error(error);
        },
      });
  }
}
