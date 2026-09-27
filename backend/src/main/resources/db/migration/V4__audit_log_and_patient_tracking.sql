CREATE TABLE audit_log (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID,
    user_name   VARCHAR(150) NOT NULL,
    action      VARCHAR(20)  NOT NULL,
    entity      VARCHAR(50)  NOT NULL,
    entity_id   UUID,
    details     TEXT,
    ip          VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity    ON audit_log (entity, entity_id);
CREATE INDEX idx_audit_log_user_id   ON audit_log (user_id);
CREATE INDEX idx_audit_log_created   ON audit_log (created_at DESC);

ALTER TABLE patients
    ADD COLUMN created_by_id   UUID,
    ADD COLUMN created_by_name VARCHAR(150),
    ADD COLUMN updated_by_id   UUID,
    ADD COLUMN updated_by_name VARCHAR(150);
