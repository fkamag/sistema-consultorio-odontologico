import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UsuarioService } from '../../usuarios/services/usuario.service';
import { ToastService } from '../../../shared/services/toast.service';

const SENHA_PATTERN = /^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$/;

@Component({
  selector: 'app-dentista-formulario',
  templateUrl: './formulario.component.html',
  styleUrls: ['./formulario.component.scss']
})
export class DentistaFormularioComponent implements OnInit, OnChanges {

  @Input() dentistaId: string | null = null;
  @Output() fechou = new EventEmitter<void>();
  @Output() salvou = new EventEmitter<void>();

  form!: FormGroup;
  loading = false;
  saving = false;
  mostrarSenha = false;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:     ['', [Validators.required, Validators.maxLength(150)]],
      email:    ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      password: [null, [Validators.minLength(8), Validators.pattern(SENHA_PATTERN)]]
    });

    this.carregarSeEditar();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dentistaId'] && this.form) {
      this.form.reset();
      this.carregarSeEditar();
    }
  }

  private carregarSeEditar(): void {
    if (!this.dentistaId) return;
    this.loading = true;
    this.usuarioService.findById(this.dentistaId).subscribe({
      next: u => {
        this.form.patchValue({ name: u.name, email: u.email });
        this.loading = false;
      },
      error: () => {
        this.toast.error('Dentista não encontrado.');
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (!this.isEdit) {
      const senhaCtrl = this.form.get('password');
      senhaCtrl?.addValidators(Validators.required);
      senhaCtrl?.updateValueAndValidity();
    }

    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.saving = true;
    const { name, email, password } = this.form.value;
    const request = { name, email, password: password || null, role: 'DENTISTA' };

    const op = this.dentistaId
      ? this.usuarioService.update(this.dentistaId, request)
      : this.usuarioService.create(request);

    op.subscribe({
      next: () => {
        this.saving = false;
        this.toast.success(this.dentistaId ? 'Dentista atualizado com sucesso.' : 'Dentista cadastrado com sucesso.');
        this.salvou.emit();
      },
      error: err => {
        this.toast.error(err.error?.message || 'Erro ao salvar dentista.');
        this.saving = false;
      }
    });
  }

  fechar(): void { this.fechou.emit(); }

  get isEdit(): boolean { return !!this.dentistaId; }
  get title(): string { return this.isEdit ? 'Editar Dentista' : 'Novo Dentista'; }

  fieldError(name: string, error: string): boolean {
    const c = this.form.get(name);
    return !!(c?.touched && c?.hasError(error));
  }
}
