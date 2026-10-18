import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  MedicalHistoryResponse, MedicalHistoryRequest,
  ClinicalNoteResponse, ClinicalNoteRequest,
  TreatmentPlanResponse, TreatmentPlanRequest,
  PlanStatus
} from '../models/prontuario.models';

@Injectable({ providedIn: 'root' })
export class ProntuarioService {
  private readonly apiBase = 'http://localhost:8080/api';

  private base(patientId: string): string {
    return `${this.apiBase}/pacientes/${patientId}/prontuario`;
  }

  constructor(private http: HttpClient) {}

  // Anamnese
  listAnamneses(patientId: string): Observable<MedicalHistoryResponse[]> {
    return this.http.get<MedicalHistoryResponse[]>(`${this.base(patientId)}/anamnese`);
  }

  createAnamnese(patientId: string, req: MedicalHistoryRequest): Observable<MedicalHistoryResponse> {
    return this.http.post<MedicalHistoryResponse>(`${this.base(patientId)}/anamnese`, req);
  }

  // Evolução Clínica
  listNotes(patientId: string): Observable<ClinicalNoteResponse[]> {
    return this.http.get<ClinicalNoteResponse[]>(`${this.base(patientId)}/evolucao`);
  }

  createNote(patientId: string, req: ClinicalNoteRequest): Observable<ClinicalNoteResponse> {
    return this.http.post<ClinicalNoteResponse>(`${this.base(patientId)}/evolucao`, req);
  }

  deleteNote(patientId: string, noteId: string): Observable<void> {
    return this.http.delete<void>(`${this.base(patientId)}/evolucao/${noteId}`);
  }

  // Plano de Tratamento
  listPlan(patientId: string): Observable<TreatmentPlanResponse[]> {
    return this.http.get<TreatmentPlanResponse[]>(`${this.base(patientId)}/plano`);
  }

  addPlanItem(patientId: string, req: TreatmentPlanRequest): Observable<TreatmentPlanResponse> {
    return this.http.post<TreatmentPlanResponse>(`${this.base(patientId)}/plano`, req);
  }

  updatePlanStatus(patientId: string, itemId: string, status: PlanStatus): Observable<TreatmentPlanResponse> {
    return this.http.patch<TreatmentPlanResponse>(
      `${this.base(patientId)}/plano/${itemId}/status?status=${status}`, {}
    );
  }

  deletePlanItem(patientId: string, itemId: string): Observable<void> {
    return this.http.delete<void>(`${this.base(patientId)}/plano/${itemId}`);
  }
}
