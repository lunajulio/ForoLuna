'use client'
import React from 'react';
import { useForm } from 'react-hook-form';

interface FormData {
  username: string;
  email: string;
  password: string;
  repeatPassword: string;
}

const RegisterForm = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>();

  const onSubmit = (data: FormData) => {
    console.log(data);
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
        
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <input 
              {...register("username")}
              type="text"
              placeholder="Username"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
          </div>
          
          <div>
            <input 
              {...register("email")}
              type="email"
              placeholder="Email"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
          </div>
          
          <div>
            <input 
              {...register("password")}
              type="password"
              placeholder="Password"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
          </div>
          
          <div>
            <input 
              {...register("repeatPassword")}
              type="password"
              placeholder="Repeat password"
              className="w-full p-3 border border-gray-700 rounded-md 
                       bg-gray-900 text-white 
                       focus:outline-none focus:ring-2 focus:ring-white
                       placeholder-gray-400"
            />
          </div>
          
          <button 
            type="submit"
            className="w-full bg-purple-950 text-white py-3 rounded-md "
          >
            REGISTER
          </button>
        </form>
      </div>
    </div>
  );
}

export default RegisterForm;