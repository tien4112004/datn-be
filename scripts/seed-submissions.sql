-- =====================================================
-- Seed Submissions for AssignmentPost: d89c6da7-dc46-45f3-9c18-20c1153d5b2f
-- Prerequisites: Class, students (enrolled), and assignment post must already exist
--
-- Variants covered:
--   1. Auto-graded, good score      (student 1)
--   2. Auto-graded, average score   (student 2)
--   3. Re-do: first attempt         (student 3, attempt 1)
--   4. Re-do: improved re-take      (student 3, attempt 2)
--   5. Teacher-graded with feedback (student 4)
--   6. Submitted, not yet graded    (student 5)
-- =====================================================

-- Verify before running:
-- SELECT p.id AS post_id, p.class_id FROM posts p WHERE p.assignment_id = 'd89c6da7-dc46-45f3-9c18-20c1153d5b2f';
-- SELECT ce.student_id FROM class_enrollments ce JOIN posts p ON ce.class_id = p.class_id WHERE p.assignment_id = 'd89c6da7-dc46-45f3-9c18-20c1153d5b2f' AND ce.status = 'ACTIVE';

WITH target_post AS (
    SELECT p.id AS post_id, p.class_id, c.owner_id AS teacher_id
    FROM posts p
    JOIN classes c ON p.class_id = c.id
    WHERE p.assignment_id = 'd89c6da7-dc46-45f3-9c18-20c1153d5b2f'
    LIMIT 1
),
enrolled AS (
    SELECT ce.student_id, ROW_NUMBER() OVER (ORDER BY ce.created_at) AS rn
    FROM class_enrollments ce
    JOIN target_post tp ON ce.class_id = tp.class_id
    WHERE ce.status = 'ACTIVE'
)
INSERT INTO submissions (id, student_id, post_id, assignment_id, questions, status, score, max_score, submitted_at, graded_by, graded_at, feedback, created_at, updated_at)

-- -------------------------------------------------------
-- Student 1: Good student, auto-graded — 39/40
--   Q1 MC:           correct (10/10)
--   Q2 Matching:     all correct (10/10)
--   Q3 Open-ended:   "3" → near-perfect (9/10)
--   Q4 Fill-in-blank: both correct (10/10)
-- -------------------------------------------------------
SELECT
    gen_random_uuid(),
    e.student_id,
    tp.post_id,
    'd89c6da7-dc46-45f3-9c18-20c1153d5b2f',
    $q1$[
        {
            "id": "mm45kixp-kgil2hvtc",
            "answer": { "type": "MULTIPLE_CHOICE", "id": "mm45kixp-a8q5abn9i" },
            "point": 10.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45kuqs-jhem3rwfw",
            "answer": { "type": "MATCHING", "matchedPairs": {
                "mm45kuqs-yohd9kgbv": "mm45kuqs-yohd9kgbv",
                "mm45kuqs-2qrffdyjl": "mm45kuqs-2qrffdyjl"
            }},
            "point": 10.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45l9ht-4ktaq2mcb",
            "answer": { "type": "OPEN_ENDED", "response": "3", "responseUrl": null },
            "point": 9.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45lgry-4jxbdsw83",
            "answer": { "type": "FILL_IN_BLANK", "blankAnswers": {
                "mm45lxo4-0x10icnzf": "2",
                "mm45lxo4-tadtc710b": "3"
            }},
            "point": 10.0, "feedback": null, "isAutoGraded": true
        }
    ]$q1$::jsonb,
    'graded', 39.0, 40,
    NOW() - INTERVAL '2 hours',
    NULL, NULL, NULL,
    NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'
FROM enrolled e, target_post tp WHERE e.rn = 1

UNION ALL

