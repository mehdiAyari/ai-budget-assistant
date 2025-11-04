export const formatCurrency = (amount: number): string => {
    return new Intl.NumberFormat('en-US', { 
      style: 'currency', 
      currency: 'USD' 
    }).format(amount || 0);
  };
  
  export const formatDate = (timestamp: number): string => {
    return new Date(timestamp).toLocaleTimeString();
  };
  
  export const formatLongDate = (date: Date): string => {
    return date.toLocaleDateString('en-US', { 
      month: 'long', 
      year: 'numeric' 
    });
  };