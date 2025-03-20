'use client'
import { useState, useEffect, useRef } from 'react'
import Link from 'next/link'
import { api } from '@/services/api'
import { BsEye } from 'react-icons/bs'
import { FaRegCommentAlt } from 'react-icons/fa'
import { GoArrowUp } from 'react-icons/go'
import { SlOptionsVertical } from 'react-icons/sl'
import { VscAccount } from "react-icons/vsc";
import { format, formatDistanceToNow, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';
import { topicService } from '@/services/topicService'
import EditQuestion from './EditQuestion'
import { Question, Comment, TopicFromBackend, CommentFromBackend } from '@/types/questions_comments'

// Interfaces

const Questions = () => {
  const [expandedQuestionId, setExpandedQuestionId] = useState<number | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const menuRef = useRef<HTMLDivElement>(null);
  const [isMenuOpen, setIsMenuOpen] = useState<{type: 'question' | 'comment', id: number} | null>(null);
  const [loading, setLoading] = useState(true);
  const [currentUser, setCurrentUser] = useState<string>('');
  const [isEditing, setIsEditing] = useState(false);
  const [questionToEdit, setQuestionToEdit] = useState<Question | null>(null);
  const [newCommentText, setNewCommentText] = useState<string>('');
  const [sendingComment, setSendingComment] = useState<boolean>(false);
  // Estado para manejar la paginación de tópicos (preguntas)
  const [currentPage, setCurrentPage] = useState<number>(0); // 0-indexed para backend
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalElements, setTotalElements] = useState<number>(0);
  const TOPICS_PER_PAGE = 5; // Tópicos por página

  useEffect(() => {
    const storedUserName = localStorage.getItem('userName');

    if (storedUserName) {
      setCurrentUser(storedUserName);
    }

    fetchQuestions(0);

    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(null);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const fetchQuestions = async (page: number = 0) => {
    try {

      setLoading(true);
      const response = await api.get(`/topico?page=${page}&size=${TOPICS_PER_PAGE}`);
      setCurrentPage(response.data.pageNumber);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
      
      const questions: Question[] = response.data.content.map((topic: TopicFromBackend): Question => ({
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
          comments: topic.numeroRespuestas,
          upvotes: 0
        },
        comments: []
      }));
  
      // Obtener comentarios para cada pregunta
      const questionsWithComments: Question[] = await Promise.all(
        questions.map(async (question): Promise<Question> => {
          try {
            const commentsResponse = await api.get<CommentFromBackend[]>(`/topico/${question.id}/respuestas`);
            
            // Log para depuración
            console.log(`Comentarios para el tópico ${question.id}:`, commentsResponse.data);
  
            return {
              ...question,
              comments: commentsResponse.data.map((comment, index): Comment => ({
                id: comment.id ?? -(index + 1), // ID temporal usando el índice si no existe uno real
                author: {
                  name: comment.autor,
                  timeAgo: formatTimeAgo(comment.fechaCreacion)
                },
                content: comment.contenido
              }))
            };
          } catch (error) {
            console.error(`Error fetching comments for topic ${question.id}:`, error);
            return question;
          }
        })
      );
  
      setQuestions(questionsWithComments);
    } catch (error) {
      console.error('Error fetching questions:', error);
    } finally {
      setLoading(false);
    }
  };



  const formatTimeAgo = (dateString: string) => {
    try {
      const date = parseISO(dateString);
      // Formatear la fecha con hora en español
      return format(date, "dd 'de' MMMM 'de' yyyy 'a las' HH:mm", { locale: es });
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

  const handleDeleteItem = async () => {
    if (!isMenuOpen) return;
  
    try {
      await topicService.deleteTopic(isMenuOpen.id);
      
      // Verificar si era el último elemento de la página
      if (questions.length === 1 && currentPage > 0) {
        // Si es el último elemento y no estamos en la primera página, 
        // volver a la página anterior
        const newPage = currentPage - 1;
        setCurrentPage(newPage);
        await fetchQuestions(newPage);
      } else {
        // Si no es el último elemento o estamos en la primera página,
        // simplemente refrescar la página actual
        await fetchQuestions(currentPage);
      }
      
      setIsMenuOpen(null);
    } catch (error) {
      console.error('Error al eliminar:', error);
    }
  };

  const handleUpdateItem = async () => {
    if (!isMenuOpen) return;

    const questionToUpdate = questions.find(q => q.id === isMenuOpen.id);

    if (questionToUpdate) {
      setQuestionToEdit(questionToUpdate);
      setIsEditing(true);
    }

    setIsMenuOpen(null);
  };

  const handleAddComment = async (questionId: number) => {
    if (!newCommentText.trim()) return;
    
    try {
      setSendingComment(true);
      
      // Formato que espera el backend
      const commentData = {
        contenido: newCommentText
      };
      
      // Enviar al backend
      const response = await api.post(`/topico/${questionId}/respuestas`, commentData);
      
      console.log('Respuesta del servidor:', response.data);
      
      // Crear el nuevo comentario con los datos de la respuesta o con datos locales
      const newComment: Comment = {
        id: response.data.id || -Date.now(), // Usar ID del backend o un ID temporal negativo
        author: {
          name: currentUser,
          timeAgo: 'Ahora mismo'
        },
        content: newCommentText
      };
      
      // Actualizar el estado local para incluir el nuevo comentario
      setQuestions(questions.map(q => {
        if (q.id === questionId) {
          return {
            ...q,
            comments: [...q.comments, newComment],
            stats: {
              ...q.stats,
              comments: q.stats.comments + 1
            }
          };
        }
        return q;
      }));
      
      // Limpiar el input
      setNewCommentText('');
    } catch (error) {
      console.error('Error al añadir comentario:', error);
    } finally {
      setSendingComment(false);
    }
  };

  return (
    <div className="space-y-4">
      {/* Mostrar el formulario de edición si estamos en modo edición */}
      {isEditing && questionToEdit && (
        <EditQuestion 
          question={questionToEdit}
          onSave={(updatedQuestion) => {
            // Actualizar la lista de preguntas con la pregunta actualizada
            setQuestions(questions.map(q => 
              q.id === updatedQuestion.id ? updatedQuestion : q
            ));
            fetchQuestions(currentPage);
            setIsEditing(false);
            setQuestionToEdit(null);
          }}
          onCancel={() => {
            setIsEditing(false);
            setQuestionToEdit(null);
          }}
        />
      )}
  
      {/* Resto del componente solo si no estamos editando */}
      {!isEditing && (
        <>
          {/* Filtros */}
          <div className="flex space-x-2 mb-6">
          <button key="new-filter" className="px-4 py-1.5 bg-blue-500 text-white rounded-full text-sm font-medium">
            New
          </button>
          <button key="top-filter" className="px-4 py-1.5 text-gray-400 hover:bg-gray-800 rounded-full text-sm">
            Top
          </button>
          <button key="hot-filter" className="px-4 py-1.5 text-gray-400 hover:bg-gray-800 rounded-full text-sm">
            Hot
          </button>
          <button key="closed-filter" className="px-4 py-1.5 text-gray-400 hover:bg-gray-800 rounded-full text-sm">
            Closed
          </button>
        </div>
  
          {/* Lista de preguntas */}
          <div className="space-y-4">
            {questions.map((question) => (
              <div key={`question-${question.id}`} className='relative'>
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
                    {/* Mostrar opciones solo si el autor es el usuario actual */}
                    {question.author.name === currentUser && (
                      <button 
                        onClick={() => setIsMenuOpen({type: 'question', id: question.id})}
                        className="text-gray-400 hover:text-gray-300"
                      >
                        <SlOptionsVertical />
                      </button>
                    )}
                  </div>
  
                  <div className="mt-3">
                    <h2 className="text-white text-lg font-medium mb-2">{question.title}</h2>
                    <p className="text-gray-300">{question.content}</p>
                  </div>
  
                  <div className="mt-4 flex items-center justify-between">
                    <div className="flex space-x-2">
                      {question.tags.map((tag, index) => (
                        <span
                          key={`${question.id}-tag-${index}`}
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
                    {question.comments && question.comments.length > 0 ? (
                      question.comments.map((comment, index) => (
                        <div key={comment.id ? `comment-${comment.id}` : `comment-index-${index}`} className="bg-gray-800 rounded-lg p-4">
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
                            {comment.author.name === currentUser && (
                              <button 
                                onClick={() => setIsMenuOpen({type: 'comment', id: comment.id ?? -(index + 1)})}
                                className="text-gray-400 hover:text-gray-300"
                              >
                                <SlOptionsVertical />
                              </button>
                            )}
                          </div>

                          <p className="mt-3 text-gray-300">{comment.content}</p>
                        </div>
                      ))
                    ) : (
                      <div className="bg-gray-800 rounded-lg p-4 text-gray-400">
                        No hay comentarios para mostrar.
                      </div>
                    )}
                    
                    {/* Input para nuevo comentario */}
                    <div className="mt-4">
                      <input
                        type="text"
                        value={newCommentText}
                        onChange={(e) => setNewCommentText(e.target.value)}
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
                        <button 
                          onClick={() => handleAddComment(question.id)}
                          disabled={sendingComment || !newCommentText.trim()}
                          className={`px-4 py-2 text-white rounded-md ${
                            sendingComment || !newCommentText.trim() 
                              ? 'bg-gray-500' 
                              : 'bg-orange-500 hover:bg-orange-600'
                          }`}
                        >
                          {sendingComment ? 'Enviando...' : 'Suggest'}
                        </button>
                      </div>
                    </div>
                  </div>
                )}
  
                {/* Menú desplegable */}
                {isMenuOpen && isMenuOpen.id === question.id && (
                  <div 
                    key={`menu-${question.id}`}
                    ref={menuRef}
                    className="absolute right-0 top-0 mt-0 w-48 bg-white rounded-md shadow-lg py-1 border z-10"
                  >
                    <button 
                      onClick={handleDeleteItem}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >
                      Delete
                    </button>
                    <button 
                      onClick={handleUpdateItem}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >
                      Update
                    </button>
                  </div>
                )}
              </div>
            ))}

            {/* Después de la lista de preguntas */}
            {totalPages > 1 && (
              <div className="flex justify-center mt-8">
                <nav className="flex items-center space-x-2">
                  <button
                    onClick={() => fetchQuestions(currentPage - 1)}
                    disabled={currentPage === 0}
                    className={`px-4 py-2 rounded-md ${
                      currentPage === 0
                        ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                        : 'bg-gray-700 text-white hover:bg-gray-600'
                    }`}
                  >
                    Anterior
                  </button>

                  {/* Números de página */}
                  <div className="flex space-x-1">
                    {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
                      // Lógica para mostrar las páginas correctas
                      let pageToShow: number;
                      
                      if (totalPages <= 5) {
                        // Si hay 5 o menos páginas, mostrar todas
                        pageToShow = i;
                      } else if (currentPage < 3) {
                        // Si estamos al inicio, mostrar las primeras 5
                        pageToShow = i;
                      } else if (currentPage > totalPages - 3) {
                        // Si estamos al final, mostrar las últimas 5
                        pageToShow = totalPages - 5 + i;
                      } else {
                        // Si estamos en medio, mostrar 2 antes y 2 después
                        pageToShow = currentPage - 2 + i;
                      }
                      
                      // Solo mostrar botones para páginas válidas
                      if (pageToShow >= 0 && pageToShow < totalPages) {
                        return (
                          <button
                            key={`page-${pageToShow}`}
                            onClick={() => fetchQuestions(pageToShow)}
                            className={`w-10 h-10 flex items-center justify-center rounded-md ${
                              currentPage === pageToShow
                                ? 'bg-orange-500 text-white'
                                : 'bg-gray-700 text-white hover:bg-gray-600'
                            }`}
                          >
                            {pageToShow + 1} {/* +1 para mostrar número de página desde 1 */}
                          </button>
                        );
                      }
                      return null;
                    })}
                  </div>

                  <button
                    onClick={() => fetchQuestions(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                    className={`px-4 py-2 rounded-md ${
                      currentPage >= totalPages - 1
                        ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                        : 'bg-gray-700 text-white hover:bg-gray-600'
                    }`}
                  >
                    Siguiente
                  </button>
                </nav>
              </div>
            )}

            {/* Información de paginación */}
            {totalElements > 0 && (
              <div className="text-center mt-3 text-sm text-gray-400">
                Mostrando {currentPage * TOPICS_PER_PAGE + 1} - {Math.min((currentPage + 1) * TOPICS_PER_PAGE, totalElements)} de {totalElements} tópicos
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default Questions