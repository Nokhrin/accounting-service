package com.nokhrin.accounting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditJpaRepository extends JpaRepository<AuditEvent, UUID> {
}
