-- Create coin_pricing table for storing AI feature pricing configuration
CREATE TABLE IF NOT EXISTS coin_pricing (
    id VARCHAR(36) PRIMARY KEY,
    resource_type VARCHAR(50) NOT NULL,
    model_name VARCHAR(100),
    base_cost INTEGER NOT NULL CHECK (base_cost >= 0),
    unit_type VARCHAR(50) DEFAULT 'PER_REQUEST',
    unit_multiplier DECIMAL(10, 2) DEFAULT 1.0,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_coin_pricing_resource_model UNIQUE (resource_type, model_name)
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_coin_pricing_resource_type ON coin_pricing(resource_type);
CREATE INDEX IF NOT EXISTS idx_coin_pricing_model_name ON coin_pricing(model_name);
CREATE INDEX IF NOT EXISTS idx_coin_pricing_active ON coin_pricing(is_active) WHERE deleted_at IS NULL;

-- Add comments for documentation
COMMENT ON TABLE coin_pricing IS 'Configuration table for AI feature coin pricing';
COMMENT ON COLUMN coin_pricing.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN coin_pricing.resource_type IS 'Type of AI resource: PRESENTATION, SLIDE, IMAGE, MINDMAP, QUESTION, ASSIGNMENT, OUTLINE';
COMMENT ON COLUMN coin_pricing.model_name IS 'Specific model name for model-specific pricing, NULL for default pricing';
COMMENT ON COLUMN coin_pricing.base_cost IS 'Base coin cost for the resource';
COMMENT ON COLUMN coin_pricing.unit_type IS 'Unit of pricing: PER_REQUEST, PER_SLIDE, PER_IMAGE, PER_QUESTION';
COMMENT ON COLUMN coin_pricing.unit_multiplier IS 'Multiplier for quantity-based pricing';
COMMENT ON COLUMN coin_pricing.description IS 'Optional description of the pricing rule';
COMMENT ON COLUMN coin_pricing.is_active IS 'Whether this pricing rule is active';
COMMENT ON COLUMN coin_pricing.deleted_at IS 'Soft delete timestamp';

-- Insert initial pricing data
INSERT INTO coin_pricing (id, resource_type, model_name, base_cost, unit_type, unit_multiplier, description, is_active, created_at, updated_at)
VALUES
    (gen_random_uuid()::text, 'OUTLINE', NULL, 5, 'PER_REQUEST', 1.0, 'Outline generation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', NULL, 50, 'PER_REQUEST', 1.0, 'Full presentation generation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', NULL, 5, 'PER_SLIDE', 1.0, 'Individual slide regeneration', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'IMAGE', NULL, 10, 'PER_IMAGE', 1.0, 'Image generation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', NULL, 20, 'PER_REQUEST', 1.0, 'Mindmap generation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', NULL, 3, 'PER_QUESTION', 1.0, 'Question generation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', NULL, 15, 'PER_REQUEST', 1.0, 'Assignment generation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_type, model_name) DO NOTHING;
