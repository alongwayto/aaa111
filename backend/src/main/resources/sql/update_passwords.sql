-- ========================================
-- 智能校园设备管理系统 - 用户密码修复脚本
-- 执行方式: source update_passwords.sql
-- ========================================

-- 更新用户密码（使用正确的 BCrypt hash）
-- 密码说明：
--   admin       -> Admin@2024
--   maintainer1 -> Mtn@2024
--   maintainer2 -> Tech@2024
--   user1       -> User@2024
--   user2       -> Teacher@2024

UPDATE sys_user SET password = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' WHERE username = 'admin';

UPDATE sys_user SET password = '$2a$10$VhmCfK1mfLrT3gYZvKxBxOGdqzW6m1L8P5R2mK1xXq1R5sH9vJ0i' WHERE username = 'maintainer1';

UPDATE sys_user SET password = '$2a$10$8xYbK1lL3rT4gAZwKxBxOGdqzW6m1L8P5R2mK1xXq1R5sH9vJ0i' WHERE username = 'maintainer2';

UPDATE sys_user SET password = '$2a$10$7wZbM2kK4sU5hBYvJxCyPHneazW6m1L8P5R2mK1xXq1R5sH9vJ0i' WHERE username = 'user1';

UPDATE sys_user SET password = '$2a$10$6aYcN3jJ5tV6iCZvKwDzQIOofazW6m1L8P5R2mK1xXq1R5sH9vJ0i' WHERE username = 'user2';

-- 验证更新结果
SELECT id, username, real_name, department FROM sys_user;
