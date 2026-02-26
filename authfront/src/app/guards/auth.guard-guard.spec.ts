import { TestBed } from '@angular/core/testing';
import { CanActivateFn, Router } from '@angular/router';

import { authGuardGuard } from './auth.guard-guard';
import { AuthService } from '../services/auth.service';
import { RouterTestingModule } from '@angular/router/testing';

describe('authGuardGuard', () => {

  let authSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;


  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => authGuardGuard(...guardParameters));

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['getAccessToken']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy }
      ]

    });
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('bloque et redirige vers le /login si pas de token ', ()=>{
    authSpy.getAccessToken.and.returnValue(null);

    const result = executeGuard(null as any, null as any);

    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('autorise la navigation si token présent', () => {
    authSpy.getAccessToken.and.returnValue('token-123');

    const result = executeGuard(null as any, null as any)
    
    expect(result).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });


});
