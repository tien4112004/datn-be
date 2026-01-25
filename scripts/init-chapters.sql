-- =====================================================
-- Chapter Seed Data - Kết nối tri thức và cuộc sống
-- Global sort_order: T (1-5) -> TV (1-5) -> TA (1-5)
-- =====================================================

-- Clean existing data (optional - comment out if you want to preserve existing)
-- DELETE FROM chapters;

INSERT INTO chapters (id, name, grade, subject, sort_order) VALUES

-- =====================================================
-- TOÁN LỚP 1 (10 Chủ đề) - sort_order: 1-10
-- =====================================================

(gen_random_uuid(), 'Chủ đề 1: Các số 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10', '1', 'T', 1),
(gen_random_uuid(), 'Chủ đề 2: Hình phẳng', '1', 'T', 2),
(gen_random_uuid(), 'Chủ đề 3: Phép cộng, phép trừ trong phạm vi 10', '1', 'T', 3),
(gen_random_uuid(), 'Chủ đề 4: Hình khối', '1', 'T', 4),
(gen_random_uuid(), 'Chủ đề 5: Ôn tập học kì I', '1', 'T', 5),
(gen_random_uuid(), 'Chủ đề 6: Các số trong phạm vi 100', '1', 'T', 6),
(gen_random_uuid(), 'Chủ đề 7: Độ dài', '1', 'T', 7),
(gen_random_uuid(), 'Chủ đề 8: Phép cộng, phép trừ trong phạm vi 100', '1', 'T', 8),
(gen_random_uuid(), 'Chủ đề 9: Thời gian', '1', 'T', 9),
(gen_random_uuid(), 'Chủ đề 10: Ôn tập cuối năm', '1', 'T', 10),

-- =====================================================
-- TOÁN LỚP 2 (14 Chủ đề) - sort_order: 11-24
-- =====================================================

(gen_random_uuid(), 'Chủ đề 1: Ôn tập và bổ sung', '2', 'T', 11),
(gen_random_uuid(), 'Chủ đề 2: Phép cộng, phép trừ trong phạm vi 20', '2', 'T', 12),
(gen_random_uuid(), 'Chủ đề 3: Làm quen với khối lượng và dung tích', '2', 'T', 13),
(gen_random_uuid(), 'Chủ đề 4: Phép cộng, phép trừ (có nhớ) trong phạm vi 100', '2', 'T', 14),
(gen_random_uuid(), 'Chủ đề 5: Hình phẳng', '2', 'T', 15),
(gen_random_uuid(), 'Chủ đề 6: Ngày - tháng, giờ - phút', '2', 'T', 16),
(gen_random_uuid(), 'Chủ đề 7: Ôn tập học kì I', '2', 'T', 17),
(gen_random_uuid(), 'Chủ đề 8: Phép nhân, phép chia', '2', 'T', 18),
(gen_random_uuid(), 'Chủ đề 9: Hình khối', '2', 'T', 19),
(gen_random_uuid(), 'Chủ đề 10: Các số trong phạm vi 1000', '2', 'T', 20),
(gen_random_uuid(), 'Chủ đề 11: Độ dài. Tiền Việt Nam', '2', 'T', 21),
(gen_random_uuid(), 'Chủ đề 12: Phép cộng, phép trừ trong phạm vi 1000', '2', 'T', 22),
(gen_random_uuid(), 'Chủ đề 13: Thống kê. Xác suất', '2', 'T', 23),
(gen_random_uuid(), 'Chủ đề 14: Ôn tập cuối năm', '2', 'T', 24),

-- =====================================================
-- TOÁN LỚP 3 (16 Chủ đề) - sort_order: 25-40
-- =====================================================

