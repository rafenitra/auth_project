import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {

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
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['',[Validators.required, Validators.minLength(6)]],
    })
  }

  

  register():void {
    if(this.form.invalid){
      this.errorMessage = 'Veuilez remplir correctement le formulaire';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Votre compte a été bien créé.';
        
        setTimeout(()=>{
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: (err) => {
        this.loading = false;
        

        switch (err.status) {
        case 400:
          this.errorMessage = 'Données invalides. Vérifiez le formulaire.';
          break;

        case 409:
          this.errorMessage = 'Cet email est déjà utilisé.';
          break;

        case 500:
          this.errorMessage = 'Une erreur est survenue.';
          break;

        default:
          this.errorMessage = err.error?.message || 'Une erreur est survenue.';
      }

      }
    })
  }

}
