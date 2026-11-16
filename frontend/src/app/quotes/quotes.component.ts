
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Client } from '../models/client.model';
import {
  Quote,
  QuoteRequest,
  QuoteStatus,
  QuoteItemRequest,
} from '../models/quote.model';

import { ClientService } from '../services/client.service';
import { QuoteService } from '../services/quote.service';

@Component({
  selector: 'app-quotes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './quotes.component.html',
  styleUrl: './quotes.component.scss',
})
export class QuotesComponent implements OnInit {
  private readonly quoteService = inject(QuoteService);
  private readonly clientService = inject(ClientService);
  private readonly cdr = inject(ChangeDetectorRef);

  quotes: Quote[] = [];
  clients: Client[] = [];

  loadingQuotes = false;
  savingQuote = false;
  showForm = false;

  editingQuoteId: number | null = null;
  selectedStatus: QuoteStatus | 'ALL' = 'ALL';

  errorMessage = '';
  successMessage = '';

  readonly statuses: { value: QuoteStatus; label: string }[] = [
    { value: 'DRAFT', label: 'Bozza' },
    { value: 'SENT', label: 'Inviato' },
    { value: 'ACCEPTED', label: 'Accettato' },
    { value: 'REJECTED', label: 'Rifiutato' },
  ];

  quoteForm: QuoteRequest = this.emptyForm();

  ngOnInit(): void {
    this.loadClients();
    this.loadQuotes();
  }

  private emptyForm(): QuoteRequest {
    return {
      title: '',
      notes: '',
      issueDate: new Date().toISOString().slice(0, 10),
      expiryDate: '',
      status: 'DRAFT',
      clientId: 0,
      items: [
        {
          description: '',
          quantity: 1,
          unitPrice: 0,
        },
      ],
    };
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

  loadQuotes(): void {
    this.loadingQuotes = true;
    this.errorMessage = '';

    this.quoteService.getAll().subscribe({
      next: (quotes) => {
        this.quotes = [...quotes].sort(
          (a, b) =>
            new Date(b.createdAt).getTime() -
            new Date(a.createdAt).getTime(),
        );

        this.loadingQuotes = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage =
          'Impossibile caricare i preventivi. Verifica che il backend sia avviato.';
        this.loadingQuotes = false;
        this.cdr.detectChanges();
      },
    });
  }

  get filteredQuotes(): Quote[] {
    if (this.selectedStatus === 'ALL') {
      return this.quotes;
    }

    return this.quotes.filter(
      (quote) => quote.status === this.selectedStatus,
    );
  }

  openCreateForm(): void {
    this.editingQuoteId = null;
    this.quoteForm = this.emptyForm();
    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  openEditForm(quote: Quote): void {
    this.editingQuoteId = quote.id;

    this.quoteForm = {
      title: quote.title,
      notes: quote.notes ?? '',
      issueDate: quote.issueDate ?? '',
      expiryDate: quote.expiryDate ?? '',
      status: quote.status,
      clientId: quote.clientId,
      items: quote.items.map((item) => ({
        description: item.description,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
      })),
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingQuoteId = null;
    this.quoteForm = this.emptyForm();
  }

  addItem(): void {
    this.quoteForm.items.push({
      description: '',
      quantity: 1,
      unitPrice: 0,
    });
  }

  removeItem(index: number): void {
    if (this.quoteForm.items.length <= 1) {
      return;
    }

    this.quoteForm.items.splice(index, 1);
  }

  get totalAmount(): number {
    return this.quoteForm.items.reduce(
      (total, item) => total + item.quantity * item.unitPrice,
      0,
    );
  }

  saveQuote(): void {
    if (this.savingQuote) {
      return;
    }

    this.savingQuote = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: QuoteRequest = {
      ...this.quoteForm,
      title: this.quoteForm.title.trim(),
      notes: this.quoteForm.notes.trim(),
      items: this.quoteForm.items.map((item: QuoteItemRequest) => ({
        description: item.description.trim(),
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
      })),
    };

    const operation =
      this.editingQuoteId === null
        ? this.quoteService.create(request)
        : this.quoteService.update(this.editingQuoteId, request);

    operation.subscribe({
      next: () => {
        const wasEditing = this.editingQuoteId !== null;

        this.savingQuote = false;
        this.showForm = false;
        this.editingQuoteId = null;
        this.quoteForm = this.emptyForm();

        this.successMessage = wasEditing
          ? 'Preventivo aggiornato correttamente.'
          : 'Preventivo creato correttamente.';

        this.loadQuotes();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.savingQuote = false;

        if (error.status === 400 && error.error) {
          const messages = Object.values(error.error) as string[];
          this.errorMessage = messages.join(' ');
        } else {
          this.errorMessage =
            'Errore durante il salvataggio del preventivo.';
        }

        this.cdr.detectChanges();
      },
    });
  }

  deleteQuote(quote: Quote): void {
    const confirmed = window.confirm(
      `Vuoi eliminare il preventivo "${quote.quoteNumber}"?`,
    );

    if (!confirmed) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.quoteService.delete(quote.id).subscribe({
      next: () => {
        this.quotes = this.quotes.filter(
          (item) => item.id !== quote.id,
        );

        this.successMessage = 'Preventivo eliminato correttamente.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage =
          'Errore durante l’eliminazione del preventivo.';
        this.cdr.detectChanges();
      },
    });
  }

  getStatusLabel(status: QuoteStatus): string {
    return this.statuses.find((item) => item.value === status)?.label ?? status;
  }

  formatDate(date: string | null): string {
    if (!date) {
      return '—';
    }

    return new Date(`${date}T00:00:00`).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatCurrency(amount: number): string {
    return amount.toLocaleString('it-IT', {
      style: 'currency',
      currency: 'EUR',
    });
  }
}
