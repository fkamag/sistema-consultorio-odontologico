import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function cpfValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = (control.value ?? '').replace(/\D/g, '');

    if (!value) return null; // deixa o Validators.required cuidar do campo vazio

    if (value.length !== 11) return { cpfInvalido: true };

    // CPFs com todos os dígitos iguais são inválidos (ex: 111.111.111-11)
    if (/^(\d)\1{10}$/.test(value)) return { cpfInvalido: true };

    const calc = (len: number): number => {
      let sum = 0;
      for (let i = 0; i < len; i++) {
        sum += parseInt(value[i]) * (len + 1 - i);
      }
      const rest = (sum * 10) % 11;
      return rest === 10 || rest === 11 ? 0 : rest;
    };

    if (calc(9) !== parseInt(value[9])) return { cpfInvalido: true };
    if (calc(10) !== parseInt(value[10])) return { cpfInvalido: true };

    return null;
  };
}
