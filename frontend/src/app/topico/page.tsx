'use client'
import React from 'react'
import NavMain from '@/components/NavMain'
import Sidebar from '../../components/Sidebar'
import RightSidebar from '../../components/RightSidebar'
import Questions from '../../components/Questions'
import Ask from '../../components/Ask'
import ProtectedRoute from '@/components/ProtectedRoute'
import { useRouter } from 'next/navigation'
import { useState } from 'react'


export default function Topico(){
  const router = useRouter();

  const [isAsking, setIsAsking] = useState(false);

  // Función que se pasa a NavMain
  const handleAskQuestion = () => {
    setIsAsking(true);
  };

  // Funciones que se pasan a Ask
  const handleQuestionSubmitted = () => {
    setIsAsking(false);
    // Aquí podrías recargar las preguntas
  };

  const handleCancelAsk = () => {
    setIsAsking(false);
  };

  return (
    <ProtectedRoute>
    <div className="min-h-screen bg-black">
      <NavMain onAskQuestion={handleAskQuestion}/>
      <div className="max-w-7xl mx-auto px-4 flex">
        {/* Sidebar izquierdo */}
        <div className="w-64 flex-shrink-0 py-4">
          <Sidebar />
        </div>

        <main className="flex-1 border-x border-gray-800 px-6 py-4">
            {isAsking ? (
              <Ask 
                onSubmit={handleQuestionSubmitted} 
                onCancel={handleCancelAsk} 
              />
            ) : (
              <Questions />
            )}
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


