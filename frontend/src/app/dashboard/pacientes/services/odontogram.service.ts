import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ToothRecord, ToothRecordRequest } from '../models/odontogram.models';

@Injectable({ providedIn: 'root' })
export class OdontogramService {
  private readonly api = '/api';

  constructor(private http: HttpClient) {}

  list(patientId: string): Observable<ToothRecord[]> {
    return this.http.get<ToothRecord[]>(`${this.api}/patients/${patientId}/odontogram`);
  }

  upsert(patientId: string, toothNumber: string, req: ToothRecordRequest): Observable<ToothRecord> {
    return this.http.put<ToothRecord>(`${this.api}/patients/${patientId}/odontogram/${toothNumber}`, req);
  }
}
