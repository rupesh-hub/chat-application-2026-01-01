import {environment} from '../../environments/environment-default';

export const API = {
  WEBSOCKET_URL: environment.WEBSOCKET_URI,
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
