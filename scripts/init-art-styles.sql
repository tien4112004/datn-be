-- Initialize Art Styles table with predefined values
-- This script populates the art_styles table with standard art style options
-- SAFE FOR PRODUCTION: idempotent, non-destructive, and will upsert entries

-- Create table if it does not exist
CREATE TABLE IF NOT EXISTS art_styles (
    id VARCHAR(50) PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    label_key VARCHAR(100) NOT NULL,
    visual VARCHAR(500),
    modifiers VARCHAR(1000),
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data JSONB DEFAULT '{}'::jsonb
);

-- Ensure columns exist (no-op if already present)
ALTER TABLE art_styles ADD COLUMN IF NOT EXISTS label_key VARCHAR(100);
ALTER TABLE art_styles ADD COLUMN IF NOT EXISTS visual VARCHAR(500);
ALTER TABLE art_styles ADD COLUMN IF NOT EXISTS modifiers VARCHAR(1000);
ALTER TABLE art_styles ADD COLUMN IF NOT EXISTS is_enabled BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE art_styles ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE art_styles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE art_styles ADD COLUMN IF NOT EXISTS data JSONB DEFAULT '{}'::jsonb;

-- Create indexes for better query performance (no-op if index exists)
CREATE INDEX IF NOT EXISTS idx_art_styles_is_enabled ON art_styles(is_enabled);
CREATE INDEX IF NOT EXISTS idx_art_styles_created_at ON art_styles(created_at);

-- Upsert Art Style values (inserts new rows or updates existing ones)
BEGIN;
INSERT INTO art_styles (id, name, label_key, visual, modifiers, is_enabled, data)
VALUES
('photorealistic', 'Photorealistic', 'photorealistic', 
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/photorealistic.png', '', true, '{}'::jsonb),

('digital-art', 'Digital Art', 'digitalArt',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/digital-art.png', '', true, '{}'::jsonb),

('oil-painting', 'Oil Painting', 'oilPainting',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/oil-painting.png', '', true, '{}'::jsonb),

('watercolor', 'Watercolor', 'watercolor',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/watercolor.png', '', true, '{}'::jsonb),

('anime', 'Anime', 'anime',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/anime.png', '', true, '{}'::jsonb),

('cartoon', 'Cartoon', 'cartoon',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/cartoon.png', '', true, '{}'::jsonb),

('sketch', 'Sketch', 'sketch',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/sketch.png', '', true, '{}'::jsonb),

('abstract', 'Abstract', 'abstract',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/abstract.png', '', true, '{}'::jsonb),

('surreal', 'Surreal', 'surreal',
 'https://pub-1027a6a79b77462da6aed9adb368a116.r2.dev/art-styles/surreal.png', '', true, '{}'::jsonb),

ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    label_key = EXCLUDED.label_key,
    visual = EXCLUDED.visual,
    modifiers = COALESCE(EXCLUDED.modifiers, art_styles.modifiers),
    is_enabled = EXCLUDED.is_enabled,
    data = COALESCE(EXCLUDED.data, art_styles.data),
    updated_at = CURRENT_TIMESTAMP;
COMMIT;

-- Done. You can verify with: SELECT id, name, label_key, visual, is_enabled FROM art_styles ORDER BY id;


