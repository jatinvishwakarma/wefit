import { apiClient } from './client';

// Realistic fallback demo data
const DEFAULT_USER_PROFILE = {
    id: 'demo-user-1',
    firstName: 'Alex',
    lastName: 'Runner',
    email: 'alex@wefit.local',
    bio: 'Marathon enthusiast & daily cyclist 🏃‍♂️🚴',
    goal: 'Hit 100km this month',
};

const DEFAULT_GAMIFICATION = {
    level: 7,
    xp: 2840,
    currentStreak: 12,
    badges: ['Early Bird', 'Streak Master', 'Century Cyclist', 'Iron Lifter']
};

const DEFAULT_LEADERBOARD = [
    { userId: 'u1', name: 'Priya N.', xp: 4820, level: 11, time: '412 min' },
    { userId: 'u2', name: 'Karan M.', xp: 3950, level: 9, time: '388 min' },
    { userId: 'demo', name: 'Alex K. (you)', xp: 2840, level: 7, time: '371 min' },
    { userId: 'u4', name: 'Maya R.', xp: 2410, level: 6, time: '352 min' },
    { userId: 'u5', name: 'Dev S.', xp: 1980, level: 5, time: '290 min' },
];

const DEFAULT_ACTIVITIES = [
    { id: 1, type: 'Running', name: 'Morning 5K Trail Run', duration: 28, distance: 5.2, calories: 340, date: 'Today, 7:15 AM', pace: '5:23 /km' },
    { id: 2, type: 'Cycling', name: 'Zone 2 Evening Ride', duration: 45, distance: 18.5, calories: 420, date: 'Yesterday, 6:00 PM', pace: '24.6 km/h' },
    { id: 3, type: 'Weightlifting', name: 'Leg Day & Squat PR', duration: 50, distance: null, calories: 380, date: '2 days ago', pace: '100 kg PR' },
    { id: 4, type: 'HIIT', name: 'Core & Cardio Blast', duration: 20, distance: null, calories: 210, date: '3 days ago', pace: 'High Intensity' },
];

const DEFAULT_FEED = [
    { id: 1, author: 'Priya N.', avatar: 'P', time: '15m ago', title: 'Finished a 10K road race! 🏅', stats: '10.2 km · 51 mins · 680 kcal', cheers: 14, comments: 3, cheered: false },
    { id: 2, author: 'Dev S.', avatar: 'D', time: '1h ago', title: 'Hit a new squat PR: 100 kg 🏋️‍♂️', stats: 'Leg day complete · 5 sets', cheers: 22, comments: 7, cheered: true },
    { id: 3, author: 'Maya R.', avatar: 'M', time: '3h ago', title: 'Weekend 40km scenic coastal cycle 🚴‍♀️', stats: '41.5 km · 1h 35m · 890 kcal', cheers: 31, comments: 5, cheered: false },
];

// User Service
export const getUserProfile = async (userId) => {
    try {
        const { data } = await apiClient.get(`/users/${userId}`);
        return data;
    } catch {
        return DEFAULT_USER_PROFILE;
    }
};

// Activity Service
export const getActivities = async () => {
    try {
        const { data } = await apiClient.get('/activities/history');
        return data && data.length > 0 ? data : DEFAULT_ACTIVITIES;
    } catch {
        const local = localStorage.getItem('wefit_custom_activities');
        if (local) {
            try { return JSON.parse(local); } catch {}
        }
        return DEFAULT_ACTIVITIES;
    }
};

export const logActivity = async (activity) => {
    const { data } = await apiClient.post('/activities', activity);
    return data;
};

// Feed Service
export const getFeed = async () => {
    try {
        const { data } = await apiClient.get('/feed');
        return data && data.length > 0 ? data : DEFAULT_FEED;
    } catch {
        return DEFAULT_FEED;
    }
};

// Gamification Service
export const getUserLevel = async () => {
    try {
        const { data } = await apiClient.get('/gamification/level');
        return data;
    } catch {
        return DEFAULT_GAMIFICATION;
    }
};

export const getLeaderboard = async () => {
    try {
        const { data } = await apiClient.get('/leaderboards/global');
        return data && data.length > 0 ? data : DEFAULT_LEADERBOARD;
    } catch {
        return DEFAULT_LEADERBOARD;
    }
};

// AI Service
export const chatWithAi = async (message) => {
    try {
        const { data } = await apiClient.post('/ai/chat', { message });
        return data;
    } catch {
        const lower = message.toLowerCase();
        let reply = "Great question! Based on your current training load and streak, focus on quality sleep and progressive overload. Let me know if you need specific exercise cues!";
        if (lower.includes('meal') || lower.includes('food') || lower.includes('protein') || lower.includes('eat')) {
            reply = "For optimal recovery, aim for 1.6 - 2.2g of protein per kg of body weight. Try grilled salmon with sweet potato and steamed greens tonight!";
        } else if (lower.includes('rest') || lower.includes('sore') || lower.includes('recovery')) {
            reply = "Your legs have accumulated high volume. Take tomorrow as an active recovery day: 20-minute gentle foam rolling followed by light mobility work.";
        } else if (lower.includes('plan') || lower.includes('marathon') || lower.includes('run')) {
            reply = "Here is a balanced 3-day split: Tuesday interval sprints (4x400m), Thursday tempo run (6km at zone 3), Saturday long easy run (12km zone 2).";
        }
        return { response: reply };
    }
};
