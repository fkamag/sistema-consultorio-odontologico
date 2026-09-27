import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error';
}

@Injectable({ providedIn: 'root' })
export class ToastService {

  private counter = 0;
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  readonly toasts$ = this.toastsSubject.asObservable();

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  private show(message: string, type: 'success' | 'error'): void {
    const id = ++this.counter;
    const current = this.toastsSubject.getValue();
    this.toastsSubject.next([...current, { id, message, type }]);
    setTimeout(() => this.remove(id), 3500);
  }

  remove(id: number): void {
    const current = this.toastsSubject.getValue();
    this.toastsSubject.next(current.filter(t => t.id !== id));
  }
}
