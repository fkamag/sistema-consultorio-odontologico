CREATE TABLE clinical_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    clinical_note_id UUID REFERENCES clinical_notes(id) ON DELETE SET NULL,
    object_name VARCHAR(500) NOT NULL,
    caption VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_id UUID,
    created_by_name VARCHAR(150)
);
