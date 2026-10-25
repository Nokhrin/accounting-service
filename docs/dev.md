# Разработка

# Приложение
## Предварительные требования

- Docker 20.10+
- Docker Compose 2.0+
- Java 25
- Maven 3.9+

## Быстрый старт

```shell
# 1. Клонировать репозиторий
git clone https://github.com/Nokhrin/accounting-service.git
cd accounting-service

# 2. Подготовить .env файл по .env.example

# 3. Поднять инфраструктуру и приложение одной командой
docker compose up -d --build

# 4. Проверить статус
docker compose ps
# Все сервисы должны быть в статусе "Up" или "healthy"

# 5. Проверить работоспособность
curl -v http://localhost:8080/actuator/health
# Ожидаемый ответ: {"status":"UP"}

docker compose logs accounting-service

```

Лог контейнера
```shell
docker compose -f /home/nohal/projects/accounting-service/docker-compose.yml logs --tail=200 accounting-service
```

Пересобрать контейнер после изменения .env
```shell
docker compose down -v --remove-orphans
docker compose up -d --build
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
# Логи всех сервисов
docker compose logs -f

# Логи только приложения
docker compose logs -f accounting-service

# Логи только БД
docker compose logs -f accounting-dev-db
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