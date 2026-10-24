ALTER TABLE patients ADD COLUMN IF NOT EXISTS convenio VARCHAR(150);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS responsible_dentist_id UUID;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS responsible_dentist_name VARCHAR(150);

CREATE TABLE tooth_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    tooth_number VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_id UUID,
    created_by_name VARCHAR(150),
    updated_by_id UUID,
    updated_by_name VARCHAR(150),
    UNIQUE (patient_id, tooth_number)
);

CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    dentist_id UUID,
    dentist_name VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO',
    notes TEXT,
    valid_until DATE,
    total_value NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_id UUID,
    created_by_name VARCHAR(150),
    updated_by_id UUID,
    updated_by_name VARCHAR(150)
);

CREATE TABLE budget_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_id UUID NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    description VARCHAR(300) NOT NULL,
    tooth VARCHAR(3),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_value NUMERIC(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    signed_at TIMESTAMP,
    signed_by_name VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_id UUID,
    created_by_name VARCHAR(150)
);
