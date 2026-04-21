import { Routes } from '@angular/router';
import { EmailTemplateComponent } from './pages/email-template/email-template.component';

/**
 * 定義 Users 子路由配置的檔案
 */
export const routes: Routes = [
  // 預設路徑顯示 SettingComponent
  {
    path: '',
    component: EmailTemplateComponent,
  },
];
