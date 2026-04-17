import { Component, OnInit } from '@angular/core';
import { Option } from '../../../../../shared/models/option.model';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../../shared/shared.module';
import { CoreModule } from '../../../../../core/core.module';
import { BaseTableCompoent } from '../../../../../shared/component/base/base-table.component';
import { TrainService } from '../../../services/train.service';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { finalize } from 'rxjs/internal/operators/finalize';

@Component({
  selector: 'app-train-stops',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [],
  templateUrl: './train-stops.component.html',
  styleUrl: './train-stops.component.scss',
})
export class TrainStopsComponent extends BaseTableCompoent implements OnInit {
  trainNoList: Option[] = []; // Active Flag 的下拉式選單
  stops: Option[] = []; // 車站資料的下拉式選單
  kinds: Option[] = []; // 車種資料的下拉式選單

  constructor(
    public ref: DynamicDialogRef,
    private trainService: TrainService,
    private dialogConfig: DynamicDialogConfig,
  ) {
    super();
  }

  ngOnInit(): void {
    const uuid = this.dialogConfig.data.uuid;
    const fromStop = this.dialogConfig.data.fromStop;
    console.log('uuid:', uuid, ', fromStop:', fromStop);
    this.cols = [
      {
        field: 'seq',
        header: '停靠順序',
        type: '',
      },
      {
        field: 'fromStop',
        header: '起站',
        type: '',
      },
      {
        field: 'arriveStartStopTime',
        header: '起站發車時間',
        type: '',
      },
      {
        field: 'toStop',
        header: '迄站',
        type: '',
      },
      {
        field: 'arriveEndStopTime',
        header: '迄站抵達時間',
        type: '',
      },
      {
        field: 'price',
        header: '票價',
        type: '',
      },
    ];
    this.query(uuid, fromStop);
  }

  /**
   * 查詢車站詳細資料
   * @param uuid
   * @param fromStop
   */
  query(uuid: string, fromStop: string) {
    this.trainService
      .queryStopDetails(uuid, fromStop)
      .pipe(finalize(() => {}))
      .subscribe((res) => {
        console.log(res);
        this.tableData = res;
      });
  }
}
