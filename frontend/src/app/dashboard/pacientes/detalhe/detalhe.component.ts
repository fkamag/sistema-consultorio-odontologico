import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Paciente } from '../models/paciente.models';
import {
  MedicalHistoryResponse, ClinicalNoteResponse, TreatmentPlanResponse,
  NoteType, PlanStatus, NOTE_TYPE_LABELS, PLAN_STATUS_LABELS, PLAN_STATUSES,
  MedicalHistoryRequest, ClinicalNoteRequest, TreatmentPlanRequest
} from '../models/prontuario.models';
import { PacienteService } from '../services/paciente.service';
import { ProntuarioService } from '../services/prontuario.service';
import { ToastService } from '../../../shared/services/toast.service';

type Aba = 'anamnese' | 'evolucao' | 'plano';

@Component({
  selector: 'app-pacientes-detalhe',
  templateUrl: './detalhe.component.html',
  styleUrls: ['./detalhe.component.scss']
})
export class DetalheComponent implements OnInit {

  patientId = '';
  paciente: Paciente | null = null;
  abaAtiva: Aba = 'anamnese';
  loading = false;

  // Anamnese
  anamneses: MedicalHistoryResponse[] = [];
  modalAnamnese = false;
  anamneseForm!: FormGroup;
  salvandoAnamnese = false;

  // Evolução Clínica
  notas: ClinicalNoteResponse[] = [];
  modalNota = false;
  notaForm!: FormGroup;
  salvandoNota = false;

  // Plano de Tratamento
  plano: TreatmentPlanResponse[] = [];
  modalPlano = false;
  planoForm!: FormGroup;
  salvandoPlano = false;

  readonly noteTypeLabels = NOTE_TYPE_LABELS;
  readonly planStatusLabels = PLAN_STATUS_LABELS;
  readonly planStatuses = PLAN_STATUSES;
  readonly noteTypes: NoteType[] = ['CONSULTA', 'PROCEDIMENTO', 'RETORNO', 'OBSERVACAO', 'INTERCORRENCIA'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private pacienteService: PacienteService,
    private prontuarioService: ProntuarioService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.patientId = this.route.snapshot.paramMap.get('id') ?? '';
    this.buildForms();
    this.carregarPaciente();
    this.carregarAba('anamnese');
  }

  private buildForms(): void {
    this.anamneseForm = this.fb.group({
      alergias:          [''],
      medicamentos:      [''],
      diabetes:          [false],
      hipertensao:       [false],
      problemasCardiacos:[false],
      tratamentosMedicos:[''],
      cirurgias:         [''],
      observacoes:       [''],
    });

    const hoje = new Date().toISOString().split('T')[0];
    this.notaForm = this.fb.group({
      noteDate:    [hoje, Validators.required],
      noteType:    ['CONSULTA', Validators.required],
      tooth:       [''],
      description: ['', Validators.required],
    });

    this.planoForm = this.fb.group({
      description:    ['', Validators.required],
      tooth:          [''],
      valueEstimated: [null],
    });
  }

  private carregarPaciente(): void {
    this.pacienteService.findById(this.patientId).subscribe({
      next: p => this.paciente = p,
      error: () => {
        this.toast.error('Paciente não encontrado.');
        this.router.navigate(['/dashboard/pacientes']);
      }
    });
  }

  mudarAba(aba: Aba): void {
    this.abaAtiva = aba;
    this.carregarAba(aba);
  }

