import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RoleLabelPipe } from './pipes/role-label.pipe';
import { MaskDirective } from './directives/mask.directive';

@NgModule({
  declarations: [RoleLabelPipe, MaskDirective],
  imports: [CommonModule],
  exports: [RoleLabelPipe, MaskDirective]
})
export class SharedModule {}