(gen_random_uuid(), 'Chủ đề 1: Ôn tập và bổ sung', '3', 'T', 25),
(gen_random_uuid(), 'Chủ đề 2: Bảng nhân, bảng chia', '3', 'T', 26),
(gen_random_uuid(), 'Chủ đề 3: Làm quen với hình phẳng, hình khối', '3', 'T', 27),
(gen_random_uuid(), 'Chủ đề 4: Nhân, chia số có hai chữ số, ba chữ số với số có một chữ số', '3', 'T', 28),
(gen_random_uuid(), 'Chủ đề 5: Một số đơn vị đo độ dài, khối lượng, dung tích, nhiệt độ', '3', 'T', 29),
(gen_random_uuid(), 'Chủ đề 6: Phép nhân, phép chia trong phạm vi 1000', '3', 'T', 30),
(gen_random_uuid(), 'Chủ đề 7: Ôn tập học kì I', '3', 'T', 31),
(gen_random_uuid(), 'Chủ đề 8: Các số trong phạm vi 10 000', '3', 'T', 32),
(gen_random_uuid(), 'Chủ đề 9: Chu vi, diện tích một số hình phẳng', '3', 'T', 33),
(gen_random_uuid(), 'Chủ đề 10: Cộng, trừ, nhân, chia trong phạm vi 10 000', '3', 'T', 34),
(gen_random_uuid(), 'Chủ đề 11: Các số trong phạm vi 100 000', '3', 'T', 35),
(gen_random_uuid(), 'Chủ đề 12: Cộng, trừ, nhân, chia trong phạm vi 100 000', '3', 'T', 36),
(gen_random_uuid(), 'Chủ đề 13: Xem đồng hồ, xem lịch, tiền Việt Nam', '3', 'T', 37),
(gen_random_uuid(), 'Chủ đề 14: Phương pháp giải bài toán có lời văn', '3', 'T', 38),
(gen_random_uuid(), 'Chủ đề 15: Làm quen với thống kê và xác suất', '3', 'T', 39),
(gen_random_uuid(), 'Chủ đề 16: Ôn tập cuối năm', '3', 'T', 40),

-- =====================================================
-- TOÁN LỚP 4 (14 Chủ đề) - sort_order: 41-54
-- =====================================================

(gen_random_uuid(), 'Chủ đề 1: Ôn tập và bổ sung', '4', 'T', 41),
(gen_random_uuid(), 'Chủ đề 2: Góc và đơn vị đo góc. Đơn vị đo diện tích', '4', 'T', 42),
(gen_random_uuid(), 'Chủ đề 3: Số có nhiều chữ số', '4', 'T', 43),
(gen_random_uuid(), 'Chủ đề 4: Một số đơn vị đo đại lượng', '4', 'T', 44),
(gen_random_uuid(), 'Chủ đề 5: Phép cộng và phép trừ', '4', 'T', 45),
(gen_random_uuid(), 'Chủ đề 6: Đường thẳng vuông góc, đường thẳng song song. Hình thoi', '4', 'T', 46),
(gen_random_uuid(), 'Chủ đề 7: Ôn tập học kì I', '4', 'T', 47),
(gen_random_uuid(), 'Chủ đề 8: Phép nhân và phép chia', '4', 'T', 48),
(gen_random_uuid(), 'Chủ đề 9: Làm quen với hình học trực quan', '4', 'T', 49),
(gen_random_uuid(), 'Chủ đề 10: Phân số', '4', 'T', 50),
(gen_random_uuid(), 'Chủ đề 11: Phép cộng, phép trừ phân số', '4', 'T', 51),
(gen_random_uuid(), 'Chủ đề 12: Phép nhân, phép chia phân số', '4', 'T', 52),
(gen_random_uuid(), 'Chủ đề 13: Làm quen với yếu tố thống kê và xác suất', '4', 'T', 53),
(gen_random_uuid(), 'Chủ đề 14: Ôn tập cuối năm', '4', 'T', 54),

-- =====================================================
-- TOÁN LỚP 5 (12 Chủ đề) - sort_order: 55-66
-- =====================================================

