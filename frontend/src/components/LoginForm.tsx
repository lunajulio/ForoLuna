'use client'
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import axios from 'axios';
import { api, getErrorMessage } from '../services/api';
import { useRouter } from 'next/navigation';
import { DatosAutenticacionUsuario, DatosTokenJWT } from '../services/auth';

const LoginForm = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<DatosAutenticacionUsuario>();
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState<string>('');
  const router = useRouter();

  const onSubmit = async (data: DatosAutenticacionUsuario) => {
    try {
      setIsLoading(true);
      setApiError('');

      const response = await api.post<DatosTokenJWT>('/login', data);

      if (response.data && response.data.jwTtoken) {
        // Guardar datos en localStorage (el interceptor de api.ts añade el token a cada petición)
        localStorage.setItem('token', response.data.jwTtoken);
        localStorage.setItem('userName', data.login);

        router.push('/topico');
      } else {
        setApiError('No se recibió token en la respuesta');
      }

    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        // Error de respuesta del servidor
        setApiError(getErrorMessage(error, 'Error en la autenticación'));
      } else if (axios.isAxiosError(error) && error.request) {
        // Error de conexión
        setApiError('Error de conexión con el servidor');
      } else {
        setApiError('Error inesperado');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-[calc(100vh-64px)] p-4">
      <div className="w-full max-w-md">
        <h1 className="text-4xl font-bold text-white mb-2 text-center">
          Welcome Back
        </h1>
        <p className="text-gray-300 mb-8 text-center">
          Sign in to your account
        </p>
        
        {apiError && (
          <div className="bg-red-500 text-white p-3 rounded-md mb-4 text-center">
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <input 
              {...register("login", { 
                required: "El usuario es requerido"
              })}
              type="text"
              placeholder="Usuario"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
            {errors.login && (
              <p className="text-red-500 text-sm mt-1">{errors.login.message}</p>
            )}
          </div>
          
          <div>
            <input 
              {...register("clave", { 
                required: "La contraseña es requerida"
              })}
              type="password"
              placeholder="Contraseña"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
            {errors.clave && (
              <p className="text-red-500 text-sm mt-1">{errors.clave.message}</p>
            )}
          </div>
          
          <button 
            type="submit"
            disabled={isLoading}
            className={`w-full bg-purple-950 text-white py-3 rounded-md 
                      ${isLoading ? 'opacity-50 cursor-not-allowed' : 'hover:bg-purple-900'}`}
          >
            {isLoading ? 'INICIANDO SESIÓN...' : 'INICIAR SESIÓN'}
          </button>

          <div className="text-center text-gray-400 mt-4">
            ¿No tienes una cuenta?{' '}
            <button
              type="button"
              onClick={() => router.push('/register')}
              className="text-white hover:underline"
            >
              Regístrate aquí
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default LoginForm;