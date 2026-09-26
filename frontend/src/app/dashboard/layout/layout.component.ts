import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription, filter } from 'rxjs';
import { AuthService } from '../../auth/services/auth.service';
import { UserSession } from '../../shared/models/auth.models';

export interface NavItem {
  key: string;
  label: string;
  icon: string;
  route: string;
  roles: Array<'ADMIN' | 'SECRETARIA' | 'DENTISTA'>;
}

export interface NavGroup {
  group: string;
  items: NavItem[];
}

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit, OnDestroy {

  user: UserSession | null = null;
  currentRoute = '';
  mobileMenuOpen = false;
  private subs = new Subscription();

  readonly icons: Record<string, string> = {
    dashboard: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3.5" y="3.5" width="7.5" height="7.5" rx="1.2"/><rect x="13" y="3.5" width="7.5" height="7.5" rx="1.2"/><rect x="3.5" y="13" width="7.5" height="7.5" rx="1.2"/><rect x="13" y="13" width="7.5" height="7.5" rx="1.2"/></svg>',
    pendencias: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="8.5"/><path d="M12 7.5v5l3 2"/></svg>',
    patients: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="9" cy="8" r="3.2"/><path d="M3 20c0-3.3 2.7-5.6 6-5.6s6 2.3 6 5.6"/><circle cx="17" cy="9" r="2.4"/><path d="M15.2 14.6c2.5.2 4.3 2.2 4.3 5"/></svg>',
    agenda: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3.5" y="5" width="17" height="15.5" rx="1.6"/><path d="M3.5 9.5h17M8 3v3.4M16 3v3.4"/></svg>',
    espera: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M8 12h8M8 9h5M8 15h3"/></svg>',
    recall: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 12a8 8 0 1 1 2.6 5.9"/><path d="M4 17v-4h4"/></svg>',
    comunicacao: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 5h16v11H8l-4 4V5z"/></svg>',
    orcamento: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M6 3.5h12v17l-3-2-3 2-3-2-3 2v-17z"/><path d="M9 8h6M9 11.5h6M9 15h4"/></svg>',
    financeiro: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="8.5"/><path d="M12 7.5v9M15 9.8c0-1.3-1.3-2.3-3-2.3s-3 .9-3 2.1c0 3 6 1.4 6 4.3 0 1.3-1.3 2.1-3 2.1s-3-1-3-2.3"/></svg>',
    materiais: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 3l8 4.5v9L12 21l-8-4.5v-9L12 3z"/><path d="M4.5 7.5L12 12l7.5-4.5M12 12v9"/></svg>',
    esterilizacao: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="13" r="7"/><path d="M12 3v3M9 4.5l1 2M15 4.5l-1 2"/></svg>',
    relatorios: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M5 19V10M12 19V5M19 19v-7"/><path d="M3 19h18"/></svg>',
    dentistas: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3.5" y="5" width="17" height="14" rx="1.8"/><circle cx="9" cy="11" r="2"/><path d="M6.3 16c.4-1.8 1.8-2.8 2.7-2.8s2.3 1 2.7 2.8M14.5 9.5h4M14.5 13h4"/></svg>',
    usuarios: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="8" r="3.4"/><path d="M5 20c0-3.6 3.1-6 7-6s7 2.4 7 6"/></svg>',
    audit: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="5" y="4.5" width="14" height="16" rx="1.6"/><path d="M9 4.2h6a1 1 0 011 1v1.1H8V5.2a1 1 0 011-1z"/><path d="M8.3 11h7.4M8.3 14.3h7.4M8.3 17.6h4.8"/></svg>'
  };

  readonly mobileNavItems: NavItem[] = [
    { key: 'dashboard', label: 'Dashboard', icon: 'dashboard', route: '/dashboard', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
    { key: 'pacientes', label: 'Pacientes', icon: 'patients', route: '/dashboard/pacientes', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
    { key: 'agenda', label: 'Agenda', icon: 'agenda', route: '/dashboard/agenda', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
    { key: 'financeiro', label: 'Financeiro', icon: 'financeiro', route: '/dashboard/financeiro', roles: ['ADMIN', 'SECRETARIA'] }
  ];

  readonly navGroups: NavGroup[] = [
    {
      group: 'Geral',
      items: [
        { key: 'dashboard', label: 'Dashboard', icon: 'dashboard', route: '/dashboard', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
        { key: 'pendencias', label: 'Pendências', icon: 'pendencias', route: '/dashboard/pendencias', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] }
      ]
    },
    {
      group: 'Atendimento',
      items: [
        { key: 'pacientes', label: 'Pacientes', icon: 'patients', route: '/dashboard/pacientes', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
        { key: 'agenda', label: 'Agenda', icon: 'agenda', route: '/dashboard/agenda', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
        { key: 'espera', label: 'Lista de Espera', icon: 'espera', route: '/dashboard/espera', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
        { key: 'recall', label: 'Retorno', icon: 'recall', route: '/dashboard/recall', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] },
        { key: 'comunicacao', label: 'Comunicações', icon: 'comunicacao', route: '/dashboard/comunicacao', roles: ['ADMIN', 'SECRETARIA'] }
      ]
    },
    {
      group: 'Financeiro',
      items: [
        { key: 'orcamentos', label: 'Orçamentos', icon: 'orcamento', route: '/dashboard/orcamentos', roles: ['ADMIN', 'SECRETARIA'] },
        { key: 'financeiro', label: 'Financeiro', icon: 'financeiro', route: '/dashboard/financeiro', roles: ['ADMIN', 'SECRETARIA'] }
      ]
    },
    {
      group: 'Materiais',
      items: [
        { key: 'materiais', label: 'Materiais', icon: 'materiais', route: '/dashboard/materiais', roles: ['ADMIN', 'SECRETARIA'] },
        { key: 'esterilizacao', label: 'Esterilização', icon: 'esterilizacao', route: '/dashboard/esterilizacao', roles: ['ADMIN', 'SECRETARIA'] }
      ]
    },
    {
      group: 'Relatórios',
      items: [
        { key: 'relatorios', label: 'Relatórios', icon: 'relatorios', route: '/dashboard/relatorios', roles: ['ADMIN', 'SECRETARIA', 'DENTISTA'] }
      ]
    },
    {
      group: 'Administração',
      items: [
        { key: 'dentistas', label: 'Dentistas', icon: 'dentistas', route: '/dashboard/dentistas', roles: ['ADMIN', 'SECRETARIA'] },
        { key: 'usuarios', label: 'Usuários', icon: 'usuarios', route: '/dashboard/usuarios', roles: ['ADMIN'] },
        { key: 'auditoria', label: 'Auditoria', icon: 'audit', route: '/dashboard/auditoria', roles: ['ADMIN'] }
      ]
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.authService.user$.subscribe(u => this.user = u)
    );
    this.subs.add(
      this.router.events.pipe(
        filter(e => e instanceof NavigationEnd)
      ).subscribe((e: any) => {
        this.currentRoute = e.urlAfterRedirects;
      })
    );
    this.currentRoute = this.router.url;
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  visibleGroups(): NavGroup[] {
    if (!this.user) return [];
    const role = this.user.role;
    return this.navGroups
      .map(g => ({
        ...g,
        items: g.items.filter(it => it.roles.includes(role))
      }))
      .filter(g => g.items.length > 0);
  }

  isActive(item: NavItem): boolean {
    if (item.route === '/dashboard') {
      return this.currentRoute === '/dashboard' || this.currentRoute === '/dashboard/';
    }
    return this.currentRoute.startsWith(item.route);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
