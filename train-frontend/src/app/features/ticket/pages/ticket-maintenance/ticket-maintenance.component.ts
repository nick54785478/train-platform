import { Component, DoCheck, OnInit } from '@angular/core';
import { BaseInlineEditeTableCompoent } from '../../../../shared/component/base/base-inline-edit-table.component';
import { OptionService } from '../../../../shared/services/option.service';
import { TrainTicketService } from '../../services/train-ticket.service';
import { LoadingMaskService } from '../../../../core/services/loading-mask.service';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { CommonModule } from '@angular/common';
import { CoreModule } from '../../../../core/core.module';
import { SharedModule } from '../../../../shared/shared.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { DataType } from '../../../../core/enums/data-type.enum';
import { Option } from '../../../../shared/models/option.model';
import { finalize } from 'rxjs/internal/operators/finalize';
import { DialogConfirmService } from '../../../../core/services/dialog-confirm.service';
import { CreateOrUpdateTicketResource } from '../../models/create-or-update-ticket-resource.model';

@Component({
  selector: 'app-ticket-maintenance',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [DialogConfirmService, SystemMessageService, LoadingMaskService],
  templateUrl: './ticket-maintenance.component.html',
  styleUrl: './ticket-maintenance.component.scss',
})
export class TicketMaintenanceComponent
  extends BaseInlineEditeTableCompoent
  implements OnInit, DoCheck
{
  trainNos: Option[] = [];
  stops: Option[] = [];
  constructor(
    private optionService: OptionService,
    private trainTicketService: TrainTicketService,
    private dialogConfirmService: DialogConfirmService,
  ) {
    super();
  }
  ngOnInit(): void {
    // 初始化表單
    this.formGroup = new FormGroup({
      trainNo: new FormControl('', [Validators.required]), // 車次
    });

    this.optionService.getSettingTypes(DataType.STOP_KIND).subscribe({
      next: (res) => {
        this.stops = res;
      },
      error: (err) => {
        this.messageService.error(err);
      },
    });

    this.optionService.getTrainNoList().subscribe({
      next: (res) => {
        this.trainNos = res;
      },
      error: (err) => {
        this.messageService.error(err);
      },
    });

    // 初始化 Table 配置
    this.cols = [
      {
        field: 'fromStop',
        header: '起站',
        type: 'dropdown',
        data: 'fromStop',
      },
      {
        field: 'toStop',
        header: '迄站',
        type: 'dropdown',
        data: 'toStop',
      },
      {
        field: 'price',
        header: '票價',
        type: 'inputNumber',
        data: '',
      },
    ];

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
   * 提交資料 - 查詢該車次的車票資訊
   * @returns
   */
  override submit() {
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
    const formData = this.formGroup.value;

    // 停靠站清單
    const resources: CreateOrUpdateTicketResource[] = this.tableData.map(
      (res) => ({
        ticketNo: res.ticketNo,
        fromStop: res.fromStop,
        toStop: res.toStop,
        price: res.price,
      }),
    );

    this.trainTicketService
      .createTicket(formData.trainNo, resources)
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

  /**
   * 查詢該車次的車票資訊
   */
  query() {
    this.submitted = true;
    if (!this.submitted || this.formGroup.invalid) {
      return;
    }
    this.loadingMaskService.show();
    const formData = this.formGroup.value;
    this.trainTicketService
      .queryTicketsByTrainNo(formData.trainNo)
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
        }),
      )
      .subscribe({
        next: (res) => {
          this.messageService.success('查詢成功');
          this.tableData = res;
          // 對所有資料進行編號
          for (var i = 0; i < this.tableData.length; i++) {
            this.tableData[i].givenIndex = i;
          }
        },
        error: (err) => {
          this.messageService.error('發生錯誤，新增失敗', err);
        },
      });
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
      fromStop: '',
      toStop: '',
      price: '',
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
    if (col.field === 'fromStop' || col.field === 'toStop') {
      return this.stops;
    }
    return [];
  }

  /**
   * 回歸原狀，原先新增的資料全部放棄。
   */
  /**
   * 回歸原狀，原先新增的資料全部放棄。
   */
  cancelAll() {
    // 若在編輯模式中取消，呼叫 cancelEdit 方法
    if (this.mode === 'edit') {
      this.cancelEdit();
    }
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
    if (!selectedData.fromStop || !selectedData.toStop || !selectedData.price) {
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
        e.fromStop = this.editingRow.fromStop;
        e.toStop = this.editingRow.toStop;
        e.price = this.editingRow.price;
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
  cancelAdd(givenIndex: number) {
    if (this.mode === 'add') {
      // 過濾出 id != null 者 (現有資料) 及 沒被選上的資料
      this.tableData = this.tableData.filter(
        (data) => data.id !== null || data?.givenIndex !== givenIndex,
      );
      // 過濾掉該 rowIndex
      this.newRowIndexes = this.newRowIndexes.filter(
        (index) => index !== givenIndex,
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
   * 判斷Type欄位是否可修改
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
}
