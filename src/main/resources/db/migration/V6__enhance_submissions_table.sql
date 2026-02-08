-- Add new columns to submissions table for assignment feature alignment
ALTER TABLE submissions
    ADD COLUMN assignment_id VARCHAR(36),
    ADD COLUMN graded_by VARCHAR(36),
    ADD COLUMN graded_at TIMESTAMP,
    ADD COLUMN max_score INTEGER,
    ADD COLUMN submitted_at TIMESTAMP,
    ADD COLUMN score INTEGER,
    ADD COLUMN grades JSONB;

-- Populate assignment_id from posts table for existing records
UPDATE submissions s
SET assignment_id = p.assignment_id
FROM posts p
WHERE s.post_id = p.id
  AND p.assignment_id IS NOT NULL;

-- Set submitted_at to created_at for existing records
UPDATE submissions
SET submitted_at = created_at
WHERE submitted_at IS NULL;

-- Create indexes for performance
CREATE INDEX idx_submissions_assignment_id ON submissions(assignment_id);
CREATE INDEX idx_submissions_student_id ON submissions(student_id);
CREATE INDEX idx_submissions_status ON submissions(status);
CREATE INDEX idx_submissions_graded_by ON submissions(graded_by);
CREATE INDEX idx_submissions_assignment_student ON submissions(assignment_id, student_id);
