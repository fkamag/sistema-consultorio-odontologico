import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardSummary } from './dashboard.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = '/api';

  constructor(private http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.api}/dashboard/summary`);
  }
}
