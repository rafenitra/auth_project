import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { Router } from '@angular/router';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['login','getAccessToken','getRefreshToken','refresh','saveTokens']);
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers:[
        {provide: AuthService, useValue: authSpy},
        {provide: Router, useValue: routerMock}
      ],
      imports: [ReactiveFormsModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    authServiceSpy.getAccessToken.and.returnValue(null);
    authServiceSpy.getRefreshToken.and.returnValue(null);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('il doit avoir un formulaire de login invalide au début', () =>{
    expect(component.form.valid).toBeFalse();
  });

  it('ceci teste si les champs username et password existent', ()=>{
    component.form.setValue({
      email: '',
      password: ''
    });

    expect(component.form.valid).toBeFalse();

    component.form.setValue({
      email: 'test@example.com',
      password: 'password123'
    });
    expect(component.form.valid).toBeTrue();
  });
  
  it('ceci doit appeler la methode login d\'AuthService',()=>{

    const userMock = {
      email: 'test@example.com',
      password: 'password123'
    };
      authServiceSpy.login.and.returnValue(of({accessToken: 'fakeAccessToken'}));
  
      component.form.setValue(userMock);
  
      component.login();
      expect(authServiceSpy.login).toHaveBeenCalledWith(userMock);
    });

    it('ceci ne doit pas appeler la méthode login car form est invalide', ()=>{
    component.form.setValue({
      email: 'invalid-email',
      password: ''
    });

    component.login();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
   });


   it('ceci doit appeler login et redirecte en cas de success', fakeAsync (()=>{
    component.form.setValue({
      email: 'test@gmail.com',
      password: 'pass213'
    });

    authServiceSpy.login.and.returnValue(of({
      accessToken: 'abc',
      refreshToken: 'xyz'
    }));

    component.login();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      email: 'test@gmail.com',
      password: 'pass213'
    });

    expect(authServiceSpy.saveTokens).toHaveBeenCalledWith('abc','xyz');
    expect(component.successMessage).toBe('Connexion réussie ! Redirection...');

    tick(1500);

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/accueil']);

   }));


   // -------------------------------------------------------
  // 6) ngOnInit : refresh automatique si tokens présents
  // -------------------------------------------------------
  it('should refresh tokens on init if tokens exist', () => {
    authServiceSpy.getAccessToken.and.returnValue('oldAccess');
    authServiceSpy.getRefreshToken.and.returnValue('oldRefresh');

    authServiceSpy.refresh.and.returnValue(
      of({
        accessToken: 'newAccess',
        refreshToken: 'newRefresh'
      })
    );

    component.ngOnInit();

    expect(authServiceSpy.refresh).toHaveBeenCalledWith('oldRefresh');
    expect(authServiceSpy.saveTokens).toHaveBeenCalledWith('newAccess', 'newRefresh');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/accueil']);
  });

  // -------------------------------------------------------
  // 7) ngOnInit : pas de refresh si tokens absents
  // -------------------------------------------------------
  it('should NOT refresh tokens if none exist', () => {
    authServiceSpy.getAccessToken.and.returnValue(null);
    authServiceSpy.getRefreshToken.and.returnValue(null);

    component.ngOnInit();

    expect(authServiceSpy.refresh).not.toHaveBeenCalled();
  });


});
