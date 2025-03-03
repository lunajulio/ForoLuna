'use client'
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { api } from '../services/api';
import { useRouter } from 'next/navigation';

interface FormData {
  nombre: string;
  login: string;  
  password: string;
  repeatPassword: string;
}

const RegisterForm = () => {
  const { register, handleSubmit, formState: { errors }, setError } = useForm<FormData>();
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState<string>('');
  const router = useRouter();

  const onSubmit = async (data: FormData) => {
    try {
      setIsLoading(true);
      setApiError('');

      if (data.password !== data.repeatPassword) {
        setError('repeatPassword', {
          type: 'manual',
          message: 'Las contraseñas no coinciden'
        });
        return;
      }

      // Log para debug
      console.log('Datos a enviar:', {
        nombre: data.nombre,
        login: data.login,
        clave: data.password
      });


      const response = await api.post('/usuarios', {
        nombre: data.nombre,
        login: data.login,
        clave: data.password
      });
      
      console.log('Respuesta:', response.data);
      router.push('/login');

    } catch (error: any) {
      console.error('Error completo:', error);
      setApiError(error.response?.data || 'Error al registrar usuario');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-[calc(100vh-64px)] p-4">
      <div className="w-full max-w-md">
        <h1 className="text-4xl font-bold text-white mb-2 text-center">
          Join Luna Community
        </h1>
        <p className="text-gray-300 mb-8 text-center">
          Get more features and priviliges by joining to the most helpful community
        </p>
        
        {apiError && (
          <div className="bg-red-500 text-white p-3 rounded-md mb-4 text-center">
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <input 
              {...register("nombre", { 
                required: "El nombre es requerido",
                minLength: {
                  value: 3,
                  message: "El nombre debe tener al menos 3 caracteres"
                }
              })}
              type="text"
              placeholder="Nombre"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
            {errors.nombre && (
              <p className="text-red-500 text-sm mt-1">{errors.nombre.message}</p>
            )}
          </div>
          
          <div>
            <input 
              {...register("login", { 
                required: "El usuario es requerido",
                minLength: {
                  value: 3,
                  message: "El usuario debe tener al menos 3 caracteres"
                }
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
              {...register("password", { 
                required: "La contraseña es requerida",
                minLength: {
                  value: 6,
                  message: "La contraseña debe tener al menos 6 caracteres"
                }
              })}
              type="password"
              placeholder="Contraseña"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
            {errors.password && (
              <p className="text-red-500 text-sm mt-1">{errors.password.message}</p>
            )}
          </div>
          
          <div>
            <input 
              {...register("repeatPassword", { 
                required: "Por favor confirma tu contraseña"
              })}
              type="password"
              placeholder="Confirmar contraseña"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
            {errors.repeatPassword && (
              <p className="text-red-500 text-sm mt-1">{errors.repeatPassword.message}</p>
            )}
          </div>
          
          <button 
            type="submit"
            disabled={isLoading}
            className={`w-full bg-purple-950 text-white py-3 rounded-md 
                      ${isLoading ? 'opacity-50 cursor-not-allowed' : 'hover:bg-purple-900'}`}
          >
            {isLoading ? 'REGISTRANDO...' : 'REGISTRAR'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default RegisterForm;