export interface TimelineEvent {
  type: string;
  date: string;
  description: string;
  detail: string | null;
  createdByName: string | null;
}
