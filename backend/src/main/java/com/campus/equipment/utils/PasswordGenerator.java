package com.campus.equipment.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类 - 用于生成 BCrypt 加密密码
 * 运行 main 方法生成密码后，可以复制到 SQL 中使用
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 要加密的密码
        String password = "admin123";
        
        // 生成 BCrypt hash
        String encodedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt Hash: " + encodedPassword);
        System.out.println();
        System.out.println("SQL 更新语句:");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
    }
}
