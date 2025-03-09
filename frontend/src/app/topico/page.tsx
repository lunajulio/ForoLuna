'use client'
import React from 'react'
import NavMain from '@/components/NavMain'
import Sidebar from '../../components/Sidebar'
import RightSidebar from '../../components/RightSidebar'
import Questions from '../../components/Questions'
import ProtectedRoute from '@/components/ProtectedRoute'

export default function Topico(){
  return (
    <ProtectedRoute>
    <div className="min-h-screen bg-black">
      <NavMain />
      <div className="max-w-7xl mx-auto px-4 flex">
        {/* Sidebar izquierdo */}
        <div className="w-64 flex-shrink-0 py-4">
          <Sidebar />
        </div>

        {/* Contenido principal - Questions */}
        <main className="flex-1 border-x border-gray-800 px-6 py-4">
          <Questions />
        </main>

        {/* Sidebar derecho */}
        <div className="w-80 flex-shrink-0 py-4 pl-6">
          <RightSidebar />
        </div>
      </div>
    </div>
    </ProtectedRoute>
  )
}


