import { Routes } from '@angular/router';
import { UploadComponent } from './pages/upload/upload.component';

/**
 * 定義 Users 子路由配置的檔案
 */
export const routes: Routes = [
  {
    path: '',
    component: UploadComponent,
  },
];
