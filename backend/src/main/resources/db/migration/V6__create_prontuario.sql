-- Anamnese (versionada por paciente)
CREATE TABLE medical_histories (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    patient_id      UUID        NOT NULL REFERENCES patients(id),
    version_number  INT         NOT NULL DEFAULT 1,
    alergias        TEXT,
    medicamentos    TEXT,
    diabetes        BOOLEAN     NOT NULL DEFAULT FALSE,
    hipertensao     BOOLEAN     NOT NULL DEFAULT FALSE,
    problemas_cardiacos BOOLEAN NOT NULL DEFAULT FALSE,
    tratamentos_medicos TEXT,
    cirurgias       TEXT,
    observacoes     TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by_id   UUID,
    created_by_name VARCHAR(150)
);
CREATE INDEX idx_medical_histories_patient ON medical_histories(patient_id);

-- Evolução clínica
CREATE TABLE clinical_notes (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    patient_id      UUID        NOT NULL REFERENCES patients(id),
    dentist_id      UUID        REFERENCES app_users(id),
    appointment_id  UUID        REFERENCES appointments(id),
    note_date       DATE        NOT NULL,
    note_type       VARCHAR(30) NOT NULL DEFAULT 'Consulta',
    tooth           VARCHAR(10),
    description     TEXT        NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by_id   UUID,
    created_by_name VARCHAR(150)
);
CREATE INDEX idx_clinical_notes_patient ON clinical_notes(patient_id);
CREATE INDEX idx_clinical_notes_date    ON clinical_notes(note_date);

-- Plano de tratamento
CREATE TABLE treatment_plan_items (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    patient_id      UUID        NOT NULL REFERENCES patients(id),
    description     VARCHAR(300) NOT NULL,
    tooth           VARCHAR(10),
    value_estimated NUMERIC(10,2),
    status          VARCHAR(30) NOT NULL DEFAULT 'PLANEJADO',
    appointment_id  UUID        REFERENCES appointments(id),
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by_id   UUID,
    created_by_name VARCHAR(150),
    updated_by_id   UUID,
    updated_by_name VARCHAR(150)
);
CREATE INDEX idx_treatment_plan_patient ON treatment_plan_items(patient_id);
