import json
from decimal import Decimal
from typing import Callable
from uuid import UUID

import httpx
import pytest
from httpx import Request, Response

from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.clients.auth import UnAuthClient
from accounting_api_tests.config import Settings
from accounting_api_tests.models.account import AccountStatus
from accounting_api_tests.models.operations import DepositRequest

ACCOUNT_ID = UUID("a1111111-1111-1111-1111-111111111111")
ACCOUNT_STATUS = AccountStatus.ACTIVE
HOLDER_ID = UUID("b1111111-1111-1111-1111-111111111111")
HOLDER_NAME = "Alice Johnson"
BASE_URL = "http://testhost"

Handler = Callable[[httpx.Request], httpx.Response]


def create_accounts_client(handler: Handler) -> AccountClient:
    transport = httpx.MockTransport(handler)
    http_client = httpx.Client(base_url=BASE_URL, transport=transport)
    settings = Settings(base_url=BASE_URL)
    return AccountClient(
        settings=settings,
        auth=UnAuthClient(),
        client=http_client,
    )


@pytest.mark.unit
def test_deposit_amount_as_string_response_parsed() -> None:
    requests_received: list[Request] = []

    def handler(test_request: Request) -> Response:
        requests_received.append(test_request)
        return Response(
            status_code=200,
            json={
                "id": str(ACCOUNT_ID),
                "balance": "3",
                "status": ACCOUNT_STATUS.value,
                "holder": {
                    "id": str(HOLDER_ID),
                    "displayName": HOLDER_NAME,
                },
            },
        )

    client = create_accounts_client(handler)
    result = client.deposit(ACCOUNT_ID, DepositRequest(amount=Decimal("1")))

    assert len(requests_received) == 1
    request = requests_received[0]
    assert request.method == "POST"
    assert request.url.path == f"/accounts/{ACCOUNT_ID}/deposit"

    body = json.loads(request.content)
    assert body["amount"] == "1"

    assert result.id == ACCOUNT_ID
    assert result.balance == Decimal("3")
    assert result.status == ACCOUNT_STATUS
