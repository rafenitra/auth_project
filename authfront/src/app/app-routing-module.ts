import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterComponent } from './register/register.component/register.component';
import { LoginComponent } from './login/login.component/login.component';
import { AccueilComponent } from './accueil/accueil.component/accueil.component';
import { ProfileComponent } from './profile/profile.component/profile.component';
import { authGuardGuard } from './guards/auth.guard-guard';

const routes: Routes = [
  {path: 'register', component: RegisterComponent},
  {path: 'login', component: LoginComponent},
  {path: 'accueil', component: AccueilComponent} ,
  {path: 'profile', component: ProfileComponent, canActivate: [authGuardGuard]},
  {path: '', redirectTo: '/login', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