-- -------------------------------------------------------
-- Student 2: Average student, auto-graded — 20/40
--   Q1 MC:           correct (10/10)
--   Q2 Matching:     both swapped (0/10)
--   Q3 Open-ended:   partial answer (5/10)
--   Q4 Fill-in-blank: 1 right, 1 wrong (5/10)
-- -------------------------------------------------------
SELECT
    gen_random_uuid(),
    e.student_id,
    tp.post_id,
    'd89c6da7-dc46-45f3-9c18-20c1153d5b2f',
    $q2$[
        {
            "id": "mm45kixp-kgil2hvtc",
            "answer": { "type": "MULTIPLE_CHOICE", "id": "mm45kixp-a8q5abn9i" },
            "point": 10.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45kuqs-jhem3rwfw",
            "answer": { "type": "MATCHING", "matchedPairs": {
                "mm45kuqs-yohd9kgbv": "mm45kuqs-2qrffdyjl",
                "mm45kuqs-2qrffdyjl": "mm45kuqs-yohd9kgbv"
            }},
            "point": 0.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45l9ht-4ktaq2mcb",
            "answer": { "type": "OPEN_ENDED", "response": "bằng 3", "responseUrl": null },
            "point": 5.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45lgry-4jxbdsw83",
            "answer": { "type": "FILL_IN_BLANK", "blankAnswers": {
                "mm45lxo4-0x10icnzf": "2",
                "mm45lxo4-tadtc710b": "5"
            }},
            "point": 5.0, "feedback": null, "isAutoGraded": true
        }
    ]$q2$::jsonb,
    'graded', 20.0, 40,
    NOW() - INTERVAL '1 hour',
    NULL, NULL, NULL,
    NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'
FROM enrolled e, target_post tp WHERE e.rn = 2

UNION ALL

-- -------------------------------------------------------
-- Student 3, Attempt 1: Re-do — first attempt, bad score — 10/40
--   Q1 MC:           wrong "3" (0/10)
--   Q2 Matching:     both swapped (0/10)
--   Q3 Open-ended:   blank (0/10)
--   Q4 Fill-in-blank: both correct (10/10)
-- -------------------------------------------------------
SELECT
    gen_random_uuid(),
    e.student_id,
    tp.post_id,
    'd89c6da7-dc46-45f3-9c18-20c1153d5b2f',
    $q3a$[
        {
            "id": "mm45kixp-kgil2hvtc",
            "answer": { "type": "MULTIPLE_CHOICE", "id": "mm45kixp-m2jyyszd7" },
            "point": 0.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45kuqs-jhem3rwfw",
            "answer": { "type": "MATCHING", "matchedPairs": {
                "mm45kuqs-yohd9kgbv": "mm45kuqs-2qrffdyjl",
                "mm45kuqs-2qrffdyjl": "mm45kuqs-yohd9kgbv"
            }},
            "point": 0.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45l9ht-4ktaq2mcb",
            "answer": { "type": "OPEN_ENDED", "response": "", "responseUrl": null },
            "point": 0.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45lgry-4jxbdsw83",
            "answer": { "type": "FILL_IN_BLANK", "blankAnswers": {
                "mm45lxo4-0x10icnzf": "2",
                "mm45lxo4-tadtc710b": "3"
            }},
            "point": 10.0, "feedback": null, "isAutoGraded": true
        }
    ]$q3a$::jsonb,
    'graded', 10.0, 40,
    NOW() - INTERVAL '3 hours',
    NULL, NULL, NULL,
    NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'
FROM enrolled e, target_post tp WHERE e.rn = 3

UNION ALL

-- -------------------------------------------------------
-- Student 3, Attempt 2: Re-do — improved re-take — 30/40
--   Q1 MC:           correct (10/10)
--   Q2 Matching:     all correct (10/10)
--   Q3 Open-ended:   "ba" → partial (5/10)
--   Q4 Fill-in-blank: 1 right, 1 wrong (5/10)
-- -------------------------------------------------------
SELECT
    gen_random_uuid(),
    e.student_id,
    tp.post_id,
    'd89c6da7-dc46-45f3-9c18-20c1153d5b2f',
    $q3b$[
        {
            "id": "mm45kixp-kgil2hvtc",
            "answer": { "type": "MULTIPLE_CHOICE", "id": "mm45kixp-a8q5abn9i" },
            "point": 10.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45kuqs-jhem3rwfw",
            "answer": { "type": "MATCHING", "matchedPairs": {
                "mm45kuqs-yohd9kgbv": "mm45kuqs-yohd9kgbv",
                "mm45kuqs-2qrffdyjl": "mm45kuqs-2qrffdyjl"
            }},
            "point": 10.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45l9ht-4ktaq2mcb",
            "answer": { "type": "OPEN_ENDED", "response": "ba", "responseUrl": null },
            "point": 5.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45lgry-4jxbdsw83",
            "answer": { "type": "FILL_IN_BLANK", "blankAnswers": {
                "mm45lxo4-0x10icnzf": "2",
                "mm45lxo4-tadtc710b": "6"
            }},
            "point": 5.0, "feedback": null, "isAutoGraded": true
        }
    ]$q3b$::jsonb,
    'graded', 30.0, 40,
    NOW() - INTERVAL '30 minutes',
    NULL, NULL, NULL,
    NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'
