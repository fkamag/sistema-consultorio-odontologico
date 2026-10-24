import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Paciente } from '../models/paciente.models';
import {
  MedicalHistoryResponse, ClinicalNoteResponse, TreatmentPlanResponse,
  NoteType, PlanStatus, NOTE_TYPE_LABELS, PLAN_STATUS_LABELS, PLAN_STATUSES,
  MedicalHistoryRequest, ClinicalNoteRequest, TreatmentPlanRequest
} from '../models/prontuario.models';
import {
  ToothRecord, ToothRecordRequest, FaceStatus,
  UPPER_TEETH, LOWER_TEETH, FACE_FILL, FACE_FILL_OPACITY, FACE_STROKE, FACE_LABEL, emptyRecord, cycleFace, mesialOnRight
} from '../models/odontogram.models';
import {
  BudgetResponse, BudgetRequest, BudgetItemRequest, BudgetStatus,
  BUDGET_STATUS_LABELS, BUDGET_STATUS_CLASS, BUDGET_STATUSES
} from '../models/budget.models';
import {
  ConsentResponse, ConsentRequest, ConsentType,
  CONSENT_TYPE_LABELS, CONSENT_TYPES
} from '../models/consent.models';
import { TimelineEvent } from '../models/timeline.models';
import { ClinicalPhoto } from '../models/photo.models';
import { PacienteService } from '../services/paciente.service';
import { ProntuarioService } from '../services/prontuario.service';
import { OdontogramService } from '../services/odontogram.service';
import { BudgetService } from '../services/budget.service';
import { ConsentService } from '../services/consent.service';
import { TimelineService } from '../services/timeline.service';
import { PhotoService } from '../services/photo.service';
import { ToastService } from '../../../shared/services/toast.service';