  private carregarAba(aba: Aba): void {
    this.loading = true;
    if (aba === 'anamnese') {
      this.prontuarioService.listAnamneses(this.patientId).subscribe({
        next: r => { this.anamneses = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar anamnese.'); this.loading = false; }
      });
    } else if (aba === 'evolucao') {
      this.prontuarioService.listNotes(this.patientId).subscribe({
        next: r => { this.notas = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar evoluções.'); this.loading = false; }
      });
    } else {
      this.prontuarioService.listPlan(this.patientId).subscribe({
        next: r => { this.plano = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar plano.'); this.loading = false; }
      });
    }
  }

  // ── Anamnese ──────────────────────────────────────────────────────────────

  abrirModalAnamnese(): void {
    const last = this.anamneses[0];
    this.anamneseForm.patchValue({
      alergias:           last?.alergias          ?? '',
      medicamentos:       last?.medicamentos       ?? '',
      diabetes:           last?.diabetes           ?? false,
      hipertensao:        last?.hipertensao        ?? false,
      problemasCardiacos: last?.problemasCardiacos ?? false,
      tratamentosMedicos: last?.tratamentosMedicos ?? '',
      cirurgias:          last?.cirurgias          ?? '',
      observacoes:        last?.observacoes        ?? '',
    });
    this.modalAnamnese = true;
  }

  salvarAnamnese(): void {
    if (this.anamneseForm.invalid) return;
    this.salvandoAnamnese = true;
    const v = this.anamneseForm.value;
    const req: MedicalHistoryRequest = {
      alergias:           v.alergias           || null,
      medicamentos:       v.medicamentos       || null,
      diabetes:           !!v.diabetes,
      hipertensao:        !!v.hipertensao,
      problemasCardiacos: !!v.problemasCardiacos,
      tratamentosMedicos: v.tratamentosMedicos || null,
      cirurgias:          v.cirurgias          || null,
      observacoes:        v.observacoes        || null,
    };
    this.prontuarioService.createAnamnese(this.patientId, req).subscribe({
      next: r => {
        this.anamneses = [r, ...this.anamneses];
        this.modalAnamnese = false;
        this.salvandoAnamnese = false;
        this.toast.success('Anamnese registrada.');
      },
      error: () => { this.toast.error('Erro ao salvar anamnese.'); this.salvandoAnamnese = false; }
    });
  }

  // ── Evolução Clínica ──────────────────────────────────────────────────────

  abrirModalNota(): void {
    const hoje = new Date().toISOString().split('T')[0];
    this.notaForm.patchValue({ noteDate: hoje, noteType: 'CONSULTA', tooth: '', description: '' });
    this.modalNota = true;
  }

  salvarNota(): void {
    if (this.notaForm.invalid) return;
    this.salvandoNota = true;
    const v = this.notaForm.value;
    const req: ClinicalNoteRequest = {
      noteDate:    v.noteDate,
      noteType:    v.noteType,
      tooth:       v.tooth || null,
      description: v.description,
      appointmentId: null,
    };
    this.prontuarioService.createNote(this.patientId, req).subscribe({
      next: r => {
        this.notas = [r, ...this.notas];
        this.modalNota = false;
        this.salvandoNota = false;
        this.toast.success('Evolução registrada.');
      },
      error: () => { this.toast.error('Erro ao salvar evolução.'); this.salvandoNota = false; }
    });
  }

  excluirNota(nota: ClinicalNoteResponse): void {
    if (!confirm(`Remover esta evolução de ${this.formatDate(nota.noteDate)}?`)) return;
    this.prontuarioService.deleteNote(this.patientId, nota.id).subscribe({
      next: () => {
        this.notas = this.notas.filter(n => n.id !== nota.id);
        this.toast.success('Evolução removida.');
      },
      error: () => this.toast.error('Erro ao remover evolução.')
    });
  }

  // ── Plano de Tratamento ───────────────────────────────────────────────────

  abrirModalPlano(): void {
    this.planoForm.reset({ description: '', tooth: '', valueEstimated: null });
    this.modalPlano = true;
  }

  salvarPlano(): void {
    if (this.planoForm.invalid) return;
    this.salvandoPlano = true;
    const v = this.planoForm.value;
    const req: TreatmentPlanRequest = {
      description:    v.description,
      tooth:          v.tooth || null,
      valueEstimated: v.valueEstimated ?? null,
    };
    this.prontuarioService.addPlanItem(this.patientId, req).subscribe({
      next: r => {
        this.plano = [r, ...this.plano];
        this.modalPlano = false;
        this.salvandoPlano = false;
        this.toast.success('Item adicionado ao plano.');
      },
      error: () => { this.toast.error('Erro ao adicionar item.'); this.salvandoPlano = false; }
    });
  }

  alterarStatus(item: TreatmentPlanResponse, status: PlanStatus): void {
    this.prontuarioService.updatePlanStatus(this.patientId, item.id, status).subscribe({
      next: r => {
        const idx = this.plano.findIndex(i => i.id === r.id);
        if (idx >= 0) this.plano[idx] = r;
        this.toast.success('Status atualizado.');
      },
      error: () => this.toast.error('Erro ao atualizar status.')
    });
  }

  excluirItemPlano(item: TreatmentPlanResponse): void {
    if (!confirm(`Remover "${item.description}" do plano?`)) return;
    this.prontuarioService.deletePlanItem(this.patientId, item.id).subscribe({
      next: () => {
        this.plano = this.plano.filter(i => i.id !== item.id);
        this.toast.success('Item removido.');
      },
      error: () => this.toast.error('Erro ao remover item.')
    });
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  voltar(): void { this.router.navigate(['/dashboard/pacientes']); }

  formatDate(iso: string | null): string {
    if (!iso) return '—';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }

  formatDateTime(iso: string | null): string {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatBRL(val: number | null): string {
    if (val == null) return '—';
    return val.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  statusClass(s: PlanStatus): string {
    const map: Record<PlanStatus, string> = {
      PLANEJADO: 'badge-gray',
      APROVADO: 'badge-blue',
      PARCIALMENTE_APROVADO: 'badge-orange',
      REALIZADO: 'badge-green',
      CANCELADO: 'badge-red',
      PENDENTE: 'badge-yellow',
    };
    return map[s] ?? 'badge-gray';
  }
}
