import React from 'react';
import { Bot, Calendar } from 'lucide-react';
import { formatLongDate } from '../../utils/formatters';

export const Header: React.FC = () => {
  return (
    <div className="bg-white shadow-sm border-b border-gray-200 px-6 py-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <Bot className="h-8 w-8 text-blue-500 mr-3" />
          <div>
            <h1 className="text-xl font-bold text-gray-800">AI Budget Assistant</h1>
            <p className="text-sm text-gray-500 flex items-center">
              <Calendar className="inline h-4 w-4 mr-1" />
              {formatLongDate(new Date())}
            </p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-sm text-gray-500">Status</p>
          <p className="text-sm font-medium text-green-600">● Online</p>
        </div>
      </div>
    </div>
  );
};