type Aba = 'anamnese' | 'odontograma' | 'tratamento' | 'evolucao' | 'plano' | 'orcamentos' | 'consentimentos' | 'timeline';

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

  // Evolução Clínica / Tratamento
  notas: ClinicalNoteResponse[] = [];
  modalNota = false;
  notaForm!: FormGroup;
  salvandoNota = false;
  modalTratamento = false;
  tratamentoForm!: FormGroup;
  salvandoTratamento = false;

  // Plano de Tratamento
  plano: TreatmentPlanResponse[] = [];
  modalPlano = false;
  planoForm!: FormGroup;
  salvandoPlano = false;

  // Odontograma
  toothRecords: ToothRecord[] = [];
  toothMap: Record<string, ToothRecord> = {};
  selectedToothForNote: number | null = null;
  denteNote = '';
  salvandoDente = false;
  readonly upperTeeth = UPPER_TEETH;
  readonly lowerTeeth = LOWER_TEETH;
  readonly FACE_FILL = FACE_FILL;
  readonly FACE_FILL_OPACITY = FACE_FILL_OPACITY;
  readonly FACE_STROKE = FACE_STROKE;
  readonly FACE_LABEL = FACE_LABEL;

  // Orçamentos
  orcamentos: BudgetResponse[] = [];
  orcamentoSelecionado: BudgetResponse | null = null;
  modalOrcamento = false;
  orcamentoForm!: FormGroup;
  orcamentoItems: BudgetItemRequest[] = [];
  salvandoOrcamento = false;
  readonly budgetStatusLabels = BUDGET_STATUS_LABELS;
  readonly budgetStatusClass = BUDGET_STATUS_CLASS;
  readonly budgetStatuses = BUDGET_STATUSES;

  // Consentimentos
  consentimentos: ConsentResponse[] = [];
  modalConsentimento = false;
  consentimentoForm!: FormGroup;
  salvandoConsentimento = false;
  consentimentoVer: ConsentResponse | null = null;
  modalAssinar = false;
  assinanteNome = '';
  salvandoAssinatura = false;
  readonly consentTypeLabels = CONSENT_TYPE_LABELS;
  readonly consentTypes = CONSENT_TYPES;

  // Fotos
  fotos: ClinicalPhoto[] = [];
  lightboxFoto: ClinicalPhoto | null = null;
  uploadandoFoto = false;

  // Linha do tempo
  timeline: TimelineEvent[] = [];

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
    private odontogramService: OdontogramService,
    private budgetService: BudgetService,
    private consentService: ConsentService,
    private timelineService: TimelineService,
    private photoService: PhotoService,
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

    this.tratamentoForm = this.fb.group({
      noteDate:    [hoje, Validators.required],
      tooth:       [''],
      description: ['', Validators.required],
    });

    this.planoForm = this.fb.group({
      description:    ['', Validators.required],
      tooth:          [''],
      valueEstimated: [null],
    });

    this.orcamentoForm = this.fb.group({
      notes:      [''],
      totalValue: [0],
      validUntil: [''],
      status:     ['RASCUNHO'],
    });

    this.consentimentoForm = this.fb.group({
      title:   ['', Validators.required],
      type:    ['TRATAMENTO_GERAL', Validators.required],
      content: ['', Validators.required],
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
    } else if (aba === 'tratamento') {
      this.prontuarioService.listNotes(this.patientId).subscribe({
        next: r => { this.notas = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar registros.'); this.loading = false; }
      });
    } else if (aba === 'evolucao') {
      this.prontuarioService.listNotes(this.patientId).subscribe({
        next: r => { this.notas = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar registros.'); this.loading = false; }
      });
      this.photoService.list(this.patientId).subscribe({
        next: r => this.fotos = r,
        error: () => {}
      });
    } else if (aba === 'plano') {
      this.prontuarioService.listPlan(this.patientId).subscribe({
        next: r => { this.plano = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar plano.'); this.loading = false; }
      });
    } else if (aba === 'odontograma') {
      this.odontogramService.list(this.patientId).subscribe({
        next: r => {
          this.toothRecords = r;
          this.toothMap = {};
          r.forEach(t => this.toothMap[t.toothNumber] = t);
          this.loading = false;
        },
        error: () => { this.toast.error('Erro ao carregar odontograma.'); this.loading = false; }
      });
    } else if (aba === 'orcamentos') {
      this.budgetService.list(this.patientId).subscribe({
        next: r => { this.orcamentos = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar orçamentos.'); this.loading = false; }
      });
    } else if (aba === 'consentimentos') {
      this.consentService.list(this.patientId).subscribe({
        next: r => { this.consentimentos = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar consentimentos.'); this.loading = false; }
      });
    } else if (aba === 'timeline') {
      this.timelineService.list(this.patientId).subscribe({
        next: r => { this.timeline = r; this.loading = false; },
        error: () => { this.toast.error('Erro ao carregar linha do tempo.'); this.loading = false; }
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
      noteDate: v.noteDate, noteType: v.noteType,
      tooth: v.tooth || null, description: v.description, appointmentId: null,
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

  abrirModalTratamento(): void {
    const hoje = new Date().toISOString().split('T')[0];
    this.tratamentoForm.reset({ noteDate: hoje, tooth: '', description: '' });
    this.modalTratamento = true;
  }

  salvarTratamento(): void {
    if (this.tratamentoForm.invalid) return;
    this.salvandoTratamento = true;
    const v = this.tratamentoForm.value;
    const req: ClinicalNoteRequest = {
      noteDate: v.noteDate, noteType: 'PROCEDIMENTO',
      tooth: v.tooth || null, description: v.description, appointmentId: null,
    };
    this.prontuarioService.createNote(this.patientId, req).subscribe({
      next: r => {
        this.notas = [r, ...this.notas];
        this.modalTratamento = false;
        this.salvandoTratamento = false;
        this.toast.success('Registro salvo.');
      },
      error: () => { this.toast.error('Erro ao salvar registro.'); this.salvandoTratamento = false; }
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
      description: v.description, tooth: v.tooth || null, valueEstimated: v.valueEstimated ?? null,
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

  // ── Odontograma ───────────────────────────────────────────────────────────

  getRecord(tooth: number): ToothRecord | undefined {
    return this.toothMap[String(tooth)];
  }

  faceFill(tooth: number, face: keyof Pick<ToothRecord, 'vestibular'|'mesial'|'distal'|'lingual'|'oclusal'>): string {
    const r = this.toothMap[String(tooth)];
    const st: FaceStatus = (r?.[face] as FaceStatus) ?? '';
    return FACE_FILL[st];
  }

  faceFillOpacity(tooth: number, face: keyof Pick<ToothRecord, 'vestibular'|'mesial'|'distal'|'lingual'|'oclusal'>): string {
    const r = this.toothMap[String(tooth)];
    const st: FaceStatus = (r?.[face] as FaceStatus) ?? '';
    return FACE_FILL_OPACITY[st];
  }

  faceStroke(tooth: number, face: keyof Pick<ToothRecord, 'vestibular'|'mesial'|'distal'|'lingual'|'oclusal'>): string {
    const r = this.toothMap[String(tooth)];
    const st: FaceStatus = (r?.[face] as FaceStatus) ?? '';
    return FACE_STROKE[st];
  }

  faceLabel(tooth: number, face: keyof Pick<ToothRecord, 'vestibular'|'mesial'|'distal'|'lingual'|'oclusal'>): string {
    const r = this.toothMap[String(tooth)];
    const st: FaceStatus = (r?.[face] as FaceStatus) ?? '';
    return FACE_LABEL[st];
  }

  isFlipped(tooth: number): boolean {
    return [1, 2].includes(Math.floor(tooth / 10));
  }

  isAusente(tooth: number): boolean {
    return this.toothMap[String(tooth)]?.ausente ?? false;
  }

  isCanal(tooth: number): boolean {
    return this.toothMap[String(tooth)]?.canal ?? false;
  }

  leftFace(tooth: number): 'mesial' | 'distal' {
    return mesialOnRight(tooth) ? 'distal' : 'mesial';
  }

  rightFace(tooth: number): 'mesial' | 'distal' {
    return mesialOnRight(tooth) ? 'mesial' : 'distal';
  }

  cycleFaceOnTooth(tooth: number, face: keyof Pick<ToothRecord, 'vestibular'|'mesial'|'distal'|'lingual'|'oclusal'>): void {
    const key = String(tooth);
    const existing = this.toothMap[key];
    const current: FaceStatus = (existing?.[face] as FaceStatus) ?? '';
    const req = this.buildRequest(tooth);
    (req as any)[face] = cycleFace(current);
    this.upsertTooth(tooth, req);
  }

  toggleCanal(tooth: number): void {
    const req = this.buildRequest(tooth);
    req.canal = !req.canal;
    this.upsertTooth(tooth, req);
  }

  toggleAusente(tooth: number): void {
    const req = this.buildRequest(tooth);
    req.ausente = !req.ausente;
    this.upsertTooth(tooth, req);
  }

  openToothNote(tooth: number): void {
    this.selectedToothForNote = tooth;
    this.denteNote = this.toothMap[String(tooth)]?.note ?? '';
  }

  saveToothNote(): void {
    if (this.selectedToothForNote == null) return;
    const req = this.buildRequest(this.selectedToothForNote);
    req.note = this.denteNote || null;
    this.upsertTooth(this.selectedToothForNote, req, () => { this.selectedToothForNote = null; });
  }

  private buildRequest(tooth: number): ToothRecordRequest {
    const key = String(tooth);
    const r = this.toothMap[key];
    if (!r) return emptyRecord(key);
    return {
      toothNumber: key,
      vestibular: r.vestibular as FaceStatus,
      mesial:     r.mesial     as FaceStatus,
      distal:     r.distal     as FaceStatus,
      lingual:    r.lingual    as FaceStatus,
      oclusal:    r.oclusal    as FaceStatus,
      canal:      r.canal,
      ausente:    r.ausente,
      note:       r.note,
    };
  }

  private upsertTooth(tooth: number, req: ToothRecordRequest, onSuccess?: () => void): void {
    this.odontogramService.upsert(this.patientId, String(tooth), req).subscribe({
      next: r => { this.toothMap[r.toothNumber] = r; onSuccess?.(); },
      error: () => this.toast.error(`Erro ao atualizar dente ${tooth}.`)
    });
  }

  // ── Orçamentos ────────────────────────────────────────────────────────────

  abrirModalOrcamento(): void {
    const validUntil = new Date(); validUntil.setDate(validUntil.getDate() + 30);
    this.orcamentoForm.reset({ notes: '', totalValue: 0, validUntil: validUntil.toISOString().split('T')[0], status: 'RASCUNHO' });
    this.modalOrcamento = true;
  }

  adicionarItemOrcamento(): void {
    this.orcamentoItems = [...this.orcamentoItems, { description: '', tooth: null, quantity: 1, unitValue: 0 }];
  }

  removerItemOrcamento(idx: number): void {
    this.orcamentoItems = this.orcamentoItems.filter((_, i) => i !== idx);
  }

  totalOrcamento(): number {
    return this.orcamentoItems.reduce((s, i) => s + (i.quantity * i.unitValue), 0);
  }

  salvarOrcamento(): void {
    this.salvandoOrcamento = true;
    const v = this.orcamentoForm.value;
    const req: BudgetRequest = {
      dentistId:  null,
      notes:      v.notes || null,
      totalValue: v.totalValue ?? 0,
      validUntil: v.validUntil || null,
      items:      [],
    };
    this.budgetService.create(this.patientId, req).subscribe({
      next: r => {
        this.orcamentos = [r, ...this.orcamentos];
        this.modalOrcamento = false;
        this.salvandoOrcamento = false;
        this.toast.success('Orçamento criado.');
      },
      error: () => { this.toast.error('Erro ao criar orçamento.'); this.salvandoOrcamento = false; }
    });
  }

  verOrcamento(o: BudgetResponse): void {
    this.orcamentoSelecionado = o;
  }

  fecharOrcamento(): void {
    this.orcamentoSelecionado = null;
  }

  alterarStatusOrcamento(o: BudgetResponse, status: BudgetStatus): void {
    this.budgetService.updateStatus(this.patientId, o.id, status).subscribe({
      next: r => {
        const idx = this.orcamentos.findIndex(x => x.id === r.id);
        if (idx >= 0) this.orcamentos[idx] = r;
        if (this.orcamentoSelecionado?.id === r.id) this.orcamentoSelecionado = r;
        this.toast.success('Status do orçamento atualizado.');
      },
      error: () => this.toast.error('Erro ao atualizar status.')
    });
  }

  excluirOrcamento(o: BudgetResponse): void {
    if (!confirm('Excluir este orçamento?')) return;
    this.budgetService.delete(this.patientId, o.id).subscribe({
      next: () => {
        this.orcamentos = this.orcamentos.filter(x => x.id !== o.id);
        if (this.orcamentoSelecionado?.id === o.id) this.orcamentoSelecionado = null;
        this.toast.success('Orçamento excluído.');
      },
      error: () => this.toast.error('Erro ao excluir orçamento.')
    });
  }

  // ── Consentimentos ────────────────────────────────────────────────────────

  abrirModalConsentimento(): void {
    this.consentimentoForm.reset({ title: '', type: 'TRATAMENTO_GERAL', content: '' });
    this.modalConsentimento = true;
  }

  salvarConsentimento(): void {
    if (this.consentimentoForm.invalid) return;
    this.salvandoConsentimento = true;
    const v = this.consentimentoForm.value;
    const req: ConsentRequest = { title: v.title, type: v.type as ConsentType, content: v.content };
    this.consentService.create(this.patientId, req).subscribe({
      next: r => {
        this.consentimentos = [r, ...this.consentimentos];
        this.modalConsentimento = false;
        this.salvandoConsentimento = false;
        this.toast.success('Consentimento criado.');
      },
      error: () => { this.toast.error('Erro ao criar consentimento.'); this.salvandoConsentimento = false; }
    });
  }

  verConsentimento(c: ConsentResponse): void {
    this.consentimentoVer = c;
  }

  fecharConsentimento(): void {
    this.consentimentoVer = null;
    this.modalAssinar = false;
    this.assinanteNome = '';
  }

  abrirAssinar(c: ConsentResponse): void {
    this.consentimentoVer = c;
    this.modalAssinar = true;
    this.assinanteNome = '';
  }

  confirmarAssinatura(): void {
    if (!this.consentimentoVer || !this.assinanteNome.trim()) return;
    this.salvandoAssinatura = true;
    this.consentService.sign(this.patientId, this.consentimentoVer.id, this.assinanteNome.trim()).subscribe({
      next: r => {
        const idx = this.consentimentos.findIndex(x => x.id === r.id);
        if (idx >= 0) this.consentimentos[idx] = r;
        this.consentimentoVer = r;
        this.modalAssinar = false;
        this.salvandoAssinatura = false;
        this.toast.success('Consentimento assinado.');
      },
      error: () => { this.toast.error('Erro ao assinar consentimento.'); this.salvandoAssinatura = false; }
    });
  }

  excluirConsentimento(c: ConsentResponse): void {
    if (!confirm('Excluir este consentimento?')) return;
    this.consentService.delete(this.patientId, c.id).subscribe({
      next: () => {
        this.consentimentos = this.consentimentos.filter(x => x.id !== c.id);
        if (this.consentimentoVer?.id === c.id) this.fecharConsentimento();
        this.toast.success('Consentimento excluído.');
      },
      error: () => this.toast.error('Erro ao excluir consentimento.')
    });
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  voltar(): void { this.router.navigate(['/dashboard/pacientes']); }

  editarPaciente(): void {
    // abre o formulário de edição na lista — volta para lista com flag de edição
    this.router.navigate(['/dashboard/pacientes'], { queryParams: { editar: this.patientId } });
  }

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
      PLANEJADO: 'badge-gray', APROVADO: 'badge-blue', PARCIALMENTE_APROVADO: 'badge-orange',
      REALIZADO: 'badge-green', CANCELADO: 'badge-red', PENDENTE: 'badge-yellow',
    };
    return map[s] ?? 'badge-gray';
  }

  timelineIcon(type: string): string {
    const icons: Record<string, string> = {
      CONSULTA: '📅', ANAMNESE: '📋', NOTA_CLINICA: '📝',
      PLANO: '🦷', ORCAMENTO: '💰', CONSENTIMENTO: '📄',
    };
    return icons[type] ?? '•';
  }

  // ── Fotos ──────────────────────────────────────────────────────────────────

  photoUrl(id: string): string {
    return this.photoService.photoUrl(id);
  }

  onFotosSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const files = Array.from(input.files);
    input.value = '';
    this.uploadandoFoto = true;
    let pending = files.length;
    files.forEach(file => {
      this.photoService.upload(this.patientId, file, '').subscribe({
        next: foto => {
          this.fotos = [foto, ...this.fotos];
          pending--;
          if (pending === 0) this.uploadandoFoto = false;
        },
        error: () => {
          this.toast.error('Erro ao enviar foto.');
          pending--;
          if (pending === 0) this.uploadandoFoto = false;
        }
      });
    });
  }

  excluirFoto(foto: ClinicalPhoto): void {
    if (!confirm('Remover esta foto?')) return;
    this.photoService.delete(this.patientId, foto.id).subscribe({
      next: () => this.fotos = this.fotos.filter(f => f.id !== foto.id),
      error: () => this.toast.error('Erro ao remover foto.')
    });
  }

  abrirLightbox(foto: ClinicalPhoto): void {
    this.lightboxFoto = foto;
  }

  fecharLightbox(): void {
    this.lightboxFoto = null;
  }
}
