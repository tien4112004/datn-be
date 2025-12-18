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
    owner_id CHAR(36) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    join_code VARCHAR(10) UNIQUE,
    settings JSONB COMMENT 'Class-specific settings like theme, allow_comments, etc.',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES teachers(id)
);

-- 3. STUDENTS
CREATE TABLE students (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('male', 'female', 'other') NOT NULL,
    address VARCHAR(255),
    parent_name VARCHAR(100),
    parent_phone VARCHAR(20),
    class_id CHAR(36),
    enrollment_date DATE NOT NULL,
    status ENUM('active', 'transferred', 'graduated', 'dropped') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE SET NULL
);

-- 4. SEATING LAYOUTS (JSON)
CREATE TABLE seating_layouts (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    class_id CHAR(36) UNIQUE,
    layout_config JSON NOT NULL COMMENT 'Contains rows, columns, separator_interval, and seat array',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 5. SCHEDULE PERIODS
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

-- 6. LESSONS (Now with JSON Objectives)
CREATE TABLE lessons (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    class_id CHAR(36),
    class_name VARCHAR(50) NOT NULL,
    subject VARCHAR(20),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration INTEGER NOT NULL,
    status ENUM('planned', 'in_progress', 'completed', 'cancelled') DEFAULT 'planned',
    notes TEXT,
    learning_objectives JSON COMMENT 'Array of objects: {description, type, is_achieved, notes}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 7. SCHEDULE PERIOD LESSONS (Junction)
CREATE TABLE schedule_period_lessons (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    period_id CHAR(36),
    lesson_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_period_lesson (period_id, lesson_id),
    FOREIGN KEY (period_id) REFERENCES schedule_periods(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- 8. LESSON RESOURCES
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
CREATE INDEX idx_classes_owner_id ON classes(owner_id);
CREATE INDEX idx_classes_join_code ON classes(join_code);
CREATE INDEX idx_classes_is_active ON classes(is_active);

CREATE INDEX idx_students_class_id ON students(class_id);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_full_name ON students(full_name);

CREATE INDEX idx_schedule_periods_class_id ON schedule_periods(class_id);
CREATE INDEX idx_schedule_periods_date ON schedule_periods(date);
CREATE INDEX idx_schedule_periods_class_date ON schedule_periods(class_id, date);

CREATE INDEX idx_lessons_class_id ON lessons(class_id);
CREATE INDEX idx_lessons_subject ON lessons(subject);
CREATE INDEX idx_lessons_status ON lessons(status);

CREATE INDEX idx_lesson_resources_lesson_id ON lesson_resources(lesson_id);