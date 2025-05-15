import mockProducts  from '../data/mockProducts'; 

export const fetchTopProducts = async () => {
    // Simulate an API call to fetch top stores
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(mockProducts.slice(0, 10)); // Return the first 5 stores as top stores
      }, 1000);
    });
  }