import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PacienteService } from '../services/paciente.service';
import { cpfValidator } from '../../../shared/validators/cpf.validator';

@Component({
  selector: 'app-pacientes-formulario',
  templateUrl: './formulario.component.html',
  styleUrls: ['./formulario.component.scss']
})
export class FormularioComponent implements OnInit, OnChanges {

  @Input() pacienteId: string | null = null;
  @Output() fechou = new EventEmitter<void>();
  @Output() salvou = new EventEmitter<void>();

  form!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private pacienteService: PacienteService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:         ['', [Validators.required, Validators.maxLength(150)]],
      cpf:          ['', [Validators.required, cpfValidator()]],
      birthDate:    [null],
      phone:        ['', Validators.maxLength(20)],
      email:        ['', [Validators.email, Validators.maxLength(150)]],
      street:       ['', Validators.maxLength(200)],
      number:       ['', Validators.maxLength(20)],
      complement:   ['', Validators.maxLength(100)],
      neighborhood: ['', Validators.maxLength(100)],
      city:         ['', Validators.maxLength(100)],
      state:        ['', Validators.maxLength(2)],
      notes:        ['']
    });

    this.carregarSeEditar();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pacienteId'] && this.form) {
      this.form.reset();
      this.errorMessage = '';
      this.carregarSeEditar();
    }
  }

  private carregarSeEditar(): void {
    if (!this.pacienteId) return;
    this.loading = true;
    this.pacienteService.findById(this.pacienteId).subscribe({
      next: p => {
        this.form.patchValue({
          name: p.name, cpf: p.cpf, birthDate: p.birthDate,
          phone: p.phone, email: p.email,
          street: p.street, number: p.number, complement: p.complement,
          neighborhood: p.neighborhood, city: p.city, state: p.state,
          notes: p.notes
        });
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Paciente não encontrado.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.errorMessage = '';
    const raw = this.form.value;
    const request = { ...raw, state: raw.state?.toUpperCase() || null };
    const op = this.pacienteId
      ? this.pacienteService.update(this.pacienteId, request)
      : this.pacienteService.create(request);
    op.subscribe({
      next: () => { this.saving = false; this.salvou.emit(); },
      error: err => { this.errorMessage = err.error?.message || 'Erro ao salvar.'; this.saving = false; }
    });
  }

  fechar(): void { this.fechou.emit(); }

  get isEdit(): boolean { return !!this.pacienteId; }
  get title(): string { return this.isEdit ? 'Editar Paciente' : 'Novo Paciente'; }
  fieldError(name: string, error: string): boolean {
    const c = this.form.get(name);
    return !!(c?.touched && c?.hasError(error));
  }
}
