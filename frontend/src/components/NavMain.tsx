'use client'
import React from 'react'
import Link from 'next/link'
import { IoNotificationsOutline } from 'react-icons/io5'
import profilePic from '../../public/profile-pic.svg'
import Image from 'next/image'
import { useState } from 'react'

const NavMain = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  return (
    <nav className="bg-black border-b">
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
              className="bg-white text-black px-4 py-1.5 rounded-md hover:bg-slate-400 transition-colors text-sm font-medium"
            >
              Ask a question
            </button>

            {/* Icono de notificaciones */}
            <button className="text-gray-600 hover:text-gray-800">
              <IoNotificationsOutline className="w-6 h-6" />
            </button>

            {/* Avatar del usuario */}
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 rounded-full overflow-hidden">
                <Image
                  src= {profilePic}
                  alt="User avatar"
                  className="w-full h-full object-cover"
                />
              </div>
              <button className="text-gray-600 hover:text-gray-800">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Menú desplegable */}
      {isMenuOpen && (
        <div className="absolute right-4 top-16 w-48 bg-white rounded-md shadow-lg py-1 border">
          <Link href="/profile" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
            Profile
          </Link>
          <Link href="/settings" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
            Settings
          </Link>
          <button 
            onClick={() => {/* lógica de logout */}} 
            className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          >
            Sign out
          </button>
        </div>
      )}
    </nav>


  )
}

export default NavMain
