-- Add contexts column to assignments table to store list of AssignmentContext
ALTER TABLE assignments ADD COLUMN IF NOT EXISTS contexts JSONB;
