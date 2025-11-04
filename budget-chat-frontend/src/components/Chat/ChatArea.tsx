import React from 'react';
import { Bot } from 'lucide-react';
import { ChatMessage } from './ChatMessage';
import { ChatInput } from './ChatInput';
import { Message } from '../../types';
import { LoadingSpinner } from '../UI/LoadingSpinner';

interface ChatAreaProps {
  messages: Message[];
  isLoading: boolean;
  isLoadingHistory?: boolean;
  messagesEndRef: React.RefObject<HTMLDivElement | null>;
  onSendMessage: (message: string) => void;
}

export const ChatArea: React.FC<ChatAreaProps> = ({ 
  messages, 
  isLoading, 
  isLoadingHistory = false,
  messagesEndRef, 
  onSendMessage 
}) => {
  return (
    <div className="flex-1 flex flex-col h-full">
      {/* Messages - with proper scrolling */}
      <div className="flex-1 overflow-y-auto p-6 space-y-4 chat-scroll">
        {isLoadingHistory ? (
          <div className="flex justify-center items-center h-full">
            <div className="flex items-center space-x-3">
              <LoadingSpinner className="text-blue-500" />
              <span className="text-gray-500">Loading conversation history...</span>
            </div>
          </div>
        ) : (
          <>
            {messages.map((message, index) => (
              <ChatMessage key={index} message={message} />
            ))}
            
            {isLoading && (
              <div className="flex justify-start">
                <div className="max-w-3xl flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gray-500 flex items-center justify-center">
                    <Bot className="h-5 w-5 text-white" />
                  </div>
                  <div className="p-4 rounded-lg bg-white shadow-md border border-gray-200">
                    <div className="flex items-center space-x-2">
                      <LoadingSpinner className="text-gray-500" />
                      <span className="text-gray-500">AI is thinking...</span>
                    </div>
                  </div>
                </div>
              </div>
            )}
            
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Fixed Input Area */}
      <div className="flex-shrink-0">
        <ChatInput onSendMessage={onSendMessage} isLoading={isLoading || isLoadingHistory} />
      </div>
    </div>
  );
};