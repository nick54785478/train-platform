import {
  APP_INITIALIZER,
  ApplicationConfig,
  importProvidersFrom,
  provideZoneChangeDetection,
} from '@angular/core';
import {
  provideRouter,
  withEnabledBlockingInitialNavigation,
} from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CoreModule } from './core/core.module';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
} from '@angular/common/http';
import { LayoutService } from './features/layout/services/layout.service';
import { SharedModule } from './shared/shared.module';
import { MessageModule } from 'primeng/message';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { AuthService } from './core/services/auth.service';

/**
 * 用於集中管理應用程序配置的檔案。這種設計模式有助於將應用程序的設置與邏輯分離，並提高可維護性和靈活性。
 * 可以包含應用的靜態配置，例如 API 基本 URL、環境變數、功能開關等。
 * - 跨模組共享設置: 將配置集中存放在 app.config.ts 中，並導出配置常量或函數，可以輕鬆被其他模組或服務注入或引用。
 * - 區分環境: 可以結合 Angular 的環境管理（environments 文件夾），在不同環境（如開發、測試、或生產）中切換配置。
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(
      routes,
      withEnabledBlockingInitialNavigation(), // 強迫 Router 等初始化完全結束才開始第一次導航
    ),

    // importProvidersFrom([BrowserAnimationsModule]) 需透過此注入，PrimeNG 才會有效果
    // 允許在應用中使用 Angular 動畫。這是處理動畫功能所必須的模組，通常在應用啟動時需要進行導入。
    importProvidersFrom([
      BrowserAnimationsModule,
      CoreModule,
      SharedModule,
      MessageModule,
    ]),

    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AuthService],
      multi: true,
    },

    provideHttpClient(withFetch(), withInterceptors([jwtInterceptor])), // 注入 provideHttpClient 以供應用層使用 HttpClient
    LayoutService, // 提供 LayoutService
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideClientHydration(),
  ],
};

// 定義一個初始化函數
function initializeApp(authService: AuthService) {
  return () => authService.initAuth();
}

function providePrimeNG(arg0: {
  theme: { preset: any };
}):
  | import('@angular/core').Provider
  | import('@angular/core').EnvironmentProviders {
  throw new Error('Function not implemented.');
}

function provideAnimationsAsync():
  | import('@angular/core').Provider
  | import('@angular/core').EnvironmentProviders {
  throw new Error('Function not implemented.');
}
