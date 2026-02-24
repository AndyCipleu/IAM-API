/**
 * Modelo de Usuario.
 *
 * Mapea el UserResponse que devuelve el backend.
 * Se usa para tipar los objetos User en toda la aplicación.
 */
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  role: string;
  createdAt: string; // ISO 8601
}

/**
 * Página de usuarios con metadatos de paginación.
 *
 * Mapea el PageResponse<UserResponse> del backend.
 */
export interface UserPage {
  content: User[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

/**
 * Filtros para búsqueda de usuarios.
 *
 * Todos los campos son opcionales.
 */
export interface UserFilters {
  email?: string;
  firstName?: string;
  lastName?: string;
  enabled?: boolean;
  role?: string;
  createdAt?: string; // ISO 8601
}
