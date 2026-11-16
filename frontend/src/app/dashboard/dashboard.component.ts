
import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  OnInit,
  ChangeDetectorRef,
} from '@angular/core';
import { forkJoin, finalize } from 'rxjs';

import { ClientService } from '../services/client.service';
import { ShootingService } from '../services/shooting.service';
import { QuoteService } from '../services/quote.service';

import { Client } from '../models/client.model';
import { Shooting } from '../models/shooting.model';
import { Quote } from '../models/quote.model';

interface MonthlyShooting {
  label: string;
  count: number;
  percentage: number;
}

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
  private readonly quoteService = inject(QuoteService);
  private readonly cdr = inject(ChangeDetectorRef);

  clients: Client[] = [];
  shootings: Shooting[] = [];
  quotes: Quote[] = [];

  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.cdr.detectChanges();

    forkJoin({
      clients: this.clientService.getAll(),
      shootings: this.shootingService.getAll(),
      quotes: this.quoteService.getAll(),
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: ({ clients, shootings, quotes }) => {
          this.clients = clients;
          this.shootings = shootings;
          this.quotes = quotes;

          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('DASHBOARD LOAD ERROR:', error);

          this.errorMessage =
            'Impossibile caricare i dati della Dashboard. Riprova più tardi.';

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
          new Date(a.startAt).getTime() -
          new Date(b.startAt).getTime(),
      )
      .slice(0, 5);
  }

  get pendingQuotes(): number {
    return this.quotes.filter(
      (quote) => quote.status === 'SENT',
    ).length;
  }

  get acceptedQuotes(): number {
    return this.quotes.filter(
      (quote) => quote.status === 'ACCEPTED',
    ).length;
  }

  get acceptedQuotesTotal(): number {
    return this.quotes
      .filter((quote) => quote.status === 'ACCEPTED')
      .reduce((total, quote) => total + quote.totalAmount, 0);
  }

  get monthlyChartData(): MonthlyShooting[] {
    const now = new Date();

    const months: {
      label: string;
      year: number;
      month: number;
      count: number;
    }[] = [];

    for (let i = 5; i >= 0; i--) {
      const date = new Date(
        now.getFullYear(),
        now.getMonth() - i,
        1,
      );

      months.push({
        label: date.toLocaleDateString('it-IT', {
          month: 'short',
        }),
        year: date.getFullYear(),
        month: date.getMonth(),
        count: 0,
      });
    }

    this.shootings
      .filter((shooting) => shooting.status !== 'CANCELLED')
      .forEach((shooting) => {
        const date = new Date(shooting.startAt);

        const month = months.find(
          (item) =>
            item.month === date.getMonth() &&
            item.year === date.getFullYear(),
        );

        if (month) {
          month.count++;
        }
      });

    const maxCount = Math.max(
      ...months.map((month) => month.count),
      1,
    );

    return months.map((month) => ({
      label: month.label,
      count: month.count,
      percentage: (month.count / maxCount) * 100,
    }));
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

  formatCurrency(amount: number): string {
    return amount.toLocaleString('it-IT', {
      style: 'currency',
      currency: 'EUR',
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
