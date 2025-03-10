'use client'
import { useState, useEffect } from 'react'
import { api } from '@/services/api'
import { BsEye } from 'react-icons/bs'
import { FaRegCommentAlt } from 'react-icons/fa'
import { GoArrowUp } from 'react-icons/go'
import { SlOptionsVertical } from 'react-icons/sl'
import { VscAccount } from "react-icons/vsc";
import { formatDistanceToNow, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';


// Interfaces
interface Comment {
  id: number;
  author: {
    name: string;
    timeAgo: string;
  };
  content: string;
  code?: string;
}

interface Question {
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

interface TopicFromBackend {
  id: number;
  titulo: string;
  mensaje: string;
  fechaCreacion: string;
  autor: string;
  curso: {
    nombre: string;
    categoria: string;
  };
  respuestas: Array<{
    id: number;
    mensaje: string;
    fechaCreacion: string;
    autor: string;
  }>;
}

const Questions = () => {
  const [expandedQuestionId, setExpandedQuestionId] = useState<number | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchQuestions();
  }, []);

  const fetchQuestions = async () => {
    try {
      const response = await api.get('/topico');
      const transformedQuestions = response.data.content.map((topic: TopicFromBackend) => ({
        id: topic.id,
        author: {
          name: topic.autor,
          timeAgo: formatTimeAgo(topic.fechaCreacion)
        },
        title: topic.titulo,
        content: topic.mensaje,
        tags: [topic.curso.nombre, topic.curso.categoria],
        stats: {
          views: 0,
          comments: topic.respuestas.length,
          upvotes: 0
        },
        comments: topic.respuestas.map(respuesta => ({
          id: respuesta.id,
          author: {
            name: respuesta.autor,
            timeAgo: formatTimeAgo(respuesta.fechaCreacion)
          },
          content: respuesta.mensaje
        }))
      }));

      setQuestions(transformedQuestions);
    } catch (error) {
      console.error('Error fetching questions:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatTimeAgo = (dateString: string) => {
    try {
      const date = parseISO(dateString);
      return formatDistanceToNow(date, { 
        addSuffix: true,
        locale: es 
      });
    } catch (error) {
      console.error('Error formatting date:', error);
      return 'fecha desconocida';
    }
  };

  const toggleComments = (questionId: number) => {
    setExpandedQuestionId(expandedQuestionId === questionId ? null : questionId);
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-white">Loading questions...</div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Filtros */}
      <div className="flex space-x-2 mb-6">
        <button className="px-4 py-1.5 bg-blue-500 text-white rounded-full text-sm font-medium">
          New
        </button>
        <button className="px-4 py-1.5 text-gray-400 hover:bg-gray-800 rounded-full text-sm">
          Top
        </button>
        <button className="px-4 py-1.5 text-gray-400 hover:bg-gray-800 rounded-full text-sm">
          Hot
        </button>
        <button className="px-4 py-1.5 text-gray-400 hover:bg-gray-800 rounded-full text-sm">
          Closed
        </button>
      </div>

      {/* Lista de preguntas */}
      <div className="space-y-4">
        {questions.map((question) => (
          <div key={question.id}>
            <div className="bg-gray-900 rounded-lg p-4">
              <div className="flex justify-between items-start">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-full overflow-hidden flex items-center justify-center bg-gray-700">
                    <VscAccount className="w-6 h-6 text-gray-300" />
                  </div>
                  <div>
                    <h3 className="text-white font-medium">{question.author.name}</h3>
                    <p className="text-gray-400 text-sm">{question.author.timeAgo}</p>
                  </div>
                </div>
                <button className="text-gray-400 hover:text-gray-300">
                  <SlOptionsVertical />
                </button>
              </div>

              <div className="mt-3">
                <h2 className="text-white text-lg font-medium mb-2">{question.title}</h2>
                <p className="text-gray-300">{question.content}</p>
              </div>

              <div className="mt-4 flex items-center justify-between">
                <div className="flex space-x-2">
                  {question.tags.map((tag) => (
                    <span
                      key={tag}
                      className="px-3 py-1 bg-gray-800 text-gray-300 rounded-full text-sm"
                    >
                      {tag}
                    </span>
                  ))}
                </div>

                <div className="flex items-center space-x-6 text-gray-400">
                  <div className="flex items-center space-x-1">
                    <BsEye />
                    <span>{question.stats.views}</span>
                  </div>
                  <button 
                    onClick={() => toggleComments(question.id)}
                    className="flex items-center space-x-1 hover:text-gray-300"
                  >
                    <FaRegCommentAlt />
                    <span>{question.stats.comments}</span>
                  </button>
                  <div className="flex items-center space-x-1">
                    <GoArrowUp />
                    <span>{question.stats.upvotes}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Sección de comentarios expandible */}
            {expandedQuestionId === question.id && (
              <div className="mt-4 space-y-4">
                {question.comments?.map((comment) => (
                  <div key={comment.id} className="bg-gray-800 rounded-lg p-4">
                    <div className="flex items-start justify-between">
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 rounded-full overflow-hidden flex items-center justify-center bg-gray-700">
                          <VscAccount className="w-6 h-6 text-gray-300" />
                        </div>
                        <div>
                          <span className="text-white">{comment.author.name}</span>
                          <p className="text-gray-400 text-sm">{comment.author.timeAgo}</p>
                        </div>
                      </div>
                      <button className="text-gray-400 hover:text-gray-300">
                        <SlOptionsVertical />
                      </button>
                    </div>

                    <p className="mt-3 text-gray-300">{comment.content}</p>

                    {comment.code && (
                      <pre className="mt-3 bg-gray-900 p-4 rounded-md text-gray-300 font-mono text-sm">
                        <code>{comment.code}</code>
                      </pre>
                    )}
                  </div>
                ))}

                {/* Input para nuevo comentario */}
                <div className="mt-4">
                  <input
                    type="text"
                    placeholder="Type here your wise suggestion"
                    className="w-full bg-gray-800 border border-gray-700 rounded-md p-3 text-white placeholder-gray-400"
                  />
                  <div className="flex justify-end mt-2 space-x-2">
                    <button 
                      onClick={() => setExpandedQuestionId(null)}
                      className="px-4 py-2 text-gray-400 hover:text-gray-300"
                    >
                      Cancel
                    </button>
                    <button className="px-4 py-2 bg-orange-500 text-white rounded-md hover:bg-orange-600">
                      Suggest
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

export default Questions