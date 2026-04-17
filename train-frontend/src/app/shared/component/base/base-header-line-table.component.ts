import { Component, inject, Injectable, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MenuItem, MessageService } from 'primeng/api';
import { Option } from '../../models/option.model';
import { SystemMessageService } from '../../../core/services/system-message.service';
import { LoadingMaskService } from '../../../core/services/loading-mask.service';
import { CommonModule } from '@angular/common';
import { CoreModule } from '../../../core/core.module';
import { SharedModule } from '../../shared.module';

/**
 * 定義基礎的 Header & Line 雙表格 Component
 */
@Component({
  selector: 'app-base-header-line-table-compoent',
  standalone: true,
  imports: [],
  providers: [],
  template: '',
})
export abstract class BaseHeaderLineTableCompoent {
  protected loadingMaskService = inject(LoadingMaskService);
  protected messageService = inject(SystemMessageService);
  /**
   * 上方頁簽
   * */
  protected detailTabs: MenuItem[] = [];

  /**
   * Header 表格資料
   * */
  protected headerTableData: any[] = [];

  /**
   * Line 表格資料
   * */
  protected lineTableData: any[] = [];

  /**
   * 是否顯示 Line Table (選用)
   */
  protected lineTableVisibled!: boolean;

  /**
   * Header 表格定義(如: 標題、值)
   * */
  protected headerCols: any[] = [];

  /**
   * Line 表格定義(如: 標題、值)
   * */
  protected lineCols: any[] = [];

  /**
   * 選中的那列資料 (亦可使用 tableData[selectedIndex] 來取得)
   * 此處設置的目的為將來維運方便
   * */
  protected rowCurrentData: any;

  /**
   * 選中的 Header Row 資料
   * */
  protected selectedHeaderData: any;

  /**
   * 選中的 Line Row 資料
   * */
  protected selectedLineData: any;

  /**
   * 選中的 rowIndex
   */
  protected selectedIndex: number = -1;

  /**
   * 正在編輯的 那列資料 (亦可使用 tableData[editingIndex] 來取得)
   * 此處設置的目的為將來維運方便
   */
  protected editingRow: any;

  /**
   * 正在編輯的  rowIndex
   */
  protected editingIndex: number = -1;

  /**
   * 定義 Form Group
   * */
  protected formGroup!: FormGroup;

  /**
   * 是否開啟 Dialog
   */
  protected dialogOpened: boolean = false;

  /**
   * 用於 Submit 狀態
   * */
  protected submitted: boolean = false;

  /**
   * 刪除 ID 清單
   */
  protected deleteList: number[] = [];

  /**
   * 根據 ID 清單刪除資料
   *@param ids
   */
  protected delete(ids: number[]) {}

  /**
   * 提交資料
   * @param tableData
   */
  protected submit(tableData: any[]) {}

  /**
   * 清除表單
   */
  protected clear() {}

  /**
   * 表單動作
   * */
  protected formAction!: string;

  /**
   * 表格內資料設定
   * */
  protected detailTabColumns: MenuItem[] = [];

  /**
   * 新增的空白列資料
   * */
  protected newRow!: any;

  /**
   * 新增的資料的 row index 清單
   */
  protected newRowIndexes: number[] = [];

  /**
   * 模式: add (新增)、edit (編輯)、delete (刪除)
   */
  protected mode: string = ''; // 模式

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
   * 將 json 字串轉為下拉式選單
   * @param rawString
   * @returns
   */
  protected passJsonToOption(rawString: string): Option[] {
    // 將單引號轉換為雙引號，讓 JSON 格式正確
    const jsonString = rawString.replace(/'/g, '"');

    // 解析為 JavaScript 物件
    const parsedArray = JSON.parse(jsonString);

    return parsedArray.map((item: any) => ({
      label: item.label,
      value: item.value,
    }));
  }

  /**
   * 檢查 Column 資料欄位(必填)是否為空
   * @param selectedData
   * @returns true/false
   */
  checkRowData(selectedData?: any): boolean {
    return true;
  }

  /**
   * 取得Inline Edit Table 的下拉選單資料
   * @param data 下拉選單參數名(通常是現有的)
   */
  loadDropdownData(col: any): any[] {
    return [];
  }
}
