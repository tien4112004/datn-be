-- Create coin_pricing table for storing AI feature pricing configuration
CREATE TABLE IF NOT EXISTS coin_pricing (
    id VARCHAR(36) PRIMARY KEY,
    resource_type VARCHAR(50) NOT NULL,
    model_id INTEGER REFERENCES model_configuration(id),
    base_cost INTEGER NOT NULL CHECK (base_cost >= 0),
    unit_type VARCHAR(50) DEFAULT 'PER_REQUEST',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_coin_pricing_resource_model UNIQUE (resource_type, model_id)
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_coin_pricing_resource_type ON coin_pricing(resource_type);
CREATE INDEX IF NOT EXISTS idx_coin_pricing_model_id ON coin_pricing(model_id);

-- Add comments for documentation
COMMENT ON TABLE coin_pricing IS 'Configuration table for AI feature coin pricing';
COMMENT ON COLUMN coin_pricing.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN coin_pricing.resource_type IS 'Type of AI resource: PRESENTATION, SLIDE, IMAGE, MINDMAP, QUESTION, ASSIGNMENT, OUTLINE';
COMMENT ON COLUMN coin_pricing.model_id IS 'Foreign key to model_configuration, NULL for default pricing';
COMMENT ON COLUMN coin_pricing.base_cost IS 'Base coin cost for the resource';
COMMENT ON COLUMN coin_pricing.unit_type IS 'Unit of pricing: PER_REQUEST, PER_SLIDE, PER_IMAGE, PER_QUESTION';
COMMENT ON COLUMN coin_pricing.description IS 'Optional description of the pricing rule';
COMMENT ON COLUMN coin_pricing.deleted_at IS 'Soft delete timestamp';

-- Insert initial pricing data
INSERT INTO coin_pricing (id, resource_type, model_id, base_cost, unit_type, description, created_at, updated_at)
VALUES
    (gen_random_uuid()::text, 'OUTLINE', NULL, 5, 'PER_REQUEST', 'Outline generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', NULL, 50, 'PER_REQUEST', 'Full presentation generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', NULL, 5, 'PER_SLIDE', 'Individual slide regeneration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'IMAGE', NULL, 10, 'PER_IMAGE', 'Image generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', NULL, 20, 'PER_REQUEST', 'Mindmap generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', NULL, 3, 'PER_QUESTION', 'Question generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', NULL, 15, 'PER_REQUEST', 'Assignment generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_type, model_id) DO NOTHING;
