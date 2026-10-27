import json
from collections.abc import Callable
from uuid import UUID

import httpx
import pytest
from httpx import Request, Response

from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.clients.auth import UnAuthClient
from accounting_api_tests.config import Settings
from accounting_api_tests.models.generated.models import AccountStatus, AmountRequest

ACCOUNT_ID = UUID("a1111111-1111-1111-1111-111111111111")
ACCOUNT_STATUS = AccountStatus.ACTIVE
HOLDER_ID = UUID("b1111111-1111-1111-1111-111111111111")
HOLDER_NAME = "Alice Johnson"
BASE_URL = "http://testhost"
OPERATION_ID = UUID("c1111111-1111-1111-1111-111111111111")

Handler = Callable[[httpx.Request], httpx.Response]


def create_accounts_client(handler: Handler, settings: Settings) -> AccountClient:
    transport = httpx.MockTransport(handler)
    http_client = httpx.Client(base_url=BASE_URL, transport=transport)
    return AccountClient(
        settings=settings,
        auth=UnAuthClient(),
        client=http_client,
    )


@pytest.mark.unit
def test_deposit_amount_as_string_response_parsed(test_settings: Settings) -> None:
    requests_received: list[Request] = []

    def handler(test_request: Request) -> Response:
        requests_received.append(test_request)
        return Response(
            status_code=200,
            json={
                "operationId": str(OPERATION_ID),
                "account": {
                    "id": str(ACCOUNT_ID),
                    "balance": 3.0,
                    "status": ACCOUNT_STATUS.value,
                    "holder": {
                        "id": str(HOLDER_ID),
                        "displayName": HOLDER_NAME,
                    },
                },
            },
        )

    client = create_accounts_client(handler, test_settings)
    result = client.deposit(ACCOUNT_ID, AmountRequest(operationId=OPERATION_ID, amount=1.0))

    assert result.operation_id == OPERATION_ID
    assert result.account.id == ACCOUNT_ID
    assert result.account.status == ACCOUNT_STATUS.value
    assert result.account.holder.display_name == HOLDER_NAME
    assert result.account.balance == 3.0
    assert len(requests_received) == 1
    request = requests_received[0]
    assert request.method == "POST"
    assert request.url.path == f"/accounts/{ACCOUNT_ID}/deposit"

    body = json.loads(request.content)
    assert body["amount"] == 1.0
