package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.AccountStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    protected AccountJpaEntity() {}

    /**
     * Уникальный идентификатор счета.
     * <p>Генерируется автоматически при сохранении (UUID v4).</p>
     *
     * @return UUID идентификатор
     * @see jakarta.persistence.GeneratedValue
     */
    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    /**
     * Текущий баланс счета.
     * <p>Ограничения БД: NOT NULL, precision=16, scale=2, CHECK (balance >= 0)</p>
     *
     * @return баланс в виде BigDecimal
     */
    public BigDecimal getBalance() { return balance; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }

    /**
     * Статус счета: ACTIVE, BLOCKED или CLOSED.
     * <p>Хранится как VARCHAR(20) в БД (EnumType.STRING).</p>
     *
     * @return текущий статус счета
     */
    public AccountStatus getStatus() { return status; }

    public void setStatus(AccountStatus status) { this.status = status; }

    /**
     * Дата и время создания счета.
     * <p>Не изменяется после создания (updatable = false).</p>
     *
     * @return timestamp создания
     */
    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    /**
     * Дата и время последнего изменения счета.
     * <p>Обновляется при каждой операции сохранения.</p>
     *
     * @return timestamp последнего изменения
     */
    public Instant getModifiedAt() { return modifiedAt; }

    public void setModifiedAt(Instant modifiedAt) { this.modifiedAt = modifiedAt; }
}