@echo off
docker rm -f competent_hofstadter dazzling_mirzakhani exciting_lewin
docker-compose up -d postgres mongodb zookeeper kafka keycloak redis
