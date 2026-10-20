from decimal import Decimal

import pytest

from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.data.builders import open_account_request
from accounting_api_tests.models.account import AccountStatus


@pytest.mark.integration
def test_open_and_get_account(account_client: AccountClient) -> None:
    req = open_account_request(initial_balance=Decimal("100.00"), holder_name="Integration Test")
    acc = account_client.open_account(req)

    assert acc.balance == Decimal("100.00")
    assert acc.status == AccountStatus.ACTIVE
    assert acc.holder.displayName == "Integration Test"

    fetched = account_client.get_account(acc.id)
    assert fetched.id == acc.id
