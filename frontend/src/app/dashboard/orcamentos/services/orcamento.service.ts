import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrcamentoPage, BudgetRequest, BudgetPayment } from '../models/orcamento.models';
import { BudgetResponse } from '../../pacientes/models/budget.models';

@Injectable({ providedIn: 'root' })
export class OrcamentoService {
  private readonly api = '/api/budgets';
  private patientApi = (patientId: string) => `/api/patients/${patientId}/budgets`;

  constructor(private http: HttpClient) {}

  listAll(status?: string, page = 0, size = 20): Observable<OrcamentoPage> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<OrcamentoPage>(this.api, { params });
  }

  create(patientId: string, req: BudgetRequest): Observable<BudgetResponse> {
    return this.http.post<BudgetResponse>(`${this.api}/patient/${patientId}`, req);
  }

  update(patientId: string, budgetId: string, req: BudgetRequest): Observable<BudgetResponse> {
    return this.http.put<BudgetResponse>(`${this.api}/${budgetId}/patient/${patientId}`, req);
  }

  findById(patientId: string, budgetId: string): Observable<BudgetResponse> {
    return this.http.get<BudgetResponse>(`${this.patientApi(patientId)}/${budgetId}`);
  }

  listPayments(patientId: string, budgetId: string): Observable<BudgetPayment[]> {
    return this.http.get<BudgetPayment[]>(`${this.patientApi(patientId)}/${budgetId}/payments`);
  }

  addPayment(patientId: string, budgetId: string, payload: {
    data: string; valor: number; formaPagamento: string; status: string;
  }): Observable<BudgetPayment> {
    return this.http.post<BudgetPayment>(`${this.patientApi(patientId)}/${budgetId}/payments`, payload);
  }

  togglePayment(patientId: string, budgetId: string, paymentId: string): Observable<BudgetPayment> {
    return this.http.patch<BudgetPayment>(
      `${this.patientApi(patientId)}/${budgetId}/payments/${paymentId}/toggle`, {});
  }

  deletePayment(patientId: string, budgetId: string, paymentId: string): Observable<void> {
    return this.http.delete<void>(`${this.patientApi(patientId)}/${budgetId}/payments/${paymentId}`);
  }
}
