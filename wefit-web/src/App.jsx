import React from 'react';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import keycloak from './keycloak';

import Landing from './pages/Landing';
import Dashboard from './pages/Dashboard';

const PrivateRoute = ({ children }) => {
  const { keycloak, initialized } = useKeycloak();

  if (!initialized) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', color: '#C9A94E' }}>Loading...</div>;
  }

  return keycloak.authenticated ? children : <Navigate to="/" />;
};

// Custom hook to use Keycloak inside PrivateRoute
import { useKeycloak as useKc } from '@react-keycloak/web';
function useKeycloakWrapper() {
   return useKc();
}

const ProtectedDashboard = () => {
  const { keycloak, initialized } = useKc();
  if (!initialized) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', color: '#C9A94E' }}>Initializing Wefit Experience...</div>;
  }
  
  if (!keycloak.authenticated) {
     return <Navigate to="/" />;
  }
  return <Dashboard />;
}

function App() {
  const initOptions = { onLoad: 'check-sso', silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html' };

  return (
    <ReactKeycloakProvider authClient={keycloak} initOptions={initOptions}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/dashboard" element={<ProtectedDashboard />} />
          {/* Add other routes later (Activity Log, Insights, etc) mapping to components */}
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </BrowserRouter>
    </ReactKeycloakProvider>
  );
}

export default App;
