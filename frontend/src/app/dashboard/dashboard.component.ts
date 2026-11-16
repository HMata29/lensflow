
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
import { PaymentService } from '../services/payment.service';

import { Client } from '../models/client.model';
import { Shooting } from '../models/shooting.model';
import { Quote } from '../models/quote.model';
import { Payment } from '../models/payment.model';

interface MonthlyShooting {
  label: string;
  count: number;
  percentage: number;
}

interface MonthlyIncome {
  label: string;
  amount: number;
  percentage: number;
}

type ActivityType = 'shooting' | 'quote' | 'payment' | 'gallery';

interface RecentActivity {
  id: string;
  type: ActivityType;
  title: string;
  description: string;
  date: string;
  timestamp: number;
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
  private readonly paymentService = inject(PaymentService);
  private readonly cdr = inject(ChangeDetectorRef);

  clients: Client[] = [];
  shootings: Shooting[] = [];
  quotes: Quote[] = [];
  payments: Payment[] = [];

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
      payments: this.paymentService.getAll(),
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: ({ clients, shootings, quotes, payments }) => {
          this.clients = clients;
          this.shootings = shootings;
          this.quotes = quotes;
          this.payments = payments;

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

  // STATISTICHE GENERALI

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


  // STATISTICHE GALLERIE

  get galleryShootings(): Shooting[] {
    return this.shootings.filter(
      (shooting) => shooting.status !== 'CANCELLED',
    );
  }

  get galleriesNotStarted(): number {
    return this.galleryShootings.filter(
      (shooting) => shooting.galleryStatus === 'NOT_STARTED',
    ).length;
  }

  get galleriesInProgress(): number {
    return this.galleryShootings.filter(
      (shooting) => shooting.galleryStatus === 'IN_PROGRESS',
    ).length;
  }

  get galleriesToSelect(): number {
    return this.galleryShootings.filter(
      (shooting) => shooting.galleryStatus === 'TO_SELECT',
    ).length;
  }

  get galleriesDelivered(): number {
    return this.galleryShootings.filter(
      (shooting) =>
        shooting.galleryStatus === 'DELIVERED' ||
        shooting.galleryStatus === 'ARCHIVED',
    ).length;
  }

  get archivedGalleries(): number {
    return this.galleryShootings.filter(
      (shooting) => shooting.galleryStatus === 'ARCHIVED',
    ).length;
  }

  get galleryProgressPercentage(): number {
    const total = this.galleryShootings.length;

    if (total === 0) {
      return 0;
    }

    return Math.round((this.galleriesDelivered / total) * 100);
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



  // ATTIVITÀ RECENTI

  get recentActivities(): RecentActivity[] {
    const activities: RecentActivity[] = [];

    const addActivity = (
      id: string,
      type: ActivityType,
      title: string,
      description: string,
      date: string | null | undefined,
    ): void => {
      if (!date) {
        return;
      }

      const timestamp = new Date(date).getTime();

      if (!Number.isFinite(timestamp)) {
        return;
      }

      activities.push({
        id,
        type,
        title,
        description,
        date,
        timestamp,
      });
    };

    // Nuovi shooting
    this.shootings.forEach((shooting) => {
      addActivity(
        `shooting-${shooting.id}`,
        'shooting',
        'Nuovo shooting',
        shooting.title,
        shooting.createdAt,
      );
    });

    // Preventivi accettati
    this.quotes.forEach((quote) => {
      if (quote.status === 'ACCEPTED') {
        addActivity(
          `quote-${quote.id}`,
          'quote',
          'Preventivo accettato',
          `${quote.quoteNumber} · ${quote.clientFirstName} ${quote.clientLastName}`,
          quote.acceptedAt,
        );
      }
    });

    // Pagamenti ricevuti
    this.payments.forEach((payment) => {
      if (payment.status === 'RECEIVED') {
        addActivity(
          `payment-${payment.id}`,
          'payment',
          'Pagamento ricevuto',
          `${payment.shootingTitle} · ${this.formatCurrency(payment.amount)}`,
          payment.paymentDate,
        );
      }
    });

    // Gallerie consegnate o archiviate
    this.shootings.forEach((shooting) => {
      if (
        shooting.galleryStatus === 'DELIVERED' ||
        shooting.galleryStatus === 'ARCHIVED'
      ) {
        addActivity(
          `gallery-${shooting.id}`,
          'gallery',
          shooting.galleryStatus === 'ARCHIVED'
            ? 'Galleria archiviata'
            : 'Galleria consegnata',
          shooting.title,
          shooting.galleryStatusUpdatedAt,
        );
      }
    });

    return activities
      .sort((a, b) => b.timestamp - a.timestamp)
      .slice(0, 10);
  }

  formatActivityDate(date: string): string {
    return new Date(date).toLocaleString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }


  // STATISTICHE FINANZIARIE

  get totalReceived(): number {
    return this.payments
      .filter((payment) => payment.status === 'RECEIVED')
      .reduce((total, payment) => total + payment.amount, 0);
  }

  get totalPending(): number {
    return this.payments
      .filter((payment) => payment.status === 'PENDING')
      .reduce((total, payment) => total + payment.amount, 0);
  }

  get totalRefunded(): number {
    return this.payments
      .filter((payment) => payment.status === 'REFUNDED')
      .reduce((total, payment) => total + payment.amount, 0);
  }

  get totalOutstanding(): number {
    const acceptedQuoteIds = new Set(
      this.quotes
        .filter((quote) => quote.status === 'ACCEPTED')
        .map((quote) => quote.id),
    );

    const eligibleShootings = this.shootings.filter(
      (shooting) =>
        shooting.quoteId != null &&
        acceptedQuoteIds.has(shooting.quoteId),
    );

    const eligibleShootingIds = new Set(
      eligibleShootings.map((shooting) => shooting.id),
    );

    const acceptedShootingTotals = eligibleShootings.reduce(
      (total, shooting) =>
        total + (shooting.quoteTotalAmount ?? 0),
      0,
    );

    const receivedForEligibleShootings = this.payments
      .filter(
        (payment) =>
          payment.status === 'RECEIVED' &&
          eligibleShootingIds.has(payment.shootingId),
      )
      .reduce((total, payment) => total + payment.amount, 0);

    return Math.max(
      0,
      acceptedShootingTotals - receivedForEligibleShootings,
    );
  }

  get monthlyIncomeChartData(): MonthlyIncome[] {
    const now = new Date();

    const months: {
      label: string;
      year: number;
      month: number;
      amount: number;
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
        amount: 0,
      });
    }

    this.payments
      .filter((payment) => payment.status === 'RECEIVED')
      .forEach((payment) => {
        const date = new Date(payment.paymentDate);

        const month = months.find(
          (item) =>
            item.month === date.getMonth() &&
            item.year === date.getFullYear(),
        );

        if (month) {
          month.amount += payment.amount;
        }
      });

    const maxAmount = Math.max(
      ...months.map((month) => month.amount),
      1,
    );

    return months.map((month) => ({
      label: month.label,
      amount: month.amount,
      percentage: (month.amount / maxAmount) * 100,
    }));
  }

  // GRAFICO SHOOTING

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

  // FORMATTAZIONE

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
