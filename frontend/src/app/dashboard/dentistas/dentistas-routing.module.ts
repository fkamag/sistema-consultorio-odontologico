import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DentistasListaComponent } from './lista/lista.component';

const routes: Routes = [
  { path: '', component: DentistasListaComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DentistasRoutingModule {}
