-- Create mindmap_comments table
CREATE TABLE IF NOT EXISTS mindmap_comments (
    id VARCHAR(36) PRIMARY KEY,
    mindmap_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    mentioned_users JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_mindmap_comments_mindmap FOREIGN KEY (mindmap_id) REFERENCES mindmaps(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_mindmap_comments_mindmap_id ON mindmap_comments(mindmap_id);
CREATE INDEX IF NOT EXISTS idx_mindmap_comments_user_id ON mindmap_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_mindmap_comments_created_at ON mindmap_comments(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_mindmap_comments_deleted_at ON mindmap_comments(deleted_at) WHERE deleted_at IS NOT NULL;

-- GIN index for JSONB mentioned_users column for efficient querying
CREATE INDEX IF NOT EXISTS idx_mindmap_comments_mentioned_users_gin ON mindmap_comments USING GIN (mentioned_users);

-- Add comments for documentation
COMMENT ON TABLE mindmap_comments IS 'Stores comments for mindmap documents';
COMMENT ON COLUMN mindmap_comments.content IS 'Comment text content with optional @mentions';
COMMENT ON COLUMN mindmap_comments.mentioned_users IS 'JSONB array of user IDs mentioned in the comment';
COMMENT ON COLUMN mindmap_comments.deleted_at IS 'Soft delete timestamp - null means not deleted';
