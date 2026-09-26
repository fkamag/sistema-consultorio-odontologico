CREATE TABLE patients (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    cpf          VARCHAR(14)  NOT NULL UNIQUE,
    birth_date   DATE,
    phone        VARCHAR(20),
    email        VARCHAR(150),
    street       VARCHAR(200),
    number       VARCHAR(20),
    complement   VARCHAR(100),
    neighborhood VARCHAR(100),
    city         VARCHAR(100),
    state        VARCHAR(2),
    notes        TEXT,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMP
);

CREATE INDEX idx_patients_cpf    ON patients (cpf);
CREATE INDEX idx_patients_name   ON patients (name);
CREATE INDEX idx_patients_active ON patients (active) WHERE deleted_at IS NULL;
