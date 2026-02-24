import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { RegisterComponent } from './register/register.component/register.component';
import { LoginComponent } from './login/login.component/login.component';
import { ReactiveFormsModule } from '@angular/forms';
import {  HTTP_INTERCEPTORS, provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptorFn } from './interceptors/auth.interceptor';
import { AccueilComponent } from './accueil/accueil.component/accueil.component';
import { ProfileComponent } from './profile/profile.component/profile.component';


@NgModule({
  declarations: [
    App,
    RegisterComponent,
    LoginComponent,
    AccueilComponent,
    ProfileComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(

      withInterceptors([authInterceptorFn])
    )
  ],
  bootstrap: [App]
})
export class AppModule { }
