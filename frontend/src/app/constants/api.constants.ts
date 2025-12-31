
export const API = {
  WEBSOCKET_URL: "http://localhost:8181/ws",
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    REFRESH: '/auth/refresh'
  },

  USERS: {
    BASE: '/users',
    BY_ID: (id: string | number) => `/users/${id}`,
    PROFILE: '/users/profile'
  },

  MESSAGES: {
    BASE: '/messages',
    BY_ID: (id: string | number) => `/messages/${id}`
  },
  DASHBOARD: {
    BASE: "/dashboard"
  }
} as const;
