
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

('1', 'Chủ đề 1: Các số từ 0 đến 10', '1', 'T', 1),
('2', 'Chủ đề 2: Làm quen với một số hình phẳng', '1', 'T', 2),
('3', 'Chủ đề 3: Phép cộng, phép trừ trong phạm vi 10', '1', 'T', 3),
('4', 'Chủ đề 4: Làm quen với một số hình khối', '1', 'T', 4),
('5', 'Chủ đề 5: Ôn tập học kì I', '1', 'T', 5),
('6', 'Chủ đề 6: Các số đến 100', '1', 'T', 6),
('7', 'Chủ đề 7: Độ dài và đo độ dài', '1', 'T', 7),
('8', 'Chủ đề 8: Phép cộng, phép trừ (không nhớ) trong phạm vi 100', '1', 'T', 8),
('9', 'Chủ đề 9: Thời gian, giờ và lịch', '1', 'T', 9),
('10', 'Chủ đề 10: Ôn tập cuối năm', '1', 'T', 10),

-- =====================================================
-- TOÁN LỚP 2 (14 Chủ đề) - sort_order: 11-24
-- =====================================================

('11', 'Chủ đề 1: Ôn tập và bổ sung', '2', 'T', 11),
('12', 'Chủ đề 2: Phép cộng, phép trừ trong phạm vi 20', '2', 'T', 12),
('13', 'Chủ đề 3: Làm quen với khối lượng và dung tích', '2', 'T', 13),
('14', 'Chủ đề 4: Phép cộng, phép trừ (có nhớ) trong phạm vi 100', '2', 'T', 14),
('15', 'Chủ đề 5: Làm quen với hình phẳng', '2', 'T', 15),
('16', 'Chủ đề 6: Ngày - tháng, giờ - phút', '2', 'T', 16),
('17', 'Chủ đề 7: Ôn tập học kì I', '2', 'T', 17),
('18', 'Chủ đề 8: Phép nhân, phép chia', '2', 'T', 18),
('19', 'Chủ đề 9: Làm quen với hình khối', '2', 'T', 19),
('20', 'Chủ đề 10: Các số trong phạm vi 1000', '2', 'T', 20),
('21', 'Chủ đề 11: Độ dài và đơn vị đo độ dài. Tiền Việt Nam', '2', 'T', 21),
('22', 'Chủ đề 12: Phép cộng, phép trừ trong phạm vi 1000', '2', 'T', 22),
('23', 'Chủ đề 13: Làm quen với yếu tố thống kê, xác suất', '2', 'T', 23),
('24', 'Chủ đề 14: Ôn tập cuối năm', '2', 'T', 24),

-- =====================================================
-- TOÁN LỚP 3 (16 Chủ đề) - sort_order: 25-40
-- =====================================================

('25', 'Chủ đề 1: Ôn tập và bổ sung', '3', 'T', 25),
('26', 'Chủ đề 2: Bảng nhân, bảng chia', '3', 'T', 26),
('27', 'Chủ đề 3: Làm quen với hình phẳng, hình khối', '3', 'T', 27),
('28', 'Chủ đề 4: Phép nhân, phép chia trong phạm vi 100', '3', 'T', 28),
('29', 'Chủ đề 5: Một số đơn vị đo độ dài, khối lượng, dung tích, nhiệt độ', '3', 'T', 29),
('30', 'Chủ đề 6: Phép nhân, phép chia trong phạm vi 1000', '3', 'T', 30),
('31', 'Chủ đề 7: Ôn tập học kì I', '3', 'T', 31),
('32', 'Chủ đề 8: Các số đến 10 000', '3', 'T', 32),
('33', 'Chủ đề 9: Chu vi, diện tích một số hình phẳng', '3', 'T', 33),
('34', 'Chủ đề 10: Cộng, trừ, nhân, chia trong phạm vi 10 000', '3', 'T', 34),
('35', 'Chủ đề 11: Các số đến 100 000', '3', 'T', 35),
('36', 'Chủ đề 12: Cộng, trừ trong phạm vi 100 000', '3', 'T', 36),
('37', 'Chủ đề 13: Xem đồng hồ, tháng - năm. Tiền Việt Nam', '3', 'T', 37),
('38', 'Chủ đề 14: Nhân, chia trong phạm vi 100 000', '3', 'T', 38),
('39', 'Chủ đề 15: Làm quen với yếu tố thống kê và xác suất', '3', 'T', 39),
('40', 'Chủ đề 16: Ôn tập cuối năm', '3', 'T', 40),

