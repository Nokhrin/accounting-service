# accounting-service
REST API для управления банковскими счетами
Стек: Java 25, Spring Boot 4.0.0, PostgreSQL 17

---
### Тестирование
```shell
mvn clean test -Dtest=TransactionUtilsTest
```


### Управление сервисами при разработке

```shell
# Начать выполнение сервисов
docker compose up -d postgres pgadmin jenkins
docker compose up --build app
# Проверка доступности приложения
curl http://localhost:8080/actuator/health
# {"status":"UP"}
# Остановить приложение (Ctrl+C в консоли с логами)

# Остановить все сервисы
docker compose down

# Пересобрать и запустить заново
docker compose up --build app

# Пересобрать app без остановки БД
docker compose up --build -d app
```

## Smoke тест окружения

### Шаг 1: Сборка проекта
```shell
mvn clean compile
# Ожидаемый результат: [INFO] BUILD SUCCESS
```

### Шаг 2: Файлы автоматизации Jenkins
```shell
ls -l Jenkinsfile jenkins/init.groovy.d/basic-security.groovy
# Ожидаемый результат: оба файла существуют и не пусты
```

### Шаг 3: БД и pgAdmin
```shell
docker compose up -d postgres pgadmin
docker compose ps postgres pgadmin
# Ожидаемый результат: STATUS содержит "healthy" для postgres и "running" для pgadmin
docker compose exec postgres pg_isready -U accounting -d accountingdb
# Ожидаемый результат: /var/run/postgresql:5432 - accepting connections
```

### Шаг 4: Приложение
```shell
docker compose up --build -d app
docker compose ps app
# Ожидаемый результат: STATUS содержит "healthy"
curl -s http://localhost:8080/actuator/health | jq
# Ожидаемый результат: {... "status": "UP", ...}
docker compose logs app | grep "Using generated security password"

# Swagger UI
PASSWORD=$(docker compose logs app | grep "Using generated security password" | awk '{print $NF}') && \
curl -v -u "user:${PASSWORD}" http://localhost:8080/swagger-ui.html
# HTTP/1.1 302
```

### Шаг 5: Запуск и проверка Jenkins
Запустите сервис Jenkins и убедитесь, что автоматическая инициализация прошла успешно.
```shell
docker compose up -d jenkins
# Проверка доступности веб-интерфейса
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8081/login
# Ожидаемый результат: 200
# http://localhost:8081/ 
```

### Шаг 6: Финальная проверка всех сервисов
```shell
docker compose ps
# Ожидаемый результат: postgres, pgadmin, app, jenkins в состоянии running/healthy
```

### Шаг 7: Остановка
```shell
docker compose down
docker compose ps
# Ожидаемый результат: пустой список
docker ps -a --filter "name=accounting-"
# Ожидаемый результат: пустой список

sudo ss -tlnp | grep -E ":(8080|8081|5050|5432)"
# Ожидаемый результат: пустой вывод (порты свободны)
```