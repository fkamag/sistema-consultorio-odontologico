import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { Subject } from 'rxjs';
import {
  BudgetStatus, TratamentoStatus, BudgetRequest,
  BUDGET_STATUSES, BUDGET_STATUS_LABELS,
  FORMAS_PAGAMENTO
} from '../models/orcamento.models';
import { BudgetResponse } from '../../pacientes/models/budget.models';
import { OrcamentoService } from '../services/orcamento.service';
import { PacienteService } from '../../pacientes/services/paciente.service';
import { ToastService } from '../../../shared/services/toast.service';
import { Paciente } from '../../pacientes/models/paciente.models';

@Component({
  selector: 'app-orcamento-formulario',
  templateUrl: './formulario.component.html',
  styleUrls: ['./formulario.component.scss']
})
export class OrcamentoFormularioComponent implements OnInit {
  @Input() orcamento: BudgetResponse | null = null;
  @Input() pacienteFixo: Paciente | null = null;
  @Output() saved = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  form!: FormGroup;
  loading = false;
  buscandoPacientes = false;
  pacientes: Paciente[] = [];
  pacienteSelecionado: Paciente | null = null;
  showPacienteDropdown = false;
  private buscaSubject = new Subject<string>();

  readonly statusOpcoes: BudgetStatus[] = BUDGET_STATUSES;
  readonly statusLabels: Record<BudgetStatus, string> = BUDGET_STATUS_LABELS;
  readonly tratamentoOpcoes: TratamentoStatus[] = ['NAO_INICIADO', 'EM_ANDAMENTO', 'FINALIZADO'];
  readonly tratamentoLabels: Record<TratamentoStatus, string> = {
    NAO_INICIADO: 'Não iniciado', EM_ANDAMENTO: 'Em andamento', FINALIZADO: 'Finalizado'
  };
  readonly formasPagamento = FORMAS_PAGAMENTO;

  constructor(
    private fb: FormBuilder,
    private service: OrcamentoService,
    private pacienteService: PacienteService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const o = this.orcamento;
    this.form = this.fb.group({
      pacienteBusca: [''],
      descricaoItens: [o?.descricaoItens || '', Validators.required],
      totalValue: [o?.totalValue ?? '', [Validators.required, Validators.min(0.01)]],
      validUntil: [o?.validUntil ? o.validUntil.substring(0, 10) : this.defaultValidade()],
      status: [o?.status || 'EM_ELABORACAO', Validators.required],
      statusTratamento: [o?.statusTratamento || 'NAO_INICIADO', Validators.required],
      situacaoSaldo: [o?.situacaoSaldo || ''],
      declaracaoIr: [o?.declaracaoIr || 'Em branco'],
      dataDeclaracao: [o?.dataDeclaracao ? o.dataDeclaracao.substring(0, 10) : this.today()],
    });

    if (o) {
      this.pacienteSelecionado = { id: o.patientId, name: '' } as Paciente;
    }
    if (this.pacienteFixo) {
      this.pacienteSelecionado = this.pacienteFixo;
    }

    this.buscaSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        this.buscandoPacientes = true;
        return this.pacienteService.list(term, 0, 10);
      })
    ).subscribe({
      next: res => { this.pacientes = res.content; this.buscandoPacientes = false; this.showPacienteDropdown = true; },
      error: () => { this.buscandoPacientes = false; }
    });
  }

  buscarPaciente(term: string): void {
    if (term.length >= 2) this.buscaSubject.next(term);
    else { this.pacientes = []; this.showPacienteDropdown = false; }
  }

  selecionarPaciente(p: Paciente): void {
    this.pacienteSelecionado = p;
    this.form.get('pacienteBusca')!.setValue(p.name, { emitEvent: false });
    this.showPacienteDropdown = false;
  }

  salvar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const patientId = this.orcamento?.patientId ?? this.pacienteSelecionado?.id;
    if (!patientId) { this.toast.error('Selecione um paciente.'); return; }

    const v = this.form.value;
    const req: BudgetRequest = {
      dentistId: null,
      descricaoItens: v.descricaoItens,
      notes: null,
      totalValue: Number(v.totalValue),
      validUntil: v.validUntil || null,
      status: v.status,
      statusTratamento: v.statusTratamento,
      situacaoSaldo: v.situacaoSaldo || null,
      declaracaoIr: v.declaracaoIr || null,
      dataDeclaracao: v.dataDeclaracao || null,
      items: []
    };

    this.loading = true;
    const obs = this.orcamento
      ? this.service.update(patientId, this.orcamento.id, req)
      : this.service.create(patientId, req);

    obs.subscribe({
      next: () => { this.loading = false; this.saved.emit(); },
      error: () => { this.toast.error('Erro ao salvar orçamento.'); this.loading = false; }
    });
  }

  private today(): string {
    return new Date().toISOString().substring(0, 10);
  }

  private defaultValidade(): string {
    const d = new Date();
    d.setDate(d.getDate() + 30);
    return d.toISOString().substring(0, 10);
  }

  formatBRL(v: number): string {
    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
