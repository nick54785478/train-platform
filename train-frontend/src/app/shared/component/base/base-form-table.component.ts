import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { SystemMessageService } from '../../../core/services/system-message.service';
import { LoadingMaskService } from '../../../core/services/loading-mask.service';

/**
 * 定義基礎的 Form 表單及 Table 表格 Component
 */
@Component({
  selector: 'app-base-table-form-compoent',
  standalone: true,
  imports: [],
  providers: [],
  template: '',
})
export abstract class BaseFormTableCompoent {
  protected loadingMaskService = inject(LoadingMaskService);
  protected messageService = inject(SystemMessageService);
  /**
   * 定義 Form Group
   * */
  protected formGroup!: FormGroup;

  protected submitted: boolean = false; // 用於 Submit 用
  /**
   * 表單動作
   * */
  protected formAction!: string;

  /**
   * 動態定義表格欄位參數
   */
  cols: any[] = [];

  /**
   * 表格資料
   */
  tableData: any;

  /**
   * 選擇的 row data
   */
  selectedData: any;

  /**
   * 是否開啟 Dialog
   */
  protected dialogOpened: boolean = false;

  constructor() {}

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
   * 紀錄該筆資料
   * @param rowData 點選的資料
   */
  clickRowActionMenu(rowData: any): void {
    this.selectedData = rowData;
  }
}
