import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UsuarioService } from '../services/usuario.service';

const SENHA_PATTERN = /^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$/;

@Component({
  selector: 'app-usuarios-formulario',
  templateUrl: './formulario.component.html',
  styleUrls: ['./formulario.component.scss']
})
export class FormularioComponent implements OnInit, OnChanges {

  @Input() usuarioId: string | null = null;
  @Output() fechou = new EventEmitter<void>();
  @Output() salvou = new EventEmitter<void>();

  form!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';
  mostrarSenha = false;

  readonly roles = [
    { value: 'ADMIN',      label: 'Administrador' },
    { value: 'SECRETARIA', label: 'Secretária' },
    { value: 'DENTISTA',   label: 'Dentista' }
  ];

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:     ['', [Validators.required, Validators.maxLength(150)]],
      email:    ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      password: [null, [Validators.minLength(8), Validators.pattern(SENHA_PATTERN)]],
      role:     ['SECRETARIA', Validators.required]
    });

    this.carregarSeEditar();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['usuarioId'] && this.form) {
      this.form.reset({ role: 'SECRETARIA' });
      this.errorMessage = '';
      this.carregarSeEditar();
    }
  }

  private carregarSeEditar(): void {
    if (!this.usuarioId) return;
    this.loading = true;
    this.usuarioService.findById(this.usuarioId).subscribe({
      next: u => {
        this.form.patchValue({ name: u.name, email: u.email, role: u.role });
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Usuário não encontrado.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.isEdit) {
      // Senha opcional na edição; remove validador required se vazio
      const senhaCtrl = this.form.get('password');
      if (!senhaCtrl?.value) senhaCtrl?.setValue(null);
    } else {
      // Na criação, senha é obrigatória
      const senhaCtrl = this.form.get('password');
      senhaCtrl?.addValidators(Validators.required);
      senhaCtrl?.updateValueAndValidity();
    }

    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.saving = true;
    this.errorMessage = '';
    const { name, email, password, role } = this.form.value;
    const request = { name, email, password: password || null, role };

    const op = this.usuarioId
      ? this.usuarioService.update(this.usuarioId, request)
      : this.usuarioService.create(request);

    op.subscribe({
      next: () => { this.saving = false; this.salvou.emit(); },
      error: err => { this.errorMessage = err.error?.message || 'Erro ao salvar.'; this.saving = false; }
    });
  }

  fechar(): void { this.fechou.emit(); }

  get isEdit(): boolean { return !!this.usuarioId; }
  get title(): string { return this.isEdit ? 'Editar Usuário' : 'Novo Usuário'; }

  fieldError(name: string, error: string): boolean {
    const c = this.form.get(name);
    return !!(c?.touched && c?.hasError(error));
  }
}