FROM enrolled e, target_post tp WHERE e.rn = 3

UNION ALL

-- -------------------------------------------------------
-- Student 4: Teacher-graded with feedback — 35/40
--   MC/Matching/Fill-in-blank: auto-graded as usual
--   Open-ended: manually graded by teacher with feedback
--   graded_by = class owner, graded_at set, feedback provided
-- -------------------------------------------------------
SELECT
    gen_random_uuid(),
    e.student_id,
    tp.post_id,
    'd89c6da7-dc46-45f3-9c18-20c1153d5b2f',
    $q4$[
        {
            "id": "mm45kixp-kgil2hvtc",
            "answer": { "type": "MULTIPLE_CHOICE", "id": "mm45kixp-a8q5abn9i" },
            "point": 10.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45kuqs-jhem3rwfw",
            "answer": { "type": "MATCHING", "matchedPairs": {
                "mm45kuqs-yohd9kgbv": "mm45kuqs-yohd9kgbv",
                "mm45kuqs-2qrffdyjl": "mm45kuqs-2qrffdyjl"
            }},
            "point": 10.0, "feedback": null, "isAutoGraded": true
        },
        {
            "id": "mm45l9ht-4ktaq2mcb",
            "answer": { "type": "OPEN_ENDED", "response": "Kết quả là 3", "responseUrl": null },
            "point": 8.0,
            "feedback": "Đúng rồi, nhưng cần trình bày rõ ràng hơn.",
            "isAutoGraded": false
        },
        {
            "id": "mm45lgry-4jxbdsw83",
            "answer": { "type": "FILL_IN_BLANK", "blankAnswers": {
                "mm45lxo4-0x10icnzf": "2",
                "mm45lxo4-tadtc710b": "3"
            }},
            "point": 10.0, "feedback": null, "isAutoGraded": true
        }
    ]$q4$::jsonb,
    'graded', 38.0, 40,
    NOW() - INTERVAL '4 hours',
    tp.teacher_id,
    NOW() - INTERVAL '1 hour',
    'Bài làm tốt. Câu tự luận cần trình bày chi tiết hơn.',
    NOW() - INTERVAL '4 hours', NOW() - INTERVAL '1 hour'
FROM enrolled e, target_post tp WHERE e.rn = 4

UNION ALL

-- -------------------------------------------------------
-- Student 5: Submitted but NOT yet graded
--   All answers present, but no scores assigned
--   status = 'submitted', score = NULL
-- -------------------------------------------------------
SELECT
    gen_random_uuid(),
    e.student_id,
    tp.post_id,
    'd89c6da7-dc46-45f3-9c18-20c1153d5b2f',
    $q5$[
        {
            "id": "mm45kixp-kgil2hvtc",
            "answer": { "type": "MULTIPLE_CHOICE", "id": "mm45kixp-pz6vh30re" },
            "point": null, "feedback": null, "isAutoGraded": false
        },
        {
            "id": "mm45kuqs-jhem3rwfw",
            "answer": { "type": "MATCHING", "matchedPairs": {
                "mm45kuqs-yohd9kgbv": "mm45kuqs-yohd9kgbv",
                "mm45kuqs-2qrffdyjl": "mm45kuqs-yohd9kgbv"
            }},
            "point": null, "feedback": null, "isAutoGraded": false
        },
        {
            "id": "mm45l9ht-4ktaq2mcb",
            "answer": { "type": "OPEN_ENDED", "response": "câu trả lời là 3", "responseUrl": null },
            "point": null, "feedback": null, "isAutoGraded": false
        },
        {
            "id": "mm45lgry-4jxbdsw83",
            "answer": { "type": "FILL_IN_BLANK", "blankAnswers": {
                "mm45lxo4-0x10icnzf": "2",
                "mm45lxo4-tadtc710b": "3"
            }},
            "point": null, "feedback": null, "isAutoGraded": false
        }
    ]$q5$::jsonb,
    'submitted', NULL, 40,
    NOW() - INTERVAL '10 minutes',
    NULL, NULL, NULL,
    NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '10 minutes'
FROM enrolled e, target_post tp WHERE e.rn = 5
;
