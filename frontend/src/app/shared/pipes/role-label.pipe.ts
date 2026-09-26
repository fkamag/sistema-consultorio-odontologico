import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'roleLabel' })
export class RoleLabelPipe implements PipeTransform {
  transform(role: string | null | undefined): string {
    switch (role) {
      case 'ADMIN': return 'Administrador';
      case 'SECRETARIA': return 'Secretária';
      case 'DENTISTA': return 'Dentista';
      default: return '';
    }
  }
}
