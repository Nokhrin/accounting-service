import httpx
from pydantic import BaseModel


class ErrorResponse(BaseModel):
    code: str
    message: str
    operationId: str | None = None
    details: list[str] | None = None


class ApiError(Exception):
    def __init__(self, response: httpx.Response, error: ErrorResponse | None = None):
        self.response = response
        self.error = error
        msg = error.model_dump_json() if error else response.text
        super().__init__(f"API Error {response.status_code}: {msg}")


class NotFoundError(ApiError):
    pass


class ConflictError(ApiError):
    pass


class BadRequestError(ApiError):
    pass
