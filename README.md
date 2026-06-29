# accounting-service
REST API для управления банковскими счетами
Стек: Java 25, Spring Boot 4.0.0, PostgreSQL 17

---
# Testing

## App

### локально
```shell
# Только PostgreSQL в контейнере
docker compose up -d postgres
# docker compose ps
# Приложение локально
mvn spring-boot:run
# Проверка миграций
docker compose exec postgres psql -U accounting -d accountingdb -c "\dt"
```

```shell
# выполнить миграции вручную
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/accountingdb \
                   -Dflyway.user=accounting \
                   -Dflyway.password=accounting \
                   -Dflyway.locations=filesystem:src/main/resources/db/migration
# проверка наличия таблиц
docker compose exec postgres psql -U accounting -d accountingdb -c "\dt"                   
```

### в контейнере
```shell
docker compose ps
docker compose up --build app

# в другом терминале
docker compose logs -f app | grep -E "(Started|Flyway|Migrating)"
# Таблицы
docker compose exec postgres psql -U accounting -d accountingdb -c "\dt"
# История миграций
docker compose exec postgres psql -U accounting -d accountingdb -c \
  "SELECT version, description, success FROM flyway_schema_history;"
```

## Swagger
```shell
# 1. Запустить PostgreSQL
docker compose up -d postgres

# 2. Запустить Spring Boot
mvn spring-boot:run

# 3. Дождаться сообщения в логах:
# "Started AccountingApp in X.XXX seconds"

# 4. Открыть в браузере:
http://localhost:8080/swagger-ui.html
# username = user, password - в консоли при старте приложения
```


---

## DB

```shell
java -Dspring.profiles.active=dev -jar app.jar
# Веб-консоль H2 на http://localhost:8080/h2-console
```

---

## Тестирование API

## Пошаговая инструкция: сборка, запрос createAccount, проверка результата

### Шаг 1: Запуск PostgreSQL в контейнере

```bash
docker compose up -d postgres
```

**Проверка:**
```bash
docker compose ps postgres
```

Ожидаемый результат: `STATUS` содержит `(healthy)`.

```bash
docker compose exec postgres pg_isready -U accounting -d accountingdb
```

Ожидаемый результат: `/var/run/postgresql:5432 - accepting connections`

---

### Шаг 2: Сборка и запуск приложения

```bash
mvn spring-boot:run
```
---

### Шаг 3: Получение пароля Spring Security

в другом терминале
```bash
PASSWORD="a4448d8c-02b6-4653-b5be-e5390a2db90b"
#PASSWORD=$(docker compose logs app 2>/dev/null | grep "Using generated security password" | awk '{print $NF}')
```

---

### Шаг 4: Выполнение POST /accounts

**Вариант А: С аутентификацией (рекомендуется)**
```bash
curl -s -u "user:${PASSWORD}" \
  -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"initialBalance": 1500.00}' | jq
```


**Ожидаемый ответ (HTTP 201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1500.00,
  "status": "ACTIVE",
  "createdAt": "2026-06-30T17:00:00.000000Z"
}
```

**Сохраните `id` для последующих запросов:**
```bash
ACCOUNT_ID="56d420c6-3422-45b3-a19f-41c09354a83b"
```

---

### Шаг 5: Проверка результата

#### 5.1. Проверка через HTTP (если есть endpoint GET)

Если реализован `GET /accounts/{id}`:
```bash
curl -s -u "user:${PASSWORD}" \
  http://localhost:8080/accounts/${ACCOUNT_ID} | jq
```

#### 5.2. Проверка через psql (прямой запрос к БД)

```bash
docker compose exec postgres psql -U accounting -d accountingdb -c \
  "SELECT id, balance, status, created_at FROM accounts;"
```

Ожидаемый результат:
```
                  id                  |  balance  | status |         created_at         
--------------------------------------+-----------+--------+----------------------------
 550e8400-e29b-41d4-a716-446655440000 | 1500.00   | ACTIVE | 2026-06-30 17:00:00.000000
```

#### 5.3. Проверка через Swagger UI

1. Откройте в браузере:
http://localhost:8080/swagger-ui.html
2. При запросе авторизации введите:
    - **Username:** `user`
    - **Password:** значение из Шага 3
3. Найдите endpoint `POST /accounts`
4. Нажмите **Try it out**
5. Введите `{"initialBalance": 1500.00}`
6. Нажмите **Execute**
7. Проверьте **Response body** и **Response code** (должен быть 201)

#### 5.4. Проверка истории миграций

```bash
docker compose exec postgres psql -U accounting -d accountingdb -c \
  "SELECT version, description, success, installed_on FROM flyway_schema_history;"
```

Ожидаемый результат:
```
 version |   description   | success |       installed_on        
---------+-----------------+---------+---------------------------
 1       | init            | t       | 2026-06-30 16:55:00.000
 2       | create accounts | t       | 2026-06-30 16:55:00.100
```

---

### Шаг 6: Проверка валидации (негативный сценарий)

**Попытка создать счет с отрицательным балансом:**
```bash
curl -s -u "user:${PASSWORD}" \
  -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"initialBalance": -100}' | jq
```

Ожидаемый ответ (HTTP 400 Bad Request):
```json
{
  "error": "initialBalance: Initial balance must be non-negative",
  "timestamp": "2026-06-30T17:05:00.000000Z"
}
```

**Попытка создать счет без указания баланса:**
```bash
curl -s -u "user:${PASSWORD}" \
  -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{}' | jq
```

Ожидаемый ответ (HTTP 400 Bad Request):
```json
{
  "error": "initialBalance: Initial balance is required",
  "timestamp": "2026-06-30T17:05:00.000000Z"
}
```

---

### Шаг 7: Остановка сервисов

**Остановить приложение:** `Ctrl+C` в терминале, где запущен `mvn spring-boot:run`

**Остановить PostgreSQL:**
```bash
docker compose down
```

**Проверка:**
```bash
docker compose ps
```

Ожидаемый результат: пустой список контейнеров.

```bash
sudo ss -tlnp | grep -E ":(8080|5432)"
```

Ожидаемый результат: пустой вывод (порты свободны).

---
