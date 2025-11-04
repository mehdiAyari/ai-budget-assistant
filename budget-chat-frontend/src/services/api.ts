import { ChatRequest, ChatResponse, QuickStats, ApiError } from '../types';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api';

class ApiService {
  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const error: ApiError = {
        message: `HTTP error! status: ${response.status}`,
        status: response.status
      };
      throw error;
    }
    
    return response.json();
  }

  async sendChatMessage(message: string): Promise<ChatResponse> {
    const request: ChatRequest = { message };
    
    const response = await fetch(`${API_BASE}/chat/message`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    return this.handleResponse<ChatResponse>(response);
  }

  async getChatHistory(): Promise<any[]> {
    const response = await fetch(`${API_BASE}/chat/history`);
    return this.handleResponse<any[]>(response);
  }

  async clearChatMemory(): Promise<{ message: string }> {
    const response = await fetch(`${API_BASE}/chat/memory`, {
      method: 'DELETE',
    });

    return this.handleResponse<{ message: string }>(response);
  }

  async getQuickStats(year: number, month: number): Promise<QuickStats> {
    const response = await fetch(`${API_BASE}/transactions/totals/${year}/${month}`);
    return this.handleResponse<QuickStats>(response);
  }

  async healthCheck(): Promise<{ status: string }> {
    const response = await fetch(`${API_BASE}/health`);
    return this.handleResponse<{ status: string }>(response);
  }

  async getMcpStatus(): Promise<{ status: string }> {
    const response = await fetch(`${API_BASE}/mcp/status`);
    return this.handleResponse<{ status: string }>(response);
  }
}

export const apiService = new ApiService();