(gen_random_uuid(), 'Chủ đề 1: Ôn tập và bổ sung', '5', 'T', 55),
(gen_random_uuid(), 'Chủ đề 2: Số thập phân', '5', 'T', 56),
(gen_random_uuid(), 'Chủ đề 3: Một số đơn vị đo diện tích', '5', 'T', 57),
(gen_random_uuid(), 'Chủ đề 4: Các phép tính với số thập phân', '5', 'T', 58),
(gen_random_uuid(), 'Chủ đề 5: Một số hình phẳng. Chu vi và diện tích', '5', 'T', 59),
(gen_random_uuid(), 'Chủ đề 6: Ôn tập học kì I', '5', 'T', 60),
(gen_random_uuid(), 'Chủ đề 7: Tỉ số và các bài toán liên quan', '5', 'T', 61),
(gen_random_uuid(), 'Chủ đề 8: Thể tích', '5', 'T', 62),
(gen_random_uuid(), 'Chủ đề 9: Diện tích và thể tích của một số hình khối', '5', 'T', 63),
(gen_random_uuid(), 'Chủ đề 10: Số đo thời gian. Vận tốc, quãng đường, thời gian', '5', 'T', 64),
(gen_random_uuid(), 'Chủ đề 11: Một số yếu tố thống kê và xác suất', '5', 'T', 65),
(gen_random_uuid(), 'Chủ đề 12: Ôn tập cuối năm', '5', 'T', 66),

-- =====================================================
-- TIẾNG VIỆT LỚP 1 (35 chapters) - sort_order: 67-101
-- =====================================================

-- Học vần: Chữ cái và dấu thanh
(gen_random_uuid(), 'a, b, c, dấu huyền, dấu sắc', '1', 'TV', 67),
(gen_random_uuid(), 'o, ô, ơ, dấu hỏi, dấu nặng', '1', 'TV', 68),
(gen_random_uuid(), 'e, ê, h, i, k, l', '1', 'TV', 69),
(gen_random_uuid(), 'm, n, p, ph, d, đ', '1', 'TV', 70),
(gen_random_uuid(), 'g, gh, q, qu, r, s', '1', 'TV', 71),
(gen_random_uuid(), 't, th, u, ư, v, x', '1', 'TV', 72),
(gen_random_uuid(), 'y, tr, ch, gi, kh', '1', 'TV', 73),
(gen_random_uuid(), 'ng, ngh, nh (Luyện tập chung)', '1', 'TV', 74),
(gen_random_uuid(), 'Ôn tập và đánh giá giữa học kì I', '1', 'TV', 75),
-- Học vần: Vần và âm cuối
(gen_random_uuid(), 'ai, oi, ơi, ui, ưi', '1', 'TV', 76),
(gen_random_uuid(), 'ay, ây, eo, ao, au, âu', '1', 'TV', 77),
(gen_random_uuid(), 'iu, ưu, am, âm, em, êm', '1', 'TV', 78),
(gen_random_uuid(), 'im, um, an, ăn, ân', '1', 'TV', 79),
(gen_random_uuid(), 'en, ên, in, un, iên, yên, uôn, ươn', '1', 'TV', 80),
(gen_random_uuid(), 'ang, ăng, âng, ong, ông, ung, ưng', '1', 'TV', 81),
(gen_random_uuid(), 'anh, ênh, inh, ach, êch, ich', '1', 'TV', 82),
(gen_random_uuid(), 'at, ăt, ất, ot, ôt, ơt, et, êt, it, ut, ưt', '1', 'TV', 83),
(gen_random_uuid(), 'Ôn tập và đánh giá cuối học kì I', '1', 'TV', 84),
-- Học kì II
(gen_random_uuid(), 'uê, uy, uơ, uya', '1', 'TV', 85),
(gen_random_uuid(), 'uân, uyên, uât, uyêt', '1', 'TV', 86),
(gen_random_uuid(), 'Ôn tập', '1', 'TV', 87),
(gen_random_uuid(), 'Chủ điểm: Tôi và các bạn', '1', 'TV', 88),
(gen_random_uuid(), 'Chủ điểm: Tôi và các bạn (tiếp theo)', '1', 'TV', 89),
(gen_random_uuid(), 'Chủ điểm: Mái ấm gia đình', '1', 'TV', 90),
(gen_random_uuid(), 'Chủ điểm: Mái ấm gia đình (tiếp theo)', '1', 'TV', 91),
(gen_random_uuid(), 'Chủ điểm: Đi học vui sao', '1', 'TV', 92),
(gen_random_uuid(), 'Chủ điểm: Đi học vui sao (tiếp theo)', '1', 'TV', 93),
(gen_random_uuid(), 'Ôn tập và đánh giá giữa học kì II', '1', 'TV', 94),
(gen_random_uuid(), 'Chủ điểm: Thiên nhiên kỳ thú', '1', 'TV', 95),
(gen_random_uuid(), 'Chủ điểm: Thiên nhiên kỳ thú (tiếp theo)', '1', 'TV', 96),
(gen_random_uuid(), 'Chủ điểm: Thế giới quanh em', '1', 'TV', 97),
(gen_random_uuid(), 'Chủ điểm: Thế giới quanh em (tiếp theo)', '1', 'TV', 98),
(gen_random_uuid(), 'Chủ điểm: Việt Nam quê hương em', '1', 'TV', 99),
(gen_random_uuid(), 'Chủ điểm: Việt Nam quê hương em (tiếp theo)', '1', 'TV', 100),
(gen_random_uuid(), 'Ôn tập và đánh giá cuối năm', '1', 'TV', 101),

