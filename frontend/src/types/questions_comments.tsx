export interface Comment {
  id: number;
  author: {
    name: string;
    timeAgo: string;
  };
  content: string;
  code?: string;
}

export interface Question {
  id: number;
  author: {
    name: string;
    timeAgo: string;
  };
  title: string;
  content: string;
  tags: string[];
  stats: {
    views: number;
    comments: number;
    upvotes: number;
  };
  comments: Comment[];
}

// Interfaz para la respuesta del backend
export interface TopicFromBackend {
  id: number;
  titulo: string;
  mensaje: string;
  fechaCreacion: string;
  autor: string;
  curso: {
    nombre: string;
    categoria: string;
  };
  numeroRespuestas: number;
}

// Interfaz para la respuesta de comentarios del backend
export interface CommentFromBackend {
  id: number;
  contenido: string;
  fechaCreacion: string;
  autor: string;
}