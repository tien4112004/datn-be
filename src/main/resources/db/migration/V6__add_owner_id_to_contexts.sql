-- Add owner_id column to contexts table for personal/public context support
-- NULL means public context (accessible to all), non-NULL means personal (owned by specific user)
ALTER TABLE contexts ADD COLUMN IF NOT EXISTS owner_id VARCHAR(36);

-- Create index for efficient querying by owner_id
CREATE INDEX IF NOT EXISTS idx_contexts_owner_id ON contexts(owner_id);
