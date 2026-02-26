import { TestBed } from "@angular/core/testing";
import { AuthService } from "../services/auth.service";
import { Router } from "@angular/router";
import { TokenRefreshService } from "../services/token-refresh.service";
import { HttpErrorResponse, HttpRequest, HttpResponse } from "@angular/common/http";
import { of, throwError } from "rxjs";
import { authInterceptorFn } from "./auth.interceptor";


describe('authInterceptorFn', () =>{

    let authSpy: jasmine.SpyObj<AuthService>;
    let routerSpy: jasmine.SpyObj<Router>;
    let refreshState: TokenRefreshService;

    beforeEach(() => {
        authSpy = jasmine.createSpyObj('AuthService', [
        'getAccessToken',
        'getRefreshToken',
        'refresh',
        'saveTokens',
        'logout'
        ]);
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);
        refreshState = { isRefreshing: false } as TokenRefreshService;

        TestBed.configureTestingModule({
            providers: [
                { provide: AuthService, useValue: authSpy },
                { provide: Router, useValue: routerSpy },
                { provide: TokenRefreshService, useValue: refreshState }
            ]
        });
    });

    it('should add Authorization header when accessToken exists', () => {
        authSpy.getAccessToken.and.returnValue('old-token');

        // next simule le handler HTTP qui reçoit la requête modifiée
        const next = (req: HttpRequest<any>) => {
            // vérifier que le header a été ajouté
            expect(req.headers.get('Authorization')).toBe('Bearer old-token');
            return of(new HttpResponse({ status: 200, body: { ok: true } }));
        };

        const req = new HttpRequest('GET', '/api/test');

        const result = TestBed.runInInjectionContext(() =>
            authInterceptorFn(req, next)
        );

        // result est un Observable qui émet la réponse
        result.subscribe((res) => {
            expect((res as HttpResponse<any>).status).toBe(200);
        });
    });

    it('should not add Authorization header when no accessToken', () => {
        authSpy.getAccessToken.and.returnValue(null);

        const next = (req: HttpRequest<any>) => {
            expect(req.headers.has('Authorization')).toBeFalse();
            return of(new HttpResponse({ status: 200 }));
        };

        const req = new HttpRequest('GET', '/api/test');

        const result = TestBed.runInInjectionContext(() =>
            authInterceptorFn(req, next)
        );

        result.subscribe((res) => {
            expect((res as HttpResponse<any>).status).toBe(200);
        });
    });


    it('should logout and navigate to /login when 401 and no refreshToken', () => {
        authSpy.getAccessToken.and.returnValue('old-token');
        authSpy.getRefreshToken.and.returnValue(null);

        // next renvoie une erreur 401
        const next = (_req: HttpRequest<any>) =>
        throwError(() => new HttpErrorResponse({ status: 401 }));

        const req = new HttpRequest('GET', '/api/protected');

        const result$ = TestBed.runInInjectionContext(() =>
        authInterceptorFn(req, next)
        );

        result$.subscribe({
        next: () => fail('should not succeed'),
        error: (err) => {
            expect(err.status).toBe(401);
            expect(authSpy.logout).toHaveBeenCalledWith('');
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
        }
        });
    });


    it('should refresh token on 401 when refreshToken exists and retry original request', () => {
    authSpy.getAccessToken.and.returnValue('old-token');
    authSpy.getRefreshToken.and.returnValue('refresh-123');

    // mock refresh pour renvoyer de nouveaux tokens
        authSpy.refresh.and.returnValue(of({ accessToken: 'new-token', refreshToken: 'new-refresh' }));
        authSpy.saveTokens.and.callThrough();

        // next doit se comporter différemment selon le header Authorization reçu :
        // - si header = old-token => renvoyer 401
        // - si header = new-token => renvoyer succès
        const next = (req: HttpRequest<any>) => {
        const authHeader = req.headers.get('Authorization');
        if (authHeader === 'Bearer old-token') {
            return throwError(() => new HttpErrorResponse({ status: 401 }));
        }
        if (authHeader === 'Bearer new-token') {
            return of(new HttpResponse({ status: 200, body: { ok: true } }));
        }
        return of(new HttpResponse({ status: 200 }));
        };

        const req = new HttpRequest('GET', '/api/protected');

        const result$ = TestBed.runInInjectionContext(() =>
        authInterceptorFn(req, next)
        );

        result$.subscribe({
        next: (res) => {
            expect((res as HttpResponse<any>).status).toBe(200);
            // saveTokens doit avoir été appelé avec les nouveaux tokens
            expect(authSpy.saveTokens).toHaveBeenCalledWith('new-token', 'new-refresh');
        },
        error: () => fail('should not error after successful refresh and retry')
        });
    });


    it('should logout and navigate when refresh fails', () => {
        authSpy.getAccessToken.and.returnValue('old-token');
        authSpy.getRefreshToken.and.returnValue('refresh-123');

        // refresh échoue
        authSpy.refresh.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

        const next = (_req: HttpRequest<any>) =>
        throwError(() => new HttpErrorResponse({ status: 401 }));

        const req = new HttpRequest('GET', '/api/protected');

        const result$ = TestBed.runInInjectionContext(() =>
        authInterceptorFn(req, next)
        );

        result$.subscribe({
        next: () => fail('should not succeed'),
        error: (err) => {
            expect(err.status).toBe(500);
            expect(authSpy.logout).toHaveBeenCalledWith('refresh-123');
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
        }
        });
    });

    it('should not attempt refresh when isRefreshing is true and rethrow original error', () => {
    authSpy.getAccessToken.and.returnValue('old-token');
    authSpy.getRefreshToken.and.returnValue('refresh-123');

    // forcer l'état isRefreshing
    refreshState.isRefreshing = true;

    const next = (_req: HttpRequest<any>) =>
      throwError(() => new HttpErrorResponse({ status: 401 }));

    const req = new HttpRequest('GET', '/api/protected');

    const result$ = TestBed.runInInjectionContext(() =>
      authInterceptorFn(req, next)
    );

    result$.subscribe({
      next: () => fail('should not succeed'),
      error: (err) => {
        expect(err.status).toBe(401);
        // si isRefreshing true, on ne doit pas appeler refresh ni logout ici
        expect(authSpy.refresh).not.toHaveBeenCalled();
        // on ne force pas la navigation ici (le code d'origine renvoie throwError)
      }
    });
  });


});