from collections.abc import Generator

import pytest

from accounting_api_tests.clients.account import AccountClient
from accounting_api_tests.clients.auth import UnAuthClient, build_auth
from accounting_api_tests.config import Settings, settings


@pytest.fixture(scope="session")
def app_settings() -> Settings:
    settings.spring_password.get_secret_value()
    return settings


@pytest.fixture(scope="session")
def account_client(app_settings: Settings) -> Generator[AccountClient, None, None]:
    auth = build_auth(
        username=app_settings.spring_username,
        password=app_settings.spring_password,
    )
    client = AccountClient(settings=app_settings, auth=auth)
    yield client
    client.close()


@pytest.fixture(scope="session")
def unauthenticated_account_client(app_settings: Settings) -> Generator[AccountClient, None, None]:
    client = AccountClient(settings=app_settings, auth=UnAuthClient())
    yield client
    client.close()
