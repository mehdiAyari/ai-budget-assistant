export interface Message {
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

export interface QuickStats {
  totalIncome: number;
  totalExpenses: number;
  netAmount: number;
}

export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  role: 'assistant';
  content: string;
  timestamp: number;
  success?: boolean;
}

export interface ApiError {
  message: string;
  status?: number;
}