import { Component, OnInit } from '@angular/core';
import { BaseFormCompoent } from '../../../../../shared/component/base/base-form.component';
import { FormControl, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../../shared/shared.module';
import { CoreModule } from '../../../../../core/core.module';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Location } from '@angular/common';
import { AccountService } from '../../../services/account.service';
import { DepositMoneyResource } from '../../../models/deposit-money-resource.model';
import { LoadingMaskService } from '../../../../../core/services/loading-mask.service';
import { finalize } from 'rxjs';
import { SystemMessageService } from '../../../../../core/services/system-message.service';

@Component({
  selector: 'app-deposit-form',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  providers: [SystemMessageService, LoadingMaskService],
  templateUrl: './deposit-form.component.html',
  styleUrl: './deposit-form.component.scss',
})
export class DepositFormComponent extends BaseFormCompoent implements OnInit {
  uuid!: string;

  constructor(
    private location: Location,
    private dialogConfig: DynamicDialogConfig,
    public ref: DynamicDialogRef,
    private accountService: AccountService,
    private loadingMaskService: LoadingMaskService,
    private messageService: SystemMessageService,
  ) {
    super();
  }
  ngOnInit(): void {
    // 監聽上一頁切換，關閉 Dialog
    this.location.onUrlChange(() => {
      this.onCloseForm();
    });

    // 初始化表單
    this.formGroup = new FormGroup({
      money: new FormControl(''), // 加值金額
    });

    this.uuid = this.dialogConfig.data;
  }

  /**
   * 提交資料
   */
  onSubmit() {
    this.submitted = true;
    if (this.formGroup.invalid || !this.submitted) {
      return;
    }

    this.loadingMaskService.show();
    let formData = this.formGroup.value;
    let requestData: DepositMoneyResource = {
      uuid: this.uuid,
      money: formData.money,
    };
    this.accountService
      .deposit(requestData)
      .pipe(
        finalize(() => {
          this.loadingMaskService.hide();
          this.submitted = false;
        }),
      )
      .subscribe({
        next: (res) => {
          console.log(res);
          if (res.code === '200' || res.code === '201') {
            this.messageService.success(res?.message);
            this.onCloseForm();
          } else {
            this.messageService.error(res?.message);
          }
        },
        error: (err) => {
          this.messageService.error(err);
        },
      });
  }

  /**
   * 關閉 Dialog
   */
  onCloseForm() {
    console.log('關閉 Dialog');
    this.ref.close();
  }
}
