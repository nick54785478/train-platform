import { Routes } from '@angular/router';
import { LoginComponent } from './features/layout/login/login.component';
import { NotFoundComponent } from './features/layout/not-found/not-found.component';
import { AccessDeniedComponent } from './features/layout/access-denied/access-denied.component';
import { ErrorComponent } from './features/layout/error/error.component';
import { LayoutComponent } from './features/layout/pages/layout.component';
import { AuthGuard } from './core/guards/auth.guard';
import { RegisterComponent } from './features/register/pages/register.component';
import { FormInvalidComponent } from './features/layout/form-invalid/form-invalid.component';
import { GuestGuard } from './core/guards/guest.guard';

/**
 * 定義根路由配置的檔案
 */
export const routes: Routes = [
  // 登入頁面（公開路徑）
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [GuestGuard],
  },

  // 主版面，所有子頁面都經 AuthGuard
  {
    path: '',
    component: LayoutComponent, // Layout 作為主版面
    canActivate: [AuthGuard], // Guard 決定是否有 token
    children: [
      {
        path: '', // 空路徑，Guard 判斷後決定導向 /features 或 /login
        loadChildren: () =>
          import('./features/features.module').then((m) => m.FeaturesModule),
      },
      {
        path: 'redirect',
        loadChildren: () =>
          import('./core/components/redirect/redirect.module').then(
            (m) => m.RedirectModule,
          ),
      },
    ],
  },

  // Not Found
  {
    path: 'not-found',
    component: NotFoundComponent,
  },
  // Access Denied
  {
    path: 'access-denied',
    component: AccessDeniedComponent,
  },
  // Form Invalid
  {
    path: 'form-invalid',
    component: FormInvalidComponent,
  },
  // Error
  {
    path: 'error',
    component: ErrorComponent,
  },

  // 通配符路由 → 404
  {
    path: '**',
    redirectTo: 'not-found',
    pathMatch: 'full',
  },
];