-- =====================================================
-- TIẾNG VIỆT LỚP 2 (8 Chủ điểm) - sort_order: 102-109
-- =====================================================

(gen_random_uuid(), 'Chủ điểm 1: Em là học sinh', '2', 'TV', 102),
(gen_random_uuid(), 'Chủ điểm 2: Đi học vui sao', '2', 'TV', 103),
(gen_random_uuid(), 'Chủ điểm 3: Mái ấm gia đình', '2', 'TV', 104),
(gen_random_uuid(), 'Chủ điểm 4: Vì cuộc sống tươi đẹp', '2', 'TV', 105),
(gen_random_uuid(), 'Chủ điểm 5: Vẻ đẹp thiên nhiên', '2', 'TV', 106),
(gen_random_uuid(), 'Chủ điểm 6: Hành tinh xanh của em', '2', 'TV', 107),
(gen_random_uuid(), 'Chủ điểm 7: Giao tiếp và kết nối', '2', 'TV', 108),
(gen_random_uuid(), 'Chủ điểm 8: Đất nước chúng mình', '2', 'TV', 109),

-- =====================================================
-- TIẾNG VIỆT LỚP 3 (11 Chủ điểm) - sort_order: 110-120
-- =====================================================

(gen_random_uuid(), 'Chủ điểm 1: Những trải nghiệm thú vị', '3', 'TV', 110),
(gen_random_uuid(), 'Chủ điểm 2: Mái trường mến yêu', '3', 'TV', 111),
(gen_random_uuid(), 'Chủ điểm 3: Cổng trường rộng mở', '3', 'TV', 112),
(gen_random_uuid(), 'Chủ điểm 4: Mái ấm gia đình', '3', 'TV', 113),
(gen_random_uuid(), 'Chủ điểm 5: Cộng đồng gắn bó', '3', 'TV', 114),
(gen_random_uuid(), 'Chủ điểm 6: Những sắc màu thiên nhiên', '3', 'TV', 115),
(gen_random_uuid(), 'Chủ điểm 7: Nghệ thuật muôn màu', '3', 'TV', 116),
(gen_random_uuid(), 'Chủ điểm 8: Quê hương tươi đẹp', '3', 'TV', 117),
(gen_random_uuid(), 'Chủ điểm 9: Những người bạn quanh ta', '3', 'TV', 118),
(gen_random_uuid(), 'Chủ điểm 10: Đất nước ngàn năm', '3', 'TV', 119),
(gen_random_uuid(), 'Chủ điểm 11: Trái Đất của chúng mình', '3', 'TV', 120),

-- =====================================================
-- TIẾNG VIỆT LỚP 4 (12 chapters) - sort_order: 121-132
-- =====================================================

