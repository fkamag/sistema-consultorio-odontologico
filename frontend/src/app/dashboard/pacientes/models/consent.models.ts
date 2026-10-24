export type ConsentType =
  | 'TRATAMENTO_GERAL'
  | 'PROCEDIMENTO_CIRURGICO'
  | 'ANESTESIA'
  | 'FOTOGRAFIAS'
  | 'MENOR';

export interface ConsentResponse {
  id: string;
  patientId: string;
  title: string;
  type: ConsentType;
  content: string;
  signedAt: string | null;
  signedByName: string | null;
  createdAt: string;
  createdByName: string | null;
}

export interface ConsentRequest {
  title: string;
  type: ConsentType;
  content: string;
}

export const CONSENT_TYPE_LABELS: Record<ConsentType, string> = {
  TRATAMENTO_GERAL:       'Tratamento Geral',
  PROCEDIMENTO_CIRURGICO: 'Procedimento Cirúrgico',
  ANESTESIA:              'Anestesia',
  FOTOGRAFIAS:            'Fotografias',
  MENOR:                  'Paciente Menor',
};

export const CONSENT_TYPES: ConsentType[] = [
  'TRATAMENTO_GERAL','PROCEDIMENTO_CIRURGICO','ANESTESIA','FOTOGRAFIAS','MENOR'
];
