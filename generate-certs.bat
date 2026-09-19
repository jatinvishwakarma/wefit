@echo off
set CERTS_DIR=certs
set KEYSTORE=%CERTS_DIR%\keystore.p12

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
