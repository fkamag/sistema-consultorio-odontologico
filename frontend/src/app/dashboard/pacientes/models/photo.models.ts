export interface ClinicalPhoto {
  id: string;
  patientId: string;
  clinicalNoteId: string | null;
  caption: string | null;
  createdAt: string;
  createdByName: string | null;
}
