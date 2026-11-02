import {
  BudgetStatus, TratamentoStatus, PaymentStatus,
  BudgetRequest, BudgetPayment,
  BUDGET_STATUS_LABELS, BUDGET_STATUS_CLASS, BUDGET_STATUSES,
  TRATAMENTO_STATUS_LABELS, TRATAMENTO_STATUS_CLASS, FORMAS_PAGAMENTO
} from '../../pacientes/models/budget.models';

export interface OrcamentoSummary {
  id: string;
  patientId: string;
  patientName: string;
  dentistId: string | null;
  dentistName: string | null;
  status: BudgetStatus;
  descricaoItens: string | null;
  notes: string | null;
  validUntil: string | null;
  totalValue: number;
  totalPago: number;
  saldo: number;
  statusTratamento: TratamentoStatus;
  createdAt: string;
  createdByName: string | null;
}

export interface OrcamentoPage {
  content: OrcamentoSummary[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export {
  BudgetStatus, TratamentoStatus, PaymentStatus,
  BudgetRequest, BudgetPayment,
  BUDGET_STATUS_LABELS, BUDGET_STATUS_CLASS, BUDGET_STATUSES,
  TRATAMENTO_STATUS_LABELS, TRATAMENTO_STATUS_CLASS, FORMAS_PAGAMENTO
};
