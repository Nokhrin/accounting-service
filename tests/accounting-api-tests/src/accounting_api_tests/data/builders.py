from decimal import Decimal
from uuid import UUID

from accounting_api_tests.models.generated.models import (
    AccountHolderRequest,
    AmountRequest,
    OpenAccountRequest,
)


def amount_request(amount: Decimal, operation_id: UUID | None = None) -> AmountRequest:
    if amount <= 0:
        raise ValueError(f"amount must be positive, got {amount}")
    return AmountRequest(operationId=operation_id, amount=amount)


def holder_request(name: str) -> AccountHolderRequest:
    if not name:
        raise ValueError("holder name must not be empty")
    return AccountHolderRequest(displayName=name)


def open_account_request(
    initial_balance: Decimal,
    holder_name: str,
    operation_id: UUID | None = None,
) -> OpenAccountRequest:
    if initial_balance < 0:
        raise ValueError(f"initial_balance must be non-negative, got {initial_balance}")
    if not holder_name:
        raise ValueError("holder_name must not be empty")
    return OpenAccountRequest(
        operationId=operation_id,
        initialBalance=initial_balance,
        holder=holder_request(holder_name),
    )
