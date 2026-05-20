USE campus_equipment;

-- ----------------------------
-- 初始化角色数据
-- ----------------------------
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'ROLE_ADMIN', '系统超级管理员，拥有所有权限'),
(2, '维护员', 'ROLE_MAINTAINER', '设备维护人员，负责故障处理和设备维护'),
(3, '普通用户', 'ROLE_USER', '普通用户，可查看设备信息和上报故障');

-- ----------------------------
-- 初始化用户数据
-- 密码说明：
--   admin       -> Admin@2024
--   maintainer1 -> Mtn@2024
--   maintainer2 -> Tech@2024
--   user1       -> User@2024
--   user2       -> Teacher@2024
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `department`, `status`) VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 'admin@campus.edu.cn', '13800000001', '信息技术中心', 1),
(2, 'maintainer1', '$2a$10$VhmCfK1mfLrT3gYZvKxBxOGdqzW6m1L8P5R2mK1xXq1R5sH9vJ0i', '张维修', 'zhang@campus.edu.cn', '13800000002', '设备维护部', 1),
(3, 'maintainer2', '$2a$10$8xYbK1lL3rT4gAZwKxBxOGdqzW6m1L8P5R2mK1xXq1R5sH9vJ0i', '李技术', 'li@campus.edu.cn', '13800000003', '设备维护部', 1),
(4, 'user1', '$2a$10$7wZbM2kK4sU5hBYvJxCyPHneazW6m1L8P5R2mK1xXq1R5sH9vJ0i', '王同学', 'wang@campus.edu.cn', '13800000004', '计算机学院', 1),
(5, 'user2', '$2a$10$6aYcN3jJ5tV6iCZvKwDzQIOofazW6m1L8P5R2mK1xXq1R5sH9vJ0i', '赵老师', 'zhao@campus.edu.cn', '13800000005', '电子工程学院', 1);

-- ----------------------------
-- 初始化用户角色关联
-- ----------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin 拥有角色 ADMIN
(2, 2),  -- maintainer1 拥有角色 MAINTAINER
(3, 2),  -- maintainer2 拥有角色 MAINTAINER
(4, 3),  -- user1 拥有角色 USER
(5, 3);  -- user2 拥有角色 USER
