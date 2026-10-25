from decimal import Decimal

import pytest

from accounting_api_tests.data.builders import open_account_request
from accounting_api_tests.models.account import OpenAccountRequest


@pytest.mark.unit
def test_builders() -> None:
    req = open_account_request(initial_balance=Decimal("50.00"), holder_name="Alice")
    assert isinstance(req, OpenAccountRequest)
    assert req.initialBalance == 50.00
    assert req.holder.displayName == "Alice"
