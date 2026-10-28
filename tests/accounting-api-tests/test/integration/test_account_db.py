from decimal import Decimal
from uuid import uuid4

import pytest

from accounting_api_tests.assertions.db import get_account_balance
from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.data.builders import amount_request, open_account_request
from accounting_api_tests.error import ConflictError


@pytest.mark.integration
def test_deposit_and_db_assertion(account_client: AccountClient) -> None:
    request = open_account_request(initial_balance=Decimal("50.00"), holder_name="Test User", operation_id=uuid4())
    account = account_client.open_account(request)

    to_deposit = amount_request(Decimal("25.00"), operation_id=uuid4())
    deposited = account_client.deposit(account.id, to_deposit)

    assert deposited.operation_id == to_deposit.operation_id
    assert deposited.account.balance == Decimal("75.00")

    db_balance = get_account_balance(account.id)
    assert db_balance == Decimal("75.00")


@pytest.mark.integration
def test_deposit_replayed_operation_id_returns_409(account_client: AccountClient) -> None:
    open_req = open_account_request(initial_balance=Decimal("50.00"), holder_name="Test User", operation_id=uuid4())
    account = account_client.open_account(open_req)

    deposit_req = amount_request(Decimal("25.00"), operation_id=uuid4())
    first = account_client.deposit(account.id, deposit_req)

    assert first.operation_id == deposit_req.operation_id
    assert first.operation_id != open_req.operation_id
    assert first.account.balance == Decimal("75.00")

    with pytest.raises(ConflictError, match="DUPLICATE_OPERATION"):
        account_client.deposit(account.id, deposit_req)

    assert get_account_balance(account.id) == Decimal("75.00")


@pytest.mark.integration
def test_deposit_with_open_account_operation_id_returns_409(account_client: AccountClient) -> None:
    open_req = open_account_request(initial_balance=Decimal("50.00"), holder_name="Test User", operation_id=uuid4())
    account = account_client.open_account(open_req)

    reused = amount_request(Decimal("25.00"), operation_id=open_req.operation_id)

    with pytest.raises(ConflictError, match="DUPLICATE_OPERATION"):
        account_client.deposit(account.id, reused)

    assert get_account_balance(account.id) == Decimal("50.00")


@pytest.mark.integration
def test_deposit_operation_id_collision_across_accounts_returns_409(account_client: AccountClient) -> None:
    a1 = account_client.open_account(
        open_account_request(initial_balance=Decimal("50.00"), holder_name="A1", operation_id=uuid4())
    )
    a2 = account_client.open_account(
        open_account_request(initial_balance=Decimal("50.00"), holder_name="A2", operation_id=uuid4())
    )

    shared_id = uuid4()
    account_client.deposit(a1.id, amount_request(Decimal("25.00"), operation_id=shared_id))

    with pytest.raises(ConflictError, match="DUPLICATE_OPERATION"):
        account_client.deposit(a2.id, amount_request(Decimal("25.00"), operation_id=shared_id))

    assert get_account_balance(a1.id) == Decimal("75.00")
    assert get_account_balance(a2.id) == Decimal("50.00")
