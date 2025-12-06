-- Create presentations table with JSONB support
CREATE TABLE presentations (
    -- Primary columns
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    
    -- JSONB column to store the entire slides array with nested structure
    slides JSONB NOT NULL DEFAULT '[]'::jsonb,
    
    -- Metadata as JSONB for flexible schema
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATE,
    
    -- Boolean flag
    is_parsed BOOLEAN DEFAULT FALSE
);

-- Indexes for performance
CREATE INDEX idx_presentations_created_at ON presentations(created_at);
CREATE INDEX idx_presentations_updated_at ON presentations(updated_at);
CREATE INDEX idx_presentations_deleted_at ON presentations(deleted_at) WHERE deleted_at IS NOT NULL;

-- GIN indexes for JSONB columns (enables efficient querying)
CREATE INDEX idx_presentations_slides_gin ON presentations USING GIN (slides);
CREATE INDEX idx_presentations_metadata_gin ON presentations USING GIN (metadata);

-- Optional: Uncomment these indexes if you frequently query specific JSONB paths
-- CREATE INDEX idx_presentations_slides_id ON presentations USING GIN ((slides -> 'id'));
-- CREATE INDEX idx_presentations_slides_elements ON presentations USING GIN ((slides -> 'elements'));

-- Comments for documentation
COMMENT ON TABLE presentations IS 'Stores presentation documents with slides in JSONB format';
COMMENT ON COLUMN presentations.slides IS 'JSONB array containing all slides with nested elements and backgrounds';
COMMENT ON COLUMN presentations.metadata IS 'JSONB object for flexible metadata storage';
COMMENT ON COLUMN presentations.is_parsed IS 'Flag indicating whether the presentation has been parsed';

--- ============================================================================ ---

-- create mindmaps table with JSONB support
CREATE TABLE IF NOT EXISTS mindmaps (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,

    nodes JSONB,
    edges JSONB,
    
    -- Audit columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes for common queries
    CONSTRAINT mindmaps_id_check CHECK (id IS NOT NULL)
);

-- Create index on title for search queries
CREATE INDEX idx_mindmaps_title ON mindmaps(title);

-- Create index on created_at for time-based queries
CREATE INDEX idx_mindmaps_created_at ON mindmaps(created_at DESC);

-- Create index on updated_at for time-based queries
CREATE INDEX idx_mindmaps_updated_at ON mindmaps(updated_at DESC);

-- Create JSONB indexes for efficient querying of nested structures
-- Index on node IDs for queries like: nodes @> '[{"id": "node-id"}]'
CREATE INDEX idx_mindmaps_nodes_jsonb ON mindmaps USING GIN(nodes);

-- Index on edge IDs for queries like: edges @> '[{"id": "edge-id"}]'
CREATE INDEX idx_mindmaps_edges_jsonb ON mindmaps USING GIN(edges);

-- Create sequence for ID generation (if using SERIAL instead of UUID)
-- This can be removed if using UUID generation at application level
CREATE SEQUENCE IF NOT EXISTS mindmaps_id_seq;


