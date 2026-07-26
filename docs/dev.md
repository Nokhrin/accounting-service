# БД

## Тестовая

```shell
docker run -d --name accounting-test-db \
  -e POSTGRES_DB=testdb \
  -e POSTGRES_USER=test \
  -e POSTGRES_PASSWORD=test \
  -p 5433:5432 \
  postgres:17-alpine
```

## Боевая

```shell
docker stop accounting-db
docker rm accounting-db
docker run -d --name accounting-db \
  -e POSTGRES_DB=accounting_db \
  -e POSTGRES_USER=accounting \
  -e POSTGRES_PASSWORD=accounting \
  -p 5432:5432 \
  postgres:17-alpine
```

---

# Старт приложения

```shell
DB_USERNAME=accounting DB_PASSWORD=accounting mvn spring-boot:run
```

```shell
# Проверка приложения
curl -v http://localhost:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
lsof -i :8080
# java    549155 nohal 191u  IPv6 1287044      0t0  TCP *:http-alt (LISTEN)
```

---

# OpenAPI

Изменить openapi.yaml
mvn clean compile
mvn spring-boot:run
Открыть в браузере
http://localhost:8080/swagger-ui.html
username: user
password: в логе spring boot