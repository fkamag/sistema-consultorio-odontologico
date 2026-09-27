import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AuditoriaRoutingModule } from './auditoria-routing.module';
import { ListaComponent } from './lista/lista.component';

@NgModule({
  declarations: [ListaComponent],
  imports: [
    CommonModule,
    FormsModule,
    AuditoriaRoutingModule
  ]
})
export class AuditoriaModule {}
