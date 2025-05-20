import mockProducts  from '../data/mockProducts'; 

export const fetchTopProducts = async () => {
    // Simulate an API call to fetch top stores
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(mockProducts.slice(0, 10)); // Return the first 5 stores as top stores
      }, 1000);
    });
  }

export const searchProducts = async (searchParams) => {
    // For development/testing, we can use mock data
    // Later you can replace this with actual API calls
    const mockProducts = await fetchTopProducts(); // Reuse your existing function

    // Filter products based on search query if provided
    if (searchParams.query) {
        const query = searchParams.query.toLowerCase();
        return mockProducts.filter(product =>
            product.name.toLowerCase().includes(query) ||
            (product.description && product.description.toLowerCase().includes(query)) ||
            (product.category && product.category.toLowerCase().includes(query))
        );
    }

    return mockProducts;
};