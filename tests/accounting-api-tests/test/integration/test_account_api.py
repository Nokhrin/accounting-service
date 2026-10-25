from decimal import Decimal

import pytest

from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.data.builders import open_account_request
from accounting_api_tests.models.generated.models import AccountStatus


@pytest.mark.integration
def test_open_and_get_account(account_client: AccountClient) -> None:
    request = open_account_request(initial_balance=Decimal("100.00"), holder_name="Integration Test")
    account = account_client.open_account(request)

    assert account.balance == Decimal("100.00")
    assert account.status == AccountStatus.ACTIVE
    assert account.holder.display_name == "Integration Test"

    fetched = account_client.get_account(account.id)
    assert fetched.id == account.id
