import os
from collections.abc import Generator
from unittest.mock import patch

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


@pytest.fixture
def test_settings() -> Settings:
    with patch.dict(
        os.environ,
        {
            "TEST_BASE_URL": "http://testhost",
            "TEST_TIMEOUT_SECONDS": "10",
            "TEST_USERNAME": "user",
            "TEST_PASSWORD": "pass",
            "TEST_DB_HOST": "localhost",
            "TEST_DB_PORT": "5432",
            "TEST_DB_NAME": "test_db",
            "TEST_DB_USER": "test_user",
            "TEST_DB_PASSWORD": "test_pass",
        },
    ):
        return Settings()
