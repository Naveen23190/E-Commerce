ALTER TABLE payment_attempts ADD COLUMN IF NOT EXISTS gateway_refund_id VARCHAR(80);
