#!/usr/bin/env python3
"""
Generate seed submissions for an assignment post.

Prerequisites:
  - Class, students (enrolled), and assignment post created manually via the app
  - pip install psycopg2-binary

Usage:
  python generate_submissions.py --assignment-post-id <uuid>
  python generate_submissions.py --assignment-post-id <uuid> --dry-run
  python generate_submissions.py --assignment-post-id <uuid> --seed 42
"""

import argparse
import json
import random
import uuid
from datetime import datetime, timedelta
from pathlib import Path

import psycopg2
from psycopg2.extras import Json

# ---------------------------------------------------------------------------
# DB Connection
# ---------------------------------------------------------------------------

def load_env(env_path):
    env = {}
    with open(env_path) as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" in line:
                key, value = line.split("=", 1)
                env[key.strip()] = value.strip()
    return env


def parse_jdbc_url(jdbc_url):
    url = jdbc_url.replace("jdbc:postgresql://", "")
    host_port, db = url.split("/", 1)
    host, port = host_port.split(":")
    return host, int(port), db


def get_db_connection(args):
    env_path = Path(__file__).parent.parent / ".env"
    env = load_env(env_path) if env_path.exists() else {}

    jdbc_url = env.get("POSTGRES_DB_URL", "jdbc:postgresql://localhost:5432/datn_monolith_db")
    host, port, dbname = parse_jdbc_url(jdbc_url)

    return psycopg2.connect(
        host=args.host or host,
        port=args.port or port,
        dbname=args.dbname or dbname,
        user=args.user or env.get("POSTGRES_DB_USERNAME", "postgres"),
        password=args.password or env.get("POSTGRES_DB_PASSWORD", "postgres"),
    )

# ---------------------------------------------------------------------------
# Data fetching
# ---------------------------------------------------------------------------

def fetch_assignment_data(conn, assignment_post_id):
    with conn.cursor() as cur:
        cur.execute("""
            SELECT p.id, p.class_id, c.owner_id
            FROM posts p
            JOIN classes c ON p.class_id = c.id
            WHERE p.assignment_id = %s
            LIMIT 1
        """, (assignment_post_id,))
        row = cur.fetchone()
        if not row:
            raise ValueError(f"No post found for assignment_post_id: {assignment_post_id}")
        post_id, class_id, teacher_id = row

        cur.execute("SELECT questions FROM assignment_post WHERE id = %s", (assignment_post_id,))
        row = cur.fetchone()
        if not row:
            raise ValueError(f"AssignmentPost not found: {assignment_post_id}")

        return post_id, class_id, teacher_id, row[0]


def fetch_enrolled_students(conn, class_id):
    """Return user_profile.id for each enrolled student.

    The app sets submission.student_id = user_profile.id (not students.id),
    so we must resolve through: class_enrollments → students → user_profile.
    """
    with conn.cursor() as cur:
        cur.execute("""
            SELECT s.user_id
            FROM class_enrollments ce
            JOIN students s ON ce.student_id = s.id
            WHERE ce.class_id = %s AND ce.status = 'ACTIVE'
            ORDER BY ce.created_at
        """, (class_id,))
        return [r[0] for r in cur.fetchall()]

# ---------------------------------------------------------------------------
# Answer generators (one per question type)
# ---------------------------------------------------------------------------

def gen_mc_answer(question, skill):
    """MULTIPLE_CHOICE: pick correct with probability=skill, else random wrong."""
    options = question["data"]["options"]
    correct = [o for o in options if o.get("isCorrect")]
    wrong = [o for o in options if not o.get("isCorrect")]

    if random.random() < skill and correct:
        selected = random.choice(correct)
        point = question["point"]
    else:
        selected = random.choice(wrong) if wrong else random.choice(options)
        point = 0.0

    return {"type": "MULTIPLE_CHOICE", "id": selected["id"]}, point


def gen_matching_answer(question, skill):
    """MATCHING: for each pair, correct match with probability=skill."""
    pairs = question["data"]["pairs"]
    # Some pairs may have id=None; use index-based fallback IDs to stay consistent
    pair_ids = [p["id"] if p.get("id") else str(i) for i, p in enumerate(pairs)]
    point_per_pair = question["point"] / len(pairs)

    # Start with correct mapping, then randomly swap some
    right_ids = list(pair_ids)
    for i in range(len(right_ids)):
        if random.random() >= skill:
            j = random.randint(0, len(right_ids) - 1)
            right_ids[i], right_ids[j] = right_ids[j], right_ids[i]

    matched = {}
    total = 0.0
    for i, pid in enumerate(pair_ids):
        matched[pid] = right_ids[i]
        if pid == right_ids[i]:
            total += point_per_pair

    return {"type": "MATCHING", "matchedPairs": matched}, round(total, 2)


