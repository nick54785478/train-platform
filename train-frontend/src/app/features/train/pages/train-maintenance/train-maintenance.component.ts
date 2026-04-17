import { Component, DoCheck, OnInit } from '@angular/core';
import { BaseInlineEditeTableCompoent } from '../../../../shared/component/base/base-inline-edit-table.component';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../shared/shared.module';
import { CoreModule } from '../../../../core/core.module';
import { LoadingMaskService } from '../../../../core/services/loading-mask.service';
import { DialogService } from 'primeng/dynamicdialog';
import { OptionService } from '../../../../shared/services/option.service';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { MenuItem } from 'primeng/api';
import { Subject } from 'rxjs/internal/Subject';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Option } from '../../../../shared/models/option.model';
import { TrainService } from '../../services/train.service';
import { finalize } from 'rxjs/internal/operators/finalize';
import { DataType } from '../../../../core/enums/data-type.enum';
import {
  UpdateStopResource,
  UpdateTrainResource,
} from '../../models/update-train-resource.model';
import { DialogConfirmService } from '../../../../core/services/dialog-confirm.service';
import { error } from 'console';

@Component({
  selector: 'app-train-maintenance',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [
    OptionService,
    DialogService,
    DialogConfirmService,
    SystemMessageService,
    LoadingMaskService,
  ],
  templateUrl: './train-maintenance.component.html',
  styleUrl: './train-maintenance.component.scss',
})
export class TrainMaintenanceComponent
  extends BaseInlineEditeTableCompoent
  implements OnInit, DoCheck
{
  trainNos: Option[] = [];
  stops: Option[] = [];
  dialogOpened: boolean = false; //  Dialog 狀態
  rowActionMenu: MenuItem[] = []; // Table Row Actions 右側選單。
  readonly _destroying$ = new Subject<void>(); // 用來取消訂閱

  constructor(
    private dialogConfirmService: DialogConfirmService,
    private optionService: OptionService,
    private trainService: TrainService,
  ) {
    super();
  }

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      trainNo: new FormControl('', [Validators.required]),
    });

    this.optionService.getSettingTypes(DataType.STOP_KIND).subscribe({
      next: (res) => {
        console.log(res);
        this.stops = res;
      },
      error: (error) => {
        this.messageService.error(
          '取得車次的下拉式選單資料時，發生錯誤',
          error.message,
        );
      },
    });

    this.optionService.getTrainNoList().subscribe({
      next: (res) => {
        console.log(res);
        this.trainNos = res;
      },
      error: (error) => {
        this.messageService.error(
          '取得車次的下拉式選單資料時，發生錯誤',
          error.message,
        );
      },
    });

    this.detailTabs = [
      {
        label: '新增',
        icon: 'pi pi-plus',
        // 當沒有表單資料，不能新增
        disabled:
          !(this.mode === '') ||
          (!this.formGroup.value.trainNo && this.tableData.length === 0),
        command: () => {
          this.addNewRow();
        },
      },
      {
        label: '提交',
        icon: 'pi pi-save',
        command: () => {
          this.submit();
        },
        // 當在新增或編輯模式時，不能提交
        disabled:
          this.tableData.length === 0 ||
          this.mode === 'add' ||
          this.mode === 'edit',
      },
      {
        label: '放棄',
        icon: 'pi pi-times',
        command: () => {
          this.cancelAll();
        },
        disabled: this.tableData.length === 0,
      },
    ];
    // 初始化 Table 配置
    this.cols = [
      {
        field: 'seq',
        header: '停站順序',
        type: 'inputNumber',
        data: '',
      },
      {
        field: 'stopName',
        header: '停靠站名',
        type: 'dropdown',
        data: 'stopName', // 取已選中的 dropdown
      },
      {
        field: 'stopTime',
        header: '停站時間',
        type: 'inputTime',
        data: '', // 取已選中的 dropdown
      },
    ];
  }

  ngDoCheck(): void {
    this.detailTabs = [
      {
        label: '新增',
        icon: 'pi pi-plus',
        // 當沒有表單資料，不能新增
        disabled:
          !(this.mode === '') ||
          (!this.formGroup.value.trainNo && this.tableData.length === 0),
        command: () => {
          this.addNewRow();
        },
      },
      {
        label: '提交',
        icon: 'pi pi-save',
        command: () => {
          this.submit();
        },
        // 當在新增或編輯模式時，不能提交
        disabled:
          this.tableData.length === 0 ||
          this.mode === 'add' ||
          this.mode === 'edit',
      },
      {
        label: '放棄',
        icon: 'pi pi-times',
        command: () => {
          this.cancelAll();
        },
        disabled: this.tableData.length === 0,
      },
    ];
  }

  /**
   * 取消編輯/新增
   * */
  cancel(rowIndex?: number) {
    if (this.mode === 'edit') {
      this.cancelEdit();
    } else if (
      this.mode === 'add' &&
      rowIndex !== -1 &&
      rowIndex !== undefined
    ) {
      this.cancelAdd(rowIndex);
    }

    this.editingIndex = -1;
    this.editingRow = null;
  }

  /**
   * 取消編輯
   */
  cancelEdit() {
    if (!this.editingRow) {
      return;
    }

    console.log(this.editingRow.givenIndex);
    // 透過 editingRow 回覆上次修改的資料
    this.tableData.forEach((e) => {
      if (
        e.id === this.editingRow.id &&
        e.givenIndex === this.editingRow.givenIndex
      ) {
        e.seq = this.editingRow.seq;
        e.stopName = this.editingRow.stopName;
        e.stopTime = this.editingRow.stopTime;
      }
    });

    // 取消，解除模式
    this.mode = '';
  }

  /**
   * 移除 id = null 的值
   * 用於移除新列 (row)
   *
   * @param rowIndex 當前 row 資料的 index
   */
  cancelAdd(rowIndex: number) {
    if (this.mode === 'add') {
      // 過濾出 id != null 者 (現有資料) 及 沒被選上的資料
      this.tableData = this.tableData.filter(
        (data) => data.id !== null || data?.givenIndex !== rowIndex,
      );
      // 過濾掉該 rowIndex
      this.newRowIndexes = this.newRowIndexes.filter(
        (index) => index !== rowIndex,
      );
    }
    // reset 新增資料
    this.newRow = null;

    // newRowIndexes 裡面還有資料，代表不能解除新增模式
    if (this.newRowIndexes.length > 0) {
      return;
    }

    this.mode = '';
  }

  /**
   * 確認編輯/新增
   * @param givenIndex 當前 row 的 givenIndex
   * */
  confirm(givenIndex: number) {
    // 當新增模式會將資料更新為最新的空資料，因為前面進新增模式時未 select
    if (this.mode === 'add') {
      // 更新為該筆資料
      this.newRow = this.tableData.find(
        (data) => data.givenIndex === givenIndex,
      );

      // 新增模式下有欄位為空值，不予以 Confirm
      if (!this.checkRowData(this.newRow)) {
        return;
      }

      // 過濾掉該 rowIndex
      this.newRowIndexes = this.newRowIndexes.filter(
        (index) => index !== givenIndex,
      );
    }

    // 編輯模式，檢查資料
    if (this.mode === 'edit' && !this.checkRowData(this.selectedData)) {
      return;
    }
    this.newRow = null;
    this.editingIndex = -1;
    this.editingRow = null;

    // newRowIndexes 裡面還有資料，代表不能解除更新資料
    if (this.newRowIndexes.length > 0) {
      return;
    }

    // 解除特定模式
    this.mode = '';
  }

  /**
   * Table Action 按鈕按下去的時候要把該筆資料記錄下來。
   * @param rowData 點選的資料
   */
  clickRowActionMenu(rowData: any): void {
    this.selectedData = rowData;

    // 開啟 Dialog
  }

  /**
   * 切換 編輯模式
   * @param givenIndex
   * @returns
   */
  onEdit(givenIndex: number) {
    // 若目前為 新增模式或刪除模式 pass
    if (this.mode === 'add' || this.mode === 'delete') {
      return;
    }

    // 避免當我進入編輯模式後，再點擊其他列導致進入其他列的編輯模式
    if (this.mode === 'edit' && givenIndex !== this.editingIndex) {
      return;
    }

    // 進入編輯模式
    this.mode = 'edit';

    if (typeof givenIndex === 'number') {
      // 選取的 rowIndex
      this.selectedIndex = givenIndex;
      // 被編輯的 row 資料
      this.editingIndex = givenIndex;
    }
    // 根據 givenIndex 找出該筆資料
    this.selectedData = this.tableData.find(
      (data) => data.givenIndex === givenIndex,
    );
    this.editingRow = { ...this.selectedData }; // 深拷貝選中的行資料，避免直接修改原始數據
  }

  /**
   * 進行刪除動作
   * @param givenIndex
   */
  onDelete(givenIndex: number) {
    // 若目前為 新增模式或編輯模式 pass
    if (this.mode === 'add' || this.mode === 'edit') {
      return;
    }
    // 進入編輯模式
    this.mode = 'delete';
    this.dialogConfirmService.confirmDelete(
      () => {
        // 確認後的動作 => 過濾該 givenIndex 的資料
        this.tableData = this.tableData.filter(
          (data) => data.givenIndex !== givenIndex,
        );
        this.mode = '';
      },
      '',
      () => {
        // 取消 delete 模式
        this.mode = '';
      },
    );
  }

  /**
   * 判斷 Type 欄位是否可修改
   * @param rowData 該 row 的資料
   * @param field 欄位名稱
   * @returns
   */
  isFieldDisabled(rowData: any, field: string): boolean {
    if (
      (field === 'type' &&
        rowData.id !== null &&
        this.formGroup.get('type')?.value !== '') ||
      (rowData.id !== null && this.formGroup.get('name')?.value !== '')
    ) {
      return true;
    }
    return false;
  }

  // 刪除幾列資料
  override delete(ids: number[], event?: Event) {}

  /**
   * 透過特定條件查詢設定資料，
   * 註.需重新排序 givenIndex
   */
  query() {
    this.submitted = true;
    if (!this.submitted || this.formGroup.invalid) {
      return;
    }

    this.loadingMaskService.show();
    const formData = this.formGroup.value;
    this.trainService
      .queryTrain(formData.trainNo)
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
        }),
      )
      .subscribe({
        next: (res) => {
          this.messageService.success('查詢成功');
          this.tableData = res.stops;
          // 對所有資料進行編號
          for (var i = 0; i < this.tableData.length; i++) {
            this.tableData[i].givenIndex = i;
          }
        },
        error: (error) => {
          this.messageService.error(error);
        },
      });
  }

  /**
   * 判斷是否為新增模式
   * @param rowData 當前的 row 資料
   * */
  isAdding(rowData: any) {
    // 這裡要使用 givenIndex ，因 Table 的 index 會隨資料數量改變
    return !rowData.id && this.newRowIndexes.includes(rowData.givenIndex);
    // rowIndex !== rowData.givenIndex;
  }

  /**
   * 判斷是否為編輯模式
   * */
  isEditing(givenIndex: any): boolean {
    return this.editingIndex === givenIndex;
  }

  /**
   * 新增一筆空的 row 資料
   * */
  addNewRow(): void {
    // 如果是編輯或刪除模式，就不新增資料
    if (this.mode === 'edit' || this.mode === 'delete') {
      return;
    }

    // 設定模式為 新增模式
    this.mode = 'add';
    this.newRow = {
      id: null,
      seq: '',
      stopName: '',
      stopTime: '',
      givenIndex: 0, // 前端給予的編號資料
      // givenIndex: this.tableData.length, // 前端給予的編號資料
    };

    // 所有編號往後推一號
    this.tableData.forEach((e) => {
      e.givenIndex += 1;
    });

    // 將 index 加入 newRowIndexes，用以紀錄更新資料的 index
    this.newRowIndexes.push(this.newRow.givenIndex);
    // 將此資料推入 tableData
    this.tableData.push(this.newRow);
    // 根據 givenIndex 重排序
    this.tableData.sort((a, b) => {
      if (a.givenIndex < b.givenIndex) {
        return -1; // a 排在 b 前
      } else if (a.givenIndex > b.givenIndex) {
        return 1; // b 排在 a 前
      } else {
        return 0; // 保持順序
      }
    });
    console.log(this.tableData);
  }

  /**
   * 在編輯模式載入下拉式選單資料
   * @param col
   * @returns
   */
  override loadDropdownData(col: any) {
    if (col.field === 'stopName') {
      return this.stops;
    }
    return [];
  }

  /**
   * 回歸原狀，原先新增的資料全部放棄。
   */
  cancelAll() {
    // 若在編輯模式中取消，呼叫 cancelEdit 方法
    if (this.mode === 'edit') {
      this.cancelEdit();
    }
    this.deleteList = [];
    this.mode = '';
    this.newRow = '';
    this.newRowIndexes = [];
    this.selectedData = null;
    this.selectedIndex = -1;
    this.editingIndex = -1;
    this.editingRow = [];
    this.tableData = this.tableData.filter((data) => data.id !== null);
  }

  /**
   * 進行刪除
   */
  onStartDelete() {
    this.mode = 'delete';
  }

  // 檢查 row 資料是否有未填欄位
  override checkRowData(selectedData: any): boolean {
    if (!selectedData.seq || !selectedData.stopName || !selectedData.stopTime) {
      return false;
    }
    return true;
  }

  /**
   * 清除表單資料
   */
  override clear() {
    // this.formGroup.reset();
    this.formGroup.setValue({
      trainNo: '', // 車次
    });

    this.tableData = [];
    this.selectedIndex = -1;
    this.selectedData = null;
    this.editingIndex = -1;
    this.editingRow = null;
    this.mode = '';
  }

  /**
   * 提交資料
   */
  override submit() {
    const requestData: UpdateTrainResource = { ...this.formGroup.value };

    // 停靠站清單
    const stops: UpdateStopResource[] = this.tableData.map((res) => ({
      uuid: res.uuid,
      seq: res.seq,
      stopName: res.stopName,
      stopTime: res.stopTime,
    }));

    requestData.stops = stops;
    console.log(requestData);

    this.submitted = true;
    if (
      !this.submitted ||
      this.formGroup.invalid ||
      this.tableData.length === 0 ||
      this.mode !== ''
    ) {
      return;
    }

    this.loadingMaskService.show();
    this.trainService
      .updateTrain(requestData)
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
        error: (err) => {
          this.messageService.error('發生錯誤，新增失敗', err);
        },
      });
  }
}
