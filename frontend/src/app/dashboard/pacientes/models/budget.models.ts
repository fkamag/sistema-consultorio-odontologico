export type BudgetStatus =
  | 'EM_ELABORACAO'
  | 'ENVIADO'
  | 'AGUARDANDO_APROVACAO'
  | 'APROVADO'
  | 'PARCIALMENTE_APROVADO'
  | 'RECUSADO'
  | 'CANCELADO'
  | 'CONCLUIDO';

export type TratamentoStatus = 'NAO_INICIADO' | 'EM_ANDAMENTO' | 'FINALIZADO';
export type PaymentStatus = 'PAGO' | 'PENDENTE';

export interface BudgetItem {
  id: string;
  budgetId: string;
  description: string;
  tooth: string | null;
  quantity: number;
  unitValue: number;
}

export interface BudgetPayment {
  id: string;
  budgetId: string;
  data: string;
  valor: number;
  formaPagamento: string;
  status: PaymentStatus;
  createdAt: string;
}

export interface BudgetResponse {
  id: string;
  patientId: string;
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
  situacaoSaldo: string | null;
  declaracaoIr: string | null;
  dataDeclaracao: string | null;
  createdAt: string;
  createdByName: string | null;
  updatedAt: string;
  updatedByName: string | null;
  items: BudgetItem[];
}

export interface BudgetItemRequest {
  description: string;
  tooth: string | null;
  quantity: number;
  unitValue: number;
}

export interface BudgetRequest {
  dentistId: string | null;
  descricaoItens: string | null;
  notes: string | null;
  totalValue?: number;
  validUntil: string | null;
  status: BudgetStatus;
  statusTratamento: TratamentoStatus;
  situacaoSaldo: string | null;
  declaracaoIr: string | null;
  dataDeclaracao: string | null;
  items: BudgetItemRequest[];
}

export const BUDGET_STATUS_LABELS: Record<BudgetStatus, string> = {
  EM_ELABORACAO:        'Em elaboração',
  ENVIADO:              'Enviado',
  AGUARDANDO_APROVACAO: 'Aguardando aprovação',
  APROVADO:             'Aprovado',
  PARCIALMENTE_APROVADO:'Parcialmente aprovado',
  RECUSADO:             'Recusado',
  CANCELADO:            'Cancelado',
  CONCLUIDO:            'Concluído',
};

export const BUDGET_STATUS_CLASS: Record<BudgetStatus, string> = {
  EM_ELABORACAO:        'badge-gray',
  ENVIADO:              'badge-blue',
  AGUARDANDO_APROVACAO: 'badge-orange',
  APROVADO:             'badge-green',
  PARCIALMENTE_APROVADO:'badge-teal',
  RECUSADO:             'badge-red',
  CANCELADO:            'badge-red',
  CONCLUIDO:            'badge-green',
};

export const BUDGET_STATUSES: BudgetStatus[] = [
  'EM_ELABORACAO','ENVIADO','AGUARDANDO_APROVACAO','APROVADO',
  'PARCIALMENTE_APROVADO','RECUSADO','CANCELADO','CONCLUIDO'
];

export const TRATAMENTO_STATUS_LABELS: Record<TratamentoStatus, string> = {
  NAO_INICIADO: 'Não iniciado',
  EM_ANDAMENTO: 'Em andamento',
  FINALIZADO:   'Finalizado',
};

export const TRATAMENTO_STATUS_CLASS: Record<TratamentoStatus, string> = {
  NAO_INICIADO: 'badge-gray',
  EM_ANDAMENTO: 'badge-blue',
  FINALIZADO:   'badge-green',
};

export const FORMAS_PAGAMENTO = [
  'Cartão de débito', 'Cartão de crédito', 'Dinheiro', 'Pix', 'Cheque', 'Serviço/itens'
];
