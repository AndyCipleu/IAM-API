import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor JWT.
 *
 * Añade automáticamente el token JWT en el header Authorization
 * de TODAS las peticiones HTTP (excepto login/register).
 *
 * Flujo:
 * 1. Angular hace petición HTTP
 * 2. Este interceptor se ejecuta ANTES de enviarla
 * 3. Obtiene el token del AuthService
 * 4. Clona la petición añadiendo el header Authorization
 * 5. Continúa con la petición modificada
 *
 * Es el equivalente a JwtAuthenticationFilter en Spring Boot,
 * pero en el lado del cliente.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  // Inyectar el AuthService para obtener el token
  const authService = inject(AuthService);

  // URLs públicas que NO necesitan token
  const publicUrls = ['/api/auth/login', '/api/auth/register'];

  // Si la URL es pública, no añadir token
  const isPublicUrl = publicUrls.some((url) => req.url.includes(url));

  if (isPublicUrl) {
    return next(req);
  }

  // Obtener el token del AuthService
  const token = authService.getAccessToken();

  // Si no hay token(usuario no autenticado), continuar sin modificar la petición
  if (!token) {
    return next(req);
  }

  //Clonar la petición añadiendo el header Authorization con el token JWT
  // Los headers de Angular son inmutables, por eso hay que clonarla
  const clonedReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(clonedReq);
};
