import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario, UsuarioRequest, PageResponse } from '../models/usuario.models';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private readonly apiUrl = '/api/usuarios';

  constructor(private http: HttpClient) {}

  list(search = '', page = 0, size = 20, role?: string): Observable<PageResponse<Usuario>> {
    let params = new HttpParams()
      .set('search', search)
      .set('page', page)
      .set('size', size);
    if (role) params = params.set('role', role);
    return this.http.get<PageResponse<Usuario>>(this.apiUrl, { params });
  }

  findById(id: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  create(request: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, request);
  }

  update(id: string, request: UsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, request);
  }

  toggleActive(id: string): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.apiUrl}/${id}/toggle-active`, {});
  }
}
