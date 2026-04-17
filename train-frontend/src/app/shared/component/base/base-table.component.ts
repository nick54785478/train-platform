import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { SystemMessageService } from '../../../core/services/system-message.service';
import { DynamicDialogRef } from 'primeng/dynamicdialog';
import { LoadingMaskService } from '../../../core/services/loading-mask.service';

/**
 * 定義基礎的 Table 表格 Component
 */
@Component({
  selector: 'app-base-form-compoent',
  standalone: true,
  imports: [],
  providers: [SystemMessageService, LoadingMaskService],
  template: '',
})
export abstract class BaseTableCompoent {
  protected loadingMaskService = inject(LoadingMaskService);
  protected messageService = inject(SystemMessageService);

  constructor() {}

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

  /**
   * 紀錄該筆資料
   * @param rowData 點選的資料
   */
  clickRowActionMenu(rowData: any): void {
    this.selectedData = rowData;
  }
}
