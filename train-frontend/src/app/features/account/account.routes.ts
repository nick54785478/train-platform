import { Routes } from '@angular/router';
import { AccountComponent } from './pages/account/account.component';

/**
 * 定義 Users 子路由配置的檔案
 */
export const routes: Routes = [
  {
    path: '',
    component: AccountComponent,
  },
];
