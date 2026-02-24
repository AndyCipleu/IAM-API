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
import { RegisterRequest } from '../../../core/models/auth.model';

/**
 * Componente de Registro.
 *
 * Permite a nuevos usuarios crear una cuenta en el sistema.
 *
 * Flujo:
 * 1. Usuario llena: email, password, firstName, lastName
 * 2. Click en "Registrarse"
 * 3. Validaciones (email válido, password mínimo 8 chars, campos requeridos)
 * 4. Llamada a AuthService.register()
 * 5. Si OK → Redirige a /dashboard (AuthService lo hace automáticamente)
 * 6. Si Error → Muestra mensaje de error
 *
 * Features:
 * - Validación en tiempo real
 * - Mostrar/ocultar password
 * - Loading spinner durante la petición
 * - Mensajes de error amigables
 * - Link a login
 */
@Component({
  selector: 'app-register',
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
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private formBuilder = inject(FormBuilder);
  private router = inject(Router);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  hidePassword = signal(true);

  // Formulario Reactivo
  registerForm: FormGroup = this.formBuilder.group({
    firstName: [
      '',
      [Validators.required, Validators.minLength(2), Validators.maxLength(50)],
    ],
    lastName: [
      '',
      [Validators.required, Validators.minLength(2), Validators.maxLength(50)],
    ],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  constructor() {
    this.errorMessage.set(null);

    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  /**
   * Maneja el submit del formulario.
   */
  onSubmit(): void {
    this.errorMessage.set(null);

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const registerData: RegisterRequest = {
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      firstName: this.registerForm.value.firstName,
      lastName: this.registerForm.value.lastName,
    };

    this.authService.register(registerData).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.errorMessage.set(null);
      },
      error: (error) => {
        this.isLoading.set(false);
        if (error.status === 409) {
          this.errorMessage.set('Este email ya está registrado');
        } else if (error.status === 429) {
          this.errorMessage.set(
            'Demasiados intentos. Intenta de nuevo más tarde',
          );
        } else if (error.status === 0) {
          this.errorMessage.set('No se puede conectar con el servidor');
        } else if (error.status === 400) {
          this.errorMessage.set('Datos inválidos. Verifica el formulario');
        } else {
          this.errorMessage.set('Error al registrarse. Intenta de nuevo');
        }
      },
    });
  }

  /**
   * Alterna la visibilidad del password.
   */
  togglePasswordVisibility() {
    this.hidePassword.set(!this.hidePassword());
  }

  /**
   * Verifica si un campo tiene errores y ha sido tocado.
   */
  hasError(field: string, error: string): boolean {
    const control = this.registerForm.get(field);
    return control ? control.hasError(error) && control.touched : false;
  }

  /**
   * Obtiene el mensaje de error para un campo.
   */
  getErrorMessage(field: string): string {
    const control = this.registerForm.get(field);

    if (!control) return '';

    if (control.hasError('requierd')) {
      const fieldNames: { [key: string]: string } = {
        email: 'Email',
        password: 'Contraseña',
        firstName: 'Nombre',
        lastName: 'Apellido',
      };

      return `${fieldNames[field]} es requerido`;
    }
    if (control.hasError('email')) {
      return 'Email inválido';
    }

    if (control.hasError('minlength')) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }

    if (control.hasError('maxlength')) {
      const maxLength = control.errors?.['maxlength'].requiredLength;
      return `Máximo ${maxLength} caracteres`;
    }

    return '';
  }
}
