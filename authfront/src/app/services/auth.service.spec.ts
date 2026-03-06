import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const basicUrl = 'http://${environment.apiUrl}:8080/auth';


  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() =>{
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  //register test
  it('doit créer un nouveau utilisateur et renvoie un message de confirmation', () =>{
    const newUser = {username: 'testuser', email: 'test@gmail.com', password: 'password123'};
    const mockResponse = 'User registered successfully';

    service.register(newUser).subscribe( (response) =>{
      expect(response).toBe(mockResponse);
      expect(typeof response).toBe('string');
    });

    const req = httpMock.expectOne(`${basicUrl}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newUser);

    req.flush(mockResponse);
  });


  //login test
  it('doit connecter les utilisateurs et renvoie un accessToken et un refreshToken', ()=>{
    const crededntials = {email: 'test@gmail.com', password: 'password123'};
    const mockResponse = {accessToken: 'fakeAccessToken', refreshToken: 'fakeRefreshToken'};

    service.login(crededntials).subscribe( (response) => {
      expect(response).not.toBeNull();
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${basicUrl}/login`);
    expect(req.request.body).toEqual(crededntials);
    expect(req.request.method).toBe('POST');

    req.flush(mockResponse);
  });

  it('doit sauvegarder les tokens dans le localStorage récupère ce qui a été sauvegardé', () =>{

    const accessToken = 'fakeAccessToken';
    const refresh = 'fakeRefreshToken';

    service.saveTokens(accessToken, refresh);

    expect(service.getAccessToken()).toBe(accessToken);
    expect(service.getRefreshToken()).toBe(refresh);

  });

  it('doit effectuer le logout et supprimer les tokens du localStorage', () =>{

    const accessToken = 'fakeAccessToken';
    const refresh = 'fakeRefreshToken';
    const mockResponse = 'Logout successful';

    service.saveTokens(accessToken, refresh);

    expect(service.getAccessToken()).toBe(accessToken);
    expect(service.getRefreshToken()).toBe(refresh);

    service.logout(refresh).subscribe((response)=>{
      expect(response).toBe(mockResponse);
    });

    const req = httpMock.expectOne(`${basicUrl}/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({refreshToken: refresh});
    req.flush(mockResponse);
  });

  it('doit chercher les informations de l\'utilisateur connecté via le token', () =>{
    const mockUser = {id: 1, username: 'testuser', email: 'test@gmail.com'};

    service.me().subscribe( (response) => {
      expect(response).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${basicUrl}/me`);
    expect(req.request.method).toBe('GET');

    req.flush(mockUser);
  });

  it('doit rafraîchir les tokens en utilisant le refreshToken', () =>{
    const refreshToken = 'fakeRefreshToken';
    const mockResponse = {accessToken: 'newFakeAccessToken', refreshToken: 'newFakeRefreshToken'};

    service.refresh(refreshToken).subscribe((response)=>{
      expect(response).not.toBeNull();
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${basicUrl}/refresh`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({refreshToken: refreshToken});
    req.flush(mockResponse);

  });

});
