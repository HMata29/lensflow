
export type ShootingStatus =
  | 'PLANNED'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface Shooting {
  id: number;
  title: string;
  category: string;
  startAt: string;
  endAt: string;
  location: string;
  status: ShootingStatus;
  notes: string;

  clientId: number;
  clientFirstName: string;
  clientLastName: string;

  // Preventivo associato
  quoteId?: number | null;
  quoteTotalAmount?: number | null;

  galleryStatus: GalleryStatus;
}

export interface ShootingRequest {
  title: string;
  category: string;
  startAt: string;
  endAt: string;
  location: string;
  status: ShootingStatus;
  notes: string;
  clientId: number;

  // Preventivo da associare allo shooting
  quoteId?: number | null;
}

export type GalleryStatus =
  | 'NOT_STARTED'
  | 'IN_PROGRESS'
  | 'TO_SELECT'
  | 'DELIVERED'
  | 'ARCHIVED';
