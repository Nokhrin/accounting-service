import httpx
from pydantic import SecretStr


class BasicAuthClient:
    def __init__(self, username: str, password: str | SecretStr) -> None:
        pwd = password.get_secret_value() if isinstance(password, SecretStr) else password
        self._auth = httpx.BasicAuth(username, pwd)

    def as_httpx_auth(self) -> httpx.BasicAuth:
        return self._auth


class UnAuthClient:
    def as_httpx_auth(self) -> None:
        return None


def build_auth(*, username: str, password: str | SecretStr) -> BasicAuthClient:
    return BasicAuthClient(username, password)
