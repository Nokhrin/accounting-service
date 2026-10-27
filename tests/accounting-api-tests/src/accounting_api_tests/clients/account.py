from typing import Any
from uuid import UUID

import httpx
from pydantic import BaseModel

from accounting_api_tests.clients.auth import BasicAuthClient, UnAuthClient, build_auth
from accounting_api_tests.config import Settings
from accounting_api_tests.config import settings as app_settings
from accounting_api_tests.error import ApiError, BadRequestError, ConflictError, NotFoundError
from accounting_api_tests.models.generated.models import (
    AccountResponse,
    AmountRequest,
    DepositResponse,
    ErrorResponse,
    OpenAccountRequest,
    TransferResponse,
    WithdrawalResponse,
)

AuthLike = BasicAuthClient | UnAuthClient | None


class AccountClient:
    def __init__(
        self,
        settings: Settings | None = None,
        auth: AuthLike = None,
        client: httpx.Client | None = None,
    ) -> None:
        self.settings = settings or app_settings
        self.auth = auth or build_auth(username=self.settings.spring_username, password=self.settings.spring_password)

        if client is not None:
            self.client = client
            self._own_client = False
        else:
            self.client = httpx.Client(
                base_url=self.settings.base_url,
                timeout=self.settings.timeout_seconds,
            )
            self._own_client = True
        if isinstance(self.auth, BasicAuthClient):
            as_httpx_auth = self.auth.as_httpx_auth()
            if as_httpx_auth is not None:
                self.client.auth = as_httpx_auth

    def close(self) -> None:
        if self._own_client:
            self.client.close()

    @staticmethod
    def _handle_response(response: httpx.Response) -> httpx.Response:
        if response.is_success:
            return response
        error: ErrorResponse | None = None
        try:
            error = ErrorResponse.model_validate_json(response.text)
        except Exception:
            error = None

        if response.status_code == 404:
            raise NotFoundError(response, error)
        if response.status_code == 409:
            raise ConflictError(response, error)
        if response.status_code == 400:
            raise BadRequestError(response, error)
        raise ApiError(response, error)

    def _post(self, path: str, json_payload: dict[str, Any]) -> httpx.Response:
        resp = self.client.post(path, json=json_payload)
        return self._handle_response(resp)

    def _get(self, path: str) -> httpx.Response:
        resp = self.client.get(path)
        return self._handle_response(resp)

    @staticmethod
    def _payload(request: BaseModel) -> dict[str, Any]:
        return request.model_dump(mode="json", by_alias=True, exclude_none=True)

    def open_account(self, request: OpenAccountRequest) -> AccountResponse:
        resp = self._post("/accounts", self._payload(request))
        return AccountResponse.model_validate_json(resp.text)

    def get_account(self, account_id: UUID) -> AccountResponse:
        resp = self._get(f"/accounts/{account_id}")
        return AccountResponse.model_validate_json(resp.text)

    def deposit(self, account_id: UUID, request: AmountRequest) -> DepositResponse:
        resp = self._post(f"/accounts/{account_id}/deposit", self._payload(request))
        return DepositResponse.model_validate_json(resp.text)

    def withdraw(self, account_id: UUID, request: AmountRequest) -> WithdrawalResponse:
        resp = self._post(f"/accounts/{account_id}/withdraw", self._payload(request))
        return WithdrawalResponse.model_validate_json(resp.text)

    def transfer(self, source_id: UUID, target_id: UUID, request: AmountRequest) -> TransferResponse:
        resp = self._post(f"/accounts/{source_id}/{target_id}/transfer", self._payload(request))
        return TransferResponse.model_validate_json(resp.text)
