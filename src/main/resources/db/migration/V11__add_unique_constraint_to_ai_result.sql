-- Add unique constraint to ai_result.presentation_id to allow overwriting
-- This ensures only one AI result per presentation
ALTER TABLE ai_result
ADD CONSTRAINT uk_ai_result_presentation_id UNIQUE (presentation_id);
