import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  
  form : FormGroup;
  loading !: boolean;
  errorMessage !: string;
  successMessage !: string;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ){
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.errorMessage = '';
    this.successMessage = '';
    this.loading = false;
  }

  login():void {
    if(this.form.invalid){
      this.errorMessage = 'Veuillez remplir correctementr le formulaire';
      return; 
    }

    this.loading = true;
    
    this.authService.login(this.form.value).subscribe({
      next: (response) => {
        this.loading = false;

        if(response.accessToken && response.refreshToken){
          this.authService.saveTokens (response.accessToken, response.refreshToken);

          this.successMessage = 'Connexion réussie ! Redirection...';

          setTimeout(()=> {
            this.router.navigate(['/accueil']);
          },1500);
        }
      },

      error: (err) => {
        switch (err.status) {
          case 400:
            this.errorMessage = 'Email ou mot de passe invalide.';
            this.loading = false;
            break;

          case 401:
            this.errorMessage = 'Identifiants incorrects.';
            this.loading = false;
            break;

          case 404:
            this.errorMessage = 'Utilisateur introuvable.';
            this.loading = false;
            break;

          case 500:
            this.errorMessage = 'Erreur interne du serveur.';
            this.loading = false;
            break;

          default:
            this.errorMessage = 'Une erreur est survenue.';
            this.loading = false;
        }
      }
    })
  }

  ngOnInit(): void {
    if(this.authService.getAccessToken()){
      this.router.navigate(['/accueil']);
    }
  }
}
