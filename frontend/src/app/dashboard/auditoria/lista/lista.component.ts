import { Component, OnInit } from '@angular/core';
import { debounceTime, Subject } from 'rxjs';
import { AuditLog, PageResponse } from '../models/auditoria.models';
import { AuditoriaService } from '../services/auditoria.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-auditoria-lista',
  templateUrl: './lista.component.html',
  styleUrls: ['./lista.component.scss']
})
export class ListaComponent implements OnInit {

  logs: AuditLog[] = [];
  totalElements = 0;
  page = 0;
  pageSize = 20;
  loading = false;
  searchTerm = '';
  dataInicio = '';
  dataFim = '';
  logSelecionado: AuditLog | null = null;

  private filtro$ = new Subject<void>();

  constructor(
    private auditoriaService: AuditoriaService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.filtro$.pipe(debounceTime(350)).subscribe(() => this.load());
    this.load();
  }

  onSearch(term: string): void {
    this.searchTerm = term;
    this.page = 0;
    this.filtro$.next();
  }

  onFiltroData(): void {
    this.page = 0;
    this.filtro$.next();
  }

  limparFiltros(): void {
    this.searchTerm = '';
    this.dataInicio = '';
    this.dataFim = '';
    this.page = 0;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.auditoriaService.list(
      this.searchTerm, this.page, this.pageSize,
      this.dataInicio || undefined,
      this.dataFim || undefined
    ).subscribe({
      next: res => this.handleResponse(res),
      error: () => this.handleError()
    });
  }

  private handleResponse(res: PageResponse<AuditLog>): void {
    this.logs = res.content;
    this.totalElements = res.totalElements;
    this.loading = false;
  }

  private handleError(): void {
    this.toast.error('Erro ao carregar registros de auditoria.');
    this.loading = false;
  }

  abrirDetalhes(log: AuditLog): void {
    this.logSelecionado = log;
  }

  fecharDetalhes(): void {
    this.logSelecionado = null;
  }

  mudarTamanhoPagina(size: number): void {
    this.pageSize = size;
    this.page = 0;
    this.load();
  }

  proximaPagina(): void { this.page++; this.load(); }
  paginaAnterior(): void { if (this.page > 0) { this.page--; this.load(); } }

  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }
  get primeiroItem(): number { return this.totalElements === 0 ? 0 : this.page * this.pageSize + 1; }
  get ultimoItem(): number { return Math.min((this.page + 1) * this.pageSize, this.totalElements); }

  actionLabel(action: string): string {
    const map: Record<string, string> = {
      CREATE: 'Criação', UPDATE: 'Edição', DELETE: 'Exclusão',
      LOGIN: 'Login', LOGOUT: 'Logout'
    };
    return map[action] ?? action;
  }

  entityLabel(entity: string): string {
    const map: Record<string, string> = { PATIENT: 'Paciente', USER: 'Usuário' };
    return map[entity] ?? entity;
  }

  formatDateTime(iso: string): string {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
