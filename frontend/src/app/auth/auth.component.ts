
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  AuthResponse,
  AuthService,
} from '../services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.scss',
})
export class AuthComponent {
  private readonly authService = inject(AuthService);

  @Output() authenticated = new EventEmitter<AuthResponse>();

  mode: 'login' | 'register' = 'login';

  email = '';
  password = '';

  firstName = '';
  lastName = '';
  studioName = '';

  loading = false;
  errorMessage = '';
  successMessage = '';

  setMode(mode: 'login' | 'register'): void {
    this.mode = mode;
    this.errorMessage = '';
    this.successMessage = '';
  }

  submit(): void {
    if (this.loading) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    if (this.mode === 'login') {
      this.authService.login({
        email: this.email.trim(),
        password: this.password,
      }).subscribe({
        next: (user) => {
          this.loading = false;
          this.authenticated.emit(user);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage =
            error.status === 401
              ? 'Email o password non corrette.'
              : 'Impossibile effettuare il login. Riprova.';
        },
      });

      return;
    }

    this.authService.register({
      email: this.email.trim(),
      password: this.password,
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim(),
      studioName: this.studioName.trim(),
    }).subscribe({
      next: () => {
        this.loading = false;
        this.mode = 'login';
        this.password = '';
        this.successMessage =
          'Account creato correttamente. Ora puoi accedere.';
      },
      error: (error) => {
        this.loading = false;

        this.errorMessage =
          error.status === 409
            ? 'Esiste già un account con questa email.'
            : error.status === 400
              ? 'Controlla i dati inseriti e la password.'
              : 'Errore durante la registrazione.';
      },
    });
  }
}
