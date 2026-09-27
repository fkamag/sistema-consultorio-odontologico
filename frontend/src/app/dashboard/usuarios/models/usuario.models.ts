export interface Usuario {
  id: string;
  name: string;
  email: string;
  role: 'ADMIN' | 'SECRETARIA' | 'DENTISTA';
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UsuarioRequest {
  name: string;
  email: string;
  password?: string | null;
  role: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
