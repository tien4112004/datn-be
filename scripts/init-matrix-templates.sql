-- =====================================================
-- Matrix Template Seed Data - Kết nối tri thức và cuộc sống
-- Table: assignment_matrix_templates
-- 30 Templates: 2 exam types × 3 subjects × 5 grades
--
-- Exam types: Giữa kì 1 (Midterm), Cuối kì 1 (Final)
-- Subjects: Toán (T), Tiếng Việt (TV), Tiếng Anh (TA)
-- Grades: 1-5
--
-- Grade-based progression:
--   Grade 1-2: difficulties=[KNOWLEDGE], questionTypes=[MC, FB]
--   Grade 3-4: difficulties=[KNOWLEDGE, COMPREHENSION], questionTypes=[MC, FB, MA]
--   Grade 5:   difficulties=[KNOWLEDGE, COMPREHENSION, APPLICATION], questionTypes=[MC, FB, MA, OE]
--
-- Matrix cell format: "count:pointsPerQuestion"
-- matrix[topicIndex][difficultyIndex][questionTypeIndex]
-- All templates total 10 points
-- owner_id = NULL for public templates
-- =====================================================

-- Clean previous public seed templates
DELETE FROM assignment_matrix_templates WHERE owner_id IS NULL;


-- =====================================================
-- TOÁN (T) - GIỮA KÌ 1
-- =====================================================

-- Template 1: Giữa kì 1 Toán Lớp 1
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Toán 1 - Kết nối tri thức', '1', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t1_gk1", "name": "Ma trận đề thi giữa kì 1 Toán 1 - Kết nối tri thức", "grade": "1", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Các số từ 0 đến 10", "hasContext": false, "subtopics": [{"id": "1", "name": "Chủ đề 1: Các số từ 0 đến 10"}]},
            {"id": "2", "name": "Hình phẳng", "hasContext": false, "subtopics": [{"id": "2", "name": "Chủ đề 2: Làm quen với một số hình phẳng"}]},
            {"id": "3", "name": "Phép cộng, phép trừ trong phạm vi 10", "hasContext": false, "subtopics": [{"id": "3", "name": "Chủ đề 3: Phép cộng, phép trừ trong phạm vi 10"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["2:0.5", "6:0.5"]],
        [["2:0.5", "4:0.5"]],
        [["2:0.5", "4:0.5"]]
    ]
}'::jsonb);

-- Template 2: Giữa kì 1 Toán Lớp 2
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Toán 2 - Kết nối tri thức', '2', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t2_gk1", "name": "Ma trận đề thi giữa kì 1 Toán 2 - Kết nối tri thức", "grade": "2", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và bổ sung", "hasContext": false, "subtopics": [{"id": "11", "name": "Chủ đề 1: Ôn tập và bổ sung"}]},
            {"id": "2", "name": "Phép cộng, phép trừ trong phạm vi 20", "hasContext": false, "subtopics": [{"id": "12", "name": "Chủ đề 2: Phép cộng, phép trừ trong phạm vi 20"}]},
            {"id": "3", "name": "Khối lượng và dung tích", "hasContext": false, "subtopics": [{"id": "13", "name": "Chủ đề 3: Làm quen với khối lượng và dung tích"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["2:0.5", "4:0.5"]],
        [["2:0.5", "6:0.5"]],
        [["2:0.5", "4:0.5"]]
    ]
}'::jsonb);

-- Template 3: Giữa kì 1 Toán Lớp 3
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Toán 3 - Kết nối tri thức', '3', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t3_gk1", "name": "Ma trận đề thi giữa kì 1 Toán 3 - Kết nối tri thức", "grade": "3", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và bổ sung", "hasContext": false, "subtopics": [{"id": "25", "name": "Chủ đề 1: Ôn tập và bổ sung"}]},
            {"id": "2", "name": "Bảng nhân, bảng chia", "hasContext": false, "subtopics": [{"id": "26", "name": "Chủ đề 2: Bảng nhân, bảng chia"}]},
            {"id": "3", "name": "Hình phẳng, hình khối", "hasContext": false, "subtopics": [{"id": "27", "name": "Chủ đề 3: Làm quen với hình phẳng, hình khối"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["0:0", "4:0.5", "0:0"], ["1:0.5", "1:0.5", "1:1.0"]],
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]],
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]]
    ]
}'::jsonb);

-- Template 4: Giữa kì 1 Toán Lớp 4
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Toán 4 - Kết nối tri thức', '4', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t4_gk1", "name": "Ma trận đề thi giữa kì 1 Toán 4 - Kết nối tri thức", "grade": "4", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và bổ sung", "hasContext": false, "subtopics": [{"id": "41", "name": "Chủ đề 1: Ôn tập và bổ sung"}]},
            {"id": "2", "name": "Góc và đơn vị đo góc", "hasContext": false, "subtopics": [{"id": "42", "name": "Chủ đề 2: Góc và đơn vị đo góc"}]},
            {"id": "3", "name": "Số có nhiều chữ số", "hasContext": false, "subtopics": [{"id": "43", "name": "Chủ đề 3: Số có nhiều chữ số"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]],
        [["0:0", "4:0.5", "0:0"], ["1:0.5", "1:0.5", "1:1.0"]],
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]]
    ]
}'::jsonb);

