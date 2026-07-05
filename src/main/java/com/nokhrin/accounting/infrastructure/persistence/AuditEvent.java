package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.event.DepositEvent;
import com.nokhrin.accounting.domain.event.AuditAction;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "action", nullable = false, length = 30)
    private AuditAction action;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "amount", precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 16, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 16, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "actor_id", nullable = false, length = 100)
    private String actorId;

    @Column(name = "actor_type", nullable = false, length = 30)
    private String actorType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEvent() {}



    public static AuditEvent from(DepositEvent event) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.id=event.id();
        auditEvent.action = AuditAction.DEPOSITED;
        auditEvent.entityId = event.accountId();
        auditEvent.amount=event.amount();
        auditEvent.balanceBefore=event.balanceBefore();
        auditEvent.balanceAfter=event.balanceAfter();
        auditEvent.actorId=event.actor().id();
        auditEvent.occurredAt=event.eventTs();

        return auditEvent;
    }
}