-- =====================================================
-- TOÁN LỚP 4 (13 Chủ đề) - sort_order: 41-53
-- =====================================================

('41', 'Chủ đề 1: Ôn tập và bổ sung', '4', 'T', 41),
('42', 'Chủ đề 2: Góc và đơn vị đo góc', '4', 'T', 42),
('43', 'Chủ đề 3: Số có nhiều chữ số', '4', 'T', 43),
('44', 'Chủ đề 4: Một số đơn vị đo đại lượng', '4', 'T', 44),
('45', 'Chủ đề 5: Phép cộng và phép trừ', '4', 'T', 45),
('46', 'Chủ đề 6: Đường thẳng vuông góc, đường thẳng song song', '4', 'T', 46),
('47', 'Chủ đề 7: Ôn tập học kì I', '4', 'T', 47),
('48', 'Chủ đề 8: Phép nhân và phép chia', '4', 'T', 48),
('49', 'Chủ đề 9: Luyện tập chung', '4', 'T', 49),
('50', 'Chủ đề 10: Phân số', '4', 'T', 50),
('51', 'Chủ đề 11: Phép cộng, phép trừ phân số', '4', 'T', 51),
('52', 'Chủ đề 12: Phép nhân, phép chia phân số', '4', 'T', 52),
('53', 'Chủ đề 13: Ôn tập cuối năm', '4', 'T', 53),


-- =====================================================
-- TOÁN LỚP 5 (12 Chủ đề) - sort_order: 55-66
-- =====================================================

('55', 'Chủ đề 1: Ôn tập và bổ sung', '5', 'T', 55),
('56', 'Chủ đề 2: Số thập phân', '5', 'T', 56),
('57', 'Chủ đề 3: Một số đơn vị đo diện tích', '5', 'T', 57),
('58', 'Chủ đề 4: Các phép tính với số thập phân', '5', 'T', 58),
('59', 'Chủ đề 5: Một số hình phẳng. Chu vi và diện tích', '5', 'T', 59),
('60', 'Chủ đề 6: Ôn tập học kì I', '5', 'T', 60),
('61', 'Chủ đề 7: Tỉ số và các bài toán liên quan', '5', 'T', 61),
('62', 'Chủ đề 8: Thể tích, đơn vị đo thể tích', '5', 'T', 62),
('63', 'Chủ đề 9: Diện tích và thể tích của một số hình khối', '5', 'T', 63),
('64', 'Chủ đề 10: Số đo thời gian, vận tốc. Các bài toán liên quan đến chuyển động thẳng đều', '5', 'T', 64),
('65', 'Chủ đề 11: Một số yếu tố thống kê và xác suất', '5', 'T', 65),
('66', 'Chủ đề 12: Ôn tập cuối năm', '5', 'T', 66),

-- =====================================================
-- TIẾNG VIỆT LỚP 1 (35 chapters) - sort_order: 67-101
-- =====================================================

-- Học vần: Chữ cái và dấu thanh
('67', 'a, b, c, dấu huyền, dấu sắc', '1', 'TV', 67),
('68', 'o, ô, ơ, dấu hỏi, dấu nặng', '1', 'TV', 68),
('69', 'e, ê, h, i, k, l', '1', 'TV', 69),
('70', 'm, n, p, ph, d, đ', '1', 'TV', 70),
('71', 'g, gh, q, qu, r, s', '1', 'TV', 71),
('72', 't, th, u, ư, v, x', '1', 'TV', 72),
('73', 'y, tr, ch, gi, kh', '1', 'TV', 73),
('74', 'ng, ngh, nh', '1', 'TV', 74),
-- Học vần: Vần và âm cuối
('75', 'ai, oi, ơi, ui, ưi', '1', 'TV', 75),
('76', 'ay, ây, eo, ao, au, âu', '1', 'TV', 76),
('77', 'iu, ưu, am, âm, em, êm', '1', 'TV', 77),
('78', 'im, um, an, ăn, ân', '1', 'TV', 78),
('79', 'en, ên, in, un, iên, yên, uôn, ươn', '1', 'TV', 79),
('80', 'ang, ăng, âng, ong, ông, ung, ưng', '1', 'TV', 80),
('81', 'anh, ênh, inh, ach, êch, ich', '1', 'TV', 81),
('82', 'at, ăt, ất, ot, ôt, ơt, et, êt, it, ut, ưt', '1', 'TV', 82),
-- Học kì II
('83', 'uê, uy, uơ, uya', '1', 'TV', 83),
('84', 'uân, uyên, uât, uyêt', '1', 'TV', 84),
('85', 'Chủ điểm 1: Tôi và các bạn', '1', 'TV', 85),
('86', 'Chủ điểm 2: Mái ấm gia đình', '1', 'TV', 86),
('87', 'Chủ điểm 3: Mái trường thân yêu', '1', 'TV', 87),
('88', 'Chủ điểm 4: Điều em cần biết', '1', 'TV', 88),
('89', 'Chủ điểm 5: Bài học từ cuộc sống', '1', 'TV', 89),
('90', 'Chủ điểm 6: Thế giới trong mắt em', '1', 'TV', 90),
('91', 'Chủ điểm 7: Gia đình thân thương', '1', 'TV', 91),
('92', 'Chủ điểm 8: Đất nước và con người', '1', 'TV', 92),

