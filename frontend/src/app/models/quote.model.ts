
export type QuoteStatus = 'DRAFT' | 'SENT' | 'ACCEPTED' | 'REJECTED';

export interface QuoteItem {
  description: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

export interface Quote {
  id: number;
  quoteNumber: string;
  title: string;
  notes: string | null;
  issueDate: string | null;
  expiryDate: string | null;
  status: QuoteStatus;
  clientId: number;
  clientName: string;
  items: QuoteItem[];
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface QuoteItemRequest {
  description: string;
  quantity: number;
  unitPrice: number;
}

export interface QuoteRequest {
  title: string;
  notes: string;
  issueDate: string;
  expiryDate: string;
  status: QuoteStatus;
  clientId: number;
  items: QuoteItemRequest[];
}
