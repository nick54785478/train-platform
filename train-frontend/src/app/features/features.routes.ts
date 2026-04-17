import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';

/**
 * 定義 Features 子路由配置的檔案
 */
export const routes: Routes = [
  // 預設路徑顯示 HomeComponent
  {
    path: '',
    loadChildren: () => import('./home/home.module').then((m) => m.HomeModule),
  },

  {
    path: 'home',
    loadChildren: () => import('./home/home.module').then((m) => m.HomeModule),
  },

  // 註冊頁面
  {
    path: 'register',
    loadChildren: () =>
      import('./register/register.module').then((m) => m.RegisterModule),
  },
  // 車次頁面
  {
    path: 'train',
    loadChildren: () =>
      import('./train/train.module').then((m) => m.TrainModule),
  },
  {
    path: 'ticket',
    loadChildren: () =>
      import('./ticket/ticket.module').then((m) => m.TicketModule),
  },
  {
    path: 'account',
    loadChildren: () =>
      import('./account/account.module').then((m) => m.AccountModule),
  },
  {
    path: 'setting',
    loadChildren: () =>
      import('./setting/setting.module').then((m) => m.SettingModule),
  },
  {
    path: 'upload',
    loadChildren: () =>
      import('./upload/upload.module').then((m) => m.UploadModule),
  },
];
