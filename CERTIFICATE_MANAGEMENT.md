# Wefit Certificate Management Strategy

## Production Strategy
For production environments, all Wefit microservices require a valid TLS certificate. 

### Generation & Provisioning
- **Cloud Deployments (AWS/GCP)**: Use a managed service like AWS Certificate Manager (ACM) attached to load balancers, or HashiCorp Vault for internal PKI.
- **On-Premise / VM Deployments**: Use `certbot` to generate Let's Encrypt certificates.

### Keystore Format
Spring Boot services require the certificates to be provided in PKCS12 format (`keystore.p12`) or JKS format.
When using standard PEM certificates (e.g., from Let's Encrypt), they must be converted to PKCS12:
```bash
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out keystore.p12 -name wefit -passout pass:changeit
```

### Rotation Strategy
1. **Automated Renewal**: Setup a cron job to run `certbot renew` weekly.
2. **Keystore Generation**: Use a post-hook in the renewal script to automatically convert the new PEM files into a new `keystore.p12`.
3. **Application Reload**: Once the new keystore is generated, services can be reloaded without downtime if Spring Cloud Config and Spring Cloud Bus are configured (`/actuator/busrefresh`). Otherwise, a rolling restart of the service pods/containers must be performed.

## Local Development
For local development, use the provided `generate-certs.bat` or `generate-certs.sh` script to generate a self-signed certificate using Java's `keytool`.
The self-signed certificate is valid for 10 years and will be placed in the `certs/` directory.
