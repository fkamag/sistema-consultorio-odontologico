import { Component } from '@angular/core';
import { Toast, ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss']
})
export class ToastComponent {
  toasts$ = this.toastService.toasts$;

  constructor(private toastService: ToastService) {}

  fechar(toast: Toast): void {
    this.toastService.remove(toast.id);
  }

  trackById(_: number, toast: Toast): number {
    return toast.id;
  }
}
