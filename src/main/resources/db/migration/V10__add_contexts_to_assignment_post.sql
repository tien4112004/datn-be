-- Add contexts column to assignment_post table
ALTER TABLE assignment_post ADD COLUMN contexts jsonb;

-- Add comment for documentation
COMMENT ON COLUMN assignment_post.contexts IS 'Array of reading passages/contexts for questions with contextId';