(gen_random_uuid(), 'Chủ điểm 1: Mỗi người một vẻ', '4', 'TV', 121),
(gen_random_uuid(), 'Chủ điểm 2: Trải nghiệm và khám phá', '4', 'TV', 122),
(gen_random_uuid(), 'Chủ điểm 3: Niềm vui sáng tạo', '4', 'TV', 123),
(gen_random_uuid(), 'Chủ điểm 4: Chắp cánh ước mơ', '4', 'TV', 124),
(gen_random_uuid(), 'Ôn tập và đánh giá giữa học kì I', '4', 'TV', 125),
(gen_random_uuid(), 'Ôn tập và đánh giá cuối học kì I', '4', 'TV', 126),
(gen_random_uuid(), 'Chủ điểm 5: Sống để yêu thương', '4', 'TV', 127),
(gen_random_uuid(), 'Chủ điểm 6: Uống nước nhớ nguồn', '4', 'TV', 128),
(gen_random_uuid(), 'Chủ điểm 7: Thế giới quanh ta', '4', 'TV', 129),
(gen_random_uuid(), 'Chủ điểm 8: Những chủ nhân tương lai', '4', 'TV', 130),
(gen_random_uuid(), 'Ôn tập và đánh giá giữa học kì II', '4', 'TV', 131),
(gen_random_uuid(), 'Ôn tập và đánh giá cuối năm', '4', 'TV', 132),

-- =====================================================
-- TIẾNG VIỆT LỚP 5 (12 chapters) - sort_order: 133-144
-- =====================================================

(gen_random_uuid(), 'Chủ điểm 1: Thế giới tuổi thơ', '5', 'TV', 133),
(gen_random_uuid(), 'Chủ điểm 2: Thiên nhiên kì thú', '5', 'TV', 134),
(gen_random_uuid(), 'Chủ điểm 3: Trên con đường học tập', '5', 'TV', 135),
(gen_random_uuid(), 'Chủ điểm 4: Nghệ thuật muôn màu', '5', 'TV', 136),
(gen_random_uuid(), 'Ôn tập và đánh giá giữa học kì I', '5', 'TV', 137),
(gen_random_uuid(), 'Ôn tập và đánh giá cuối học kì I', '5', 'TV', 138),
(gen_random_uuid(), 'Chủ điểm 5: Vẻ đẹp cuộc sống', '5', 'TV', 139),
(gen_random_uuid(), 'Chủ điểm 6: Hương sắc trăm miền', '5', 'TV', 140),
(gen_random_uuid(), 'Chủ điểm 7: Tiếp bước cha ông', '5', 'TV', 141),
(gen_random_uuid(), 'Chủ điểm 8: Thế giới của chúng ta', '5', 'TV', 142),
(gen_random_uuid(), 'Ôn tập và đánh giá giữa học kì II', '5', 'TV', 143),
(gen_random_uuid(), 'Ôn tập và đánh giá cuối năm', '5', 'TV', 144),

-- =====================================================
-- TIẾNG ANH LỚP 1 (16 Units) - sort_order: 145-160
-- =====================================================

(gen_random_uuid(), 'Unit 1 - In the school playground (Âm /b/)', '1', 'TA', 145),
(gen_random_uuid(), 'Unit 2 - In the dining room (Âm /c/)', '1', 'TA', 146),
(gen_random_uuid(), 'Unit 3 - At the street market (Âm /a/)', '1', 'TA', 147),
(gen_random_uuid(), 'Unit 4 - In the bedroom (Âm /d/)', '1', 'TA', 148),
(gen_random_uuid(), 'Unit 5 - At the fish tank (Âm /f/)', '1', 'TA', 149),
(gen_random_uuid(), 'Unit 6 - In the classroom (Âm /g/)', '1', 'TA', 150),
(gen_random_uuid(), 'Unit 7 - In the garden (Âm /h/)', '1', 'TA', 151),
(gen_random_uuid(), 'Unit 8 - In the park (Âm /i/)', '1', 'TA', 152),
(gen_random_uuid(), 'Unit 9 - In the shop (Âm /l/)', '1', 'TA', 153),
(gen_random_uuid(), 'Unit 10 - At the zoo (Âm /m/)', '1', 'TA', 154),
(gen_random_uuid(), 'Unit 11 - At the bus stop (Âm /n/)', '1', 'TA', 155),
(gen_random_uuid(), 'Unit 12 - At the lake (Âm /o/)', '1', 'TA', 156),
(gen_random_uuid(), 'Unit 13 - In the school canteen (Âm /p/)', '1', 'TA', 157),
(gen_random_uuid(), 'Unit 14 - In the toy shop (Âm /r/)', '1', 'TA', 158),
(gen_random_uuid(), 'Unit 15 - At the football match (Âm /s/)', '1', 'TA', 159),
(gen_random_uuid(), 'Unit 16 - At home (Âm /t/)', '1', 'TA', 160),

