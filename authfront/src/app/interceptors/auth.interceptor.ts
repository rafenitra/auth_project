import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { TokenRefreshService } from '../services/token-refresh.service';

export const authInterceptorFn: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const router = inject(Router);
  const refreshState = inject(TokenRefreshService);

  const accessToken = authService.getAccessToken();

  let authReq = req;

  if (accessToken) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${accessToken}` }
    });
  }

  return next(authReq).pipe(
    catchError((error) => {

      if (error.status === 401) {

        const refreshToken = authService.getRefreshToken();

        if (!refreshToken) {
          authService.logout("");
          router.navigate(['/login']);
          return throwError(() => error);
        }

        //  Empêche la boucle infinie
        if (refreshState.isRefreshing) {
          return throwError(() => error);
        }

        refreshState.isRefreshing = true;

        return authService.refresh(refreshToken).pipe(
          switchMap((res: any) => {
            refreshState.isRefreshing = false;

            authService.saveTokens(res.accessToken, res.refreshToken);

            const newReq = req.clone({
              setHeaders: { Authorization: `Bearer ${res.accessToken}` }
            });

            return next(newReq);
          }),
          catchError((refreshError) => {
            refreshState.isRefreshing = false;
            authService.logout(refreshToken);
            router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};