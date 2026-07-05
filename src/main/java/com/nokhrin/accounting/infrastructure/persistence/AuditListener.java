package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.event.DepositEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditListener.class);

    private final AuditJpaRepository auditJpaRepository;

    public AuditListener(AuditJpaRepository auditJpaRepository) {
        this.auditJpaRepository = auditJpaRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDepositEvent(DepositEvent depositEvent) {
        LOGGER.info("Processing Deposit: accountId={}, amount={}", depositEvent.accountId(), depositEvent.amount());

        auditJpaRepository.save(AuditEvent.from(depositEvent));
    }
}
