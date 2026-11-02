import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  OrcamentoSummary, BudgetStatus,
  BUDGET_STATUS_LABELS, BUDGET_STATUS_CLASS, BUDGET_STATUSES,
  TRATAMENTO_STATUS_LABELS, TRATAMENTO_STATUS_CLASS, TratamentoStatus
} from '../models/orcamento.models';
import { BudgetResponse } from '../../pacientes/models/budget.models';
import { OrcamentoService } from '../services/orcamento.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-orcamentos-lista',
  templateUrl: './lista.component.html',
  styleUrls: ['./lista.component.scss']
})
export class OrcamentosListaComponent implements OnInit {

  orcamentos: OrcamentoSummary[] = [];
  totalElements = 0;
  page = 0;
  pageSize = 20;
  loading = false;
  statusFiltro = '';

  modalAberto: 'formulario' | 'pagamentos' | null = null;
  orcamentoSelecionado: OrcamentoSummary | null = null;
  orcamentoEditando: BudgetResponse | null = null;
  carregandoOrcamento = false;

  readonly statusOpcoes: BudgetStatus[] = BUDGET_STATUSES;
  readonly statusLabels: Record<BudgetStatus, string> = BUDGET_STATUS_LABELS;
  readonly statusClass: Record<BudgetStatus, string> = BUDGET_STATUS_CLASS;
  readonly tratamentoLabels: Record<TratamentoStatus, string> = TRATAMENTO_STATUS_LABELS;
  readonly tratamentoClass: Record<TratamentoStatus, string> = TRATAMENTO_STATUS_CLASS;

  constructor(
    private service: OrcamentoService,
    private toast: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listAll(this.statusFiltro || undefined, this.page, this.pageSize).subscribe({
      next: res => { this.orcamentos = res.content; this.totalElements = res.totalElements; this.loading = false; },
      error: () => { this.toast.error('Erro ao carregar orçamentos.'); this.loading = false; }
    });
  }

  filtrar(): void { this.page = 0; this.load(); }

  novoOrcamento(): void {
    this.orcamentoEditando = null;
    this.orcamentoSelecionado = null;
    this.modalAberto = 'formulario';
  }

  abrirOrcamento(o: OrcamentoSummary): void {
    this.orcamentoSelecionado = o;
    this.carregandoOrcamento = true;
    this.modalAberto = 'formulario';
    this.service.findById(o.patientId, o.id).subscribe({
      next: res => { this.orcamentoEditando = res; this.carregandoOrcamento = false; },
      error: () => { this.toast.error('Erro ao carregar orçamento.'); this.carregandoOrcamento = false; this.modalAberto = null; }
    });
  }

  abrirPagamentos(o: OrcamentoSummary, event: Event): void {
    event.stopPropagation();
    this.orcamentoSelecionado = o;
    this.modalAberto = 'pagamentos';
  }

  fecharModal(): void { this.modalAberto = null; this.orcamentoEditando = null; this.orcamentoSelecionado = null; }

  onSaved(): void { this.fecharModal(); this.load(); }

  onPagamentosAtualizado(): void { this.load(); }

  proximaPagina(): void { this.page++; this.load(); }
  paginaAnterior(): void { if (this.page > 0) { this.page--; this.load(); } }
  mudarTamanhoPagina(size: number): void { this.pageSize = size; this.page = 0; this.load(); }

  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }
  get primeiroItem(): number { return this.totalElements === 0 ? 0 : this.page * this.pageSize + 1; }
  get ultimoItem(): number { return Math.min((this.page + 1) * this.pageSize, this.totalElements); }

  formatBRL(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
