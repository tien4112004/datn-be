-- Convert DocumentVisit timestamp columns from TIMESTAMP to TIMESTAMP WITH TIME ZONE
-- This migration converts local timestamps to UTC timestamps (Instant type in Java)

-- Note: Adjust the timezone 'Asia/Ho_Chi_Minh' if your database server is in a different timezone
-- Common alternatives: 'UTC', 'Asia/Bangkok', 'America/New_York', etc.

-- Convert last_visited to TIMESTAMPTZ
ALTER TABLE document_visits
    ALTER COLUMN last_visited TYPE TIMESTAMP WITH TIME ZONE
    USING last_visited AT TIME ZONE 'Asia/Ho_Chi_Minh';

-- Convert created_at to TIMESTAMPTZ
ALTER TABLE document_visits
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE
    USING created_at AT TIME ZONE 'Asia/Ho_Chi_Minh';

-- Convert updated_at to TIMESTAMPTZ
ALTER TABLE document_visits
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE
    USING updated_at AT TIME ZONE 'Asia/Ho_Chi_Minh';
