import { Directive, HostListener, Input } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({ selector: '[appMask]' })
export class MaskDirective {

  @Input('appMask') mask!: 'cpf' | 'phone';

  constructor(private control: NgControl) {}

  @HostListener('input', ['$event'])
  onInput(event: InputEvent): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '');
    const formatted = this.mask === 'cpf' ? this.formatCpf(digits) : this.formatPhone(digits);
    this.control.control?.setValue(formatted, { emitEvent: false });
    input.value = formatted;
  }

  private formatCpf(digits: string): string {
    const d = digits.slice(0, 11);
    if (d.length <= 3) return d;
    if (d.length <= 6) return `${d.slice(0,3)}.${d.slice(3)}`;
    if (d.length <= 9) return `${d.slice(0,3)}.${d.slice(3,6)}.${d.slice(6)}`;
    return `${d.slice(0,3)}.${d.slice(3,6)}.${d.slice(6,9)}-${d.slice(9)}`;
  }

  private formatPhone(digits: string): string {
    const d = digits.slice(0, 11);
    if (d.length <= 2) return d.length ? `(${d}` : '';
    if (d.length <= 6) return `(${d.slice(0,2)}) ${d.slice(2)}`;
    if (d.length <= 10) return `(${d.slice(0,2)}) ${d.slice(2,6)}-${d.slice(6)}`;
    return `(${d.slice(0,2)}) ${d.slice(2,7)}-${d.slice(7)}`;
  }
}
