@echo off
set CERTS_DIR=certs
set KEYSTORE=%CERTS_DIR%\keystore.p12
set TRUSTSTORE=%CERTS_DIR%\truststore.p12
set CERT_FILE=%CERTS_DIR%\wefit.crt

if not exist "%CERTS_DIR%" (
    mkdir "%CERTS_DIR%"
)

if not exist "%KEYSTORE%" (
    echo Generating self-signed certificate...
    keytool -genkeypair -alias wefit -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore "%KEYSTORE%" -validity 3650 -storepass changeit -dname "CN=localhost, OU=Wefit, O=Wefit, L=City, ST=State, C=Country" -ext "SAN=dns:localhost,ip:127.0.0.1"
    echo Keystore generated at %KEYSTORE%
) else (
    echo Keystore already exists.
)

if not exist "%TRUSTSTORE%" (
    echo Generating truststore...
    keytool -exportcert -alias wefit -keystore "%KEYSTORE%" -storepass changeit -file "%CERT_FILE%"
    keytool -importcert -noprompt -alias wefit -file "%CERT_FILE%" -keystore "%TRUSTSTORE%" -storepass changeit -storetype PKCS12
    echo Truststore generated at %TRUSTSTORE%
) else (
    echo Truststore already exists.
)

