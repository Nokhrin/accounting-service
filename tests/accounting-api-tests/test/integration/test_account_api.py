from decimal import Decimal
from uuid import uuid4

import pytest

from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.data.builders import amount_request, open_account_request
from accounting_api_tests.error import ConflictError
from accounting_api_tests.models.generated.models import AccountStatus


@pytest.mark.integration
def test_open_and_get_account(account_client: AccountClient) -> None:
    request = open_account_request(
        initial_balance=Decimal("100.00"), holder_name="Integration Test", operation_id=uuid4()
    )
    account = account_client.open_account(request)

    assert account.balance == Decimal("100.00")
    assert account.status == AccountStatus.ACTIVE
    assert account.holder.display_name == "Integration Test"

    fetched = account_client.get_account(account.id)
    assert fetched.id == account.id


@pytest.mark.integration
def test_deposit_idempotency_returns_409_on_replay(account_client: AccountClient) -> None:
    open_req = open_account_request(Decimal("100.00"), "Test", uuid4())
    account = account_client.open_account(open_req)

    deposit_req = amount_request(Decimal("50.00"), uuid4())
    first = account_client.deposit(account.id, deposit_req)
    assert first.account.balance == Decimal("150.00")

    with pytest.raises(ConflictError, match="DUPLICATE_OPERATION"):
        account_client.deposit(account.id, deposit_req)

    fetched = account_client.get_account(account.id)
    assert fetched.balance == Decimal("150.00")


@pytest.mark.integration
def test_withdraw_idempotency_returns_409_on_replay(account_client: AccountClient) -> None:
    open_req = open_account_request(Decimal("100.00"), "Test", uuid4())
    account = account_client.open_account(open_req)

    withdraw_req = amount_request(Decimal("30.00"), uuid4())
    first = account_client.withdraw(account.id, withdraw_req)
    assert first.account.balance == Decimal("70.00")

    with pytest.raises(ConflictError, match="DUPLICATE_OPERATION"):
        account_client.withdraw(account.id, withdraw_req)

    assert account_client.get_account(account.id).balance == Decimal("70.00")


@pytest.mark.integration
def test_transfer_idempotency_returns_409_on_replay(account_client: AccountClient) -> None:
    a1 = account_client.open_account(open_account_request(Decimal("100.00"), "A", uuid4()))
    a2 = account_client.open_account(open_account_request(Decimal("50.00"), "B", uuid4()))

    transfer_req = amount_request(Decimal("40.00"), uuid4())
    first = account_client.transfer(a1.id, a2.id, transfer_req)
    assert first.source_account.balance == Decimal("60.00")
    assert first.target_account.balance == Decimal("90.00")

    with pytest.raises(ConflictError, match="DUPLICATE_OPERATION"):
        account_client.transfer(a1.id, a2.id, transfer_req)

    assert account_client.get_account(a1.id).balance == Decimal("60.00")
    assert account_client.get_account(a2.id).balance == Decimal("90.00")