def _make_wrong_blank(correct, all_correct_answers):
    """Generate a plausible wrong answer for a fill-in-blank segment."""
    # Numeric: offset by ±1~5
    if correct.isdigit():
        offsets = [i for i in range(-3, 4) if i != 0 and int(correct) + i >= 0]
        return str(int(correct) + random.choice(offsets)) if offsets else "?"

    # Text: use other blanks' answers as distractors, or mutate
    others = [a for a in all_correct_answers if a != correct]
    if others:
        return random.choice(others)

    # Fallback: truncate, repeat a char, or return generic wrong
    if len(correct) > 1:
        strategies = [
            correct[:-1],                          # drop last char
            correct[0] + correct,                   # duplicate first char
            correct[::-1],                          # reverse
        ]
        return random.choice(strategies)

    return "?"


def gen_fill_in_blank_answer(question, skill):
    """FILL_IN_BLANK: for each blank, correct with probability=skill."""
    segments = question["data"]["segments"]
    blanks = [s for s in segments if s["type"] == "BLANK"]
    point_per_blank = question["point"] / len(blanks) if blanks else 0

    # Collect all correct answers across blanks for cross-distractor use
    all_correct = [b["content"] for b in blanks]

    blank_answers = {}
    total = 0.0

    for blank in blanks:
        correct = blank["content"]
        acceptable = blank.get("acceptableAnswers") or []

        if random.random() < skill:
            blank_answers[blank["id"]] = random.choice(acceptable) if acceptable else correct
            total += point_per_blank
        else:
            blank_answers[blank["id"]] = _make_wrong_blank(correct, all_correct)

    return {"type": "FILL_IN_BLANK", "blankAnswers": blank_answers}, round(total, 2)


# Vietnamese response templates for open-ended
_OE_GOOD = [
    "Kết quả là {a}",
    "{a}",
    "Đáp án là {a}",
    "Theo em, đáp án là {a}",
]
_OE_PARTIAL = [
    "Em nghĩ là {a}",
    "Bằng {a}",
    "Có thể là {a}",
    "Câu trả lời: {a}",
]
_OE_BAD = [
    "Em không biết",
    "Em chưa hiểu đề bài",
    "",
    "Không rõ",
]