-- Template 5: Giữa kì 1 Toán Lớp 5
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Toán 5 - Kết nối tri thức', '5', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t5_gk1", "name": "Ma trận đề thi giữa kì 1 Toán 5 - Kết nối tri thức", "grade": "5", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và bổ sung", "hasContext": false, "subtopics": [{"id": "55", "name": "Chủ đề 1: Ôn tập và bổ sung"}]},
            {"id": "2", "name": "Số thập phân", "hasContext": false, "subtopics": [{"id": "56", "name": "Chủ đề 2: Số thập phân"}]},
            {"id": "3", "name": "Đơn vị đo diện tích", "hasContext": false, "subtopics": [{"id": "57", "name": "Chủ đề 3: Một số đơn vị đo diện tích"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING", "OPEN_ENDED"]
    },
    "matrix": [
        [["0:0", "3:0.5", "0:0", "0:0"], ["1:0.5", "1:0.5", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:1.0"]],
        [["0:0", "3:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["0:0", "3:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]]
    ]
}'::jsonb);


-- =====================================================
-- TOÁN (T) - CUỐI KÌ 1
-- =====================================================

-- Template 6: Cuối kì 1 Toán Lớp 1
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Toán 1 - Kết nối tri thức', '1', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t1_ck1", "name": "Ma trận đề thi cuối kì 1 Toán 1 - Kết nối tri thức", "grade": "1", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Các số từ 0 đến 10", "hasContext": false, "subtopics": [{"id": "1", "name": "Chủ đề 1: Các số từ 0 đến 10"}]},
            {"id": "2", "name": "Hình phẳng", "hasContext": false, "subtopics": [{"id": "2", "name": "Chủ đề 2: Làm quen với một số hình phẳng"}]},
            {"id": "3", "name": "Phép cộng, phép trừ trong phạm vi 10", "hasContext": false, "subtopics": [{"id": "3", "name": "Chủ đề 3: Phép cộng, phép trừ trong phạm vi 10"}]},
            {"id": "4", "name": "Hình khối", "hasContext": false, "subtopics": [{"id": "4", "name": "Chủ đề 4: Làm quen với một số hình khối"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["2:0.5", "4:0.5"]],
        [["2:0.5", "4:0.5"]],
        [["2:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 7: Cuối kì 1 Toán Lớp 2
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Toán 2 - Kết nối tri thức', '2', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t2_ck1", "name": "Ma trận đề thi cuối kì 1 Toán 2 - Kết nối tri thức", "grade": "2", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và Phép tính phạm vi 20", "hasContext": false, "subtopics": [{"id": "11", "name": "Chủ đề 1: Ôn tập và bổ sung"}, {"id": "12", "name": "Chủ đề 2: Phép cộng, phép trừ trong phạm vi 20"}]},
            {"id": "2", "name": "Khối lượng và dung tích", "hasContext": false, "subtopics": [{"id": "13", "name": "Chủ đề 3: Làm quen với khối lượng và dung tích"}]},
            {"id": "3", "name": "Phép cộng, trừ có nhớ phạm vi 100", "hasContext": false, "subtopics": [{"id": "14", "name": "Chủ đề 4: Phép cộng, phép trừ (có nhớ) trong phạm vi 100"}]},
            {"id": "4", "name": "Hình phẳng và Thời gian", "hasContext": false, "subtopics": [{"id": "15", "name": "Chủ đề 5: Làm quen với hình phẳng"}, {"id": "16", "name": "Chủ đề 6: Ngày - tháng, giờ - phút"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["2:0.5", "4:0.5"]],
        [["2:0.5", "4:0.5"]],
        [["2:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 8: Cuối kì 1 Toán Lớp 3
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Toán 3 - Kết nối tri thức', '3', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t3_ck1", "name": "Ma trận đề thi cuối kì 1 Toán 3 - Kết nối tri thức", "grade": "3", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và Bảng nhân chia", "hasContext": false, "subtopics": [{"id": "25", "name": "Chủ đề 1: Ôn tập và bổ sung"}, {"id": "26", "name": "Chủ đề 2: Bảng nhân, bảng chia"}]},
            {"id": "2", "name": "Hình phẳng, hình khối", "hasContext": false, "subtopics": [{"id": "27", "name": "Chủ đề 3: Làm quen với hình phẳng, hình khối"}]},
            {"id": "3", "name": "Phép nhân, chia trong phạm vi 100-1000", "hasContext": false, "subtopics": [{"id": "28", "name": "Chủ đề 4: Phép nhân, phép chia trong phạm vi 100"}, {"id": "30", "name": "Chủ đề 6: Phép nhân, phép chia trong phạm vi 1000"}]},
            {"id": "4", "name": "Đơn vị đo", "hasContext": false, "subtopics": [{"id": "29", "name": "Chủ đề 5: Một số đơn vị đo độ dài, khối lượng, dung tích, nhiệt độ"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]],
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]],
        [["1:0.5", "1:0.5", "0:0"], ["0:0", "1:0.5", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0"], ["0:0", "1:0.5", "1:0.5"]]
    ]
}'::jsonb);

-- Template 9: Cuối kì 1 Toán Lớp 4
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Toán 4 - Kết nối tri thức', '4', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t4_ck1", "name": "Ma trận đề thi cuối kì 1 Toán 4 - Kết nối tri thức", "grade": "4", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và Góc", "hasContext": false, "subtopics": [{"id": "41", "name": "Chủ đề 1: Ôn tập và bổ sung"}, {"id": "42", "name": "Chủ đề 2: Góc và đơn vị đo góc"}]},
            {"id": "2", "name": "Số có nhiều chữ số", "hasContext": false, "subtopics": [{"id": "43", "name": "Chủ đề 3: Số có nhiều chữ số"}]},
            {"id": "3", "name": "Đơn vị đo đại lượng", "hasContext": false, "subtopics": [{"id": "44", "name": "Chủ đề 4: Một số đơn vị đo đại lượng"}]},
            {"id": "4", "name": "Phép cộng, trừ và Đường thẳng", "hasContext": false, "subtopics": [{"id": "45", "name": "Chủ đề 5: Phép cộng và phép trừ"}, {"id": "46", "name": "Chủ đề 6: Đường thẳng vuông góc, đường thẳng song song"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]],
        [["1:0.5", "2:0.5", "0:0"], ["0:0", "1:0.5", "1:1.0"]],
        [["1:0.5", "1:0.5", "0:0"], ["0:0", "1:0.5", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0"], ["0:0", "1:0.5", "1:0.5"]]
    ]
}'::jsonb);

-- Template 10: Cuối kì 1 Toán Lớp 5
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Toán 5 - Kết nối tri thức', '5', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_t5_ck1", "name": "Ma trận đề thi cuối kì 1 Toán 5 - Kết nối tri thức", "grade": "5", "subject": "T", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Ôn tập và Số thập phân", "hasContext": false, "subtopics": [{"id": "55", "name": "Chủ đề 1: Ôn tập và bổ sung"}, {"id": "56", "name": "Chủ đề 2: Số thập phân"}]},
            {"id": "2", "name": "Đơn vị đo diện tích", "hasContext": false, "subtopics": [{"id": "57", "name": "Chủ đề 3: Một số đơn vị đo diện tích"}]},
            {"id": "3", "name": "Phép tính với số thập phân", "hasContext": false, "subtopics": [{"id": "58", "name": "Chủ đề 4: Các phép tính với số thập phân"}]},
            {"id": "4", "name": "Hình phẳng, chu vi, diện tích", "hasContext": false, "subtopics": [{"id": "59", "name": "Chủ đề 5: Một số hình phẳng. Chu vi và diện tích"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING", "OPEN_ENDED"]
    },
    "matrix": [
        [["0:0", "3:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["0:0", "3:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0", "0:0"], ["0:0", "1:0.5", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0", "0:0"], ["0:0", "1:0.5", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]]
    ]
}'::jsonb);


-- =====================================================
-- TIẾNG VIỆT (TV) - GIỮA KÌ 1
-- =====================================================

-- Template 11: Giữa kì 1 Tiếng Việt Lớp 1
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Việt 1 - Kết nối tri thức', '1', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv1_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Việt 1 - Kết nối tri thức", "grade": "1", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Học vần: Chữ cái và dấu thanh", "hasContext": false, "subtopics": [{"id": "67", "name": "a, b, c, dấu huyền, dấu sắc"}, {"id": "68", "name": "o, ô, ơ, dấu hỏi, dấu nặng"}, {"id": "69", "name": "e, ê, h, i, k, l"}, {"id": "70", "name": "m, n, p, ph, d, đ"}]},
            {"id": "2", "name": "Học vần: Phụ âm và âm ghép", "hasContext": false, "subtopics": [{"id": "71", "name": "g, gh, q, qu, r, s"}, {"id": "72", "name": "t, th, u, ư, v, x"}, {"id": "73", "name": "y, tr, ch, gi, kh"}, {"id": "74", "name": "ng, ngh, nh"}]},
            {"id": "3", "name": "Viết chính tả", "hasContext": false, "subtopics": [{"id": "67", "name": "a, b, c, dấu huyền, dấu sắc"}, {"id": "68", "name": "o, ô, ơ, dấu hỏi, dấu nặng"}, {"id": "71", "name": "g, gh, q, qu, r, s"}, {"id": "72", "name": "t, th, u, ư, v, x"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["6:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 12: Giữa kì 1 Tiếng Việt Lớp 2
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Việt 2 - Kết nối tri thức', '2', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv2_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Việt 2 - Kết nối tri thức", "grade": "2", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Em lớn lên từng ngày", "hasContext": true, "subtopics": [{"id": "93", "name": "Chủ điểm 1: Em lớn lên từng ngày"}]},
            {"id": "2", "name": "Đọc hiểu: Đi học vui sao", "hasContext": true, "subtopics": [{"id": "94", "name": "Chủ điểm 2: Đi học vui sao"}]},
            {"id": "3", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "93", "name": "Chủ điểm 1: Em lớn lên từng ngày"}, {"id": "94", "name": "Chủ điểm 2: Đi học vui sao"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["6:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 13: Giữa kì 1 Tiếng Việt Lớp 3
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Việt 3 - Kết nối tri thức', '3', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv3_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Việt 3 - Kết nối tri thức", "grade": "3", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Những trải nghiệm thú vị", "hasContext": true, "subtopics": [{"id": "102", "name": "Chủ điểm 1: Những trải nghiệm thú vị"}]},
            {"id": "2", "name": "Đọc hiểu: Cổng trường rộng mở", "hasContext": true, "subtopics": [{"id": "103", "name": "Chủ điểm 2: Cổng trường rộng mở"}]},
            {"id": "3", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "102", "name": "Chủ điểm 1: Những trải nghiệm thú vị"}, {"id": "103", "name": "Chủ điểm 2: Cổng trường rộng mở"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["4:0.5", "0:0", "0:0"], ["1:0.5", "1:0.5", "1:1.0"]],
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]],
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]]
    ]
}'::jsonb);

-- Template 14: Giữa kì 1 Tiếng Việt Lớp 4
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Việt 4 - Kết nối tri thức', '4', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv4_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Việt 4 - Kết nối tri thức", "grade": "4", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Mỗi người một vẻ", "hasContext": true, "subtopics": [{"id": "110", "name": "Chủ điểm 1: Mỗi người một vẻ"}]},
            {"id": "2", "name": "Đọc hiểu: Trải nghiệm và khám phá", "hasContext": true, "subtopics": [{"id": "111", "name": "Chủ điểm 2: Trải nghiệm và khám phá"}]},
            {"id": "3", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "110", "name": "Chủ điểm 1: Mỗi người một vẻ"}, {"id": "111", "name": "Chủ điểm 2: Trải nghiệm và khám phá"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["4:0.5", "0:0", "0:0"], ["1:0.5", "1:0.5", "1:1.0"]],
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]],
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]]
    ]
}'::jsonb);

-- Template 15: Giữa kì 1 Tiếng Việt Lớp 5
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Việt 5 - Kết nối tri thức', '5', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv5_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Việt 5 - Kết nối tri thức", "grade": "5", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Thế giới tuổi thơ", "hasContext": true, "subtopics": [{"id": "121", "name": "Chủ điểm 1: Thế giới tuổi thơ"}]},
            {"id": "2", "name": "Đọc hiểu: Thiên nhiên kì thú", "hasContext": true, "subtopics": [{"id": "122", "name": "Chủ điểm 2: Thiên nhiên kì thú"}]},
            {"id": "3", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "121", "name": "Chủ điểm 1: Thế giới tuổi thơ"}, {"id": "122", "name": "Chủ điểm 2: Thiên nhiên kì thú"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING", "OPEN_ENDED"]
    },
    "matrix": [
        [["3:0.5", "0:0", "0:0", "0:0"], ["1:0.5", "1:0.5", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:1.0"]],
        [["2:0.5", "1:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["2:0.5", "1:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]]
    ]
}'::jsonb);


-- =====================================================
-- TIẾNG VIỆT (TV) - CUỐI KÌ 1
-- =====================================================

-- Template 16: Cuối kì 1 Tiếng Việt Lớp 1
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Việt 1 - Kết nối tri thức', '1', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv1_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Việt 1 - Kết nối tri thức", "grade": "1", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Học vần: Chữ cái và dấu thanh", "hasContext": false, "subtopics": [{"id": "67", "name": "a, b, c, dấu huyền, dấu sắc"}, {"id": "68", "name": "o, ô, ơ, dấu hỏi, dấu nặng"}, {"id": "69", "name": "e, ê, h, i, k, l"}, {"id": "70", "name": "m, n, p, ph, d, đ"}]},
            {"id": "2", "name": "Học vần: Phụ âm và âm ghép", "hasContext": false, "subtopics": [{"id": "71", "name": "g, gh, q, qu, r, s"}, {"id": "72", "name": "t, th, u, ư, v, x"}, {"id": "73", "name": "y, tr, ch, gi, kh"}, {"id": "74", "name": "ng, ngh, nh"}]},
            {"id": "3", "name": "Vần có âm cuối", "hasContext": false, "subtopics": [{"id": "75", "name": "ai, oi, ơi, ui, ưi"}, {"id": "76", "name": "ay, ây, eo, ao, au, âu"}, {"id": "77", "name": "iu, ưu, am, âm, em, êm"}, {"id": "78", "name": "im, um, an, ăn, ân"}, {"id": "79", "name": "en, ên, in, un, iên, yên, uôn, ươn"}, {"id": "80", "name": "ang, ăng, âng, ong, ông, ung, ưng"}, {"id": "81", "name": "anh, ênh, inh, ach, êch, ich"}, {"id": "82", "name": "at, ăt, ất, ot, ôt, ơt, et, êt, it, ut, ưt"}]},
            {"id": "4", "name": "Vần phức", "hasContext": false, "subtopics": [{"id": "83", "name": "uê, uy, uơ, uya"}, {"id": "84", "name": "uân, uyên, uât, uyêt"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 17: Cuối kì 1 Tiếng Việt Lớp 2
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Việt 2 - Kết nối tri thức', '2', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv2_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Việt 2 - Kết nối tri thức", "grade": "2", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Em lớn lên và Đi học vui", "hasContext": true, "subtopics": [{"id": "93", "name": "Chủ điểm 1: Em lớn lên từng ngày"}, {"id": "94", "name": "Chủ điểm 2: Đi học vui sao"}]},
            {"id": "2", "name": "Đọc hiểu: Niềm vui tuổi thơ", "hasContext": true, "subtopics": [{"id": "95", "name": "Chủ điểm 3: Niềm vui tuổi thơ"}]},
            {"id": "3", "name": "Đọc hiểu: Mái ấm gia đình", "hasContext": true, "subtopics": [{"id": "96", "name": "Chủ điểm 4: Mái ấm gia đình"}]},
            {"id": "4", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "93", "name": "Chủ điểm 1: Em lớn lên từng ngày"}, {"id": "94", "name": "Chủ điểm 2: Đi học vui sao"}, {"id": "95", "name": "Chủ điểm 3: Niềm vui tuổi thơ"}, {"id": "96", "name": "Chủ điểm 4: Mái ấm gia đình"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 18: Cuối kì 1 Tiếng Việt Lớp 3
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Việt 3 - Kết nối tri thức', '3', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv3_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Việt 3 - Kết nối tri thức", "grade": "3", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Trải nghiệm và Cổng trường", "hasContext": true, "subtopics": [{"id": "102", "name": "Chủ điểm 1: Những trải nghiệm thú vị"}, {"id": "103", "name": "Chủ điểm 2: Cổng trường rộng mở"}]},
            {"id": "2", "name": "Đọc hiểu: Mái nhà và Cộng đồng", "hasContext": true, "subtopics": [{"id": "104", "name": "Chủ điểm 3: Mái nhà yêu thương"}, {"id": "105", "name": "Chủ điểm 4: Cộng đồng gắn bó"}]},
            {"id": "3", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "102", "name": "Chủ điểm 1: Những trải nghiệm thú vị"}, {"id": "103", "name": "Chủ điểm 2: Cổng trường rộng mở"}, {"id": "104", "name": "Chủ điểm 3: Mái nhà yêu thương"}, {"id": "105", "name": "Chủ điểm 4: Cộng đồng gắn bó"}]},
            {"id": "4", "name": "Viết", "hasContext": false, "subtopics": [{"id": "102", "name": "Chủ điểm 1: Những trải nghiệm thú vị"}, {"id": "103", "name": "Chủ điểm 2: Cổng trường rộng mở"}, {"id": "104", "name": "Chủ điểm 3: Mái nhà yêu thương"}, {"id": "105", "name": "Chủ điểm 4: Cộng đồng gắn bó"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]],
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]],
        [["1:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:0.5"]]
    ]
}'::jsonb);

-- Template 19: Cuối kì 1 Tiếng Việt Lớp 4
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Việt 4 - Kết nối tri thức', '4', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv4_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Việt 4 - Kết nối tri thức", "grade": "4", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Mỗi người một vẻ và Trải nghiệm", "hasContext": true, "subtopics": [{"id": "110", "name": "Chủ điểm 1: Mỗi người một vẻ"}, {"id": "111", "name": "Chủ điểm 2: Trải nghiệm và khám phá"}]},
            {"id": "2", "name": "Đọc hiểu: Sáng tạo và Ước mơ", "hasContext": true, "subtopics": [{"id": "112", "name": "Chủ điểm 3: Niềm vui sáng tạo"}, {"id": "113", "name": "Chủ điểm 4: Chắp cánh ước mơ"}]},
            {"id": "3", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "110", "name": "Chủ điểm 1: Mỗi người một vẻ"}, {"id": "111", "name": "Chủ điểm 2: Trải nghiệm và khám phá"}, {"id": "112", "name": "Chủ điểm 3: Niềm vui sáng tạo"}, {"id": "113", "name": "Chủ điểm 4: Chắp cánh ước mơ"}]},
            {"id": "4", "name": "Viết", "hasContext": false, "subtopics": [{"id": "110", "name": "Chủ điểm 1: Mỗi người một vẻ"}, {"id": "111", "name": "Chủ điểm 2: Trải nghiệm và khám phá"}, {"id": "112", "name": "Chủ điểm 3: Niềm vui sáng tạo"}, {"id": "113", "name": "Chủ điểm 4: Chắp cánh ước mơ"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]],
        [["2:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:1.0"]],
        [["1:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0"], ["1:0.5", "0:0", "1:0.5"]]
    ]
}'::jsonb);

-- Template 20: Cuối kì 1 Tiếng Việt Lớp 5
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Việt 5 - Kết nối tri thức', '5', 'TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_tv5_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Việt 5 - Kết nối tri thức", "grade": "5", "subject": "TV", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Đọc hiểu: Tuổi thơ và Thiên nhiên", "hasContext": true, "subtopics": [{"id": "121", "name": "Chủ điểm 1: Thế giới tuổi thơ"}, {"id": "122", "name": "Chủ điểm 2: Thiên nhiên kì thú"}]},
            {"id": "2", "name": "Đọc hiểu: Học tập và Nghệ thuật", "hasContext": true, "subtopics": [{"id": "123", "name": "Chủ điểm 3: Trên con đường học tập"}, {"id": "124", "name": "Chủ điểm 4: Nghệ thuật muôn màu"}]},
            {"id": "3", "name": "Luyện từ và câu", "hasContext": false, "subtopics": [{"id": "121", "name": "Chủ điểm 1: Thế giới tuổi thơ"}, {"id": "122", "name": "Chủ điểm 2: Thiên nhiên kì thú"}, {"id": "123", "name": "Chủ điểm 3: Trên con đường học tập"}, {"id": "124", "name": "Chủ điểm 4: Nghệ thuật muôn màu"}]},
            {"id": "4", "name": "Viết", "hasContext": false, "subtopics": [{"id": "121", "name": "Chủ điểm 1: Thế giới tuổi thơ"}, {"id": "122", "name": "Chủ điểm 2: Thiên nhiên kì thú"}, {"id": "123", "name": "Chủ điểm 3: Trên con đường học tập"}, {"id": "124", "name": "Chủ điểm 4: Nghệ thuật muôn màu"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING", "OPEN_ENDED"]
    },
    "matrix": [
        [["2:0.5", "1:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["2:0.5", "1:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "1:0.5", "0:0", "0:0"], ["1:0.5", "0:0", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]]
    ]
}'::jsonb);


-- =====================================================
-- TIẾNG ANH (TA) - GIỮA KÌ 1
-- =====================================================

-- Template 21: Giữa kì 1 Tiếng Anh Lớp 1
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Anh 1 - Kết nối tri thức', '1', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta1_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Anh 1 - Kết nối tri thức", "grade": "1", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Phonics: Sounds /b/ and /c/", "hasContext": false, "subtopics": [{"id": "133", "name": "Unit 1 - In the school playground (Âm /b/)"}, {"id": "134", "name": "Unit 2 - In the dining room (Âm /c/)"}]},
            {"id": "2", "name": "Phonics: Sounds /a/ and /d/", "hasContext": false, "subtopics": [{"id": "135", "name": "Unit 3 - At the street market (Âm /a/)"}, {"id": "136", "name": "Unit 4 - In the bedroom (Âm /d/)"}]},
            {"id": "3", "name": "Vocabulary and Communication", "hasContext": false, "subtopics": [{"id": "133", "name": "Unit 1 - In the school playground (Âm /b/)"}, {"id": "134", "name": "Unit 2 - In the dining room (Âm /c/)"}, {"id": "135", "name": "Unit 3 - At the street market (Âm /a/)"}, {"id": "136", "name": "Unit 4 - In the bedroom (Âm /d/)"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["4:0.5", "4:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 22: Giữa kì 1 Tiếng Anh Lớp 2
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Anh 2 - Kết nối tri thức', '2', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta2_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Anh 2 - Kết nối tri thức", "grade": "2", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Phonics: Sounds /p/ and /r/", "hasContext": false, "subtopics": [{"id": "149", "name": "Unit 1: At my birthday party (Âm /p/)"}, {"id": "150", "name": "Unit 2: In the backyard (Âm /r/)"}]},
            {"id": "2", "name": "Phonics: Sounds /s/ and /t/", "hasContext": false, "subtopics": [{"id": "151", "name": "Unit 3: At the seaside (Âm /s/)"}, {"id": "152", "name": "Unit 4: In the countryside (Âm /t/)"}]},
            {"id": "3", "name": "Vocabulary and Communication", "hasContext": false, "subtopics": [{"id": "149", "name": "Unit 1: At my birthday party (Âm /p/)"}, {"id": "150", "name": "Unit 2: In the backyard (Âm /r/)"}, {"id": "151", "name": "Unit 3: At the seaside (Âm /s/)"}, {"id": "152", "name": "Unit 4: In the countryside (Âm /t/)"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["4:0.5", "4:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 23: Giữa kì 1 Tiếng Anh Lớp 3
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Anh 3 - Kết nối tri thức', '3', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta3_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Anh 3 - Kết nối tri thức", "grade": "3", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Greetings and Introduction", "hasContext": false, "subtopics": [{"id": "165", "name": "Unit 1: Hello"}, {"id": "166", "name": "Unit 2: Our names"}, {"id": "167", "name": "Unit 3: Our friends"}]},
            {"id": "2", "name": "Body and Hobbies", "hasContext": false, "subtopics": [{"id": "168", "name": "Unit 4: Our bodies"}, {"id": "169", "name": "Unit 5: My hobbies"}]},
            {"id": "3", "name": "Reading and Communication", "hasContext": true, "subtopics": [{"id": "165", "name": "Unit 1: Hello"}, {"id": "166", "name": "Unit 2: Our names"}, {"id": "167", "name": "Unit 3: Our friends"}, {"id": "168", "name": "Unit 4: Our bodies"}, {"id": "169", "name": "Unit 5: My hobbies"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["2:0.5", "0:0", "2:0.5"], ["1:0.5", "1:0.5", "1:1.0"]],
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]],
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]]
    ]
}'::jsonb);

-- Template 24: Giữa kì 1 Tiếng Anh Lớp 4
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Anh 4 - Kết nối tri thức', '4', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta4_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Anh 4 - Kết nối tri thức", "grade": "4", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Friends and Daily Life", "hasContext": false, "subtopics": [{"id": "185", "name": "Unit 1: My friends"}, {"id": "186", "name": "Unit 2: Time and daily routines"}, {"id": "187", "name": "Unit 3: My week"}]},
            {"id": "2", "name": "Celebrations and Abilities", "hasContext": false, "subtopics": [{"id": "188", "name": "Unit 4: My birthday party"}, {"id": "189", "name": "Unit 5: Things we can do"}]},
            {"id": "3", "name": "Reading Comprehension", "hasContext": true, "subtopics": [{"id": "185", "name": "Unit 1: My friends"}, {"id": "186", "name": "Unit 2: Time and daily routines"}, {"id": "187", "name": "Unit 3: My week"}, {"id": "188", "name": "Unit 4: My birthday party"}, {"id": "189", "name": "Unit 5: Things we can do"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["2:0.5", "0:0", "2:0.5"], ["1:0.5", "1:0.5", "1:1.0"]],
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]],
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]]
    ]
}'::jsonb);

-- Template 25: Giữa kì 1 Tiếng Anh Lớp 5
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi giữa kì 1 Tiếng Anh 5 - Kết nối tri thức', '5', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta5_gk1", "name": "Ma trận đề thi giữa kì 1 Tiếng Anh 5 - Kết nối tri thức", "grade": "5", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "About Me and My Home", "hasContext": false, "subtopics": [{"id": "205", "name": "Unit 1: All about me"}, {"id": "206", "name": "Unit 2: Our homes"}]},
            {"id": "2", "name": "Friends and Activities", "hasContext": false, "subtopics": [{"id": "207", "name": "Unit 3: My foreign friends"}, {"id": "208", "name": "Unit 4: Our free-time activities"}]},
            {"id": "3", "name": "Reading: Future Jobs", "hasContext": true, "subtopics": [{"id": "209", "name": "Unit 5: My future job"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING", "OPEN_ENDED"]
    },
    "matrix": [
        [["2:0.5", "0:0", "1:0.5", "0:0"], ["1:0.5", "1:0.5", "1:0.5", "0:0"], ["0:0", "0:0", "0:0", "1:1.0"]],
        [["1:0.5", "0:0", "2:0.5", "0:0"], ["1:0.5", "1:0.5", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "0:0", "2:0.5", "0:0"], ["1:0.5", "1:0.5", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]]
    ]
}'::jsonb);


-- =====================================================
-- TIẾNG ANH (TA) - CUỐI KÌ 1
-- =====================================================

-- Template 26: Cuối kì 1 Tiếng Anh Lớp 1
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Anh 1 - Kết nối tri thức', '1', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta1_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Anh 1 - Kết nối tri thức", "grade": "1", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Phonics: /b/, /c/, /a/, /d/", "hasContext": false, "subtopics": [{"id": "133", "name": "Unit 1 - In the school playground (Âm /b/)"}, {"id": "134", "name": "Unit 2 - In the dining room (Âm /c/)"}, {"id": "135", "name": "Unit 3 - At the street market (Âm /a/)"}, {"id": "136", "name": "Unit 4 - In the bedroom (Âm /d/)"}]},
            {"id": "2", "name": "Phonics: /f/, /g/", "hasContext": false, "subtopics": [{"id": "137", "name": "Unit 5 - At the fish tank (Âm /f/)"}, {"id": "138", "name": "Unit 6 - In the classroom (Âm /g/)"}]},
            {"id": "3", "name": "Phonics: /h/, /i/", "hasContext": false, "subtopics": [{"id": "139", "name": "Unit 7 - In the garden (Âm /h/)"}, {"id": "140", "name": "Unit 8 - In the park (Âm /i/)"}]},
            {"id": "4", "name": "Vocabulary and Communication", "hasContext": false, "subtopics": [{"id": "133", "name": "Unit 1 - In the school playground (Âm /b/)"}, {"id": "134", "name": "Unit 2 - In the dining room (Âm /c/)"}, {"id": "135", "name": "Unit 3 - At the street market (Âm /a/)"}, {"id": "136", "name": "Unit 4 - In the bedroom (Âm /d/)"}, {"id": "137", "name": "Unit 5 - At the fish tank (Âm /f/)"}, {"id": "138", "name": "Unit 6 - In the classroom (Âm /g/)"}, {"id": "139", "name": "Unit 7 - In the garden (Âm /h/)"}, {"id": "140", "name": "Unit 8 - In the park (Âm /i/)"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 27: Cuối kì 1 Tiếng Anh Lớp 2
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Anh 2 - Kết nối tri thức', '2', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta2_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Anh 2 - Kết nối tri thức", "grade": "2", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Phonics: /p/, /r/, /s/, /t/", "hasContext": false, "subtopics": [{"id": "149", "name": "Unit 1: At my birthday party (Âm /p/)"}, {"id": "150", "name": "Unit 2: In the backyard (Âm /r/)"}, {"id": "151", "name": "Unit 3: At the seaside (Âm /s/)"}, {"id": "152", "name": "Unit 4: In the countryside (Âm /t/)"}]},
            {"id": "2", "name": "Phonics: /u/, /v/", "hasContext": false, "subtopics": [{"id": "153", "name": "Unit 5: In the classroom (Âm /u/)"}, {"id": "154", "name": "Unit 6: On the farm (Âm /v/)"}]},
            {"id": "3", "name": "Phonics: /k/, /w/", "hasContext": false, "subtopics": [{"id": "155", "name": "Unit 7: In the kitchen (Âm /k/)"}, {"id": "156", "name": "Unit 8: In the village (Âm /w/)"}]},
            {"id": "4", "name": "Vocabulary and Communication", "hasContext": false, "subtopics": [{"id": "149", "name": "Unit 1: At my birthday party (Âm /p/)"}, {"id": "150", "name": "Unit 2: In the backyard (Âm /r/)"}, {"id": "151", "name": "Unit 3: At the seaside (Âm /s/)"}, {"id": "152", "name": "Unit 4: In the countryside (Âm /t/)"}, {"id": "153", "name": "Unit 5: In the classroom (Âm /u/)"}, {"id": "154", "name": "Unit 6: On the farm (Âm /v/)"}, {"id": "155", "name": "Unit 7: In the kitchen (Âm /k/)"}, {"id": "156", "name": "Unit 8: In the village (Âm /w/)"}]}
        ],
        "difficulties": ["KNOWLEDGE"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK"]
    },
    "matrix": [
        [["4:0.5", "2:0.5"]],
        [["4:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]],
        [["2:0.5", "2:0.5"]]
    ]
}'::jsonb);

-- Template 28: Cuối kì 1 Tiếng Anh Lớp 3
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Anh 3 - Kết nối tri thức', '3', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta3_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Anh 3 - Kết nối tri thức", "grade": "3", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Greetings and Friends", "hasContext": false, "subtopics": [{"id": "165", "name": "Unit 1: Hello"}, {"id": "166", "name": "Unit 2: Our names"}, {"id": "167", "name": "Unit 3: Our friends"}]},
            {"id": "2", "name": "Body and Hobbies", "hasContext": false, "subtopics": [{"id": "168", "name": "Unit 4: Our bodies"}, {"id": "169", "name": "Unit 5: My hobbies"}]},
            {"id": "3", "name": "School Life", "hasContext": false, "subtopics": [{"id": "170", "name": "Unit 6: Our school"}, {"id": "171", "name": "Unit 7: Classroom instructions"}, {"id": "172", "name": "Unit 8: School things"}]},
            {"id": "4", "name": "Reading Comprehension", "hasContext": true, "subtopics": [{"id": "173", "name": "Unit 9: Colours"}, {"id": "174", "name": "Unit 10: Breaktime activities"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]],
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]],
        [["1:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:0.5"]],
        [["1:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:0.5"]]
    ]
}'::jsonb);

-- Template 29: Cuối kì 1 Tiếng Anh Lớp 4
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Anh 4 - Kết nối tri thức', '4', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta4_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Anh 4 - Kết nối tri thức", "grade": "4", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "Friends, Time, and Week", "hasContext": false, "subtopics": [{"id": "185", "name": "Unit 1: My friends"}, {"id": "186", "name": "Unit 2: Time and daily routines"}, {"id": "187", "name": "Unit 3: My week"}]},
            {"id": "2", "name": "Birthday and Abilities", "hasContext": false, "subtopics": [{"id": "188", "name": "Unit 4: My birthday party"}, {"id": "189", "name": "Unit 5: Things we can do"}]},
            {"id": "3", "name": "School Facilities and Timetables", "hasContext": false, "subtopics": [{"id": "190", "name": "Unit 6: Our school facilities"}, {"id": "191", "name": "Unit 7: Our timetables"}, {"id": "192", "name": "Unit 8: My favourite subjects"}]},
            {"id": "4", "name": "Reading: Sports and Holidays", "hasContext": true, "subtopics": [{"id": "193", "name": "Unit 9: Our sports day"}, {"id": "194", "name": "Unit 10: Our summer holidays"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING"]
    },
    "matrix": [
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]],
        [["2:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:1.0"]],
        [["1:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:0.5"]],
        [["1:0.5", "0:0", "1:0.5"], ["0:0", "1:0.5", "1:0.5"]]
    ]
}'::jsonb);

-- Template 30: Cuối kì 1 Tiếng Anh Lớp 5
INSERT INTO assignment_matrix_templates (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(gen_random_uuid(), NULL, 'Ma trận đề thi cuối kì 1 Tiếng Anh 5 - Kết nối tri thức', '5', 'TA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
'{
    "metadata": {"id": "tpl_ta5_ck1", "name": "Ma trận đề thi cuối kì 1 Tiếng Anh 5 - Kết nối tri thức", "grade": "5", "subject": "TA", "createdAt": "2024-01-01T00:00:00.000Z"},
    "dimensions": {
        "topics": [
            {"id": "1", "name": "About Me and Home", "hasContext": false, "subtopics": [{"id": "205", "name": "Unit 1: All about me"}, {"id": "206", "name": "Unit 2: Our homes"}]},
            {"id": "2", "name": "Friends and Activities", "hasContext": false, "subtopics": [{"id": "207", "name": "Unit 3: My foreign friends"}, {"id": "208", "name": "Unit 4: Our free-time activities"}]},
            {"id": "3", "name": "School Life", "hasContext": false, "subtopics": [{"id": "210", "name": "Unit 6: Our school rooms"}, {"id": "211", "name": "Unit 7: Our favourite school activities"}, {"id": "212", "name": "Unit 8: In our classroom"}]},
            {"id": "4", "name": "Reading: Jobs and Trips", "hasContext": true, "subtopics": [{"id": "209", "name": "Unit 5: My future job"}, {"id": "213", "name": "Unit 9: Our outdoor activities"}, {"id": "214", "name": "Unit 10: Our school trip"}]}
        ],
        "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
        "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING", "OPEN_ENDED"]
    },
    "matrix": [
        [["1:0.5", "0:0", "2:0.5", "0:0"], ["1:0.5", "1:0.5", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "0:0", "2:0.5", "0:0"], ["1:0.5", "1:0.5", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "0:0", "1:0.5", "0:0"], ["1:0.5", "0:0", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]],
        [["1:0.5", "0:0", "1:0.5", "0:0"], ["1:0.5", "0:0", "0:0", "0:0"], ["0:0", "0:0", "0:0", "1:0.5"]]
    ]
}'::jsonb);
