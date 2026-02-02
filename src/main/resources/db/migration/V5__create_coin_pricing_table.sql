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

-- Insert default pricing (NULL model_id = applies to all models)
INSERT INTO coin_pricing (id, resource_type, model_id, base_cost, unit_type, description, created_at, updated_at)
VALUES
    -- Default pricing for all resources
    (gen_random_uuid()::text, 'OUTLINE', NULL, 5, 'PER_REQUEST', 'Default outline generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', NULL, 50, 'PER_REQUEST', 'Default presentation generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', NULL, 5, 'PER_SLIDE', 'Default slide regeneration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', NULL, 20, 'PER_REQUEST', 'Default mindmap generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', NULL, 3, 'PER_QUESTION', 'Default question generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', NULL, 15, 'PER_REQUEST', 'Default assignment generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'IMAGE', NULL, 10, 'PER_IMAGE', 'Default image generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- TEXT Model 1: GPT-4 Optimized (gpt-4o-mini)
    (gen_random_uuid()::text, 'OUTLINE', 1, 6, 'PER_REQUEST', 'GPT-4 Optimized', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', 1, 60, 'PER_REQUEST', 'GPT-4 Optimized', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', 1, 6, 'PER_SLIDE', 'GPT-4 Optimized', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', 1, 24, 'PER_REQUEST', 'GPT-4 Optimized', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', 1, 4, 'PER_QUESTION', 'GPT-4 Optimized', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', 1, 18, 'PER_REQUEST', 'GPT-4 Optimized', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- TEXT Model 2: GPT-4.1 Nano
    (gen_random_uuid()::text, 'OUTLINE', 2, 4, 'PER_REQUEST', 'GPT-4.1 Nano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', 2, 40, 'PER_REQUEST', 'GPT-4.1 Nano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', 2, 4, 'PER_SLIDE', 'GPT-4.1 Nano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', 2, 16, 'PER_REQUEST', 'GPT-4.1 Nano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', 2, 2, 'PER_QUESTION', 'GPT-4.1 Nano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', 2, 12, 'PER_REQUEST', 'GPT-4.1 Nano', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- TEXT Model 3: Gemini 2.5 Flash (default)
    (gen_random_uuid()::text, 'OUTLINE', 3, 5, 'PER_REQUEST', 'Gemini 2.5 Flash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', 3, 50, 'PER_REQUEST', 'Gemini 2.5 Flash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', 3, 5, 'PER_SLIDE', 'Gemini 2.5 Flash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', 3, 20, 'PER_REQUEST', 'Gemini 2.5 Flash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', 3, 3, 'PER_QUESTION', 'Gemini 2.5 Flash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', 3, 15, 'PER_REQUEST', 'Gemini 2.5 Flash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- TEXT Model 4: Gemini 2.5 Flash Lite
    (gen_random_uuid()::text, 'OUTLINE', 4, 3, 'PER_REQUEST', 'Gemini 2.5 Flash Lite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', 4, 30, 'PER_REQUEST', 'Gemini 2.5 Flash Lite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', 4, 3, 'PER_SLIDE', 'Gemini 2.5 Flash Lite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', 4, 12, 'PER_REQUEST', 'Gemini 2.5 Flash Lite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', 4, 2, 'PER_QUESTION', 'Gemini 2.5 Flash Lite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', 4, 10, 'PER_REQUEST', 'Gemini 2.5 Flash Lite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- TEXT Model 9: Mistral Small 3.2 24B Instruct (free tier)
    (gen_random_uuid()::text, 'OUTLINE', 9, 2, 'PER_REQUEST', 'Mistral Small 3.2 24B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', 9, 20, 'PER_REQUEST', 'Mistral Small 3.2 24B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', 9, 2, 'PER_SLIDE', 'Mistral Small 3.2 24B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', 9, 8, 'PER_REQUEST', 'Mistral Small 3.2 24B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', 9, 1, 'PER_QUESTION', 'Mistral Small 3.2 24B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', 9, 6, 'PER_REQUEST', 'Mistral Small 3.2 24B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- TEXT Model 16: Gemma 3
    (gen_random_uuid()::text, 'OUTLINE', 16, 3, 'PER_REQUEST', 'Gemma 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'PRESENTATION', 16, 30, 'PER_REQUEST', 'Gemma 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'SLIDE', 16, 3, 'PER_SLIDE', 'Gemma 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'MINDMAP', 16, 12, 'PER_REQUEST', 'Gemma 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'QUESTION', 16, 2, 'PER_QUESTION', 'Gemma 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid()::text, 'ASSIGNMENT', 16, 10, 'PER_REQUEST', 'Gemma 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- IMAGE Model 5: Nano Banana (Gemini 2.5 Flash Image) - default
    (gen_random_uuid()::text, 'IMAGE', 5, 10, 'PER_IMAGE', 'Nano Banana (Gemini 2.5 Flash Image)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- IMAGE Model 13: Nano Banana Pro (Gemini 3 Pro Image)
    (gen_random_uuid()::text, 'IMAGE', 13, 15, 'PER_IMAGE', 'Nano Banana Pro (Gemini 3 Pro Image)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- IMAGE Model 14: Imagen 4 Fast
    (gen_random_uuid()::text, 'IMAGE', 14, 20, 'PER_IMAGE', 'Imagen 4 Fast', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- IMAGE Model 15: Imagen 4 Ultra
    (gen_random_uuid()::text, 'IMAGE', 15, 30, 'PER_IMAGE', 'Imagen 4 Ultra', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_type, model_id) DO NOTHING;
