
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  Shooting,
  GalleryStatus,
} from '../models/shooting.model';

import { ShootingService } from '../services/shooting.service';

@Component({
  selector: 'app-galleries',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './galleries.component.html',
  styleUrl: './galleries.component.scss',
})
export class GalleriesComponent implements OnInit {
  private readonly shootingService = inject(ShootingService);
  private readonly cdr = inject(ChangeDetectorRef);

  shootings: Shooting[] = [];

  loading = false;
  errorMessage = '';
  successMessage = '';

  searchTerm = '';
  selectedFilter: GalleryStatus | 'ALL' = 'ALL';

  updatingIds = new Set<number>();

  readonly statuses: { value: GalleryStatus; label: string }[] = [
    { value: 'NOT_STARTED', label: 'Da preparare' },
    { value: 'IN_PROGRESS', label: 'In lavorazione' },
    { value: 'TO_SELECT', label: 'Da selezionare' },
    { value: 'DELIVERED', label: 'Consegnata' },
    { value: 'ARCHIVED', label: 'Archiviata' },
  ];

  ngOnInit(): void {
    this.loadShootings();
  }

  loadShootings(): void {
    this.loading = true;
    this.errorMessage = '';

    this.shootingService.getAll().subscribe({
      next: (shootings) => {
        this.shootings = [...shootings]
          .filter((shooting) => shooting.status !== 'CANCELLED')
          .sort(
            (a, b) =>
              new Date(b.startAt).getTime() -
              new Date(a.startAt).getTime(),
          );

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossibile caricare le gallerie.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get filteredShootings(): Shooting[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.shootings.filter((shooting) => {
      const fullName =
        `${shooting.clientFirstName} ${shooting.clientLastName}`.toLowerCase();

      const matchesSearch =
        !search ||
        shooting.title.toLowerCase().includes(search) ||
        fullName.includes(search) ||
        shooting.category.toLowerCase().includes(search);

      const matchesStatus =
        this.selectedFilter === 'ALL' ||
        shooting.galleryStatus === this.selectedFilter;

      return matchesSearch && matchesStatus;
    });
  }

  getStatusLabel(status: GalleryStatus): string {
    return (
      this.statuses.find((item) => item.value === status)?.label ??
      status
    );
  }

  getCount(status: GalleryStatus): number {
    return this.shootings.filter(
      (shooting) => shooting.galleryStatus === status,
    ).length;
  }

  updateGalleryStatus(
    shooting: Shooting,
    newStatus: GalleryStatus,
  ): void {
    const previousStatus = shooting.galleryStatus;

    if (
      newStatus === previousStatus ||
      this.updatingIds.has(shooting.id)
    ) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.updatingIds.add(shooting.id);

    this.shootingService
      .updateGalleryStatus(shooting.id, newStatus)
      .subscribe({
        next: (updatedShooting) => {
          shooting.galleryStatus = updatedShooting.galleryStatus;
          this.updatingIds.delete(shooting.id);

          this.successMessage = 'Stato della galleria aggiornato.';
          this.cdr.detectChanges();
        },
        error: () => {
          shooting.galleryStatus = previousStatus;
          this.updatingIds.delete(shooting.id);

          this.errorMessage =
            'Errore durante l’aggiornamento dello stato.';

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
}
