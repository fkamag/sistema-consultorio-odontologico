import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TimelineEvent } from '../models/timeline.models';

@Injectable({ providedIn: 'root' })
export class TimelineService {
  private readonly api = '/api';

  constructor(private http: HttpClient) {}

  list(patientId: string): Observable<TimelineEvent[]> {
    return this.http.get<TimelineEvent[]>(`${this.api}/patients/${patientId}/timeline`);
  }
}
