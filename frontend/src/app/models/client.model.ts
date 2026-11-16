
export interface Client {
  id: number;
  firstName: string;
  lastName: string;
  email: string | null;
  phone: string | null;
  company: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ClientRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  company: string;
  notes: string;
}
