# accounting-api-tests/src/accounting_api_tests/models/account.py
from decimal import Decimal
from enum import StrEnum
from uuid import UUID

from pydantic import BaseModel, Field


class AccountStatus(StrEnum):
    ACTIVE = "ACTIVE"
    BLOCKED = "BLOCKED"
    CLOSED = "CLOSED"


class AccountHolderResponse(BaseModel):
    id: UUID
    displayName: str


class AccountRequest(BaseModel):
    id: UUID
    balance: Decimal
    status: AccountStatus
    holder: AccountHolderResponse


class AccountHolderRequest(BaseModel):
    id: UUID
    displayName: str


class OpenAccountRequest(BaseModel):
    initialBalance: Decimal = Field(ge=0)
    holder: AccountHolderRequest


class BlockAccountRequest(BaseModel):
    id: UUID


class ActivateAccountRequest(BaseModel):
    id: UUID


class CloseAccountRequest(BaseModel):
    id: UUID
