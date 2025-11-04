import { useState, useCallback, useRef, useEffect } from 'react';
import { Message, QuickStats } from '../types';
import { apiService } from '../services/api';

export const useChat = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingHistory, setIsLoadingHistory] = useState(true);
  const [quickStats, setQuickStats] = useState<QuickStats>({ 
    totalIncome: 0, 
    totalExpenses: 0, 
    netAmount: 0 
  });
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const currentMonth = new Date().getMonth() + 1;
  const currentYear = new Date().getFullYear();

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  const fetchQuickStats = useCallback(async () => {
    try {
      const stats = await apiService.getQuickStats(currentYear, currentMonth);
      setQuickStats(stats);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  }, [currentYear, currentMonth]);

  const convertBackendMessage = (backendMsg: any): Message => {
    const role = backendMsg.messageType?.toLowerCase() === 'user' ? 'user' : 'assistant';
    return {
      role: role as 'user' | 'assistant',
      content: backendMsg.content || backendMsg.text || '',
      timestamp: Date.now()
    };
  };

  const showWelcomeMessage = useCallback(() => {
    return {
      role: 'assistant' as const,
      content: `Hello! I'm your AI budget assistant with **conversation memory**. I can help you manage your finances through natural conversation.

**Here's what I can do:**
- "Add $50 expense for groceries to Food category"
- "Create a budget of $300 for Transportation this month" 
- "Show me my current spending"
- "I spent $25 on coffee today"
- "Set up a $1000 budget for rent"

**I remember our conversation**, so you can ask follow-up questions like:
- "How much have I spent so far?"
- "Add another $20 to that category"
- "What was my budget for that again?"

Just **talk to me naturally** and I'll help manage your budget while remembering our context!`,
      timestamp: Date.now()
    };
  }, []);

  const loadChatHistory = useCallback(async () => {
    try {
      setIsLoadingHistory(true);
      const history = await apiService.getChatHistory();
      const welcomeMessage = showWelcomeMessage();
      
      if (history && history.length > 0) {
        const convertedMessages = history
          .filter(msg => msg.content || msg.text)
          .map(convertBackendMessage);
        
        setMessages([welcomeMessage, ...convertedMessages]);
      } else {
        setMessages([welcomeMessage]);
      }
    } catch (error) {
      console.error('Error loading chat history:', error);
      setMessages([showWelcomeMessage()]);
    } finally {
      setIsLoadingHistory(false);
    }
  }, [showWelcomeMessage]);

  const sendMessage = useCallback(async (inputMessage: string) => {
    if (!inputMessage.trim() || isLoading) return;

    const userMessage: Message = { 
      role: 'user', 
      content: inputMessage, 
      timestamp: Date.now() 
    };
    
    setMessages(prev => [...prev, userMessage]);
    setIsLoading(true);

    try {
      const aiResponse = await apiService.sendChatMessage(inputMessage);
      setMessages(prev => [...prev, aiResponse]);
      
      setTimeout(() => {
        fetchQuickStats();
      }, 500);
      
    } catch (error) {
      const errorMessage: Message = {
        role: 'assistant',
        content: 'Sorry, I encountered an error. Please make sure the backend is running and try again.',
        timestamp: Date.now()
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  }, [isLoading, fetchQuickStats]);

  const clearChatMemory = useCallback(async () => {
    try {
      await apiService.clearChatMemory();
      setMessages([showWelcomeMessage()]);
    } catch (error) {
      console.error('Error clearing chat memory:', error);
    }
  }, [showWelcomeMessage]);

  const initializeApp = useCallback(() => {
    fetchQuickStats();
    loadChatHistory();
  }, [fetchQuickStats, loadChatHistory]);

  useEffect(() => {
    initializeApp();
  }, [initializeApp]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  return {
    messages,
    isLoading,
    isLoadingHistory,
    quickStats,
    messagesEndRef,
    sendMessage,
    clearChatMemory,
    fetchQuickStats
  };
};
