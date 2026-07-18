# Accounting Service

Backend для управления банковскими счетами на основе гексагональной архитектуры.

## Доменная модель

**Сущности:**
- `Account` - банковский счет (immutable record)
- `AccountHolder` - владелец счета (UUID + displayName)
- `AccountStatus` - статус (ACTIVE, BLOCKED, CLOSED)

**Операции:**
- `Deposit` - пополнение счета
- `Withdrawal` - снятие средств
- `Transfer` - перевод между счетами

Каждая операция возвращает типизированный результат (`DepositResult`, `WithdrawalResult`, `TransferResult`).

**События:**
- `Deposited`, `Withdrawn`, `Transferred` - доменные события для аудита

## Архитектура

```
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│  Account, Operations, Events, Ports     │
└─────────────────────────────────────────┘
↓
┌─────────────────────────────────────────┐
│        Infrastructure Layer             │
│  Adapters, JPA Entities, Mappers        │
└─────────────────────────────────────────┘
```

**Принципы:**
- Домен не зависит от фреймворков
- Порты определяют контракты, адаптеры реализуют
- Иммутабельность доменных объектов
- Типобезопасность через sealed interfaces

## Тестирование

- **Unit-тесты** для доменной логики
- **Property-based тесты** (jqwik) для инвариантов
- **Интеграционные тесты** для адаптеров

## Технологии

- Java 25
- Spring Boot 4.1
- PostgreSQL 17
- JPA/Hibernate
- JUnit 5 + Mockito + jqwik
