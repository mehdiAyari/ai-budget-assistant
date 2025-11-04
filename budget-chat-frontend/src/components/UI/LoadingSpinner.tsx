import React from 'react';
import { Loader } from 'lucide-react';

interface LoadingSpinnerProps {
  size?: number;
  className?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ 
  size = 4, 
  className = '' 
}) => {
  return (
    <Loader 
      className={`h-${size} w-${size} animate-spin ${className}`} 
    />
  );
};