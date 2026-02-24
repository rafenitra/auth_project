import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, Observable, switchMap, throwError } from "rxjs";
import { AuthService } from "../services/auth.service";
import { Router } from "@angular/router";

@Injectable()
export class AuthInterceptor implements HttpInterceptor{

    private isRefreshing = false;

    constructor(private authService: AuthService, private router: Router){}


    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        
        const accessToken = this.authService.getAccessToken();

        let authReq = req;

        if (accessToken) {
            authReq = authReq.clone({
                setHeaders: {Authorization: `Bearer ${accessToken}`}
            });
        }

        return next.handle(authReq).pipe(
            catchError((error: HttpErrorResponse)=> {

                //cas 1
                if( error.status === 401 && !this.isRefreshing){
                    this.isRefreshing = true;

                    const refreshToken = this.authService.getRefreshToken();

                    // Si pas de refreshToken → logout direct
                    if (!refreshToken) {
                        this.isRefreshing = false;
                        this.authService.logout(refreshToken || "").subscribe(()=>{
                            // Redirection vers la page de login ou autre action
                            this.router.navigate(['/login']);
                        });
                        return throwError(() => error);
                    }

                    return this.authService.refresh(refreshToken).pipe(
                        switchMap((res: any)=>{
                            this.isRefreshing = false;

                            this.authService.saveTokens(res.accessToken, res.refreshToken);

                            const newReq = req.clone({
                                setHeaders: {Authorization: `Bearer ${res.accessToken}`}
                            });

                            return next.handle(newReq);
                        }),

                        catchError((err)=>{
                            this.isRefreshing = false;
                            this.authService.logout(refreshToken).subscribe(()=>{
                                // Redirection vers la page de login ou autre action
                                this.router.navigate(['/login']);
                            });
                            return throwError(() => err);
                        })
                    );
                }

                //cas 2
                return throwError(() => error);
            })
        );
    }
    
}
