import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConsentResponse, ConsentRequest } from '../models/consent.models';

@Injectable({ providedIn: 'root' })
export class ConsentService {
  private readonly api = '/api';

  constructor(private http: HttpClient) {}

  list(patientId: string): Observable<ConsentResponse[]> {
    return this.http.get<ConsentResponse[]>(`${this.api}/patients/${patientId}/consents`);
  }

  create(patientId: string, req: ConsentRequest): Observable<ConsentResponse> {
    return this.http.post<ConsentResponse>(`${this.api}/patients/${patientId}/consents`, req);
  }

  sign(patientId: string, consentId: string, signerName: string): Observable<ConsentResponse> {
    return this.http.post<ConsentResponse>(
      `${this.api}/patients/${patientId}/consents/${consentId}/sign`,
      null,
      { params: { signerName } }
    );
  }

  delete(patientId: string, consentId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/patients/${patientId}/consents/${consentId}`);
  }
}
