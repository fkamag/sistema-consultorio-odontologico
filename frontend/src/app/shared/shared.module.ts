import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RoleLabelPipe } from './pipes/role-label.pipe';
import { MaskDirective } from './directives/mask.directive';
import { ToastComponent } from './components/toast/toast.component';

@NgModule({
  declarations: [RoleLabelPipe, MaskDirective, ToastComponent],
  imports: [CommonModule],
  exports: [RoleLabelPipe, MaskDirective, ToastComponent]
})
export class SharedModule {}
