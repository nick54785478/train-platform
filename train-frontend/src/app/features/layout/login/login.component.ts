import { Component, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { CoreModule } from '../../../core/core.module';
import { BaseFormCompoent } from '../../../shared/component/base/base-form.component';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { LoginService } from '../services/login.service';
import { finalize, Subject, takeUntil } from 'rxjs';
import { LoadingMaskService } from '../../../core/services/loading-mask.service';
import { SystemMessageService } from '../../../core/services/system-message.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { StorageService } from '../../../core/services/storage.service';
import { SystemStorageKey } from '../../../core/enums/system-storage.enum';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [SharedModule, CoreModule],
  providers: [
    AuthService,
    StorageService,
    Router,
    LoadingMaskService,
    SystemMessageService,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent
  extends BaseFormCompoent
  implements OnInit, OnDestroy
{
  private readonly _destroying$ = new Subject<void>();

  constructor(
    public router: Router,
    private loginService: LoginService,
    private authService: AuthService,
    private storageService: StorageService,
    private messageService: SystemMessageService,
    private loadingMaskService: LoadingMaskService,
  ) {
    super();
    console.log('LoginComponent 被實例化了！');
  }

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      username: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required]),
    });
  }

  /**
   * 使用者登入
   * @returns
   */
  login() {
    // 登入前清除舊 token
    this.authService.clearToken();

    this.submitted = true;

    if (!this.submitted || !this.formGroup.valid) {
      return;
    }

    const formData = this.formGroup.value;
    const request = { ...formData };

    this.loginService
      .login(request)
      .pipe(
        takeUntil(this._destroying$),
        finalize(() => {
          this.submitted = false;
        }),
      )
      .subscribe({
        next: (res) => {
          console.log(res);

          if (res?.code === '500') {
            this.messageService.error(res.message);
            return;
          }

          this.messageService.success('使用者登入成功');

          // 1. 存 token（順序很重要）
          this.setJwToken(res.token);

          // 2. 存 refresh token
          this.storageService.setLocalStorageItem(
            SystemStorageKey.REFRESH_TOKEN,
            res?.refreshToken,
          );

          // 3. 存 username
          this.setUsername(formData.username);

          // 4. 更新狀態流（可保留）
          this.authService.tokenSubject$.next(res.token);

          // 5. 取得原本要去的頁面（重點）
          const redirectUrl =
            this.storageService.getSessionStorageItem(
              SystemStorageKey.REDIRECT_URL,
            ) || '/home';

          // 6. 清掉（避免污染）
          this.storageService.removeSessionStorageItem(
            SystemStorageKey.REDIRECT_URL,
          );

          console.log('導向:', redirectUrl);

          // 7. 導頁（最終修正）
          this.router.navigate([redirectUrl]);
        },
        error: (err) => {
          this.messageService.error(err.message);
        },
      });
  }

  ngOnDestroy(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }

  /**
   * 設置使用者名稱進 LocalStorage 和 SessionStorage
   * @param username
   */
  setUsername(username: string) {
    // 透過後端 API 取得使用者個人資訊
    // this.loginService.getUserInfo(username).subscribe((res) => {
    //   // 設置 name
    //   this.storageService.setSessionStorageItem(
    //     SystemStorageKey.NAME,
    //     res.name
    //   );
    //   // 設置 name
    //   this.storageService.setLocalStorageItem(SystemStorageKey.NAME, res.name);
    // });

    // 設置 username
    this.storageService.setSessionStorageItem(
      SystemStorageKey.USERNAME,
      username,
    );

    // 設置 username
    this.storageService.setLocalStorageItem(
      SystemStorageKey.USERNAME,
      username,
    );
  }

  /**
   * 設置 Token 進 LocalStorage 和 SessionStorage
   * @param token
   */
  setJwToken(token: string) {
    // 先將 Token 清除，再設置進去
    this.storageService.removeSessionStorageItem(SystemStorageKey.JWT_TOKEN);
    // 設置 Token 進 SessionStorage
    this.storageService.setSessionStorageItem(
      SystemStorageKey.JWT_TOKEN,
      token,
    );
    // 設置 Token 進 LocalStorage
    this.storageService.setLocalStorageItem(SystemStorageKey.JWT_TOKEN, token);
  }
}
