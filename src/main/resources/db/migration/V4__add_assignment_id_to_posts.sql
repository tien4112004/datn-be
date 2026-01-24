-- Add assignment_id column to posts table for linking Homework posts to assignments
ALTER TABLE posts ADD COLUMN IF NOT EXISTS assignment_id VARCHAR(36);

-- Add due_date column for Homework posts
ALTER TABLE posts ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;

-- Create index for efficient querying by assignment_id
CREATE INDEX IF NOT EXISTS idx_posts_assignment_id ON posts(assignment_id);

-- Create index for efficient querying by due_date (for upcoming homework queries)
CREATE INDEX IF NOT EXISTS idx_posts_due_date ON posts(due_date);

-- Add comments for documentation
COMMENT ON COLUMN posts.assignment_id IS 'References an assignment for Homework type posts to enable submission and evaluation workflows';
COMMENT ON COLUMN posts.due_date IS 'Due date for Homework type posts';
