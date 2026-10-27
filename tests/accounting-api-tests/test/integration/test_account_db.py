from decimal import Decimal
from uuid import UUID, uuid4

import pytest

from accounting_api_tests.assertions.db import get_account_balance
from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.data.builders import amount_request, open_account_request


@pytest.mark.integration
def test_deposit_and_db_assertion(account_client: AccountClient) -> None:
    req = open_account_request(initial_balance=Decimal("50.00"), holder_name="Test User")
    account = account_client.open_account(req)

    operation_id: UUID = uuid4()
    deposit_req = amount_request(Decimal("25.00"), operation_id=operation_id)
    res = account_client.deposit(account.id, deposit_req)

    # assert res.operation_id == operation_id # сервер генерирует свой operationId
    assert res.account.balance == Decimal("75.00")

    db_balance = get_account_balance(account.id)
    assert db_balance is not None, "Account not found in database"
    assert db_balance == Decimal("75.00")
