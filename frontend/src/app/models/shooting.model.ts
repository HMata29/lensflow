
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
}