-- =====================================================
-- TIẾNG VIỆT LỚP 2 (9 Chủ điểm) - sort_order: 96-104
-- =====================================================

('93', 'Chủ điểm 1: Em lớn lên từng ngày', '2', 'TV', 93),
('94', 'Chủ điểm 2: Đi học vui sao', '2', 'TV', 94),
('95', 'Chủ điểm 3: Niềm vui tuổi thơ', '2', 'TV', 95),
('96', 'Chủ điểm 4: Mái ấm gia đình', '2', 'TV', 96),
('97', 'Chủ điểm 5: Vẻ đẹp quanh em', '2', 'TV', 97),
('98', 'Chủ điểm 6: Hành tinh xanh của em', '2', 'TV', 98),
('99', 'Chủ điểm 7: Giao tiếp và kết nối', '2', 'TV', 99),
('100', 'Chủ điểm 8: Con người Việt Nam', '2', 'TV', 100),
('101', 'Chủ điểm 9: Việt Nam quê em', '2', 'TV', 101),

-- =====================================================
-- TIẾNG VIỆT LỚP 3 (8 Chủ điểm) - sort_order: 105-112
-- =====================================================

('102', 'Chủ điểm 1: Những trải nghiệm thú vị', '3', 'TV', 102),
('103', 'Chủ điểm 2: Cổng trường rộng mở', '3', 'TV', 103),
('104', 'Chủ điểm 3: Mái nhà yêu thương', '3', 'TV', 104),
('105', 'Chủ điểm 4: Cộng đồng gắn bó', '3', 'TV', 105),
('106', 'Chủ điểm 5: Những sắc màu thiên nhiên', '3', 'TV', 106),
('107', 'Chủ điểm 6: Bài học từ cuộc sống', '3', 'TV', 107),
('108', 'Chủ điểm 7: Đất nước ngàn năm', '3', 'TV', 108),
('109', 'Chủ điểm 8: Trái Đất của chúng mình', '3', 'TV', 109),

-- =====================================================
-- TIẾNG VIỆT LỚP 4 (12 chapters) - sort_order: 121-132
-- =====================================================

('110', 'Chủ điểm 1: Mỗi người một vẻ', '4', 'TV', 110),
('111', 'Chủ điểm 2: Trải nghiệm và khám phá', '4', 'TV', 111),
('112', 'Chủ điểm 3: Niềm vui sáng tạo', '4', 'TV', 112),
('113', 'Chủ điểm 4: Chắp cánh ước mơ', '4', 'TV', 113),
('114', 'Ôn tập và đánh giá giữa học kì I', '4', 'TV', 114),
('115', 'Chủ điểm 5: Sống để yêu thương', '4', 'TV', 115),
('116', 'Chủ điểm 6: Uống nước nhớ nguồn', '4', 'TV', 116),
('117', 'Chủ điểm 7: Thế giới quanh ta', '4', 'TV', 117),
('118', 'Chủ điểm 8: Những chủ nhân tương lai', '4', 'TV', 118),
('119', 'Ôn tập và đánh giá giữa học kì II', '4', 'TV', 119),
('120', 'Ôn tập và đánh giá cuối năm', '4', 'TV', 120),

-- =====================================================
-- TIẾNG VIỆT LỚP 5 (12 chapters) - sort_order: 133-144
-- =====================================================

