-- Add feedback column to submissions table for overall teacher feedback
ALTER TABLE submissions ADD COLUMN IF NOT EXISTS feedback TEXT;
