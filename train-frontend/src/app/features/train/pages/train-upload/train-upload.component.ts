import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnInit } from '@angular/core';
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
import { TrainService } from '../../services/train.service';
import { finalize, map } from 'rxjs';
import { TemplateType } from '../../../../core/enums/template-type.enum';
import { SaveDownloadFileService } from '../../../../shared/services/save-download-file.service';

@Component({
  selector: 'app-train-upload',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [
    SystemMessageService,
    LoadingMaskService,
    SaveDownloadFileService,
  ],
  templateUrl: './train-upload.component.html',
  styleUrl: './train-upload.component.scss',
})
export class TrainUploadComponent
  extends BaseUploadCompoent
  implements OnInit, AfterViewInit
{
  sheetNameOptions: any[] = [];

  spreadsheet: any;

  constructor(
    private trainService: TrainService,
    private saveDownloadFileService: SaveDownloadFileService,
  ) {
    super();
  }

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      fileName: new FormControl('', [Validators.required]),
      sheetName: new FormControl('', [Validators.required]),
    });
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

    this.formGroup.patchValue({
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
    if (!this.submitted || !this.workbook) {
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

    const formData = new FormData();
    this.loadingMaskService.show();
    formData.append('file', file);
    formData.append('mapping', 'TRAIN_MAPPING');
    formData.append('sheetMapping', 'TRAIN_SHEET_NAME');
    this.trainService
      .upload(formData)
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
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
          this.messageService.error(error.message);
        },
      });
  }

  // // 選擇檔案
  // onFileSelected(event: any) {
  //   const file = event.target.files[0];
  //   if (!file) return;

  //   const reader = new FileReader();
  //   reader.onload = (e: any) => {
  //     const data = new Uint8Array(e.target.result);
  //     const workbook = XLSX.read(data, { type: 'array' });

  //     // 取得第一張工作表
  //     const sheetName = workbook.SheetNames[0];
  //     const worksheet = workbook.Sheets[sheetName];

  //     // 轉換為 JSON
  //     const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
  //     console.log('解析結果:', jsonData);
  //   };

  //   reader.readAsArrayBuffer(file);
  // }

  /**
   * 預覽功能
   * @param sheetNameFormControlName
   * @returns
   */
  override preview(sheetNameFormControlName: string) {
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
        this.maxColumn,
      );

    // 請注意，使用setTimeout的方式可能不是最佳解決方案，但在某些情況下可能能解決異步操作所導致的問題。
    setTimeout(() => {
      console.log(this.spreadsheetContainer);

      // 創建一個新的預覽表格
      this.jexcel = jspreadsheet(
        this.spreadsheetContainer.nativeElement,
        jspreadsheetOptions,
      );

      if (this.jexcel) {
        // 把滑鼠右鍵的選單全移除
        this.jexcel.contextMenu.remove();
      }
    }, 100);
  }

  /**
   * 下載火車上傳範本
   */
  downloadTemplate(event: Event) {
    event.preventDefault();
    console.log('下載範本');

    this.loadingMaskService.show();
    this.trainService
      .downloadTemplate('TRAIN_UPLOAD')
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
        }),
      )
      .subscribe({
        next: (res) => {
          this.messageService.success('下載成功');
          console.log(res.filename);
          this.saveDownloadFileService.saveBufferAsXlsx(
            res.body,
            decodeURI(res.filename),
            // res.filename
          );
        },
        error: (err) => {
          this.messageService.error(err);
        },
      });
  }
}
