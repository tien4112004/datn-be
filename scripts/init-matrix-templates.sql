-- =====================================================
-- Matrix Template Data - Kết nối tri thức và cuộc sống
-- Table: exam_matrices
-- =====================================================

INSERT INTO exam_matrices (id, owner_id, name, grade, subject, created_at, updated_at, matrix_data) VALUES
(
    gen_random_uuid(),
    (SELECT id FROM teachers ORDER BY created_at LIMIT 1),
    'Ma trận đề thi giữa kì 1 Tiếng Việt 1 - Kết nối tri thức',
    '1',
    'TV',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    (
        '{
            "metadata": {
                "id": "' || gen_random_uuid() || '",
                "name": "Ma trận đề thi giữa kì 1 Tiếng Việt 1 - Kết nối tri thức",
                "grade": "1",
                "subject": "TV",
                "createdAt": "' || to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"') || '"
            },
            "dimensions": {
                "topics": [
                    {
                        "id": "1",
                        "name": "Chữ cái và Dấu thanh (Chương 67-70)",
                        "subtopics": [
                            {"id": "topic_basic_sounds", "name": "Học vần: a, b, c, o, ô, ơ, e, ê, i (kết hợp dấu thanh)"}
                        ]
                    },
                    {
                        "id": "2",
                        "name": "Phụ âm và Âm ghép (Chương 71-74)",
                        "subtopics": [
                            {"id": "topic_complex_sounds", "name": "Học vần: g, gh, q, qu, ng, ngh, tr, ch, nh"}
                        ]
                    },
                    {
                        "id": "3",
                        "name": "Viết chính tả (tập chép)",
                        "subtopics": [
                            {"id": "topic_writing_dictation", "name": "Viết chính tả: Tập chép đoạn văn ngắn"}
                        ]
                    },
                    {
                        "id": "4",
                        "name": "Bài tập Tiếng Việt",
                        "subtopics": [
                            {"id": "topic_writing_exercises", "name": "Bài tập: Điền âm, vần, quy tắc chính tả (c/k, g/gh, ng/ngh)"}
                        ]
                    }
                ],
                "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
                "questionTypes": ["MULTIPLE_CHOICE", "FILL_IN_BLANK", "MATCHING", "OPEN_ENDED"]
            },
            "matrix": [
                [
                    ["4:0.5", "0:0", "0:0", "0:0"],
                    ["1:1.0", "0:0", "0:0", "0:0"],
                    ["0:0", "0:0", "0:0", "0:0"]
                ],
                [
                    ["2:0.5", "0:0", "0:0", "0:0"],
                    ["2:1.0", "0:0", "0:0", "0:0"],
                    ["0:0", "0:0", "0:0", "0:0"]
                ],
                [
                    ["0:0", "0:0", "0:0", "0:0"],
                    ["0:0", "0:0", "0:0", "0:0"],
                    ["0:0", "0:0", "0:0", "1:2.0"]
                ],
                [
                    ["0:0", "2:1.0", "0:0", "0:0"],
                    ["0:0", "0:0", "0:0", "0:0"],
                    ["0:0", "0:0", "0:0", "0:0"]
                ]
            ]
        }'
    )::jsonb
);