def _make_wrong_oe(expected):
    """Generate a plausible wrong open-ended response."""
    if not expected:
        return random.choice(_OE_BAD)

    if expected.isdigit():
        wrong_val = int(expected) + random.choice([-2, -1, 1, 2, 3])
        return random.choice([f"Có lẽ là {wrong_val}", f"{wrong_val}", f"Em nghĩ là {wrong_val}"])

    # Text: partial quote, reversed, or just wrong
    variants = [
        expected[:len(expected)//2] + "...",    # truncated
        f"Không phải {expected}",               # negation
    ]
    return random.choice(variants + _OE_BAD)


def gen_open_ended_answer(question, skill):
    """OPEN_ENDED: generate Vietnamese response, score proportional to skill."""
    expected = question["data"].get("expectedAnswer", "")

    if skill > 0.7:
        response = random.choice(_OE_GOOD).format(a=expected)
        point = round(question["point"] * random.uniform(0.7, 1.0), 1)
    elif skill > 0.4:
        response = random.choice(_OE_PARTIAL).format(a=expected)
        point = round(question["point"] * random.uniform(0.3, 0.7), 1)
    else:
        response = _make_wrong_oe(expected)
        point = round(question["point"] * random.uniform(0.0, 0.3), 1)

    return {"type": "OPEN_ENDED", "response": response, "responseUrl": None}, point

# ---------------------------------------------------------------------------
# Submission generation
# ---------------------------------------------------------------------------

_GENERATORS = {
    "MULTIPLE_CHOICE": gen_mc_answer,
    "MATCHING": gen_matching_answer,
    "FILL_IN_BLANK": gen_fill_in_blank_answer,
    "OPEN_ENDED": gen_open_ended_answer,
}


def generate_answers(questions, skill):
    """Generate answer data for all questions at a given skill level."""
    answers = []
    total_score = 0.0

    for q in questions:
        gen = _GENERATORS.get(q["type"])
        if not gen:
            continue
        answer, point = gen(q, skill)
        answers.append({
            "id": q["id"],
            "answer": answer,
            "point": point,
            "feedback": None,
            "isAutoGraded": True,
        })
        total_score += point

    return answers, round(total_score, 2)


# Vietnamese teacher feedback pool
_TEACHER_FEEDBACKS = [
    "Bài làm tốt, cần cố gắng hơn ở phần tự luận.",
    "Em cần xem lại phần nối câu.",
    "Kết quả khá tốt. Tiếp tục phát huy!",
    "Cần cải thiện phần điền khuyết.",
    "Bài làm chưa đạt yêu cầu, cần ôn tập thêm.",
    "Em làm tốt lắm!",
    "Cần trình bày rõ ràng hơn.",
    "Bài làm đạt yêu cầu.",
]

_TEACHER_OE_FEEDBACKS = [
    "Đúng rồi, nhưng cần trình bày rõ ràng hơn.",
    "Câu trả lời chưa đầy đủ.",
    "Tốt!",
    "Cần giải thích thêm.",
    "Chính xác.",
    "Sai rồi, em xem lại nhé.",
]


def generate_all_submissions(students, questions, teacher_id, post_id, assignment_post_id, max_score):
    submissions = []
    now = datetime.now()

    for student_id in students:
        skill = max(0.0, min(1.0, random.gauss(0.6, 0.2)))
        answers, score = generate_answers(questions, skill)
        submitted_at = now - timedelta(hours=random.uniform(0.5, 48))
        variant = random.random()

        # --- Variant: submitted, not yet graded (~10%) ---
        if variant < 0.10:
            submissions.append(_make_submission(
                student_id, post_id, assignment_post_id, max_score,
                questions=[{**a, "point": None, "isAutoGraded": False} for a in answers],
                status="submitted", score=None,
                submitted_at=submitted_at,
            ))

        # --- Variant: teacher-graded with feedback (~15%) ---
        elif variant < 0.25:
            graded_at = submitted_at + timedelta(hours=random.uniform(1, 24))
            for a in answers:
                if a["answer"].get("type") == "OPEN_ENDED":
                    a["isAutoGraded"] = False
                    a["feedback"] = random.choice(_TEACHER_OE_FEEDBACKS)

            submissions.append(_make_submission(
                student_id, post_id, assignment_post_id, max_score,
                questions=answers, status="graded", score=score,
                submitted_at=submitted_at,
                graded_by=teacher_id, graded_at=graded_at,
                feedback=random.choice(_TEACHER_FEEDBACKS),
                updated_at=graded_at,
            ))

        # --- Variant: re-do — two attempts (~10%) ---
        elif variant < 0.35:
            # First attempt (worse)
            bad_skill = max(0.0, skill - random.uniform(0.2, 0.4))
            bad_answers, bad_score = generate_answers(questions, bad_skill)
            first_at = submitted_at - timedelta(hours=random.uniform(2, 24))

            submissions.append(_make_submission(
                student_id, post_id, assignment_post_id, max_score,
                questions=bad_answers, status="graded", score=bad_score,
                submitted_at=first_at,
            ))
            # Second attempt (improved)
            submissions.append(_make_submission(
                student_id, post_id, assignment_post_id, max_score,
                questions=answers, status="graded", score=score,
                submitted_at=submitted_at,
            ))

        # --- Variant: normal auto-graded (~65%) ---
        else:
            submissions.append(_make_submission(
                student_id, post_id, assignment_post_id, max_score,
                questions=answers, status="graded", score=score,
                submitted_at=submitted_at,
            ))

    return submissions


def _make_submission(student_id, post_id, assignment_post_id, max_score, *,
                     questions, status, score, submitted_at,
                     graded_by=None, graded_at=None, feedback=None, updated_at=None):
    return {
        "id": str(uuid.uuid4()),
        "student_id": student_id,
        "post_id": post_id,
        "assignment_id": assignment_post_id,
        "questions": questions,
        "status": status,
        "score": score,
        "max_score": max_score,
        "submitted_at": submitted_at,
        "graded_by": graded_by,
        "graded_at": graded_at,
        "feedback": feedback,
        "created_at": submitted_at,
        "updated_at": updated_at or submitted_at,
    }

# ---------------------------------------------------------------------------
# DB insertion
# ---------------------------------------------------------------------------

INSERT_SQL = """
    INSERT INTO submissions (
        id, student_id, post_id, assignment_id, questions,
        status, score, max_score, submitted_at,
        graded_by, graded_at, feedback,
        created_at, updated_at
    ) VALUES (
        %s, %s, %s, %s, %s,
        %s, %s, %s, %s,
        %s, %s, %s,
        %s, %s
    )
"""


def insert_submissions(conn, submissions):
    with conn.cursor() as cur:
        for s in submissions:
            cur.execute(INSERT_SQL, (
                s["id"], s["student_id"], s["post_id"], s["assignment_id"],
                Json(s["questions"]),
                s["status"], s["score"], s["max_score"], s["submitted_at"],
                s["graded_by"], s["graded_at"], s["feedback"],
                s["created_at"], s["updated_at"],
            ))
    conn.commit()

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        description="Generate seed submissions for an assignment post",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument("--assignment-post-id", required=True, help="The assignment_post.id UUID")
    parser.add_argument("--seed", type=int, default=None, help="Random seed for reproducibility")
    parser.add_argument("--dry-run", action="store_true", help="Print summary without inserting")
    # DB overrides (optional, defaults from .env)
    parser.add_argument("--host", help="DB host")
    parser.add_argument("--port", type=int, help="DB port")
    parser.add_argument("--dbname", help="DB name")
    parser.add_argument("--user", help="DB user")
    parser.add_argument("--password", help="DB password")
    args = parser.parse_args()

    if args.seed is not None:
        random.seed(args.seed)

    conn = get_db_connection(args)
    try:
        post_id, class_id, teacher_id, questions = fetch_assignment_data(conn, args.assignment_post_id)
        students = fetch_enrolled_students(conn, class_id)

        if not students:
            print("No enrolled students found in this class!")
            return

        supported_types = set(_GENERATORS.keys())
        unsupported = [q["type"] for q in questions if q["type"] not in supported_types]
        if unsupported:
            print(f"WARNING: Skipping unsupported question types: {', '.join(set(unsupported))}")

        max_score = int(sum(q.get("point", 0) for q in questions if q["type"] in supported_types))

        print(f"Assignment Post: {args.assignment_post_id}")
        print(f"Post ID:         {post_id}")
        print(f"Class ID:        {class_id}")
        print(f"Teacher ID:      {teacher_id}")
        print(f"Questions:       {len(questions)} ({', '.join(q['type'] for q in questions)})")
        print(f"Max score:       {max_score}")
        print(f"Students:        {len(students)}")
        print()

        submissions = generate_all_submissions(
            students, questions, teacher_id, post_id,
            args.assignment_post_id, max_score,
        )

        # Print summary grouped by variant
        counts = {"auto-graded": 0, "teacher-graded": 0, "submitted": 0, "re-do": 0}
        seen_students = set()
        for s in submissions:
            sid = s["student_id"]
            if s["status"] == "submitted":
                counts["submitted"] += 1
            elif s["graded_by"]:
                counts["teacher-graded"] += 1
            elif sid in seen_students:
                counts["re-do"] += 1
            else:
                counts["auto-graded"] += 1
            seen_students.add(sid)

        print(f"Generated {len(submissions)} submissions:")
        for variant, count in counts.items():
            if count:
                print(f"  {variant:20s} {count}")
        print()

        # Detail table
        print(f"{'Status':<22} {'Student':>10} {'Score':>8} {'Submitted':>20}")
        print("-" * 65)
        for s in sorted(submissions, key=lambda x: x["submitted_at"]):
            label = s["status"]
            if s["graded_by"]:
                label += " (teacher)"
            score_str = f"{s['score']}/{s['max_score']}" if s["score"] is not None else "—"
            print(f"{label:<22} {s['student_id'][:10]:>10} {score_str:>8} {str(s['submitted_at'])[:19]:>20}")

        if args.dry_run:
            print("\n[DRY RUN] No data inserted.")
        else:
            insert_submissions(conn, submissions)
            print(f"\nInserted {len(submissions)} submissions.")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