-- =====================================================
-- TIẾNG ANH LỚP 2 (16 Units) - sort_order: 161-176
-- =====================================================

(gen_random_uuid(), 'Unit 1: At my birthday party (Âm /p/)', '2', 'TA', 161),
(gen_random_uuid(), 'Unit 2: In the backyard (Âm /r/)', '2', 'TA', 162),
(gen_random_uuid(), 'Unit 3: At the seaside (Âm /s/)', '2', 'TA', 163),
(gen_random_uuid(), 'Unit 4: In the countryside (Âm /t/)', '2', 'TA', 164),
(gen_random_uuid(), 'Unit 5: In the classroom (Âm /u/)', '2', 'TA', 165),
(gen_random_uuid(), 'Unit 6: On the farm (Âm /v/)', '2', 'TA', 166),
(gen_random_uuid(), 'Unit 7: In the kitchen (Âm /k/)', '2', 'TA', 167),
(gen_random_uuid(), 'Unit 8: In the village (Âm /w/)', '2', 'TA', 168),
(gen_random_uuid(), 'Unit 9: In the grocery store (Âm /y/)', '2', 'TA', 169),
(gen_random_uuid(), 'Unit 10: At the zoo (Âm /z/)', '2', 'TA', 170),
(gen_random_uuid(), 'Unit 11: In the playground (Âm /ch/)', '2', 'TA', 171),
(gen_random_uuid(), 'Unit 12: At the lakeside (Âm /sh/)', '2', 'TA', 172),
(gen_random_uuid(), 'Unit 13: In the school canteen (Âm /th/)', '2', 'TA', 173),
(gen_random_uuid(), 'Unit 14: In the toy shop (Âm /wh/)', '2', 'TA', 174),
(gen_random_uuid(), 'Unit 15: At the football match (Âm /th/ - voiced)', '2', 'TA', 175),
(gen_random_uuid(), 'Unit 16: At home (Ôn tập tổng hợp)', '2', 'TA', 176),

-- =====================================================
-- TIẾNG ANH LỚP 3 (20 Units) - sort_order: 177-196
-- =====================================================

(gen_random_uuid(), 'Unit 1: Hello', '3', 'TA', 177),
(gen_random_uuid(), 'Unit 2: Our names', '3', 'TA', 178),
(gen_random_uuid(), 'Unit 3: Our friends', '3', 'TA', 179),
(gen_random_uuid(), 'Unit 4: Our bodies', '3', 'TA', 180),
(gen_random_uuid(), 'Unit 5: My hobbies', '3', 'TA', 181),
(gen_random_uuid(), 'Unit 6: Our school', '3', 'TA', 182),
(gen_random_uuid(), 'Unit 7: Classroom instructions', '3', 'TA', 183),
(gen_random_uuid(), 'Unit 8: School things', '3', 'TA', 184),
(gen_random_uuid(), 'Unit 9: Colours', '3', 'TA', 185),
(gen_random_uuid(), 'Unit 10: Breaktime activities', '3', 'TA', 186),
(gen_random_uuid(), 'Unit 11: My family', '3', 'TA', 187),
(gen_random_uuid(), 'Unit 12: Jobs', '3', 'TA', 188),
(gen_random_uuid(), 'Unit 13: My house', '3', 'TA', 189),
(gen_random_uuid(), 'Unit 14: My bedroom', '3', 'TA', 190),
(gen_random_uuid(), 'Unit 15: At the dining table', '3', 'TA', 191),
(gen_random_uuid(), 'Unit 16: My pets', '3', 'TA', 192),
(gen_random_uuid(), 'Unit 17: Our toys', '3', 'TA', 193),
(gen_random_uuid(), 'Unit 18: Playing and doing', '3', 'TA', 194),
(gen_random_uuid(), 'Unit 19: Outdoor activities', '3', 'TA', 195),
(gen_random_uuid(), 'Unit 20: At the zoo', '3', 'TA', 196),

