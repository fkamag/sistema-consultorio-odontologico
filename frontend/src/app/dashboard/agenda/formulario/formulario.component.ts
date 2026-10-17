import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Agendamento, AgendamentoRequest } from '../models/agenda.models';
import { AgendaService } from '../services/agenda.service';
import { PacienteService } from '../../pacientes/services/paciente.service';
import { UsuarioService } from '../../usuarios/services/usuario.service';
import { ToastService } from '../../../shared/services/toast.service';
import { Paciente } from '../../pacientes/models/paciente.models';
import { Usuario } from '../../usuarios/models/usuario.models';

export const PROCEDIMENTOS = [
  { label: 'Avaliação',   minutos: 30 },
  { label: 'Limpeza',     minutos: 45 },
  { label: 'Restauração', minutos: 60 },
  { label: 'Cirurgia',    minutos: 90 },
  { label: 'Outro',       minutos: 30 },
];

@Component({
  selector: 'app-agenda-formulario',
  templateUrl: './formulario.component.html',
  styleUrls: ['./formulario.component.scss']
})
export class FormularioComponent implements OnInit {

  @Input() agendamento: Agendamento | null = null;
  @Input() preData: Date | null = null;
  @Input() preDentistId: string | null = null;
  @Output() salvo  = new EventEmitter<Agendamento>();
  @Output() fechou = new EventEmitter<void>();

  form!: FormGroup;
  salvando = false;
  pacientes: Paciente[] = [];
  dentistas: Usuario[] = [];
  procedimentos = PROCEDIMENTOS;

  // Busca autocomplete de paciente
  buscaPaciente = '';
  pacienteSugestoes: Paciente[] = [];
  pacienteSelecionado: Paciente | null = null;
  sugestoesVisiveis = false;

  constructor(
    private fb: FormBuilder,
    private agendaService: AgendaService,
    private pacienteService: PacienteService,
    private usuarioService: UsuarioService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const duracao = this.agendamento?.durationMinutes ?? 30;
    this.form = this.fb.group({
      patientId:       [this.agendamento?.patientId  ?? '', Validators.required],
      dentistId:       [this.agendamento?.dentistId  ?? this.preDentistId ?? '', Validators.required],
      startTime:       [this.agendamento ? this.toInputValue(this.agendamento.startTime)
                                         : this.toInputValue(this.preData?.toISOString() ?? ''), Validators.required],
      durationMinutes: [duracao, [Validators.required, Validators.min(10), Validators.max(480)]],
      notes:           [this.agendamento?.notes ?? '']
    });

    this.carregarDados();
  }

  private carregarDados(): void {
    this.pacienteService.list('', 0, 500).subscribe({
      next: res => {
        this.pacientes = res.content;
        if (this.agendamento?.patientId) {
          this.pacienteSelecionado = this.pacientes.find(p => p.id === this.agendamento!.patientId) ?? null;
          if (this.pacienteSelecionado) this.buscaPaciente = this.pacienteSelecionado.name;
        }
      }
    });
    this.usuarioService.list('', 0, 100).subscribe({
      next: res => this.dentistas = res.content.filter(u => u.role === 'DENTISTA' && u.active)
    });
  }

  // ── Autocomplete de paciente ─────────────────────────────────────────────

  onBuscaInput(): void {
    const q = this.buscaPaciente.trim().toLowerCase();
    if (q.length < 2) {
      this.pacienteSugestoes = [];
      this.sugestoesVisiveis = false;
      return;
    }
    this.pacienteSugestoes = this.pacientes
      .filter(p => p.name.toLowerCase().includes(q))
      .slice(0, 8);
    this.sugestoesVisiveis = this.pacienteSugestoes.length > 0;
  }

  selecionarPaciente(p: Paciente): void {
    this.pacienteSelecionado = p;
    this.buscaPaciente = p.name;
    this.sugestoesVisiveis = false;
    this.form.patchValue({ patientId: p.id });
  }

  limparPaciente(): void {
    this.pacienteSelecionado = null;
    this.buscaPaciente = '';
    this.sugestoesVisiveis = false;
    this.form.patchValue({ patientId: '' });
  }

  onBuscaBlur(): void {
    // Aguarda um tick para que o clique na sugestão seja processado primeiro
    setTimeout(() => { this.sugestoesVisiveis = false; }, 150);
  }

  // ── Procedimento → preenche duração ──────────────────────────────────────

  onProcedimentoChange(event: Event): void {
    const label = (event.target as HTMLSelectElement).value;
    const proc = this.procedimentos.find(p => p.label === label);
    if (proc) this.form.patchValue({ durationMinutes: proc.minutos });
  }

  get procedimentoAtual(): string {
    const dur = this.form.value.durationMinutes;
    return this.procedimentos.find(p => p.minutos === dur)?.label ?? 'Outro';
  }

  // ── Salvar ───────────────────────────────────────────────────────────────

  salvar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.salvando = true;

    const v = this.form.value;
    const request: AgendamentoRequest = {
      patientId:       v.patientId,
      dentistId:       v.dentistId,
      startTime:       v.startTime,
      durationMinutes: v.durationMinutes,
      notes:           v.notes || null
    };

    const op$ = this.agendamento
      ? this.agendaService.update(this.agendamento.id, request)
      : this.agendaService.create(request);

    op$.subscribe({
      next: saved => {
        this.toast.success(this.agendamento ? 'Agendamento atualizado.' : 'Agendamento criado.');
        this.salvo.emit(saved);
        this.salvando = false;
      },
      error: err => {
        const msg = err?.error?.message ?? 'Erro ao salvar agendamento.';
        this.toast.error(msg);
        this.salvando = false;
      }
    });
  }

  fechar(): void { this.fechou.emit(); }

  get modoEdicao(): boolean { return !!this.agendamento; }

  get telefonePaciente(): string {
    return this.pacienteSelecionado?.phone ?? '—';
  }

  private toInputValue(iso: string): string {
    if (!iso) return '';
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}
