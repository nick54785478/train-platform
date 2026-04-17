import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { map } from 'rxjs/internal/operators/map';
import { filter } from 'rxjs/internal/operators/filter';
import { take } from 'rxjs/internal/operators/take';

/**
 * 反向守衛 (GuestGuard)
 *
 * 邏輯是：如果你已經登入，任何想要去 /login 的請求都會被強行彈回首頁。
 */
@Injectable({
  providedIn: 'root',
})
export class GuestGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.getInitialized().pipe(
      filter((init) => init), // 確保拿到初始化後的狀態
      take(1),
      map(() => {
        if (this.authService.isAuthenticated()) {
          // 關鍵點：如果你已經登入，直接把你送走，不准載入 Login 組件
          return this.router.createUrlTree(['/']);
        }
        return true;
      }),
    );
  }
}
