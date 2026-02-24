import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { provideAnimations } from '@angular/platform-browser/animations';

/**
 * Configuración global de la aplicación Angular.
 *
 * Aquí se registran todos los providers necesarios:
 * - Router: Para navegación entre páginas
 * - HttpClient: Para hacer peticiones HTTP
 * - Animations: Para animaciones de Angular Material
 *
 * Es el equivalente a un @Configuration en Spring Boot.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    // Detección de cambios optimizada para mejorar rendimiento
    provideZoneChangeDetection({ eventCoalescing: true }),
    // Router con las rutas de la app
    provideRouter(routes),
    // HttpClient para peticiones HTTP
    // withInterceptors: Aquí añadiremos el JWT interceptor después
    provideHttpClient(withInterceptors([jwtInterceptor])),
    provideAnimations(),
  ],
};
