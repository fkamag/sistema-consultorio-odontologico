import { Component, OnInit } from '@angular/core';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { Usuario, PageResponse } from '../../usuarios/models/usuario.models';
import { UsuarioService } from '../../usuarios/services/usuario.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-dentistas-lista',
  templateUrl: './lista.component.html',
  styleUrls: ['./lista.component.scss']
})
export class DentistasListaComponent implements OnInit {

  dentistas: Usuario[] = [];
  totalElements = 0;
  page = 0;
  pageSize = 20;
  loading = false;
  searchTerm = '';

  modalAberto = false;
  dentistaEditandoId: string | null = null;

  private search$ = new Subject<string>();

  constructor(
    private usuarioService: UsuarioService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.search$.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap(term => {
        this.loading = true;
        this.page = 0;
        return this.usuarioService.list(term, 0, this.pageSize, 'DENTISTA');
      })
    ).subscribe({
      next: res => this.handleResponse(res),
      error: () => this.handleError()
    });

    this.load();
  }

  onSearch(term: string): void {
    this.searchTerm = term;
    this.search$.next(term);
  }

  load(): void {
    this.loading = true;
    this.usuarioService.list(this.searchTerm, this.page, this.pageSize, 'DENTISTA').subscribe({
      next: res => this.handleResponse(res),
      error: () => this.handleError()
    });
  }

  private handleResponse(res: PageResponse<Usuario>): void {
    this.dentistas = res.content;
    this.totalElements = res.totalElements;
    this.loading = false;
  }

  private handleError(): void {
    this.toast.error('Erro ao carregar dentistas.');
    this.loading = false;
  }

  abrirNovo(): void {
    this.dentistaEditandoId = null;
    this.modalAberto = true;
  }

  abrirEditar(id: string): void {
    this.dentistaEditandoId = id;
    this.modalAberto = true;
  }

  fecharModal(): void {
    this.modalAberto = false;
    this.dentistaEditandoId = null;
  }

  aoSalvar(): void {
    this.fecharModal();
    this.load();
  }

  toggleActive(dentista: Usuario, event: Event): void {
    event.stopPropagation();
    this.usuarioService.toggleActive(dentista.id).subscribe({
      next: atualizado => {
        const idx = this.dentistas.findIndex(d => d.id === atualizado.id);
        if (idx !== -1) this.dentistas[idx] = atualizado;
        const acao = atualizado.active ? 'ativado' : 'desativado';
        this.toast.success(`Dentista ${acao} com sucesso.`);
      },
      error: err => {
        this.toast.error(err.error?.message || 'Erro ao alterar status do dentista.');
      }
    });
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
}
