import { Component, OnInit } from '@angular/core';
import { AgendaService } from '../services/agenda.service';
import { UsuarioService } from '../../usuarios/services/usuario.service';
import { ToastService } from '../../../shared/services/toast.service';
import { Agendamento, DiaMiniCalendario, SlotHorario, SlotCategoria } from '../models/agenda.models';
import { Usuario } from '../../usuarios/models/usuario.models';

@Component({
  selector: 'app-agenda-calendario',
  templateUrl: './calendario.component.html',
  styleUrls: ['./calendario.component.scss']
})
export class CalendarioComponent implements OnInit {

  dentistas: Usuario[] = [];
  dentistaSelecionado = '';

  mesAtual: Date = new Date();
  diaSelecionado: Date = new Date();

  diasDoMes: DiaMiniCalendario[] = [];
  agendamentosMes: Agendamento[] = [];
  carregando = false;

  modoVisualizacao: 'lista' | 'grade' = 'lista';

  modalAberto = false;
  agendamentoSelecionado: Agendamento | null = null;
  preData: Date | null = null;
  preHora: string | null = null;

  detalhesAberto: Agendamento | null = null;

  readonly diasSemana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

  constructor(
    private agendaService: AgendaService,
    private usuarioService: UsuarioService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.montarDiasDoMes();
    this.carregar();
    this.usuarioService.list('', 0, 100).subscribe({
      next: res => {
        this.dentistas = res.content.filter(u => u.role === 'DENTISTA' && u.active);
      }
    });
  }

  carregar(): void {
    this.carregando = true;
    const from = this.formatDate(this.inicioDeMes(this.mesAtual));
    const to   = this.formatDate(this.fimDeMes(this.mesAtual));
    this.agendaService.listByPeriod(this.dentistaSelecionado || null, from, to).subscribe({
      next: lista => {
        this.agendamentosMes = lista;
        this.montarDiasDoMes();
        this.carregando = false;
      },
      error: () => {
        this.toast.error('Erro ao carregar agenda.');
        this.carregando = false;
      }
    });
  }

  selecionarDia(dia: DiaMiniCalendario): void {
    if (!dia.doMes) return;
    this.diaSelecionado = dia.data;
    this.montarDiasDoMes();
  }

  mesAnterior(): void {
    this.mesAtual = new Date(this.mesAtual.getFullYear(), this.mesAtual.getMonth() - 1, 1);
    this.agendamentosMes = [];
    this.montarDiasDoMes();
    this.carregar();
  }

  proximoMes(): void {
    this.mesAtual = new Date(this.mesAtual.getFullYear(), this.mesAtual.getMonth() + 1, 1);
    this.agendamentosMes = [];
    this.montarDiasDoMes();
    this.carregar();
  }

  irParaHoje(): void {
    const hoje = new Date();
    this.diaSelecionado = hoje;
    const mesoBudou = this.mesAtual.getFullYear() !== hoje.getFullYear()
                   || this.mesAtual.getMonth()    !== hoje.getMonth();
    if (mesoBudou) {
      this.mesAtual = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
      this.agendamentosMes = [];
      this.montarDiasDoMes();
      this.carregar();
    } else {
      this.montarDiasDoMes();
    }
  }

  get agendamentosHoje(): Agendamento[] {
    return this.agendamentosMes
      .filter(a => {
        const d = new Date(a.startTime);
        return d.getFullYear() === this.diaSelecionado.getFullYear()
            && d.getMonth()    === this.diaSelecionado.getMonth()
            && d.getDate()     === this.diaSelecionado.getDate();
      })
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  }

  get labelMes(): string {
    return this.mesAtual.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  }

