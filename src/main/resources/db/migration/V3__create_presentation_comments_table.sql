-- Create presentation_comments table
CREATE TABLE IF NOT EXISTS presentation_comments (
    id VARCHAR(36) PRIMARY KEY,
    presentation_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    mentioned_users JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_presentation_comments_presentation FOREIGN KEY (presentation_id) REFERENCES presentations(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_presentation_comments_presentation_id ON presentation_comments(presentation_id);
CREATE INDEX IF NOT EXISTS idx_presentation_comments_user_id ON presentation_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_presentation_comments_created_at ON presentation_comments(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_presentation_comments_deleted_at ON presentation_comments(deleted_at) WHERE deleted_at IS NOT NULL;

-- GIN index for JSONB mentioned_users column for efficient querying
CREATE INDEX IF NOT EXISTS idx_presentation_comments_mentioned_users_gin ON presentation_comments USING GIN (mentioned_users);

-- Add comments for documentation
COMMENT ON TABLE presentation_comments IS 'Stores comments for presentation documents';
COMMENT ON COLUMN presentation_comments.content IS 'Comment text content with optional @mentions';
COMMENT ON COLUMN presentation_comments.mentioned_users IS 'JSONB array of user IDs mentioned in the comment';
COMMENT ON COLUMN presentation_comments.deleted_at IS 'Soft delete timestamp - null means not deleted';
