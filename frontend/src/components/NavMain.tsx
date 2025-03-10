'use client'
import React, { useState, useEffect, useRef } from 'react'
import Link from 'next/link'
import { IoNotificationsOutline } from 'react-icons/io5'
import profilePic from '../../public/profile-pic.svg'
import Image from 'next/image'
import { useRouter } from 'next/navigation'
import { api } from '@/services/api'

interface NavMainProps {
  onAskQuestion: () => void;
}

const NavMain: React.FC<NavMainProps> = ({ onAskQuestion }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [userName, setUserName] = useState<string>('');
  const menuRef = useRef<HTMLDivElement>(null);
  const router = useRouter();

  useEffect(() => {
    const storedUserName = localStorage.getItem('userName');
    if (storedUserName) {
      setUserName(storedUserName);
    } else {
      router.push('/login');
    }

    // Función para manejar clics fuera del menú
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(false);
      }
    };

    // Añadir event listener
    document.addEventListener('mousedown', handleClickOutside);

    // Limpiar el event listener
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [router]);

  const handleLogout = () => {
    // Limpiar localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    
    // Limpiar headers de axios
    delete api.defaults.headers.common['Authorization'];
    router.push('/login');
  };

  return (
    <nav className="sticky top-0 z-50 backdrop-blur-sm transition-colors bg-black/80 border-b">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex justify-between items-center h-14">
          {/* Logo y nombre */}
          <div className="flex items-center">
            <Link href="/" className="flex items-center">
              <div className="ml-2">
                <span className="text-white">ForumLuna</span>
              </div>
            </Link>
          </div>

          {/* Sección central - Open Question */}
          <div className="flex-1 flex justify-center px-4">
            <span className="text-white">Open Question</span>
          </div>

          {/* Botones de la derecha */}
          <div className="flex items-center space-x-4">
            <button 
              onClick={onAskQuestion}
              className="bg-white text-black px-4 py-1.5 rounded-md hover:bg-slate-400 transition-colors text-sm font-medium"
            >
              Ask a question
            </button>

            {/* Icono de notificaciones */}
            <button className="text-gray-300 hover:text-white">
              <IoNotificationsOutline className="w-6 h-6" />
            </button>

            {/* Avatar del usuario */}
            <div className="relative">
              <div className="flex items-center space-x-2">
                <span className="text-white">{userName}</span>
                <div className="w-8 h-8 rounded-full overflow-hidden">
                  <Image
                    src={profilePic}
                    alt="User avatar"
                    className="w-full h-full object-cover"
                  />
                </div>
                <button 
                  onClick={() => setIsMenuOpen(!isMenuOpen)}
                  className="text-gray-300 hover:text-white"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </button>
              </div>

              {/* Menú desplegable */}
              {isMenuOpen && (
                <div 
                  ref={menuRef}
                  className="absolute right-0 top-full mt-2 w-48 bg-white rounded-md shadow-lg py-1 border"
                >
                  <Link 
                    href="/profile" 
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Profile
                  </Link>
                  <Link 
                    href="/settings" 
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Settings
                  </Link>
                  <button 
                    onClick={handleLogout} 
                    className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Sign out
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default NavMain