'use client'
import React, { useState } from 'react';
import { topicService } from '../services/topicService';
import { Question } from '@/types/questions_comments'
import { getErrorMessage } from '@/services/api'



interface EditQuestionProps {
  question: Question;
  onSave: (updatedQuestion: Question) => void;
  onCancel: () => void;
}

const EditQuestion: React.FC<EditQuestionProps> = ({ question, onSave, onCancel }) => {
    const [title, setTitle] = useState(question.title);
    const [content, setContent] = useState(question.content);
    const [error, setError] = useState('');
  
    const handleSubmit = async (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        
        try {
          await topicService.updateTopic(question.id, {
            titulo: title,
            mensaje: content
          });
          
          onSave({
            ...question,  
            title,        
            content       
          });
        } catch (error) {
          setError(getErrorMessage(error, 'Error al actualizar la pregunta'));
        }
      };
  
    return (
      <div className="bg-gray-900 rounded-lg p-6">
        <h2 className="text-xl font-bold text-white mb-4">Editar pregunta</h2>
        {error && (
          <div className="bg-red-500 text-white p-3 rounded-md mb-4">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-white mb-2">Título</label>
            <input 
              type="text" 
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full bg-gray-800 border border-gray-700 rounded-md p-3 text-white"
              required
            />
          </div>
          <div className="mb-4">
            <label className="block text-white mb-2">Contenido</label>
            <textarea 
              value={content}
              onChange={(e) => setContent(e.target.value)}
              className="w-full bg-gray-800 border border-gray-700 rounded-md p-3 text-white min-h-32"
              required
            />
          </div>
          <div className="flex justify-end space-x-3">
            <button 
              type="button"
              onClick={onCancel}
              className="px-4 py-2 text-gray-400 hover:text-gray-300"
            >
              Cancelar
            </button>
            <button 
              type="submit"
              className="px-4 py-2 bg-orange-500 text-white rounded-md hover:bg-orange-600"
            >
              Guardar
            </button>
          </div>
        </form>
      </div>
    );
  };

export default EditQuestion;