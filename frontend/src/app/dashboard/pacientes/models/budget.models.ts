export type BudgetStatus = 'RASCUNHO' | 'ENVIADO' | 'APROVADO' | 'REPROVADO' | 'EXPIRADO';

export interface BudgetItem {
  id: string;
  budgetId: string;
  description: string;
  tooth: string | null;
  quantity: number;
  unitValue: number;
}

export interface BudgetResponse {
  id: string;
  patientId: string;
  dentistId: string | null;
  dentistName: string | null;
  status: BudgetStatus;
  notes: string | null;
  validUntil: string | null;
  totalValue: number;
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
  notes: string | null;
  totalValue?: number;
  validUntil: string | null;
  items: BudgetItemRequest[];
}

export const BUDGET_STATUS_LABELS: Record<BudgetStatus, string> = {
  RASCUNHO: 'Rascunho',
  ENVIADO:  'Enviado',
  APROVADO: 'Aprovado',
  REPROVADO:'Reprovado',
  EXPIRADO: 'Expirado',
};

export const BUDGET_STATUS_CLASS: Record<BudgetStatus, string> = {
  RASCUNHO: 'badge-gray',
  ENVIADO:  'badge-blue',
  APROVADO: 'badge-green',
  REPROVADO:'badge-red',
  EXPIRADO: 'badge-orange',
};

export const BUDGET_STATUSES: BudgetStatus[] = ['RASCUNHO','ENVIADO','APROVADO','REPROVADO','EXPIRADO'];
