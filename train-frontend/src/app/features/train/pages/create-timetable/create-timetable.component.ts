import { Component, DoCheck, OnInit } from '@angular/core';
import { BaseInlineEditeTableCompoent } from '../../../../shared/component/base/base-inline-edit-table.component';
import { CoreModule } from '../../../../core/core.module';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../shared/shared.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Option } from '../../../../shared/models/option.model';
import { OptionService } from '../../../../shared/services/option.service';
import { SystemMessageService } from '../../../../core/services/system-message.service';
import { MenuItem } from 'primeng/api';
import { TrainService } from '../../services/train.service';
import {
  CreateStopResource,
  CreateTrainResource,
} from '../../models/create-train-resource.model';
import { finalize } from 'rxjs';
import { LoadingMaskService } from '../../../../core/services/loading-mask.service';
import { DataType } from '../../../../core/enums/data-type.enum';

@Component({
  selector: 'app-create-timetable',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [],
  templateUrl: './create-timetable.component.html',
  styleUrl: './create-timetable.component.scss',
})
export class CreateTimetableComponent
  extends BaseInlineEditeTableCompoent
  implements OnInit, DoCheck
{
  kinds: Option[] = []; // 火車種類下拉選單資料
  stopKinds: Option[] = []; // 車站種類下拉選單資料
  lineDetailTabs: MenuItem[] = []; // Stop 的 按鈕
  lock: boolean = false;

  constructor(
    private optionService: OptionService,
    private trainService: TrainService,
  ) {
    super();
  }
  ngDoCheck(): void {
    this.detailTabs = [
      {
        label: '新增',
        icon: 'pi pi-plus',
        command: () => {
          this.addNewRow();
        },
        disabled: !(this.mode === ''),
      },
      {
        label: '放棄',
        icon: 'pi pi-times',
        command: () => {
          this.cancelAll();
        },
        disabled: false,
      },
    ];
    this.detailTabs = [
      {
        label: '提交',
        icon: 'pi pi-save',
        command: () => {
          this.onSubmit();
        },
        disabled: this.tableData.length === 0 || this.mode !== '',
      },
      {
        label: '清除',
        icon: 'pi pi-times',
        command: () => {
          this.clear();
        },
        disabled: false,
      },
    ];
  }

  ngOnInit(): void {
    this.lineDetailTabs = [
      {
        label: '新增',
        icon: 'pi pi-plus',
        command: () => {
          this.addNewRow();
        },
        disabled: !(this.mode === ''),
      },
      {
        label: '放棄',
        icon: 'pi pi-times',
        command: () => {
          this.cancelAll();
        },
        disabled: false,
      },
    ];

    this.detailTabs = [
      {
        label: '提交',
        icon: 'pi pi-save',
        command: () => {
          this.onSubmit();
        },
        disabled: this.tableData.length === 0 || this.mode !== '',
      },
      {
        label: '清除',
        icon: 'pi pi-times',
        command: () => {
          this.clear();
        },
        disabled: false,
      },
    ];

    this.formGroup = new FormGroup({
      trainKind: new FormControl('', [Validators.required]),
      trainNo: new FormControl('', [
        Validators.required,
        Validators.pattern(/^[1-9]\d*$/), // 正則表達式檢查正整數
      ]),
    });

    //	 取得火車種類的下拉式選單資料
    this.optionService.getTrainKinds().subscribe({
      next: (res) => {
        this.kinds = res;
      },
      error: (error) => {
        this.messageService.error(
          '取得火車種類的下拉式選單資料時，發生錯誤',
          error.message,
        );
      },
    });

    this.optionService.getSettingTypes(DataType.STOP_KIND).subscribe({
      next: (res) => {
        console.log(res);
        this.stopKinds = res;
      },
      error: (error) => {
        this.messageService.error(
          '取得車站配置的下拉式選單資料時，發生錯誤',
          error.message,
        );
      },
    });

    // 初始化 Table 配置
    this.cols = [
      {
        field: 'seq',
        header: '停站順序',
        type: 'inputNumber',
        data: '', // 取已選中的 dropdown
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

  /**
   * 提交資料
   */
  onSubmit() {
    const requestData: CreateTrainResource = { ...this.formGroup.value };

    // 停靠站清單
    const stops: CreateStopResource[] = this.tableData.map((res) => ({
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
      .createTrain(requestData)
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
   * 清除表單資料
   */
  override clear() {
    // this.formGroup.reset();
    this.formGroup.setValue({
      trainNo: '', // 角色名稱
      trainKind: '', // 種類
    });

    this.tableData = [];
    this.selectedIndex = -1;
    this.selectedData = null;
    this.editingIndex = -1;
    this.editingRow = null;
    this.mode = '';
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
   * 確認編輯/新增
   * @param rowIndex 當前 row 的 Index
   * */
  confirm(rowIndex: number) {
    // 當新增模式會將資料更新為最新的空資料，因為前面進新增模式時未 select
    if (this.mode === 'add') {
      // 更新為該筆資料
      this.newRow = this.tableData[rowIndex];
      console.log(this.checkRowData(this.newRow));

      // 新增模式下有欄位為空值，不予以 Confirm
      if (!this.checkRowData(this.tableData[rowIndex])) {
        return;
      }

      // 過濾掉該 rowIndex
      this.newRowIndexes = this.newRowIndexes.filter(
        (index) => index !== rowIndex,
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

  // 檢查 row 資料是否有未填欄位
  override checkRowData(selectedData: any): boolean {
    if (!selectedData.seq || !selectedData.stopName || !selectedData.stopTime) {
      return false;
    }
    return true;
  }

  /**
   * Table Action 按鈕按下去的時候要把該筆資料記錄下來。
   * @param rowData 點選的資料
   */
  clickRowActionMenu(rowData: any): void {
    this.selectedData = rowData;
  }

  onEdit(rowIndex: number) {
    // 若目前為 新增模式 pass
    if (this.mode === 'add' || this.mode === 'delete') {
      return;
    }

    // 避免當我進入編輯模式後，再點擊其他列導致進入其他列的編輯模式
    if (this.mode === 'edit' && rowIndex !== this.editingIndex) {
      return;
    }

    // 進入編輯模式
    this.mode = 'edit';

    if (typeof rowIndex === 'number') {
      // 選取的 rowIndex
      this.selectedIndex = rowIndex;
      // 被編輯的 row 資料
      this.editingIndex = rowIndex;
    }
    this.selectedData = this.tableData[rowIndex];
    this.editingRow = { ...this.selectedData }; // 深拷貝選中的行資料，避免直接修改原始數據
  }

  /**
   * 取消編輯
   */
  cancelEdit() {
    if (!this.editingRow) {
      return;
    }
    // 透過 editingRow 回覆上次修改的資料
    this.tableData.forEach((e) => {
      if (
        e.id === this.editingRow.id &&
        e.givenIndex === this.editingRow.givenIndex
      ) {
        e.type = this.editingRow.type;
        e.name = this.editingRow.name;
        e.code = this.editingRow.code;
        e.activeFlag = this.editingRow.activeFlag;
        e.description = this.editingRow.description;
      }
    });
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
   * 回歸原狀，原先新增的資料全部放棄。
   */
  cancelAll() {
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
   * 判斷是否為編輯模式
   * */
  isEditing(rowIndex: any): boolean {
    return this.editingIndex === rowIndex;
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
   * 進行刪除
   * @param id
   * @param isSelected 是否被選中
   */
  onDelete(id: number, isSelected: boolean) {
    // 如果不包含該 id 加入
    if (isSelected) {
      // 若選中，添加到陣列
      this.deleteList.push(id);
    } else {
      // 若取消選中，從陣列移除
      this.deleteList = this.deleteList.filter((e) => e !== id);
    }
    console.log(this.deleteList);
  }

  // 載入 dropdown 資料
  override loadDropdownData(col: any): any[] {
    // 如果已經載入過資料，則不再重新請求
    switch (col.field) {
      case 'stopName':
        return this.stopKinds;
      default:
        return [];
    }
  }
}
