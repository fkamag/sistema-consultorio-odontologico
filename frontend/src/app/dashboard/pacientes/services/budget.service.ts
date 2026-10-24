import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BudgetResponse, BudgetRequest, BudgetStatus } from '../models/budget.models';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private readonly api = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  list(patientId: string): Observable<BudgetResponse[]> {
    return this.http.get<BudgetResponse[]>(`${this.api}/patients/${patientId}/budgets`);
  }

  create(patientId: string, req: BudgetRequest): Observable<BudgetResponse> {
    return this.http.post<BudgetResponse>(`${this.api}/patients/${patientId}/budgets`, req);
  }

  updateStatus(patientId: string, budgetId: string, status: BudgetStatus): Observable<BudgetResponse> {
    return this.http.patch<BudgetResponse>(
      `${this.api}/patients/${patientId}/budgets/${budgetId}/status`,
      null,
      { params: { status } }
    );
  }

  delete(patientId: string, budgetId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/patients/${patientId}/budgets/${budgetId}`);
  }
}
