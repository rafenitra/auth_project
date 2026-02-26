import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterComponent } from './register.component';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';



describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {

    const authSpy = jasmine.createSpyObj('AuthService', ['register']);

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [ReactiveFormsModule],
      providers:[
        {provide: AuthService, useValue: authSpy}
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('il doit avoir un formulaire de registre invalide au début', () =>{
    expect(component.form.valid).toBeFalse();
  });

  it('il doit y avoir les trois champs email, username et password',()=>{
    component.form.setValue({
      email: '',
      username: '',
      password: ''
    });

    expect(component.form.valid).toBeFalse();

    component.form.setValue({
      email: 'test@example.com',
      username: 'testuser',
      password: 'password123'
    });
    expect(component.form.valid).toBeTrue();
  });


  it('ceci doit appeler la methode register d\'AuthService',()=>{
    authServiceSpy.register.and.returnValue(of('OK'));

    component.form.setValue({
      email: 'test@example.com',
      password: 'password123',
      username: 'testuser'
    });

    component.register();

    expect(authServiceSpy.register).toHaveBeenCalledWith({
      email: 'test@example.com',
      password: 'password123',
      username: 'testuser'
    });
  });


   it('ceci ne doit pas appeler la méthode register car form est invalide', ()=>{
    component.form.setValue({
      email: 'invalid-email',
      password: '',
      username: 'myUsername'
    });

    component.register();
    expect(authServiceSpy.register).not.toHaveBeenCalled();
   });

   it('ceci doit donner un message d\'erreur en cas d\'erreur de register', ()=>{
    authServiceSpy.register.and.returnValue(throwError(()=>new Error('Erreur')));

    component.form.setValue({
      email: 'test@example.com',
      password: 'password123',
      username: 'testuser'
    });
    
    component.register();
    expect(authServiceSpy.register).toHaveBeenCalled();
    expect(component.errorMessage).toBe('Une erreur est survenue.');

  });


});
