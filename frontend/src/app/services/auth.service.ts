
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, switchMap, tap } from 'rxjs';

export interface AuthResponse {
  email: string;
  authorities: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegistrationRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  studioName: string;
}

export interface RegistrationResponse {
  email: string;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/auth';

  private readonly userSubject =
    new BehaviorSubject<AuthResponse | null>(null);

  readonly user$ = this.userSubject.asObservable();

  get currentUser(): AuthResponse | null {
    return this.userSubject.value;
  }

  private prepareCsrf(): Observable<void> {
    return this.http.get<void>(`${this.apiUrl}/csrf`, {
      withCredentials: true,
    });
  }

  restoreSession(): Observable<AuthResponse> {
    return this.http
      .get<AuthResponse>(`${this.apiUrl}/me`, {
        withCredentials: true,
      })
      .pipe(
        tap((user) => this.userSubject.next(user)),
      );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.prepareCsrf().pipe(
      switchMap(() =>
        this.http.post<AuthResponse>(
          `${this.apiUrl}/login`,
          request,
          { withCredentials: true },
        ),
      ),
      tap((user) => this.userSubject.next(user)),
    );
  }

  register(
    request: RegistrationRequest,
  ): Observable<RegistrationResponse> {
    return this.prepareCsrf().pipe(
      switchMap(() =>
        this.http.post<RegistrationResponse>(
          `${this.apiUrl}/register`,
          request,
          { withCredentials: true },
        ),
      ),
    );
  }

  logout(): Observable<void> {
    return this.prepareCsrf().pipe(
      switchMap(() =>
        this.http.post<void>(
          `${this.apiUrl}/logout`,
          {},
          { withCredentials: true },
        ),
      ),
      tap(() => this.userSubject.next(null)),
    );
  }

  clearSession(): void {
    this.userSubject.next(null);
  }
}
