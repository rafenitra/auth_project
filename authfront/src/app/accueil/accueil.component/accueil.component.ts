import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-accueil',
  standalone: false,
  templateUrl: './accueil.component.html',
  styleUrl: './accueil.component.css',
})
export class AccueilComponent {

  showMenu = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleMenu() {
    this.showMenu = !this.showMenu;
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  logout() {
    this.authService.logout(localStorage.getItem('refreshToken') || "").subscribe((res)=>{
      // Redirection vers la page de login ou autre action
      if(res.length > 0){
        this.router.navigate(['/login']);
      }
    });
  }

}
