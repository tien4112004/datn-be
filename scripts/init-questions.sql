INSERT INTO questions (id, type, difficulty, title, explanation, grade, chapter_id, subject, data, context_id, created_at, updated_at) VALUES
----  a, b, c, dấu huyền, dấu sắc   
('3002', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "b"?', 'Tiếng "ba" được cấu tạo từ âm đầu "b" và âm chính "a".', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ba", "isCorrect": true}, {"text": "ca", "isCorrect": false}, {"text": "cà", "isCorrect": false}, {"text": "cá", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3003', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "c"?', 'Tiếng "ca" được cấu tạo từ âm đầu "c" và âm chính "a".', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ca", "isCorrect": true}, {"text": "ba", "isCorrect": false}, {"text": "bà", "isCorrect": false}, {"text": "bá", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3004', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "bà" gồm những bộ phận nào cấu thành?', 'Tiếng "bà" gồm âm đầu "b", âm chính "a" và thanh huyền.', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Âm b, âm a và thanh huyền", "isCorrect": true}, {"text": "Âm b và âm a", "isCorrect": false}, {"text": "Âm c, âm a và thanh huyền", "isCorrect": false}, {"text": "Âm b, âm a và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3005', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "cá" gồm những bộ phận nào cấu thành?', 'Tiếng "cá" gồm âm đầu "c", âm chính "a" và thanh sắc.', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Âm c, âm a và thanh sắc", "isCorrect": true}, {"text": "Âm c và âm a", "isCorrect": false}, {"text": "Âm b, âm a và thanh sắc", "isCorrect": false}, {"text": "Âm c, âm a và thanh huyền", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3006', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa thanh huyền?', 'Tiếng "cà" có dấu huyền đặt trên đầu chữ a.', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cà", "isCorrect": true}, {"text": "cá", "isCorrect": false}, {"text": "ca", "isCorrect": false}, {"text": "ba", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3007', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa thanh sắc?', 'Tiếng "bá" có dấu sắc đặt trên đầu chữ a.', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bá", "isCorrect": true}, {"text": "bà", "isCorrect": false}, {"text": "ba", "isCorrect": false}, {"text": "ca", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3008', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thêm thanh huyền vào tiếng "ca", ta được tiếng nào?', 'Tiếng "ca" kết hợp với thanh huyền tạo thành tiếng "cà".', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cà", "isCorrect": true}, {"text": "cá", "isCorrect": false}, {"text": "ba", "isCorrect": false}, {"text": "bà", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3009', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thêm thanh sắc vào tiếng "ba", ta được tiếng nào?', 'Tiếng "ba" kết hợp với thanh sắc tạo thành tiếng "bá".', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bá", "isCorrect": true}, {"text": "bà", "isCorrect": false}, {"text": "ca", "isCorrect": false}, {"text": "cá", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3010', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Trong tiếng "ba", âm nào đứng trước, âm nào đứng sau?', 'Tiếng "ba" có âm đầu "b" đứng trước và âm chính "a" đứng sau.', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Âm b đứng trước, âm a đứng sau", "isCorrect": true}, {"text": "Âm a đứng trước, âm b đứng sau", "isCorrect": false}, {"text": "Chỉ có âm b", "isCorrect": false}, {"text": "Chỉ có âm a", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3011', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Trong tiếng "ca", âm đầu là âm nào?', 'Âm đầu của tiếng "ca" là chữ cái "c".', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Âm c", "isCorrect": true}, {"text": "Âm a", "isCorrect": false}, {"text": "Âm b", "isCorrect": false}, {"text": "Thanh ngang", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3012', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có cấu tạo khác với các tiếng còn lại: ba, ca, bà, cà?', 'Tiếng "ba" và "ca" không có thanh (thanh ngang), trong khi "bà" và "cà" có thanh huyền.', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ba (không có dấu thanh)", "isCorrect": true}, {"text": "bà (có thanh huyền)", "isCorrect": false}, {"text": "cà (có thanh huyền)", "isCorrect": false}, {"text": "cả (có thanh hỏi)", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3013', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Dấu huyền được đặt ở đâu trong tiếng "cà"?', 'Trong tiếng "cà", dấu huyền được đặt phía trên âm chính "a".', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Trên đầu chữ a", "isCorrect": true}, {"text": "Dưới chân chữ c", "isCorrect": false}, {"text": "Trên đầu chữ c", "isCorrect": false}, {"text": "Dưới chân chữ a", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3014', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm chính trong các tiếng "ba, ca, bà, cá" là âm nào?', 'Tất cả các tiếng này đều sử dụng âm chính là "a".', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Âm a", "isCorrect": true}, {"text": "Âm b", "isCorrect": false}, {"text": "Âm c", "isCorrect": false}, {"text": "Âm o", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3015', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào dưới đây bắt đầu bằng âm "c" và có thanh sắc?', 'Tiếng "cá" thỏa mãn cả hai điều kiện: âm đầu "c" và thanh sắc.', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cá", "isCorrect": true}, {"text": "cà", "isCorrect": false}, {"text": "bá", "isCorrect": false}, {"text": "ca", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3016', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Từ nào có thể điền vào chỗ trống: "Bé có con ..." (biết âm đã học)?', 'Trong các lựa chọn, tiếng "cá" chứa các âm và thanh đã học (c, a, thanh sắc).', 1, '67', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cá", "isCorrect": true}, {"text": "bò", "isCorrect": false}, {"text": "gà", "isCorrect": false}, {"text": "hổ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

---- o, ô, ơ, dấu hỏi, dấu nặng
('3017', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "o"?', 'Tiếng "bò" gồm âm đầu b, âm chính o và thanh huyền.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bò", "isCorrect": true}, {"text": "bô", "isCorrect": false}, {"text": "bơ", "isCorrect": false}, {"text": "ba", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3018', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "ô"?', 'Tiếng "cô" gồm âm đầu c và âm chính ô.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cô", "isCorrect": true}, {"text": "co", "isCorrect": false}, {"text": "ca", "isCorrect": false}, {"text": "cơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3019', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "ơ"?', 'Tiếng "bơ" gồm âm đầu b và âm chính ơ.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bơ", "isCorrect": true}, {"text": "ba", "isCorrect": false}, {"text": "bo", "isCorrect": false}, {"text": "bô", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3020', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng "cỏ" có chứa dấu thanh nào?', 'Tiếng "cỏ" gồm âm c, âm o và thanh hỏi đặt trên đầu âm o.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thanh hỏi", "isCorrect": true}, {"text": "thanh nặng", "isCorrect": false}, {"text": "thanh huyền", "isCorrect": false}, {"text": "thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3021', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng "cọ" có chứa dấu thanh nào?', 'Tiếng "cọ" gồm âm c, âm o và thanh nặng đặt dưới âm o.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thanh nặng", "isCorrect": true}, {"text": "thanh hỏi", "isCorrect": false}, {"text": "thanh huyền", "isCorrect": false}, {"text": "thanh ngang", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3022', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "bỏ" được cấu tạo từ những bộ phận nào?', 'Tiếng "bỏ" gồm âm đầu b, âm chính o và thanh hỏi.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm b, âm o và thanh hỏi", "isCorrect": true}, {"text": "âm b, âm o và thanh nặng", "isCorrect": false}, {"text": "âm b, âm ô và thanh hỏi", "isCorrect": false}, {"text": "âm c, âm o và thanh hỏi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3023', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thêm thanh nặng vào tiếng "cô", ta được tiếng nào?', 'Tiếng "cô" thêm thanh nặng tạo thành tiếng "cộ" (trong từ xe cộ).', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cộ", "isCorrect": true}, {"text": "cọ", "isCorrect": false}, {"text": "cợ", "isCorrect": false}, {"text": "cố", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3024', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Âm "b" ghép với âm "ơ" tạo thành tiếng nào?', 'Âm đầu b kết hợp với âm chính ơ tạo thành tiếng bơ.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bơ", "isCorrect": true}, {"text": "ba", "isCorrect": false}, {"text": "bo", "isCorrect": false}, {"text": "bô", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3025', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Dấu hỏi được đặt ở vị trí nào trong tiếng "hổ"?', 'Dấu hỏi luôn được đặt ở phía trên âm chính (âm ô).', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Trên đầu âm ô", "isCorrect": true}, {"text": "Dưới chân âm ô", "isCorrect": false}, {"text": "Trên đầu âm h", "isCorrect": false}, {"text": "Bên cạnh âm ô", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3026', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Dấu nặng được đặt ở vị trí nào trong tiếng "bộ"?', 'Dấu nặng luôn được đặt ở phía dưới âm chính (âm ô).', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Dưới chân âm ô", "isCorrect": true}, {"text": "Trên đầu âm ô", "isCorrect": false}, {"text": "Dưới chân âm b", "isCorrect": false}, {"text": "Trên đầu âm b", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3027', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào dưới đây có thanh hỏi?', 'Trong các từ trên, chỉ có tiếng "mỏ" là mang thanh hỏi.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mỏ", "isCorrect": true}, {"text": "mọ", "isCorrect": false}, {"text": "mơ", "isCorrect": false}, {"text": "mô", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3028', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có chứa cả âm "c" và âm "ơ"?', 'Tiếng "cờ" gồm âm đầu c, âm chính ơ và thanh huyền.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cờ", "isCorrect": true}, {"text": "bơ", "isCorrect": false}, {"text": "cô", "isCorrect": false}, {"text": "ca", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3029', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Trong các tiếng "ba, bo, bô, bơ", tiếng nào có chứa âm "ô"?', 'Tiếng "bô" được tạo thành từ âm b và âm ô.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bô", "isCorrect": true}, {"text": "ba", "isCorrect": false}, {"text": "bo", "isCorrect": false}, {"text": "bơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3030', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có cấu tạo gồm: âm c + âm o + thanh nặng?', 'Ghép các bộ phận c, o và thanh nặng ta được tiếng cọ.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cọ", "isCorrect": true}, {"text": "cỏ", "isCorrect": false}, {"text": "bọ", "isCorrect": false}, {"text": "cộ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3031', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Bé vẽ quả..."', 'Dựa trên các âm đã học, "cọ" là từ phù hợp nhất để hoàn thành câu.', 1, '68', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cọ", "isCorrect": true}, {"text": "cô", "isCorrect": false}, {"text": "bơ", "isCorrect": false}, {"text": "ba", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- e, ê, h, i, k, l

('3032', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "e"?', 'Tiếng "ve" gồm âm đầu v (chưa học kỹ nhưng nhận diện mặt chữ) và âm chính e. Trong các phương án, "be" là tiếng chứa e rõ rệt nhất.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "be", "isCorrect": true}, {"text": "bê", "isCorrect": false}, {"text": "ba", "isCorrect": false}, {"text": "bo", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3033', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "ê"?', 'Tiếng "bê" gồm âm đầu b và âm chính ê.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bê", "isCorrect": true}, {"text": "be", "isCorrect": false}, {"text": "ba", "isCorrect": false}, {"text": "bi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3034', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "h"?', 'Tiếng "hè" gồm âm đầu h, âm chính e và thanh huyền.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hè", "isCorrect": true}, {"text": "bè", "isCorrect": false}, {"text": "lê", "isCorrect": false}, {"text": "kẻ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3035', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "i"?', 'Tiếng "bi" gồm âm đầu b và âm chính i.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bi", "isCorrect": true}, {"text": "be", "isCorrect": false}, {"text": "ba", "isCorrect": false}, {"text": "bo", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3036', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "k"?', 'Tiếng "kệ" gồm âm đầu k, âm chính ê và thanh nặng.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "kệ", "isCorrect": true}, {"text": "hè", "isCorrect": false}, {"text": "lê", "isCorrect": false}, {"text": "bê", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3037', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "l"?', 'Tiếng "lê" gồm âm đầu l và âm chính ê.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lê", "isCorrect": true}, {"text": "bê", "isCorrect": false}, {"text": "hẹ", "isCorrect": false}, {"text": "kê", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3038', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "hè" được cấu tạo từ những bộ phận nào?', 'Tiếng "hè" gồm âm đầu h, âm chính e và dấu thanh huyền.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm h, âm e và thanh huyền", "isCorrect": true}, {"text": "âm b, âm e và thanh huyền", "isCorrect": false}, {"text": "âm h, âm ê và thanh huyền", "isCorrect": false}, {"text": "âm h, âm e và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3039', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "kệ" gồm những bộ phận nào cấu thành?', 'Tiếng "kệ" gồm âm đầu k, âm chính ê và thanh nặng đặt dưới chữ ê.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm k, âm ê và thanh nặng", "isCorrect": true}, {"text": "âm c, âm ê và thanh nặng", "isCorrect": false}, {"text": "âm k, âm e và thanh nặng", "isCorrect": false}, {"text": "âm k, âm ê và thanh hỏi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3040', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "lọ", âm nào đứng trước, âm nào đứng sau?', 'Tiếng "lọ" có âm đầu l đứng trước, âm chính o đứng sau và thanh nặng.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm l đứng trước, âm o đứng sau", "isCorrect": true}, {"text": "âm o đứng trước, âm l đứng sau", "isCorrect": false}, {"text": "âm b đứng trước, âm o đứng sau", "isCorrect": false}, {"text": "âm l đứng trước, âm a đứng sau", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3041', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thêm thanh huyền vào tiếng "be", ta được tiếng nào?', 'Tiếng "be" thêm dấu huyền tạo thành tiếng "bè".', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bè", "isCorrect": true}, {"text": "bê", "isCorrect": false}, {"text": "bé", "isCorrect": false}, {"text": "hè", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3042', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có cấu tạo gồm: âm l + âm ê + thanh sắc?', 'Ghép các bộ phận l, ê và thanh sắc ta được tiếng "lế" (thường gặp trong từ "lễ lế" hoặc tên riêng).', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lế", "isCorrect": true}, {"text": "lê", "isCorrect": false}, {"text": "kế", "isCorrect": false}, {"text": "bế", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3043', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Trong các tiếng "ba, bi, bo, bu", tiếng nào có chứa âm "i"?', 'Tiếng "bi" được tạo thành từ âm b và âm i.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bi", "isCorrect": true}, {"text": "ba", "isCorrect": false}, {"text": "bo", "isCorrect": false}, {"text": "bu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3044', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Theo quy tắc chính tả, âm "k" thường đứng trước những âm nào?', 'Âm "k" luôn đứng trước các nguyên âm i, e, ê.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "i, e, ê", "isCorrect": true}, {"text": "a, o, ô", "isCorrect": false}, {"text": "u, ư, o", "isCorrect": false}, {"text": "ơ, a, ô", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3045', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có chứa cả âm "h" và âm "i"?', 'Tiếng "hí" gồm âm đầu h, âm chính i và thanh sắc.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hí", "isCorrect": true}, {"text": "bi", "isCorrect": false}, {"text": "he", "isCorrect": false}, {"text": "hổ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3046', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Bé có hòn ..." (biết âm đã học)?', 'Trong các lựa chọn, "bi" là từ phù hợp nhất về cả nghĩa và các âm đã học.', 1, '69', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bi", "isCorrect": true}, {"text": "bơ", "isCorrect": false}, {"text": "cá", "isCorrect": false}, {"text": "le", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- m, n, p, ph, d, đ
('3047', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "m"?', 'Tiếng "mẹ" gồm âm đầu m, âm chính e và thanh nặng.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mẹ", "isCorrect": true}, {"text": "be", "isCorrect": false}, {"text": "de", "isCorrect": false}, {"text": "ne", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3048', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "n"?', 'Tiếng "nơ" gồm âm đầu n và âm chính ơ.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nơ", "isCorrect": true}, {"text": "mơ", "isCorrect": false}, {"text": "lơ", "isCorrect": false}, {"text": "hơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3049', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm "ph" xuất hiện trong tiếng nào sau đây?', 'Tiếng "phà" gồm âm đầu là âm ghép ph, âm chính a và thanh huyền.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "phà", "isCorrect": true}, {"text": "ba", "isCorrect": false}, {"text": "da", "isCorrect": false}, {"text": "ca", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3050', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng "dê" có âm đầu là âm gì?', 'Tiếng "dê" bắt đầu bằng âm đầu d.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm d", "isCorrect": true}, {"text": "âm đ", "isCorrect": false}, {"text": "âm l", "isCorrect": false}, {"text": "âm h", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3051', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng "đỏ" có âm đầu là âm gì?', 'Tiếng "đỏ" bắt đầu bằng âm đầu đ.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm đ", "isCorrect": true}, {"text": "âm d", "isCorrect": false}, {"text": "âm b", "isCorrect": false}, {"text": "âm n", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3052', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "mẹ" được cấu tạo bởi những bộ phận nào?', 'Tiếng "mẹ" gồm âm đầu m, âm chính e và thanh nặng đặt dưới chữ e.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm m, âm e và thanh nặng", "isCorrect": true}, {"text": "âm n, âm e và thanh nặng", "isCorrect": false}, {"text": "âm m, âm ê và thanh nặng", "isCorrect": false}, {"text": "âm m, âm e và thanh hỏi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3053', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay âm đầu "d" trong tiếng "da" bằng âm "đ", ta được tiếng mới là gì?', 'Thay d bằng đ trong tiếng "da" ta được tiếng "đá" (nếu thêm thanh) hoặc tiếng "đa". Lựa chọn đúng nhất ở đây là "đa".', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đa", "isCorrect": true}, {"text": "ba", "isCorrect": false}, {"text": "ca", "isCorrect": false}, {"text": "na", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3054', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Ghép âm "ph", âm "ơ" và thanh hỏi, ta được tiếng nào?', 'Âm ph + ơ + thanh hỏi tạo thành tiếng "phở".', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "phở", "isCorrect": true}, {"text": "phơ", "isCorrect": false}, {"text": "phỡ", "isCorrect": false}, {"text": "mở", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3055', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "đi", âm nào đứng trước, âm nào đứng sau?', 'Tiếng "đi" có âm đầu đ đứng trước và âm chính i đứng sau.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm đ đứng trước, âm i đứng sau", "isCorrect": true}, {"text": "âm i đứng trước, âm đ đứng sau", "isCorrect": false}, {"text": "âm d đứng trước, âm i đứng sau", "isCorrect": false}, {"text": "âm đ đứng trước, âm e đứng sau", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3056', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "p"?', 'Âm "p" thường xuất hiện trong các tiếng như "pin", "pô" hoặc đi kèm thành "ph". Ở đây chọn "pô".', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "pô", "isCorrect": true}, {"text": "bô", "isCorrect": false}, {"text": "cô", "isCorrect": false}, {"text": "mô", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3057', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tìm tiếng có âm đầu "n" và thanh sắc?', 'Tiếng "ná" gồm âm đầu n, âm chính a và thanh sắc.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ná", "isCorrect": true}, {"text": "nà", "isCorrect": false}, {"text": "má", "isCorrect": false}, {"text": "lá", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3058', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào dưới đây có cấu tạo gồm: âm đầu đ + âm chính o + thanh huyền?', 'Ghép đ + o + thanh huyền ta được tiếng "đò".', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đò", "isCorrect": true}, {"text": "đó", "isCorrect": false}, {"text": "bò", "isCorrect": false}, {"text": "cò", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3059', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm chính trong tiếng "mơ" là âm nào?', 'Tiếng "mơ" có âm đầu m và âm chính là ơ.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm ơ", "isCorrect": true}, {"text": "âm ô", "isCorrect": false}, {"text": "âm o", "isCorrect": false}, {"text": "âm m", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3060', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào thích hợp để điền vào chỗ trống: "Bé đi bộ ở trên ..." (biết âm đã học)?', 'Trong các từ, "đê" là từ chứa các âm đã học và phù hợp về nghĩa.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đê", "isCorrect": true}, {"text": "phà", "isCorrect": false}, {"text": "đò", "isCorrect": false}, {"text": "xe", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3061', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm đầu khác với các tiếng còn lại: đa, đu, đi, da?', 'Các tiếng "đa, đu, đi" đều bắt đầu bằng âm đ, riêng tiếng "da" bắt đầu bằng âm d.', 1, '70', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "da", "isCorrect": true}, {"text": "đa", "isCorrect": false}, {"text": "đi", "isCorrect": false}, {"text": "đu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- g, gh, q, qu, r, s
('3062', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "g"?', 'Tiếng "gà" gồm âm đầu g, âm chính a và thanh huyền.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "gà", "isCorrect": true}, {"text": "ghế", "isCorrect": false}, {"text": "nhà", "isCorrect": false}, {"text": "quả", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3063', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "gh"?', 'Tiếng "ghế" sử dụng âm ghép "gh" đứng trước âm "ê".', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ghế", "isCorrect": true}, {"text": "gỗ", "isCorrect": false}, {"text": "giỏ", "isCorrect": false}, {"text": "ga", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3064', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "qu"?', 'Tiếng "quả" gồm âm "qu" kết hợp với âm "a" và thanh hỏi.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "quả", "isCorrect": true}, {"text": "ca", "isCorrect": false}, {"text": "ba", "isCorrect": false}, {"text": "da", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3065', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm "r" xuất hiện trong tiếng nào sau đây?', 'Tiếng "rổ" bắt đầu bằng âm đầu r.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "rổ", "isCorrect": true}, {"text": "đỏ", "isCorrect": false}, {"text": "hổ", "isCorrect": false}, {"text": "nơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3066', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "s"?', 'Tiếng "sẻ" gồm âm đầu s, âm chính e và thanh hỏi.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sẻ", "isCorrect": true}, {"text": "le", "isCorrect": false}, {"text": "be", "isCorrect": false}, {"text": "he", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3067', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Theo quy tắc chính tả, âm "gh" đứng trước những âm nào?', 'Âm "gh" chỉ đứng trước các âm i, e, ê. Các trường hợp khác dùng "g".', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "i, e, ê", "isCorrect": true}, {"text": "a, o, ô", "isCorrect": false}, {"text": "u, ư, ơ", "isCorrect": false}, {"text": "a, ă, â", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3068', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Để viết đúng tiếng "ghi", ta dùng âm đầu là "g" hay "gh"?', 'Vì đứng trước âm "i" nên ta phải dùng "gh" theo quy tắc chính tả.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "gh", "isCorrect": true}, {"text": "g", "isCorrect": false}, {"text": "q", "isCorrect": false}, {"text": "h", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3069', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Để viết đúng tiếng "gà", ta dùng âm đầu là "g" hay "gh"?', 'Đứng trước âm "a" ta dùng đơn vị chữ "g" đơn.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "g", "isCorrect": true}, {"text": "gh", "isCorrect": false}, {"text": "k", "isCorrect": false}, {"text": "qu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3070', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "quê" được cấu tạo từ những bộ phận nào?', 'Tiếng "quê" gồm âm "qu" đứng trước và âm "ê" đứng sau.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm qu và âm ê", "isCorrect": true}, {"text": "âm q và âm u", "isCorrect": false}, {"text": "âm qu và âm e", "isCorrect": false}, {"text": "âm k và âm ê", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3071', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi ghép âm "r", âm "o" và thanh hỏi, ta được tiếng nào?', 'Các bộ phận r + o + hỏi tạo thành tiếng "rổ".', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "rổ", "isCorrect": true}, {"text": "rỗ", "isCorrect": false}, {"text": "lổ", "isCorrect": false}, {"text": "hổ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3072', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Trong tiếng "quạ", thanh nặng được đặt ở đâu?', 'Thanh nặng được đặt phía dưới âm chính "a".', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "Dưới âm a", "isCorrect": true}, {"text": "Dưới âm q", "isCorrect": false}, {"text": "Trên âm a", "isCorrect": false}, {"text": "Dưới âm u", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3073', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm đầu "s" và thanh sắc?', 'Tiếng "số" gồm âm đầu s, âm chính ô và thanh sắc.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "số", "isCorrect": true}, {"text": "rổ", "isCorrect": false}, {"text": "cá", "isCorrect": false}, {"text": "lá", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3074', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có cấu tạo khác với các tiếng còn lại: ga, gỗ, ghé, quả?', 'Tiếng "ghé" và "quả" là âm ghép (gh, qu), "ga, gỗ" là âm đơn. Tuy nhiên, xét về quy tắc g/gh, "ghé" là trường hợp đặc biệt đi với e.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ghé (dùng âm ghép gh)", "isCorrect": true}, {"text": "ga (dùng âm đơn g)", "isCorrect": false}, {"text": "gỗ (dùng âm đơn g)", "isCorrect": false}, {"text": "gà (dùng âm đơn g)", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3075', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Bé có quả ..." (biết âm đã học)?', 'Trong các lựa chọn, "lê" là quả quen thuộc và chứa âm đã học.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lê", "isCorrect": true}, {"text": "rổ", "isCorrect": false}, {"text": "ghế", "isCorrect": false}, {"text": "sẻ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3076', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "rễ", âm nào đứng trước, âm nào đứng sau?', 'Tiếng "rễ" có âm đầu r đứng trước, âm chính ê đứng sau và thanh ngã.', 1, '71', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm r đứng trước, âm ê đứng sau", "isCorrect": true}, {"text": "âm ê đứng trước, âm r đứng sau", "isCorrect": false}, {"text": "âm s đứng trước, âm ê đứng sau", "isCorrect": false}, {"text": "âm d đứng trước, âm ê đứng sau", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

----- t, th, u, ư, v, x
('3077', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "t"?', 'Tiếng "tủ" gồm âm đầu t, âm chính u và thanh hỏi.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "tủ", "isCorrect": true}, {"text": "hổ", "isCorrect": false}, {"text": "thỏ", "isCorrect": false}, {"text": "nơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3078', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "th"?', 'Tiếng "thỏ" gồm âm ghép th, âm chính o và thanh hỏi.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thỏ", "isCorrect": true}, {"text": "tô", "isCorrect": false}, {"text": "cỏ", "isCorrect": false}, {"text": "mỏ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3079', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "u"?', 'Tiếng "thu" gồm âm đầu th và âm chính u.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thu", "isCorrect": true}, {"text": "thư", "isCorrect": false}, {"text": "thê", "isCorrect": false}, {"text": "tho", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3080', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "ư"?', 'Tiếng "sư" gồm âm đầu s và âm chính ư.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sư", "isCorrect": true}, {"text": "sa", "isCorrect": false}, {"text": "se", "isCorrect": false}, {"text": "so", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3081', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm "v" xuất hiện trong tiếng nào sau đây?', 'Tiếng "vở" bắt đầu bằng âm đầu v.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vở", "isCorrect": true}, {"text": "dở", "isCorrect": false}, {"text": "nở", "isCorrect": false}, {"text": "bở", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3082', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "x"?', 'Tiếng "xe" gồm âm đầu x và âm chính e.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "xe", "isCorrect": true}, {"text": "se", "isCorrect": false}, {"text": "be", "isCorrect": false}, {"text": "le", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3083', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "thỏ" được cấu tạo từ những bộ phận nào?', 'Tiếng "thỏ" gồm âm đầu th, âm chính o và dấu thanh hỏi.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm th, âm o và thanh hỏi", "isCorrect": true}, {"text": "âm t, âm o và thanh hỏi", "isCorrect": false}, {"text": "âm h, âm o và thanh hỏi", "isCorrect": false}, {"text": "âm th, âm o và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3084', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay âm đầu "t" trong tiếng "tu" bằng âm "th", ta được tiếng mới là gì?', 'Thay t bằng th trong "tu" ta được tiếng "thu".', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thu", "isCorrect": true}, {"text": "tư", "isCorrect": false}, {"text": "nụ", "isCorrect": false}, {"text": "vụ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3085', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Ghép âm "v", âm "e" và thanh sắc, ta được tiếng nào?', 'Các bộ phận v + e + sắc tạo thành tiếng "vé".', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vé", "isCorrect": true}, {"text": "vẻ", "isCorrect": false}, {"text": "vè", "isCorrect": false}, {"text": "vẽ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3086', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "tử", âm nào đứng trước, âm nào đứng sau?', 'Tiếng "tử" có âm đầu t đứng trước và âm chính ư đứng sau cùng thanh hỏi.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm t đứng trước, âm ư đứng sau", "isCorrect": true}, {"text": "âm ư đứng trước, âm t đứng sau", "isCorrect": false}, {"text": "âm th đứng trước, âm ư đứng sau", "isCorrect": false}, {"text": "âm t đứng trước, âm u đứng sau", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3087', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có thanh nặng?', 'Tiếng "tạ" gồm âm t, âm a và thanh nặng đặt dưới chữ a.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "tạ", "isCorrect": true}, {"text": "tá", "isCorrect": false}, {"text": "tả", "isCorrect": false}, {"text": "ta", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3088', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tìm tiếng có âm đầu "x" và âm chính "ư"?', 'Tiếng "xứ" thỏa mãn cả âm đầu x và âm chính ư.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "xứ", "isCorrect": true}, {"text": "xô", "isCorrect": false}, {"text": "sư", "isCorrect": false}, {"text": "thư", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3089', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm chính trong tiếng "nụ" là âm nào?', 'Tiếng "nụ" có âm đầu n và âm chính là u.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm u", "isCorrect": true}, {"text": "âm ư", "isCorrect": false}, {"text": "âm o", "isCorrect": false}, {"text": "âm n", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3090', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào thích hợp để điền vào chỗ trống: "Bé đi ..." (biết âm đã học)?', 'Dựa trên nghĩa và các âm đã học, "xe" là từ phù hợp nhất.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "xe", "isCorrect": true}, {"text": "vở", "isCorrect": false}, {"text": "tủ", "isCorrect": false}, {"text": "thỏ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3091', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm chính khác với các tiếng còn lại: thu, nụ, thú, thư?', 'Các tiếng "thu, nụ, thú" đều có âm chính u, riêng tiếng "thư" có âm chính ư.', 1, '72', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thư", "isCorrect": true}, {"text": "thu", "isCorrect": false}, {"text": "nụ", "isCorrect": false}, {"text": "thú", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

----- y, tr, ch, gi, kh

('3092', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa chữ "y"?', 'Tiếng "y" trong từ "y tá" là một tiếng đặc biệt chỉ có một âm chính là y.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "y", "isCorrect": true}, {"text": "bi", "isCorrect": false}, {"text": "ca", "isCorrect": false}, {"text": "le", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3093', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng "tre" có âm đầu là âm gì?', 'Tiếng "tre" bắt đầu bằng âm ghép "tr".', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm tr", "isCorrect": true}, {"text": "âm ch", "isCorrect": false}, {"text": "âm t", "isCorrect": false}, {"text": "âm r", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3094', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm "ch" xuất hiện trong tiếng nào sau đây?', 'Tiếng "chợ" gồm âm đầu ch, âm chính ơ và thanh nặng.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chợ", "isCorrect": true}, {"text": "trà", "isCorrect": false}, {"text": "giỏ", "isCorrect": false}, {"text": "khế", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3095', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây bắt đầu bằng âm "gi"?', 'Tiếng "giỏ" có âm đầu là gi, âm chính là o.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "giỏ", "isCorrect": true}, {"text": "dỏ", "isCorrect": false}, {"text": "vỏ", "isCorrect": false}, {"text": "nơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3096', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm đầu của tiếng "khế" là âm gì?', 'Tiếng "khế" bắt đầu bằng âm ghép "kh".', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm kh", "isCorrect": true}, {"text": "âm k", "isCorrect": false}, {"text": "âm h", "isCorrect": false}, {"text": "âm g", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3097', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "chợ" được cấu tạo từ những bộ phận nào?', 'Tiếng "chợ" gồm âm đầu ch, âm chính ơ và dấu thanh nặng.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm ch, âm ơ và thanh nặng", "isCorrect": true}, {"text": "âm tr, âm ơ và thanh nặng", "isCorrect": false}, {"text": "âm ch, âm o và thanh nặng", "isCorrect": false}, {"text": "âm c, âm h và âm ơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3098', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi ghép âm "tr", âm "a" và thanh huyền, ta được tiếng nào?', 'Các bộ phận tr + a + huyền tạo thành tiếng "trà".', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "trà", "isCorrect": true}, {"text": "chà", "isCorrect": false}, {"text": "tạ", "isCorrect": false}, {"text": "rà", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3099', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "khỉ" gồm âm đầu "kh", âm chính "i" và thanh gì?', 'Tiếng "khỉ" sử dụng thanh hỏi đặt trên đầu âm i.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thanh hỏi", "isCorrect": true}, {"text": "thanh ngã", "isCorrect": false}, {"text": "thanh sắc", "isCorrect": false}, {"text": "thanh nặng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3100', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "giá", âm nào đứng trước, âm nào đứng sau?', 'Tiếng "giá" có âm đầu gi đứng trước và âm chính a đứng sau kèm thanh sắc.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm gi đứng trước, âm a đứng sau", "isCorrect": true}, {"text": "âm g đứng trước, âm i đứng sau", "isCorrect": false}, {"text": "âm d đứng trước, âm a đứng sau", "isCorrect": false}, {"text": "âm gi đứng trước, âm e đứng sau", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3101', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tìm tiếng có âm đầu "kh" và âm chính "o"?', 'Tiếng "kho" thỏa mãn cả âm đầu kh và âm chính o.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "kho", "isCorrect": true}, {"text": "khô", "isCorrect": false}, {"text": "cho", "isCorrect": false}, {"text": "go", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3102', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Từ nào dưới đây viết đúng chính tả?', '"Giỏ cá" dùng âm "gi". Các phương án khác sai âm đầu hoặc thiếu dấu.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "giỏ cá", "isCorrect": true}, {"text": "dỏ cá", "isCorrect": false}, {"text": "rỏ cá", "isCorrect": false}, {"text": "vỏ cá", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3103', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm chính trong tiếng "chỉ" là âm nào?', 'Tiếng "chỉ" có âm đầu ch và âm chính là i.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm i", "isCorrect": true}, {"text": "âm y", "isCorrect": false}, {"text": "âm ch", "isCorrect": false}, {"text": "âm e", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3104', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm đầu khác với các tiếng còn lại: tre, trà, trưa, chó?', 'Các tiếng "tre, trà, trưa" đều bắt đầu bằng tr, riêng "chó" bắt đầu bằng ch.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chó", "isCorrect": true}, {"text": "tre", "isCorrect": false}, {"text": "trà", "isCorrect": false}, {"text": "trưa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3105', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào thích hợp điền vào chỗ trống: "Bố đi ..."?', 'Dựa trên các âm đã học, "chợ" là từ phù hợp nhất về nghĩa.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chợ", "isCorrect": true}, {"text": "khỉ", "isCorrect": false}, {"text": "tre", "isCorrect": false}, {"text": "y", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3106', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "ý" gồm những bộ phận nào?', 'Tiếng "ý" chỉ gồm âm chính y và thanh sắc.', 1, '73', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm y và thanh sắc", "isCorrect": true}, {"text": "âm i và thanh sắc", "isCorrect": false}, {"text": "âm y và thanh huyền", "isCorrect": false}, {"text": "âm gi và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- ng, ngh, nh (Luyện tập chung)

('3107', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "ng"?', 'Tiếng "ngư" gồm âm đầu ng và âm chính ư.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ngư", "isCorrect": true}, {"text": "nghề", "isCorrect": false}, {"text": "nhà", "isCorrect": false}, {"text": "ca", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3108', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa âm "ngh"?', 'Tiếng "nghệ" sử dụng âm ghép ngh đứng trước âm ê.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nghệ", "isCorrect": true}, {"text": "ngà", "isCorrect": false}, {"text": "nho", "isCorrect": false}, {"text": "bơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3109', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Âm "nh" xuất hiện trong tiếng nào sau đây?', 'Tiếng "nhà" bắt đầu bằng âm đầu nh.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nhà", "isCorrect": true}, {"text": "gà", "isCorrect": false}, {"text": "ca", "isCorrect": false}, {"text": "ba", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3110', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Theo quy tắc chính tả, âm "ngh" đứng trước những âm nào?', 'Âm "ngh" chỉ đứng trước các âm i, e, ê.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "i, e, ê", "isCorrect": true}, {"text": "a, o, ô", "isCorrect": false}, {"text": "u, ư, ơ", "isCorrect": false}, {"text": "a, ă, â", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3111', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Để viết đúng tiếng "nghe", ta dùng âm đầu nào?', 'Vì đứng trước âm "e" nên phải dùng "ngh" theo quy tắc chính tả.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ngh", "isCorrect": true}, {"text": "ng", "isCorrect": false}, {"text": "nh", "isCorrect": false}, {"text": "h", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3112', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Để viết đúng tiếng "ngà", ta dùng âm đầu nào?', 'Đứng trước âm "a" ta dùng chữ "ng" đơn.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ng", "isCorrect": true}, {"text": "ngh", "isCorrect": false}, {"text": "nh", "isCorrect": false}, {"text": "gh", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3113', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "nhỏ" được cấu tạo từ những bộ phận nào?', 'Tiếng "nhỏ" gồm âm đầu nh, âm chính o và thanh hỏi.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm nh, âm o và thanh hỏi", "isCorrect": true}, {"text": "âm n, âm o và thanh hỏi", "isCorrect": false}, {"text": "âm nh, âm ô và thanh hỏi", "isCorrect": false}, {"text": "âm nh, âm o và thanh ngã", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3114', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm đầu "nh" và thanh sắc?', 'Tiếng "nhá" gồm âm đầu nh, âm chính a và thanh sắc.', 1, '74', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nhá", "isCorrect": true}, {"text": "nhà", "isCorrect": false}, {"text": "ngá", "isCorrect": false}, {"text": "lá", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----  ai, oi, ơi, ui, ưi
('3115', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ai"?', 'Tiếng "nai" gồm âm đầu n kết hợp với vần ai.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nai", "isCorrect": true}, {"text": "nơi", "isCorrect": false}, {"text": "nổi", "isCorrect": false}, {"text": "núi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3116', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "oi"?', 'Tiếng "voi" gồm âm đầu v kết hợp với vần oi.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "voi", "isCorrect": true}, {"text": "vải", "isCorrect": false}, {"text": "vơi", "isCorrect": false}, {"text": "vui", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3117', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ơi"?', 'Tiếng "mới" gồm âm đầu m, vần ơi và thanh sắc.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mới", "isCorrect": true}, {"text": "mái", "isCorrect": false}, {"text": "mỏi", "isCorrect": false}, {"text": "mùi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3118', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ui"?', 'Tiếng "túi" gồm âm đầu t, vần ui và thanh sắc.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "túi", "isCorrect": true}, {"text": "tái", "isCorrect": false}, {"text": "tơi", "isCorrect": false}, {"text": "tửi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3119', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ưi"?', 'Tiếng "gửi" gồm âm đầu g, vần ưi và thanh hỏi.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "gửi", "isCorrect": true}, {"text": "gái", "isCorrect": false}, {"text": "gói", "isCorrect": false}, {"text": "gùi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3120', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "vải" được cấu tạo từ những bộ phận nào?', 'Tiếng "vải" gồm âm đầu v, vần ai và dấu thanh hỏi.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm v, vần ai và thanh hỏi", "isCorrect": true}, {"text": "âm v, vần oi và thanh hỏi", "isCorrect": false}, {"text": "âm v, vần ơi và thanh hỏi", "isCorrect": false}, {"text": "âm v, vần ui và thanh hỏi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3121', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay âm đầu "n" trong tiếng "nai" bằng âm "v", ta được tiếng nào?', 'Thay n bằng v trong "nai" ta được tiếng "vai".', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vai", "isCorrect": true}, {"text": "voi", "isCorrect": false}, {"text": "vơi", "isCorrect": false}, {"text": "vui", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3122', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "ngửi" gồm âm đầu "ngh", vần "ưi" và thanh gì?', 'Tiếng "ngửi" sử dụng thanh hỏi đặt trên âm ư.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thanh hỏi", "isCorrect": true}, {"text": "thanh ngã", "isCorrect": false}, {"text": "thanh sắc", "isCorrect": false}, {"text": "thanh nặng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3123', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Các tiếng "bài, vải, mái" có chung vần gì?', 'Cả ba tiếng này đều có chung vần ai.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vần ai", "isCorrect": true}, {"text": "vần oi", "isCorrect": false}, {"text": "vần ơi", "isCorrect": false}, {"text": "vần ui", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3124', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào dưới đây có vần "ơi" và thanh hỏi?', 'Tiếng "thổi" chứa vần oi, tiếng "thởi" (trong từ lởi xởi) chứa vần ơi và thanh hỏi.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thởi", "isCorrect": true}, {"text": "thổi", "isCorrect": false}, {"text": "thái", "isCorrect": false}, {"text": "thui", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3125', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào thích hợp để điền vào chỗ trống: "Bé ... quà cho bà"?', 'Dựa trên nghĩa của câu, "gửi" là từ phù hợp nhất.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "gửi", "isCorrect": true}, {"text": "ngửi", "isCorrect": false}, {"text": "gói", "isCorrect": false}, {"text": "gùi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3126', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "núi", vần "ui" đứng ở vị trí nào?', 'Trong tiếng "núi", âm đầu n đứng trước và vần ui đứng sau.', 1, '75', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đứng sau âm n", "isCorrect": true}, {"text": "đứng trước âm n", "isCorrect": false}, {"text": "đứng ở giữa âm n", "isCorrect": false}, {"text": "đứng một mình", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

----- ay, ây, eo, ao, au, âu
('3127', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ay"?', 'Tiếng "máy" gồm âm đầu m kết hợp với vần ay và thanh sắc.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "máy", "isCorrect": true}, {"text": "mây", "isCorrect": false}, {"text": "mèo", "isCorrect": false}, {"text": "máu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3128', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ây"?', 'Tiếng "cây" gồm âm đầu c kết hợp với vần ây.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cây", "isCorrect": true}, {"text": "cay", "isCorrect": false}, {"text": "cao", "isCorrect": false}, {"text": "cau", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3129', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "eo"?', 'Tiếng "mèo" gồm âm đầu m kết hợp với vần eo và thanh huyền.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mèo", "isCorrect": true}, {"text": "mái", "isCorrect": false}, {"text": "mâu", "isCorrect": false}, {"text": "màu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3130', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ao"?', 'Tiếng "sáo" gồm âm đầu s kết hợp với vần ao và thanh sắc.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sáo", "isCorrect": true}, {"text": "sau", "isCorrect": false}, {"text": "sâu", "isCorrect": false}, {"text": "sây", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3131', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "au"?', 'Tiếng "rau" gồm âm đầu r kết hợp với vần au.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "rau", "isCorrect": true}, {"text": "rêu", "isCorrect": false}, {"text": "râu", "isCorrect": false}, {"text": "rao", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3132', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "âu"?', 'Tiếng "gấu" gồm âm đầu g kết hợp với vần âu và thanh sắc.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "gấu", "isCorrect": true}, {"text": "gái", "isCorrect": false}, {"text": "gạo", "isCorrect": false}, {"text": "gàu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3133', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "kéo" được cấu tạo từ những bộ phận nào?', 'Tiếng "kéo" gồm âm đầu k, vần eo và thanh sắc.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm k, vần eo và thanh sắc", "isCorrect": true}, {"text": "âm k, vần ao và thanh sắc", "isCorrect": false}, {"text": "âm c, vần eo và thanh sắc", "isCorrect": false}, {"text": "âm k, vần au và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3134', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "au" trong tiếng "cau" bằng vần "âu", ta được tiếng nào?', 'Thay vần au bằng âu trong "cau" ta được tiếng "câu".', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "câu", "isCorrect": true}, {"text": "cay", "isCorrect": false}, {"text": "cây", "isCorrect": false}, {"text": "cao", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3135', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có cấu tạo gồm: âm đầu ch + vần ao + thanh huyền?', 'Ghép các bộ phận ch, ao và huyền ta được tiếng "chào".', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chào", "isCorrect": true}, {"text": "cháu", "isCorrect": false}, {"text": "châu", "isCorrect": false}, {"text": "chèo", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3136', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "mèo" và "méo" khác nhau ở bộ phận nào?', 'Hai tiếng này giống nhau âm đầu và vần, chỉ khác nhau ở thanh huyền và thanh sắc.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thanh điệu", "isCorrect": true}, {"text": "âm đầu", "isCorrect": false}, {"text": "vần", "isCorrect": false}, {"text": "âm cuối", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3137', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào thích hợp điền vào chỗ trống: "Đôi ... của bé"?', 'Tiếng "tay" (vần ay) phù hợp nhất về nghĩa.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "tay", "isCorrect": true}, {"text": "tây", "isCorrect": false}, {"text": "tao", "isCorrect": false}, {"text": "tâu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3138', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "chấu", vần "âu" đứng ở vị trí nào?', 'Vần âu đứng sau âm đầu ch.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đứng sau âm ch", "isCorrect": true}, {"text": "đứng trước âm ch", "isCorrect": false}, {"text": "đứng một mình", "isCorrect": false}, {"text": "đứng ở giữa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3139', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Các tiếng "lau, cau, màu" có chung vần gì?', 'Cả ba tiếng này đều có chung vần au.', 1, '76', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vần au", "isCorrect": true}, {"text": "vần âu", "isCorrect": false}, {"text": "vần ao", "isCorrect": false}, {"text": "vần ay", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- iu, ưu, am, âm, em, êm

('3140', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "iu"?', 'Tiếng "rìu" gồm âm đầu r, vần iu và thanh huyền.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "rìu", "isCorrect": true}, {"text": "rượu", "isCorrect": false}, {"text": "rau", "isCorrect": false}, {"text": "reo", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3141', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ưu"?', 'Tiếng "lựu" gồm âm đầu l, vần ưu và thanh nặng.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lựu", "isCorrect": true}, {"text": "liu", "isCorrect": false}, {"text": "lo", "isCorrect": false}, {"text": "lưa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3142', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "am"?', 'Tiếng "cam" gồm âm đầu c kết hợp với vần am.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cam", "isCorrect": true}, {"text": "cầm", "isCorrect": false}, {"text": "cơm", "isCorrect": false}, {"text": "ca", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3143', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "âm"?', 'Tiếng "nấm" gồm âm đầu n, vần âm và thanh sắc.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nấm", "isCorrect": true}, {"text": "nam", "isCorrect": false}, {"text": "nôm", "isCorrect": false}, {"text": "nem", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3144', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "em"?', 'Tiếng "kem" gồm âm đầu k kết hợp với vần em.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "kem", "isCorrect": true}, {"text": "kêm", "isCorrect": false}, {"text": "kim", "isCorrect": false}, {"text": "cam", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3145', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "êm"?', 'Tiếng "nệm" gồm âm đầu n, vần êm và thanh nặng.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nệm", "isCorrect": true}, {"text": "nem", "isCorrect": false}, {"text": "nam", "isCorrect": false}, {"text": "nôm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3146', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Ghép âm "n", vần "em" và thanh sắc, ta được tiếng nào?', 'Các bộ phận n + em + sắc tạo thành tiếng "ném".', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ném", "isCorrect": true}, {"text": "nệm", "isCorrect": false}, {"text": "nam", "isCorrect": false}, {"text": "nêm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3147', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "đêm" được cấu tạo từ những bộ phận nào?', 'Tiếng "đêm" gồm âm đầu đ và vần êm.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm đ và vần êm", "isCorrect": true}, {"text": "âm đ và vần em", "isCorrect": false}, {"text": "âm đ và vần am", "isCorrect": false}, {"text": "âm đ và vần ôm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3148', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "am" và thanh huyền?', 'Tiếng "hàm" thỏa mãn cả vần am và thanh huyền.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hàm", "isCorrect": true}, {"text": "hầm", "isCorrect": false}, {"text": "hám", "isCorrect": false}, {"text": "ham", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3149', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm chính khác với các tiếng còn lại: nem, tem, xem, đêm?', 'Các tiếng "nem, tem, xem" đều có vần em, riêng tiếng "đêm" có vần êm.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đêm", "isCorrect": true}, {"text": "nem", "isCorrect": false}, {"text": "tem", "isCorrect": false}, {"text": "xem", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3150', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào thích hợp để điền vào chỗ trống: "Bé ăn que ..."?', 'Dựa trên nghĩa và vần đã học, "kem" là từ phù hợp nhất.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "kem", "isCorrect": true}, {"text": "nệm", "isCorrect": false}, {"text": "cam", "isCorrect": false}, {"text": "rìu", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3151', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "lựu", vần "ưu" đứng ở vị trí nào?', 'Vần ưu đứng sau âm đầu l.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đứng sau âm l", "isCorrect": true}, {"text": "đứng trước âm l", "isCorrect": false}, {"text": "đứng một mình", "isCorrect": false}, {"text": "đứng ở giữa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3152', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Các tiếng "sâm, mâm, nấm" có chung vần gì?', 'Cả ba tiếng này đều có chung vần âm.', 1, '77', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vần âm", "isCorrect": true}, {"text": "vần am", "isCorrect": false}, {"text": "vần em", "isCorrect": false}, {"text": "vần êm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- im, um, an, ăn, ân
('3153', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "im"?', 'Tiếng "chim" gồm âm đầu ch kết hợp với vần im.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chim", "isCorrect": true}, {"text": "chum", "isCorrect": false}, {"text": "chân", "isCorrect": false}, {"text": "chăn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3154', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "um"?', 'Tiếng "chum" gồm âm đầu ch kết hợp với vần um.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chum", "isCorrect": true}, {"text": "chim", "isCorrect": false}, {"text": "chan", "isCorrect": false}, {"text": "châm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3155', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "an"?', 'Tiếng "bạn" gồm âm đầu b, vần an và thanh nặng.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bạn", "isCorrect": true}, {"text": "bắn", "isCorrect": false}, {"text": "bân", "isCorrect": false}, {"text": "băm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3156', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ăn"?', 'Tiếng "khăn" gồm âm đầu kh kết hợp với vần ăn.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "khăn", "isCorrect": true}, {"text": "khán", "isCorrect": false}, {"text": "khân", "isCorrect": false}, {"text": "khâm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3157', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ân"?', 'Tiếng "sân" gồm âm đầu s kết hợp với vần ân.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sân", "isCorrect": true}, {"text": "sàn", "isCorrect": false}, {"text": "săn", "isCorrect": false}, {"text": "sim", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3158', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "phím" được cấu tạo từ những bộ phận nào?', 'Tiếng "phím" gồm âm đầu ph, vần im và thanh sắc.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm ph, vần im và thanh sắc", "isCorrect": true}, {"text": "âm p, vần im và thanh sắc", "isCorrect": false}, {"text": "âm ph, vần um và thanh sắc", "isCorrect": false}, {"text": "âm ph, vần am và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3159', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "an" trong tiếng "bàn" bằng vần "ân", ta được tiếng nào?', 'Thay vần an bằng ân trong "bàn" ta được tiếng "bần".', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bần", "isCorrect": true}, {"text": "bắn", "isCorrect": false}, {"text": "bạn", "isCorrect": false}, {"text": "bùm", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3160', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Ghép âm "m", vần "ăn" và thanh nặng, ta được tiếng nào?', 'Các bộ phận m + ăn + nặng tạo thành tiếng "mặn".', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mặn", "isCorrect": true}, {"text": "mân", "isCorrect": false}, {"text": "màn", "isCorrect": false}, {"text": "mãn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3161', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "chân", vần "ân" đứng ở vị trí nào?', 'Vần ân đứng sau âm đầu ch.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đứng sau âm ch", "isCorrect": true}, {"text": "đứng trước âm ch", "isCorrect": false}, {"text": "đứng một mình", "isCorrect": false}, {"text": "không có âm ch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3162', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "an" và thanh huyền?', 'Tiếng "đàn" thỏa mãn cả vần an và thanh huyền.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đàn", "isCorrect": true}, {"text": "đán", "isCorrect": false}, {"text": "đân", "isCorrect": false}, {"text": "đăn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3163', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào thích hợp để điền vào chỗ trống: "Bé đắp ..."?', 'Dựa trên nghĩa và các vần đã học, "chăn" (vần ăn) là từ phù hợp nhất.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chăn", "isCorrect": true}, {"text": "chân", "isCorrect": false}, {"text": "chum", "isCorrect": false}, {"text": "bàn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3164', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Các tiếng "nhãn, bản, dán" có chung vần gì?', 'Cả ba tiếng này đều có chung vần an.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vần an", "isCorrect": true}, {"text": "vần ăn", "isCorrect": false}, {"text": "vần ân", "isCorrect": false}, {"text": "vần am", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3165', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần khác với các tiếng còn lại: gân, thân, nhãn, phân?', 'Các tiếng "gân, thân, phân" đều có vần ân, riêng tiếng "nhãn" có vần an.', 1, '78', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nhãn", "isCorrect": true}, {"text": "gân", "isCorrect": false}, {"text": "thân", "isCorrect": false}, {"text": "phân", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- en, ên, in, un, iên, yên, uôn, ươn

('3166', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "en"?', 'Tiếng "đèn" gồm âm đầu đ, vần en và thanh huyền.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đèn", "isCorrect": true}, {"text": "đến", "isCorrect": false}, {"text": "đan", "isCorrect": false}, {"text": "đôn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3167', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ên"?', 'Tiếng "nến" gồm âm đầu n, vần ên và thanh sắc.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "nến", "isCorrect": true}, {"text": "nèn", "isCorrect": false}, {"text": "nôn", "isCorrect": false}, {"text": "nan", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3168', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "in"?', 'Tiếng "pin" gồm âm đầu p kết hợp với vần in.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "pin", "isCorrect": true}, {"text": "pan", "isCorrect": false}, {"text": "pôn", "isCorrect": false}, {"text": "pên", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3169', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "un"?', 'Tiếng "giun" gồm âm đầu gi kết hợp với vần un.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "giun", "isCorrect": true}, {"text": "gian", "isCorrect": false}, {"text": "gien", "isCorrect": false}, {"text": "gôn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3170', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "iên"?', 'Tiếng "kiến" gồm âm đầu k, vần iên và thanh sắc.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "kiến", "isCorrect": true}, {"text": "kén", "isCorrect": false}, {"text": "kín", "isCorrect": false}, {"text": "kên", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3171', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây bắt đầu bằng vần "yên"?', 'Tiếng "yến" là tiếng đặc biệt bắt đầu bằng y và mang vần yên.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "yến", "isCorrect": true}, {"text": "tiên", "isCorrect": false}, {"text": "điện", "isCorrect": false}, {"text": "hiền", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3172', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uôn"?', 'Tiếng "khuôn" gồm âm đầu kh kết hợp với vần uôn.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "khuôn", "isCorrect": true}, {"text": "khăn", "isCorrect": false}, {"text": "khen", "isCorrect": false}, {"text": "khôn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3173', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ươn"?', 'Tiếng "lươn" gồm âm đầu l kết hợp với vần ươn.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lươn", "isCorrect": true}, {"text": "luôn", "isCorrect": false}, {"text": "lan", "isCorrect": false}, {"text": "lên", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3174', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "biển" được cấu tạo từ những bộ phận nào?', 'Tiếng "biển" gồm âm đầu b, vần iên và thanh hỏi.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm b, vần iên và thanh hỏi", "isCorrect": true}, {"text": "âm b, vần yên và thanh hỏi", "isCorrect": false}, {"text": "âm b, vần iên và thanh ngã", "isCorrect": false}, {"text": "âm b, vần en và thanh hỏi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3175', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "en" trong tiếng "sen" bằng vần "ên", ta được tiếng nào?', 'Thay en bằng ên trong "sen" ta được tiếng "sên".', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sên", "isCorrect": true}, {"text": "sân", "isCorrect": false}, {"text": "sin", "isCorrect": false}, {"text": "son", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3176', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào có vần "in" và mang thanh sắc?', 'Tiếng "chín" gồm âm đầu ch, vần in và thanh sắc.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chín", "isCorrect": true}, {"text": "chín", "isCorrect": false}, {"text": "chìn", "isCorrect": false}, {"text": "chịn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3177', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào có vần "un" và âm đầu là "ph"?', 'Tiếng "phun" thỏa mãn âm đầu ph và vần un.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "phun", "isCorrect": true}, {"text": "phan", "isCorrect": false}, {"text": "phơi", "isCorrect": false}, {"text": "phân", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3178', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có vần "uôn" và thanh nặng?', 'Tiếng "cuộn" gồm âm đầu c, vần uôn và thanh nặng.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cuộn", "isCorrect": true}, {"text": "cuốn", "isCorrect": false}, {"text": "buồn", "isCorrect": false}, {"text": "luôn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3179', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có vần "ươn" và thanh huyền?', 'Tiếng "vườn" gồm âm đầu v, vần ươn và thanh huyền.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vườn", "isCorrect": true}, {"text": "vượn", "isCorrect": false}, {"text": "vươn", "isCorrect": false}, {"text": "mượn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3180', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào chỉ gồm vần "yên" và không có âm đầu?', 'Tiếng "yên" (trong bình yên) chỉ gồm vần yên.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "yên", "isCorrect": true}, {"text": "liên", "isCorrect": false}, {"text": "tiên", "isCorrect": false}, {"text": "diên", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3181', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Ghép âm "h", vần "iên" và thanh huyền, ta được tiếng nào?', 'Các bộ phận h + iên + huyền tạo thành tiếng "hiền".', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hiền", "isCorrect": true}, {"text": "hiện", "isCorrect": false}, {"text": "hiển", "isCorrect": false}, {"text": "hên", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3182', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "en" và âm đầu là "s"?', 'Tiếng "sen" (hoa sen) thỏa mãn âm đầu s và vần en.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sen", "isCorrect": true}, {"text": "sên", "isCorrect": false}, {"text": "sin", "isCorrect": false}, {"text": "san", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3183', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào có vần "ên" và thanh huyền?', 'Tiếng "mền" (cái chăn) gồm âm đầu m, vần ên và thanh huyền.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mền", "isCorrect": true}, {"text": "mên", "isCorrect": false}, {"text": "mèn", "isCorrect": false}, {"text": "mận", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3184', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Con ... bò dưới đất."', 'Từ "giun" (vần un) là từ phù hợp nhất về nghĩa.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "giun", "isCorrect": true}, {"text": "lươn", "isCorrect": false}, {"text": "kiến", "isCorrect": false}, {"text": "vượn", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3185', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Mẹ đắp ... cho bé ngủ."', 'Từ "mền" (vần ên) là từ phù hợp nhất về nghĩa và vần đã học.', 1, '79', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mền", "isCorrect": true}, {"text": "mền", "isCorrect": false}, {"text": "đèn", "isCorrect": false}, {"text": "vải", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- ang, ăng, âng, ong, ông, ung, ưng

('3186', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ang"?', 'Tiếng "sáng" gồm âm đầu s, vần ang và thanh sắc.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sáng", "isCorrect": true}, {"text": "sắng", "isCorrect": false}, {"text": "sâng", "isCorrect": false}, {"text": "sang", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3187', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ăng"?', 'Tiếng "măng" gồm âm đầu m kết hợp với vần ăng.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "măng", "isCorrect": true}, {"text": "mang", "isCorrect": false}, {"text": "mâng", "isCorrect": false}, {"text": "mông", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3188', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "âng"?', 'Tiếng "vầng" gồm âm đầu v, vần âng và thanh huyền.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vầng", "isCorrect": true}, {"text": "vàng", "isCorrect": false}, {"text": "văng", "isCorrect": false}, {"text": "vòng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3189', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ong"?', 'Tiếng "võng" gồm âm đầu v, vần ong và thanh ngã.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "võng", "isCorrect": true}, {"text": "vũng", "isCorrect": false}, {"text": "vông", "isCorrect": false}, {"text": "vâng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3190', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ông"?', 'Tiếng "trống" gồm âm đầu tr, vần ông và thanh sắc.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "trống", "isCorrect": true}, {"text": "trang", "isCorrect": false}, {"text": "trưng", "isCorrect": false}, {"text": "trong", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3191', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ung"?', 'Tiếng "thùng" gồm âm đầu th, vần ung và thanh huyền.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thùng", "isCorrect": true}, {"text": "thàng", "isCorrect": false}, {"text": "thông", "isCorrect": false}, {"text": "thưng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3192', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ưng"?', 'Tiếng "trứng" gồm âm đầu tr, vần ưng và thanh sắc.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "trứng", "isCorrect": true}, {"text": "trung", "isCorrect": false}, {"text": "trăng", "isCorrect": false}, {"text": "trong", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3193', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "bảng" được cấu tạo từ những bộ phận nào?', 'Tiếng "bảng" gồm âm đầu b, vần ang và thanh hỏi.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm b, vần ang và thanh hỏi", "isCorrect": true}, {"text": "âm b, vần ăng và thanh hỏi", "isCorrect": false}, {"text": "âm b, vần âng và thanh hỏi", "isCorrect": false}, {"text": "âm b, vần ong và thanh hỏi", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3194', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "ang" trong tiếng "mang" bằng vần "ăng", ta được tiếng nào?', 'Thay ang bằng ăng trong "mang" ta được tiếng "măng".', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "măng", "isCorrect": true}, {"text": "mâng", "isCorrect": false}, {"text": "mông", "isCorrect": false}, {"text": "mùng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3195', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "ong" và mang thanh sắc?', 'Tiếng "chóng" (trong chóng mặt) thỏa mãn vần ong và thanh sắc.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "chóng", "isCorrect": true}, {"text": "chồng", "isCorrect": false}, {"text": "chông", "isCorrect": false}, {"text": "chùng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3196', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Vần nào xuất hiện trong cả ba tiếng: thông, hồng, cổng?', 'Cả ba tiếng này đều có chung vần ông.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vần ông", "isCorrect": true}, {"text": "vần ong", "isCorrect": false}, {"text": "vần ung", "isCorrect": false}, {"text": "vần ưng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3197', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn vần thích hợp điền vào chỗ trống: "Cây s... xum xuê quả"?', 'Vần "ung" ghép với "s" tạo thành tiếng "sung" là tên một loại cây.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ung", "isCorrect": true}, {"text": "ưng", "isCorrect": false}, {"text": "ang", "isCorrect": false}, {"text": "ong", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3198', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Dấu thanh trong tiếng "vầng" được đặt ở đâu?', 'Dấu huyền được đặt trên âm chính là chữ cái "â".', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "trên đầu chữ â", "isCorrect": true}, {"text": "trên đầu chữ n", "isCorrect": false}, {"text": "dưới chữ â", "isCorrect": false}, {"text": "trên đầu chữ v", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3199', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "trăng" và "trắng" khác nhau ở bộ phận nào?', 'Hai tiếng này giống nhau về âm đầu và vần, chỉ khác nhau về dấu thanh (ngang và sắc).', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thanh điệu", "isCorrect": true}, {"text": "âm đầu", "isCorrect": false}, {"text": "vần", "isCorrect": false}, {"text": "âm cuối", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3200', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào dưới đây bắt đầu bằng âm "ng" và có vần "âng"?', 'Tiếng "ngẩng" (trong ngẩng đầu) thỏa mãn cả âm đầu ng và vần âng.', 1, '80', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ngẩng", "isCorrect": true}, {"text": "ngang", "isCorrect": false}, {"text": "ngóng", "isCorrect": false}, {"text": "ngưng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- anh, ênh, inh, ach, êch, ich
('3201', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "anh"?', 'Tiếng "bánh" gồm âm đầu b, vần anh và thanh sắc.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bánh", "isCorrect": true}, {"text": "bệnh", "isCorrect": false}, {"text": "bình", "isCorrect": false}, {"text": "bạch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3202', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ênh"?', 'Tiếng "lệnh" gồm âm đầu l, vần ênh và thanh nặng.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lệnh", "isCorrect": true}, {"text": "lành", "isCorrect": false}, {"text": "linh", "isCorrect": false}, {"text": "lệch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3203', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "inh"?', 'Tiếng "kính" gồm âm đầu k, vần inh và thanh sắc.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "kính", "isCorrect": true}, {"text": "kênh", "isCorrect": false}, {"text": "cành", "isCorrect": false}, {"text": "kịch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3204', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ach"?', 'Tiếng "sạch" gồm âm đầu s, vần ach và thanh nặng.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sạch", "isCorrect": true}, {"text": "sanh", "isCorrect": false}, {"text": "sênh", "isCorrect": false}, {"text": "sịch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3205', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "êch"?', 'Tiếng "ếch" là tiếng chỉ gồm vần êch và thanh sắc.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ếch", "isCorrect": true}, {"text": "ênh", "isCorrect": false}, {"text": "ach", "isCorrect": false}, {"text": "ích", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3206', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ich"?', 'Tiếng "thích" gồm âm đầu th, vần ich và thanh sắc.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thích", "isCorrect": true}, {"text": "thanh", "isCorrect": false}, {"text": "thênh", "isCorrect": false}, {"text": "thạch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3207', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "cành" được cấu tạo từ những bộ phận nào?', 'Tiếng "cành" gồm âm đầu c, vần anh và thanh huyền.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm c, vần anh và thanh huyền", "isCorrect": true}, {"text": "âm c, vần ach và thanh huyền", "isCorrect": false}, {"text": "âm ch, vần anh và thanh huyền", "isCorrect": false}, {"text": "âm c, vần inh và thanh huyền", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3208', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "ach" trong tiếng "gạch" bằng vần "ich", ta được tiếng nào?', 'Thay vần ach bằng ich trong "gạch" ta được tiếng "gịch" (trong từ huỵch gịch).', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "gịch", "isCorrect": true}, {"text": "gành", "isCorrect": false}, {"text": "gênh", "isCorrect": false}, {"text": "gạch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3209', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "inh" và mang thanh sắc?', 'Tiếng "kính" thỏa mãn vần inh và thanh sắc.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "kính", "isCorrect": true}, {"text": "kình", "isCorrect": false}, {"text": "kênh", "isCorrect": false}, {"text": "kịch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3210', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "ach" và mang thanh nặng?', 'Tiếng "mạch" (trong mạch máu hoặc mạch lạc) có vần ach và thanh nặng.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mạch", "isCorrect": true}, {"text": "mách", "isCorrect": false}, {"text": "mình", "isCorrect": false}, {"text": "mệnh", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3211', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Vần "anh" và vần "ach" giống nhau ở điểm nào?', 'Cả hai vần đều bắt đầu bằng âm chính là "a".', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đều có âm chính là a", "isCorrect": true}, {"text": "đều có âm cuối là nh", "isCorrect": false}, {"text": "đều có âm cuối là ch", "isCorrect": false}, {"text": "đều có âm chính là i", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3212', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Bé đọc quyển ..."?', 'Từ "sách" (vần ach) phù hợp nhất về nghĩa.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "sách", "isCorrect": true}, {"text": "sinh", "isCorrect": false}, {"text": "sên", "isCorrect": false}, {"text": "sanh", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3213', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Mẹ mua ... chưng"?', 'Từ "bánh" (vần anh) phù hợp nhất về nghĩa và vần đã học.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bánh", "isCorrect": true}, {"text": "bệnh", "isCorrect": false}, {"text": "bình", "isCorrect": false}, {"text": "bịch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3214', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm đầu "l", vần "ênh" và thanh huyền?', 'Ghép l + ênh + huyền ta được tiếng "lềnh" (trong từ lềnh bềnh).', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lềnh", "isCorrect": true}, {"text": "lệnh", "isCorrect": false}, {"text": "lành", "isCorrect": false}, {"text": "lệch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3215', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong các tiếng "ếch, kệch, lệch", tiếng nào không có âm đầu?', 'Tiếng "ếch" chỉ có vần êch và thanh sắc, không có âm đầu.', 1, '81', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ếch", "isCorrect": true}, {"text": "kệch", "isCorrect": false}, {"text": "lệch", "isCorrect": false}, {"text": "bịch", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


------ at, ăt, ất, ot, ôt, ơt, et, êt, it, ut, ưt

('3216', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "at"?', 'Tiếng "hạt" gồm âm đầu h, vần at và thanh nặng.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hạt", "isCorrect": true}, {"text": "mắt", "isCorrect": false}, {"text": "tất", "isCorrect": false}, {"text": "hót", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3217', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ăt"?', 'Tiếng "mắt" gồm âm đầu m, vần ăt và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mắt", "isCorrect": true}, {"text": "mát", "isCorrect": false}, {"text": "mất", "isCorrect": false}, {"text": "mứt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3218', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ất"?', 'Tiếng "tất" (trong đôi tất) gồm âm đầu t, vần ất và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "tất", "isCorrect": true}, {"text": "tát", "isCorrect": false}, {"text": "tắt", "isCorrect": false}, {"text": "tốt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3219', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ot"?', 'Tiếng "hót" gồm âm đầu h, vần ot và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hót", "isCorrect": true}, {"text": "hốt", "isCorrect": false}, {"text": "hớt", "isCorrect": false}, {"text": "hát", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3220', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ôt"?', 'Tiếng "tốt" gồm âm đầu t, vần ôt và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "tốt", "isCorrect": true}, {"text": "tót", "isCorrect": false}, {"text": "tớt", "isCorrect": false}, {"text": "tết", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3221', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ơt"?', 'Tiếng "vợt" gồm âm đầu v, vần ơt và thanh nặng.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vợt", "isCorrect": true}, {"text": "vật", "isCorrect": false}, {"text": "vệt", "isCorrect": false}, {"text": "vụt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3222', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "et"?', 'Tiếng "hét" gồm âm đầu h, vần et và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hét", "isCorrect": true}, {"text": "hết", "isCorrect": false}, {"text": "hát", "isCorrect": false}, {"text": "hít", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3223', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "êt"?', 'Tiếng "tết" gồm âm đầu t, vần êt và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "tết", "isCorrect": true}, {"text": "tất", "isCorrect": false}, {"text": "tắt", "isCorrect": false}, {"text": "tát", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3224', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "it"?', 'Tiếng "thịt" gồm âm đầu th, vần it và thanh nặng.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thịt", "isCorrect": true}, {"text": "thụt", "isCorrect": false}, {"text": "thớt", "isCorrect": false}, {"text": "thật", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3225', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ut"?', 'Tiếng "bút" gồm âm đầu b, vần ut và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "bút", "isCorrect": true}, {"text": "bứt", "isCorrect": false}, {"text": "bét", "isCorrect": false}, {"text": "bốt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3226', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "ưt"?', 'Tiếng "mứt" gồm âm đầu m, vần ưt và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mứt", "isCorrect": true}, {"text": "mít", "isCorrect": false}, {"text": "mất", "isCorrect": false}, {"text": "mút", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3227', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "bát" được cấu tạo từ những bộ phận nào?', 'Tiếng "bát" gồm âm đầu b, vần at và thanh sắc.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm b, vần at và thanh sắc", "isCorrect": true}, {"text": "âm b, vần ăt và thanh sắc", "isCorrect": false}, {"text": "âm b, vần ất và thanh sắc", "isCorrect": false}, {"text": "âm b, vần et và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3228', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "mật" được cấu tạo từ những bộ phận nào?', 'Tiếng "mật" gồm âm đầu m, vần ất và thanh nặng.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm m, vần ất và thanh nặng", "isCorrect": true}, {"text": "âm m, vần ăt và thanh nặng", "isCorrect": false}, {"text": "âm m, vần at và thanh nặng", "isCorrect": false}, {"text": "âm m, vần et và thanh nặng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3229', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "et" trong tiếng "hét" bằng vần "êt", ta được tiếng nào?', 'Thay et bằng êt trong "hét" ta được tiếng "hết".', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hết", "isCorrect": true}, {"text": "hát", "isCorrect": false}, {"text": "hít", "isCorrect": false}, {"text": "hốt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3230', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "ot" trong tiếng "hót" bằng vần "ôt", ta được tiếng nào?', 'Thay ot bằng ôt trong "hót" ta được tiếng "hốt".', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "hốt", "isCorrect": true}, {"text": "hét", "isCorrect": false}, {"text": "hát", "isCorrect": false}, {"text": "hớt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3231', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "it" và âm đầu là "v"?', 'Ghép v + it và thêm thanh nặng ta được tiếng "vịt".', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vịt", "isCorrect": true}, {"text": "vụt", "isCorrect": false}, {"text": "vật", "isCorrect": false}, {"text": "vát", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3232', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Các tiếng "mặt, cắt, dắt" có chung vần gì?', 'Cả ba tiếng này đều có chung vần ăt.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "vần ăt", "isCorrect": true}, {"text": "vần at", "isCorrect": false}, {"text": "vần ất", "isCorrect": false}, {"text": "vần ut", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3233', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "ut" và mang thanh nặng?', 'Tiếng "lụt" (lũ lụt) thỏa mãn vần ut và thanh nặng.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lụt", "isCorrect": true}, {"text": "lát", "isCorrect": false}, {"text": "lít", "isCorrect": false}, {"text": "lọt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3234', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Bé có quả ..."?', 'Từ "mít" (vần it) là loại quả quen thuộc và phù hợp nhất.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mít", "isCorrect": true}, {"text": "mứt", "isCorrect": false}, {"text": "mật", "isCorrect": false}, {"text": "mặt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3235', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn vần thích hợp điền vào chỗ trống: "Mẹ mua đ... tất"?', 'Vần "ôi" ghép với "đ" tạo thành tiếng "đôi". Tuy nhiên, từ cần điền là "tất" (vần ất).', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "ất", "isCorrect": true}, {"text": "ăt", "isCorrect": false}, {"text": "at", "isCorrect": false}, {"text": "et", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3236', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Trong tiếng "thớt", vần "ơt" đứng ở vị trí nào?', 'Vần ơt đứng sau âm đầu th.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đứng sau âm th", "isCorrect": true}, {"text": "đứng trước âm th", "isCorrect": false}, {"text": "đứng một mình", "isCorrect": false}, {"text": "đứng ở giữa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3237', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Dấu thanh trong tiếng "đất" được đặt ở đâu?', 'Dấu sắc được đặt trên âm chính là chữ cái "â".', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "trên đầu chữ â", "isCorrect": true}, {"text": "trên đầu chữ t", "isCorrect": false}, {"text": "dưới chữ â", "isCorrect": false}, {"text": "trên đầu chữ đ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3238', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có âm chính khác với các tiếng còn lại: mứt, đứt, nứt, mút?', 'Các tiếng "mứt, đứt, nứt" đều có vần ưt, riêng "mút" có vần ut.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "mút", "isCorrect": true}, {"text": "mứt", "isCorrect": false}, {"text": "đứt", "isCorrect": false}, {"text": "nứt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3239', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Ghép âm "r", vần "ot" và thanh sắc, ta được tiếng nào?', 'Các bộ phận r + ot + sắc tạo thành tiếng "rót".', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "rót", "isCorrect": true}, {"text": "rốt", "isCorrect": false}, {"text": "rét", "isCorrect": false}, {"text": "rút", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3240', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào dưới đây bắt đầu bằng âm "c" và có vần "ăt"?', 'Tiếng "cắt" thỏa mãn cả âm đầu c và vần ăt.', 1, '82', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "cắt", "isCorrect": true}, {"text": "cát", "isCorrect": false}, {"text": "cất", "isCorrect": false}, {"text": "cột", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


----- uê, uy, uơ, uya
('3241', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uê"?', 'Tiếng "thuê" gồm âm đầu th kết hợp với vần uê.', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thuê", "isCorrect": true}, {"text": "thua", "isCorrect": false}, {"text": "thùy", "isCorrect": false}, {"text": "thơ", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3242', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uy"?', 'Tiếng "lũy" (trong lũy tre) gồm âm đầu l, vần uy và thanh ngã.', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "lũy", "isCorrect": true}, {"text": "lụa", "isCorrect": false}, {"text": "lê", "isCorrect": false}, {"text": "loa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3243', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uơ"?', 'Tiếng "huơ" (trong huơ tay) gồm âm đầu h kết hợp với vần uơ.', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "huơ", "isCorrect": true}, {"text": "hoa", "isCorrect": false}, {"text": "huệ", "isCorrect": false}, {"text": "huy", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3244', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uya"?', 'Tiếng "khuya" gồm âm đầu kh kết hợp với vần uya.', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "khuya", "isCorrect": true}, {"text": "khoa", "isCorrect": false}, {"text": "khua", "isCorrect": false}, {"text": "khế", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3245', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "thuế" được cấu tạo từ những bộ phận nào?', 'Tiếng "thuế" gồm âm đầu th, vần uê và thanh sắc.', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm th, vần uê và thanh sắc", "isCorrect": true}, {"text": "âm th, vần uy và thanh sắc", "isCorrect": false}, {"text": "âm t, vần uê và thanh sắc", "isCorrect": false}, {"text": "âm th, vần uơ và thanh sắc", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3246', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Dấu thanh trong tiếng "lũy" được đặt ở vị trí nào?', 'Trong vần uy, dấu thanh được đặt trên đầu âm chính là chữ cái "y".', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "trên đầu chữ y", "isCorrect": true}, {"text": "trên đầu chữ u", "isCorrect": false}, {"text": "dưới chữ y", "isCorrect": false}, {"text": "dưới chữ u", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3247', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn vần thích hợp điền vào chỗ trống: "Bé h... tay chào mẹ"?', 'Vần "uơ" ghép với âm "h" tạo thành tiếng "huơ" (chỉ hành động đưa tay qua lại).', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "uơ", "isCorrect": true}, {"text": "uê", "isCorrect": false}, {"text": "uy", "isCorrect": false}, {"text": "uya", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3248', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Đêm ..., bé đã ngủ say"?', 'Từ "khuya" (vần uya) chỉ thời gian muộn về đêm, phù hợp nhất về nghĩa.', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "khuya", "isCorrect": true}, {"text": "huệ", "isCorrect": false}, {"text": "lũy", "isCorrect": false}, {"text": "thuế", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3249', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào dưới đây có vần "uê" và mang thanh nặng?', 'Tiếng "huệ" (tên một loài hoa) gồm âm đầu h, vần uê và thanh nặng.', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "huệ", "isCorrect": true}, {"text": "huế", "isCorrect": false}, {"text": "huy", "isCorrect": false}, {"text": "hòa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3250', 'MULTIPLE_CHOICE', 'COMPRE_HENSION', 'Tiếng nào có âm đầu "h", vần "uy" và không có dấu thanh (thanh ngang)?', 'Ghép h + uy ta được tiếng "huy" (trong huy hiệu).', 1, '83', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "huy", "isCorrect": true}, {"text": "hủy", "isCorrect": false}, {"text": "huệ", "isCorrect": false}, {"text": "hoa", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),


------ uân, uyên, uât, uyêt

('3251', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uân"?', 'Tiếng "xuân" gồm âm đầu x và vần uân.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "xuân", "isCorrect": true}, {"text": "xuyên", "isCorrect": false}, {"text": "xuất", "isCorrect": false}, {"text": "xê", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3252', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uyên"?', 'Tiếng "khuyên" gồm âm đầu kh kết hợp với vần uyên.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "khuyên", "isCorrect": true}, {"text": "khuân", "isCorrect": false}, {"text": "khuất", "isCorrect": false}, {"text": "khuya", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3253', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uât"?', 'Tiếng "luật" gồm âm đầu l, vần uât và thanh nặng.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "luật", "isCorrect": true}, {"text": "luân", "isCorrect": false}, {"text": "luyện", "isCorrect": false}, {"text": "tuyết", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3254', 'MULTIPLE_CHOICE', 'KNOWLEDGE', 'Tiếng nào dưới đây có chứa vần "uyêt"?', 'Tiếng "duyệt" gồm âm đầu d, vần uyêt và thanh nặng.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "duyệt", "isCorrect": true}, {"text": "dân", "isCorrect": false}, {"text": "dưới", "isCorrect": false}, {"text": "uất", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3255', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "quân" được cấu tạo từ những bộ phận nào?', 'Tiếng "quân" gồm âm đầu qu và vần uân.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "âm qu và vần uân", "isCorrect": true}, {"text": "âm q và vần uân", "isCorrect": false}, {"text": "âm qu và vần uât", "isCorrect": false}, {"text": "âm qu và âm uân", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3256', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Dấu thanh trong tiếng "thuyền" được đặt ở vị trí nào?', 'Trong vần uyên, dấu thanh được đặt trên đầu âm chính là chữ cái "ê".', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "trên đầu chữ ê", "isCorrect": true}, {"text": "trên đầu chữ u", "isCorrect": false}, {"text": "trên đầu chữ y", "isCorrect": false}, {"text": "trên đầu chữ n", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3257', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng "tuyết" gồm âm đầu "t", vần "uyêt" và thanh gì?', 'Tiếng "tuyết" sử dụng thanh sắc đặt trên đầu chữ ê.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "thanh sắc", "isCorrect": true}, {"text": "thanh huyền", "isCorrect": false}, {"text": "thanh hỏi", "isCorrect": false}, {"text": "thanh nặng", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3258', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Khi thay vần "uân" trong tiếng "tuần" bằng vần "uât", ta được tiếng nào?', 'Thay uân bằng uât trong "tuần" ta được tiếng "tuật".', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "tuật", "isCorrect": true}, {"text": "tuất", "isCorrect": false}, {"text": "tuyết", "isCorrect": false}, {"text": "tuyên", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3259', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Chim vành ..."?', 'Từ "khuyên" (vần uyên) tạo thành tên loài chim quen thuộc.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "khuyên", "isCorrect": true}, {"text": "khuân", "isCorrect": false}, {"text": "khuất", "isCorrect": false}, {"text": "tuyên", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3260', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Chọn từ thích hợp điền vào chỗ trống: "Mùa ... có hoa đào nở"?', 'Mùa "xuân" (vần uân) là mùa hoa đào nở.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "xuân", "isCorrect": true}, {"text": "xuất", "isCorrect": false}, {"text": "xuyên", "isCorrect": false}, {"text": "tuyết", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3261', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Tiếng nào có vần "uât" và mang thanh sắc?', 'Tiếng "xuất" (trong sản xuất) thỏa mãn vần uât và thanh sắc.', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "xuất", "isCorrect": true}, {"text": "thuật", "isCorrect": false}, {"text": "tuần", "isCorrect": false}, {"text": "duyệt", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now()),

('3262', 'MULTIPLE_CHOICE', 'COMPREHENSION', 'Vần "uân" và vần "uât" giống nhau ở điểm nào?', 'Cả hai vần đều bắt đầu bằng phần âm đệm và âm chính là "uâ".', 1, '84', 'TV', 
$${"type": "MULTIPLE_CHOICE", "options": [{"text": "đều có phần đầu là uâ", "isCorrect": true}, {"text": "đều có âm cuối là n", "isCorrect": false}, {"text": "đều có âm cuối là t", "isCorrect": false}, {"text": "đều có phần đầu là uyê", "isCorrect": false}], "shuffleOptions": true}$$::jsonb, 
NULL, now(), now())
ON CONFLICT (id) DO NOTHING;