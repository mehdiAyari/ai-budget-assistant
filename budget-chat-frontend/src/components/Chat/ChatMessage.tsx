import React from 'react';
import { User, Bot } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { Message } from '../../types';
import { formatDate } from '../../utils/formatters';

interface ChatMessageProps {
  message: Message;
}

export const ChatMessage: React.FC<ChatMessageProps> = ({ message }) => {
  const isUser = message.role === 'user';
  
  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div className={`max-w-3xl flex ${isUser ? 'flex-row-reverse' : 'flex-row'} items-start space-x-3`}>
        <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${
          isUser ? 'bg-blue-500' : 'bg-gray-500'
        }`}>
          {isUser ? 
            <User className="h-5 w-5 text-white" /> : 
            <Bot className="h-5 w-5 text-white" />
          }
        </div>
        <div className={`p-4 rounded-lg ${
          isUser 
            ? 'bg-blue-500 text-white' 
            : 'bg-white shadow-md border border-gray-200'
        }`}>
          {isUser ? (
            <div className="whitespace-pre-wrap text-white">{message.content}</div>
          ) : (
            <div className="markdown-content prose prose-sm max-w-none">
              <ReactMarkdown>
                {message.content}
              </ReactMarkdown>
            </div>
          )}
          <div className={`text-xs mt-2 ${
            isUser ? 'text-blue-100' : 'text-gray-500'
          }`}>
            {formatDate(message.timestamp)}
          </div>
        </div>
      </div>
    </div>
  );
};