  get labelDiaSelecionado(): string {
    return this.diaSelecionado.toLocaleDateString('pt-BR', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });
  }

  alternarModo(): void {
    this.modoVisualizacao = this.modoVisualizacao === 'lista' ? 'grade' : 'lista';
  }

  get slots(): SlotHorario[] {
    const inicio = 8 * 60;
    const fim    = 18 * 60;
    const incremento = 30;
    const dia = this.formatDate(this.diaSelecionado);
    const slots: SlotHorario[] = [];

    for (let t = inicio; t < fim; t += incremento) {
      const hora = String(Math.floor(t / 60)).padStart(2, '0') + ':' + String(t % 60).padStart(2, '0');
      let categoria: SlotCategoria = 'livre';
      let label = 'Livre';
      let detalhe = '';
      let agendamentoId: string | undefined;
      let patientId: string | undefined;

      // Almoço 12:00–13:00
      if (t >= 12 * 60 && t < 13 * 60) {
        categoria = 'almoco';
        label = 'Almoço';
      }

      if (categoria === 'livre') {
        const consulta = this.agendamentosHoje.find(a => {
          if (a.status === 'CANCELADO') return false;
          const start = new Date(a.startTime);
          const startMin = start.getHours() * 60 + start.getMinutes();
          const endMin   = startMin + a.durationMinutes;
          return t < endMin && (t + incremento) > startMin;
        });
        if (consulta) {
          categoria = 'ocupado';
          label = consulta.patientName;
          detalhe = consulta.notes ?? '';
          agendamentoId = consulta.id;
          patientId = consulta.patientId;
        }
      }

      slots.push({ hora, categoria, label, detalhe, agendamentoId, patientId });
    }
    return slots;
  }

  abrirSlotLivre(slot: SlotHorario): void {
    if (slot.categoria !== 'livre') return;
    const [h, m] = slot.hora.split(':').map(Number);
    const data = new Date(this.diaSelecionado);
    data.setHours(h, m, 0, 0);
    this.agendamentoSelecionado = null;
    this.preData = data;
    this.preHora = slot.hora;
    this.modalAberto = true;
  }

  abrirSlotOcupado(slot: SlotHorario): void {
    if (slot.categoria !== 'ocupado' || !slot.agendamentoId) return;
    const ag = this.agendamentosHoje.find(a => a.id === slot.agendamentoId) ?? null;
    this.detalhesAberto = ag;
  }

  slotBorderColor(categoria: SlotCategoria): string {
    const map: Record<SlotCategoria, string> = {
      livre:        'var(--color-line)',
      ocupado:      'var(--color-rust)',
      almoco:       'var(--color-ink-soft)',
      indisponivel: 'var(--color-gold)',
    };
    return map[categoria];
  }

  abrirNovoNoDia(): void {
    this.agendamentoSelecionado = null;
    this.preData = new Date(this.diaSelecionado);
    this.preData.setHours(8, 0, 0, 0);
    this.preHora = null;
    this.modalAberto = true;
  }

  abrirDetalhes(ag: Agendamento): void {
    this.detalhesAberto = ag;
  }

  editarAgendamento(ag: Agendamento): void {
    this.detalhesAberto = null;
    this.agendamentoSelecionado = ag;
    this.preData = null;
    this.modalAberto = true;
  }

  alterarStatus(ag: Agendamento, status: string): void {
    this.agendaService.changeStatus(ag.id, status).subscribe({
      next: atualizado => {
        this.toast.success('Status atualizado.');
        this.detalhesAberto = atualizado;
        this.carregar();
      },
      error: err => {
        const msg = err?.error?.message ?? 'Erro ao alterar status.';
        this.toast.error(msg);
      }
    });
  }

  aoSalvar(_ag: Agendamento): void {
    this.modalAberto = false;
    this.carregar();
  }

  fecharModal(): void {
    this.modalAberto = false;
    this.agendamentoSelecionado = null;
    this.preData = null;
    this.preHora = null;
  }

  fecharDetalhes(): void { this.detalhesAberto = null; }

  statusLabel(s: string): string {
    const map: Record<string, string> = {
      AGENDADO: 'Agendado', CONFIRMADO: 'Confirmado',
      REALIZADO: 'Realizado', CANCELADO: 'Cancelado'
    };
    return map[s] ?? s;
  }

  proximosStatus(atual: string): string[] {
    const map: Record<string, string[]> = {
      AGENDADO:   ['CONFIRMADO', 'CANCELADO'],
      CONFIRMADO: ['REALIZADO', 'CANCELADO'],
      REALIZADO:  [],
      CANCELADO:  []
    };
    return map[atual] ?? [];
  }

  formatHora(iso: string): string {
    return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatDataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  private montarDiasDoMes(): void {
    const ano = this.mesAtual.getFullYear();
    const mes = this.mesAtual.getMonth();
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);

    const primeiro = new Date(ano, mes, 1);
    const diasAntes = primeiro.getDay();
    const ultimoDia = new Date(ano, mes + 1, 0).getDate();

    const dias: DiaMiniCalendario[] = [];

    for (let i = diasAntes; i > 0; i--) {
      dias.push({ data: new Date(ano, mes, 1 - i), doMes: false, hoje: false, selecionado: false, temAgendamento: false });
    }

    for (let d = 1; d <= ultimoDia; d++) {
      const data = new Date(ano, mes, d);
      const temAg = this.agendamentosMes.some(a => {
        const ad = new Date(a.startTime);
        return ad.getFullYear() === ano && ad.getMonth() === mes && ad.getDate() === d;
      });
      dias.push({
        data,
        doMes: true,
        hoje: data.getTime() === hoje.getTime(),
        selecionado: data.toDateString() === this.diaSelecionado.toDateString(),
        temAgendamento: temAg
      });
    }

    let d = 1;
    while (dias.length % 7 !== 0) {
      dias.push({ data: new Date(ano, mes + 1, d++), doMes: false, hoje: false, selecionado: false, temAgendamento: false });
    }

    this.diasDoMes = dias;
  }

  private inicioDeMes(d: Date): Date {
    return new Date(d.getFullYear(), d.getMonth(), 1);
  }

  private fimDeMes(d: Date): Date {
    return new Date(d.getFullYear(), d.getMonth() + 1, 0);
  }

  private formatDate(d: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  }
}
