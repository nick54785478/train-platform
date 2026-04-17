import {
  AfterViewInit,
  Component,
  ElementRef,
  inject,
  ViewChild,
} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import * as XLSX from 'xlsx';
import jspreadsheet from 'jspreadsheet-ce';
import { FileUpload } from 'primeng/fileupload';
import { ExcelData } from '../../models/excel-data.model';
import { JspreadsheetWrapper } from '../../wrapper/jspreadsheet-wrapper';
import { ExcelFileReaderService } from '../../services/excel-file-reader.service';
import { SystemMessageService } from '../../../core/services/system-message.service';
import { LoadingMaskService } from '../../../core/services/loading-mask.service';
import { Option } from '../../../shared/models/option.model';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../shared.module';
import { CoreModule } from '../../../core/core.module';

/**
 * 定義基礎的 Form 表單 Component
 */
@Component({
  selector: 'app-base-form-compoent',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [LoadingMaskService, SystemMessageService],
  template: '',
})
export abstract class BaseUploadCompoent implements AfterViewInit {
  protected elementRef = inject(ElementRef);
  protected loadingMaskService = inject(LoadingMaskService);
  protected messageService = inject(SystemMessageService);
  protected excelFileReaderService = inject(ExcelFileReaderService);

  constructor() {}

  /**
   * 選取的檔案。
   */
  selectedFile: File | undefined;

  /**
   * 檔案大小上限: 多少 MB。
   * 預設是 30 MB，可以 override 這個自行定義大小。
   */
  maxFileSize: number = 30;

  /**
   * 預覽時呈現的列數上限。
   * 預設是 50，可以 override 這個自行定義大小。
   * 數字越大會影響到效能。
   */
  maxRow: number = 50;

  /**
   * 預覽時呈現的欄位數上限。
   * 預設是 50，可以 override 這個自行定義大小。
   * 數字越大會影響到效能。
   */
  maxColumn: number = 50;

  /**
   * 檔案是否超過大小上限。
   */
  fileSizeInvalid: boolean = false;

  /**
   * 檔案解析出來的 WorkBook 內容。
   */
  workbook: XLSX.WorkBook | undefined;

  /**
   * 是否顯示預覽 panel
   */
  showPreview: boolean = false;

  /**
   * 檔案預覽的物件。
   */
  jexcel: jspreadsheet.JspreadsheetInstance | undefined;

  /**
   * 檔案上傳的元件。
   * 會用 @ViewChild('fileUploadComponent') 去畫面取得上傳的元件。
   */
  @ViewChild('fileUploadComponent') fileUploadComponent!: FileUpload;

  /**
   * 檔案預覽的容器。
   * 會用 @ViewChild('spreadsheet') 去畫面取得上傳的元件。
   */
  @ViewChild('spreadsheet') spreadsheetContainer!: ElementRef;

  /**
   * 定義 Form Group
   * */
  protected formGroup!: FormGroup;

  /**
   * 用於 Submit 用
   */
  protected submitted: boolean = false;

  /**
   * 表單動作
   */
  protected formAction!: string;

  ngAfterViewInit(): void {
    // 使用 setTimeout 函式將 jexcel 初始化，確保 `ViewChild` 存在。
    setTimeout(() => {
      console.log(this.spreadsheetContainer); // 確保 `ViewChild` 存在。
    });
  }

  /**
   * Patch FormGroup 的值
   * @param data
   */
  patchFormGroupValue(data?: any) {}

  /**
   * 取得 FormControl。
   * @param formControlName formControlNameformControl 的名稱
   * @returns FormControl
   */
  formControl(formControlName: string): FormControl {
    return this.formGroup.get(formControlName) as FormControl;
  }

  /**
   * 判斷 formControl 欄位是否有錯誤。
   * @param formControlName formControl 的名稱
   * @returns boolean 欄位是否有錯誤
   */
  formControlInvalid(formControlName: string): boolean {
    const formControl = this.formGroup.get(formControlName);
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
  fileUploadHandler(
    event: any,
    fileNameFormControlName: string,
    sheetNameOptions: Option[]
  ): void {
    console.log('fileUploadHandler');

    // 清除檔案預覽
    this.destroyJExcel();

    // 先清除畫面上的訊息，避免錯誤重複或被前一個訊息誤導
    this.messageService.clear();

    this.selectedFile = event.files[0] as File;
    // 檢查檔案大小是否超過上限
    this.fileSizeInvalid = this.excelFileReaderService.fileSizeInvalid(
      this.selectedFile,
      this.maxFileSize
    );
    if (this.fileSizeInvalid) {
      // 如果檔案太大，把檔案名稱寫回欄位，為了消掉必填的驗證
      this.formControl(fileNameFormControlName).patchValue(
        this.selectedFile.name
      );
      sheetNameOptions = [];
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
   * 檔案解析後需要做的事。通常是
   * (1) 設定檔案名稱到畫面欄位
   * (2) 設定下拉選單的清單
   * (3) 設定頁籤下拉選單選定第一個頁籤
   */
  abstract afterFileParseSuccess(result: ExcelData): void;

  /**
   * 檔案解析後需要做的事。通常是
   * (1) 清除畫面欄位的檔案名稱
   * (2) 清除下拉選單的清單
   */
  abstract afterFileParseFail(): void;

  /**
   * 預覽功能
   * @param sheetNameFormControlName
   * @returns
   */
  preview(sheetNameFormControlName: string) {
    console.log('preview');

    // 清除檔案預覽
    this.destroyJExcel();

    if (!sheetNameFormControlName) {
      return;
    }

    this.showPreview = true;

    // 建立 jspreadsheet.JSpreadsheetOptions 物件
    const options: jspreadsheet.JSpreadsheetOptions = {};

    // 把 XLSX.WorkBook 轉成網頁表格 Jspreadsheet CE 的設定 Options
    const jspreadsheetOptions =
      JspreadsheetWrapper.convertWorkbookToJspreadsheetOptions(
        this.workbook!,
        this.formControl(sheetNameFormControlName).getRawValue(), // 要查詢哪一個頁籤的資料
        this.maxRow,
        this.maxColumn
      );

    // 使用 setTimeout 函式將 jexcel 的初始化延遲到下一個JavaScript事件迴圈。
    // 請注意，使用setTimeout的方式可能不是最佳解決方案，但在某些情況下可能能解決異步操作所導致的問題。
    setTimeout(() => {
      console.log(this.spreadsheetContainer);

      // 創建一個新的預覽表格
      this.jexcel = jspreadsheet(
        this.spreadsheetContainer.nativeElement,
        jspreadsheetOptions
      );

      if (this.jexcel) {
        // 把滑鼠右鍵的選單全移除
        this.jexcel.contextMenu.remove();
      }
    }, 100);
  }

  /**
   * 清除檔案預覽
   */
  destroyJExcel(): void {
    this.showPreview = false;
    this.jexcel?.destroy();
  }

  /**
   * formControl 的檔案大小 Validator
   * @returns
   */
  fileSizeValidator(): ValidatorFn {
    return (formControl: AbstractControl): ValidationErrors | null => {
      if (this.selectedFile) {
        const error = this.excelFileReaderService.fileSizeInvalid(
          this.selectedFile,
          this.maxFileSize
        );
        console.log('fileSizeValidator error = ' + error);
        if (error) {
          // 回傳 ValidationErrors，invalidFileSize 是 error 的 key
          // this.selectedFile = undefined;
          return { invalidFileSize: true };
        }
      }

      // 驗證結果符合需求，回傳 null
      return null;
    };
  }
}
