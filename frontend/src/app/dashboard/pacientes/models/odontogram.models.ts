export type FaceStatus = '' | 'pendente' | 'analisado' | 'tratado';
export const FACE_CYCLE: FaceStatus[] = ['', 'pendente', 'analisado', 'tratado'];
export const FACE_LABEL: Record<FaceStatus, string> = {
  '': 'Sem marcação', pendente: 'Pendente', analisado: 'Analisado', tratado: 'Tratado'
};
// Cores escolhidas com máxima separação no espectro: amarelo / roxo / verde
const FACE_AMARELO = '#D4A017';  // pendente
const FACE_ROXO    = '#9B59B6';  // analisado
const FACE_VERDE   = '#27AE60';  // tratado

export const FACE_FILL: Record<FaceStatus, string> = {
  '': 'var(--color-surface)',
  pendente: FACE_AMARELO,
  analisado: FACE_ROXO,
  tratado:   FACE_VERDE,
};

export const FACE_FILL_OPACITY: Record<FaceStatus, string> = {
  '': '1',
  pendente: '0.5',
  analisado: '0.5',
  tratado: '0.5',
};

export const FACE_STROKE: Record<FaceStatus, string> = {
  '': 'var(--color-line)',
  pendente: FACE_AMARELO,
  analisado: FACE_ROXO,
  tratado:   FACE_VERDE,
};

export interface ToothRecord {
  id: string;
  patientId: string;
  toothNumber: string;
  vestibular: FaceStatus;
  mesial: FaceStatus;
  distal: FaceStatus;
  lingual: FaceStatus;
  oclusal: FaceStatus;
  canal: boolean;
  ausente: boolean;
  note: string | null;
  updatedAt: string;
  updatedByName: string | null;
}

export interface ToothRecordRequest {
  toothNumber: string;
  vestibular: FaceStatus;
  mesial: FaceStatus;
  distal: FaceStatus;
  lingual: FaceStatus;
  oclusal: FaceStatus;
  canal: boolean;
  ausente: boolean;
  note: string | null;
}

export const UPPER_TEETH = [18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28];
export const LOWER_TEETH = [48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38];

export function emptyRecord(toothNumber: string): ToothRecordRequest {
  return { toothNumber, vestibular: '', mesial: '', distal: '', lingual: '', oclusal: '', canal: false, ausente: false, note: null };
}

export function cycleFace(current: FaceStatus): FaceStatus {
  const idx = FACE_CYCLE.indexOf(current);
  return FACE_CYCLE[(idx + 1) % FACE_CYCLE.length];
}

/** Quadrante 1 e 4: mesial está à direita do centro da boca */
export function mesialOnRight(toothNumber: number): boolean {
  const q = Math.floor(toothNumber / 10);
  return q === 1 || q === 4;
}
