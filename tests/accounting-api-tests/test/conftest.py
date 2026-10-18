import pytest

from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.clients.auth import build_auth, UnAuthClient
from accounting_api_tests.config import Settings, settings


@pytest.fixture(scope="session")
def app_settings() -> Settings:
    return settings


@pytest.fixture(scope="session")
def account_client(app_settings: Settings) -> AccountClient:
    auth = build_auth(
        username=app_settings.spring_username,
        password=app_settings.spring_password,
    )
    client = AccountClient(settings=app_settings, auth=auth)
    yield client
    client.close()


@pytest.fixture(scope="session")
def unauthenticated_account_client(app_settings: Settings) -> AccountClient:
    client = AccountClient(settings=app_settings, auth=UnAuthClient())
    yield client
    client.close()
