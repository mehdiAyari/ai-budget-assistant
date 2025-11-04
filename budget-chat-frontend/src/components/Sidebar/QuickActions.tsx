import React from 'react';
import { Button } from '../UI/Button';

interface QuickActionsProps {
  onActionClick: (action: string) => void;
}

const quickActions = [
  { text: "Show my budgets", action: "Show me all my current budgets" },
  { text: "Add expense", action: "I want to add an expense" },
  { text: "Create budget", action: "Help me create a new budget" },
  { text: "Financial advice", action: "Give me financial advice based on my spending" }
];

export const QuickActions: React.FC<QuickActionsProps> = ({ onActionClick }) => {
  return (
    <div className="p-6 border-b border-gray-200">
      <h3 className="text-lg font-semibold text-gray-800 mb-3">Quick Actions</h3>
      <div className="flex flex-wrap gap-2">
        {quickActions.map((item, index) => (
          <Button
            key={index}
            variant="quick-action"
            size="sm"
            onClick={() => onActionClick(item.action)}
          >
            {item.text}
          </Button>
        ))}
      </div>
    </div>
  );
};