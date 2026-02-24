import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';

import { AuthService } from '../../core/services/auth.service';

/**
 * Componente Dashboard.
 *
 * Página principal después del login.
 *
 * Muestra:
 * - Bienvenida al usuario (nombre)
 * - Información del usuario (email, roles)
 * - Opciones de navegación
 * - Botón de logout
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  private router = inject(Router);

  // Obtener el usuario actual del AuthService
  currentUser = this.authService.currentUser;

  ngOnInit(): void {
    console.log('Dashboard loaded');
    console.log('Current user:', this.currentUser());
  }

  /**
   * Navegar a la lista de usuarios.
   */
  goToUsers(): void {
    this.router.navigate(['/users']);
  }

  /**
   * Cerrar sesión.
   */
  logout(): void {
    console.log('Logging out...');
    this.authService.logout();
  }
}
