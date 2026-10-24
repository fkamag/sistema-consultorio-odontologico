import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DashboardService } from './dashboard.service';
import { DashboardSummary, AppointmentSummary } from './dashboard.models';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  summary: DashboardSummary | null = null;
  loading = true;
  erro = false;

  constructor(
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.dashboardService.getSummary().subscribe({
      next: s => { this.summary = s; this.loading = false; },
      error: () => { this.erro = true; this.loading = false; }
    });
  }

  irParaPaciente(id: string): void {
    this.router.navigate(['/dashboard/pacientes', id]);
  }

  irParaAgenda(): void {
    this.router.navigate(['/dashboard/agenda']);
  }

  formatHora(iso: string): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      AGENDADO:   'badge-yellow',
      CONFIRMADO: 'badge-green',
      REALIZADO:  'badge-blue',
      CANCELADO:  'badge-red',
    };
    return map[status] ?? 'badge-gray';
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      AGENDADO:   'Agendado',
      CONFIRMADO: 'Confirmado',
      REALIZADO:  'Realizado',
      CANCELADO:  'Cancelado',
    };
    return map[status] ?? status;
  }
}
