/**
 * Request de login.
 *
 * Datos que enviamos al backend en POST /api/auth/login
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Request de registro.
 *
 * Datos que enviamos al backend en POST /api/auth/register
 */
export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

/**
 * Response de autenticación.
 *
 * Datos que el backend devuelve después de login o register exitoso.
 */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: {
    id?: string;
    email: string;
    firstName: string;
    lastName: string;
    enabled: boolean;
    role?: string;
    createdAt?: string; // ISO 8601
  };
}

/**
 * Usuario autenticado guardado en localStorage.
 *
 * Combinación de los datos del usuario + tokens.
 */
export interface AuthUser {
  user: {
    id?: string;
    email: string;
    firstName: string;
    lastName: string;
    enabled: boolean;
    role?: string;
    createdAt?: string; // ISO 8601
  };
  tokens: {
    accessToken: string;
    refreshToken: string;
  };
}
