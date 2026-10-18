import uuid
from decimal import Decimal

from accounting_api_tests.models.account import AccountHolderRequest, OpenAccountRequest


def holder_request(name: str = "Test User") -> AccountHolderRequest:
    return AccountHolderRequest(id=uuid.uuid4(), displayName=name)


def open_account_request(initial_balance: Decimal = Decimal(100.00),
                         holder_name: str = "Test User") -> OpenAccountRequest:
    return OpenAccountRequest(initialBalance=initial_balance, holder=holder_request(holder_name))

