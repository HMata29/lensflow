import { CalendarComponent } from './calendar/calendar.component';
import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShootingsComponent } from './shootings/shootings.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { QuotesComponent } from './quotes/quotes.component';
import { PaymentsComponent } from './payments/payments.component';
import { GalleriesComponent } from './galleries/galleries.component';

import { ClientService } from './services/client.service';
import { Client, ClientRequest } from './models/client.model';

interface PhotographerSettings {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  studioName: string;
  address: string;
  vatNumber: string;
  currency: string;
  dateFormat: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CalendarComponent,
    ShootingsComponent,
    DashboardComponent,
    QuotesComponent,
    PaymentsComponent,
    GalleriesComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})


export class App {

  // IMPOSTAZIONI PROFILO

private readonly settingsStorageKey = 'lensflow-settings';

settings: PhotographerSettings = this.loadSettings();

settingsSaved = false;

private defaultSettings(): PhotographerSettings {
  return {
    firstName: 'Marco',
    lastName: 'Bianchi',
    email: '',
    phone: '',
    studioName: '',
    address: '',
    vatNumber: '',
    currency: 'EUR',
    dateFormat: 'dd/MM/yyyy',
  };
}

private loadSettings(): PhotographerSettings {
  const defaults = this.defaultSettings();

  try {
    if (typeof window === 'undefined') {
      return defaults;
    }

    const saved = localStorage.getItem(this.settingsStorageKey);

    return saved
      ? { ...defaults, ...JSON.parse(saved) }
      : defaults;
  } catch {
    return defaults;
  }
}

saveSettings(): void {
  try {
    localStorage.setItem(
      this.settingsStorageKey,
      JSON.stringify(this.settings),
    );

    this.settingsSaved = true;
  } catch {
    this.settingsSaved = false;
  }
}

get photographerName(): string {
  const name = `${this.settings.firstName} ${this.settings.lastName}`.trim();

  return name || 'Fotografo';
}

get photographerInitial(): string {
  return this.settings.firstName.trim().charAt(0).toUpperCase() || 'F';
}

get studioRole(): string {
  return this.settings.studioName.trim() || 'Fotografo freelance';
}


  private readonly clientService = inject(ClientService);
  private readonly cdr = inject(ChangeDetectorRef);

  clients: Client[] = [];
  loadingClients = false;
  savingClient = false;
  clientError = '';
  clientSuccess = '';

  showClientForm = false;
  editingClientId: number | null = null;

  clientForm: ClientRequest = this.emptyClientForm();

  private emptyClientForm(): ClientRequest {
    return {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      company: '',
      notes: '',
    };
  }

  loadClients(): void {
    this.loadingClients = true;
    this.clientError = '';

    this.clientService.getAll().subscribe({
      next: (clients) => {
        console.log('CLIENTS NEXT:', clients);

        this.clients = clients;
        this.loadingClients = false;

        console.log('LOADING CLIENTS:', this.loadingClients);

        this.cdr.detectChanges();
      },
      error: () => {
        this.clientError =
          'Impossibile caricare i clienti. Verifica che il backend sia avviato.';
        this.loadingClients = false;
        this.cdr.detectChanges();
      },
    });
  }

  openCreateClientForm(): void {
    this.editingClientId = null;
    this.clientForm = this.emptyClientForm();
    this.clientError = '';
    this.clientSuccess = '';
    this.showClientForm = true;
  }

  openEditClientForm(client: Client): void {
    this.editingClientId = client.id;

    this.clientForm = {
      firstName: client.firstName,
      lastName: client.lastName,
      email: client.email ?? '',
      phone: client.phone ?? '',
      company: client.company ?? '',
      notes: client.notes ?? '',
    };

    this.clientError = '';
    this.clientSuccess = '';
    this.showClientForm = true;
  }

  cancelClientForm(): void {
    this.showClientForm = false;
    this.editingClientId = null;
    this.clientForm = this.emptyClientForm();
  }

