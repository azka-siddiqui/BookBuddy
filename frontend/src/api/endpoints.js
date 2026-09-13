import { apiFetch } from './client';

/** Authentication */
export const authApi = {
  login: (username, password) =>
    apiFetch('/api/auth/login', { method: 'POST', body: { username, password }, auth: false }),
  register: (username, displayName, password) =>
    apiFetch('/api/auth/register', {
      method: 'POST',
      body: { username, displayName, password },
      auth: false,
    }),
  me: () => apiFetch('/api/auth/me'),
};

/** Catalog: search, rankings, wishlist, ratings */
export const catalogApi = {
  search: (query, limit = 20) =>
    apiFetch(`/api/books/search?q=${encodeURIComponent(query)}&limit=${limit}`),
  top: (by = 'rating', n = 5) => apiFetch(`/api/books/top?by=${by}&n=${n}`),
  addToWishlist: (bookId) => apiFetch(`/api/wishlist/${bookId}`, { method: 'POST' }),
  removeFromWishlist: (bookId) => apiFetch(`/api/wishlist/${bookId}`, { method: 'DELETE' }),
  rate: (bookId, score) => apiFetch(`/api/ratings/${bookId}`, { method: 'PUT', body: { score } }),
};

/** Profile: reading journey */
export const profileApi = {
  summary: () => apiFetch('/api/profile/summary'),
  books: (status) =>
    apiFetch(`/api/profile/books${status ? `?status=${status}` : ''}`),
  updateProgress: (bookId, status, pageReached) =>
    apiFetch('/api/profile/progress', {
      method: 'POST',
      body: { bookId, status, pageReached },
    }),
  completionRate: (bookId) => apiFetch(`/api/profile/books/${bookId}/completion-rate`),
};

/** Recommendations and common books */
export const recommendationsApi = {
  books: (limit = 6) => apiFetch(`/api/recommendations/books?limit=${limit}`),
  common: (withUsername) =>
    apiFetch(`/api/recommendations/common?withUsername=${encodeURIComponent(withUsername)}`),
};

/** Book clubs */
export const clubsApi = {
  list: () => apiFetch('/api/clubs'),
  join: (clubId) => apiFetch(`/api/clubs/${clubId}/join`, { method: 'POST' }),
  recommendations: (limit = 5) => apiFetch(`/api/clubs/recommendations?limit=${limit}`),
};
