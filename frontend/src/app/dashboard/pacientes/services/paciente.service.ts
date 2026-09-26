import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Paciente, PacienteRequest, PageResponse } from '../models/paciente.models';

@Injectable({ providedIn: 'root' })
export class PacienteService {

  private readonly apiUrl = 'http://localhost:8080/api/pacientes';

  constructor(private http: HttpClient) {}

  list(search = '', page = 0, size = 20): Observable<PageResponse<Paciente>> {
    const params = new HttpParams()
      .set('search', search)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<Paciente>>(this.apiUrl, { params });
  }

  findById(id: string): Observable<Paciente> {
    return this.http.get<Paciente>(`${this.apiUrl}/${id}`);
  }

  create(request: PacienteRequest): Observable<Paciente> {
    return this.http.post<Paciente>(this.apiUrl, request);
  }

  update(id: string, request: PacienteRequest): Observable<Paciente> {
    return this.http.put<Paciente>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
