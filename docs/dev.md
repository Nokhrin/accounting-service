# Разработка

# Приложение
## Предварительные требования

- Java 25
- Maven 3.9+
- Docker 20.10+
- Docker Compose 2.0+

## Быстрый старт

```shell
# 1. Клонировать репозиторий
git clone https://github.com/Nokhrin/accounting-service.git
cd accounting-service

# 2. Подготовить .env файл по .env.example

# 3. Поднять БД и Приложение
set -a; source .env; set +a
make up
```

Для локальной разработки
```shell
# БД
docker compose -f docker-compose.yml --env-file .env up -d accounting-dev-db
# Приложение
set -a; source .env; set +a
export DB_URL="jdbc:postgresql://127.0.0.1:${DB_HOST_PORT:-5433}/${DEV_DB}"
export SPRING_PROFILES_ACTIVE=dev
java -jar target/accounting-service-0.0.1-SNAPSHOT.jar
```

---

## Проверка минимального функционала

### Получить пароль Spring Security

```shell
# После запуска приложения найти в логах:
grep "Using generated security password" <(java -jar target/accounting-service-0.0.1-SNAPSHOT.jar 2>&1 | head -50)
```

Или из Docker-контейнера:

```shell
docker compose logs accounting-service 2>&1 | grep "Using generated security password" | awk '{print $NF}'
```

### Проверка health endpoint

```shell
curl -v http://localhost:8080/actuator/health
```

Ожидаемый ответ:
```json
{"status":"UP"}
```

### Проверка Swagger UI (только dev/local)

```shell
# Открыть в браузере:
http://localhost:8080/swagger-ui.html

# Или получить OpenAPI spec:
curl -u "user:your_password" http://localhost:8080/v3/api-docs
```

### Создание счёта

```shell
PASSWORD=$(docker compose logs accounting-service 2>&1 | grep "Using generated security password" | awk '{print $NF}')

curl -u "user:$PASSWORD" \
  -H "Content-Type: application/json" \
  -d '{
    "operationId": "550e8400-e29b-41d4-a716-446655440000",
    "initialBalance": 100.00,
    "holder": {"displayName": "Test User"}
  }' \
  -X POST \
  http://localhost:8080/accounts
```

### Получение информации о счёте

```shell
ACCOUNT_ID="скопировать из ответа выше"

curl -u "user:$PASSWORD" \
  http://localhost:8080/accounts/$ACCOUNT_ID
```

### Пополнение счёта

```shell
curl -u "user:$PASSWORD" \
  -H "Content-Type: application/json" \
  -d '{
    "operationId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 50.00
  }' \
  -X POST \
  http://localhost:8080/accounts/$ACCOUNT_ID/deposit
```

### Проверка идемпотентности (повторный запрос с тем же operationId)

```shell
# Должен вернуть 409 Conflict
curl -u "user:$PASSWORD" \
  -H "Content-Type: application/json" \
  -d '{
    "operationId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 50.00
  }' \
  -X POST \
  http://localhost:8080/accounts/$ACCOUNT_ID/deposit
```

Ожидаемый ответ:
```json
{
  "code": "DUPLICATE_OPERATION",
  "message": "Operation already processed: 550e8400-e29b-41d4-a716-446655440001"
}
```


Лог контейнера
```shell
docker compose -f /home/nohal/projects/accounting-service/docker-compose.yml logs --tail=200 accounting-service
```

Пересобрать контейнер после изменения .env
```shell
make up
```



## Перед коммитом

Проверить сборку
```shell
make install
make check-python
make test
make render-diagrams
```

Проверить compose
```shell
set -a; source .env; set +a
docker compose -f docker-compose.yml config --quiet
```

---

## Подключение к базам данных

| Окружение | Порт | База данных           | Пользователь | Пароль    |
|-----------|------|-----------------------|--------------|-----------|
| Prod-like | 5432 | `accounting_prod_db`  | `accounting` | из `.env` |
| Dev       | 5433 | `accounting_dev_db`   | `dev`        | из `.env` |
| Local     | 5434 | `accounting_local_db` | `local`      | из `.env` |

DBeaver / DataGrip:
- Host: `localhost`
- Port: `5432` (prod), `5433` (dev), `5434` (local)
- Database/Username/Password: см. таблицу выше

psql CLI:
```shell
docker compose exec -T accounting-dev-db \
  psql -U dev -d accounting_dev_db \
  -c "SELECT id, balance, status, holder_display_name FROM accounts ORDER BY balance DESC;"
```

### API документация

После запуска приложения:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: http://localhost:8080/v3/api-docs

Аутентификация:
- Username: `user`
- Password: найти в логах Spring Boot при старте (строка `Using generated security password:`)

```shell
# Получить пароль из логов
PASSWORD=$(docker compose logs accounting-service 2>&1 | grep "Using generated security password" | awk '{print $NF}')
```

После внесения изменений в openapi.yml
```shell
mvn clean compile
```

### Примеры запросов

