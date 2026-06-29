package com.nokhrin.accounting.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name ="updated_at", nullable = false)
    private Instant updatedAt;

    public Account(BigDecimal initialBalance) {
        if(initialBalance==null
        || initialBalance.compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException(
                    "Initial balance must be non-negative. " +
                            "Provided: " + initialBalance
            );
        }
        this.balance=initialBalance;
        this.status=AccountStatus.ACTIVE;
        this.createdAt=Instant.now();
        this.updatedAt=this.createdAt;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UUID getId() {
        return id;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
