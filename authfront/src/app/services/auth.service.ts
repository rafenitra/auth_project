import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { finalize, Observable } from 'rxjs';
import { environment } from '../../environments/environment';


@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private apiUrl = environment.apiUrl + '/auth';
  //private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient){
  }

  register(data: any): Observable<string>{
    return this.http.post(`${this.apiUrl}/register`, data, { responseType: 'text' });
  }

  login(data: any): Observable<any>{
    return this.http.post(`${this.apiUrl}/login`, data);
  }

  saveTokens(accessToken: string, refreshToken: string):void {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }

  getAccessToken(){
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(){
    return localStorage.getItem('refreshToken');
  }



  me():Observable<any>{
    return this.http.get(`${this.apiUrl}/me`);
  }

  refresh(refreshToken: String): Observable<any>{
    return this.http.post(`${this.apiUrl}/refresh`, { refreshToken });
  }

  logout(refreshToken: String): Observable<string>{

    return this.http.post(`${this.apiUrl}/logout`,{refreshToken}, {responseType: 'text'})
    .pipe(
      finalize(()=>{
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
      })
    );
  }
}
