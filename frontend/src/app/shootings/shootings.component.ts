
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Client } from '../models/client.model';
import {
  Shooting,
  ShootingRequest,
  ShootingStatus,
} from '../models/shooting.model';

import { ClientService } from '../services/client.service';
import { ShootingService } from '../services/shooting.service';
import { SHOOTING_CATEGORIES } from '../config/shooting-categories';

@Component({
  selector: 'app-shootings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shootings.component.html',
  styleUrl: './shootings.component.scss',
})
export class ShootingsComponent implements OnInit {
  private readonly shootingService = inject(ShootingService);
  private readonly clientService = inject(ClientService);
  private readonly cdr = inject(ChangeDetectorRef);

  shootings: Shooting[] = [];
  clients: Client[] = [];

  loadingShootings = false;
  savingShooting = false;
  showForm = false;

  editingShootingId: number | null = null;

  errorMessage = '';
  successMessage = '';

  readonly categories = SHOOTING_CATEGORIES;

  readonly statuses: { value: ShootingStatus; label: string }[] = [
    { value: 'PLANNED', label: 'Pianificato' },
    { value: 'CONFIRMED', label: 'Confermato' },
    { value: 'COMPLETED', label: 'Completato' },
    { value: 'CANCELLED', label: 'Annullato' },
  ];

  readonly timeOptions: string[] = this.generateTimeOptions();

  shootingForm: ShootingRequest = this.emptyForm();

  startDate = '';
  startTime = '';
  endDate = '';
  endTime = '';

  ngOnInit(): void {
    this.loadClients();
    this.loadShootings();
  }

  private emptyForm(): ShootingRequest {
    return {
      title: '',
      category: '',
      startAt: '',
      endAt: '',
      location: '',
      status: 'PLANNED',
      notes: '',
      clientId: 0,
    };
  }

  private generateTimeOptions(): string[] {
    const options: string[] = [];

    for (let hour = 0; hour < 24; hour++) {
      for (const minute of [0, 30]) {
        const formattedHour = String(hour).padStart(2, '0');
        const formattedMinute = String(minute).padStart(2, '0');

        options.push(`${formattedHour}:${formattedMinute}`);
      }
    }

    return options;
  }

  loadClients(): void {
    this.clientService.getAll().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossibile caricare i clienti.';
        this.cdr.detectChanges();
      },
    });
  }

  loadShootings(): void {
    this.loadingShootings = true;
    this.errorMessage = '';

    this.shootingService.getAll().subscribe({
      next: (shootings) => {
        this.shootings = [...shootings].sort(
          (a, b) =>
            new Date(a.startAt).getTime() - new Date(b.startAt).getTime(),
        );

        this.loadingShootings = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage =
          'Impossibile caricare gli shooting. Verifica che il backend sia avviato.';
        this.loadingShootings = false;
        this.cdr.detectChanges();
      },
    });
  }

  openCreateForm(): void {
    this.editingShootingId = null;
    this.shootingForm = this.emptyForm();

    this.startDate = '';
    this.startTime = '';
    this.endDate = '';
    this.endTime = '';

    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  openEditForm(shooting: Shooting): void {
    this.editingShootingId = shooting.id;

    this.shootingForm = {
      title: shooting.title,
      category: shooting.category,
      startAt: shooting.startAt,
      endAt: shooting.endAt,
      location: shooting.location ?? '',
      status: shooting.status,
      notes: shooting.notes ?? '',
      clientId: shooting.clientId,
    };

    const start = this.toDateTimeLocal(shooting.startAt);
    const end = this.toDateTimeLocal(shooting.endAt);

    this.startDate = start.split('T')[0];
    this.startTime = start.split('T')[1];
    this.endDate = end.split('T')[0];
    this.endTime = end.split('T')[1];

    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingShootingId = null;
    this.shootingForm = this.emptyForm();

    this.startDate = '';
    this.startTime = '';
    this.endDate = '';
    this.endTime = '';
  }

  saveShooting(): void {
    if (this.savingShooting) {
      return;
    }

    if (!this.shootingForm.clientId) {
      this.errorMessage = 'Seleziona un cliente.';
      return;
    }

    if (
      !this.startDate ||
      !this.startTime ||
      !this.endDate ||
      !this.endTime
    ) {
      this.errorMessage = 'Inserisci data e orario di inizio e fine.';
      return;
    }

    const startAt = `${this.startDate}T${this.startTime}`;
    const endAt = `${this.endDate}T${this.endTime}`;

    if (
      new Date(endAt).getTime() <= new Date(startAt).getTime()
    ) {
      this.errorMessage =
        'La data e l’ora di fine devono essere successive all’inizio.';
      return;
    }

    this.savingShooting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: ShootingRequest = {
      title: this.shootingForm.title.trim(),
      category: this.shootingForm.category.trim(),
      startAt,
      endAt,
      location: this.shootingForm.location.trim(),
      status: this.shootingForm.status,
      notes: this.shootingForm.notes.trim(),
      clientId: Number(this.shootingForm.clientId),
    };

    const operation =
      this.editingShootingId === null
        ? this.shootingService.create(request)
        : this.shootingService.update(this.editingShootingId, request);

    operation.subscribe({
      next: () => {
        const wasEditing = this.editingShootingId !== null;

        this.savingShooting = false;
        this.showForm = false;
        this.editingShootingId = null;
        this.shootingForm = this.emptyForm();

        this.startDate = '';
        this.startTime = '';
        this.endDate = '';
        this.endTime = '';

        this.successMessage = wasEditing
          ? 'Shooting aggiornato correttamente.'
          : 'Shooting creato correttamente.';

        this.loadShootings();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.savingShooting = false;

        if (error.status === 400 && error.error) {
          const messages = Object.values(error.error) as string[];
          this.errorMessage = messages.join(' ');
        } else {
          this.errorMessage =
            'Errore durante il salvataggio dello shooting.';
        }

        this.cdr.detectChanges();
      },
    });
  }

  deleteShooting(shooting: Shooting): void {
    const confirmed = window.confirm(
      `Vuoi eliminare lo shooting "${shooting.title}"?`,
    );

    if (!confirmed) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.shootingService.delete(shooting.id).subscribe({
      next: () => {
        this.shootings = this.shootings.filter(
          (item) => item.id !== shooting.id,
        );

        this.successMessage = 'Shooting eliminato correttamente.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage =
          'Errore durante l’eliminazione dello shooting.';
        this.cdr.detectChanges();
      },
    });
  }

  formatDate(dateTime: string): string {
    return new Date(dateTime).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatTime(dateTime: string): string {
    return new Date(dateTime).toLocaleTimeString('it-IT', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  getStatusLabel(status: ShootingStatus): string {
    return this.statuses.find((item) => item.value === status)?.label ?? status;
  }

  private toDateTimeLocal(value: string): string {
    const date = new Date(value);

    const pad = (number: number): string => String(number).padStart(2, '0');

    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }
}
