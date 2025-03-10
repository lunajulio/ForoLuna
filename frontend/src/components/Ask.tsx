'use client'
import React, { useState } from 'react';
import { api } from '@/services/api';
import { IoImageOutline } from 'react-icons/io5';

// Lista predefinida de categorías
const CATEGORIAS = [
  'Programación Backend',
  'Programación Frontend',
  'Base de Datos',
  'DevOps',
  'Mobile Development',
  'Machine Learning',
  'Arquitectura de Software',
  'Testing',
  'Cloud Computing',
  'Seguridad'
];

interface AskProps {
  onSubmit: () => void;
  onCancel: () => void;
}

const Ask: React.FC<AskProps> = ({ onSubmit, onCancel }) => {
  const [topicName, setTopicName] = useState('');
  const [category, setCategory] = useState('');
  const [title, setTitle] = useState('');
  const [question, setQuestion] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    if (!category) {
        setError('Please select a category');
        return;
      }
    
    if (topicName.length < 3) {
        setError('Topic name must be at least 3 characters long');
        return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('No hay token de autenticación');
        return;
      }

      const topicoData = {
        titulo: title,
        mensaje: question,
        fechaCreacion: new Date().toISOString(),
        curso: {
          nombre: topicName,
          categoria: category
        }
      };

      console.log('Enviando datos:', topicoData);

      const response = await api.post('/topico', topicoData);
      
      if (response.status === 201 || response.status === 200) {
        onSubmit();
      }

    } catch (error: any) {
      console.error('Error al crear tópico:', error);
      setError(error.response?.data?.message || 'Error al crear el tópico');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-6 bg-gray-900 rounded-lg shadow-lg">
      {error && (
        <div className="bg-red-500 text-white p-3 rounded-md mb-4">
          {error}
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Campo para el nombre del tópico */}
        <div>
          <label className="block text-gray-300 mb-2">Topic Name</label>
          <input
            type="text"
            value={topicName}
            onChange={(e) => setTopicName(e.target.value)}
            placeholder="e.g., Spring Security, React Hooks, Docker"
            className="w-full p-3 bg-gray-800 border border-gray-700 rounded-md text-gray-200 
                     focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent
                     placeholder-gray-400"
            required
          />
        </div>

        {/* Selector de categoría */}
        <div>
          <label className="block text-gray-300 mb-2">Category</label>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="w-full p-3 bg-gray-800 border border-gray-700 rounded-md text-gray-200 
                     focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            required
          >
            <option value="">Select a category</option>
            {CATEGORIAS.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>

        {/* Campo de título */}
        <div>
          <label className="block text-gray-300 mb-2">Title</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Type catching attention title"
            className="w-full p-3 bg-gray-800 border border-gray-700 rounded-md text-gray-200 
                     focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent
                     placeholder-gray-400"
            required
          />
        </div>

        {/* Campo de pregunta */}
        <div>
          <label className="block text-gray-300 mb-2">Question</label>
          <textarea
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Type your question"
            rows={8}
            className="w-full p-3 bg-gray-800 border border-gray-700 rounded-md text-gray-200 
                     focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent
                     placeholder-gray-400 resize-none"
            required
          />
        </div>

        {/* Botones de acción */}
        <div className="flex justify-between items-center pt-4">
          <button
            type="button"
            className="flex items-center px-4 py-2 text-blue-400 hover:bg-gray-800 rounded-md transition-colors"
          >
            <IoImageOutline className="w-5 h-5 mr-2" />
            Add Image
          </button>

          <div className="space-x-3">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 text-gray-300 bg-gray-800 rounded-md hover:bg-gray-700 transition-colors"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className={`px-4 py-2 text-white bg-orange-500 rounded-md hover:bg-orange-600 transition-colors
                        ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
              disabled={isLoading}
            >
              {isLoading ? 'Publishing...' : 'Publish'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default Ask;