  saveClient(): void {
    if (this.savingClient) {
      return;
    }

    this.savingClient = true;
    this.clientError = '';
    this.clientSuccess = '';

    const request: ClientRequest = {
      firstName: this.clientForm.firstName.trim(),
      lastName: this.clientForm.lastName.trim(),
      email: this.clientForm.email.trim(),
      phone: this.clientForm.phone.trim(),
      company: this.clientForm.company.trim(),
      notes: this.clientForm.notes.trim(),
    };

    const operation =
      this.editingClientId === null
        ? this.clientService.create(request)
        : this.clientService.update(this.editingClientId, request);

    operation.subscribe({
      next: (response) => {
        console.log('SAVE NEXT ESEGUITO:', response);

        const wasEditing = this.editingClientId !== null;

        this.savingClient = false;
        this.showClientForm = false;
        this.editingClientId = null;
        this.clientForm = this.emptyClientForm();

        this.clientSuccess = wasEditing
          ? 'Cliente aggiornato correttamente.'
          : 'Cliente salvato correttamente.';

        this.loadClients();

        this.cdr.detectChanges();
      },
      error: (error) => {
        this.savingClient = false;
        console.error('SAVE ERROR:', error);

        if (error.status === 400 && error.error) {
          const messages = Object.values(error.error) as string[];
          this.clientError = messages.join(' ');
        } else {
          this.clientError =
            'Errore durante il salvataggio del cliente.';
        }
        this.cdr.detectChanges();
      },
    });
  }

  deleteClient(client: Client): void {
    const confirmed = window.confirm(
      `Vuoi eliminare il cliente ${client.firstName} ${client.lastName}?`,
    );

    if (!confirmed) {
      return;
    }

    this.clientError = '';
    this.clientSuccess = '';

    this.clientService.delete(client.id).subscribe({
      next: () => {
        this.clients = this.clients.filter(
          (item) => item.id !== client.id,
        );

        this.clientSuccess = 'Cliente eliminato correttamente.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.clientError =
          'Errore durante l’eliminazione del cliente.';
          this.cdr.detectChanges();
      },
    });
  }

  activePage = 'Dashboard';

  readonly navigation = [
    { label: 'Dashboard', icon: '◫' },
    { label: 'Calendario', icon: '▦' },
    { label: 'Shooting', icon: '◎' },
    { label: 'Clienti', icon: '♙' },
    { label: 'Preventivi', icon: '▤' },
    { label: 'Pagamenti', icon: '€' },
    { label: 'Gallerie', icon: '▧' },
  ];

  readonly stats = [
    {
      label: 'Shooting programmati',
      value: '08',
      detail: '+2 questa settimana',
      icon: '▦',
      color: 'gold',
    },
    {
      label: 'Nuovi clienti',
      value: '04',
      detail: '+12% questo mese',
      icon: '♙',
      color: 'green',
    },
    {
      label: 'Preventivi in attesa',
      value: '03',
      detail: '€ 4.200 totali',
      icon: '▤',
      color: 'purple',
    },
    {
      label: 'Pagamenti da ricevere',
      value: '02',
      detail: '€ 2.350 totali',
      icon: '▱',
      color: 'blue',
    },
  ];

  readonly upcomingShoots = [
    {
      day: '24',
      month: 'GEN',
      weekday: 'SAB',
      title: 'Matrimonio — Sara & Luca',
      location: 'Villa Castelbarco, Milano',
      time: '10:00 — 18:00',
      status: 'Confermato',
      statusClass: 'confirmed',
      image: 'wedding',
    },
    {
      day: '29',
      month: 'GEN',
      weekday: 'GIO',
      title: 'Servizio ritratti — Giulia R.',
      location: 'Studio fotografico, Milano',
      time: '14:00 — 16:00',
      status: 'Confermato',
      statusClass: 'confirmed',
      image: 'portrait',
    },
    {
      day: '01',
      month: 'FEB',
      weekday: 'DOM',
      title: 'Evento aziendale — TechWave',
      location: 'MiCo, Milano',
      time: '17:00 — 22:00',
      status: 'In preparazione',
      statusClass: 'pending',
      image: 'event',
    },
  ];

  readonly activities = [
    {
      icon: '▤',
      title: 'Nuovo preventivo creato',
      description: 'Campagna commerciale — #PF-013',
      time: '2 ore fa',
      color: 'gold',
    },
    {
      icon: '♙',
      title: 'Nuovo cliente aggiunto',
      description: 'Luca Moretti',
      time: '5 ore fa',
      color: 'blue',
    },
    {
      icon: '▧',
      title: 'Galleria consegnata',
      description: 'Servizio ritratti — Anna B.',
      time: 'Ieri',
      color: 'purple',
    },
  ];

  selectPage(page: string): void {
  this.activePage = page;

  if (page === 'Clienti') {
    this.loadClients();
  }
}
}
