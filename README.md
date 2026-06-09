# bank-api
REST API для управления банковскими счетами
Стек: Java 25, Spring Boot 4.0.0, PostgreSQL 17

---

### Управление сервисами при разработке

```shell
# Начать выполнение сервисов
docker compose up -d postgres pgadmin
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
