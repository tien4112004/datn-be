-- 1. TEACHERS
CREATE TABLE teachers (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. CLASSES
CREATE TABLE classes (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(50) NOT NULL,
    grade SMALLINT NOT NULL CHECK (grade >= 1 AND grade <= 12),
    academic_year VARCHAR(9) NOT NULL,
    current_enrollment INTEGER DEFAULT 0,
    teacher_id CHAR(36),
    classroom VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- 3. STUDENTS (Removed class_id)
CREATE TABLE students (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('male', 'female', 'other') NOT NULL,
    address VARCHAR(255),
    parent_name VARCHAR(100),
    parent_phone VARCHAR(20),
    enrollment_date DATE NOT NULL COMMENT 'Date student joined the school',
    status ENUM('active', 'transferred', 'graduated', 'dropped') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. CLASS ENROLLMENTS (New N-N Junction Table)
CREATE TABLE class_enrollments (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    class_id CHAR(36) NOT NULL,
    student_id CHAR(36) NOT NULL,
    enrolled_at DATE DEFAULT (CURRENT_DATE),
    status ENUM('active', 'dropped', 'completed') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_enrollment (class_id, student_id),
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- 5. SEATING LAYOUTS (JSON)
CREATE TABLE seating_layouts (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    class_id CHAR(36) UNIQUE,
    layout_config JSON NOT NULL COMMENT 'Contains rows, columns, separator_interval, and seat array',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 6. SCHEDULE PERIODS -- TO BE REMOVED OR MERGE WITH THE LESSON
CREATE TABLE schedule_periods (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    class_id CHAR(36),
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    category ENUM('assignment', 'exam', 'fieldTrip', 'meeting', 'holiday', 'presentation', 'other') DEFAULT 'other',
    location VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 7. LESSONS (JSON Objectives)
CREATE TABLE lessons (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    class_id CHAR(36),
    subject VARCHAR(20) COMMENT 'actually the "tag" for the lesson, example: Math-1', 
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status ENUM('draft', 'published') DEFAULT 'published',
    type ENUM('assignment', 'anouncement', 'lesson')
    notes TEXT,
    -- teacher parts
    learning_objectives JSON COMMENT 'Array of objects: {description, type, is_achieved, notes}',
    lesson_plan TEXT COMMENT 'Lesson plan, formatted in rich text or markdown',
    
    -- student parts, for quickly clone to class
    -- clone behavior: If a lesson is cloned for class, do not include "teacher part"
    attachments JSON COMMENT 'Array of attachment objects: {id, name, url, file_path, type}, may be the lesson_resources',
    due_date DATETIME COMMENT 'May be due date or the teaching date, based on kind of lesson',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 8. SCHEDULE PERIOD LESSONS (Junction) -- TO BE REMOVED
CREATE TABLE schedule_period_lessons (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    period_id CHAR(36),
    lesson_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_period_lesson (period_id, lesson_id),
    FOREIGN KEY (period_id) REFERENCES schedule_periods(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- 9. LESSON RESOURCES
CREATE TABLE lesson_resources (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    lesson_id CHAR(36),
    name VARCHAR(200) NOT NULL,
    type ENUM('presentation', 'mindmap', 'document', 'video', 'audio', 'image', 'worksheet', 'equipment', 'other') NOT NULL,
    url VARCHAR(500),
    file_path VARCHAR(500),
    description TEXT,
    is_required BOOLEAN DEFAULT FALSE,
    is_prepared BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- INDEXES
CREATE INDEX idx_classes_grade ON classes(grade);
CREATE INDEX idx_classes_academic_year ON classes(academic_year);
CREATE INDEX idx_classes_teacher_id ON classes(teacher_id);

CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_full_name ON students(full_name);

CREATE INDEX idx_class_enrollments_class_id ON class_enrollments(class_id);
CREATE INDEX idx_class_enrollments_student_id ON class_enrollments(student_id);

CREATE INDEX idx_schedule_periods_class_id ON schedule_periods(class_id);
CREATE INDEX idx_schedule_periods_date ON schedule_periods(date);

CREATE INDEX idx_lessons_class_id ON lessons(class_id);
CREATE INDEX idx_lessons_subject ON lessons(subject);

CREATE INDEX idx_lesson_resources_lesson_id ON lesson_resources(lesson_id);