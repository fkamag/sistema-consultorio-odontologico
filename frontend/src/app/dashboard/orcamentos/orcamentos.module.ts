import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { OrcamentosRoutingModule } from './orcamentos-routing.module';
import { OrcamentosListaComponent } from './lista/lista.component';
import { OrcamentoFormularioComponent } from './formulario/formulario.component';
import { OrcamentoPagamentosComponent } from './pagamentos/pagamentos.component';

@NgModule({
  declarations: [
    OrcamentosListaComponent,
    OrcamentoFormularioComponent,
    OrcamentoPagamentosComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    OrcamentosRoutingModule
  ]
})
export class OrcamentosModule {}
