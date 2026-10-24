import httpx
import pytest

from accounting_api_tests.config import Settings


@pytest.mark.smoke
def test_health_endpoint_responds_up(app_settings: Settings) -> None:
    response = httpx.get(
        f"{app_settings.base_url}/actuator/health",
        timeout=app_settings.timeout_seconds,
    )

    assert response.status_code == 200, f"Expected HTTP 200, got {response.status_code}. Body: {response.text[:500]}"

    payload = response.json()
    assert isinstance(payload, dict), f"Expected JSON object, got {type(payload)}"
    assert payload.get("status") == "UP", f"Expected status UP, got {payload}"
