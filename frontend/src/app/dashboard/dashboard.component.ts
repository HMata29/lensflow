
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';

import { ClientService } from '../services/client.service';
import { ShootingService } from '../services/shooting.service';
import { Client } from '../models/client.model';
import { Shooting } from '../models/shooting.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly shootingService = inject(ShootingService);
  private readonly cdr = inject(ChangeDetectorRef);

  clients: Client[] = [];
  shootings: Shooting[] = [];

  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.clientService.getAll().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossibile caricare i clienti.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });

    this.shootingService.getAll().subscribe({
      next: (shootings) => {
        this.shootings = shootings;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossibile caricare gli shooting.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get totalClients(): number {
    return this.clients.length;
  }

  get monthlyShootings(): number {
    const now = new Date();

    return this.shootings.filter((shooting) => {
      const date = new Date(shooting.startAt);

      return (
        date.getMonth() === now.getMonth() &&
        date.getFullYear() === now.getFullYear() &&
        shooting.status !== 'CANCELLED'
      );
    }).length;
  }

  get completedShootings(): number {
    return this.shootings.filter(
      (shooting) => shooting.status === 'COMPLETED',
    ).length;
  }

  get upcomingShootings(): Shooting[] {
    const now = new Date();

    return this.shootings
      .filter(
        (shooting) =>
          new Date(shooting.startAt) >= now &&
          shooting.status !== 'CANCELLED' &&
          shooting.status !== 'COMPLETED',
      )
      .sort(
        (a, b) =>
          new Date(a.startAt).getTime() - new Date(b.startAt).getTime(),
      )
      .slice(0, 5);
  }

  formatDate(dateTime: string): string {
    return new Date(dateTime).toLocaleDateString('it-IT', {
      weekday: 'short',
      day: '2-digit',
      month: 'short',
    });
  }

  formatTime(dateTime: string): string {
    return new Date(dateTime).toLocaleTimeString('it-IT', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  getStatusLabel(status: Shooting['status']): string {
    switch (status) {
      case 'PLANNED':
        return 'Pianificato';
      case 'CONFIRMED':
        return 'Confermato';
      case 'COMPLETED':
        return 'Completato';
      case 'CANCELLED':
        return 'Annullato';
      default:
        return status;
    }
  }
}
