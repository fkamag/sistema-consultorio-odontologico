import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Agendamento, AgendamentoRequest } from '../models/agenda.models';

@Injectable({ providedIn: 'root' })
export class AgendaService {

  private readonly apiUrl = '/api/agendamentos';

  constructor(private http: HttpClient) {}

  listByPeriod(dentistId: string | null, from: string, to: string): Observable<Agendamento[]> {
    let params = new HttpParams().set('from', from).set('to', to);
    if (dentistId) params = params.set('dentistId', dentistId);
    return this.http.get<Agendamento[]>(this.apiUrl, { params });
  }

  listByPatient(patientId: string): Observable<Agendamento[]> {
    return this.http.get<Agendamento[]>(`${this.apiUrl}/paciente/${patientId}`);
  }

  create(request: AgendamentoRequest): Observable<Agendamento> {
    return this.http.post<Agendamento>(this.apiUrl, request);
  }

  update(id: string, request: AgendamentoRequest): Observable<Agendamento> {
    return this.http.put<Agendamento>(`${this.apiUrl}/${id}`, request);
  }

  changeStatus(id: string, status: string): Observable<Agendamento> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<Agendamento>(`${this.apiUrl}/${id}/status`, null, { params });
  }
}
