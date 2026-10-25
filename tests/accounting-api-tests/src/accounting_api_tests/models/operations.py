# accounting-api-tests/src/accounting_api_tests/models/operations.py
from decimal import Decimal

from pydantic import BaseModel, Field

from accounting_api_tests.models.account import AccountRequest


class AmountRequest(BaseModel):
    amount: Decimal = Field(gt=0)


class DepositRequest(BaseModel):
    amount: Decimal = Field(gt=0)


class WithdrawalRequest(BaseModel):
    amount: Decimal = Field(gt=0)


class TransferRequest(BaseModel):
    amount: Decimal = Field(gt=0)


class TransferResponse(BaseModel):
    sourceAccount: AccountRequest
    targetAccount: AccountRequest
