-- Checkmate seed data (users ~30, study_groups ~10, plus related tables)
BEGIN;
SET search_path TO public;
-- Optional: clear existing seeded data
TRUNCATE TABLE group_verification_methods, group_verification_schedule, group_verification_frequency, group_invitations, study_group_tags, inquiry_comment, inquiry, notice, hashtags, study_user, study_groups, users RESTART IDENTITY;
INSERT INTO users (user_id, email, name, nickname, gender, birthdate, phone_number, is_active, fav_category_1, fav_category_2, fav_category_3, profile_image_url) VALUES
  ('267081d6-6494-5e96-8b99-2c637976b26b', 'user01@example.com', 'User01', 'u01', 'FEMALE', '1995-03-19', '010-9001-1001', true, 'WAKE', NULL, NULL, NULL),
  ('817d1b96-e913-52d5-a02b-83d6c3454f2d', 'user02@example.com', 'User02', 'u02', 'MALE', '1995-06-04', '010-9002-1002', true, 'SEATED', NULL, NULL, NULL),
  ('73da3155-fde0-5355-a8bd-ff62a69d3a27', 'user03@example.com', 'User03', 'u03', 'FEMALE', '1995-08-20', '010-9003-1003', true, 'COTE', 'CERT', NULL, NULL),
  ('2bb08dea-daa7-59c0-b4bb-4d5d19c0cae1', 'user04@example.com', 'User04', 'u04', 'MALE', '1995-11-05', '010-9004-1004', true, 'LANG', NULL, NULL, 'https://pics.example.com/u04.png'),
  ('76f0a825-b223-5908-96ca-710a6e10ae6d', 'user05@example.com', 'User05', 'u05', 'FEMALE', '1996-01-21', '010-9005-1005', true, 'CERT', NULL, 'SEATED', NULL),
  ('af7d24ac-519b-5d02-8607-1db817e5b740', 'user06@example.com', 'User06', 'u06', 'MALE', '1996-04-07', '010-9006-1006', true, 'ETC', 'SEATED', NULL, NULL),
  ('0caafffe-4ca4-5521-924c-48d1b06607b1', 'user07@example.com', 'User07', 'u07', 'FEMALE', '1996-06-23', '010-9007-1007', true, 'WAKE', NULL, NULL, NULL),
  ('5903b5cc-f44b-51e7-a88a-f5e1d49ee157', 'user08@example.com', 'User08', 'u08', 'MALE', '1996-09-08', '010-9008-1008', true, 'SEATED', NULL, NULL, 'https://pics.example.com/u08.png'),
  ('281fd5b3-39a5-5aea-b812-41750c5f6a15', 'user09@example.com', 'User09', 'u09', 'FEMALE', '1996-11-24', '010-9009-1009', true, 'COTE', 'CERT', NULL, NULL),
  ('d9c50348-d82b-5cec-b29d-ea5e0bfab91f', 'user10@example.com', 'User10', 'u10', 'MALE', '1997-02-09', '010-9010-1010', true, 'LANG', NULL, 'WAKE', NULL),
  ('4bfefaed-8313-57d2-bc29-052c1d9e5385', 'user11@example.com', 'User11', 'u11', 'FEMALE', '1997-04-27', '010-9011-1011', true, 'CERT', NULL, NULL, NULL),
  ('ab937cea-8537-5b8c-98c9-bc3ebf7fb15c', 'user12@example.com', 'User12', 'u12', 'MALE', '1997-07-13', '010-9012-1012', true, 'ETC', 'SEATED', NULL, 'https://pics.example.com/u12.png'),
  ('4fc95afd-4b8a-5dbf-8abb-f9ca5010513f', 'user13@example.com', 'User13', 'u13', 'FEMALE', '1997-09-28', '010-9013-1013', false, 'WAKE', NULL, NULL, NULL),
  ('784cb960-e53c-53a2-9662-7f5a6b46cb88', 'user14@example.com', 'User14', 'u14', 'MALE', '1997-12-14', '010-9014-1014', true, 'SEATED', NULL, NULL, NULL),
  ('9cd3641e-d016-56ea-937f-6bc6272b0de3', 'user15@example.com', 'User15', 'u15', 'FEMALE', '1998-03-01', '010-9015-1015', true, 'COTE', 'CERT', 'ETC', NULL),
  ('9ecfd5f6-000c-5147-8b5e-ad29d027716e', 'user16@example.com', 'User16', 'u16', 'MALE', '1998-05-17', '010-9016-1016', true, 'LANG', NULL, NULL, 'https://pics.example.com/u16.png'),
  ('1c464f97-f3eb-5bb6-9420-364f3b287166', 'user17@example.com', 'User17', 'u17', 'FEMALE', '1998-08-02', '010-9017-1017', true, 'CERT', NULL, NULL, NULL),
  ('aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', 'user18@example.com', 'User18', 'u18', 'MALE', '1998-10-18', '010-9018-1018', true, 'ETC', 'SEATED', NULL, NULL),
  ('7fa8d638-25a4-55b1-aff5-ae28d651d2ee', 'user19@example.com', 'User19', 'u19', 'FEMALE', '1999-01-03', '010-9019-1019', true, 'WAKE', NULL, NULL, NULL),
  ('39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 'user20@example.com', 'User20', 'u20', 'MALE', '1999-03-21', '010-9020-1020', true, 'SEATED', NULL, 'CERT', 'https://pics.example.com/u20.png'),
  ('a1984805-03ed-5349-aafb-9a048ffcb695', 'user21@example.com', 'User21', 'u21', 'FEMALE', '1999-06-06', '010-9021-1021', true, 'COTE', 'CERT', NULL, NULL),
  ('b5143ad5-9424-55f2-a50d-d1efab382adb', 'user22@example.com', 'User22', 'u22', 'MALE', '1999-08-22', '010-9022-1022', true, 'LANG', NULL, NULL, NULL),
  ('1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 'user23@example.com', 'User23', 'u23', 'FEMALE', '1999-11-07', '010-9023-1023', true, 'CERT', NULL, NULL, NULL),
  ('c10e9968-a097-53e7-895b-8fd165e668da', 'user24@example.com', 'User24', 'u24', 'MALE', '2000-01-23', '010-9024-1024', true, 'ETC', 'SEATED', NULL, 'https://pics.example.com/u24.png'),
  ('2c4cba41-bb1a-5a8d-a54b-db495fda167b', 'user25@example.com', 'User25', 'u25', 'FEMALE', '2000-04-09', '010-9025-1025', true, 'WAKE', NULL, 'LANG', NULL),
  ('71395a94-f2ca-51a3-8672-b75192f3e2af', 'user26@example.com', 'User26', 'u26', 'MALE', '2000-06-25', '010-9026-1026', false, 'SEATED', NULL, NULL, NULL),
  ('377b7fa0-0847-559f-ae18-8366ceca7390', 'user27@example.com', 'User27', 'u27', 'FEMALE', '2000-09-10', '010-9027-1027', true, 'COTE', 'CERT', NULL, NULL),
  ('3ee39332-1c89-51e7-9b65-ecf77321ce8a', 'user28@example.com', 'User28', 'u28', 'MALE', '2000-11-26', '010-9028-1028', true, 'LANG', NULL, NULL, 'https://pics.example.com/u28.png'),
  ('33174d73-4ea1-5242-8602-c05aa878eb6e', 'user29@example.com', 'User29', 'u29', 'FEMALE', '2001-02-11', '010-9029-1029', true, 'CERT', NULL, NULL, NULL),
  ('b16ac12d-1684-5a4f-941f-92bb98e90a29', 'user30@example.com', 'User30', 'u30', 'MALE', '2001-04-29', '010-9030-1030', true, 'ETC', 'SEATED', 'COTE', NULL);

INSERT INTO study_groups (group_id, title, description, min_members, max_members, current_members, status, owner_user_id, thumbnail_type, thumbnail_url, category, join_type, start_date, end_date, duration_weeks, is_indefinite, closed_at) VALUES
  (1, 'Study Group 01', '자동 생성 스터디 그룹 01', 2, 9, 6, 'RECRUITING', '267081d6-6494-5e96-8b99-2c637976b26b', 'DEFAULT', NULL, 'WAKE', 'PUBLIC', '2026-01-12', '2026-02-23', 6, false, NULL),
  (2, 'Study Group 02', '자동 생성 스터디 그룹 02', 2, 12, 7, 'RECRUITING', '817d1b96-e913-52d5-a02b-83d6c3454f2d', 'UPLOAD', 'https://pics.example.com/groups/g2.jpg', 'SEATED', 'PUBLIC', '2026-01-05', '2026-03-02', 8, false, NULL),
  (3, 'Study Group 03', '자동 생성 스터디 그룹 03', 2, 15, 8, 'RECRUITING', '73da3155-fde0-5355-a8bd-ff62a69d3a27', 'DEFAULT', NULL, 'COTE', 'INVITE_ONLY', '2025-12-20', NULL, NULL, true, NULL),
  (4, 'Study Group 04', '자동 생성 스터디 그룹 04', 2, 18, 5, 'CLOSED', '2bb08dea-daa7-59c0-b4bb-4d5d19c0cae1', 'UPLOAD', 'https://pics.example.com/groups/g4.jpg', 'LANG', 'PUBLIC', '2025-12-22', '2026-01-19', 4, false, '2026-01-07 18:00:00'),
  (5, 'Study Group 05', '자동 생성 스터디 그룹 05', 2, 6, 6, 'RECRUITING', '76f0a825-b223-5908-96ca-710a6e10ae6d', 'DEFAULT', NULL, 'CERT', 'PUBLIC', '2025-12-15', '2026-01-26', 6, false, NULL),
  (6, 'Study Group 06', '자동 생성 스터디 그룹 06', 2, 9, 7, 'RECRUITING', 'af7d24ac-519b-5d02-8607-1db817e5b740', 'UPLOAD', 'https://pics.example.com/groups/g6.jpg', 'ETC', 'INVITE_ONLY', '2025-11-20', NULL, NULL, true, NULL),
  (7, 'Study Group 07', '자동 생성 스터디 그룹 07', 2, 12, 8, 'CLOSED', '0caafffe-4ca4-5521-924c-48d1b06607b1', 'DEFAULT', NULL, 'WAKE', 'PUBLIC', '2025-12-01', '2026-02-09', 10, false, '2025-12-29 18:00:00'),
  (8, 'Study Group 08', '자동 생성 스터디 그룹 08', 2, 15, 5, 'RECRUITING', '5903b5cc-f44b-51e7-a88a-f5e1d49ee157', 'UPLOAD', 'https://pics.example.com/groups/g8.jpg', 'SEATED', 'PUBLIC', '2025-11-24', '2025-12-22', 4, false, NULL),
  (9, 'Study Group 09', '자동 생성 스터디 그룹 09', 2, 18, 6, 'RECRUITING', '281fd5b3-39a5-5aea-b812-41750c5f6a15', 'DEFAULT', NULL, 'COTE', 'INVITE_ONLY', '2025-10-21', NULL, NULL, true, NULL),
  (10, 'Study Group 10', '자동 생성 스터디 그룹 10', 2, 6, 6, 'CLOSED', 'd9c50348-d82b-5cec-b29d-ea5e0bfab91f', 'UPLOAD', 'https://pics.example.com/groups/g10.jpg', 'LANG', 'PUBLIC', '2025-11-10', '2026-01-05', 8, false, '2025-12-20 18:00:00');

INSERT INTO study_user (user_id, study_id, study_id2, user_id2, role, status, is_study_notification) VALUES
  ('267081d6-6494-5e96-8b99-2c637976b26b', 1, 1, '267081d6-6494-5e96-8b99-2c637976b26b', 'Leader', 'ACTIVE', true),
  ('39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 1, 1, '39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 'Member', 'ACTIVE', true),
  ('9ecfd5f6-000c-5147-8b5e-ad29d027716e', 1, 1, '9ecfd5f6-000c-5147-8b5e-ad29d027716e', 'Member', 'ACTIVE', true),
  ('ab937cea-8537-5b8c-98c9-bc3ebf7fb15c', 1, 1, 'ab937cea-8537-5b8c-98c9-bc3ebf7fb15c', 'Member', 'ACTIVE', true),
  ('c10e9968-a097-53e7-895b-8fd165e668da', 1, 1, 'c10e9968-a097-53e7-895b-8fd165e668da', 'Member', 'ACTIVE', true),
  ('1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 1, 1, '1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 'Member', 'ACTIVE', true),
  ('817d1b96-e913-52d5-a02b-83d6c3454f2d', 2, 2, '817d1b96-e913-52d5-a02b-83d6c3454f2d', 'Leader', 'ACTIVE', true),
  ('2bb08dea-daa7-59c0-b4bb-4d5d19c0cae1', 2, 2, '2bb08dea-daa7-59c0-b4bb-4d5d19c0cae1', 'Member', 'ACTIVE', true),
  ('1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 2, 2, '1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 'Member', 'ACTIVE', true),
  ('784cb960-e53c-53a2-9662-7f5a6b46cb88', 2, 2, '784cb960-e53c-53a2-9662-7f5a6b46cb88', 'Member', 'ACTIVE', true),
  ('281fd5b3-39a5-5aea-b812-41750c5f6a15', 2, 2, '281fd5b3-39a5-5aea-b812-41750c5f6a15', 'Member', 'ACTIVE', true),
  ('a1984805-03ed-5349-aafb-9a048ffcb695', 2, 2, 'a1984805-03ed-5349-aafb-9a048ffcb695', 'Member', 'ACTIVE', true),
  ('1c464f97-f3eb-5bb6-9420-364f3b287166', 2, 2, '1c464f97-f3eb-5bb6-9420-364f3b287166', 'Member', 'ACTIVE', true),
  ('73da3155-fde0-5355-a8bd-ff62a69d3a27', 3, 3, '73da3155-fde0-5355-a8bd-ff62a69d3a27', 'Leader', 'ACTIVE', true),
  ('ab937cea-8537-5b8c-98c9-bc3ebf7fb15c', 3, 3, 'ab937cea-8537-5b8c-98c9-bc3ebf7fb15c', 'Member', 'ACTIVE', true),
  ('76f0a825-b223-5908-96ca-710a6e10ae6d', 3, 3, '76f0a825-b223-5908-96ca-710a6e10ae6d', 'Member', 'ACTIVE', true),
  ('2c4cba41-bb1a-5a8d-a54b-db495fda167b', 3, 3, '2c4cba41-bb1a-5a8d-a54b-db495fda167b', 'Member', 'ACTIVE', true),
  ('267081d6-6494-5e96-8b99-2c637976b26b', 3, 3, '267081d6-6494-5e96-8b99-2c637976b26b', 'Member', 'ACTIVE', true),
  ('784cb960-e53c-53a2-9662-7f5a6b46cb88', 3, 3, '784cb960-e53c-53a2-9662-7f5a6b46cb88', 'Member', 'ACTIVE', true),
  ('9ecfd5f6-000c-5147-8b5e-ad29d027716e', 3, 3, '9ecfd5f6-000c-5147-8b5e-ad29d027716e', 'Member', 'ACTIVE', true),
  ('d9c50348-d82b-5cec-b29d-ea5e0bfab91f', 3, 3, 'd9c50348-d82b-5cec-b29d-ea5e0bfab91f', 'Member', 'ACTIVE', true),
  ('2bb08dea-daa7-59c0-b4bb-4d5d19c0cae1', 4, 4, '2bb08dea-daa7-59c0-b4bb-4d5d19c0cae1', 'Leader', 'ACTIVE', true),
  ('73da3155-fde0-5355-a8bd-ff62a69d3a27', 4, 4, '73da3155-fde0-5355-a8bd-ff62a69d3a27', 'Member', 'ACTIVE', true),
  ('39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 4, 4, '39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 'Member', 'ACTIVE', true),
  ('267081d6-6494-5e96-8b99-2c637976b26b', 4, 4, '267081d6-6494-5e96-8b99-2c637976b26b', 'Member', 'ACTIVE', true),
  ('4fc95afd-4b8a-5dbf-8abb-f9ca5010513f', 4, 4, '4fc95afd-4b8a-5dbf-8abb-f9ca5010513f', 'Member', 'ACTIVE', true),
  ('76f0a825-b223-5908-96ca-710a6e10ae6d', 5, 5, '76f0a825-b223-5908-96ca-710a6e10ae6d', 'Leader', 'ACTIVE', true),
  ('1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 5, 5, '1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 'Member', 'ACTIVE', true),
  ('71395a94-f2ca-51a3-8672-b75192f3e2af', 5, 5, '71395a94-f2ca-51a3-8672-b75192f3e2af', 'Member', 'ACTIVE', true),
  ('4bfefaed-8313-57d2-bc29-052c1d9e5385', 5, 5, '4bfefaed-8313-57d2-bc29-052c1d9e5385', 'Member', 'ACTIVE', true),
  ('0caafffe-4ca4-5521-924c-48d1b06607b1', 5, 5, '0caafffe-4ca4-5521-924c-48d1b06607b1', 'Member', 'ACTIVE', true),
  ('aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', 5, 5, 'aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', 'Member', 'ACTIVE', true),
  ('af7d24ac-519b-5d02-8607-1db817e5b740', 6, 6, 'af7d24ac-519b-5d02-8607-1db817e5b740', 'Leader', 'ACTIVE', true),
  ('a1984805-03ed-5349-aafb-9a048ffcb695', 6, 6, 'a1984805-03ed-5349-aafb-9a048ffcb695', 'Member', 'ACTIVE', true),
  ('39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 6, 6, '39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 'Member', 'ACTIVE', true),
  ('aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', 6, 6, 'aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', 'Member', 'ACTIVE', true),
  ('9ecfd5f6-000c-5147-8b5e-ad29d027716e', 6, 6, '9ecfd5f6-000c-5147-8b5e-ad29d027716e', 'Member', 'ACTIVE', true),
  ('c10e9968-a097-53e7-895b-8fd165e668da', 6, 6, 'c10e9968-a097-53e7-895b-8fd165e668da', 'Member', 'ACTIVE', true),
  ('1c464f97-f3eb-5bb6-9420-364f3b287166', 6, 6, '1c464f97-f3eb-5bb6-9420-364f3b287166', 'Member', 'ACTIVE', true),
  ('0caafffe-4ca4-5521-924c-48d1b06607b1', 7, 7, '0caafffe-4ca4-5521-924c-48d1b06607b1', 'Leader', 'ACTIVE', true),
  ('9ecfd5f6-000c-5147-8b5e-ad29d027716e', 7, 7, '9ecfd5f6-000c-5147-8b5e-ad29d027716e', 'Member', 'ACTIVE', true),
  ('784cb960-e53c-53a2-9662-7f5a6b46cb88', 7, 7, '784cb960-e53c-53a2-9662-7f5a6b46cb88', 'Member', 'ACTIVE', true),
  ('76f0a825-b223-5908-96ca-710a6e10ae6d', 7, 7, '76f0a825-b223-5908-96ca-710a6e10ae6d', 'Member', 'ACTIVE', true),
  ('5903b5cc-f44b-51e7-a88a-f5e1d49ee157', 7, 7, '5903b5cc-f44b-51e7-a88a-f5e1d49ee157', 'Member', 'ACTIVE', true),
  ('9cd3641e-d016-56ea-937f-6bc6272b0de3', 7, 7, '9cd3641e-d016-56ea-937f-6bc6272b0de3', 'Member', 'ACTIVE', true),
  ('33174d73-4ea1-5242-8602-c05aa878eb6e', 7, 7, '33174d73-4ea1-5242-8602-c05aa878eb6e', 'Member', 'ACTIVE', true),
  ('d9c50348-d82b-5cec-b29d-ea5e0bfab91f', 7, 7, 'd9c50348-d82b-5cec-b29d-ea5e0bfab91f', 'Member', 'ACTIVE', true),
  ('5903b5cc-f44b-51e7-a88a-f5e1d49ee157', 8, 8, '5903b5cc-f44b-51e7-a88a-f5e1d49ee157', 'Leader', 'ACTIVE', true),
  ('a1984805-03ed-5349-aafb-9a048ffcb695', 8, 8, 'a1984805-03ed-5349-aafb-9a048ffcb695', 'Member', 'ACTIVE', true),
  ('281fd5b3-39a5-5aea-b812-41750c5f6a15', 8, 8, '281fd5b3-39a5-5aea-b812-41750c5f6a15', 'Member', 'ACTIVE', true),
  ('73da3155-fde0-5355-a8bd-ff62a69d3a27', 8, 8, '73da3155-fde0-5355-a8bd-ff62a69d3a27', 'Member', 'ACTIVE', true),
  ('377b7fa0-0847-559f-ae18-8366ceca7390', 8, 8, '377b7fa0-0847-559f-ae18-8366ceca7390', 'Member', 'ACTIVE', true),
  ('281fd5b3-39a5-5aea-b812-41750c5f6a15', 9, 9, '281fd5b3-39a5-5aea-b812-41750c5f6a15', 'Leader', 'ACTIVE', true),
  ('1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 9, 9, '1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 'Member', 'ACTIVE', true),
  ('267081d6-6494-5e96-8b99-2c637976b26b', 9, 9, '267081d6-6494-5e96-8b99-2c637976b26b', 'Member', 'ACTIVE', true),
  ('9cd3641e-d016-56ea-937f-6bc6272b0de3', 9, 9, '9cd3641e-d016-56ea-937f-6bc6272b0de3', 'Member', 'ACTIVE', true),
  ('39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 9, 9, '39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 'Member', 'ACTIVE', true),
  ('af7d24ac-519b-5d02-8607-1db817e5b740', 9, 9, 'af7d24ac-519b-5d02-8607-1db817e5b740', 'Member', 'ACTIVE', true),
  ('d9c50348-d82b-5cec-b29d-ea5e0bfab91f', 10, 10, 'd9c50348-d82b-5cec-b29d-ea5e0bfab91f', 'Leader', 'ACTIVE', true),
  ('281fd5b3-39a5-5aea-b812-41750c5f6a15', 10, 10, '281fd5b3-39a5-5aea-b812-41750c5f6a15', 'Member', 'ACTIVE', true),
  ('9cd3641e-d016-56ea-937f-6bc6272b0de3', 10, 10, '9cd3641e-d016-56ea-937f-6bc6272b0de3', 'Member', 'ACTIVE', true),
  ('c10e9968-a097-53e7-895b-8fd165e668da', 10, 10, 'c10e9968-a097-53e7-895b-8fd165e668da', 'Member', 'ACTIVE', true),
  ('aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', 10, 10, 'aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', 'Member', 'ACTIVE', true),
  ('2c4cba41-bb1a-5a8d-a54b-db495fda167b', 10, 10, '2c4cba41-bb1a-5a8d-a54b-db495fda167b', 'Member', 'QUIT', true);

INSERT INTO hashtags (hashtag_id, name, normalized_name, use_cnt) VALUES
  (1, '기상', '기상', 2),
  (2, '착석', '착석', 2),
  (3, '코테', '코테', 2),
  (4, '영어', '영어', 2),
  (5, '자격증', '자격증', 1),
  (6, '헬스', '헬스', 0),
  (7, '토익', '토익', 2),
  (8, '알고리즘', '알고리즘', 2),
  (9, 'CS', 'cs', 3),
  (10, 'Java', 'java', 0),
  (11, 'Spring', 'spring', 0),
  (12, 'SQL', 'sql', 3),
  (13, '매일', '매일', 7),
  (14, '주말', '주말', 4);

INSERT INTO study_group_tags (mapping_id, group_id, hashtag_id) VALUES
  (1, 1, 1),
  (2, 1, 13),
  (3, 1, 14),
  (4, 2, 2),
  (5, 2, 13),
  (6, 2, 12),
  (7, 3, 3),
  (8, 3, 8),
  (9, 3, 9),
  (10, 4, 4),
  (11, 4, 7),
  (12, 4, 13),
  (13, 5, 5),
  (14, 5, 12),
  (15, 5, 14),
  (16, 6, 9),
  (17, 6, 13),
  (18, 6, 14),
  (19, 7, 1),
  (20, 7, 13),
  (21, 7, 14),
  (22, 8, 2),
  (23, 8, 13),
  (24, 8, 12),
  (25, 9, 3),
  (26, 9, 8),
  (27, 9, 9),
  (28, 10, 4),
  (29, 10, 7),
  (30, 10, 13);

INSERT INTO group_verification_methods (method_id, group_id, method_code) VALUES
  (1, 1, 'PHOTO'),
  (2, 1, 'GPS'),
  (3, 2, 'PHOTO'),
  (4, 2, 'CHECKLIST'),
  (5, 3, 'PHOTO'),
  (6, 3, 'GITHUB'),
  (7, 4, 'PHOTO'),
  (8, 5, 'PHOTO'),
  (9, 5, 'CHECKLIST'),
  (10, 6, 'PHOTO'),
  (11, 6, 'GPS'),
  (12, 7, 'PHOTO'),
  (13, 7, 'GPS'),
  (14, 8, 'PHOTO'),
  (15, 8, 'CHECKLIST'),
  (16, 9, 'PHOTO'),
  (17, 9, 'GITHUB'),
  (18, 10, 'PHOTO');

INSERT INTO group_verification_schedule (schedule_id, group_id, end_time, check_end_time, days_of_week) VALUES
  (1, 1, '23:59:00', '00:15:00', 127),
  (2, 2, '23:59:00', '00:15:00', 127),
  (3, 3, '23:59:00', '00:15:00', 127),
  (4, 4, '23:59:00', '00:15:00', 127),
  (5, 5, '23:59:00', '00:15:00', 127),
  (6, 6, '23:59:00', '00:15:00', 127),
  (7, 7, '23:59:00', '00:15:00', 127),
  (8, 8, '23:59:00', '00:15:00', 127),
  (9, 9, '23:59:00', '00:15:00', 127),
  (10, 10, '23:59:00', '00:15:00', 127);

INSERT INTO group_verification_frequency (frequency_id, group_id, unit, required_cnt) VALUES
  (1, 1, 'DAY', 1),
  (2, 2, 'DAY', 1),
  (3, 3, 'WEEK', 3),
  (4, 4, 'WEEK', 3),
  (5, 5, 'WEEK', 3),
  (6, 6, 'WEEK', 3),
  (7, 7, 'DAY', 1),
  (8, 8, 'DAY', 1),
  (9, 9, 'WEEK', 3),
  (10, 10, 'WEEK', 3);

INSERT INTO group_invitations (invite_id, group_id, invite_code, invite_token, expires_at, max_uses, used_cnt, revoked_at) VALUES
  (1, 3, 'INV033724', 'e188e75b1a6943c49d88ea1196f2e66f', '2026-02-18 23:59:59', 50, 0, NULL),
  (2, 6, 'INV067658', '95a8e40374194a4aa5a4ab6ec62b6132', '2026-02-18 23:59:59', 50, 0, NULL),
  (3, 9, 'INV098956', '27228d9882714567a21e279d08cc0ebd', '2026-02-18 23:59:59', 50, 0, NULL);


-- Align identity sequences after explicit ID inserts (important for app-side INSERTs)
SELECT setval(pg_get_serial_sequence('study_groups','group_id'), (SELECT COALESCE(MAX(group_id), 0) FROM study_groups));
SELECT setval(pg_get_serial_sequence('hashtags','hashtag_id'), (SELECT COALESCE(MAX(hashtag_id), 0) FROM hashtags));
SELECT setval(pg_get_serial_sequence('study_group_tags','mapping_id'), (SELECT COALESCE(MAX(mapping_id), 0) FROM study_group_tags));
SELECT setval(pg_get_serial_sequence('group_verification_methods','method_id'), (SELECT COALESCE(MAX(method_id), 0) FROM group_verification_methods));
SELECT setval(pg_get_serial_sequence('group_verification_schedule','schedule_id'), (SELECT COALESCE(MAX(schedule_id), 0) FROM group_verification_schedule));
SELECT setval(pg_get_serial_sequence('group_verification_frequency','frequency_id'), (SELECT COALESCE(MAX(frequency_id), 0) FROM group_verification_frequency));
SELECT setval(pg_get_serial_sequence('group_invitations','invite_id'), (SELECT COALESCE(MAX(invite_id), 0) FROM group_invitations));
-- -------------------------------------------------------------------
-- Additional seed: notice / inquiry / inquiry_comment
-- -------------------------------------------------------------------

-- Global notices
INSERT INTO notice (title, content, view_count) VALUES
  ('[공지] 체크메이트 베타 오픈', '베타 기간 동안 피드백 주시면 빠르게 개선하겠습니다. 버그 제보는 문의하기를 이용해 주세요.', 23),
  ('[공지] 인증 업로드 가이드', '사진/스크린샷 인증은 1~3장 업로드 가능하며, 용량 제한은 설정값을 따릅니다.', 11),
  ('[점검] 서버 점검 안내', '2026-01-20 02:00~03:00 (KST) 점검 예정입니다. 점검 중 일부 기능이 제한될 수 있습니다.', 7),
  ('[업데이트] GitHub 커밋 인증 개선', '코테 카테고리 스터디에서 커밋 인증이 더 안정적으로 동작하도록 개선했습니다.', 5),
  ('[공지] 커뮤니티 운영 원칙', '서로 존중하는 커뮤니티 문화를 위해 비방/혐오/도배 행위는 제재될 수 있습니다.', 18),
  ('[이벤트] 7일 연속 인증 뱃지', '연속 인증 이벤트가 시작되었습니다! 7일 연속 인증 시 뱃지를 획득할 수 있어요.', 9),
  ('[공지] 알림 설정 확인', '앱 푸시/이메일 알림 설정은 마이페이지에서 변경할 수 있습니다.', 4);

-- Inquiries (explicit inquiry_id for easy referencing by comments)
INSERT INTO inquiry (inquiry_id, user_id, title, content, status) VALUES
  (1,  '76f0a825-b223-5908-96ca-710a6e10ae6d', '로그인이 자꾸 풀려요', '앱을 켤 때마다 다시 로그인해야 합니다. 해결 방법이 있나요?', 'PENDING'),
  (2,  '281fd5b3-39a5-5aea-b812-41750c5f6a15', '초대코드가 만료됐다고 떠요', '친구가 준 초대코드가 만료됐다고 나옵니다. 다시 발급 가능한가요?', 'ANSWERED'),
  (3,  'ab937cea-8537-5b8c-98c9-bc3ebf7fb15c', '사진 인증 업로드 오류', '사진 선택 후 업로드 버튼을 누르면 실패합니다. 와이파이/데이터 모두 동일해요.', 'ANSWERED'),
  (4,  '9cd3641e-d016-56ea-937f-6bc6272b0de3', '계정 탈퇴는 어디서 하나요?', '마이페이지에서 탈퇴 메뉴를 못 찾겠습니다.', 'ANSWERED'),
  (5,  'aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481', '포인트 환불 문의', '포인트 상품을 잘못 구매했습니다. 취소/환불 가능할까요?', 'PENDING'),
  (6,  '39ecd285-e8ad-5665-9db6-e0f9d83d59ef', '스터디 알림이 안 와요', '인증 마감 알림이 오지 않습니다. 설정은 켜져 있어요.', 'ANSWERED'),
  (7,  '2c4cba41-bb1a-5a8d-a54b-db495fda167b', '시간대 설정이 KST로 고정인가요?', '해외에서 사용할 때 인증 시간이 이상하게 잡힙니다.', 'PENDING'),
  (8,  'b16ac12d-1684-5a4f-941f-92bb98e90a29', '닉네임 변경 제한', '닉네임을 바꾸려고 하는데 저장이 안 됩니다. 제한이 있나요?', 'ANSWERED'),
  (9,  '1e5a040c-3373-56c0-b49e-09e5ccc77ca4', 'GPS 인증 위치 등록', 'GPS 인증에서 위치 등록이 안 되거나 지도 로딩이 느립니다.', 'PENDING'),
  (10, '71395a94-f2ca-51a3-8672-b75192f3e2af', '체크리스트 인증 항목 추가', '체크리스트 인증에서 항목을 여러 개로 확장할 계획이 있나요?', 'ANSWERED'),
  (11, '5903b5cc-f44b-51e7-a88a-f5e1d49ee157', '스터디 정원이 꽉 찼는데 대기 기능 있나요?', '정원이 가득 찼을 때 대기 신청 기능이 있으면 좋겠습니다.', 'PENDING'),
  (12, '817d1b96-e913-52d5-a02b-83d6c3454f2d', '이메일 변경 문의', '가입 이메일을 바꾸고 싶습니다. 어디서 변경하나요?', 'ANSWERED');

-- Ensure inquiry identity sequence is aligned after explicit IDs
SELECT setval(pg_get_serial_sequence('inquiry','inquiry_id'), (SELECT COALESCE(MAX(inquiry_id), 0) FROM inquiry));


-- Ensure identity sequences are aligned after explicit IDs

-- Inquiry comments
-- NOTE: author_type values: 'ADMIN' or 'USER'
INSERT INTO inquiry_comment (inquiry_id, user_id, author_type, content) VALUES
  (2,  '281fd5b3-39a5-5aea-b812-41750c5f6a15', 'USER',  '초대코드는 어제 생성했고 오늘 접속했는데 만료로 표시됩니다.'),
  (2,  '267081d6-6494-5e96-8b99-2c637976b26b', 'ADMIN', '확인 감사합니다. 초대코드 만료시간 설정( expires_at )이 지난 경우입니다. 새 초대코드를 재발급해 드렸습니다.'),

  (3,  'ab937cea-8537-5b8c-98c9-bc3ebf7fb15c', 'USER',  '안드로이드에서 발생하고, 사진 2장을 올릴 때 자주 실패합니다.'),
  (3,  '267081d6-6494-5e96-8b99-2c637976b26b', 'ADMIN', '업로드 용량/확장자 제한에 걸렸을 가능성이 있습니다. 1장(5MB 이하)으로 테스트 부탁드리고, 재현 시 기기/OS 버전도 함께 남겨주세요.'),

  (4,  '9cd3641e-d016-56ea-937f-6bc6272b0de3', 'USER',  '설정 화면에서 찾았습니다. 혹시 탈퇴 후 재가입 제한이 있나요?'),
  (4,  '267081d6-6494-5e96-8b99-2c637976b26b', 'ADMIN', '탈퇴 즉시 데이터가 비활성화되며, 재가입은 가능하지만 일부 기록은 복구되지 않을 수 있습니다.'),

  (6,  '39ecd285-e8ad-5665-9db6-e0f9d83d59ef', 'USER',  'iOS에서 푸시 알림 권한은 허용 상태입니다.'),
  (6,  '267081d6-6494-5e96-8b99-2c637976b26b', 'ADMIN', '알림 토큰 갱신 문제일 수 있어 로그아웃 후 재로그인해 보시고, 계속되면 앱 재설치 후 확인 부탁드립니다.'),

  (8,  'b16ac12d-1684-5a4f-941f-92bb98e90a29', 'USER',  '욕설/특수문자 필터가 있는지 궁금합니다.'),
  (8,  '267081d6-6494-5e96-8b99-2c637976b26b', 'ADMIN', '닉네임은 2~12자, 일부 특수문자/금칙어는 제한됩니다. 제한 메시지가 안 뜨는 문제는 개선하겠습니다.'),

  (10, '71395a94-f2ca-51a3-8672-b75192f3e2af', 'USER',  '체크 항목을 5개 이상으로 늘리고 싶어요.'),
  (10, '267081d6-6494-5e96-8b99-2c637976b26b', 'ADMIN', '로드맵에 포함되어 있습니다. 우선 10개까지 지원하는 방향으로 검토 중입니다.'),

  (12, '817d1b96-e913-52d5-a02b-83d6c3454f2d', 'USER',  '현재 이메일을 더 이상 사용하지 못합니다.'),
  (12, '267081d6-6494-5e96-8b99-2c637976b26b', 'ADMIN', '보안상 이메일 변경은 본인 확인 후 처리됩니다. 고객센터 안내에 따라 인증 정보를 제출해 주세요.');


COMMIT;
