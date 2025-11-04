import React from 'react';
import { QuickStats } from './QuickStats';
import { QuickActions } from './QuickActions';
import { ExampleCommands } from './ExampleCommands';
import { QuickStats as QuickStatsType } from '../../types';
import { Button } from '../UI/Button';
import { RotateCcw } from 'lucide-react';

interface SidebarProps {
  stats: QuickStatsType;
  onActionClick: (action: string) => void;
  onClearMemory?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ 
  stats, 
  onActionClick, 
  onClearMemory 
}) => {
  return (
    <div className="w-full h-full bg-white shadow-lg flex flex-col">
      <QuickStats stats={stats} />
      <QuickActions onActionClick={onActionClick} />
      
      {/* Chat Memory Section */}
      {onClearMemory && (
        <div className="p-4 border-b border-gray-200">
          <h3 className="text-sm font-semibold text-gray-700 mb-3">💭 Chat Memory</h3>
          <p className="text-xs text-gray-500 mb-3">
            I remember our conversation context. Clear memory to start fresh.
          </p>
          <Button
            variant="outline"
            size="sm"
            onClick={onClearMemory}
            className="w-full text-xs"
          >
            <RotateCcw className="h-3 w-3 mr-1" />
            Clear Memory
          </Button>
        </div>
      )}
      
      <ExampleCommands />
    </div>
  );
};