// components/Questions.tsx
'use client'
import { useState } from 'react'
import Image from 'next/image'
import { BsEye } from 'react-icons/bs'
import { FaRegCommentAlt } from 'react-icons/fa'
import { GoArrowUp } from 'react-icons/go'
import { SlOptionsVertical } from 'react-icons/sl'

interface Comment {
  id: number;
  author: {
    name: string;
    avatar: string;
    timeAgo: string;
  };
  content: string;
  code?: string;
}

interface Question {
  id: number;
  author: {
    name: string;
    avatar: string;
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

const Questions = () => {
  const [expandedQuestionId, setExpandedQuestionId] = useState<number | null>(null);

  const toggleComments = (questionId: number) => {
    setExpandedQuestionId(expandedQuestionId === questionId ? null : questionId);
  }

  const questions = [
    {
      id: 1,
      author: {
        name: 'Golanginya',
        avatar: '/avatars/golanginya.jpg',
        timeAgo: '5 min ago'
      },
      title: 'How to patch KDE on FreeBSD?',
      content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Consequat aliquet maecenas ut sit nulla',
      tags: ['golang', 'linux', 'overflow'],
      stats: {
        views: 125,
        comments: 15,
        upvotes: 155
      },
      comments: [
        {
          id: 1,
          author: {
            name: '@unkind',
            avatar: '/avatars/unkind.jpg',
            timeAgo: '12 November 2020 19:35'
          },
          content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
          code: 'package main'
        }
      ]
    },
    {
      id: 2,
      author: {
        name: 'Linuxoid',
        avatar: '/avatars/linuxoid.jpg',
        timeAgo: '25 min ago'
      },
      title: 'What is a difference between Java nad JavaScript?',
      content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Bibendum vitae etiam lectus amet enim.',
      tags: ['java', 'javascript', 'wtf'],
      stats: {
        views: 125,
        comments: 15,
        upvotes: 155
      },
      comments: [
        {
          id: 1,
          author: {
            name: '@unkind',
            avatar: '/avatars/unkind.jpg',
            timeAgo: '12 November 2020 19:35'
          },
          content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
          code: 'package main'
        }
      ]
    },
    {
      id: 3,
      author: {
        name: 'Linuxoid',
        avatar: '/avatars/linuxoid.jpg',
        timeAgo: '25 min ago'
      },
      title: 'What is a difference between Java nad JavaScript?',
      content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Bibendum vitae etiam lectus amet enim.',
      tags: ['java', 'javascript', 'wtf'],
      stats: {
        views: 125,
        comments: 15,
        upvotes: 155
      },
      comments: [
        {
          id: 1,
          author: {
            name: '@unkind',
            avatar: '/avatars/unkind.jpg',
            timeAgo: '12 November 2020 19:35'
          },
          content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
          code: 'package main'
        }
      ]
    }
  ]

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
                  <div className="w-10 h-10 rounded-full overflow-hidden">
                    <Image
                      src={question.author.avatar}
                      alt={question.author.name}
                      width={40}
                      height={40}
                      className="object-cover"
                    />
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
                        <div className="w-8 h-8 rounded-full overflow-hidden">
                          <Image
                            src={comment.author.avatar}
                            alt={comment.author.name}
                            width={32}
                            height={32}
                            className="object-cover"
                          />
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