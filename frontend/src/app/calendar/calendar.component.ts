
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';

import { Shooting } from '../models/shooting.model';
import { ShootingService } from '../services/shooting.service';

interface CalendarDay {
  date: Date;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  shootings: Shooting[];
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss',
})
export class CalendarComponent implements OnInit {
  private readonly shootingService = inject(ShootingService);
  private readonly cdr = inject(ChangeDetectorRef);

  shootings: Shooting[] = [];
  calendarDays: CalendarDay[] = [];

  loading = false;
  errorMessage = '';

  selectedDate = new Date();
  currentMonth = new Date();

  readonly weekdays = ['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom'];

  ngOnInit(): void {
    this.loadShootings();
  }

  loadShootings(): void {
    this.loading = true;
    this.errorMessage = '';

    this.shootingService.getAll().subscribe({
      next: (shootings) => {
        this.shootings = shootings;
        this.loading = false;
        this.generateCalendar();
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage =
          'Impossibile caricare gli shooting. Verifica che il backend sia avviato.';
        this.loading = false;
        this.generateCalendar();
        this.cdr.detectChanges();
      },
    });
  }

  generateCalendar(): void {
    const year = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);

    const startOffset = (firstDayOfMonth.getDay() + 6) % 7;
    const totalDays = lastDayOfMonth.getDate();

    const totalCells = Math.ceil((startOffset + totalDays) / 7) * 7;

    this.calendarDays = [];

    for (let index = 0; index < totalCells; index++) {
      const date = new Date(year, month, 1 - startOffset + index);

      this.calendarDays.push({
        date,
        dayNumber: date.getDate(),
        isCurrentMonth: date.getMonth() === month,
        isToday: this.isSameDay(date, new Date()),
        shootings: this.getShootingsForDate(date),
      });
    }
  }

  getShootingsForDate(date: Date): Shooting[] {
    return this.shootings.filter((shooting) => {
      const startAt = new Date(shooting.startAt);
      return this.isSameDay(startAt, date);
    });
  }

  selectDate(date: Date): void {
    this.selectedDate = new Date(date);
  }

  previousMonth(): void {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() - 1,
      1,
    );

    this.generateCalendar();
  }

  nextMonth(): void {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() + 1,
      1,
    );

    this.generateCalendar();
  }

  goToToday(): void {
    this.currentMonth = new Date();
    this.selectedDate = new Date();
    this.generateCalendar();
  }

  isSelectedDate(date: Date): boolean {
    return this.isSameDay(date, this.selectedDate);
  }

  get selectedDateShootings(): Shooting[] {
    return this.getShootingsForDate(this.selectedDate);
  }

  get monthLabel(): string {
    return this.currentMonth.toLocaleDateString('it-IT', {
      month: 'long',
      year: 'numeric',
    });
  }

  get selectedDateLabel(): string {
    return this.selectedDate.toLocaleDateString('it-IT', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
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

  private isSameDay(first: Date, second: Date): boolean {
    return (
      first.getFullYear() === second.getFullYear() &&
      first.getMonth() === second.getMonth() &&
      first.getDate() === second.getDate()
    );
  }
}