('121', 'Chủ điểm 1: Thế giới tuổi thơ', '5', 'TV', 121),
('122', 'Chủ điểm 2: Thiên nhiên kì thú', '5', 'TV', 122),
('123', 'Chủ điểm 3: Trên con đường học tập', '5', 'TV', 123),
('124', 'Chủ điểm 4: Nghệ thuật muôn màu', '5', 'TV', 124),
('125', 'Ôn tập và đánh giá giữa học kì I', '5', 'TV', 125),
('126', 'Ôn tập và đánh giá cuối học kì I', '5', 'TV', 126),
('127', 'Chủ điểm 5: Vẻ đẹp cuộc sống', '5', 'TV', 127),
('128', 'Chủ điểm 6: Hương sắc trăm miền', '5', 'TV', 128),
('129', 'Chủ điểm 7: Tiếp bước cha ông', '5', 'TV', 129),
('130', 'Chủ điểm 8: Thế giới của chúng ta', '5', 'TV', 130),
('131', 'Ôn tập và đánh giá giữa học kì II', '5', 'TV', 131),
('132', 'Ôn tập và đánh giá cuối năm', '5', 'TV', 132),

-- =====================================================
-- TIẾNG ANH LỚP 1 (16 Units) - sort_order: 145-160
-- =====================================================

('133', 'Unit 1 - In the school playground (Âm /b/)', '1', 'TA', 133),
('134', 'Unit 2 - In the dining room (Âm /c/)', '1', 'TA', 134),
('135', 'Unit 3 - At the street market (Âm /a/)', '1', 'TA', 135),
('136', 'Unit 4 - In the bedroom (Âm /d/)', '1', 'TA', 136),
('137', 'Unit 5 - At the fish tank (Âm /f/)', '1', 'TA', 137),
('138', 'Unit 6 - In the classroom (Âm /g/)', '1', 'TA', 138),
('139', 'Unit 7 - In the garden (Âm /h/)', '1', 'TA', 139),
('140', 'Unit 8 - In the park (Âm /i/)', '1', 'TA', 140),
('141', 'Unit 9 - In the shop (Âm /l/)', '1', 'TA', 141),
('142', 'Unit 10 - At the zoo (Âm /m/)', '1', 'TA', 142),
('143', 'Unit 11 - At the bus stop (Âm /n/)', '1', 'TA', 143),
('144', 'Unit 12 - At the lake (Âm /o/)', '1', 'TA', 144),
('145', 'Unit 13 - In the school canteen (Âm /p/)', '1', 'TA', 145),
('146', 'Unit 14 - In the toy shop (Âm /r/)', '1', 'TA', 146),
('147', 'Unit 15 - At the football match (Âm /s/)', '1', 'TA', 147),
('148', 'Unit 16 - At home (Âm /t/)', '1', 'TA', 148),

-- =====================================================
-- TIẾNG ANH LỚP 2 (16 Units) - sort_order: 161-176
-- =====================================================

('149', 'Unit 1: At my birthday party (Âm /p/)', '2', 'TA', 149),
('150', 'Unit 2: In the backyard (Âm /r/)', '2', 'TA', 150),
('151', 'Unit 3: At the seaside (Âm /s/)', '2', 'TA', 151),
('152', 'Unit 4: In the countryside (Âm /t/)', '2', 'TA', 152),
('153', 'Unit 5: In the classroom (Âm /u/)', '2', 'TA', 153),
('154', 'Unit 6: On the farm (Âm /v/)', '2', 'TA', 154),
('155', 'Unit 7: In the kitchen (Âm /k/)', '2', 'TA', 155),
('156', 'Unit 8: In the village (Âm /w/)', '2', 'TA', 156),
('157', 'Unit 9: In the grocery store (Âm /y/)', '2', 'TA', 157),
('158', 'Unit 10: At the zoo (Âm /z/)', '2', 'TA', 158),
('159', 'Unit 11: In the playground (Âm /ch/)', '2', 'TA', 159),
('160', 'Unit 12: At the lakeside (Âm /sh/)', '2', 'TA', 160),
('161', 'Unit 13: In the school canteen (Âm /th/)', '2', 'TA', 161),
('162', 'Unit 14: In the toy shop (Âm /wh/)', '2', 'TA', 162),
('163', 'Unit 15: At the football match (Âm /th/ - voiced)', '2', 'TA', 163),
('164', 'Unit 16: At home (Ôn tập tổng hợp)', '2', 'TA', 164),

-- =====================================================
-- TIẾNG ANH LỚP 3 (20 Units) - sort_order: 177-196
-- =====================================================

