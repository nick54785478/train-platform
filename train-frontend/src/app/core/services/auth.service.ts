import { Injectable, OnDestroy } from '@angular/core';
import { StorageService } from './storage.service';
import { SystemStorageKey } from '../enums/system-storage.enum';
import { Observable } from 'rxjs/internal/Observable';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { of } from 'rxjs/internal/observable/of';
import { BehaviorSubject, Subject } from 'rxjs';
import { RefreshTokenResponse } from '../models/refresh-token.response.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly baseApiUrl = environment.apiEndpoint;

  tokenSubject$ = new BehaviorSubject<string>('');

  constructor(
    private storageService: StorageService,
    private http: HttpClient,
  ) {}

  private initialized$ = new BehaviorSubject<boolean>(false);

  initAuth(): Promise<void> {
    return new Promise((resolve) => {
      const token = this.getJwtToken();

      // 如果有 Token，先塞進 Subject
      if (token && !this.checkExpired(token)) {
        this.tokenSubject$.next(token);
      }

      // 稍微延後 50ms 再宣告初始化完成
      // 這 50ms 會被 app.component 的 splashScreen 蓋住，使用者無感，但能確保路由狀態穩定
      setTimeout(() => {
        this.initialized$.next(true);
        resolve();
      }, 50);
    });
  }
  getInitialized(): Observable<boolean> {
    return this.initialized$.asObservable();
  }

  /**
   * 取得 JwtToken
   * @returns token
   */
  getJwtToken(): string | null {
    return (
      this.storageService.getLocalStorageItem(SystemStorageKey.JWT_TOKEN) ||
      this.storageService.getSessionStorageItem(SystemStorageKey.JWT_TOKEN)
    );
  }

  /**
   * 刷新 Token
   * */
  refreshToken(refreshToken: string): Observable<RefreshTokenResponse> {
    return this.http.post<RefreshTokenResponse>(`${this.baseApiUrl}/refresh`, {
      token: refreshToken,
    });
  }

  /**
   * 確定是否已登入
   * @param token
   * @returns boolean
   */
  isAuthenticated(): boolean {
    const token = this.getJwtToken();
    return !!token && !this.checkExpired(token);
  }

  /**
   * 清除 Token
   */
  clearToken() {
    this.storageService.removeLocalStorageItem(SystemStorageKey.JWT_TOKEN);
    this.storageService.removeSessionStorageItem(SystemStorageKey.JWT_TOKEN);
  }

  /**
   * 檢測 Token 是否過期
   * @param token
   * @returns boolean
   */
  checkExpired(token: string): boolean {
    if (!token) {
      return true;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp <= currentTime;
    } catch {
      return true;
    }
  }
}
