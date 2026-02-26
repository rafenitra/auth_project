import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent {

  user!: any;
  loading!: boolean;
  errorMessage !:string;

  constructor(private authService: AuthService) {
    this.user = null;
    this.loading = true;
    this.errorMessage = '';
  }

  ngOnInit(): void {
    this.authService.me().subscribe({
      next: (res) => {
        this.user = res;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Impossible de charger votre profil.';
        this.loading = false;
      }
    });
  }

}
