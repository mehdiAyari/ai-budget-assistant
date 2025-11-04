import React from 'react';
import { TrendingUp, TrendingDown, DollarSign } from 'lucide-react';
import { QuickStats as QuickStatsType } from '../../types';
import { formatCurrency } from '../../utils/formatters';

interface QuickStatsProps {
  stats: QuickStatsType;
}

export const QuickStats: React.FC<QuickStatsProps> = ({ stats }) => {
  return (
    <div className="p-6 border-b border-gray-200">
      <h2 className="text-xl font-bold text-gray-800 mb-4">Budget Overview</h2>
      
      <div className="space-y-3">
        <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
          <div className="flex items-center">
            <TrendingUp className="h-5 w-5 text-green-500 mr-2" />
            <span className="text-sm font-medium text-gray-700">Income</span>
          </div>
          <span className="font-semibold text-green-600">
            {formatCurrency(stats.totalIncome)}
          </span>
        </div>
        
        <div className="flex items-center justify-between p-3 bg-red-50 rounded-lg">
          <div className="flex items-center">
            <TrendingDown className="h-5 w-5 text-red-500 mr-2" />
            <span className="text-sm font-medium text-gray-700">Expenses</span>
          </div>
          <span className="font-semibold text-red-600">
            {formatCurrency(stats.totalExpenses)}
          </span>
        </div>
        
        <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
          <div className="flex items-center">
            <DollarSign className="h-5 w-5 text-blue-500 mr-2" />
            <span className="text-sm font-medium text-gray-700">Net</span>
          </div>
          <span className={`font-semibold ${
            stats.netAmount >= 0 ? 'text-green-600' : 'text-red-600'
          }`}>
            {formatCurrency(stats.netAmount)}
          </span>
        </div>
      </div>
    </div>
  );
};