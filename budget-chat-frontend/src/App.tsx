import React, { useState } from 'react';
import { Header } from './components/Layout/Header';
import { Sidebar } from './components/Sidebar/Sidebar';
import { ChatArea } from './components/Chat/ChatArea';
import { useChat } from './hooks/useChat';
import { Menu, X } from 'lucide-react';
import './App.css';

const App: React.FC = () => {
  const {
    messages,
    isLoading,
    isLoadingHistory,
    quickStats,
    messagesEndRef,
    sendMessage,
    clearChatMemory
  } = useChat();

  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleQuickAction = (action: string) => {
    sendMessage(action);
    setSidebarOpen(false); // Close sidebar on mobile after action
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Mobile sidebar backdrop */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
      
      {/* Fixed Sidebar - Hidden on mobile, fixed on desktop */}
      <div className={`
        w-80 bg-white shadow-lg flex flex-col fixed left-0 top-0 h-full z-30 transform transition-transform duration-300 ease-in-out
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} 
        lg:translate-x-0
      `}>
        {/* Mobile close button */}
        <div className="lg:hidden p-4 border-b border-gray-200">
          <button
            onClick={() => setSidebarOpen(false)}
            className="p-2 rounded-lg hover:bg-gray-100"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        
        <Sidebar 
          stats={quickStats} 
          onActionClick={handleQuickAction}
          onClearMemory={clearChatMemory}
        />
      </div>
      
      {/* Main Content Area */}
      <div className="flex-1 flex flex-col lg:ml-80">
        {/* Header with mobile menu button */}
        <div className="bg-white shadow-sm border-b border-gray-200 px-6 py-4 flex items-center justify-between">
          <button
            className="lg:hidden p-2 rounded-lg hover:bg-gray-100"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu className="h-5 w-5" />
          </button>
          <div className="flex-1 lg:flex-none">
            <Header />
          </div>
        </div>
        
        <ChatArea 
          messages={messages}
          isLoading={isLoading}
          isLoadingHistory={isLoadingHistory}
          messagesEndRef={messagesEndRef}
          onSendMessage={sendMessage}
        />
      </div>
    </div>
  );
};

export default App;