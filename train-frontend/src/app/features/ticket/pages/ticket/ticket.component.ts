import { AfterViewInit, Component, OnInit } from '@angular/core';
import { DialogService } from 'primeng/dynamicdialog';
import { CommonModule } from '@angular/common';
import { CoreModule } from '../../../../core/core.module';
import { SharedModule } from '../../../../shared/shared.module';
import { TrainTicketService } from '../../services/train-ticket.service';
import { StorageService } from '../../../../core/services/storage.service';
import { Router } from '@angular/router';
import { StepQueryKey } from '../../../../core/enums/step-query-key.enum copy';

@Component({
  selector: 'app-ticket',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [DialogService, TrainTicketService],
  templateUrl: './ticket.component.html',
  styleUrl: './ticket.component.scss',
})
export class TicketComponent implements OnInit, AfterViewInit {
  activeStepIndex: number = 0; // 控制階段

  constructor(private storageService: StorageService, private router: Router) {}

  ngOnInit() {
    this.activeStepIndex = Number(
      this.storageService.getSessionStorageItem('step')
    );
    console.log(this.activeStepIndex);
  }

  ngAfterViewInit(): void {
    this.activeStepIndex = Number(
      this.storageService.getSessionStorageItem('step')
    );
  }

  query() {}

  clear() {}

  /**
   * 切換 Step
   * @param event
   */
  onStepChange(event: any): void {
    console.log('Active step changed to:', event);
    console.log(this.activeStepIndex);
    if (event > this.activeStepIndex) {
      return;
    }
    this.storageService.setSessionStorageItem('step', event);
    this.activeStepIndex = event;

    // 當點擊 step 1
    if (this.activeStepIndex === 0) {
      this.router.navigate(['/ticket/train-info']);
      // 當點擊 step 2
    } else if (this.activeStepIndex === 1) {
      let queryParam = this.storageService.getSessionStorageItem(
        StepQueryKey.STEP2
      );

      // 如果 Session Storage 有 queryParam 資料，代表要從其他 Step 切回 Step2
      if (queryParam) {
        this.router.navigate(['/ticket/train-selecting/'], {
          queryParams: this.parseQueryString(queryParam),
        });
      }
      // 當點擊 step 3
    } else if (this.activeStepIndex === 2) {
      this.router.navigate(['/ticket/ticket-detail']);
    }
  }

  /**
   * 將 queryParam 字串轉換為 queryParam
   * @param queryString
   */
  parseQueryString(queryString: string): any {
    const params: any = {};
    const searchParams = new URLSearchParams(queryString);

    searchParams.forEach((value, key) => {
      params[key] = value; // 將 key 和 value 存入物件
    });

    console.log(params);
    return params;
  }
}
