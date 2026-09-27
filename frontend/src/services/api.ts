import axios from 'axios';

// En producción se define en el build (ej. "/api", servido por el proxy en el mismo dominio)
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar el token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Si el token expiró o es inválido, cerrar sesión y volver al login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isLoginRequest = error.config?.url === '/login';
    if (error.response?.status === 401 && !isLoginRequest) {
      localStorage.removeItem('token');
      localStorage.removeItem('userName');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Extrae el mensaje que envía el backend ({ message }) o usa uno por defecto
export const getErrorMessage = (error: unknown, fallback: string): string => {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message;
    if (typeof message === 'string' && message) {
      return message;
    }
  }
  return fallback;
};