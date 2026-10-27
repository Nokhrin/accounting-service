from decimal import Decimal

import pytest

from accounting_api_tests.data.builders import open_account_request
from accounting_api_tests.models.generated.models import OpenAccountRequest


@pytest.mark.unit
def test_builders() -> None:
    req = open_account_request(initial_balance=Decimal("50.00"), holder_name="Alice")
    assert isinstance(req, OpenAccountRequest)
    assert req.initial_balance == Decimal("50.00")
    assert req.holder.display_name == "Alice"
