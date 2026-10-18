import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListaComponent } from './lista/lista.component';
import { DetalheComponent } from './detalhe/detalhe.component';

const routes: Routes = [
  { path: '', component: ListaComponent },
  { path: ':id', component: DetalheComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PacientesRoutingModule {}
