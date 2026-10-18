from uuid import UUID

import httpx

from accounting_api_tests.clients.auth import UnAuthClient, build_auth, BasicAuthClient
from accounting_api_tests.config import Settings, settings as app_settings
from accounting_api_tests.error import ApiError, BadRequestError, ConflictError, ErrorResponse, NotFoundError
from accounting_api_tests.models.account import AccountRequest, OpenAccountRequest
from accounting_api_tests.models.operations import AmountRequest, DepositRequest, TransferRequest, TransferResponse, \
    WithdrawalRequest

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

        self.client.auth = self.auth.as_httpx_auth()

    def close(self) -> None:
        if self._own_client:
            self.client.close()

    def _handle_response(self, response: httpx.Response) -> httpx.Response:
        if response.is_success:
            return response
        error: ErrorResponse | None = None
        try:
            error = ErrorResponse.model_validate_json(response.text)
        except Exception:
            error = None

        if response.status_code == 404: raise NotFoundError(response, error)
        if response.status_code == 409: raise ConflictError(response, error)
        if response.status_code == 400: raise BadRequestError(response, error)
        raise ApiError(response, error)

    def _post(self, path: str, json_payload: dict) -> httpx.Response:
        resp = self.client.post(path, json=json_payload)
        return self._handle_response(resp)

    def _get(self, path: str) -> httpx.Response:
        resp = self.client.get(path)
        return self._handle_response(resp)

    def open_account(self, request: OpenAccountRequest) -> AccountRequest:
        payload = request.model_dump(mode="json")
        print(f"[DEBUG PAYLOAD] {payload}")
        resp = self._post("/accounts", payload)
        return AccountRequest.model_validate_json(resp.text)

    def get_account(self, account_id: UUID) -> AccountRequest:
        resp = self._get(f"/accounts/{account_id}")
        return AccountRequest.model_validate_json(resp.text)

    def deposit(self, account_id: UUID, request: DepositRequest | AmountRequest) -> AccountRequest:
        payload = request.model_dump(mode="json") if hasattr(request, "model_dump") else AmountRequest(
            amount=request).model_dump(mode="json")
        resp = self._post(f"/accounts/{account_id}/deposit", payload)
        return AccountRequest.model_validate_json(resp.text)

    def withdraw(self, account_id: UUID, request: WithdrawalRequest | AmountRequest) -> AccountRequest:
        payload = request.model_dump(mode="json") if hasattr(request, "model_dump") else AmountRequest(
            amount=request).model_dump(mode="json")
        resp = self._post(f"/accounts/{account_id}/withdraw", payload)
        return AccountRequest.model_validate_json(resp.text)

    def transfer(self, source_id: UUID, target_id: UUID, request: TransferRequest | AmountRequest) -> TransferResponse:
        payload = request.model_dump(mode="json") if hasattr(request, "model_dump") else AmountRequest(
            amount=request).model_dump(mode="json")
        resp = self._post(f"/accounts/{source_id}/{target_id}/transfer", payload)
        return TransferResponse.model_validate_json(resp.text)
