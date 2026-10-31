# Accounting Service

Backend для управления банковскими счетами

## Требования
- счет не может быть удален
- баланс счета >=0
- сумма операции >0
- операции разрешены над счетами в статусе `ACTIVE`
- операция закрытия разрешена над счетами c балансом =0
- Домен не зависит от фреймворков
- Порты определяют контракты
- Адаптеры реализуют контракты
- Иммутабельность доменных объектов

## Тестирование

- Замкнутые множества операций, результатов, событий -> конечное множество сценариев 
- Доменная логика: unit, property-based, mutation
- Адаптеры: интеграционные тесты

## Технологии

- Java 25
- Spring Boot 4
- PostgreSQL 17
- JUnit 5 + Mockito + jqwik + pitest

- [Архитектура](https://nokhrin.github.io/accounting-service/index.html)
- [Разработка](docs/dev.md)


Статус: ![CI](https://github.com/Nokhrin/accounting-service/actions/workflows/check-pr.yml/badge.svg)