import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AccueilComponent } from './accueil.component';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('AccueilComponent', () => {
  let component: AccueilComponent;
  let fixture: ComponentFixture<AccueilComponent>;
  let authSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['logout']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [AccueilComponent],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccueilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem('refreshToken');
    authSpy.logout.calls.reset();
    routerSpy.navigate.calls.reset();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have showMenu false initially', () => {
    expect(component.showMenu).toBeFalse();
  });

  it('toggleMenu should toggle showMenu', () => {
    expect(component.showMenu).toBeFalse();
    component.toggleMenu();
    expect(component.showMenu).toBeTrue();
    component.toggleMenu();
    expect(component.showMenu).toBeFalse();
  });

  it('goToProfile should navigate to /profile', () => {
    component.goToProfile();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/profile']);
  });

  it('logout should call authService.logout with refreshToken from localStorage and navigate on non-empty response', fakeAsync(() => {
    // préparer localStorage
    localStorage.setItem('refreshToken', 'my-refresh-token');

    // mocker la réponse : tableau non vide
    authSpy.logout.and.returnValue(of('ok'));

    component.logout();
    // subscription synchrone ici (of emits synchronously)
    tick();

    expect(authSpy.logout).toHaveBeenCalledWith('my-refresh-token');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('logout should call authService.logout with empty string if no refreshToken in localStorage', fakeAsync(() => {
    // s'assurer qu'il n'y a pas de refreshToken
    localStorage.removeItem('refreshToken');

    authSpy.logout.and.returnValue(of('ok'));

    component.logout();
    tick();

    expect(authSpy.logout).toHaveBeenCalledWith('');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('logout should NOT navigate when response is empty', fakeAsync(() => {
    localStorage.setItem('refreshToken', 'token-2');

    // mocker réponse vide
    authSpy.logout.and.returnValue(of());

    component.logout();
    tick();

    expect(authSpy.logout).toHaveBeenCalledWith('token-2');
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  }));
});