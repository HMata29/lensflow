
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  activePage = 'Dashboard';

  readonly navigation = [
    { label: 'Dashboard', icon: '◫' },
    { label: 'Calendario', icon: '▦' },
    { label: 'Shooting', icon: '◎' },
    { label: 'Clienti', icon: '♙' },
    { label: 'Preventivi', icon: '▤' },
    { label: 'Gallerie', icon: '▧' },
    { label: 'Statistiche', icon: '▥' },
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
  }
}
