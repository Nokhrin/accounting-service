package com.nokhrin.accounting.domain.event;

public enum AuditAction {
    ACCOUNT_OPENED,
    ACCOUNT_CLOSED,
    ACCOUNT_FROZEN,
    ACCOUNT_UNFROZEN,
    DEPOSITED,
    WITHDRAWN,
    TRANSFERRED,
    BALANCE_UPDATED,
    LOGIN_FAILED,
    PASSWORD_CHANGED
}