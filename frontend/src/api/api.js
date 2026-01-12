/**
 * Service API pour communiquer avec le backend Spring Boot
 * Tous les appels API transitent par ce service
 */

const API_BASE_URL = '/api'; // Utilise le proxy Vite

/**
 * Fonction utilitaire pour faire des requêtes
 */
const fetchAPI = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`;
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  try {
    const response = await fetch(url, { ...defaultOptions, ...options });
    
    if (!response.ok) {
      const errorData = await response.text();
      throw new Error(`Erreur ${response.status}: ${errorData}`);
    }

    // Vérifier si la réponse contient du JSON
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return response;
  } catch (error) {
    console.error('Erreur API:', error);
    throw error;
  }
};

/**
 * ========== EQUIPE ==========
 */
export const equipeAPI = {
  getAll: async () => fetchAPI('/equipes'),
  getById: async (id) => fetchAPI(`/equipes/${id}`),
  create: async (data) => fetchAPI('/equipes', { method: 'POST', body: JSON.stringify(data) }),
  update: async (id, data) => fetchAPI(`/equipes/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: async (id) => fetchAPI(`/equipes/${id}`, { method: 'DELETE' }),
};

/**
 * ========== JOUEUR ==========
 */
export const joueurAPI = {
  getAll: async () => fetchAPI('/joueurs'),
  getById: async (id) => fetchAPI(`/joueurs/${id}`),
  getByEquipe: async (equipeId) => fetchAPI(`/joueurs/equipe/${equipeId}`),
  create: async (data) => fetchAPI('/joueurs', { method: 'POST', body: JSON.stringify(data) }),
  update: async (id, data) => fetchAPI(`/joueurs/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: async (id) => fetchAPI(`/joueurs/${id}`, { method: 'DELETE' }),
};

/**
 * ========== ARENE ==========
 */
export const areneAPI = {
  getAll: async () => fetchAPI('/arenes'),
  getById: async (id) => fetchAPI(`/arenes/${id}`),
  getBySport: async (sport) => fetchAPI(`/arenes/sport/${sport}`),
  getByEquipe: async (equipeId) => fetchAPI(`/arenes/equipe/${equipeId}`),
  create: async (data) => fetchAPI('/arenes', { method: 'POST', body: JSON.stringify(data) }),
  update: async (id, data) => fetchAPI(`/arenes/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: async (id) => fetchAPI(`/arenes/${id}`, { method: 'DELETE' }),
};

/**
 * ========== SPORT ==========
 */
export const sportAPI = {
  getAll: async () => fetchAPI('/sports'),
  getById: async (id) => fetchAPI(`/sports/${id}`),
  getByCode: async (code) => fetchAPI(`/sports/code/${code}`),
  create: async (data) => fetchAPI('/sports', { method: 'POST', body: JSON.stringify(data) }),
  update: async (id, data) => fetchAPI(`/sports/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: async (id) => fetchAPI(`/sports/${id}`, { method: 'DELETE' }),
};

/**
 * ========== SESSION ==========
 */
export const sessionAPI = {
  getAll: async () => fetchAPI('/sessions'),
  getById: async (id) => fetchAPI(`/sessions/${id}`),
  getActive: async () => fetchAPI('/sessions/active'),
  create: async (data) => fetchAPI('/sessions', { method: 'POST', body: JSON.stringify(data) }),
  update: async (id, data) => fetchAPI(`/sessions/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: async (id) => fetchAPI(`/sessions/${id}`, { method: 'DELETE' }),
  terminate: async (id) => fetchAPI(`/sessions/${id}/terminate`, { method: 'POST' }),
};

/**
 * ========== METRIC VALUE ==========
 */
export const metricValueAPI = {
  getAll: async () => fetchAPI('/metrics'),
  getBySession: async (sessionId) => fetchAPI(`/metrics/session/${sessionId}`),
  create: async (data) => fetchAPI('/metrics', { method: 'POST', body: JSON.stringify(data) }),
  update: async (id, data) => fetchAPI(`/metrics/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: async (id) => fetchAPI(`/metrics/${id}`, { method: 'DELETE' }),
};

export default {
  equipeAPI,
  joueurAPI,
  areneAPI,
  sportAPI,
  sessionAPI,
  metricValueAPI,
};
