import axios from 'axios';
import keycloak from '../keycloak';

export const apiClient = axios.create({
    baseURL: 'https://localhost:8443/api/v1', // API Gateway URL
    headers: {
        'Content-Type': 'application/json',
    },
});

apiClient.interceptors.request.use(
    async (config) => {
        if (keycloak.authenticated) {
            try {
                // Update token if it's expired
                await keycloak.updateToken(30);
                config.headers.Authorization = `Bearer ${keycloak.token}`;
                // Set the X-User-Id header for the microservices
                config.headers['X-User-Id'] = keycloak.tokenParsed?.sub;
            } catch (error) {
                console.warn("Failed to refresh token", error);
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);
