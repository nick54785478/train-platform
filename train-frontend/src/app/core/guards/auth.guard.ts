import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { StorageService } from '../services/storage.service';
import { SystemStorageKey } from '../enums/system-storage.enum';
import { map } from 'rxjs/internal/operators/map';
import { of } from 'rxjs/internal/observable/of';
import { filter } from 'rxjs/internal/operators/filter';
import { take } from 'rxjs/internal/operators/take';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  private platformId = inject(PLATFORM_ID);
  private publicPaths: string[] = ['/login', '/help', '/register']; // 公開路徑

  constructor(
    private authService: AuthService,
    private router: Router,
    private storageService: StorageService,
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): Observable<boolean | UrlTree> {
    const currentPath = state.url;

    // 如果是在伺服器端渲染，直接不准跳轉登入頁，畫面會保持空白直到前端接手
    if (!isPlatformBrowser(this.platformId)) {
      return of(false); // 或返回一個特定的空白 RouterTree
    }

    // 公開路徑直接放行
    if (this.publicPaths.includes(currentPath)) {
      return of(true);
    }

    // 等待 AuthService 初始化完成
    return this.authService.getInitialized().pipe(
      filter((init) => init), // 等初始化完成
      take(1),
      map(() => {
        const isAuth = this.authService.isAuthenticated();
        console.log(
          'AuthGuard 檢查結果:',
          isAuth,
          'Token:',
          this.authService.getJwtToken(),
        );
        if (isAuth) {
          // 已登入 → 放行
          return true;
        }

        // 未登入 → 存下要跳轉的 url，導向 login
        this.storageService.setSessionStorageItem(
          SystemStorageKey.REDIRECT_URL,
          state.url,
        );

        return this.router.createUrlTree(['/login']);
      }),
    );
  }
}
