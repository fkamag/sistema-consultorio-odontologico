import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BudgetPayment, FORMAS_PAGAMENTO } from '../models/orcamento.models';
import { OrcamentoSummary } from '../models/orcamento.models';
import { OrcamentoService } from '../services/orcamento.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-orcamento-pagamentos',
  templateUrl: './pagamentos.component.html',
  styleUrls: ['./pagamentos.component.scss']
})
export class OrcamentoPagamentosComponent implements OnInit {
  @Input() orcamento!: OrcamentoSummary;
  @Output() close = new EventEmitter<void>();
  @Output() atualizado = new EventEmitter<void>();

  form!: FormGroup;
  pagamentos: BudgetPayment[] = [];
  loading = false;
  adicionando = false;

  readonly formasPagamento = FORMAS_PAGAMENTO;

  constructor(
    private fb: FormBuilder,
    private service: OrcamentoService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      data: [this.today(), Validators.required],
      valor: ['', [Validators.required, Validators.min(0.01)]],
      formaPagamento: ['Pix', Validators.required],
      status: ['PAGO', Validators.required]
    });
    this.carregarPagamentos();
  }

  carregarPagamentos(): void {
    this.loading = true;
    this.service.listPayments(this.orcamento.patientId, this.orcamento.id).subscribe({
      next: res => { this.pagamentos = res; this.loading = false; },
      error: () => { this.toast.error('Erro ao carregar pagamentos.'); this.loading = false; }
    });
  }

  adicionar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.value;
    this.adicionando = true;
    this.service.addPayment(this.orcamento.patientId, this.orcamento.id, v).subscribe({
      next: () => {
        this.form.get('valor')!.reset();
        this.adicionando = false;
        this.carregarPagamentos();
        this.atualizado.emit();
      },
      error: () => { this.toast.error('Erro ao adicionar pagamento.'); this.adicionando = false; }
    });
  }

  toggle(pg: BudgetPayment): void {
    this.service.togglePayment(this.orcamento.patientId, this.orcamento.id, pg.id).subscribe({
      next: () => { this.carregarPagamentos(); this.atualizado.emit(); },
      error: () => this.toast.error('Erro ao alterar status.')
    });
  }

  get totalPago(): number {
    return this.pagamentos.filter(p => p.status === 'PAGO').reduce((s, p) => s + p.valor, 0);
  }

  get saldo(): number {
    return Math.max(0, this.orcamento.totalValue - this.totalPago);
  }

  get quitado(): boolean {
    return this.orcamento.totalValue > 0 && this.saldo <= 0;
  }

  private today(): string {
    return new Date().toISOString().substring(0, 10);
  }

  formatBRL(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  formatData(iso: string): string {
    return new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR');
  }
}
