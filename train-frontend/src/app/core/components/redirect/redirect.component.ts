import {
  AfterViewChecked,
  AfterViewInit,
  Component,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { CoreModule } from '../../core.module';
import { SharedModule } from '../../../shared/shared.module';
import { AuthService } from '../../services/auth.service';
import { StorageService } from '../../services/storage.service';
import { SystemStorageKey } from '../../enums/system-storage.enum';
import { Subject } from 'rxjs/internal/Subject';
import { Router, RouterModule } from '@angular/router';
import { NavigateService } from '../../services/navigate.service';
import {
  async,
  defaultIfEmpty,
  delay,
  firstValueFrom,
  lastValueFrom,
  startWith,
  takeUntil,
  tap,
  timer,
} from 'rxjs';
import { CommonModule } from '@angular/common';
import { LoginComponent } from '../../../features/layout/login/login.component';

@Component({
  selector: 'app-redirect',
  standalone: true,
  imports: [SharedModule, CoreModule],
  templateUrl: './redirect.component.html',
  styleUrl: './redirect.component.scss',
  providers: [],
})
export class RedirectComponent implements OnInit, OnDestroy {
  private readonly _destroying$ = new Subject<void>();

  constructor(
    private router: Router,
    private authService: AuthService,
    private storageService: StorageService,
  ) {}

  ngOnInit(): void {
    this.redirect();
  }

  ngOnDestroy(): void {
    this._destroying$.next();
    this._destroying$.complete();
  }

  /**
   * 重導向
   */
  redirect() {
    // 🔥 取出 redirectUrl
    const redirectUrl =
      this.storageService.getSessionStorageItem(
        SystemStorageKey.REDIRECT_URL,
      ) || '/home'; // ✅ 改這裡（不要再用 /redirect）

    // 🔥 用完就清掉（避免下次誤用）
    this.storageService.removeSessionStorageItem(SystemStorageKey.REDIRECT_URL);

    // 🔥 取得 query params
    const queryParams = this.storageService.getSessionStorageItem(
      SystemStorageKey.QUERY_PARAMS,
    );

    // 🔥 同樣清掉
    this.storageService.removeSessionStorageItem(SystemStorageKey.QUERY_PARAMS);

    if (queryParams) {
      this.router.navigate([redirectUrl], {
        queryParams: JSON.parse(queryParams),
      });
    } else {
      this.router.navigate([redirectUrl]);
    }
  }
}
