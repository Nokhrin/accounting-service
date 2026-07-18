# Старт приложения
```shell
mvn clean spring-boot:run
```

```shell
# Проверка приложения
curl -v http://localhost:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
lsof -i :8080
# java    549155 nohal 191u  IPv6 1287044      0t0  TCP *:http-alt (LISTEN)
```


# Контейнер БД

## Тестовая

```shell
docker run -d --name accounting-test-db \
  -e POSTGRES_DB=testdb \
  -e POSTGRES_USER=test \
  -e POSTGRES_PASSWORD=test \
  -p 5433:5432 \
  postgres:17-alpine
```

```text
Подключение в DBeaver:
    Host: localhost
    Port: 5433
    Database: testdb
    Username: test
    Password: test
```

## Боевая

```shell
docker stop accounting-db
docker rm accounting-db
docker run -d --name accounting-db \
  -e POSTGRES_DB=accountingdb \
  -e POSTGRES_USER=accounting \
  -e POSTGRES_PASSWORD=accounting \
  -p 5432:5432 \
  postgres:17-alpine
```


drafts
---

# Проверить статус
docker ps -a --filter name=accounting-db

# Если остановлен — запустить
docker start accounting-db

# Подключиться через psql
docker exec -it accounting-db psql -U accounting -d accountingdb


# 1. Запуск контейнера с пробросом порта
docker run -d --name accounting-db \
-e POSTGRES_DB=accountingdb \
-e POSTGRES_USER=accounting \
-e POSTGRES_PASSWORD=accounting \
-p 5432:5432 \
postgres:17-alpine


# Проверка готовности postgresql
docker exec accounting-db pg_isready -U accounting -d accountingdb

# 2. Подключение в db клиенте
Host: localhost
Port: 5432
Database: accountingdb
URL: jdbc:postgresql://localhost:5432/accountingdb
User: accounting
Password: accounting