-- Add assignment settings columns to assignments table
ALTER TABLE assignments
    ADD COLUMN max_submissions INTEGER,
    ADD COLUMN allow_retake BOOLEAN DEFAULT TRUE,
    ADD COLUMN shuffle_questions BOOLEAN DEFAULT FALSE,
    ADD COLUMN show_correct_answers BOOLEAN DEFAULT TRUE,
    ADD COLUMN show_score_immediately BOOLEAN DEFAULT TRUE,
    ADD COLUMN passing_score DOUBLE PRECISION,
    ADD COLUMN time_limit INTEGER,
    ADD COLUMN available_from TIMESTAMP,
    ADD COLUMN available_until TIMESTAMP,
    ADD COLUMN topics JSONB,
    ADD COLUMN matrix_cells JSONB;

-- Set sensible defaults for existing assignments
UPDATE assignments
SET allow_retake = TRUE,
    shuffle_questions = FALSE,
    show_correct_answers = TRUE,
    show_score_immediately = TRUE
WHERE allow_retake IS NULL;

-- Create indexes on availability dates for query performance
CREATE INDEX idx_assignments_available_from ON assignments(available_from);
CREATE INDEX idx_assignments_available_until ON assignments(available_until);
