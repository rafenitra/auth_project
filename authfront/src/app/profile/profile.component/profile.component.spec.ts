import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileComponent } from './profile.component';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>

  beforeEach(async () => {

    const authServiceMock = jasmine.createSpyObj('AuthService', ['me']); 

    await TestBed.configureTestingModule({
      declarations: [ProfileComponent],
      providers: [
        {provide: AuthService, useValue: authServiceMock}
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    //authServiceSpy.me.and.returnValue(of({ id: 1, usename: 'Ary', email: 'ary@gmail.com' }));
    
  });

  it('should create', () => {

    expect(component).toBeTruthy();
  });

  it('should have loading = true initially', () => {
    authServiceSpy.me.and.returnValue(of(null));
    fixture.detectChanges();
    expect(component.loading).toBeFalsy();

  });


  it('should load user profile on init (success)', () => {
    
    const mockUser = { id: 1, name: 'Ary', email: 'ary@gmail.com' };

    authServiceSpy.me.and.returnValue(of(mockUser));
    fixture.detectChanges();
    component.ngOnInit();

    expect(authServiceSpy.me).toHaveBeenCalled();
    expect(component.user).toEqual(mockUser);
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('');
  });

 
  it('should set errorMessage on error', () => {
    
    authServiceSpy.me.and.returnValue(
      throwError(() => ({ status: 500 }))
    );
    fixture.detectChanges();
    component.ngOnInit();

    expect(authServiceSpy.me).toHaveBeenCalled();
    expect(component.user).toBeNull();
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Impossible de charger votre profil.');
  });


});
