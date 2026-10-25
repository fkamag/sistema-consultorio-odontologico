import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { DentistasRoutingModule } from './dentistas-routing.module';
import { DentistasListaComponent } from './lista/lista.component';
import { DentistaFormularioComponent } from './formulario/formulario.component';

@NgModule({
  declarations: [
    DentistasListaComponent,
    DentistaFormularioComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DentistasRoutingModule
  ]
})
export class DentistasModule {}
