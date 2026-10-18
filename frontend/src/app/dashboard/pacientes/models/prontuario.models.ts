export type NoteType = 'CONSULTA' | 'PROCEDIMENTO' | 'RETORNO' | 'OBSERVACAO' | 'INTERCORRENCIA';
export type PlanStatus =
  | 'PLANEJADO' | 'APROVADO' | 'PARCIALMENTE_APROVADO'
  | 'REALIZADO' | 'CANCELADO' | 'PENDENTE';

export interface MedicalHistoryResponse {
  id: string;
  patientId: string;
  versionNumber: number;
  alergias: string | null;
  medicamentos: string | null;
  diabetes: boolean;
  hipertensao: boolean;
  problemasCardiacos: boolean;
  tratamentosMedicos: string | null;
  cirurgias: string | null;
  observacoes: string | null;
  createdAt: string;
  createdByName: string | null;
}

export interface MedicalHistoryRequest {
  alergias: string | null;
  medicamentos: string | null;
  diabetes: boolean;
  hipertensao: boolean;
  problemasCardiacos: boolean;
  tratamentosMedicos: string | null;
  cirurgias: string | null;
  observacoes: string | null;
}

export interface ClinicalNoteResponse {
  id: string;
  patientId: string;
  dentistId: string | null;
  dentistName: string | null;
  appointmentId: string | null;
  noteDate: string;
  noteType: NoteType;
  tooth: string | null;
  description: string;
  createdAt: string;
}

export interface ClinicalNoteRequest {
  noteDate: string;
  noteType: NoteType;
  tooth: string | null;
  description: string;
  appointmentId: string | null;
}

export interface TreatmentPlanResponse {
  id: string;
  patientId: string;
  description: string;
  tooth: string | null;
  valueEstimated: number | null;
  status: PlanStatus;
  appointmentId: string | null;
  createdAt: string;
  createdByName: string | null;
  updatedAt: string;
  updatedByName: string | null;
}

export interface TreatmentPlanRequest {
  description: string;
  tooth: string | null;
  valueEstimated: number | null;
}

export const NOTE_TYPE_LABELS: Record<NoteType, string> = {
  CONSULTA: 'Consulta',
  PROCEDIMENTO: 'Procedimento',
  RETORNO: 'Retorno',
  OBSERVACAO: 'Observação',
  INTERCORRENCIA: 'Intercorrência',
};

export const PLAN_STATUS_LABELS: Record<PlanStatus, string> = {
  PLANEJADO: 'Planejado',
  APROVADO: 'Aprovado',
  PARCIALMENTE_APROVADO: 'Parcial',
  REALIZADO: 'Realizado',
  CANCELADO: 'Cancelado',
  PENDENTE: 'Pendente',
};

export const PLAN_STATUSES: PlanStatus[] = [
  'PLANEJADO', 'APROVADO', 'PARCIALMENTE_APROVADO', 'REALIZADO', 'CANCELADO', 'PENDENTE'
];