```shell
PASSWORD=$(docker compose logs accounting-service 2>&1 | grep "Using generated security password" | awk '{print $NF}')
# Получить информацию о счете
curl -u "user:$PASSWORD" \
  http://localhost:8080/accounts/a1111111-1111-1111-1111-111111111111

# Пополнение счета
curl -u "user:$PASSWORD" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}' \
  -X POST \
  http://localhost:8080/accounts/a1111111-1111-1111-1111-111111111111/deposit
  

# Пополнение
curl -s -u "user:$PASSWORD" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}' \
  -X POST \
  http://localhost:8080/accounts/a1111111-1111-1111-1111-111111111111/deposit

# Запрещённая операция: пополнение BLOCKED-счёта -> 409
curl -s -u "user:$PASSWORD" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}' \
  -X POST \
  http://localhost:8080/accounts/a6666666-6666-6666-6666-666666666666/deposit
```



## Запуск тестов

```shell
# Все тесты
mvn test

# С покрытием Jacoco
mvn verify

# Только unit-тесты
mvn test -Dtest='!IntegrationTest,!RepositoryTest'

# Только интеграционные тесты
mvn test -Dtest='IntegrationTest,RepositoryTest'
```

## Docker: принудительная пересборка
```shell
docker compose down
docker compose build --no-cache accounting-service
docker compose up -d
# Подождать 20 секунд до полного старта и проверить логи
docker compose logs -f accounting-service
```
docker compose --env-file .env up -d --build --force-recreate accounting-service

## Docker: БД

```shell
# Подключиться к БД внутри контейнера
docker compose exec accounting-dev-db psql -U dev -d accounting_dev_db

# Выполнить SQL запрос
docker compose exec accounting-dev-db psql -U dev -d accounting_dev_db -c "SELECT  FROM accounts;"

# Сделать дамп базы данных
docker compose exec accounting-dev-db pg_dump -U dev accounting_dev_db > backup.sql

# Восстановить из дампа
cat backup.sql | docker compose exec -T accounting-dev-db psql -U dev -d accounting_dev_db
```

## Docker: Логи

```shell
# Сохранить вывод сборки в файл
docker compose -f docker-compose.yml --env-file .env build --no-cache accounting-service 2>&1 | tee build.log

# Все сервисы
docker compose -f docker-compose.yml --env-file .env logs

# Конкретный сервис
docker compose -f docker-compose.yml --env-file .env logs accounting-service
docker compose -f docker-compose.yml --env-file .env logs accounting-dev-db

# Следить в реальном времени
docker compose -f docker-compose.yml --env-file .env logs -f accounting-service

# Последние N строк
docker compose -f docker-compose.yml --env-file .env logs --tail=200 accounting-service

# Сохранить в файл
docker compose -f docker-compose.yml --env-file .env logs --no-color > docker.log
```

## Docker: Завершение работы

```shell
# Остановить все сервисы
docker compose down
# Полная очистка Docker
docker system prune -a --volumes
```

## CI/CD

Проект использует GitHub Actions:

- Check Pull Request: запуск тестов при открытии PR
- Render Diagrams: генерация SVG из PlantUML при изменении `docs/.puml`
- Deploy to GitHub Pages: публикация документации


## Регламент работы с падением scheduled-скана уязвимостей
- Открыть артефакт dependency-check-report.html 
- Если CVE имеет патч: поднять версию зависимости в pom.xml, удалить соответствующую запись suppressions, коммит в ветку chore/suppressions-<date>
- Если патча нет: добавить CVE в dependency-check-suppressions.xml, коммит в ветку chore/suppressions-<date>
- Выполнить workflow
- Убедиться, что статус зеленый

---

# Автотесты

## Установка окружения
```shell
cd tests/accounting-api-tests
python3.14 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip pip-tools
pip-sync requirements.txt
```

### Обновление зависимостей
```shell
pip-compile requirements.in
pip-sync requirements.txt
```

## Выполнение тестов


```shell
# Запуск всех тестов
pytest -v
# Запуск smoke-тестов
pytest -m smoke -v
# Запуск интеграционных тестов
pytest -m integration -v

# Проверка линтеров и типов
ruff check
ruff format --check
mypy
```

---

# FAQ

## Docker
### Could not transfer metadata ... repo.maven.apache.org: Temporary failure in name resolution
```shell
sudo systemctl restart docker
make up
```

## Особенности openSUSE

### Доступ к volume
Добавлять `:Z`
```shell
docker run --rm   -u "$(operationId -u):$(operationId -g)"   -v "$PWD:/data:Z"   -w /data   plantuml/plantuml:1.2026.8   -tsvg docs/architecture.puml
```

### Проблема: "Failed to mark memory page as executable"

Симптомы:
```
Error occurred during initialization of VM
Failed to mark memory page as executable - check if grsecurity/PaX is enabled
```

Решение: базовый образ alpine не подходит, использовать debian

### Проблема: Permission denied при монтировании volumes

Симптомы:
```
docker run: permission denied
```

Решение: Используйте `:Z` суффикс для SELinux/openSUSE:

```yaml
volumes:
  - ./docker/seed.sql:/docker-entrypoint-initdb.d/init.sql:ro,Z
```

### masquerade в зоне docker

На openSUSE с firewalld Docker-трафик проходит через зону docker. Masquerade (NAT) должен быть включён для исходящего трафика контейнеров.
```shell
# Проверить текущее состояние
sudo firewall-cmd --zone=docker --query-masquerade

# Если выводит "no" — включить
sudo firewall-cmd --zone=docker --add-masquerade --permanent
sudo firewall-cmd --reload

# Проверить результат
docker run --rm maven:4.0.0-rc-7-eclipse-temurin-25 curl -s -o /dev/null -w "%{http_code}" https://repo.maven.apache.org/maven2/
```

---