import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: 'http://localhost:8084',
  realm: 'wefit',
  clientId: 'wefit-frontend',
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;
