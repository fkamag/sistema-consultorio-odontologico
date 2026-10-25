import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CalendarioComponent } from './calendario/calendario.component';

const routes: Routes = [
  { path: '', component: CalendarioComponent },
  { path: 'grade', component: CalendarioComponent, data: { modo: 'grade' } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AgendaRoutingModule {}
