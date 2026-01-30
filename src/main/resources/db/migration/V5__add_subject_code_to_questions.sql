-- Add subject_code column to questions table
ALTER TABLE questions ADD COLUMN IF NOT EXISTS subject_code VARCHAR(10);

-- Create index for subject_code filtering
CREATE INDEX IF NOT EXISTS idx_questions_subject_code ON questions(subject_code);

-- Update index for combined filtering with subject_code
DROP INDEX IF EXISTS idx_question_filter;
CREATE INDEX idx_question_filter ON questions(owner_id, topic, grade_level, subject_code);

-- Add comment
COMMENT ON COLUMN questions.subject_code IS 'Subject code: T (Toán), TV (Tiếng Việt), TA (Tiếng Anh)';
