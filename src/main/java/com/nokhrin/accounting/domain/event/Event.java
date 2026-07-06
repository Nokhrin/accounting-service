package com.nokhrin.accounting.domain.event;

public sealed interface Event permits DepositEvent, WithdrawEvent, TransferEvent, AccountCreatedEvent {
}
