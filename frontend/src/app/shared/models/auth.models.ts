export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  name: string;
  role: 'ADMIN' | 'SECRETARIA' | 'DENTISTA';
}

export interface UserSession {
  name: string;
  role: 'ADMIN' | 'SECRETARIA' | 'DENTISTA';
}
