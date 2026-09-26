import { Component, OnInit } from '@angular/core';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { Paciente, PageResponse } from '../models/paciente.models';
import { PacienteService } from '../services/paciente.service';

@Component({
  selector: 'app-pacientes-lista',
  templateUrl: './lista.component.html',
  styleUrls: ['./lista.component.scss']
})
export class ListaComponent implements OnInit {

  pacientes: Paciente[] = [];
  totalElements = 0;
  page = 0;
  pageSize = 20;
  loading = false;
  searchTerm = '';
  errorMessage = '';

  modalAberto = false;
  pacienteEditandoId: string | null = null;

  private search$ = new Subject<string>();

  constructor(private pacienteService: PacienteService) {}

  ngOnInit(): void {
    this.search$.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap(term => {
        this.loading = true;
        this.page = 0;
        return this.pacienteService.list(term, 0, this.pageSize);
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
    this.errorMessage = '';
    this.pacienteService.list(this.searchTerm, this.page, this.pageSize).subscribe({
      next: res => this.handleResponse(res),
      error: () => this.handleError()
    });
  }

  private handleResponse(res: PageResponse<Paciente>): void {
    this.pacientes = res.content;
    this.totalElements = res.totalElements;
    this.loading = false;
  }

  private handleError(): void {
    this.errorMessage = 'Erro ao carregar pacientes.';
    this.loading = false;
  }

  abrirNovo(): void {
    this.pacienteEditandoId = null;
    this.modalAberto = true;
  }

  abrirEditar(id: string): void {
    this.pacienteEditandoId = id;
    this.modalAberto = true;
  }

  fecharModal(): void {
    this.modalAberto = false;
    this.pacienteEditandoId = null;
  }

  aoSalvar(): void {
    this.fecharModal();
    this.load();
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

  formatDate(iso: string | null): string {
    if (!iso) return '—';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }
}
