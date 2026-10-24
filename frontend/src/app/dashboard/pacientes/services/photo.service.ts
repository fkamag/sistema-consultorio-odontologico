import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClinicalPhoto } from '../models/photo.models';

@Injectable({ providedIn: 'root' })
export class PhotoService {
  private readonly api = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  list(patientId: string): Observable<ClinicalPhoto[]> {
    return this.http.get<ClinicalPhoto[]>(`${this.api}/patients/${patientId}/photos`);
  }

  upload(patientId: string, file: File, caption: string, clinicalNoteId?: string): Observable<ClinicalPhoto> {
    const fd = new FormData();
    fd.append('file', file);
    if (caption) fd.append('caption', caption);
    if (clinicalNoteId) fd.append('clinicalNoteId', clinicalNoteId);
    return this.http.post<ClinicalPhoto>(`${this.api}/patients/${patientId}/photos`, fd);
  }

  photoUrl(id: string): string {
    return `${this.api}/photos/${id}/content`;
  }

  delete(patientId: string, id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/patients/${patientId}/photos/${id}`);
  }
}
