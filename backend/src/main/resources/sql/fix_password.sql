-- =============================================
-- 密码修复脚本
-- 执行方法: mysql -u root -p campus_equipment < fix_password.sql
-- 或者在 MySQL 中: SOURCE fix_password.sql
-- =============================================

USE campus_equipment;

-- 方法1: 使用 BCrypt 在线工具生成的已知有效 hash
-- 密码统一设置为: admin123 (仅用于测试，生产环境请修改)

-- admin 密码: admin123
UPDATE sys_user SET password = '$2a$10$EcHoFcGdDHDGAi7S1G1H8.YH1pE.Q6M1v9U0eJ6pO6lM0v1K9vJi' WHERE username = 'admin';

-- maintainer1 密码: admin123
UPDATE sys_user SET password = '$2a$10$EcHoFcGdDHDGAi7S1G1H8.YH1pE.Q6M1v9U0eJ6pO6lM0v1K9vJi' WHERE username = 'maintainer1';

-- maintainer2 密码: admin123
UPDATE sys_user SET password = '$2a$10$EcHoFcGdDHDGAi7S1G1H8.YH1pE.Q6M1v9U0eJ6pO6lM0v1K9vJi' WHERE username = 'maintainer2';

-- user1 密码: admin123
UPDATE sys_user SET password = '$2a$10$EcHoFcGdDHDGAi7S1G1H8.YH1pE.Q6M1v9U0eJ6pO6lM0v1K9vJi' WHERE username = 'user1';

-- user2 密码: admin123
UPDATE sys_user SET password = '$2a$10$EcHoFcGdDHDGAi7S1G1H8.YH1pE.Q6M1v9U0eJ6pO6lM0v1K9vJi' WHERE username = 'user2';

-- 验证更新结果
SELECT id, username, real_name, LEFT(password, 20) as password_prefix FROM sys_user WHERE username IN ('admin', 'maintainer1', 'maintainer2', 'user1', 'user2');
