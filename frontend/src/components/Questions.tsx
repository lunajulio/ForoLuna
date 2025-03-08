// components/Questions.tsx
import Image from 'next/image'
import { BsEye } from 'react-icons/bs'
import { FaRegCommentAlt } from 'react-icons/fa'
import { GoArrowUp } from 'react-icons/go'
import { SlOptionsVertical } from 'react-icons/sl'

const Questions = () => {
  const questions = [
    {
      id: 1,
      author: {
        name: 'Golanginya',
        avatar: '/avatars/golanginya.jpg', // Asegúrate de tener estas imágenes
        timeAgo: '5 min ago'
      },
      title: 'How to patch KDE on FreeBSD?',
      content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Consequat aliquet maecenas ut sit nulla',
      tags: ['golang', 'linux', 'overflow'],
      stats: {
        views: 125,
        comments: 15,
        upvotes: 155
      }
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
      }
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
        }
      },
    // ... más preguntas
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
          <div key={question.id} className="bg-gray-900 rounded-lg p-4">
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
                <div className="flex items-center space-x-1">
                  <FaRegCommentAlt />
                  <span>{question.stats.comments}</span>
                </div>
                <div className="flex items-center space-x-1">
                  <GoArrowUp />
                  <span>{question.stats.upvotes}</span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Questions