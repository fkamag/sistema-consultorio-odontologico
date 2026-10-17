import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AgendaRoutingModule } from './agenda-routing.module';
import { CalendarioComponent } from './calendario/calendario.component';
import { FormularioComponent } from './formulario/formulario.component';

@NgModule({
  declarations: [
    CalendarioComponent,
    FormularioComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    AgendaRoutingModule
  ]
})
export class AgendaModule {}
