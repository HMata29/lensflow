export type PaymentMethod =
  | 'CASH'
  | 'BANK_TRANSFER'
  | 'PAYPAL'
  | 'POS';

export type PaymentType =
  | 'DEPOSIT'
  | 'INSTALLMENT'
  | 'BALANCE';

export type PaymentStatus =
  | 'RECEIVED'
  | 'PENDING'
  | 'REFUNDED';

export interface Payment {
  id: number;
  shootingId: number;
  shootingTitle: string;
  clientFirstName: string;
  clientLastName: string;
  quoteTotalAmount: number;
  amount: number;
  paymentDate: string;
  method: PaymentMethod;
  type: PaymentType;
  status: PaymentStatus;
  notes: string;
}

export interface PaymentRequest {
  shootingId: number;
  amount: number;
  paymentDate: string;
  method: PaymentMethod;
  type: PaymentType;
  status: PaymentStatus;
  notes: string;
}