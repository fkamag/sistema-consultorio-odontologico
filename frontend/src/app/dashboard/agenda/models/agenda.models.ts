export interface Agendamento {
  id: string;
  patientId: string;
  patientName: string;
  dentistId: string;
  dentistName: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  status: 'AGENDADO' | 'CONFIRMADO' | 'REALIZADO' | 'CANCELADO';
  notes?: string;
  createdAt: string;
  createdByName?: string;
  updatedByName?: string;
}

export interface AgendamentoRequest {
  patientId: string;
  dentistId: string;
  startTime: string;
  durationMinutes: number;
  notes?: string | null;
}

export interface DiaMiniCalendario {
  data: Date;
  doMes: boolean;
  hoje: boolean;
  selecionado: boolean;
  temAgendamento: boolean;
}

export type SlotCategoria = 'livre' | 'ocupado' | 'almoco' | 'indisponivel';

export interface SlotHorario {
  hora: string;
  categoria: SlotCategoria;
  label: string;
  detalhe: string;
  agendamentoId?: string;
  patientId?: string;
}
