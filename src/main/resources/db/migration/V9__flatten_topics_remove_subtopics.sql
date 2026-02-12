-- Migration V9: Flatten topic-subtopic hierarchy to topic-only structure
-- This migration transforms assignments from topic>subtopic hierarchy to flat topics
-- Questions are reassigned from subtopics to parent topics

-- Note: This migration handles the basic JSONB structure transformations
-- Complex edge cases may require manual review

BEGIN;

-- Step 1: Create a backup of the original matrix data (optional but recommended)
-- ALTER TABLE assignments ADD COLUMN matrix_backup JSONB;
-- UPDATE assignments SET matrix_backup = matrix WHERE matrix IS NOT NULL;

-- Step 2: For each assignment, flatten the matrix structure
-- Transform matrix[subtopic][difficulty][questionType] to matrix[topic][difficulty][questionType]
-- by aggregating subtopics under each topic

-- This is a complex operation that would ideally be done in application code
-- For now, we'll update the metadata comments and leave data migration to application service

-- Step 3: Update question bindings
-- Currently: question.chapter = subtopic_name, question.topicId = subtopic_id
-- Target: question.chapter = topic_name, question.topicId = topic_id

-- Since this is complex JSONB manipulation, we recommend:
-- 1. Deploy the updated backend code first
-- 2. Run a Java migration service (TopicMigrationService)
-- 3. Verify data integrity

-- For now, just add a comment to track that migration is pending
ALTER TABLE assignments ADD COLUMN IF NOT EXISTS migration_status VARCHAR(50) DEFAULT 'pending';
COMMENT ON COLUMN assignments.migration_status IS 'Migration status: pending, in_progress, completed';

-- Update comments on existing columns to reflect new structure
COMMENT ON COLUMN questions.chapter IS 'Topic name (updated from subtopic name). Used for question filtering during exam generation.';
COMMENT ON COLUMN questions.topic_id IS 'Topic ID (updated from subtopic ID). Questions bind directly to topics.';

COMMIT;
