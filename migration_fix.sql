-- SQL Migration Script to Fix Notifications Table
-- This script can be used if you want to preserve existing data
-- Run this script directly in PostgreSQL before starting the application

-- Option 1: Update existing rows with default values (if you have existing data)
-- Update NULL values in existing rows
UPDATE notifications SET title = 'Notification' WHERE title IS NULL;
UPDATE notifications SET type = 'GENERAL' WHERE type IS NULL;
UPDATE notifications SET recipient_id = 0 WHERE recipient_id IS NULL;
UPDATE notifications SET appointment_id = 0 WHERE appointment_id IS NULL;
UPDATE notifications SET is_read = false WHERE is_read IS NULL;

-- Then alter columns to be NOT NULL
ALTER TABLE notifications ALTER COLUMN title SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN type SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN recipient_id SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN appointment_id SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN is_read SET NOT NULL;

-- Option 2: Drop and recreate the table (if you don't need existing data)
-- DROP TABLE IF EXISTS notifications CASCADE;

-- The table will be recreated automatically by Hibernate on next startup

