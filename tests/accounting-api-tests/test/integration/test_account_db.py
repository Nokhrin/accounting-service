from decimal import Decimal

import pytest

from accounting_api_tests.assertions.db import get_account_balance
from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.data.builders import open_account_request
from accounting_api_tests.models.operations import AmountRequest


@pytest.mark.integration
def test_deposit_and_db_assertion(account_client: AccountClient):
    req = open_account_request(initial_balance=Decimal("50.00"))
    acc = account_client.open_account(req)

    deposit_req = AmountRequest(amount=Decimal("25.00"))
    res = account_client.deposit(acc.id, deposit_req)

    assert res.balance == Decimal("75.00")

    db_balance = get_account_balance(acc.id)
    assert db_balance is not None, "Account not found in database"
    assert db_balance == Decimal("75.00")
