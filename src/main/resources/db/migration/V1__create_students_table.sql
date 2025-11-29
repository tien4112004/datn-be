-- Create students table for storing student information
CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    address VARCHAR(500),
    parent_name VARCHAR(255),
    parent_phone VARCHAR(20),
    class_id VARCHAR(50),
    enrollment_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on class_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_students_class_id ON students(class_id);

-- Create index on status for filtering
CREATE INDEX IF NOT EXISTS idx_students_status ON students(status);

-- Add comment to table
COMMENT ON TABLE students IS 'Table for storing student information imported from CSV';