-- =====================================================
-- TIẾNG ANH LỚP 4 (20 Units) - sort_order: 197-216
-- =====================================================

(gen_random_uuid(), 'Unit 1: My friends', '4', 'TA', 197),
(gen_random_uuid(), 'Unit 2: Time and daily routines', '4', 'TA', 198),
(gen_random_uuid(), 'Unit 3: My week', '4', 'TA', 199),
(gen_random_uuid(), 'Unit 4: My birthday party', '4', 'TA', 200),
(gen_random_uuid(), 'Unit 5: Things we can do', '4', 'TA', 201),
(gen_random_uuid(), 'Unit 6: Our school facilities', '4', 'TA', 202),
(gen_random_uuid(), 'Unit 7: Our timetables', '4', 'TA', 203),
(gen_random_uuid(), 'Unit 8: My favourite subjects', '4', 'TA', 204),
(gen_random_uuid(), 'Unit 9: Our school sports day', '4', 'TA', 205),
(gen_random_uuid(), 'Unit 10: Our school trip', '4', 'TA', 206),
(gen_random_uuid(), 'Unit 11: My family members', '4', 'TA', 207),
(gen_random_uuid(), 'Unit 12: Jobs', '4', 'TA', 208),
(gen_random_uuid(), 'Unit 13: Appearance', '4', 'TA', 209),
(gen_random_uuid(), 'Unit 14: Daily activities', '4', 'TA', 210),
(gen_random_uuid(), 'Unit 15: My family''s weekends', '4', 'TA', 211),
(gen_random_uuid(), 'Unit 16: Weather', '4', 'TA', 212),
(gen_random_uuid(), 'Unit 17: In the city', '4', 'TA', 213),
(gen_random_uuid(), 'Unit 18: At the shopping mall', '4', 'TA', 214),
(gen_random_uuid(), 'Unit 19: Animal world', '4', 'TA', 215),
(gen_random_uuid(), 'Unit 20: Our summer holidays', '4', 'TA', 216),

-- =====================================================
-- TIẾNG ANH LỚP 5 (20 Units) - sort_order: 217-236
-- =====================================================

(gen_random_uuid(), 'Unit 1: All about me', '5', 'TA', 217),
(gen_random_uuid(), 'Unit 2: Our holidays', '5', 'TA', 218),
(gen_random_uuid(), 'Unit 3: My foreign friends', '5', 'TA', 219),
(gen_random_uuid(), 'Unit 4: My birthday party', '5', 'TA', 220),
(gen_random_uuid(), 'Unit 5: My favourite food and drink', '5', 'TA', 221),
(gen_random_uuid(), 'Unit 6: My school', '5', 'TA', 222),
(gen_random_uuid(), 'Unit 7: My school timetable', '5', 'TA', 223),
(gen_random_uuid(), 'Unit 8: My school subjects', '5', 'TA', 224),
(gen_random_uuid(), 'Unit 9: Our school sports day', '5', 'TA', 225),
(gen_random_uuid(), 'Unit 10: Our school trip', '5', 'TA', 226),
(gen_random_uuid(), 'Unit 11: My family life', '5', 'TA', 227),
(gen_random_uuid(), 'Unit 12: Jobs', '5', 'TA', 228),
(gen_random_uuid(), 'Unit 13: My appearance', '5', 'TA', 229),
(gen_random_uuid(), 'Unit 14: My daily activities', '5', 'TA', 230),
(gen_random_uuid(), 'Unit 15: My family''s weekends', '5', 'TA', 231),
(gen_random_uuid(), 'Unit 16: Weather', '5', 'TA', 232),
(gen_random_uuid(), 'Unit 17: In the city', '5', 'TA', 233),
(gen_random_uuid(), 'Unit 18: At the shopping mall', '5', 'TA', 234),
(gen_random_uuid(), 'Unit 19: The animal world', '5', 'TA', 235),
(gen_random_uuid(), 'Unit 20: Our summer holidays', '5', 'TA', 236);
