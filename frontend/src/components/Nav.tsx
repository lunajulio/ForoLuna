import React from 'react'
import Link from 'next/link'

const Nav = () => {
  return (
    <nav className="sticky top-0 z-50 backdrop-blur-sm bg-black shadow-sm border-b transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo y nombre */}
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <span className="text-orange-500 text-2xl"></span>
            </div>
            <div className="ml-2">
              <span className="text-white">ForumLuna</span>
            </div>
          </div>

          {/* Botones de la derecha */}
          <div className="flex items-center space-x-4">
            <Link 
              href="/register" 
              className="bg-white text-black px-4 py-2 rounded-md hover:bg-slate-400 transition-colors"
            >
              Register
            </Link>
            <Link 
              href="/login" 
              className="bg-white text-black px-4 py-2 rounded-md hover:bg-slate-400 transition-colors"
            >
              Login
            </Link>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Nav