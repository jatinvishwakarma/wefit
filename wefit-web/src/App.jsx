import React from 'react';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import keycloak from './keycloak';

import Landing from './pages/Landing';
import Dashboard from './pages/Dashboard';

const LoadingScreen = () => (
  <div style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    background: '#0A0C10',
    color: '#ccff00',
    fontFamily: 'Outfit, sans-serif',
    fontSize: '1.2rem',
    letterSpacing: '-0.02em',
  }}>
    <div style={{ textAlign: 'center' }}>
      <div style={{ fontSize: '3rem', fontWeight: 800, marginBottom: '12px' }}>WEFIT</div>
      <div style={{ color: 'rgba(255,255,255,0.4)', fontSize: '0.9rem' }}>Loading your experience...</div>
    </div>
  </div>
);

const ProtectedDashboard = ({ view }) => {
  const { keycloak: kc, initialized } = useKeycloak();
  const isGuest = localStorage.getItem('wefit_guest_mode') === 'true';

  if (!initialized && !isGuest) return <LoadingScreen />;
  if (!kc?.authenticated && !isGuest) return <Navigate to="/" />;
  return <Dashboard initialView={view} />;
};

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/dashboard" element={<ProtectedDashboard view="dashboard" />} />
        <Route path="/log" element={<ProtectedDashboard view="log" />} />
        <Route path="/history" element={<ProtectedDashboard view="history" />} />
        <Route path="/insights" element={<ProtectedDashboard view="insights" />} />
        <Route path="/feed" element={<ProtectedDashboard view="feed" />} />
        <Route path="/notifications" element={<ProtectedDashboard view="notifications" />} />
        <Route path="/friends" element={<ProtectedDashboard view="friends" />} />
        <Route path="/profile" element={<ProtectedDashboard view="profile" />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
}

function App() {
  const initOptions = {
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    checkLoginIframe: false,
  };

  const onKeycloakEvent = (event, error) => {
    if (error) {
      console.warn('Keycloak event:', event, error);
    }
  };

  return (
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={initOptions}
      onEvent={onKeycloakEvent}
    >
      <AppRoutes />
    </ReactKeycloakProvider>
  );
}

export default App;
