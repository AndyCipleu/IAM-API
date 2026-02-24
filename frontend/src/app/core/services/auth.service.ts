import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RegisterRequest,
} from '../models/auth.model';
import { Observable, tap } from 'rxjs';

/**
 * Servicio de autenticación.
 *
 * Gestiona el login, register, logout y almacenamiento de tokens.
 * Es un servicio singleton (una sola instancia en toda la app).
 *
 * @Injectable({ providedIn: 'root' })
 * - 'root' significa que es singleton global
 * - Angular lo crea automáticamente cuando alguien lo inyecta
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // Inyección de dependencias (como @Autowired en Spring)
  private http = inject(HttpClient);
  private router = inject(Router);

  // URL base de la API (gracias al proxy, no necesitamos localhost:8080)
  private readonly API_URL = '/api/auth';

  // Signal: Estado reactivo del usuario autenticado
  // Similar a un BehaviorSubject de RxJS pero más simple
  private currentUserSignal = signal<AuthUser | null>(
    this.loadUserFromStorage(),
  );

  // Exponer el signal como readonly (nadie puede modificarlo desde fuera)
  readonly currentUser = this.currentUserSignal.asReadonly();

  /**
   * Verifica si hay un usuario autenticado.
   *
   * @returns true si hay token válido
   */
  isAuthenticated(): boolean {
    return this.currentUserSignal()?.tokens.accessToken ? true : false;
  }

  /**
   * Obtiene el access token actual.
   *
   * @returns Token JWT o null si no está autenticado
   */
  getAccessToken(): string | null {
    return this.currentUserSignal()?.tokens.accessToken || null;
  }

  /**
   * Verifica si el usuario tiene un rol específico.
   *
   * @param role Nombre del rol (ej: 'ROLE_ADMIN')
   * @returns true si el usuario tiene el rol
   */
  hasRole(role: string): boolean {
    const user = this.currentUserSignal();

    return user ? user.user.role === role : false;
  }

  /**
   * Login de usuario.
   *
   * 1. Hace POST a /api/auth/login
   * 2. Si OK, guarda tokens + datos de usuario
   * 3. Redirige al dashboard
   *
   * @param credentials Email y password
   * @returns Observable con la respuesta
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(tap((response) => this.handleAuthSuccess(response)));
  }

  /**
   * Registro de nuevo usuario.
   *
   * 1. Hace POST a /api/auth/register
   * 2. Si OK, guarda tokens + datos de usuario
   * 3. Redirige al dashboard
   *
   * @param data Datos del nuevo usuario
   * @returns Observable con la respuesta
   */
  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/register`, data)
      .pipe(tap((response) => this.handleAuthSuccess(response)));
  }

  /**
   * Logout de usuario.
   *
   * 1. Hace POST a /api/auth/logout (backend invalida token)
   * 2. Limpia localStorage
   * 3. Redirige a login
   */
  logout(): void {
    const token = this.getAccessToken();

    if (token) {
      // Llamar al backend para invalidar el token
      this.http.post(`${this.API_URL}/logout`, { token }).subscribe({
        next: () => this.clearAuthData(),
        error: () => this.clearAuthData(), // En caso de error, igual limpiamos localStorage
      });
    } else {
      this.clearAuthData();
    }
  }

  /**
   * Maneja el éxito de login/register.
   *
   * 1. Toma los datos del usuario que vienen en la respuesta
   * 2. Guarda todo en localStorage
   * 3. Actualiza el signal (estado reactivo)
   * 4. Redirige al dashboard
   *
   * Nota: El backend ya envía los datos del usuario, no hay que
   * decodificar el JWT. Solo lo decodificamos si hace falta obtener
   * datos adicionales (como el ID) que no vienen en la respuesta.
   *
   * @param response Respuesta del backend con tokens y datos de usuario
   */
  private handleAuthSuccess(response: AuthResponse): void {
    const authUser: AuthUser = {
      user: {
        id: response.user.id,
        email: response.user.email,
        firstName: response.user.firstName,
        lastName: response.user.lastName,
        enabled: response.user.enabled,
        role: response.user.role,
        createdAt: response.user.createdAt,
      },
      tokens: {
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      },
    };

    // Guardar en localStorage
    localStorage.setItem('authUser', JSON.stringify(authUser));

    // Actualizar el signal
    this.currentUserSignal.set(authUser);

    // Redirigir al dashboard
    this.router.navigate(['/dashboard']);
  }

  /**
   * Limpia los datos de autenticación.
   *
   * 1. Borra de localStorage
   * 2. Resetea el signal a null
   * 3. Redirige a login
   */
  private clearAuthData(): void {
    localStorage.removeItem('authUser');
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Carga el usuario desde localStorage al iniciar la app.
   *
   * Se ejecuta cuando se crea el servicio (inicio de la app).
   *
   * @returns Usuario guardado o null
   */
  private loadUserFromStorage(): AuthUser | null {
    const stored = localStorage.getItem('authUser');

    if (!stored) return null;

    try {
      return JSON.parse(stored);
    } catch {
      // Si el JSON está corrupto, limpiar y retornar null
      localStorage.removeItem('authUser');
      return null;
    }
  }
}
