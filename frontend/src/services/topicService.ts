import { api } from './api';

interface DatosActualizarTopico {
  id: number;
  titulo?: string;
  mensaje?: string;
  cursoNombre?: string;
  cursoCategoría?: string;
}

export const topicService = {
  // Actualizar tópico
  updateTopic: async (id: number, data: DatosActualizarTopico) => {
    try {
      const response = await api.put(`/topico/${id}`, data);
      return response.data;
    } catch (error) {
      console.error('Error updating topic:', error);
      throw error;
    }
  },

  // Eliminar tópico (eliminación lógica)
  deleteTopic: async (id: number) => {
    try {
      const response = await api.delete(`/topico/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error deleting topic:', error);
      throw error;
    }
  }
};