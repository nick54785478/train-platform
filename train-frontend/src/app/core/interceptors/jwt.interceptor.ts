import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';
import { catchError, filter, switchMap, take, throwError } from 'rxjs';
import { SystemMessageService } from '../services/system-message.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const messageService = inject(SystemMessageService);

  const isPublicApi =
    req.url.includes('/api/v1/login') || req.url.includes('/api/v1/refresh');

  if (isPublicApi) {
    return next(req);
  }

  return authService.getInitialized().pipe(
    filter((init) => init),
    take(1),
    switchMap(() => {
      const token = authService.getJwtToken();

      if (!token || authService.checkExpired(token)) {
        console.warn('Token 無效');

        authService.clearToken();

        // ❗不要導頁
        return throwError(() => new Error('UNAUTHORIZED'));
      }

      const authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });

      return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401 || error.status === 403) {
            authService.clearToken();

            messageService.error('登入已失效，請重新登入');

            // ❗不要在這裡導頁
          }

          return throwError(() => error);
        }),
      );
    }),
  );
};
