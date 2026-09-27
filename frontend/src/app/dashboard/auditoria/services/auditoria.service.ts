import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog, PageResponse } from '../models/auditoria.models';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {

  private readonly apiUrl = 'http://localhost:8080/api/auditoria';

  constructor(private http: HttpClient) {}

  list(search = '', page = 0, size = 20, from?: string, to?: string): Observable<PageResponse<AuditLog>> {
    let params = new HttpParams()
      .set('search', search)
      .set('page', page)
      .set('size', size);
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get<PageResponse<AuditLog>>(this.apiUrl, { params });
  }
}
