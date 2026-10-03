import { apiClient } from './client';

// User Service
export const getUserProfile = async (userId) => {
    const { data } = await apiClient.get(`/users/${userId}`);
    return data;
};

// Activity Service
export const getActivities = async () => {
    const { data } = await apiClient.get('/activities/history');
    return data;
};

// Feed Service
export const getFeed = async () => {
    const { data } = await apiClient.get('/feed');
    return data;
};

// Gamification Service
export const getUserLevel = async () => {
    const { data } = await apiClient.get('/gamification/level');
    return data;
};

export const getLeaderboard = async () => {
    const { data } = await apiClient.get('/leaderboards/global');
    return data;
};

// AI Service
export const chatWithAi = async (message) => {
    const { data } = await apiClient.post('/ai/chat', { message });
    return data;
};
