import os

import httpx
import pytest

BASE_URL = os.getenv("ACCOUNTING_API_TESTS_BASE_URL", "http://localhost:8080")
TIMEOUT_SECONDS = float(os.getenv("ACCOUNTING_API_TESTS_TIMEOUT_SECONDS", "10"))


@pytest.mark.smoke
def test_health_endpoint_responds_up() -> None:
    response = httpx.get(
        f"{BASE_URL}/actuator/health",
        timeout=TIMEOUT_SECONDS,
    )

    assert response.status_code == 200, f"Expected HTTP 200, got {response.status_code}. Body: {response.text[:500]}"

    payload = response.json()
    assert isinstance(payload, dict), f"Expected JSON object, got {type(payload)}"
    assert payload.get("status") == "UP", f"Expected status UP, got {payload}"
