export interface AppointmentSummary {
  id: string;
  patientId: string;
  patientName: string;
  dentistId: string;
  dentistName: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  status: string;
  notes: string;
}

export interface DashboardSummary {
  consultasHoje: number;
  confirmadosHoje: number;
  canceladasHoje: number;
  pacientesAtivos: number;
  orcamentosEmAberto: number;
  proximasConsultasHoje: AppointmentSummary[];
}
