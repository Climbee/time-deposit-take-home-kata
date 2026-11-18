-- Sample data for local runs only (profile "local"); one deposit on each side of every plan boundary.
INSERT INTO time_deposits (plan_type, days, balance) VALUES
    ('basic',   30,  10000.00),
    ('basic',   45,  1234567.00),
    ('student', 365, 10000.00),
    ('student', 366, 10000.00),
    ('premium', 45,  10000.00),
    ('premium', 46,  10000.00);

INSERT INTO withdrawals (time_deposit_id, amount, date) VALUES
    (2, 500.00, '2026-10-01'),
    (2, 250.00, '2026-11-01'),
    (6, 100.00, '2026-11-10');
