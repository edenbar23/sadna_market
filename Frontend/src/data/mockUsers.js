export const mockUsers = [
  {
      role: "user",
      username: "alice123",
      stores: ["1", "2"],
  
      // storeId -> { productId -> quantity }
      cart: {
        s1: {
          p1: 2,
          p2: 1,
        },
        s3: {
          p5: 3,
        },
      },
  
      // List of order IDs
      orders: ["o1", "o3", "o7"],
  
      // List of message IDs
      messages: ["m1", "m2", "m10"],
    },
  
    {
      username: "bob456",
      role: "user",
      stores: [],
  
      cart: {
        s2: {
          p3: 1,
          p4: 2,
        },
      },
  
      orders: ["o4", "o8"],
      messages: ["m3", "m5", "m8"],
    },
  
    {
      username: "charlie789",
      role: "admin",
      stores: ["s3"],
  
      cart: {},
  
      orders: [],
      messages: [],
    },
  ];
  
  