-- src/main/resources/db/seed.sql
-- ACTIVE с положительным балансом (валидный source/target для transfer)
INSERT INTO accounts (id, balance, status, holder_id, holder_display_name) VALUES
  ('a1111111-1111-1111-1111-111111111111', 10000.00, 'ACTIVE', 'b1111111-1111-1111-1111-111111111111', 'Alice Johnson'),
  ('a2222222-2222-2222-2222-222222222222',  5000.00, 'ACTIVE', 'b2222222-2222-2222-2222-222222222222', 'Bob Smith'),
-- ACTIVE с нулевым балансом (валидный target для transfer, candidate для close)
  ('a4444444-4444-4444-4444-444444444444',     0.00, 'ACTIVE', 'b4444444-4444-4444-4444-444444444444', 'Diana Zero'),
-- ACTIVE с минимальным балансом (edge case для DecimalMin 0.01)
  ('a5555555-5555-5555-5555-555555555555',     0.01, 'ACTIVE', 'b5555555-5555-5555-5555-555555555555', 'Eve Cent'),
-- BLOCKED с положительным балансом (операции запрещены -> ACCOUNT_NOT_OPERABLE)
  ('a6666666-6666-6666-6666-666666666666',  1500.00, 'BLOCKED', 'b6666666-6666-6666-6666-666666666666', 'Frank Blocked'),
-- BLOCKED с нулевым балансом (нельзя close без активации -> ACCOUNT_NOT_ACTIVE)
  ('a7777777-7777-7777-7777-777777777777',     0.00, 'BLOCKED', 'b7777777-7777-7777-7777-777777777777', 'Grace ZeroBlocked'),
-- CLOSED с нулевым балансом (единственное валидное конечное состояние -> ACCOUNT_NOT_OPERABLE)
  ('a3333333-3333-3333-3333-333333333333',     0.00, 'CLOSED',  'b3333333-3333-3333-3333-333333333333', 'Charlie Brown')
ON CONFLICT (id) DO NOTHING;