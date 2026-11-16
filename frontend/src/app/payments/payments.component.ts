import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject,
} from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  Payment,
  PaymentRequest,
  PaymentMethod,
  PaymentType,
  PaymentStatus,
} from '../models/payment.model';

import { Shooting } from '../models/shooting.model';
import { PaymentService } from '../services/payment.service';
import { ShootingService } from '../services/shooting.service';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payments.component.html',
  styleUrl: './payments.component.scss',
})
export class PaymentsComponent implements OnInit {
  private readonly paymentService = inject(PaymentService);
  private readonly shootingService = inject(ShootingService);
  private readonly cdr = inject(ChangeDetectorRef);

  payments: Payment[] = [];
  shootings: Shooting[] = [];

  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  searchTerm = '';
  statusFilter = 'ALL';

  showForm = false;
  editingPaymentId: number | null = null;

  form: PaymentRequest = this.emptyForm();

  readonly methods: { value: PaymentMethod; label: string }[] = [
    { value: 'CASH', label: 'Contanti' },
    { value: 'BANK_TRANSFER', label: 'Bonifico bancario' },
    { value: 'PAYPAL', label: 'PayPal' },
    { value: 'POS', label: 'Carta / POS' },
  ];

  readonly types: { value: PaymentType; label: string }[] = [
    { value: 'DEPOSIT', label: 'Acconto' },
    { value: 'INSTALLMENT', label: 'Rata' },
    { value: 'BALANCE', label: 'Saldo' },
  ];

  readonly statuses: { value: PaymentStatus; label: string }[] = [
    { value: 'RECEIVED', label: 'Ricevuto' },
    { value: 'PENDING', label: 'In attesa' },
    { value: 'REFUNDED', label: 'Rimborsato' },
  ];

  ngOnInit(): void {
    this.loadData();
  }

  private emptyForm(): PaymentRequest {
    return {
      shootingId: 0,
      amount: 0,
      paymentDate: new Date().toISOString().slice(0, 10),
      method: 'BANK_TRANSFER',
      type: 'DEPOSIT',
      status: 'RECEIVED',
      notes: '',
    };
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    let paymentsLoaded = false;
    let shootingsLoaded = false;
    let failed = false;

    const finish = (): void => {
      if (paymentsLoaded && shootingsLoaded || failed) {
        this.loading = false;
        this.cdr.detectChanges();
      }
    };

    this.paymentService.getAll().subscribe({
      next: (payments) => {
        this.payments = payments;
        paymentsLoaded = true;
        finish();
      },
      error: () => {
        failed = true;
        this.errorMessage = 'Impossibile caricare i pagamenti.';
        finish();
      },
    });

    this.shootingService.getAll().subscribe({
      next: (shootings) => {
        this.shootings = shootings;
        shootingsLoaded = true;
        finish();
      },
      error: () => {
        failed = true;
        this.errorMessage = 'Impossibile caricare gli shooting.';
        finish();
      },
    });
  }

  get eligibleShootings(): Shooting[] {
    return this.shootings.filter(
      (shooting) => shooting.quoteId != null,
    );
  }

  get filteredPayments(): Payment[] {
    const term = this.searchTerm.trim().toLowerCase();

    return this.payments.filter((payment) => {
      const matchesStatus =
        this.statusFilter === 'ALL' ||
        payment.status === this.statusFilter;

      const searchable = [
        payment.shootingTitle,
        payment.clientFirstName,
        payment.clientLastName,
        this.getMethodLabel(payment.method),
        this.getTypeLabel(payment.type),
      ]
        .join(' ')
        .toLowerCase();

      return matchesStatus &&
        (!term || searchable.includes(term));
    }).sort(
      (a, b) =>
        new Date(b.paymentDate).getTime() -
        new Date(a.paymentDate).getTime(),
    );
  }

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
    const shootingTotals = new Map<number, number>();
    const shootingPaid = new Map<number, number>();

    for (const payment of this.payments) {
      shootingTotals.set(
        payment.shootingId,
        payment.quoteTotalAmount,
      );

      if (payment.status === 'RECEIVED') {
        shootingPaid.set(
          payment.shootingId,
          (shootingPaid.get(payment.shootingId) ?? 0) +
            payment.amount,
        );
      }
    }

    let outstanding = 0;

    for (const [shootingId, total] of shootingTotals) {
      const paid = shootingPaid.get(shootingId) ?? 0;
      outstanding += Math.max(0, total - paid);
    }

    return outstanding;
  }

  get receivedCount(): number {
    return this.payments.filter(
      (payment) => payment.status === 'RECEIVED',
    ).length;
  }

  openCreateForm(): void {
    this.editingPaymentId = null;
    this.form = this.emptyForm();
    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  openEditForm(payment: Payment): void {
    this.editingPaymentId = payment.id;

    this.form = {
      shootingId: payment.shootingId,
      amount: payment.amount,
      paymentDate: payment.paymentDate,
      method: payment.method,
      type: payment.type,
      status: payment.status,
      notes: payment.notes ?? '',
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingPaymentId = null;
    this.form = this.emptyForm();
  }

  savePayment(): void {
    if (this.saving) {
      return;
    }

    if (
      !this.form.shootingId ||
      this.form.amount <= 0 ||
      !this.form.paymentDate
    ) {
      this.errorMessage =
        'Seleziona uno shooting e inserisci un importo e una data validi.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: PaymentRequest = {
      ...this.form,
      amount: Number(this.form.amount),
      notes: this.form.notes.trim(),
    };

    const operation =
      this.editingPaymentId === null
        ? this.paymentService.create(request)
        : this.paymentService.update(this.editingPaymentId, request);

    operation.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.editingPaymentId = null;
        this.form = this.emptyForm();

        this.successMessage = 'Pagamento salvato correttamente.';
        this.loadData();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage =
          error?.error?.message ??
          'Errore durante il salvataggio del pagamento.';
        this.cdr.detectChanges();
      },
    });
  }

  deletePayment(payment: Payment): void {
    const confirmed = window.confirm(
      `Vuoi eliminare il pagamento di ${this.formatCurrency(payment.amount)}?`,
    );

    if (!confirmed) {
      return;
    }

    this.paymentService.delete(payment.id).subscribe({
      next: () => {
        this.payments = this.payments.filter(
          (item) => item.id !== payment.id,
        );
        this.successMessage = 'Pagamento eliminato correttamente.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage =
          'Errore durante l’eliminazione del pagamento.';
        this.cdr.detectChanges();
      },
    });
  }

  getShootingLabel(shooting: Shooting): string {
    return `${shooting.title} — ${shooting.clientFirstName} ${shooting.clientLastName}`;
  }

  getSelectedShootingTotal(): number {
    const shooting = this.eligibleShootings.find(
      (item) => item.id === Number(this.form.shootingId),
    );

    return shooting?.quoteTotalAmount ?? 0;
  }

  getMethodLabel(method: PaymentMethod): string {
    return this.methods.find((item) => item.value === method)?.label ?? method;
  }

  getTypeLabel(type: PaymentType): string {
    return this.types.find((item) => item.value === type)?.label ?? type;
  }

  getStatusLabel(status: PaymentStatus): string {
    return this.statuses.find((item) => item.value === status)?.label ?? status;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('it-IT', {
      style: 'currency',
      currency: 'EUR',
    }).format(value ?? 0);
  }

  formatDate(value: string): string {
    return new Date(`${value}T00:00:00`).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  trackByPayment(_index: number, payment: Payment): number {
    return payment.id;
  }
}