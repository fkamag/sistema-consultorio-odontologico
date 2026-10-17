CREATE TABLE appointments (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    patient_id       UUID         NOT NULL REFERENCES patients(id),
    dentist_id       UUID         NOT NULL REFERENCES app_users(id),
    start_time       TIMESTAMP    NOT NULL,
    duration_minutes INT          NOT NULL DEFAULT 30,
    status           VARCHAR(20)  NOT NULL DEFAULT 'AGENDADO',
    notes            TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by_id    UUID,
    created_by_name  VARCHAR(150),
    updated_by_id    UUID,
    updated_by_name  VARCHAR(150)
);

CREATE INDEX idx_appointments_start_time  ON appointments(start_time);
CREATE INDEX idx_appointments_dentist_id  ON appointments(dentist_id);
CREATE INDEX idx_appointments_patient_id  ON appointments(patient_id);
CREATE INDEX idx_appointments_status      ON appointments(status);
