import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Auth Guard - Protege rutas privadas.
 *
 * Verifica si el usuario está autenticado antes de permitir
 * el acceso a una ruta.
 *
 * Flujo:
 * 1. Usuario intenta acceder a una ruta protegida (ej: /dashboard)
 * 2. Este guard se ejecuta ANTES de mostrar el componente
 * 3. Verifica si hay token válido
 * 4. Si SÍ hay token → Permite acceso (return true)
 * 5. Si NO hay token → Redirige a /login (return false)
 *
 * Equivalente a:
 * - Spring Security: @PreAuthorize, SecurityFilterChain
 * - Middleware en Express/NestJS
 *
 * @param route Información de la ruta a la que se intenta acceder
 * @param state Estado actual de la navegación
 * @returns true si puede acceder, false si no
 */
export const authGuard: CanActivateFn = (route, state) => {
  // Inyectar servicios necesarios
  const authService = inject(AuthService);
  const router = inject(Router);

  // Verificar si el usuario está autenticado
  if (authService.isAuthenticated()) return true;

  // Usuario NO autenticado → Redirigir a login
  console.warn('Access denied. Redirecting to login...');

  // Guardar la URL a la que intentó acceder
  // Después del login, podemos redirigir aquí
  const returnUrl = state.url;

  // Redirigir a login con la URL de retorno como query param
  router.navigate(['/login'], { queryParams: { returnUrl } });

  return false;
};
