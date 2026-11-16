
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Shooting, ShootingRequest } from '../models/shooting.model';

@Injectable({
  providedIn: 'root',
})
export class ShootingService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/shootings';

  getAll(): Observable<Shooting[]> {
    return this.http.get<Shooting[]>(this.apiUrl);
  }

  getById(id: number): Observable<Shooting> {
    return this.http.get<Shooting>(`${this.apiUrl}/${id}`);
  }

  create(request: ShootingRequest): Observable<Shooting> {
    return this.http.post<Shooting>(this.apiUrl, request);
  }

  update(id: number, request: ShootingRequest): Observable<Shooting> {
    return this.http.put<Shooting>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
