export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}

export interface TotalsResponse {
  totalIncome: number;
  totalExpenses: number;
  netAmount: number;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

export interface ChatHistory {
  messages: ChatMessage[];
}