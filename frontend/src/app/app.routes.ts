import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Ruta por defecto: redirige a /dashboard
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full',
  },

  // ===== RUTAS PÚBLICAS (sin autenticación) =====
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent,
      ),
    title: 'Login - IAM',
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(
        (m) => m.RegisterComponent,
      ),
    title: 'Register - IAM',
  },

  // ===== RUTAS PRIVADAS (requieren autenticación) =====
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(
        (m) => m.DashboardComponent,
      ),
    canActivate: [authGuard], // ← Protegida por el guard
    title: 'Dashboard - IAM',
  },
  // {
  //   path: 'users',
  //   loadComponent: () =>
  //     import('./features/users/user-list/user-list.component').then(
  //       (m) => m.UserListComponent,
  //     ),
  //   canActivate: [authGuard], // ← Protegida por el guard
  //   title: 'Users - IAM',
  // },

  // ===== RUTA 404 (cualquier ruta no definida) =====
  {
    path: '**',
    redirectTo: '/dashboard',
  },
];
