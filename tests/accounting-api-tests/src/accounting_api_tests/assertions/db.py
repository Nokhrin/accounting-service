from decimal import Decimal
from uuid import UUID

import psycopg

from accounting_api_tests.config import settings


def get_account_balance(account_id: UUID) -> Decimal | None:
    dsn = (
        f"host={settings.db_host} "
        f"port={settings.db_port} "
        f"dbname={settings.db_name} "
        f"user={settings.db_user} "
        f"password={settings.db_password.get_secret_value()}"
    )
    try:
        with psycopg.connect(dsn) as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT balance FROM accounts WHERE id = %s", (str(account_id),))
                row = cur.fetchone()
                return Decimal(row[0]) if row else None
    except psycopg.Error:
        return None


def get_account_status(conn: psycopg.Connection, account_id: UUID) -> str:
    with conn.cursor() as cur:
        cur.execute(
            "SELECT status FROM accounts WHERE id = %s",
            (str(account_id),),
        )
        row = cur.fetchone()
    if row is None:
        raise AssertionError(f"Account {account_id} not found in database")
    return row[0]


def count_deposited_events_for_account(
    conn: psycopg.Connection,
    account_id: UUID,
) -> int:
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT count(*)
            FROM events
            WHERE type = 'FundsDeposited'
              AND payload->>'targetAccountId' = %s
            """,
            (str(account_id),),
        )
        row = cur.fetchone()
    return int(row[0])
