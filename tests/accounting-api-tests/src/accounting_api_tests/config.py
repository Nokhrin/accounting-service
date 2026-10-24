from pathlib import Path

from pydantic import Field, SecretStr
from pydantic_settings import BaseSettings, SettingsConfigDict


def get_project_root() -> Path:
    current = Path(__file__).resolve()
    for parent in current.parents:
        if (parent / "pom.xml").exists():
            return parent
    return current.parents[4]


PROJECT_ROOT = get_project_root()
ENV_FILE_PATH = PROJECT_ROOT / ".env"


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(ENV_FILE_PATH),
        env_file_encoding="utf-8",
        extra="ignore",
    )

    base_url: str = Field(validation_alias="TEST_BASE_URL")
    timeout_seconds: float = Field(validation_alias="TEST_TIMEOUT_SECONDS")
    spring_username: str = Field(validation_alias="TEST_USERNAME")
    spring_password: SecretStr = Field(validation_alias="TEST_PASSWORD")
    db_host: str = Field(validation_alias="TEST_DB_HOST")
    db_port: int = Field(validation_alias="TEST_DB_PORT")
    db_name: str = Field(validation_alias="TEST_DB_NAME")
    db_user: str = Field(validation_alias="TEST_DB_USER")
    db_password: SecretStr = Field(validation_alias="TEST_DB_PASSWORD")


settings = Settings()