('165', 'Unit 1: Hello', '3', 'TA', 165),
('166', 'Unit 2: Our names', '3', 'TA', 166),
('167', 'Unit 3: Our friends', '3', 'TA', 167),
('168', 'Unit 4: Our bodies', '3', 'TA', 168),
('169', 'Unit 5: My hobbies', '3', 'TA', 169),
('170', 'Unit 6: Our school', '3', 'TA', 170),
('171', 'Unit 7: Classroom instructions', '3', 'TA', 171),
('172', 'Unit 8: School things', '3', 'TA', 172),
('173', 'Unit 9: Colours', '3', 'TA', 173),
('174', 'Unit 10: Breaktime activities', '3', 'TA', 174),
('175', 'Unit 11: My family', '3', 'TA', 175),
('176', 'Unit 12: Jobs', '3', 'TA', 176),
('177', 'Unit 13: My house', '3', 'TA', 177),
('178', 'Unit 14: My bedroom', '3', 'TA', 178),
('179', 'Unit 15: At the dining table', '3', 'TA', 179),
('180', 'Unit 16: My pets', '3', 'TA', 180),
('181', 'Unit 17: Our toys', '3', 'TA', 181),
('182', 'Unit 18: Playing and doing', '3', 'TA', 182),
('183', 'Unit 19: Outdoor activities', '3', 'TA', 183),
('184', 'Unit 20: At the zoo', '3', 'TA', 184),

-- =====================================================
-- TIẾNG ANH LỚP 4 (20 Units) - sort_order: 197-216
-- =====================================================

('185', 'Unit 1: My friends', '4', 'TA', 185),
('186', 'Unit 2: Time and daily routines', '4', 'TA', 186),
('187', 'Unit 3: My week', '4', 'TA', 187),
('188', 'Unit 4: My birthday party', '4', 'TA', 188),
('189', 'Unit 5: Things we can do', '4', 'TA', 189),
('190', 'Unit 6: Our school facilities', '4', 'TA', 190),
('191', 'Unit 7: Our timetables', '4', 'TA', 191),
('192', 'Unit 8: My favourite subjects', '4', 'TA', 192),
('193', 'Unit 9: Our sports day', '4', 'TA', 193),
('194', 'Unit 10: Our summer holidays', '4', 'TA', 194),
('195', 'Unit 11: My home', '4', 'TA', 195),
('196', 'Unit 12: Jobs', '4', 'TA', 196),
('197', 'Unit 13: Appearance', '4', 'TA', 197),
('198', 'Unit 14: Daily activities', '4', 'TA', 198),
('199', 'Unit 15: My family''s weekends', '4', 'TA', 199),
('200', 'Unit 16: Weather', '4', 'TA', 200),
('201', 'Unit 17: In the city', '4', 'TA', 201),
('202', 'Unit 18: At the shopping centre', '4', 'TA', 202),
('203', 'Unit 19: The animal world', '4', 'TA', 203),
('204', 'Unit 20: At summer camp', '4', 'TA', 204),

-- =====================================================
-- TIẾNG ANH LỚP 5 (20 Units) - sort_order: 217-236
-- =====================================================

('205', 'Unit 1: All about me', '5', 'TA', 205),
('206', 'Unit 2: Our homes', '5', 'TA', 206),
('207', 'Unit 3: My foreign friends', '5', 'TA', 207),
('208', 'Unit 4: Our free-time activities', '5', 'TA', 208),
('209', 'Unit 5: My future job', '5', 'TA', 209),
('210', 'Unit 6: Our school rooms', '5', 'TA', 210),
('211', 'Unit 7: Our favourite school activities', '5', 'TA', 211),
('212', 'Unit 8: In our classroom', '5', 'TA', 212),
('213', 'Unit 9: Our outdoor activities', '5', 'TA', 213),
('214', 'Unit 10: Our school trip', '5', 'TA', 214),
('215', 'Unit 11: My family', '5', 'TA', 215),
('216', 'Unit 12: Our important days', '5', 'TA', 216),
('217', 'Unit 13: My favourite stories', '5', 'TA', 217),
('218', 'Unit 14: Aspects of life', '5', 'TA', 218),
('219', 'Unit 15: Our health', '5', 'TA', 219),
('220', 'Unit 16: Our geography', '5', 'TA', 220),
('221', 'Unit 17: Our history', '5', 'TA', 221),
('222', 'Unit 18: Our traditions', '5', 'TA', 222),
('223', 'Unit 19: Famous people', '5', 'TA', 223),
('224', 'Unit 20: Future life', '5', 'TA', 224);
