import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.model';
/**
 * Componente de Login.
 *
 * Permite a los usuarios autenticarse en el sistema.
 *
 * Flujo:
 * 1. Usuario llena email y password
 * 2. Click en "Login"
 * 3. Validación del formulario (email válido, campos requeridos)
 * 4. Llamada a AuthService.login()
 * 5. Si OK → Redirige a /dashboard (AuthService lo hace automáticamente)
 * 6. Si Error → Muestra mensaje de error
 *
 * Features:
 * - Validación en tiempo real (email válido, campos requeridos)
 * - Mostrar/ocultar password
 * - Loading spinner durante la petición
 * - Mensajes de error amigables
 * - Link a registro
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  // Inyección de dependencias
  private authService = inject(AuthService);
  private formBuilder = inject(FormBuilder);
  private router = inject(Router);

  // Estado reactivo
  isLoading = signal(false); // Muestra el spinner durante la petición
  errorMessage = signal<string | null>(null); // Mensaje de error si falla el login
  hidePassword = signal(true); // Mostrar/ocultar password

  // Formulario reactivo
  loginForm: FormGroup = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  constructor() {
    // Si el usuario ya está autenticado, redirigir al dashboard
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  /**
   * Maneja el submit del formulario.
   *
   * Flujo:
   * 1. Verifica que el formulario sea válido
   * 2. Activa el loading spinner
   * 3. Llama al AuthService.login()
   * 4. Si OK → AuthService redirige automáticamente a /dashboard
   * 5. Si Error → Muestra mensaje de error
   */
  onSubmit(): void {
    // Limpiar mensaje de error previo
    this.errorMessage.set(null);

    // Validar el formulario
    if (this.loginForm.invalid) {
      // Marcar todos los campos como touched para mostrar errores
      this.loginForm.markAllAsTouched();
      return;
    }

    // Activar spinner
    this.isLoading.set(true);

    // Preparar datos de login
    const loginData: LoginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password,
    };

    // Llamar al AuthService
    this.authService.login(loginData).subscribe({
      next: () => {
        // Login exitoso
        // El AuthService ya redirige a /dashboard automáticamente
        this.isLoading.set(false);
      },
      error: (error) => {
        // Login fallido
        this.isLoading.set(false);

        // Mostrar mensaje de error amigable
        if (error.status === 401) {
          this.errorMessage.set('Email o contraseña incorrectos');
        } else if (error.status === 429) {
          this.errorMessage.set(
            'Demasiados intentos. Intenta de nuevo más tarde',
          );
        } else if (error.status === 0) {
          this.errorMessage.set('No se puede conectar con el servidor');
        } else {
          this.errorMessage.set('Error al iniciar sesión. Intenta de nuevo');
        }

        console.error('Login error:', error);
      },
    });
  }

  /**
   * Alterna la visibilidad del password.
   */
  togglePasswordVisibility(): void {
    this.hidePassword.set(!this.hidePassword());
  }

  /**
   * Verifica si un campo tiene errores y ha sido tocado.
   *
   * Se usa en el template para mostrar mensajes de error.
   */
  hasError(field: string, error: string): boolean {
    const control = this.loginForm.get(field);
    return control ? control.hasError(error) && control.touched : false;
  }

  /**
   * Obtiene el mensaje de error para un campo.
   */
  getErrorMessage(field: string): string {
    const control = this.loginForm.get(field);

    if (!control) return '';

    if (control.hasError('required')) {
      return `${field === 'email' ? 'Email' : 'Contraseña'} es requerido`;
    }

    if (control.hasError('email')) {
      return 'Email inválido';
    }

    if (control.hasError('minlength')) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }

    return '';
  }
}
