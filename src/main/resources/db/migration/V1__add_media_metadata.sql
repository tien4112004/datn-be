-- Migration: Add metadata columns to medias table
-- Execute this manually before deploying the code changes

-- Add new columns
ALTER TABLE medias
  ADD COLUMN IF NOT EXISTS is_generated BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS presentation_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS prompt TEXT,
  ADD COLUMN IF NOT EXISTS model VARCHAR(100),
  ADD COLUMN IF NOT EXISTS provider VARCHAR(50);

-- Add indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_medias_presentation_id ON medias(presentation_id);
CREATE INDEX IF NOT EXISTS idx_medias_is_generated ON medias(is_generated);
CREATE INDEX IF NOT EXISTS idx_medias_model ON medias(model);
CREATE INDEX IF NOT EXISTS idx_medias_provider ON medias(provider);

-- Optional: Update existing records to explicitly set is_generated = false
-- (This is redundant since DEFAULT FALSE is already set, but included for clarity)
UPDATE medias SET is_generated = FALSE WHERE is_generated IS NULL;
