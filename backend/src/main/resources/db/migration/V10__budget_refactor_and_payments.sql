-- Atualizar status existentes para os novos valores antes de alterar o tipo
UPDATE budgets SET status = 'APROVADO'       WHERE status = 'APROVADO';
UPDATE budgets SET status = 'EM_ELABORACAO'  WHERE status = 'RASCUNHO';
UPDATE budgets SET status = 'ENVIADO'        WHERE status = 'ENVIADO';
UPDATE budgets SET status = 'RECUSADO'       WHERE status = 'REPROVADO';
UPDATE budgets SET status = 'CANCELADO'      WHERE status = 'EXPIRADO';

-- Novos campos no orçamento
ALTER TABLE budgets
    ADD COLUMN IF NOT EXISTS descricao_itens        TEXT,
    ADD COLUMN IF NOT EXISTS status_tratamento      VARCHAR(30) NOT NULL DEFAULT 'NAO_INICIADO',
    ADD COLUMN IF NOT EXISTS situacao_saldo         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS declaracao_ir          VARCHAR(50),
    ADD COLUMN IF NOT EXISTS data_declaracao        DATE;

-- Tabela de pagamentos vinculados ao orçamento
CREATE TABLE IF NOT EXISTS budget_payments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_id        UUID         NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    patient_id       UUID         NOT NULL,
    data             DATE         NOT NULL,
    valor            NUMERIC(12,2) NOT NULL,
    forma_pagamento  VARCHAR(50)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PAGO',
    created_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_budget_payments_budget  ON budget_payments(budget_id);
CREATE INDEX IF NOT EXISTS idx_budget_payments_patient ON budget_payments(patient_id);
