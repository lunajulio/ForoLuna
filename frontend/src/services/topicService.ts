import { api } from './api';

interface DatosActualizarTopico {
  titulo?: string;
  mensaje?: string;
}

export const topicService = {
  // Actualizar tópico
  updateTopic: async (id: number, data: DatosActualizarTopico) => {
    const response = await api.put(`/topico/${id}`, data);
    return response.data;
  },

  // Eliminar tópico (eliminación lógica)
  deleteTopic: async (id: number) => {
    const response = await api.delete(`/topico/${id}`);
    return response.data;
  }
};
