# 🌐 Wefit Web Client (`wefit-web`)

## Purpose
The `wefit-web` project is the luxury frontend web application for the Wefit fitness platform. It provides a polished, interactive user interface built with React, Vite, and Keycloak, connecting to the Spring Cloud backend via the API Gateway.

## Architecture
- **Framework:** React 18+ via Vite
- **Styling:** Vanilla CSS (CSS Variables) with a strict luxury design system (dark gold theme, glassmorphism, OLED blacks). No external UI libraries like Tailwind.
- **Routing:** React Router v6
- **Authentication:** Keycloak via `@react-keycloak/web`. Token management and session refresh is handled seamlessly on the client.
- **Icons:** `lucide-react`

## Directory Structure
- `src/index.css`: Global design tokens and base styles.
- `src/keycloak.js`: Keycloak client configuration.
- `src/App.jsx`: Main routing setup with Keycloak Provider and Private Routes.
- `src/components/`: Reusable UI elements (e.g. `Sidebar.jsx`, `Sidebar.css`).
- `src/pages/`: Full screen views (`Landing.jsx`, `Dashboard.jsx`, etc).

## Connecting to Backend
The app communicates with the microservices through the API Gateway (`https://localhost:8443` or `http://localhost:8080`). Authentication tokens (JWT) retrieved by Keycloak must be attached as `Bearer` tokens in the `Authorization` header for all requests to the Gateway.

## How to Run
1. Ensure Keycloak is running at `http://localhost:8090`.
2. Ensure the API Gateway is running to handle API requests.
3. In the `wefit-web` directory, run:
   ```bash
   npm install
   npm run dev
   ```
4. Access the app at `http://localhost:5173`.
